import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class ClientHandler implements Runnable{

  private Socket clientSocket;
  private String header = "";

  public ClientHandler(Socket socket) {
    this.clientSocket = socket;
  }

  public String getHeader() {
    return header;
  }

  public void run() {
    try {
      // Get the input and output streams of the client socket
      InputStream input = clientSocket.getInputStream();
      OutputStream output = clientSocket.getOutputStream();

      InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());

      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

      // Read the HTTP request from the client
      Scanner scanner = new Scanner(input);
      String line = bufferedReader.readLine();
      String[] lineArray = line.split(" ");
      String method = lineArray[0];
      String path = lineArray[1];
      String protocol = lineArray[2];
      if (path.equals("/") || path.equals("/index.html")) {
        try {
          String contentType = "text/html";
          byte[] data = Files.readAllBytes(Path.of("public/index.html"));
          String headerAsString = "HTTP/1.1 200 OK\r\n" +
              "Content-Length: " + data.length + "\r\n" +
              "Content-Type: " + contentType + "\r\n" +
              "\r\n";
          header = new String(headerAsString.getBytes(), StandardCharsets.UTF_8);
          output.write(("HTTP/1.1 200 OK\r\n" +
              "Content-Length: " + data.length + "\r\n" +
              "Content-Type: " + contentType + "\r\n" +
              "\r\n").getBytes());
          output.write(data);
          System.out.println("PRINTED FROM THE CLIENTHANDLER: \n" + header);
        } catch (IOException e) {
          output.write(("HTTP/1.1 404 Not Found\r\n" + "Content-Length: 0\r\n" +
              "\r\n").getBytes());
        }
      } else {
          try {
            //File file = new File("." + path);
            String contentType = "text/plain";
            if (path.endsWith(".html")) {
              contentType = "text/html";
            } else if (path.endsWith(".png")) {
              contentType = "image/png";
            }
            Path filePath = Path.of("public" + path);
            // checking for subfolders
            File file = filePath.toFile();
            if (file.isDirectory()) {
              contentType = "text/html";
              filePath = filePath.resolve("index.html");
            }
            byte[] data = Files.readAllBytes(Path.of(filePath.toUri()));
            String headerAsString = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + data.length + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "\r\n";
            header = new String(headerAsString.getBytes(), StandardCharsets.UTF_8);
            output.write(("HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + data.length + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "\r\n").getBytes());
            output.write(data);
            System.out.println("PRINTED FROM THE CLIENTHANDLER: \n" + header);
          } catch (IOException e) {
            output.write(("HTTP/1.1 404 Not Found\r\n" + "Content-Length: 0\r\n" +
                "\r\n").getBytes());
          }
        }
      // Close the client socket
      output.close();
      input.close();
      clientSocket.close();
      //System.out.println("\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}


