package silentrelay;
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

    public String generateSignature() {
        return "lol";
    }

    @Override
    public String toString() {
        return "Message{" +
                "encryptedMessage='" + encryptedMessage + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

}

