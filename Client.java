import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Client implements Runnable{

  private Socket clientSocket;
  private String header = "";

  private BufferedReader bufferedReader;

  public Client(Socket socket, BufferedReader bufferedReader) {
    this.clientSocket = socket;
    this.bufferedReader = bufferedReader;
  }

  public String getHeader() {
    return header;
  }

  public void setHeader(String response, int contentLength, String contentType, String date) {
    String headerAsString = response +
        "Content-Length: " + contentLength + "\r\n" +
        "Content-Type: " + contentType + "\r\n" +
        "Date:" + date + "\r\n" +
        "\r\n";
    this.header = new String(headerAsString.getBytes(), StandardCharsets.UTF_8);
  }
  public void run() {
    try {
      OutputStream output = clientSocket.getOutputStream();
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
      String path = lineArray[1];
      LocalDateTime currentDateTime = LocalDateTime.now();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      String formattedDateTime = currentDateTime.format(formatter);
      byte[] tempData = new byte[]{};
      if (path.equals("/") || path.equals("/index.html")) {
        String contentType = "text/html";
        byte[] data = Files.readAllBytes(Path.of("public/index.html"));
        try {
          setHeader("HTTP/1.1 200 OK\r\n", data.length, contentType, formattedDateTime);
          output.write(("HTTP/1.1 200 OK\r\n" +
              "Content-Length: " + data.length + "\r\n" +
              "Content-Type: " + contentType + "\r\n" +
              "\r\n").getBytes());
          output.write(data);
        } catch (IOException e) {
          setHeader("HTTP/1.1 404 Not Found\r\n", data.length, contentType, formattedDateTime);
          output.write(("""
              HTTP/1.1 404 Not Found\r
              Content-Length: 0\r
              \r
              """).getBytes());
        }
      } else if (path.equals("/redirect")) {
        try {
          setHeader("HTTP/1.1 302 Found\r\n", tempData.length, "null", formattedDateTime);
          output.write(("""
              HTTP/1.1 302 Found\r
              Location: /a\r
              \r
              """).getBytes());
        } catch (IOException e) {
          setHeader("HTTP/1.1 500 Internal Server Error\r\n", tempData.length, "null", formattedDateTime);
          output.write(("""
              HTTP/1.1 500 Internal Server Error\r
              Content-Length: 0\r
              \r
              """).getBytes());
        }
      }
      else {
        Path filePath = Path.of("public" + path);
        File file = filePath.toFile();
        try {
            if (!file.exists()) {
              setHeader("HTTP/1.1 404 Not Found\r\n", tempData.length, "null", formattedDateTime);
              output.write(("""
                  HTTP/1.1 404 Not Found\r
                  Content-Length: 0\r
                  \r
                  """).getBytes());
            } else {
              String contentType = "text/plain";
              if (path.endsWith(".html")) {
                contentType = "text/html";
              } else if (path.endsWith(".png")) {
                contentType = "image/png";
              }
              if (file.isDirectory()) {
                contentType = "text/html";
                filePath = filePath.resolve("index.html");
              }
              byte[] data = Files.readAllBytes(Path.of(filePath.toUri()));
              tempData = data;
              setHeader("HTTP/1.1 200 OK\r\n", data.length, contentType, formattedDateTime);
              output.write(("HTTP/1.1 200 OK\r\n" +
                  "Content-Length: " + data.length + "\r\n" +
                  "Content-Type: " + contentType + "\r\n" +
                  "\r\n").getBytes());
              output.write(data);
            }
          } catch (IOException e) {
            setHeader("HTTP/1.1 500 Internal Server Error\r\n", tempData.length, "null", formattedDateTime);
          output.write(("""
              HTTP/1.1 500 Internal Server Error\r
              Content-Length: 0\r
              \r
              """).getBytes());
          }
        }
      //clientSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}


