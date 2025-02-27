import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private Socket socket;
    private BufferedReader input;
    private BufferedWriter output;
    private String username;
    private volatile boolean is_running = true;

    private final static PrintStream err_log = new PrintStream(System.err);
    private final static PrintStream console = new PrintStream(System.out);
    private final static Scanner scanner = new Scanner(System.in);

    public ChatClient(String server_ip, int port) {
        try {
            socket = new Socket(server_ip, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            console.print("Enter your username: ");
            username = scanner.nextLine();
            output.write(username);
            output.newLine();
            output.flush();

            console.println("Connected to chat. Type messages below:");
            console.println("Type /exit to disconnect");
            console.println("Type /msg \"username\" <message> to send a private message");
            console.println("Type /list to see all online users");

            new Thread(this::listenForMessages).start(); // listen for messages in a separate thread
        }
        catch (IOException exception) {
            err_log.println("[ChatClient] An error occurred while connecting to server: closing client socket... ");
            close();
        }
    }

    public void listenForMessages() {
        try {
            String message;
            while (is_running && (message = input.readLine()) != null) {
                console.println(message);
            }
        }
        catch (IOException exception) {
            if (is_running) {
                err_log.println("[ChatClient] An error occurred while reading message: " + exception.getMessage());
            }
            close();
        }
    }

    public void sendMessage(String message) {
        try {
            output.write(message);
            output.newLine();
            output.flush();
        }
        catch (IOException exception) {
            err_log.println("[ChatClient] An error occurred while sending message: " + exception.getMessage());
            close();
        }
    }

    public void close() {
        is_running = false;
        try {
            if (output != null) {
                output.close();
            }

            if (input != null) {
                input.close();
            }

            if (socket != null) {
                socket.close();
            }

        }
        catch (IOException exception) {
            err_log.println("[ChatClient] An error occurred while closing client socket: " + exception.getMessage());
        }
        System.exit(0);
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public static void main(String[] args) {
        try {
            console.print("Enter server IP address (type localhost or nothing for localhost): ");
            String server_ip = scanner.nextLine().trim();

            if (server_ip.isEmpty()) {
                server_ip = "localhost";
            }

            int port = 8080;
            ChatClient client = new ChatClient(server_ip, port);

            while (client.isConnected()) {
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("/exit")) {
                    console.println("Disconnecting...");
                    client.sendMessage(message);
                    break;
                }
                client.sendMessage(message);
            }

            client.close();
        }
        catch (Exception exception) {
            err_log.println("[ChatClient] An error occurred in main loop: " + exception.getMessage());
        }
        finally {
            scanner.close();
        }
    }
}
