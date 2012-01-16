package org.jboss.capedwarf.testsuite.jpa;

/**
 * The entity class.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface Entity {
    /**
     * Get the entity id.
     *
     * @return the id
     */
    Long getId();

    /**
     * Set identifier.
     *
     * @param identifier the id
     */
    void setId(Long identifier);
}
