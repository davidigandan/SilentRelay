package silentrelay;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Client {

    // Initialise host, port and uuid
    private static String host;
    private static int port;
    private static String uuid;


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
            

        } catch (Exception e) {
            
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
