import java.io.*;
import java.net.*;
import java.util.*;

public class ConnectionService implements Runnable{

    public static ArrayList<ConnectionService> clientsConnected = new ArrayList<>();
    private Socket client;
    private BufferedReader in;
    private BufferedWriter out;
    private String cUsername;
    private boolean superUser;
    private boolean privateChat;
    private ConnectionService privateChatWith;
    public ConnectionService(Socket socket) {
        try {
            this.client = socket;
            this.out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.cUsername = in.readLine();
            if (cUsername.contains("Super")) {
                this.superUser=true;
                this.cUsername= cUsername.split(" ", 2)[1];
            }
            this.privateChat=false;
            clientsConnected.add(this);
            broadCastMsg("Server: " + cUsername + " has joined the chat!");
        } catch(IOException e) {
            terminate(client, in, out);
        }
    }


    @Override
    public void run() {
        String message;

        while(true) {
            try {
                if (!(client.isConnected() && (message = in.readLine())!=null)) break;
                if(this.superUser) {
                    specialTreatment(message);
                }
                else if (privateChat) {
                    if(message.contains("Exit Private chat")) {
                        endDirectMessage(this);
                    }
                    else{
                        directMessage(privateChatWith, message);
                    }
                } else {
                    broadCastMsg(message);
                }
            } catch (IOException e) {
                terminate(client, in, out);
                break;
            }
        }

    }

    public void broadCastMsg(String message) {
        for(ConnectionService client : clientsConnected ) {
            try {
                if(!client.cUsername.equals(this.cUsername)) {
                    client.out.write(message);
                    client.out.newLine();
                    client.out.flush();
                }
            }catch(IOException e) {
                terminate(this.client, in, out);
                break;
            }
        }
    }

    public void removeClient() {
        clientsConnected.remove(this);
        broadCastMsg("Server: " + cUsername + " has left the chat!");
    }

    public void terminate(Socket socket, BufferedReader input, BufferedWriter output) {
        removeClient();
        try {
            if(input != null) {
                input.close();
            }
            if(output != null) {
                output.close();
            }
            if(socket != null) {
                socket.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    public void directMessage(ConnectionService user, String msg) {
        try {
            user.out.write("Private message " + msg);
            user.out.newLine();
            user.out.flush();

        }catch(IOException e){
            terminate(this.client, in, out);
        }
    }

    public void endDirectMessage(ConnectionService user) {
        for(ConnectionService client : clientsConnected) {
            if(client.equals(user.privateChatWith)) {
                directMessage(this, "Private chat has ended");
                directMessage(client, "Private chat has ended");
                user.privateChat=false;
                user.privateChatWith = null;
                client.privateChat = false;
                client.privateChatWith = null;
                break;
            }
        }
    }

    public void specialTreatment(String msg) {
        if(msg.contains("Terminate"))
        {
            boolean terminated = false;
            ConnectionService clientToEnd = null;
            for(ConnectionService clientToTerminate : clientsConnected ) {
                if (msg.split(" ")[2].equals(clientToTerminate.cUsername)) {
                    terminated = true;
                    directMessage(clientToTerminate,"You have been terminated!");
                    broadCastMsg("Server: " + clientToTerminate.cUsername + " has been kicked out!");
                    clientToEnd = clientToTerminate;
                }
            }
            if(clientToEnd!=null)
                clientsConnected.remove(clientToEnd);
            if (!terminated) {
                directMessage(this, "User not found");}
        }
        else if(msg.contains("Exit Private chat")&& this.privateChat) {
            endDirectMessage(this);
        }
        else if(msg.contains("Private chat")) {
            for(ConnectionService client : clientsConnected) {
                if(msg.contains(client.cUsername)&&client.cUsername!=this.cUsername) {

                    if(client.privateChat) {
                        endDirectMessage(client);
                    }
                    if (this.privateChat) {
                        endDirectMessage(this);
                    }

                    client.privateChat=true;
                    client.privateChatWith= this;
                    this.privateChat=true;
                    this.privateChatWith=client;
                    directMessage(client, "You are now in a private chat with " + this.cUsername + "!");
                    directMessage(client, "To exit the private chat, write Exit Private chat");
                    directMessage(this, "You are now in a private chat with " + client.cUsername + "!");
                    directMessage(this, "To exit the private chat, write Exit Private chat");
                    break;
                }
            }if(!privateChat) {
                directMessage(this, "User not found");
            }
        }
        else if(privateChat) {
            directMessage(privateChatWith, msg);
        }
        else {
            broadCastMsg(msg);
        }
    }
}
