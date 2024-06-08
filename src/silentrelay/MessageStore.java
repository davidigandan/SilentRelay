package silentrelay;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDateTime;



public class MessageStore {
    private HashMap<String, ArrayList<Message>> userMessages = new HashMap<>();

    public void storeEncryptedMessage(String hashedRecieverUserID, String encryptedMessage) {
        Message messageEntry = new Message(encryptedMessage, LocalDateTime.now());
        userMessages.computeIfAbsent(hashedRecieverUserID, k -> new ArrayList<>()).add(messageEntry);
    }

    public ArrayList<Message> getEncryptedMessages(String userId) {
        return userMessages.getOrDefault(userId, new ArrayList<>());
    }
}
