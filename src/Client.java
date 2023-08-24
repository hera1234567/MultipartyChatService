import java.net.*;
import java.util.*;
import java.io.*;
public class Client {

    private Socket client;
    private BufferedReader in;
    private BufferedWriter out;
    private String username;

    public Client(Socket client, String username) {
        try {
            this.client = client;
            this.out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.username = username;
        } catch (IOException e) {
            terminate(client, in, out);
        }
    }

    public void sendMsg() {
        try {
            out.write(username);
            out.newLine();
            out.flush();
            String realUsername;
            if(username.contains("Super")) {realUsername = username.split(" ", 2)[1];}
            else{realUsername=username;}
            Scanner scanner = new Scanner(System.in);
            while(!client.isClosed()) {
                String message = scanner.nextLine();
                out.write(realUsername + ": " + message);
                out.newLine();
                out.flush();
            }
        }catch(IOException e) {
            terminate(client, in, out);
        }
    }

    public void listenForMsg() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                String messageFromChat;

                while(!client.isClosed()) {
                    try {
                        messageFromChat = in.readLine();
                        if(messageFromChat.contains("terminated")){
                            terminate(client, in, out);
                            break;
                        } else {
                            System.out.println(messageFromChat);
                        }
                    }catch(IOException e) {
                        terminate(client, in, out);
                        break;
                    }
                }
            }

        }).start();
    }

    public void terminate(Socket client, BufferedReader in, BufferedWriter out) {
        System.out.println("Shutting down chat");
        try {
            if(in != null) {
                in.close();
            }
            if(out != null) {
                out.close();
            }
            if(client != null) {
                client.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username: ");
        String username = scanner.nextLine();
        System.out.println("Are you a SuperUser : (type yes for yes) ");
        String input = scanner.nextLine();
        if (input.equals("yes")) {
            username = "Super " + username;
            System.out.println("You are now a SuperUser!");
            System.out.println("To create a private chat, write Private chat 'username'");
            System.out.println("To terminate a user, write Terminate 'username'");
        }
        Socket socket = new Socket("localhost", 7896);
        Client client = new Client(socket, username);
        client.listenForMsg();
        client.sendMsg();
    }

}