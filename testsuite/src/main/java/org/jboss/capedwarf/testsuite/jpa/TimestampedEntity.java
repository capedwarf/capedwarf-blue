package org.jboss.capedwarf.testsuite.jpa;

import javax.persistence.Basic;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@javax.persistence.Entity // TODO -- re-check this
@MappedSuperclass
public abstract class TimestampedEntity extends AbstractEntity implements TimestampAware {
    private static long serialVersionUID = 3l;

    private long timestamp;
    private long expirationTime;

    public TimestampedEntity() {
        super();
    }

    @Basic
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Basic
    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    @Transient
    public String getInfo() {
        StringBuilder builder = new StringBuilder();
        addInfo(builder);
        return builder.toString();
    }

    protected void addInfo(StringBuilder builder) {
        builder.append("timestamp=").append(timestamp);
    }
}

