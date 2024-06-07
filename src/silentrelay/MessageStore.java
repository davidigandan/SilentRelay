package silentrelay;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDateTime;



public class MessageStore {
    private HashMap<String, ArrayList<Message>> userMessages = new HashMap<>();

    public void addMessage(String hashedRecieverUserID, String encryptedMessage, String signature) {
        Message messageEntry = new Message(encryptedMessage, LocalDateTime.now());

    }
}
