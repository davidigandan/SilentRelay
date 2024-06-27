package silentrelay;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDateTime;

public class MessageStore {
    public static HashMap<String, ArrayList<Message>> userMessages = new HashMap<>();

    public MessageStore() {
        ArrayList<Message> user1Inbox = new ArrayList<>();
        user1Inbox.add(new Message("ciphertext1", LocalDateTime.now()));
        user1Inbox.add(new Message("ciphertext2", LocalDateTime.now().minusDays(1)));
        
        ArrayList<Message> user2Inbox = new ArrayList<>();
        user2Inbox.add(new Message("ciphertext3", LocalDateTime.now()));
        user2Inbox.add(new Message("ciphertext4", LocalDateTime.now().minusDays(2)));

        userMessages.put("7f9d2d08fe481ce3916eb0c94996ba17", user1Inbox);
        userMessages.put("b1f301eb69cab1479da78defe30da613", user2Inbox);
    }

    public static Boolean storeEncryptedMessage(String hashedRecieverUserID, String encryptedMessage, LocalDateTime timestamp) {
        try {
            Message messageEntry = new Message(encryptedMessage, timestamp);
            userMessages.computeIfAbsent(hashedRecieverUserID, k -> new ArrayList<Message>()).add(messageEntry);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ArrayList<Message> getEncryptedMessages(String hashedClientId) {
        return userMessages.getOrDefault(hashedClientId, new ArrayList<Message>());
    }

    public void clearUsersInbox(String hashedClientId){
        // Logic to clear a users inbox after messages have been sent
        userMessages.remove(hashedClientId);
    }
}
