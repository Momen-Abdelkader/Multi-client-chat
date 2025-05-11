# Multi-client-chat

A simple client-server chat application implemented in Java that allows multiple users to communicate through a central server.

## Features

- Multi-user chat communication
- Private messaging between users
- Simple and intuitive command system

## Components

The application consists of three main Java classes:

1. **ChatServer**: Manages connections and message distribution.
2. **ChatClient**: Connects to the server and provides a user interface.
3. **ClientHandler**: Handles individual client connections on the server side.

## Requirements

- Java Runtime Environment (JRE) 8 or higher
- Network connectivity between client and server

## How to Run

### Option 1: Running from Source Files (Simplest Method)

#### Starting the Server

```
java ChatServer.java
```

#### Starting a Client

```
java ChatClient.java
```

When starting a client:
1. Enter the server IP address when prompted (or press Enter for localhost).
2. Enter your desired username.
3. Start chatting!

### Alternative: Running from Compiled Classes

If you prefer to compile first:

1. Compile the Java files:
   ```
   javac ChatServer.java ClientHandler.java
   javac ChatClient.java
   ```

2. Run the compiled classes:
   ```
   java ChatServer
   java ChatClient
   ```

### Option 2: Using an IDE

1. Import the source files into your preferred IDE (Eclipse, IntelliJ IDEA, NetBeans)
2. Run the `ChatServer` class to start the server
3. Run the `ChatClient` class to start a client


## Client Commands

| Command | Description |
|---------|-------------|
| `/exit` | Disconnect from the chat server |
| `/msg "username" <message>` | Send a private message to a specific user |
| `/list` | Display all currently online users |

## Implementation Details

### ChatServer
- Accepts client connections and creates a `ClientHandler` for each client
- Maintains a list of connected clients
- Provides methods for broadcasting messages to all clients
- Handles private messages between clients

### ChatClient
- Connects to the server using a socket
- Provides a command-line interface for user interaction
- Runs a separate thread for receiving messages from the server
- Handles user commands and message sending

### ClientHandler
- Manages communication with a specific client
- Parses commands from client messages
- Routes messages between the client and server

## Error Handling

The application includes robust error handling for:
- Connection failures
- Client disconnections
- I/O errors
- Invalid commands

## Troubleshooting

### Common Issues:
1. **"Address already in use" error**: The server port (8080 by default) is already in use. Either stop the other application using the port or change the port number in `ChatServer.java`.

2. **Connection refused**: Ensure the server is running before starting clients. Check if a firewall is blocking the connection.

3. **Clients can't connect to a remote server**: Make sure to use the correct IP address of the server and that there are no network restrictions.

## License

This project is open source and available under the [MIT License](LICENSE).
