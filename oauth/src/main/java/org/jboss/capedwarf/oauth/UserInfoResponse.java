package org.jboss.capedwarf.oauth;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 *
 * Represents data returned by https://www.googleapis.com/oauth2/v1/userinfo
 */
public class UserInfoResponse {

    private String id;

    private String email;

    @JsonProperty("verified_email")
    private boolean verifiedEmail;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVerifiedEmail() {
        return verifiedEmail;
    }

    public void setVerifiedEmail(boolean verifiedEmail) {
        this.verifiedEmail = verifiedEmail;
    }
}
