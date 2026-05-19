import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SocketClient {

  public static void main(String[] args) {
    try (Socket socket = new Socket("127.0.0.1", 9000)) {

      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

      /* create device UUID */
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create("http://127.0.0.1:8080/mcu/111"))
              .POST(HttpRequest.BodyPublishers.ofString(""))
              .build();

      HttpResponse<String> id = client.send(request, HttpResponse.BodyHandlers.ofString());
      String uuid = id.body().replace("\"", "");
      System.out.println(uuid);

      /* send message over socket, identifying self by uuid */
      String msg = "How are yopu server?";
      writer.write(uuid);
      writer.newLine();
      writer.write(msg);
      writer.newLine();
      writer.flush();

      /* read server response */
      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      String response = reader.readAllAsString();
      if (response.length() > 0) {
        boolean b = response.charAt(0) == 0 ? false : true;
        System.out.println("Server response: " + (b ? "OK" : "NOT ALLOWED"));
        System.out.println("Server message: " + response);

      } else {
        System.out.println("Failed to get proper response from server");
      }

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
