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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

  public void setHeader(String header) {
    this.header = header;
  }

  public void setHeaderTest(String response, int contentLength, String contentType, String date) {
    String headerAsString = response +
        "Content-Length: " + contentLength + "\r\n" +
        "Content-Type: " + contentType + "\r\n" +
        "Date:" + date + "\r\n" +
        "\r\n";
    this.header = new String(headerAsString.getBytes(), StandardCharsets.UTF_8);
  }
  public void run() {
    try {
      // Get the output stream of the client socket
      OutputStream output = clientSocket.getOutputStream();
      InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      // Read the HTTP request from the client
      String line;
      StringBuilder reqBuilder = new StringBuilder();
      while ((line = bufferedReader.readLine()) != null) {
        reqBuilder.append(line).append("\r\n");
        if (line.isEmpty()) {
          break;
        }
      }
      String request = reqBuilder.toString();
      String[] lineArray = request.split(" ");
      String method = lineArray[0];
      String path = lineArray[1];
      String protocol = lineArray[2];
      LocalDateTime currentDateTime = LocalDateTime.now();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      String formattedDateTime = currentDateTime.format(formatter);
      if (path.equals("/") || path.equals("/index.html")) {
        String contentType = "text/html";
        byte[] data = Files.readAllBytes(Path.of("public/index.html"));
        try {
          //String contentType = "text/html";
          //byte[] data = Files.readAllBytes(Path.of("public/index.html"));
          setHeaderTest("HTTP/1.1 200 OK\r\n", data.length, contentType, formattedDateTime);
          output.write(("HTTP/1.1 200 OK\r\n" +
              "Content-Length: " + data.length + "\r\n" +
              "Content-Type: " + contentType + "\r\n" +
              "\r\n").getBytes());
          output.write(data);
        } catch (IOException e) {
          setHeaderTest("HTTP/1.1 404 Not Found\r\n", data.length, contentType, formattedDateTime);
          output.write(("HTTP/1.1 404 Not Found\r\n" + "Content-Length: 0\r\n" +
              "\r\n").getBytes());
        }
      } else {
        byte[] tempData = new byte[]{};
        try {
            //File file = new File("." + path);
            String contentType = "text/plain";
            if (path.endsWith(".html")) {
              contentType = "text/html";
            } else if (path.endsWith(".png")) {
              contentType = "image/png";
            }
            Path filePath = Path.of("public" + path);
            File file = filePath.toFile();
            if (file.isDirectory()) {
              contentType = "text/html";
              filePath = filePath.resolve("index.html");
            }
            byte[] data = Files.readAllBytes(Path.of(filePath.toUri()));
            tempData = data;
            setHeaderTest("HTTP/1.1 200 OK\r\n", data.length, contentType, formattedDateTime);
            output.write(("HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + data.length + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "\r\n").getBytes());
            output.write(data);
          } catch (IOException e) {
            setHeaderTest("HTTP/1.1 404 Not Found\r\n", tempData.length, "null", formattedDateTime);
            output.write(("HTTP/1.1 404 Not Found\r\n" + "Content-Length: 0\r\n" +
                "\r\n").getBytes());
          }
        }
      // Close the client socket
      //output.close();
      //input.close();
      //clientSocket.close();
      //bufferedReader.close();
      //inputStreamReader.close();
      //System.out.println("\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}


