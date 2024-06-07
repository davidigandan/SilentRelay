import java.time.LocalDateTime;

// Contains Encrypted Message, Signature and Timestamp
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

    @Override
    public String toString() {
        return "Message{" +
                "encryptedMessage='" + encryptedMessage + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

}

