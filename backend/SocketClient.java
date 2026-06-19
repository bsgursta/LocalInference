import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Manual test client for McuSocket. Sends one request at a time and blocks until the corresponding
 * server response is fully read before moving on to the next
 *
 * <p>Covers: REGISTER, RECONNECT, NOTIFY, HEALTHCHECK
 */
public class SocketClient {

  private static final String HOST = "127.0.0.1";
  private static final int PORT = 9000;
  private static final int UUID_LENGTH = 36;

  private static final String MCU_UUID = "dd1d2e0c-504e-445c-8e0e-136e392ef4f3";
  private static final String RECONNECT_KEY = "34eb893b-64a9-464f-be33-ea22234fbb6a";

  // REGISTER or RECONNECT
  private static final boolean USE_RECONNECT = true;

  // How many NOTIFY/HEALTHCHECK requests to send after the initial handshake.
  private static final int REQUEST_COUNT = 10;

  private final BufferedReader reader;
  private final BufferedWriter writer;

  public static void main(String[] args) {
    try (Socket socket = new Socket(HOST, PORT)) {
      SocketClient client =
          new SocketClient(
              new BufferedReader(new InputStreamReader(socket.getInputStream())),
              new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));

      client.runHandshake();
      client.runRequestLoop();

    } catch (IOException e) {
      System.out.println("Connection failed: " + e.getMessage());
    }
  }

  public SocketClient(BufferedReader reader, BufferedWriter writer) {
    this.reader = reader;
    this.writer = writer;
  }

  // ================================================================
  // Handshake
  // ================================================================

  /**
   * Sends UUID + ':' + (REGISTER or RECONNECT), per {@link #USE_RECONNECT}, then blocks for the
   * single-char ack and the 36-char reconnect key the server returns on success.
   */
  private void runHandshake() throws IOException {
    System.out.println("--- Handshake: " + (USE_RECONNECT ? "RECONNECT" : "REGISTER") + " ---");

    writer.write(MCU_UUID);
    writer.write(':');

    if (USE_RECONNECT) {
      writer.write("RECONNECT");
      writer.newLine();
      writer.write(RECONNECT_KEY);
    } else {
      writer.write("REGISTER");
    }
    writer.newLine();
    writer.flush();

    char ack = readAck();
    System.out.println("Handshake ack: " + ack);

    if (ack != '0') {
      throw new IOException("Handshake rejected by server");
    }

    String newReconnectKey = readFixed(UUID_LENGTH);
    System.out.println("New reconnect key: " + newReconnectKey);
  }

  // ================================================================
  // request loop
  // ================================================================

  /**
   * Alternates NOTIFY (with a payload) and HEALTHCHECK (no payload), waiting for the ack after each
   * before sending the next
   */
  private void runRequestLoop() throws IOException {
    System.out.println("--- Request loop ---");

    for (int i = 1; i <= REQUEST_COUNT; i++) {
      boolean sendHealthcheck = i % 5 == 0;

      char ack = sendHealthcheck ? sendHealthcheck() : sendNotify("Incident #" + i);

      System.out.println(
          "["
              + i
              + "/"
              + REQUEST_COUNT
              + "] "
              + (sendHealthcheck ? "HEALTHCHECK" : "NOTIFY")
              + " -> ack: "
              + ack);

      if (ack != '0') {
        System.out.println("Server rejected request " + i + " - stopping.");
        break;
      }
    }
  }

  /** Sends NOTIFY + payload, returns the single-char ack. */
  private char sendNotify(String payload) throws IOException {
    writer.write("NOTIFY");
    writer.newLine();
    writer.write(payload);
    writer.newLine();
    writer.flush();
    return readAck();
  }

  /** Sends HEALTHCHECK (no payload), returns the single-char ack. */
  private char sendHealthcheck() throws IOException {
    writer.write("HEALTHCHECK");
    writer.newLine();
    writer.flush();
    return readAck();
  }

  // ================================================================
  // Read helpers
  // ================================================================

  private char readAck() throws IOException {
    int c = reader.read();
    if (c == -1) throw new IOException("Connection closed while waiting for ack");
    return (char) c;
  }

  private String readFixed(int length) throws IOException {
    char[] buf = new char[length];
    int read = reader.read(buf, 0, length);
    if (read != length) throw new IOException("Expected " + length + " chars, got " + read);
    return new String(buf);
  }
}
