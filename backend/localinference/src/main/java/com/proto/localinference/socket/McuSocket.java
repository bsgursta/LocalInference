package com.proto.localinference.socket;

import com.proto.localinference.dto.McuArg;
import com.proto.localinference.exceptions.InvalidSocketConnectionException;
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

  private static final int SO_TIMEOUT_MS = 5000; // drop connection if silent for 5s

  private final ConcurrentHashMap<UUID, BufferedWriter> connections = new ConcurrentHashMap<>();

  private ServerSocket server;
  private SocketService service;

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

  public McuSocket(SocketService socketService) {
    this.service = socketService;
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

  public boolean isConnected(UUID clientId) {
    return !connections.containsKey(clientId) ? false : send(clientId, "PING");
  }

  /**
   * This is a new socket connection that is made. Incoming con must ALWAYS pass the following
   * param: <br>
   * {@code UUID}:{@code REGISTER_CON}\n <br>
   * From then on, MCU may make any kind of request as long as it follows the pattern of:<br>
   * {@code {UUID}}\n{@link McuOption @McuOptions}\n{@code {PAYLOAD}}\n
   *
   * @param con
   */
  private void handleClient(Socket con) {
    UUID clientId = null;
    try {
      con.setSoTimeout(SO_TIMEOUT_MS); // 5 second to verify identity else close con
      System.out.println("inc con from " + con.getRemoteSocketAddress().toString());

      BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));

      /* read line 1 for uuid, then read line 2 & 3 for args and context */
      clientId = service.validateMcuConnectionAndReturnClientIdOrRejectCon(reader, writer);
      McuArg arg = service.readInstructionsAndPayloadIfAny(reader, writer);

      if (arg.instruction() != McuOption.REGISTER && arg.instruction() != McuOption.REREGISTER) {
        service.reject(writer);
        throw new InvalidSocketConnectionException(
            InvalidSocketConnectionException.SocketErrorOption.ImproperSequenceOfEvents);
      }

      con.setSoTimeout(0);
      connections.put(clientId, writer);
      service.acknowledge(writer);

      /* handle other instructions */
      while (!con.isClosed()) {
        clientId = service.validateMcuConnectionAndReturnClientIdOrRejectCon(reader, writer);
        arg = service.readInstructionsAndPayloadIfAny(reader, writer);

        System.out.println(
            "Client "
                + clientId.toString()
                + " requested "
                + arg.instruction().toString()
                + (arg.details() == null ? "" : " with args: " + arg.details()));

        service.acknowledge(writer);
      }
      System.out.println("finished loop");

    } catch (InvalidSocketConnectionException e) {
      System.out.println(e.getMessage());
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    } catch (SocketException e) {
      System.out.println(e.getMessage());
    } catch (IOException e) {
      System.out.println(e.getMessage());
    } finally {
      connections.remove(clientId);
      System.out.println("Con was closed");
    }
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
}
