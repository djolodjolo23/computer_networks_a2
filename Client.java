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


  public Client(Socket socket) {
    this.clientSocket = socket;
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
      ArrayList<String> folders = new ArrayList<>();
      InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      String line;
      // code below added for triggering the 500 internal error.
      /**
       try {
         Object obj = null;
         obj.toString();
       } catch (Exception e) {
         e.printStackTrace();
         setHeader("HTTP/1.1 500 Internal Server Error\r\n", 0, "null", "null");
         output.write(("""
                  HTTP/1.1 500 Internal Server Error\r
                  Content-Length: 0\r
                  \r
                  """).getBytes());
       }
       */
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
            System.out.println("Assigned a new client to a separate thread!");
            System.out.println(
                hostPort + "," + " Method:" + methodResourceVersion[0] + ", Path: " + methodResourceVersion[1]
                    + ", Version: " + methodResourceVersion[2]);
            if (checkIfFileExists(methodResourceVersion[1])|| folders.contains(methodResourceVersion[1])) {
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
          String path = methodResourceVersion[1];
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
                  Location: /index.html\r
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
          } else {
            try {
              if (validatePath(path)) {
                output.write(("HTTP/1.1 404 Not Found\r\n" + "Content-Length: 0\r\n" +
                    "\r\n").getBytes());
              } else {
                Path filePath = Path.of("public" + path);
                File file = filePath.toFile();
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
          InetAddress clientInetAddress = clientSocket.getInetAddress();
          int port = clientSocket.getPort();
          String header = getHeader();
          if (!Objects.equals(header, "")) {
            String[] headerArray = header.split("\r\n");
            String response = headerArray[0].substring(8);
            String dateTime = headerArray[3];
            String contentLength = headerArray[1];
            String contentType = headerArray[2];
            InetAddress addr = InetAddress.getLocalHost();
            String hostname = addr.getHostName();
            clientSocket.close();
            System.out.println(
                "Client: " + clientInetAddress + port + ", Version: " + methodResourceVersion[2]
                    + ", Response:" + response + ", " + dateTime + " \nServername:" + hostname + ", "
                    + contentLength + ", " + checkIfSocketIsClosed(clientSocket)
                    + ", " + contentType);
            System.out.println("\n");
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  /**
   * A method to ensure that the requested path is in 'public' directory.
   */
  private boolean validatePath(String path) {
    Path publicPath = Paths.get("public").normalize();
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
  private boolean checkIfFileExists(String fileName) {
    if (fileName.charAt(fileName.length() -1) == '/') {
      if (fileName.length() != 1) {
        fileName = fileName.substring(0, fileName.length() - 1);
      }
    }
    String folderPath = fileName.substring(0, fileName.lastIndexOf('/'));
    File folder = new File("public" + folderPath);
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


