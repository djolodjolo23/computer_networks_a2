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
          Client client = new Client(clientSocket);
          Thread clientThread = new Thread(client);
          clientThread.start();
          clientThread.join();
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
}
