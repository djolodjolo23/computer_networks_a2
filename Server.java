import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;

public class Server {



  public static void main(String[] args) {
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

    try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]))) {
      // ready to receive messages
      System.out.println("Server has succesfully started. Listening on the port " + args[0]);
      URL url = Server.class.getResource("/public");
      assert url != null;
      String path = url.getPath();
      System.out.println("Detailed file path: " + path + "\n");
      while (true) {
        // client that's accepted
        // can accept multiple connections since in while(true) loop
        try (Socket client = serverSocket.accept()) {

          // read the requests and listen to the message
          InputStreamReader inputStreamReader = new InputStreamReader(client.getInputStream());


          BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

          // read the first request from the client
          //StringBuilder requestBuilder = new StringBuilder();

          //System.out.println("Client connected from " + localAddress + ":" + remotePort + " (localhost = " + isItSame + ")");


          //requestBuilder.append("Host: ").append(localAddress).append(" (localhost = ").append(isItSame).append(")");
          String firstLine = bufferedReader.readLine();
          String hostPort = bufferedReader.readLine();
          String[] methodResourceVersion = firstLine.split(" ");
          String resource = methodResourceVersion[1];
          if (checkIfFileExists(resource)) {
            System.out.println("Requested file exists!");
          }

          System.out.println("--- REQUEST ---");
          System.out.println(hostPort + "," + " Method: " + methodResourceVersion[0] + " Version: " + methodResourceVersion[2]);


          if (resource.equals("/joke.png") || resource.equals("/joke.png/")) {
            FileInputStream image = new FileInputStream("public/joke.png");
            System.out.println(image);
            OutputStream clientOutput = client.getOutputStream();
            clientOutput.write(("HTTP/1.1 200 OK\r\n").getBytes());
            clientOutput.write(("\r\n").getBytes());
            clientOutput.write(image.readAllBytes());
            clientOutput.flush();

          } else if (resource.equals("/hello.html") || resource.equals("/hello.html/")) {
            OutputStream clientOutput = client.getOutputStream();
            clientOutput.write(("HTTP/1.1 200 OK\r\n").getBytes());
            clientOutput.write(("\r\n").getBytes());
            clientOutput.write(("Hello world!").getBytes());
            clientOutput.flush();
          } else {
            OutputStream clientOutput = client.getOutputStream();
            clientOutput.write(("HTTP/1.1 200 OK\r\n").getBytes());
            clientOutput.write(("\r\n").getBytes());
            clientOutput.write(("THIS IS THE HOMEPAGE OF YOUR WEBSERVER!!!\n\n").getBytes());
            clientOutput.write(("Type /joke.png to see the joke.png file.\n").getBytes());
            clientOutput.write(("Type hello.html to see the hello.html message.\n").getBytes());
            clientOutput.flush();
          }

        } catch (IOException e) {
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


  static boolean checkIfFileExists(String fileName) {
    File folder = new File("public");
    File[] files = folder.listFiles();
    boolean doesTheFileExist = false;
    if (files != null) {
      for (File file : files) {
        String fileNameWithoutFolder = "/" + file.getName();
        if (fileNameWithoutFolder.equals(fileName)) {
          doesTheFileExist = true;
          break;
        }
      }
    }
    return doesTheFileExist;
  }
}
