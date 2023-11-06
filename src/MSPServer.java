import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class MSPServer {
    private static final int PORT = 12345;
    private static List<PrintWriter> clientWriters = new ArrayList<>();
    private static List<String> connectedUsers = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("MSP Server is running on port " + PORT);
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("CONNECT")) {
                        handleConnect(inputLine);
                    } else if (inputLine.startsWith("DISCONNECT")) {
                        handleDisconnect();
                        break;
                    } else if (inputLine.startsWith("SEND")) {
                        handleSend(inputLine);
                    } else if (inputLine.equals("LIST")) {
                        handleList();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (username != null) {
                    connectedUsers.remove(username);
                }
                clientWriters.remove(out);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleConnect(String connectMessage) {
            username = connectMessage.substring(8).trim();
            connectedUsers.add(username);
            clientWriters.add(out);
            sendMessageToClient("Connected as " + username);
            System.out.println(username + " has connected.");
        }

        private void handleDisconnect() {
            if (username != null) {
                sendMessageToClient(username + " has disconnected.");
                System.out.println(username + " has disconnected.");
            }
        }

        private void handleSend(String sendMessage) {
            if (username != null) {
                String message = sendMessage.substring(6).trim();
                for (PrintWriter writer : clientWriters) {
                    writer.println(username + ": " + message);
                }
            }
        }

        private void handleList() {
            out.println("Connected Users: " + String.join(", ", connectedUsers));
        }

        private void sendMessageToClient(String message) {
            out.println(message);
        }
    }
}
