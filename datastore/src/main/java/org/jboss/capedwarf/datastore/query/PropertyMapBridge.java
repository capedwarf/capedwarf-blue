/*
 *
 *  * JBoss, Home of Professional Open Source.
 *  * Copyright 2011, Red Hat, Inc., and individual contributors
 *  * as indicated by the @author tags. See the copyright.txt file in the
 *  * distribution for a full listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.jboss.capedwarf.datastore.query;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.User;
import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.BridgeFactory;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;

import java.util.Date;
import java.util.Map;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class PropertyMapBridge implements FieldBridge {
    public static final TwoWayFieldBridge TEXT_BRIDGE = new TextBridge();
    public static final TwoWayFieldBridge PHONE_NUMBER_BRIDGE = new PhoneNumberBridge();
    public static final TwoWayFieldBridge POSTAL_ADDRESS_BRIDGE = new PostalAddressBridge();
    public static final TwoWayFieldBridge EMAIL_BRIDGE = new EmailBridge();
    public static final TwoWayFieldBridge USER_BRIDGE = new UserBridge();
    public static final TwoWayFieldBridge LINK_BRIDGE = new LinkBridge();
    public static final TwoWayFieldBridge KEY_BRIDGE = new KeyBridge();
    public static final TwoWayFieldBridge RATING_BRIDGE = new RatingBridge();
    public static final TwoWayFieldBridge GEO_PT_BRIDGE = new GeoPtBridge();
    public static final TwoWayFieldBridge CATEGORY_BRIDGE = new CategoryBridge();
    public static final TwoWayFieldBridge IM_HANDLE_BRIDGE = new IMHandleBridge();
    public static final TwoWayFieldBridge BLOB_KEY_BRIDGE = new BlobKeyBridge();
    public static final TwoWayFieldBridge BLOB_BRIDGE = new BlobBridge();
    public static final TwoWayFieldBridge SHORT_BLOB_BRIDGE = new ShortBlobBridge();

    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        Map<String, ?> entityProperties = (Map<String, ?>) value;
        for (Map.Entry<String, ?> entry : entityProperties.entrySet()) {
            luceneOptions.addFieldToDocument(entry.getKey(), convertToString(entry.getValue()), document);
        }
    }

    public String convertToString(Object value) {
        if (value instanceof String) {
            return String.valueOf(value);
//            return BridgeFactory.STRING.objectToString(value);
        } else if (value instanceof Boolean) {
            return BridgeFactory.BOOLEAN.objectToString(value);
        } else if (value instanceof Integer) {
            return BridgeFactory.INTEGER.objectToString(value);
        } else if (value instanceof Byte) {
            return BridgeFactory.INTEGER.objectToString(value);
        } else if (value instanceof Short) {
            return BridgeFactory.INTEGER.objectToString(value);
        } else if (value instanceof Long) {
            return BridgeFactory.LONG.objectToString(value);
        } else if (value instanceof Float) {
            return BridgeFactory.FLOAT.objectToString(value);
        } else if (value instanceof Double) {
            return BridgeFactory.DOUBLE.objectToString(value);
        } else if (value instanceof Date) {
            return BridgeFactory.DATE_MILLISECOND.objectToString(value);

        } else if (value instanceof Text) {
            return TEXT_BRIDGE.objectToString(value);
        } else if (value instanceof PhoneNumber) {
            return PHONE_NUMBER_BRIDGE.objectToString(value);
        } else if (value instanceof PostalAddress) {
            return POSTAL_ADDRESS_BRIDGE.objectToString(value);
        } else if (value instanceof Email) {
            return EMAIL_BRIDGE.objectToString(value);
        } else if (value instanceof User) {
            return USER_BRIDGE.objectToString(value);
        } else if (value instanceof Link) {
            return LINK_BRIDGE.objectToString(value);
        } else if (value instanceof Key) {
            return KEY_BRIDGE.objectToString(value);
        } else if (value instanceof Rating) {
            return RATING_BRIDGE.objectToString(value);
        } else if (value instanceof GeoPt) {
            return GEO_PT_BRIDGE.objectToString(value);
        } else if (value instanceof Category) {
            return CATEGORY_BRIDGE.objectToString(value);
        } else if (value instanceof IMHandle) {
            return IM_HANDLE_BRIDGE.objectToString(value);
        } else if (value instanceof BlobKey) {
            return BLOB_KEY_BRIDGE.objectToString(value);
        } else if (value instanceof Blob) {
            return BLOB_BRIDGE.objectToString(value);
        } else if (value instanceof ShortBlob) {
            return SHORT_BLOB_BRIDGE.objectToString(value);
        }

        throw new IllegalArgumentException("Cannot convert value to string. Value was " + value);
    }

    private static class TextBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((Text) object).getValue();
        }
    }

    private static class PhoneNumberBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((PhoneNumber) object).getNumber();
        }
    }

    private static class PostalAddressBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((PostalAddress) object).getAddress();
        }
    }

    private static class EmailBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((Email) object).getEmail();
        }
    }

    private static class UserBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((User) object).getEmail();    // TODO: add other properties
        }
    }

    private static class LinkBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((Link) object).getValue();
        }
    }

    private static class KeyBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((Key) object).getKind();    // TODO: add other properties
        }
    }

    private static class RatingBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return BridgeFactory.INTEGER.objectToString(((Rating) object).getRating());
        }
    }

    private static class GeoPtBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return BridgeFactory.FLOAT.objectToString(((GeoPt) object).getLatitude())
                    + ";"
                    + BridgeFactory.FLOAT.objectToString(((GeoPt) object).getLongitude());
        }
    }

    private static class CategoryBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((Category) object).getCategory();
        }
    }

    private static class IMHandleBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((IMHandle) object).getProtocol()
                    + ";"
                    + ((IMHandle) object).getAddress();
        }
    }

    private static class BlobKeyBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            return ((BlobKey) object).getKeyString();
        }
    }

    private static class BlobBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            byte[] bytes = ((Blob) object).getBytes();
            return bytesToString(bytes);
        }
    }

    private static class ShortBlobBridge extends ObjectToStringFieldBridge {
        public String objectToString(Object object) {
            byte[] bytes = ((ShortBlob) object).getBytes();
            return bytesToString(bytes);
        }
    }

    private static String bytesToString(byte bytes[]) {
        // TODO: This impl is temporary. Find better one.
        StringBuilder sbuf = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toString(aByte, 16);
            String twoCharHex = (hex.length() == 1 ? "0" : "") + hex;
            sbuf.append(twoCharHex);
        }
        return sbuf.toString();
    }

    /**
     *
     */
    public abstract static class ObjectToStringFieldBridge implements TwoWayFieldBridge {
        public Object get(String name, Document document) {
            throw new UnsupportedOperationException("should not be called");
        }

        public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
            throw new UnsupportedOperationException("should not be called");
        }
    }
}

