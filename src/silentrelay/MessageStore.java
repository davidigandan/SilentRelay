package silentrelay;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDateTime;



public class MessageStore {
    private HashMap<String, ArrayList<Message>> userMessages = new HashMap<>();

    public Boolean storeEncryptedMessage(String hashedRecieverUserID, String encryptedMessage) {
        try {
            Message messageEntry = new Message(encryptedMessage, LocalDateTime.now());
            userMessages.computeIfAbsent(hashedRecieverUserID, k -> new ArrayList<Message>()).add(messageEntry);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ArrayList<Message> getEncryptedMessages(String userId) {
        return userMessages.getOrDefault(userId, new ArrayList<Message>());
    }
}
