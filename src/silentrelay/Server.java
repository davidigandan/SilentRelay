package silentrelay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.net.ServerSocket;

public class Server {
    private static int port;
    private static MessageStore messageStore = new MessageStore();

    public static void main(String[] args) throws IOException {

        // Check if port is provided correctly
        if (args.length !=1) {
            
            System.err.println("Usage: java Server <port>");
            System.err.println("There is something wrong with the number of arguments. Exiting...");
            System.exit(1);
        }

        port = Integer.parseInt(args[0]);

        // Create and accept socket connection
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while(true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                handleClient(socket);
            }

            
        } catch (IOException e) {
            System.err.println("Could not listen or form socket connections on port " + port);
            System.exit(1);
        }
    }

    private static void handleClient(Socket socket) throws IOException {
        try(
            // Inputs to the server
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            // Outputs from the server
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true); 
        ) {
            // 
            String hashedClientUserId = reader.readLine();
            writer.print(retrieveUserInbox(hashedClientUserId));
        }
    }

    

    private static char[] retrieveUserInbox(String hashedClientUseId) {
        // Logic to return no messages
        if (messageStore.getEncryptedMessages(hashedClientUseId).isEmpty()) {
          return "There are no messages found".toCharArray();  
        } else {
            // Logic to return messages
            StringBuilder stringBuilderUserInbox = new StringBuilder();
            ArrayList<Message> arrayListUserInbox = messageStore.getEncryptedMessages(hashedClientUseId);
            for (Message message:arrayListUserInbox) {
                stringBuilderUserInbox.append("Encrypted Message: ");
                stringBuilderUserInbox.append(message.getEncryptedMessage());
                stringBuilderUserInbox.append("Message Timestamp: ");
                stringBuilderUserInbox.append(message.getTimestamp());
                stringBuilderUserInbox.append("\n");
            }

        }
        
        // Logic to remove hashedUserId key when there are no mesage objects left
    }

}
