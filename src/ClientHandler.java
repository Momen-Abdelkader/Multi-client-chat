import java.io.*;
import java.net.Socket;


public class ClientHandler implements Runnable {

    private Socket client;
    private ChatServer server;
    private BufferedReader input;
    private BufferedWriter output;
    private String client_username;
    private volatile boolean is_running = true;
    private final static PrintStream err_log = new PrintStream(System.err);
    public volatile static int anonymous_id = 1;

    ClientHandler(Socket client, ChatServer server) {
        try {
            this.client = client;
            this.server = server;
            this.input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            client_username = input.readLine();

            if (client_username == null || client_username.trim().isEmpty()) {
                client_username = "Anonymous - " + String.valueOf(anonymous_id);
                anonymous_id++;
            }

            server.broadcastMessage(client_username + " has joined the chat.", this);
        }
        catch (IOException exception) {
            err_log.println("[ClientHandler] An error occurred while initializing client: " + exception.getMessage());
            closeConnection();
        }
    }

    public void closeConnection() {
        is_running = false;
        try {
            server.removeClient(this);

            if (output != null) {
                output.close();
            }

            if (input != null) {
                input.close();
            }

            if (client != null) {
                client.close();
            }

        }
        catch (IOException exception) {
            err_log.println("[ClientHandler] An error occurred while closing client socket: " + exception.getMessage());
        }
    }

    public void sendMessage(String message) {
        try {
            if (output != null && is_running) {
                output.write(message);
                output.newLine();
                output.flush();
            }
        }
        catch (IOException exception) {
            err_log.println("[ClientHandler] An error occurred while sending message: " + exception.getMessage());
            closeConnection();
        }
    }

    private void handlePrivateMessage(String message) {
        try {
            String trimmed_message = message.substring(5).trim();

            if (!trimmed_message.startsWith("\"")) {
                sendMessage("[SERVER] Invalid private message format. Use /msg <username> <message>.");
                return;
            }

            int user_name_end_index = trimmed_message.indexOf("\"", 1);
            if (user_name_end_index == -1) {
                sendMessage("[SERVER] Invalid private message format. Use /msg \"username\" <message>.");
                return;
            }

            String receiver_username = trimmed_message.substring(1, user_name_end_index);
            String private_message = trimmed_message.substring(user_name_end_index + 1).trim();

            if (private_message.isEmpty()) {
                sendMessage("[SERVER] Message cannot be empty.");
                return;
            }

            boolean is_message_sent = server.sendPrivateMessage(this, receiver_username, private_message);

            if (!is_message_sent) {
                sendMessage("[SERVER] User " + receiver_username + " is not online.");
            }

        }
        catch (Exception exception) {
            err_log.println("[ClientHandler] An error occurred while handling private message: " + exception.getMessage());
        }
    }

    public String getClientUsername() {
        return client_username;
    }


    @Override
    public void run() {
        try {
            String message;
            while (is_running && (message = input.readLine()) != null) {

                if (message.trim().equalsIgnoreCase("/exit")) {
                    break;
                }

                if (message.startsWith("/msg ")) {
                    handlePrivateMessage(message);
                }
                else if (!message.trim().isEmpty()) {
                    server.broadcastMessage(client_username + ": " + message, this);
                }
            }
        }
        catch (IOException exception) {
            if (is_running) {
                err_log.println("[ClientHandler] An error occurred while reading message: " + exception.getMessage());
            }
        }
        finally {
            closeConnection();
        }
    }
}
