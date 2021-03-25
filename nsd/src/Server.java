import java.net.*;
import java.io.*;

public class Server {
    public static void main(String[] args) throws IOException {

        if (args.length != 0) {
            System.err.println("Usage: java Server");
            System.exit(1);
        } //make sure user does not attempt to enter parameters from command line

        try (
                ServerSocket serverSocket = new ServerSocket(Parameters.SERVERPORT); //assign server to port 12345
        ) {
            Socket clientSocket;
            int counter = 0; //start counter to assign each user a numerical id

            while (true) {
                clientSocket = serverSocket.accept();
                counter++;
                new ClientHandler(clientSocket, "Client" + counter).start();
            }
        }


    }
}
