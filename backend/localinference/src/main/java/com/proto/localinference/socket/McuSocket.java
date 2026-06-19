package com.proto.localinference.socket;

import com.proto.localinference.dto.McuArgRecord;
import com.proto.localinference.dto.RegisterResult;
import com.proto.localinference.exceptions.InvalidSocketRequestException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
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

  private static final int SO_TIMEOUT_MS = 7500;
  private static final int SO_HEALTHCHECK_TIMEOUT_MS = 45000;

  /** Live writer handles, keyed by MCU UUID. */
  private final ConcurrentHashMap<UUID, BufferedWriter> connections = new ConcurrentHashMap<>();

  /**
   * Per-connection queue of server-initiated push packets. Populated via {@link #pushToMcu};
   * drained after each MCU request is acked.
   */
  private final ConcurrentHashMap<UUID, BlockingQueue<McuPushPacket>> pushQueues =
      new ConcurrentHashMap<>();

  private ServerSocket server;
  private final SocketService service;

  private final ExecutorService acceptor = Executors.newSingleThreadExecutor();
  private final ExecutorService handlers =
      new ThreadPoolExecutor(
          1,
          2,
          60L,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<>(100),
          new ThreadPoolExecutor.AbortPolicy());

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

  // ================================================================
  // Socket loop
  // ================================================================

  private void listen() {
    try {
      server = new ServerSocket(PORT);
      while (!server.isClosed()) {
        Socket con = server.accept();
        handlers.submit(() -> handleClient(con));
      }
    } catch (Exception e) {
      System.out.println("Socket listener error: " + e.getMessage());
    }
  }

  /**
   * Full lifecycle of one MCU connection.
   *
   * <h3>Handshake (once, under SO_TIMEOUT):</h3>
   *
   * <pre>
   *   MCU -> SRV:  UUID:                    (36-char UUID + ':' delimiter)
   *   MCU -> SRV:  REGISTER\n               (first-time connect)
   *               or RECONNECT\n
   *                  RECONNECT_KEY\n        (subsequent connects)
   *   SRV -> MCU:  '0'                      (single char, no newline)
   *   SRV -> MCU:  NEW_RECONNECT_KEY        (36 chars, no newline; MCU reads fixed 36)
   * </pre>
   *
   * <h3>Per-message loop (MCU-initiated):</h3>
   *
   * <pre>
   *   MCU -> SRV:  COMMAND\n
   *   MCU -> SRV:  PAYLOAD\n                (only if command requires it)
   *   SRV -> MCU:  '0' or '1'              (single char, no newline)
   *   [server push packets, if any]
   * </pre>
   */
  private void handleClient(Socket con) {
    UUID clientId = null;
    BufferedReader reader = null;
    BufferedWriter writer = null;

    try {
      con.setSoTimeout(SO_TIMEOUT_MS);
      System.out.println("Incoming connection from " + con.getRemoteSocketAddress());

      reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));

      // Handshake
      clientId = service.parseUuid(reader);
      McuArgRecord handshakeArg = service.parseCommandAndPayload(reader);

      HandshakeResult handshake =
          processAuthentication(clientId, handshakeArg, con.getInetAddress(), writer);

      con.setSoTimeout(SO_HEALTHCHECK_TIMEOUT_MS);
      connections.put(clientId, writer);
      pushQueues.put(clientId, new LinkedBlockingQueue<>());

      acknowledge(writer);
      writer.write(handshake.reconnectKey().toString()); /* UUID len */
      writer.flush();

      System.out.println("Client " + clientId + " successfully authenticated");

      // Main loop
      while (!con.isClosed()) {
        McuArgRecord arg = service.parseCommandAndPayload(reader);

        System.out.println(
            "Client "
                + clientId
                + " -> "
                + arg.command()
                + (arg.details().isEmpty() ? " [no payload]" : " [" + arg.details() + "]"));

        handleCommand(clientId, arg, writer);
        flushPendingPushes(clientId, reader, writer);
      }

    } catch (Exception e) {
      System.out.println("Connection error [" + clientId + "]: " + e.getMessage());
      try {
        if (writer != null) reject(writer);
      } catch (IOException ignore) {
        System.out.println("Failed to send rejection to client");
      }
    } finally {
      if (clientId != null) {
        connections.remove(clientId);
        pushQueues.remove(clientId);
      }
      try {
        con.close();
      } catch (IOException e) {
        System.out.println("Failed to close socket for " + clientId);
      }
      System.out.println("Connection closed: " + clientId);
    }
  }

  /**
   * Hook for first-ever-connection behavior (e.g. push an initial UPDATE with the latest model,
   * send a welcome NOTIFY, log a provisioning event, etc).
   *
   * <p>Called only after the connection is fully registered in {@code connections} / {@code
   * pushQueues} and the handshake ack has been sent, so any push enqueued here is safe to land on
   * the very next iteration of the main loop.
   *
   * <p>TODO: implement first-connection behavior.
   */
  private void onFirstConnection(UUID clientId) {
    System.out.println("Client " + clientId + " connected for the first time");
    // TODO: e.g. pushToMcu(clientId, McuCommand.UPDATE, currentModelPayload);
  }

  /** Handles a single MCU-initiated command and sends exactly one ack. */
  private void handleCommand(UUID clientId, McuArgRecord arg, BufferedWriter writer)
      throws IOException, InvalidSocketRequestException {
    switch (arg.command()) {
      case REGISTER, RECONNECT -> {
        // Only valid during handshake
        throw new InvalidSocketRequestException(
            InvalidSocketRequestException.SocketErrorOption.ImproperSequenceOfEvents);
      }
      case GPS -> {
        service.persistGpsPosition(clientId, arg.details());
        acknowledge(writer);
      }
      case UPDATE -> {
        // Server-initiated only - MCU must not send these
        throw new InvalidSocketRequestException(
            InvalidSocketRequestException.SocketErrorOption.ImproperSequenceOfEvents);
      }
      case NOTIFY -> {
        // TODO: process incident notification (arg.details() carries the payload)
        acknowledge(writer);
      }
      case HEALTHCHECK -> {
        acknowledge(writer);
      }
      case AUDIO_STREAM -> {
        // TODO: handle audio stream chunk (arg.details() carries the payload)
        acknowledge(writer);
      }
      default ->
          throw new InvalidSocketRequestException(
              InvalidSocketRequestException.SocketErrorOption.ImproperSequenceOfEvents);
    }
  }

  /**
   * Drains the push queue for this client, sending each pending packet in order. An IOException
   * from any send propagates up to kill the connection.
   */
  private void flushPendingPushes(UUID clientId, BufferedReader reader, BufferedWriter writer)
      throws IOException {
    BlockingQueue<McuPushPacket> queue = pushQueues.get(clientId);
    if (queue == null || queue.isEmpty()) return;

    McuPushPacket push;
    while ((push = queue.poll()) != null) {
      sendPush(clientId, push, reader, writer);
    }
  }

  /**
   * Sends a single server-initiated push and waits for the MCU's single-char ack.
   *
   * <h3>Wire format (server → MCU):</h3>
   *
   * <pre>
   *   INC\n
   *   COMMAND\n
   *   LENGTH\n       (decimal char count of payload; MCU readLines this to get the number)
   *   [PAYLOAD]      (exactly LENGTH chars, no terminator - only present when LENGTH > 0)
   * </pre>
   *
   * <h3>MCU response:</h3>
   *
   * <pre>
   *   '0' or '1'     (single char, no newline)
   * </pre>
   */
  private void sendPush(
      UUID clientId, McuPushPacket push, BufferedReader reader, BufferedWriter writer)
      throws IOException {
    String payload = push.payload() != null ? push.payload() : "";
    int payloadLength = payload.length();

    writer.write("INC");
    writer.newLine();
    writer.write(push.command().name());
    writer.newLine();
    writer.write(String.valueOf(payloadLength));
    writer.newLine();
    if (payloadLength > 0) {
      writer.write(payload);
    }
    writer.flush();

    int mcuAck = reader.read();
    if (mcuAck != '0') {
      System.out.println("MCU " + clientId + " rejected push: " + push.command());
    }
  }

  // ================================================================
  // Handshake helpers
  // ================================================================

  /** Reconnect key plus whether this handshake was the MCU's first-ever connection. */
  private record HandshakeResult(UUID reconnectKey, boolean firstConnection) {}

  private HandshakeResult processAuthentication(
      UUID clientId, McuArgRecord arg, InetAddress ipAddress, BufferedWriter writer)
      throws IOException {
    return switch (arg.command()) {
      case REGISTER -> {
        RegisterResult result =
            service
                .processRegister(clientId, ipAddress)
                .orElseThrow(
                    () ->
                        new InvalidSocketRequestException(
                            InvalidSocketRequestException.SocketErrorOption
                                .ImproperSequenceOfEvents));
        yield new HandshakeResult(result.reconnectKey(), result.firstConnection());
      }
      case RECONNECT -> {
        UUID reconnectKey =
            service
                .processReconnect(clientId, arg.details(), ipAddress)
                .orElseThrow(
                    () ->
                        new InvalidSocketRequestException(
                            InvalidSocketRequestException.SocketErrorOption
                                .ImproperSequenceOfEvents));
        yield new HandshakeResult(reconnectKey, false);
      }
      default -> {
        throw new InvalidSocketRequestException(
            InvalidSocketRequestException.SocketErrorOption.ImproperSequenceOfEvents);
      }
    };
  }

  private boolean send(UUID clientId, String message) {
    BufferedWriter writer = connections.get(clientId);
    if (writer == null) return false;
    try {
      writer.write(message);
      writer.flush();
      return true;
    } catch (IOException e) {
      connections.remove(clientId);
      System.out.println("Failed to send to client " + clientId);
      return false;
    }
  }

  // ===================================================
  // Ack / reject primitives
  // ===================================================

  private void acknowledge(BufferedWriter writer) throws IOException {
    if (writer == null) return;
    writer.write('0');
    writer.flush();
  }

  private void reject(BufferedWriter writer) throws IOException {
    if (writer == null) return;
    writer.write('1');
    writer.flush();
  }

  // ================================================================
  // Public API - called by server-side code (controllers, services)
  // ================================================================

  /** Returns true if the MCU is connected and the socket is alive. */
  public boolean isConnected(UUID clientId) {
    return connections.containsKey(clientId) && send(clientId, "PING");
  }

  /**
   * Enqueues a server-initiated command for delivery to the specified MCU. Delivered after the
   * server acks the MCU's next message. Returns false if the MCU is not currently connected.
   */
  public boolean pushToMcu(UUID clientId, McuCommand command, String payload) {
    BlockingQueue<McuPushPacket> queue = pushQueues.get(clientId);
    if (queue == null) return false;
    return queue.offer(new McuPushPacket(command, payload));
  }
}
