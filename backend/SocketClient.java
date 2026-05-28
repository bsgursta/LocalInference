import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class SocketClient {

  public static void main(String[] args) {
    try (Socket socket = new Socket("127.0.0.1", 9000)) {

      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

      /* create device UUID */
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create("http://127.0.0.1:8080/mcu"))
              .POST(HttpRequest.BodyPublishers.ofString(""))
              .build();

      HttpResponse<String> id = client.send(request, HttpResponse.BodyHandlers.ofString());
      String uuid = id.body().replace("\"", "");
      System.out.println(uuid);

      /* send message over socket, identifying self by uuid */
      String msg = "PING\nLmao just testing";
      writer.write(uuid.toString());
      writer.newLine();
      writer.write(msg);
      writer.newLine();
      writer.flush();

      /* read server response */
      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      // String statusLine = reader.readLine(); // "1" or "0"
      // String messageLine = reader.readLine(); // "OK received..."

      // if (statusLine != null) {
      //   boolean ok = statusLine.charAt(0) == '1';
      //   System.out.println("Server response: " + (ok ? "OK" : "NOT ALLOWED"));
      //   System.out.println("Server message: " + messageLine);
      // } else {
      //   System.out.println("No response from server");
      // }

      // while (!socket.isClosed()) {
      //   System.out.println(reader.readLine());
      // }

      Thread listener =
          new Thread(
              () -> {
                try {
                  String line;
                  while ((line = reader.readLine()) != null) {
                    System.out.println("Server: " + line);
                  }
                } catch (IOException e) {
                  System.out.println("Connection closed: " + e.getMessage());
                }
              });
      listener.setDaemon(true);
      listener.start();

      // main thread blocks here — type messages to send, "quit" to exit
      Scanner scanner = new Scanner(System.in);
      while (scanner.hasNextLine()) {
        String input = scanner.nextLine();
        if ("quit".equalsIgnoreCase(input)) break;
        writer.write(input);
        writer.newLine();
        writer.flush();
      }
      scanner.close();

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
