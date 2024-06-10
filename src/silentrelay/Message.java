package silentrelay;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
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

    public String generateSignature() throws IOException {
        try {
            byte[] keyBytes = Files.readAllBytes(Paths.get("server.prv"));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = keyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        }
        
    }

    @Override
    public String toString() {
        return "Message{" +
                "encryptedMessage='" + encryptedMessage + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

}

