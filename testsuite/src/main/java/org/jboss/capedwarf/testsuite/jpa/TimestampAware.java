package org.jboss.capedwarf.testsuite.jpa;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface TimestampAware {
    /**
     * Get timestamp.
     *
     * @return the timestamp
     */
    long getTimestamp();

    /**
     * Set timestamp.
     *
     * @param timestamp the timestamp
     */
    void setTimestamp(long timestamp);

    /**
     * The expiration time.
     *
     * @return the expiration time
     */
    long getExpirationTime();
}
