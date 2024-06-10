package silentrelay;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDateTime;



public class MessageStore {
    public HashMap<String, ArrayList<Message>> userMessages = new HashMap<>();

    public Boolean storeEncryptedMessage(String hashedRecieverUserID, String encryptedMessage, LocalDateTime timestamp) {
        try {
            Message messageEntry = new Message(encryptedMessage, timestamp);
            userMessages.computeIfAbsent(hashedRecieverUserID, k -> new ArrayList<Message>()).add(messageEntry);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ArrayList<Message> getEncryptedMessages(String userId) {
        return userMessages.getOrDefault(userId, new ArrayList<Message>());
    }

    public void clearUsersInbox(String hashedUserId){
        // Logic to clear a users inbox after messages have been sent
        userMessages.remove(hashedUserId);
    }
}
