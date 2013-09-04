package org.jboss.capedwarf.channel.manager;

/**
 *
 */
public class Message {

    public static final Message NULL = new Message("", "");

    private String id;
    private String message;

    public Message(String id, String message) {
        this.id = id;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Message{" +
            "id='" + id + '\'' +
            ", message='" + message + '\'' +
            '}';
    }
}
