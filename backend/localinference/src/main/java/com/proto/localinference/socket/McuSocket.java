package com.proto.localinference.socket;

import com.proto.localinference.dto.McuInstructions;
import com.proto.localinference.exceptions.InvalidSocketConnectionException;
import com.proto.localinference.services.McuService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class McuSocket {

  @Value(value = "${socket.port}")
  private int PORT;

  /*
  consider this if mcu can handle encryption

  SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
  server = factory.createServerSocket(PORT);
  */

  private static final int CHUNK_SIZE = 512; // max chars per payload read
  private static final int SO_TIMEOUT_MS = 5000; // drop connection if silent for 5s

  private final ConcurrentHashMap<UUID, BufferedWriter> connections = new ConcurrentHashMap<>();

  private ServerSocket server;
  private SocketService socketService;
  private McuService service;

  private ExecutorService acceptor = Executors.newSingleThreadExecutor();
  private ExecutorService handlers =
      new ThreadPoolExecutor(
          1,
          2, // core, max threads
          60L,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<>(100), // max 100 queued connections
          new ThreadPoolExecutor.AbortPolicy() // reject beyond that
          );

  public McuSocket(SocketService socketService, McuService service) {
    this.socketService = socketService;
    this.service = service;
  }

  @PostConstruct
  public void start() {
    acceptor.submit(this::listen);
  }

  @PreDestroy
  public void stop() throws IOException {
    server.close();
    acceptor.shutdown();
    handlers.shutdown();
    // close remaining living connections
    connections
        .values()
        .forEach(
            w -> {
              try {
                w.close();
              } catch (IOException ignore) {
              }
            });
  }

  private void listen() {
    try {
      server = new ServerSocket(PORT);

      while (!server.isClosed()) {
        Socket con = server.accept();
        handlers.submit(() -> handleClient(con));
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * This is a new socket connection that is made. Incoming con must ALWAYS pass the following
   * param: <br>
   * {@code {UUID}}\n{@code REGISTER_CON}\n <br>
   * From then on, MCU may make any kind of request as long as it follows the pattern of:<br>
   * {@code {UUID}}\n{@link McuOptions @McuOptions}\n{@code {PAYLOAD}}\n
   *
   * @param con
   */
  private void handleClient(Socket con) {
    UUID clientId = null;
    try {
      con.setSoTimeout(SO_TIMEOUT_MS); // 5 second to verify identity else close con
      System.out.println("inc con from " + con.getInetAddress().getHostAddress());

      BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));

      /* validate UUID as key code */
      clientId = socketService.validateMcuConnectionAndReturnClientIdOrRejectCon(reader, writer);
      /* read register McuOptions enum */
      McuInstructions instructions = readInstructions(reader, writer);
      if (instructions.instruction() != McuOptions.REGISTER
          && instructions.instruction() != McuOptions.REREGISTER) socketService.reject(writer);

      con.setSoTimeout(0);
      connections.put(clientId, writer);
      acknowledge(writer);

      /* handle other instructions */

      while (!con.isClosed()) {
        clientId = socketService.validateMcuConnectionAndReturnClientIdOrRejectCon(reader, writer);
        instructions = readInstructions(reader, writer);

        System.out.println(
            "Client "
                + clientId.toString()
                + " requested "
                + instructions.instruction().toString()
                + (instructions.details() == null ? "" : " with args: " + instructions.details()));

        acknowledge(writer);

        // /* parse instructions */
        // handlePayloadFromMcu(clientId, reader, writer);
      }
      System.out.println("finished loop");

    } catch (SocketException e) {
      System.out.println("Socket timed out or failed to access socket");
    } catch (IOException e) {
      System.out.println(e.getMessage() + " IO");
    } catch (InvalidSocketConnectionException e) {
      System.out.println(e.getMessage() + " socket");
    } catch (IllegalArgumentException e) {
      System.out.println("Invalid formatting");
    } finally {
      connections.remove(clientId);
      System.out.println("Closed con");
    }
  }

  /**
   * After verifying identity of client, read the next line which should contain a {@link }
   *
   * @param reader
   * @param writer
   * @return
   * @throws IOException
   * @throws IllegalArgumentException
   */
  private McuInstructions readInstructions(BufferedReader reader, BufferedWriter writer)
      throws IOException, IllegalArgumentException, InvalidSocketConnectionException {

    StringBuilder sBuilder = new StringBuilder();
    sBuilder.append(reader.readLine());
    String opt = sBuilder.toString();
    if ("null".equals(opt)) throw new InvalidSocketConnectionException("No McuOptions provided");

    McuOptions option = McuOptions.valueOf(opt);
    System.out.println("provided opt: " + opt);

    /* read McuOptions and see if need to read more of the payload */
    if (!doesRequirePayloadProcessing(option)) return new McuInstructions(option, "");

    // sBuilder.setLength(0); /* does jvm collect this? */

    /* */
    String payload = reader.readLine();
    if (payload == null || "".equals(payload))
      throw new InvalidSocketConnectionException("Required payload not provided");
    return new McuInstructions(option, payload);
  }

  private boolean doesRequirePayloadProcessing(McuOptions options)
      throws InvalidSocketConnectionException {
    switch (options) {
      case McuOptions.NOTIFY:
        return true;
      case McuOptions.REGISTER:
        return false;
      case McuOptions.REREGISTER:
        return true;
      case McuOptions.PING:
        return false;
      case McuOptions.UPDATE:
        return true;
      case McuOptions.NIL:
        return false;
      default:
        throw new InvalidSocketConnectionException("Invalid option provided");
    }
  }

  private void acknowledge(BufferedWriter writer) throws IOException {
    writer.write("0");
    writer.newLine();
    writer.flush();
  }

  private boolean send(UUID clientId, String message) {
    /* send info to client */
    BufferedWriter writer = connections.get(clientId);
    if (writer == null) return false;

    try {
      writer.write(message);
      writer.newLine();
      writer.flush();
      return true;
    } catch (IOException e) {
      connections.remove(clientId);
      System.out.println("Failed to send message to client " + clientId.toString());
      return false;
    }
  }

  public boolean isConnected(UUID clientId) {
    if (!connections.containsKey(clientId)) return false;

    return send(clientId, "PING");
  }
}
