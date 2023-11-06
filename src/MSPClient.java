import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MSPClient {
    public static void main(String[] args) {
        final String serverHost = "localhost";
        final int serverPort = 12345;

        try (
                Socket socket = new Socket(serverHost, serverPort);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            out.println("CONNECT " + username);

            String response;
            while ((response = in.readLine()) != null) {
                if (response.equals("Connected as " + username)) {
                    System.out.println("Connected to the MSP server as " + username);
                } else {
                    System.out.println(response);
                }

                if (response.equals(username + " has disconnected.")) {
                    break;
                }

                if (response.equals("Connected Users:")) {
                    while (!(response = in.readLine()).isEmpty()) {
                        System.out.println(response);
                    }
                }

                if (response.startsWith(username)) {
                    System.out.print("Enter a message: ");
                    String message = scanner.nextLine();
                    out.println("SEND #" + message + "@" + username);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
