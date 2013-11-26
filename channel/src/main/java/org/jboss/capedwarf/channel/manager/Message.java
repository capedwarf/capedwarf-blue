package org.jboss.capedwarf.channel.manager;

import java.io.Serializable;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.ProvidedId;
import org.hibernate.search.annotations.TermVector;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Indexed
@ProvidedId
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TOKEN = "_token_";
    public static final String MSG = "_msg_";

    public static final Message NULL = new Message("", "");

    private String id;
    private String token;
    private String message;

    public Message(String token, String message) {
        this.token = token;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    @Field(name = TOKEN, analyze = Analyze.NO, norms = Norms.NO, termVector = TermVector.NO)
    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("Message{id='%s', token='%s', message='%s'}", id, token, message);
    }
}
