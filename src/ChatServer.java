import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.StringJoiner;

public class ChatServer {

    private final ServerSocket listener;
    private final static int PORT = 8080;
    private LinkedList<ClientHandler> clients = new LinkedList<>();
    private volatile boolean is_running = true;

    private final static PrintStream log = new PrintStream(System.out);
    private final static PrintStream err_log = new PrintStream(System.err);

    public ChatServer() throws IOException {
        this(PORT);
    }

    public ChatServer(int port) throws IOException {
        listener = new ServerSocket(port);
    }

    public void start() {
        log.println("[SERVER] Server started on port " + listener.getLocalPort());
        log.println("[SERVER] Press Ctrl+C to stop the server");

        try {
            while (is_running && !listener.isClosed()) {
                Socket pending_client = listener.accept(); // wait for a client to connect to the port
                log.println("[SERVER] A new client has connected.");
                ClientHandler new_client = new ClientHandler(pending_client, this);
                addClient(new_client);
                // start a new thread for each client
                Thread client_thread = new Thread(new_client);
                client_thread.start();
            }
        }
        catch (IOException exception) {
            err_log.println("[SERVER] An error occurred: closing server socket... ");
            close();
        }
    }

    public void close() {
        is_running = false;
        try {
            if (listener != null) {
                listener.close();
            }
        }
        catch (IOException exception)  {
            err_log.println("[SERVER] Could not close server socket:  " + exception.getMessage());
        }
    }

    public synchronized void broadcastMessage(String message, ClientHandler sender) {
        log.println(message);
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public synchronized boolean sendPrivateMessage(ClientHandler sender, String receiver_username, String private_message) {
        boolean is_receiver_found = false;
        String sender_username = sender.getClientUsername();

        for (ClientHandler receiver : clients) {
            if (receiver != null && receiver.getClientUsername().equalsIgnoreCase(receiver_username)) {
                receiver.sendMessage("[PM from " + sender_username + "] " + private_message);
                sender.sendMessage("[PM to " + receiver_username + "] " + private_message);
                is_receiver_found = true;
                log.println("[PRIVATE] " + sender_username + " to " + receiver_username + ": " + private_message);
                break;
            }
        }


        return is_receiver_found;
    }

    public synchronized String getOnlineUsers() {
        StringJoiner online_users = new StringJoiner(", ");
        for (ClientHandler client : clients) {
            online_users.add(client.getClientUsername());
        }
        return online_users.toString();
    }

    public synchronized void removeClient(ClientHandler client) {
        if (clients.contains(client)) {
            broadcastMessage(client.getClientUsername() + " has left the chat.", null);
            log.println("[SERVER] Client " + client.getClientUsername() + " has left the chat.");
            clients.remove(client);
            log.println("[SERVER] Active clients: " + clients.size());
        }
    }

    public synchronized void addClient(ClientHandler client) {
        clients.add(client);
        log.println("[SERVER] Active clients: " + clients.size());
    }

    public static void main(String[] args)  {
        try {
            ChatServer server = new ChatServer();
            server.start();
        }
        catch (IOException exception) {
            err_log.println("[SERVER] Failed to start server: " + exception.getMessage());
            System.exit(1);
        }
    }
}
