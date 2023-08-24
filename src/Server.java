
import java.net.*;
import java.util.*;
import java.io.*;

public class Server {
    public static final int PORT = 7896;
    private static ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        Server.serverSocket = serverSocket;
    }
    public void run() {
        try {
            while(!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getLocalAddress());
                ConnectionService service = new ConnectionService(clientSocket);

                Thread thread = new Thread(service);
                thread.start();
            }

        } catch (IOException e) {
            try {
                if(serverSocket!=null) {
                    serverSocket.close();
                }
            } catch(IOException ie) {
                ie.printStackTrace();
            }
        }
    }

    public static void main(String args[]) throws IOException {
        serverSocket = new ServerSocket(PORT);
        Server server = new Server(serverSocket);
        server.run();
    }

}
