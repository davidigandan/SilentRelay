import java.time.LocalDateTime;

public class Message {
    private String encryptedMessage;
    private String signature;
    private LocalDateTime timestamp;

    public Message(String encryptedMessage, String signature, LocalDateTime timestamp) {
        this.encryptedMessage = encryptedMessage;
        this.signature = signature;
        this.timestamp = timestamp;
    }

    public String getEncryptedMessage() {
        return encryptedMessage;
    }

    public String getSignature() {
        return signature;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "encryptedMessage='" + encryptedMessage + '\'' +
                ", signature='" + signature + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

}
