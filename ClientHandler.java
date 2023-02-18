import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class ClientHandler implements Runnable{

  private Socket clientSocket;

  public ClientHandler(Socket socket) {
    this.clientSocket = socket;
  }

  public void run() {
    try {
      // Get the input and output streams of the client socket
      InputStream input = clientSocket.getInputStream();
      OutputStream output = clientSocket.getOutputStream();

      InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());

      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

      //String firstLine = bufferedReader.readLine();
      //String hostPort = bufferedReader.readLine();
      //String[] methodResourceVersion = firstLine.split(" ");
      //String resource = methodResourceVersion[1];

      // Read the HTTP request from the client
      Scanner scanner = new Scanner(input);
      String requestLine = scanner.nextLine();
      String[] parts = requestLine.split(" ");
      String method = parts[0];
      String path = parts[1];
      String protocol = parts[2];

      //System.out.println("Received " + method + " request for " + path);
      /**
      if (path.equals("/clown.png") || path.equals("/clown.png/")) {
        FileInputStream image = new FileInputStream("public/clown.png");
        System.out.println(image);
        OutputStream clientOutput = clientSocket.getOutputStream();
        clientOutput.write(("HTTP/1.1 200 OK\r\n").getBytes());
        clientOutput.write(("\r\n").getBytes());
        clientOutput.write(image.readAllBytes());
        clientOutput.flush();
      } else if (path.equals("/clowns.html") || path.equals("/clowns.html/")) {
        OutputStream clientOutput = clientSocket.getOutputStream();
        File file = new File("public/clowns.html");
        String content = new String(Files.readAllBytes(file.toPath()));
        clientOutput.write(("HTTP/1.1 200 OK\r\n").getBytes());
        clientOutput.write(("Content-Type: text/html\r\n").getBytes());
        clientOutput.write(("\r\n").getBytes());
        clientOutput.write(content.getBytes());
        clientOutput.flush();
      } else if (path.equals("/fun.html") || path.equals("/fun.html/")) {
        OutputStream clientOutput = clientSocket.getOutputStream();
        File file = new File("public/fun.html");
        String content = new String(Files.readAllBytes(file.toPath()));
        clientOutput.write(("HTTP/1.1 200 OK\r\n").getBytes());
        clientOutput.write(("Content-Type: text/html\r\n").getBytes());
        clientOutput.write(("\r\n").getBytes());
        clientOutput.write(content.getBytes());
        // Read the image file and write it to the output stream
      }else {
        OutputStream clientOutput = clientSocket.getOutputStream();
        File file = new File("public/index.html");
        String content = new String(Files.readAllBytes(file.toPath()));
        clientOutput.write(("HTTP/1.1 200 OK\r\n").getBytes());
        clientOutput.write(("Content-Type: text/html\r\n").getBytes());
        clientOutput.write(("\r\n").getBytes());
        clientOutput.write(content.getBytes());
      }
       */
      // Serve the requested file or return a 404 error
      if (path.equals("/") || path.equals("/index.html")) {
        try {
          String contentType = "text/html";
          byte[] data = Files.readAllBytes(Path.of("public/index.html"));
          output.write(("HTTP/1.1 200 OK\r\n" +
              "Content-Length: " + data.length + "\r\n" +
              "Content-Type: " + contentType + "\r\n" +
              "\r\n").getBytes());
          output.write(data);
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
            File file = filePath.toFile();
            if (file.isDirectory()) {
              contentType = "text/html";
              filePath = filePath.resolve("index.html");
              file = filePath.toFile();
            }
            byte[] data = Files.readAllBytes(Path.of(filePath.toUri()));
            output.write(("HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + data.length + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "\r\n").getBytes());
            output.write(data);
          } catch (IOException e) {
            output.write(("HTTP/1.1 404 Not Found\r\n" + "Content-Length: 0\r\n" +
                "\r\n").getBytes());
          }
        }
      // Close the client socket
      output.close();
      input.close();
      clientSocket.close();
      System.out.println("\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}


