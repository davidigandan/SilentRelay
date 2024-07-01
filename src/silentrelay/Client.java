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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Client {

    // Initialise host, port and uuid
    private static String host;
    private static int port;
    private static String uuid;

    public static HashMap <String, Integer> prependConstants = new HashMap<>();
    
    static {prependConstants.put("~msg", 19);prependConstants.put("~tmstp", 19);prependConstants.put("~sig", 19);}
    
    
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Client <host> <port> <uuid>");
            System.out.println("There's something wrong with the number of arguments. Exiting...");
            System.exit(1);
        }

        host = args[0];
        port = Integer.parseInt(args[1]);
        uuid = (args[2]);        

        try {
            // Socket connection
            Socket socket = new Socket(host, port);

            InputStream input= socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
         

            
            // Hash userId and send to the server
            writer.println(hashUserId(uuid));
            reader.mark(50000000);
          
            if (reader.readLine().equals("There are no messages found")) {
                System.out.println("There are no messages found.");
                sendNewMessage();

            } else {
                reader.reset();
                // Store all received messages into SingleClientMessage instances
                ArrayList<SingleClientMessage> recievedInbox = new ArrayList<SingleClientMessage>();
                System.out.println("I'm here1");
                storeAllLinesAsSCM(reader, recievedInbox);
                System.out.println("I'm here2");

                // Verify signatures, or disconnect from server if verification fails
                String serverKey = "./src/silentrelay/keys/server.pub";
                Boolean allVerified = verifyRecievedBox(recievedInbox, serverKey);
                System.out.println(allVerified);

                if (!allVerified) {
                    socket.close();
                    System.out.println("Server compromised");
                    System.exit(1);
                }

                decryptAndDisplay(recievedInbox);

                String timestampCiphertextAndSignatureOrStatus = sendNewMessage();
                if (timestampCiphertextAndSignatureOrStatus.equals("Invalid input")) {
                    System.out.println("That input is invalid.");
                    socket.close();
                    System.exit(1);
                    
                } else if (timestampCiphertextAndSignatureOrStatus.equals("No message to be sent")) {
                    System.out.println("No message will be sent. Terminating program.");
                    socket.close();
                    System.exit(0);
                } else {
                    writer.println(timestampCiphertextAndSignatureOrStatus);
                    System.out.println("Message sent. Terminating program");
                    socket.close();
                    System.exit(0);
                }
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }


    private static String sendNewMessage() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, SignatureException {
        System.out.println("Do you want to send a new message (y/n)?");
        Scanner prompt = new Scanner(System.in);
        String answer = prompt.nextLine();

        if (answer.equals("y")) {
            System.out.println("Enter username of recipient: ");
            String recipient = prompt.nextLine();

            System.out.println("Enter your message: ");
            String message = prompt.nextLine();
            prompt.close();

            // make this encrypt the message and then return the message for sending
            String clientEncryptedMessage = encrypt(recipient+message);
            LocalDateTime timestamp = LocalDateTime.now();
            String signature = generateSenderSignature(clientEncryptedMessage, timestamp.toString());
            return "Encrypted Message: " + clientEncryptedMessage + "\nMessage Timestamp: " + timestamp.toString() +  "\nMessage Signature: " + signature;

        } else if (answer.equals("n")) {
            prompt.close();
            return "No message to be sent";
            
        } else {
            prompt.close();
            return "Invalid input";
        } 
    }


    private static String generateSenderSignature(String clientEncryptedMessage, String timestamp) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        byte[] keyBytes = Files.readAllBytes(Paths.get("./src/silentrelay/keys/" + uuid + ".prv"));

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update((timestamp + clientEncryptedMessage).getBytes(StandardCharsets.UTF_8));
        byte[] messageSignatureBytes = signer.sign();

        // Convert the signature bytes to a hexadecimal string
        StringBuilder messageSignatureHex = new StringBuilder();
        for (byte b : messageSignatureBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                messageSignatureHex.append('0');
            }
            messageSignatureHex.append(hex);
        }

        return messageSignatureHex.toString();
        
    }


    private static String encrypt(String message) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] publicKeyBytes = Files.readAllBytes(Paths.get("./src/silentrelay/keys/server.pub"));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);
        return encryptedBase64;
    }


    private static void decryptAndDisplay(ArrayList<SingleClientMessage> recievedInbox) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
        String pathToKey = "./src/silentrelay/keys/" + uuid + ".prv";
        for (SingleClientMessage scm: recievedInbox) {
            System.out.println("\nSent at: " + scm.getMessageTimestampAsString() + ".\n Message Content: " + getDecryptedMessage(scm, pathToKey));

        }
    }


    private static String getDecryptedMessage(SingleClientMessage scm, String keyPath) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String ciphertext = scm.getMessageContent();
        byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);

        byte[] privateKeyBytes = Files.readAllBytes(Paths.get(keyPath));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decryptedBytes = cipher.doFinal(ciphertextBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }


    public static Boolean verifyRecievedBox(ArrayList<SingleClientMessage> recievedInbox, String key) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, IOException {
        //Verify each message and delete
        ArrayList<Boolean> allTrue = new ArrayList<Boolean>();
        for (SingleClientMessage scm: recievedInbox)  {
            
            if (authenticSCM(scm, key)) {
                allTrue.add(true);  
            } else {
                allTrue.add(false);
            }
        }

        if (allTrue.contains(false)) {
            return false;
        } else {
            return true;
        }
        

    }


    public static Boolean authenticSCM(SingleClientMessage scm, String key) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] signatureBytes = hexStringToByteArray(scm.getMessageSignature().substring(prependConstants.get("~sig")).trim());
        String dataToVerify = scm.getMessageContent().substring(prependConstants.get("~msg")).trim() + scm.getMessageTimestampAsString().substring(prependConstants.get("~tmstp")).trim();
    
        // System.out.println("Current working directory: " + System.getProperty("user.dir"));
        byte[] publicKeyBytes = Files.readAllBytes(Paths.get(key));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);

        verifier.update(dataToVerify.getBytes(StandardCharsets.UTF_8));
        return verifier.verify(signatureBytes);

    }


    private static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) <<
                    4) +
                Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
    
    // public static void storeAllLinesAsSCM(BufferedReader reader, ArrayList<SingleClientMessage> recievedInbox) throws IOException {
    //     String line;
    //     int lineCount = 0;
    //     String[] linesBuffer = new String [];

    // }

    public static void storeAllLinesAsSCM(BufferedReader reader, ArrayList<SingleClientMessage> recievedInbox) throws IOException {
        String line;
        int lineCount = 0;
        String[] linesBuffer = new String[3];
        
        while((line = reader.readLine()) != null) {
            System.out.println("a line:" +line);
            // Store the line in the linesBuffer
            linesBuffer[lineCount%3] = line;
            lineCount++;

            // If buffer is filled with 3 lines, create a SingleClientMessage instance
            if (lineCount % 3 == 0) {

                SingleClientMessage singleMessage = new SingleClientMessage(linesBuffer[0], linesBuffer[1], linesBuffer[2]);
                recievedInbox.add(singleMessage);
                linesBuffer = new String[3]; //Reset the buffer
            }

        }
        System.out.println("I left the while loop");
        // Check for any remaining lines that did not complete a full message
        if (lineCount % 3 != 0) {
            System.out.println("Incomplete message received"); // Debugging statement
            for (int i = 0; i < lineCount % 3; i++) {
                System.out.println("Buffer line " + i + ": " + linesBuffer[i]); // Debugging statement
            }
        }
    }


    public static char[] hashUserId(String uuid) throws NoSuchAlgorithmException {
        byte[] byteUserId = ("gfhk2024:" + uuid).getBytes();
        MessageDigest hasher = MessageDigest.getInstance("MD5");
        hasher.update(byteUserId);

        byte[] hashAsBytes = hasher.digest();
        // Convert the byte array to a hexadecimal string
        StringBuilder hashAsHex = new StringBuilder();
        for (byte b : hashAsBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hashAsHex.append('0');
            hashAsHex.append(hex);
        }

        return hashAsHex.toString().toCharArray();
    } 
}




