import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class Server {

  static String resource = "";


  public static void main(String[] args) {
    argsCheck(args);
    try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]))) {
      // ready to receive messages
      System.out.println("Server has successfully started. Listening on the port " + args[0]);
      URL url = Server.class.getResource("/public");
      assert url != null;
      String path = url.getPath();
      System.out.println("Detailed file path: " + path + "\n");
      while (true) {
        // client that's accepted
        // can accept multiple connections since in while(true) loop
        try (Socket clientSocket = serverSocket.accept()) {

          ArrayList<String> folders = new ArrayList<>();
          // Create a new thread to handle the client connection
          InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
          BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
          Client client = new Client(clientSocket);

          String line;
          if ((line = bufferedReader.readLine()) != null) {
            String hostPort = bufferedReader.readLine();
            if (!(hostPort == null)) {
              String[] methodResourceVersion = line.split(" ");
              setResource(methodResourceVersion[1]);
              if (!methodResourceVersion[1].endsWith(".html") && !methodResourceVersion[1].endsWith(".png")) {
                if (methodResourceVersion[1].charAt(methodResourceVersion[1].length() - 1) == '/'
                    || methodResourceVersion[1].charAt(methodResourceVersion[1].length() - 2) == '/') {
                  folders.add(methodResourceVersion[1]);
                }
              }
              Thread clientThread = new Thread(client);
              clientThread.start();
              clientThread.join();
              System.out.println("Assigned a new client to a separate thread!");
              System.out.println(
                  hostPort + "," + " Method:" + methodResourceVersion[0] + ", Path: " + methodResourceVersion[1]
                      + ", Version: " + methodResourceVersion[2]);
              if (checkIfFileExists(methodResourceVersion[1]) || folders.contains(methodResourceVersion[1])) {
                System.out.println("Requested file exists!");
                if (folders.contains(methodResourceVersion[1])) {
                  System.out.println("Requested item is a folder.");
                } else {
                  if (methodResourceVersion[1].endsWith(".html")) {
                    System.out.println("Requested item is an html file.");
                  } else if (methodResourceVersion[1].endsWith(".png")) {
                    System.out.println("Requested item is an png file.");
                  }
                }
              } else {
                System.out.println("Requested file does not exist!");
              }
              InetAddress clientInetAddress = clientSocket.getInetAddress();
              int port = clientSocket.getPort();
              String header = client.getHeader();
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
        } catch (IOException | InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static boolean integerCheck(String var0) {
    try {
      Integer.parseInt(var0);
      return true;
    } catch (NumberFormatException var2) {
      return false;
    }
  }

  static String getResource() {
    return resource;
  }

  static void setResource(String resource) {
    Server.resource = resource;
  }

  static void argsCheck(String[] args) {
    if (args.length != 2) {
      System.err.println("There must be two program arguments, the listening port and a relative folder path");
      System.exit(1);
    } else if (!integerCheck(args[0])) {
      System.err.println("error, the port number is not an integer value");
      System.exit(1);
    } else if (args[0].length() > 5) {
      System.err.println("error, the port number is longer than 5");
      System.exit(1);
    } else if (!Objects.equals(args[1], "public")) {
      System.err.println("the folder name should be 'public' ");
      System.exit(1);
    }
  }

  static String checkIfSocketIsClosed(Socket socket) {
    if (socket.isClosed()) {
      return " Connection: closed";
    } else {
      return " Connection: open";
    }
  }


  static boolean checkIfFileExists(String fileName) {
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
