package silentrelay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.net.ServerSocket;

public class Server {
    private static int port;
    private static MessageStore messageStore = new MessageStore();

    public static void main(String[] args) throws IOException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

        // Check if port is provided correctly
        if (args.length !=1) {
            
            System.err.println("Usage: java Server <port>");
            System.err.println("There is something wrong with the number of arguments. Exiting...");
            System.exit(1);
        }

        port = Integer.parseInt(args[0]);

        // Create and accept socket connection. Then handle the connection
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while(true) {
                Socket socket = serverSocket.accept();
                handleClient(socket);
            }

            
        } catch (IOException e) {
            System.err.println("Could not listen or form socket connections on port " + port);
            System.exit(1);
        }
    }

    private static void handleClient(Socket socket) throws IOException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        try(
            // Inputs to the server
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            // Outputs from the server
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true); 
        ) {
            String hashedClientUserId = reader.readLine();
            System.out.println(hashedClientUserId + "has connected");
            writer.println(retrieveUserInbox(hashedClientUserId));
            
            // Checking what client sent to reader
            String line;
            while ((line = reader.readLine()) != null ) {
                System.out.println("a line from reader: " + line);
            }

            ArrayList<SingleClientMessage> outbox = new ArrayList<SingleClientMessage>();
            Client.storeAllLinesAsSCM(reader, outbox);
            socket.shutdownOutput();

            System.out.println("Length of outbox: " + outbox.size());
            String pathToClientKey = "./src/silentrelay/keys/ai189.pub";
            ArrayList<Boolean> outboxVerification = new ArrayList<Boolean>();
            for (SingleClientMessage scm: outbox) {
                outboxVerification.add(Client.authenticSCM(scm, pathToClientKey));
            }
            
            // Remove scm's that can't be verified
            for (int i=0; i < outboxVerification.size(); i++) {
                if (!outboxVerification.get(i)) {
                    outbox.remove(i);
                }
            }

            
            decrypt(outbox, pathToClientKey);

            
            String publicKeyPath = "./src/silentrelay/keys/" + outbox.get(0).getMessageContent().substring(19,24)+ ".pub";
            String hashedRecieverId = Client.hashUserId(outbox.get(0).getMessageContent().substring(19,24)).toString();
            reEncrypt(outbox, publicKeyPath);
            storeOutbox(outbox, hashedRecieverId);
            
        }


        catch(IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }




    private static void storeOutbox(ArrayList<SingleClientMessage> outbox, String hashedRecieverId) {
        for (SingleClientMessage scm: outbox) {
            LocalDateTime scmLocalDateTime = LocalDateTime.parse(scm.getMessageTimestampAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            MessageStore.storeEncryptedMessage(hashedRecieverId, scm.getMessageContent(), scmLocalDateTime);
        }
    }

    private static void reEncrypt(ArrayList<SingleClientMessage> outbox, String publicKeyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        for (SingleClientMessage scm: outbox) {
            byte[] publicKeyBytes = Files.readAllBytes(Paths.get(publicKeyPath));
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encryptedBytes = cipher.doFinal(publicKeyBytes);
            String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);
            scm.setMessageContent(encryptedBase64);
        }
    }
    
    private static void decrypt(ArrayList<SingleClientMessage> outbox, String keyPath) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        for (SingleClientMessage scm: outbox) {
            String ciphertext = scm.getMessageContent();
            byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);

            byte[] publicKeyBytes = Files.readAllBytes(Paths.get(keyPath));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);

            scm.setMessageContent(new String(cipher.doFinal(ciphertextBytes),StandardCharsets.UTF_8));
        }        
    }

    private static char[] retrieveUserInbox(String hashedClientUserId) {
        // Logic to return no messages
        if (messageStore.getEncryptedMessages(hashedClientUserId).isEmpty()) {
          return "There are no messages found".toCharArray();  
        } else {
            // Logic to return user messages
            
            StringBuilder stringBuilderUserInbox = new StringBuilder();
            ArrayList<Message> arrayListUserInbox = messageStore.getEncryptedMessages(hashedClientUserId);
            for (Message message:arrayListUserInbox) {
                
                String messageSignature = message.generateSignature();
                

                stringBuilderUserInbox.append("Encrypted Message: ")
                                      .append(message.getEncryptedMessage())
                                      .append("\nMessage Timestamp: ")
                                      .append(message.getTimestamp())
                                      .append("\nMessage Signature: ")
                                      .append(messageSignature)
                                      .append("\n");
                                      
            }

            stringBuilderUserInbox.deleteCharAt((stringBuilderUserInbox.lastIndexOf("\n")));

            // Logic to remove hashedUserId key when there are no mesage objects left
            messageStore.clearUsersInbox(hashedClientUserId);
            System.out.println(stringBuilderUserInbox.toString().toCharArray());
            // Remove this
            return stringBuilderUserInbox.toString().toCharArray();
            
        }
        
    }

}
