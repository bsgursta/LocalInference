package com.proto.localinference.socket;

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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class McuSocket {

  @Value(value = "${socket.port}")
  private int PORT;

  private ServerSocket server;
  private ExecutorService acceptor = Executors.newSingleThreadExecutor();
  private ExecutorService handlers = Executors.newCachedThreadPool();
  private McuService service;

  public McuSocket(McuService service) {
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
    try (con) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));

      /* print whats in the buffer */
      String idLine = reader.readLine();
      String payloadLine = reader.readLine();
      UUID clientId = UUID.fromString(idLine.trim());

      System.out.println("Incoming id: " + idLine);
      System.out.println("Incoming message: " + payloadLine);

      if (service.getClient(clientId) != null) {
        /* key is a UUID from UUID.randomUUID() and should be the FIRST LINE as the auth */
        writer.write(1);
        writer.newLine();
        writer.write(
            "OK message was received of len "
                + (idLine.length() + payloadLine.length())
                + " bytes");
        writer.newLine();
        writer.flush();

      } else {
        /* raise error */
        writer.write(0);
        writer.newLine();
        writer.flush();
      }

    } catch (IOException e) {
      /* err1: failed to uuid-ify -> autoreject */
      System.out.println(e.getMessage());
    }
  }
}
