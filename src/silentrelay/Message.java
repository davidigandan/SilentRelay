package silentrelay;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;

// Contains Encrypted Message and Timestamp
public class Message {
    private String encryptedMessage;
    private LocalDateTime timestamp;

    public Message(String encryptedMessage, LocalDateTime timestamp) {
        this.encryptedMessage = encryptedMessage;
        this.timestamp = timestamp;
    }

    public String getEncryptedMessage() {
        return encryptedMessage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public String generateSignature()  {

        try{
            byte[] keyBytes = Files.readAllBytes(Paths.get("./src/silentrelay/keys/server.prv"));

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey);

            signer.update((this.getEncryptedMessage()+this.getTimestamp()).getBytes(StandardCharsets.UTF_8));
            System.out.println("Line 42, dataToSign: " + this.getEncryptedMessage()+this.getTimestamp());
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

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
            
    }

    @Override
    public String toString() {
        return "Message{" +
                "encryptedMessage='" + this.encryptedMessage + '\'' +
                ", timestamp=" + this.timestamp +
                '}';
    }

}

