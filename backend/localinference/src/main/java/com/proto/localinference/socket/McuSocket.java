package com.proto.localinference.socket;

import com.proto.localinference.dto.McuInstructions;
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
import java.util.Optional;
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

  private void handleClient(Socket con) {
    Optional<UUID> clientId = null;
    try {
      con.setSoTimeout(SO_TIMEOUT_MS); // 5 second read timeout
      BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));

      /* validate UUID as key code */
      clientId = socketService.validateMcuConnectionAndReturnClientId(reader);

      if (clientId.isEmpty() || service.getClient(clientId.get()).isEmpty()) {
        reject(writer);
        return;
      }

      System.out.println("Incoming id: " + clientId.get().toString());

      /* parse instructions */
      handlePayloadFromMcu(clientId.get(), reader, writer);

      con.setSoTimeout(0);
      connections.put(clientId.get(), writer);

      /* handle instruction details */

      while (!con.isClosed()) {
        clientId = socketService.validateMcuConnectionAndReturnClientId(reader);

        if (clientId.isEmpty() || service.getClient(clientId.get()).isEmpty()) {
          reject(writer);
          return;
        }

        System.out.println("Incoming id: " + clientId);

        /* parse instructions */
        handlePayloadFromMcu(clientId.get(), reader, writer);
      }
      System.out.println("Closed con");

    } catch (IOException e) {
      System.out.println(e.getMessage());
    } finally {
      if (clientId != null) connections.remove(clientId.get());
    }
  }

  private Optional<McuInstructions> handlePayloadFromMcu(
      UUID clientId, BufferedReader reader, BufferedWriter writer) throws IOException {

    /* TODO: Finish reading details, currently accepts only 512 bytes*/
    var optMsg = socketService.parseInstructionCodeAndDetails(clientId, reader, writer);

    /* then process payload */
    if (optMsg.isEmpty()) {
      reject(writer);
      return Optional.empty();
    }

    System.out.println(
        "Instruction {"
            + optMsg.get().instruction().toString()
            + "} and payload from "
            + clientId.toString()
            + ": "
            + optMsg.get().details());

    acknowledge(writer);
    writer.write("OK received " + optMsg.get().details().length() + " bytes");
    writer.newLine();
    writer.flush();

    return optMsg;
  }

  private void reject(BufferedWriter writer) throws IOException {
    writer.write("0");
    writer.newLine();
    writer.flush();
    writer.close();
  }

  private void acknowledge(BufferedWriter writer) throws IOException {
    writer.write("1");
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
