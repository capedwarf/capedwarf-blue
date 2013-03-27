package org.jboss.capedwarf.datastore;


import java.io.Serializable;

import org.jboss.capedwarf.datastore.query.Bridge;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class RawValue implements Serializable {

    private static final long serialVersionUID = 1L;

    private Object value;

    public RawValue(Object value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public <T> T asStrictType(Class<T> type) {
        try {
            return type.cast(asType(type));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Type mismatch");
        }
    }

    public Object asType(Class<?> type) {
        Bridge bridge = Bridge.getBridge(type);
        return bridge.convertValue(value);
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "RawValue: " + getValue();
    }
}
