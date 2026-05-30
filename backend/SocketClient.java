import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class SocketClient {

  public static void main(String[] args) {
    try (Socket socket = new Socket("127.0.0.1", 9000)) {

      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

      /* create device UUID */
      // HttpClient client = HttpClient.newHttpClient();
      // HttpRequest request =
      //     HttpRequest.newBuilder()
      //         .uri(URI.create("http://127.0.0.1:8080/mcu"))
      //         .POST(HttpRequest.BodyPublishers.ofString(""))
      //         .build();

      // HttpResponse<String> id = client.send(request, HttpResponse.BodyHandlers.ofString());
      String uuid = "dd1d2e0c-504e-445c-8e0e-136e392ef4f3";
      System.out.println(uuid);

      /* send message over socket, identifying self by uuid */
      String msg = "REGISTER";

      writer.write(uuid.toString() + '\n');
      writer.write(msg + '\n');
      writer.flush();

      for (int i = 1; i < 100; i++) {
        writer.write(uuid.toString());
        writer.newLine();
        writer.write("NOTIFY");
        writer.newLine();
        writer.write("Line " + (char) ((i % 10) + 'a'));
        writer.newLine();
        writer.flush();
      }

      /* read server response */
      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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
