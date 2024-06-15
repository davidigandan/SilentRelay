package silentrelay;

import java.time.LocalDateTime;

public class SingleClientMessage {
    private String messageContent;
    private LocalDateTime messageTimestamp;
    private String messageSignature;


    public SingleClientMessage(String messageContent, LocalDateTime messageTimestamp, String messageSignature) {
        this.messageContent = messageContent;
        this.messageTimestamp = messageTimestamp;
        this.messageSignature = messageSignature;
    }


    public String getMessageContent() {
        return messageContent;
    }


    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }


    public LocalDateTime getMessageTimestamp() {
        return messageTimestamp;
    }


    public void setMessageTimestamp(LocalDateTime messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }


    public String getMessageSignature() {
        return messageSignature;
    }


    public void setMessageSignature(String messageSignature) {
        this.messageSignature = messageSignature;
    }


    @Override
    public String toString() {
        return "SingleClientMessage [messageContent=" + messageContent + ", messageTimestamp=" + messageTimestamp
                + ", messageSignature=" + messageSignature + "]";
    }
    

    
}
