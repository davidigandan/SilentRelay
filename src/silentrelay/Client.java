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
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;

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

        try(
            // Socket connection
            Socket socket = new Socket(host, port);

            InputStream input= socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
        )  {
            // Hash userId and send to the server
            writer.println(hashUserId(uuid));

            
            // Store all received messages into SingleClientmessage instances
            ArrayList<SingleClientMessage> recievedInbox = new ArrayList<SingleClientMessage>();
            storeAllLinesAsSCM(reader, recievedInbox);

            // Verify signatures, or disconnect from server if verification fails
            Boolean allVerified = verifyRecievedInbox(recievedInbox);
            // System.out.println("Line 69, allVerified " + allVerified);
            
            
            if (!allVerified) {
                socket.close();
                System.out.println("Server compromised");
                System.exit(1);
            }

            // Carry on implementation here
            // decryptAndDisplay(recievedInbox); Signature still isn't verifying; 


        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }


    private static Boolean verifyRecievedInbox(ArrayList<SingleClientMessage> recievedInbox) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException, IOException {
        //Verify each message and delete
        ArrayList<Boolean> allTrue = new ArrayList<Boolean>();
        for (SingleClientMessage scm: recievedInbox)  {
            
            if (authenticSCM(scm)) {
                allTrue.add(true);  
            } else {
                allTrue.add(false);
            }
        }

        // System.out.println("Line100 " + allTrue.toString());

        if (allTrue.contains(false)) {
            return false;
        } else {
            return true;
        }
        

    }


    private static Boolean authenticSCM(SingleClientMessage scm) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        // System.out.println("Line114, wahtisByted:" + scm.getMessageTimestampAsString().substring(prependConstants.get("~tmstp")).trim());
        byte[] signatureBytes = hexStringToByteArray(scm.getMessageSignature().substring(prependConstants.get("~sig")).trim());
        String dataToVerify = scm.getMessageContent().substring(prependConstants.get("~msg")).trim() + scm.getMessageTimestampAsString().substring(prependConstants.get("~tmstp")).trim();
        System.out.println("Line 117, sigToVerify: " + scm.getMessageSignature().substring(prependConstants.get("~sig")).trim());
        System.out.println("Line 118, dataToVerify: " + dataToVerify);
        
        // System.out.println("Current working directory: " + System.getProperty("user.dir"));
        byte[] publicKeyBytes = Files.readAllBytes(Paths.get("./src/silentrelay/keys/server.pub"));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);

        verifier.update(dataToVerify.getBytes(StandardCharsets.UTF_8));
        return verifier.verify(signatureBytes);

    }


    private static byte[] hexStringToByteArray(String hexString) {
        // System.out.println("Line 135 " + hexStringi);
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) <<
                    4) +
                Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
    

    private static void storeAllLinesAsSCM(BufferedReader reader, ArrayList<SingleClientMessage> recievedInbox) throws IOException {
        String line;
        int lineCount = 0;
        String[] linesBuffer = new String[3];

        while((line = reader.readLine()) != null) {
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
    }


    private static char[] hashUserId(String uuid) throws NoSuchAlgorithmException {
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




