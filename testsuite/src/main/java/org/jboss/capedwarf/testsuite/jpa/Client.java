package org.jboss.capedwarf.testsuite.jpa;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@javax.persistence.Entity
public class Client extends TimestampedEntity {
    private static long serialVersionUID = 4l;

    // user info
    private String username;
    private String password;
    private String token;
    private String email;
    private String recovery;

    // lower case
    private String lowercaseUsername;

    // app reg id
    private String registrationId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        if (username != null) {
            lowercaseUsername = username.toLowerCase();
        } else {
            lowercaseUsername = null;
        }
    }

    public String getLowercaseUsername() {
        return lowercaseUsername;
    }

    public void setLowercaseUsername(String lowercaseUsername) {
        this.lowercaseUsername = lowercaseUsername;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRecovery() {
        return recovery;
    }

    public void setRecovery(String recovery) {
        this.recovery = recovery;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    protected void addInfo(StringBuilder builder) {
        super.addInfo(builder);
        builder.append(", username=").append(username);
        builder.append(", token=").append(token);
        builder.append(", email=").append(email);
    }
}
