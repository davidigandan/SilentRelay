package silentrelay;

public class SingleClientMessage {
    private String messageContent;
    private String messageTimestampAsString;
    private String messageSignature;


    public SingleClientMessage(String messageContent, String messageTimestampAsString, String messageSignature) {
        this.messageContent = messageContent;
        this.messageTimestampAsString = messageTimestampAsString;
        this.messageSignature = messageSignature;
    }


    public String getMessageContent() {
        return messageContent;
    }


    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }


    public String getMessageTimestampAsString() {
        return messageTimestampAsString;
    }


    public void setMessageTimestampAsString(String messageTimestampAsString) {
        this.messageTimestampAsString = messageTimestampAsString;
    }


    public String getMessageSignature() {
        return messageSignature;
    }


    public void setMessageSignature(String messageSignature) {
        this.messageSignature = messageSignature;
    }


    @Override
    public String toString() {
        return "SingleClientMessage [messageContent=" + messageContent + ", messageTimestampAsString=" + messageTimestampAsString
                + ", messageSignature=" + messageSignature + "]";
    }
    

    
}
