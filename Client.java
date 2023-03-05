import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class Client implements Runnable{

  private Socket clientSocket;
  private String header = "";

  private String[] args;


  public Client(Socket socket, String[] args) {
    this.clientSocket = socket;
    this.args = args;
  }

  public String getHeader() {
    return header;
  }

  /**
   * Sets the header with the needed data for printing.
   *
   * @param response is the response code and the message.
   * @param contentLength is the length of the byte array.
   * @param contentType is the type of the requested content.
   * @param date is the current time and date.
   */
  public void setHeader(String response, int contentLength, String contentType, String date) {
    String headerAsString = response +
        "Content-Length: " + contentLength + "\r\n" +
        "Content-Type: " + contentType + "\r\n" +
        "Date:" + date + "\r\n" +
        "\r\n";
    this.header = new String(headerAsString.getBytes(), StandardCharsets.UTF_8);
  }

  /**
   * A run method started inside the main method when the client is accepted.
   */
  @Override
  public void run() {
    try {
      OutputStream output = clientSocket.getOutputStream();
      ArrayList<String> folders = new ArrayList<>();
      InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      String line;
      var error500 = Path.of(args[1] + "/500.html");
      var error400 = Path.of(args[1] + "/retro404.win98.html");
      var homepage = Path.of(args[1] + "/index.html");
      if ((line = bufferedReader.readLine()) != null) {
        String hostPort = bufferedReader.readLine();
        if (!(hostPort == null)) {
          String[] methodResourceVersion = line.split(" ");
          if (!methodResourceVersion[1].endsWith(".html") && !methodResourceVersion[1].endsWith(".png")
              && !methodResourceVersion[1].endsWith(".html/") && !methodResourceVersion[1].endsWith(".png/")) {
            if (methodResourceVersion[1].charAt(methodResourceVersion[1].length() - 1) == '/'
                || methodResourceVersion[1].charAt(methodResourceVersion[1].length() - 2) == '/') {
              folders.add(methodResourceVersion[1]);
            }
          }
          if (!methodResourceVersion[1].equals("/favicon.ico")) {
            System.out.println("Assigned a new client to a separate thread!");
            System.out.println(
                hostPort + "," + " Method:" + methodResourceVersion[0] + ", Path: " + methodResourceVersion[1]
                    + ", Version: " + methodResourceVersion[2]);
            if (checkIfFileExists(methodResourceVersion[1], args[1]) || folders.contains(methodResourceVersion[1])) {
              System.out.println("Requested file exists!");
              if (folders.contains(methodResourceVersion[1])) {
                System.out.println("Requested item is a folder.");
              } else {
                if (methodResourceVersion[1].endsWith(".html") || methodResourceVersion[1].endsWith(".html/")) {
                  System.out.println("Requested item is an html file.");
                } else if (methodResourceVersion[1].endsWith(".png") || methodResourceVersion[1].endsWith(".png/")) {
                  System.out.println("Requested item is an png file.");
                }
              }
            } else {
              System.out.println("Requested file does not exist!");
            }
          }
          String path = methodResourceVersion[1];
          LocalDateTime currentDateTime = LocalDateTime.now();
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
          String formattedDateTime = currentDateTime.format(formatter);
          byte[] data;
          String contentType = "text/html";
          if (path.equals("/") || path.equals("/index.html")) {
            data = Files.readAllBytes(homepage);
            try {
              setHeader("HTTP/1.1 200 OK\r\n", data.length, contentType, formattedDateTime);
              output.write(("HTTP/1.1 200 OK\r\n" +
                  "Content-Length: " + data.length + "\r\n" +
                  "Content-Type: " + contentType + "\r\n" +
                  "\r\n").getBytes());
              output.write(data);
            } catch (IOException e) {
              data = Files.readAllBytes(error400);
              setHeader("HTTP/1.1 404 Not Found\r\n", data.length, contentType, formattedDateTime);
              output.write(("HTTP/1.1 200 OK\r\n" +
                      "Content-Length: " + data.length + "\r\n" +
                      "Content-Type: " + contentType + "\r\n" +
                      "\r\n").getBytes());
              output.write(data);
            }
          } else if (path.equals("/redirect")) {
            try {
              // = Files.readAllBytes(Path.of(args[1] + "/abc"));
              data = Files.readAllBytes(homepage);
              setHeader("HTTP/1.1 302 Found\r\n", data.length, contentType, formattedDateTime);
              output.write(("""
                  HTTP/1.1 302 Found\r
                  Location: /index.html\r
                  \r
                  """).getBytes());
            } catch (IOException e) {
              data = Files.readAllBytes(error500);
              setHeader("HTTP/1.1 500 Internal Server Error\r\n", data.length, contentType, formattedDateTime);
              output.write(("HTTP/1.1 200 OK\r\n" +
                      "Content-Length: " + data.length + "\r\n" +
                      "Content-Type: " + contentType + "\r\n" +
                      "\r\n").getBytes());
              output.write(data);
            }
          } else {
            try {
              if (validatePath(path, args[1])) {
                data = Files.readAllBytes(error400);
              } else {
                Path filePath = Path.of(args[1] + path);
                File file = filePath.toFile();
                if (!file.exists()) {
                  data = Files.readAllBytes(error400);
                  setHeader("HTTP/1.1 404 Not Found\r\n", data.length, contentType, formattedDateTime);
                } else {
                  if (path.endsWith(".html")) {
                    contentType = "text/html";
                  } else if (path.endsWith(".png")) {
                    contentType = "image/png";
                  }
                  if (file.isDirectory()) {
                    contentType = "text/html";
                    filePath = filePath.resolve("index.html");
                  }
                  data = Files.readAllBytes(Path.of(filePath.toUri()));
                  setHeader("HTTP/1.1 200 OK\r\n", data.length, contentType, formattedDateTime);
                }

              }
              output.write(("HTTP/1.1 200 OK\r\n" +
                      "Content-Length: " + data.length + "\r\n" +
                      "Content-Type: " + contentType + "\r\n" +
                      "\r\n").getBytes());
              output.write(data);
            } catch (IOException e) {
              data = Files.readAllBytes(error500);
              setHeader("HTTP/1.1 500 Internal Server Error\r\n", data.length, contentType, formattedDateTime);
              output.write(("HTTP/1.1 200 OK\r\n" +
                      "Content-Length: " + data.length + "\r\n" +
                      "Content-Type: " + contentType + "\r\n" +
                      "\r\n").getBytes());
              output.write(data);
            }
          }
          InetAddress clientInetAddress = clientSocket.getInetAddress();
          int port = clientSocket.getPort();
          String header = getHeader();
          if (!Objects.equals(header, "")) {
            String[] headerArray = header.split("\r\n");
            String response = headerArray[0].substring(8);
            String dateTime = headerArray[3];
            String contentLength = headerArray[1];
            contentType = headerArray[2];
            InetAddress addr = InetAddress.getLocalHost();
            String hostname = addr.getHostName();
            clientSocket.close();
            if (!path.equals("/favicon.ico")) {
              System.out.println(
                  "Client: " + clientInetAddress + port + ", Version: " + methodResourceVersion[2]
                      + ", Response:" + response + ", " + dateTime + " \nServername:" + hostname + ", "
                      + contentLength + ", " + checkIfSocketIsClosed(clientSocket)
                      + ", " + contentType);
              System.out.println("\n");
            }
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  /**
   * A method to ensure that the requested path is in public directory.
   */
  private boolean validatePath(String path, String folderName) {
    Path publicPath = Paths.get(folderName).normalize();
    Path requestedPath = publicPath.resolve(path).normalize();
    return requestedPath.startsWith(publicPath);
  }

  /**
   * A method for checking the socket status.

   * @return is true or false.
   */
  private String checkIfSocketIsClosed(Socket socket) {
    if (socket.isClosed()) {
      return " Connection: closed";
    } else {
      return " Connection: open";
    }
  }

  /**
   * A method for checking if the file exists inside the public directory.
   *
   * @param fileName is the name of the file, or the resource.
   * @return is true or false, depending on the outcome.
   */
  private boolean checkIfFileExists(String fileName, String folderName) {
    if (fileName.charAt(fileName.length() -1) == '/') {
      if (fileName.length() != 1) {
        fileName = fileName.substring(0, fileName.length() - 1);
      }
    }
    String folderPath = fileName.substring(0, fileName.lastIndexOf('/'));
    File folder = new File(folderName + folderPath);
    File[] files = folder.listFiles();
    boolean doesTheFileExist = false;
    if (files != null) {
      for (File file : files) {
        String fileNameWithoutFolder = folderPath + "/" + file.getName();
        if (fileNameWithoutFolder.equals(fileName)) {
          doesTheFileExist = true;
          break;
        }
      }
    }
    return doesTheFileExist;
  }
}


