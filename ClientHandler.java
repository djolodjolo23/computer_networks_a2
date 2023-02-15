import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
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

      System.out.println("Received " + method + " request for " + path);

      if (path.equals("/joke.png") || path.equals("/joke.png/")) {
        FileInputStream image = new FileInputStream("public/joke.png");
        System.out.println(image);
        OutputStream clientOutput = clientSocket.getOutputStream();
        clientOutput.write(("HTTP/1.1 200 OK\r\n").getBytes());
        clientOutput.write(("\r\n").getBytes());
        clientOutput.write(image.readAllBytes());
        clientOutput.flush();

      } else if (path.equals("/hello.html") || path.equals("/hello.html/")) {
        OutputStream clientOutput = clientSocket.getOutputStream();
        clientOutput.write(("HTTP/1.1 200 OK\r\n").getBytes());
        clientOutput.write(("\r\n").getBytes());
        clientOutput.write(("Hello world!").getBytes());
        clientOutput.flush();
      } else {
        OutputStream clientOutput = clientSocket.getOutputStream();
        clientOutput.write(("HTTP/1.1 200 OK\r\n").getBytes());
        clientOutput.write(("\r\n").getBytes());
        clientOutput.write(("THIS IS THE HOMEPAGE OF YOUR WEBSERVER!!!\n\n").getBytes());
        clientOutput.write(("Type /joke.png to see the joke.png file.\n").getBytes());
        clientOutput.write(("Type hello.html to see the hello.html message.\n").getBytes());
        clientOutput.flush();
      }

      // Serve the requested file or return a 404 error
      try {
        File file = new File("." + path);
        String contentType = "text/plain";
        if (path.endsWith(".html")) {
          contentType = "text/html";
        } else if (path.endsWith(".png")) {
          contentType = "image/png";
        }
        byte[] data = Files.readAllBytes(file.toPath());

        output.write(("HTTP/1.1 200 OK\r\n" +
            "Content-Length: " + data.length + "\r\n" +
            "Content-Type: " + contentType + "\r\n" +
            "\r\n").getBytes());
        output.write(data);
      } catch (IOException e) {
        output.write(("HTTP/1.1 404 Not Found\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n").getBytes());
      }

      // Close the client socket
      output.close();
      input.close();
      clientSocket.close();
      System.out.println("Client disconnected");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}


