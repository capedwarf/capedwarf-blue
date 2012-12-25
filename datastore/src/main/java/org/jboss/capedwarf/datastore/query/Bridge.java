/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.capedwarf.datastore.query;

import java.util.Collection;
import java.util.Date;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.IMHandle;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.PhoneNumber;
import com.google.appengine.api.datastore.PostalAddress;
import com.google.appengine.api.datastore.Rating;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import org.hibernate.search.bridge.TwoWayStringBridge;
import org.hibernate.search.bridge.builtin.BooleanBridge;
import org.hibernate.search.bridge.builtin.FloatBridge;
import org.hibernate.search.bridge.builtin.StringBridge;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public enum Bridge implements TwoWayStringBridge {

    // WARNING: DO NOT CHANGE NAMES (they are stored in the datastore - see Projections)

    NULL("000", new NullBridge()),

    LONG("010", new LongBridge()),
    RATING("010", new RatingBridge()),
    DATE("010", new DateBridge()),

    BOOLEAN("020", new BooleanBridge()),

    //    SHORT_BLOB("030", new ShortBlobBridge()),
    SHORT_BLOB("040", new ShortBlobBridge()),

    STRING("040", StringBridge.INSTANCE),
    PHONE_NUMBER("040", new PhoneNumberBridge()),
    POSTAL_ADDRESS("040", new PostalAddressBridge()),
    EMAIL("040", new EmailBridge()),
    IM_HANDLE("040", new IMHandleBridge()),
    LINK("040", new LinkBridge()),
    CATEGORY("040", new CategoryBridge()),
    BLOB_KEY("040", new BlobKeyBridge()),

    DOUBLE("050", new DoubleBridge()),
    FLOAT("050", new FloatBridge()),

    GEO_PT("060", new GeoPtBridge()),

    USER("070", new UserBridge()),

    KEY("080", new KeyBridge()),

    COLLECTION("999", new CollectionBridge()),
    TEXT("999", new TextBridge()),
    BLOB("999", new BlobBridge()),
    EMBEDDED_ENTITY("999", new EmbeddedEntityBridge());


    private TwoWayStringBridge bridge;

    private Bridge(String orderPrefix, TwoWayStringBridge bridge) {
        this.bridge = new OrderingWrapper(orderPrefix, bridge);
    }

    public String objectToString(Object object) {
        return bridge.objectToString(object);
    }

    public Object stringToObject(String stringValue) {
        return bridge.stringToObject(stringValue);
    }

    public static Bridge matchBridge(Object value) {
        if (value == null) {
            return NULL;
        }

        if (value instanceof String) {
            return STRING;
        } else if (value instanceof Collection) {
            return COLLECTION;
        } else if (value instanceof Boolean) {
            return BOOLEAN;
        } else if (value instanceof Float || value instanceof Double) {
            return DOUBLE;
        } else if (value instanceof Number) {
            return LONG;
        } else if (value instanceof Date) {
            return DATE;
        } else if (value instanceof Text) {
            return TEXT;
        } else if (value instanceof PhoneNumber) {
            return PHONE_NUMBER;
        } else if (value instanceof PostalAddress) {
            return POSTAL_ADDRESS;
        } else if (value instanceof Email) {
            return EMAIL;
        } else if (value instanceof User) {
            return USER;
        } else if (value instanceof Link) {
            return LINK;
        } else if (value instanceof Key) {
            return KEY;
        } else if (value instanceof Rating) {
            return RATING;
        } else if (value instanceof GeoPt) {
            return GEO_PT;
        } else if (value instanceof Category) {
            return CATEGORY;
        } else if (value instanceof IMHandle) {
            return IM_HANDLE;
        } else if (value instanceof BlobKey) {
            return BLOB_KEY;
        } else if (value instanceof Blob) {
            return BLOB;
        } else if (value instanceof ShortBlob) {
            return SHORT_BLOB;
        } else if (value instanceof EmbeddedEntity) {
            return EMBEDDED_ENTITY;
        }
        throw new IllegalArgumentException("No matching bridge. Value was " + value);
    }

    public static class NullBridge implements TwoWayStringBridge {
        public static final String NULL_TOKEN = "__capedwarf___NULL___";

        public Object stringToObject(String stringValue) {
            return null;
        }

        public String objectToString(Object object) {
            return NULL_TOKEN;
        }
    }

    private static class CollectionBridge implements TwoWayStringBridge {
        public Object stringToObject(String stringValue) {
            return null;  // TODO
        }

        public String objectToString(Object object) {
            return object.toString();
        }
    }

    private static class DoubleBridge implements TwoWayStringBridge {
        public String objectToString(Object object) {
            return double2sortableStr(((Number) object).doubleValue());
        }

        public Object stringToObject(String stringValue) {
            long f = sortableStr2long(stringValue);
            if (f < 0) f ^= 0x7fffffffffffffffL;
            return Double.longBitsToDouble(f);
        }

        public static String double2sortableStr(double val) {
            long f = Double.doubleToRawLongBits(val);
            if (f < 0) f ^= 0x7fffffffffffffffL;
            return long2sortableStr(f);
        }

        // uses binary representation of an int to build a string of
        // chars that will sort correctly.  Only char ranges
        // less than 0xd800 will be used to avoid UCS-16 surrogates.
        // we can use the lowest 15 bits of a char, (or a mask of 0x7fff)
        private static String long2sortableStr(long val) {
            char[] out = new char[5];
            int offset = 0;
            val += Long.MIN_VALUE;
            out[offset++] = (char) (val >>> 60);
            out[offset++] = (char) (val >>> 45 & 0x7fff);
            out[offset++] = (char) (val >>> 30 & 0x7fff);
            out[offset++] = (char) (val >>> 15 & 0x7fff);
            out[offset] = (char) (val & 0x7fff);
            return new String(out);
        }

        private static long sortableStr2long(String sval) {
            char[] out = sval.toCharArray();
            int offset = 0;
            long val = (long) (out[offset++]) << 60;
            val |= ((long) out[offset++]) << 45;
            val |= ((long) out[offset++]) << 30;
            val |= out[offset++] << 15;
            val |= out[offset];
            val -= Long.MIN_VALUE;
            return val;
        }
    }

    private static class LongBridge implements TwoWayStringBridge {

        private static final int RADIX = 36;

        private static final char NEGATIVE_PREFIX = '-';

        // NB: NEGATIVE_PREFIX must be < POSITIVE_PREFIX
        private static final char POSITIVE_PREFIX = '0';

        //NB: this must be less than
        /**
         * Equivalent to longToString(Long.MIN_VALUE)
         */
        public static final String MIN_STRING_VALUE = NEGATIVE_PREFIX + "0000000000000";
        /**
         * Equivalent to longToString(Long.MAX_VALUE)
         */
        public static final String MAX_STRING_VALUE = POSITIVE_PREFIX + "1y2p0ij32e8e7";

        /**
         * the length of (all) strings returned by [EMAIL PROTECTED] #longToString}
         */
        public static final int STR_SIZE = MIN_STRING_VALUE.length();

        public String objectToString(Object object) {
            long num = ((Number) object).longValue();
            return longToString(num);
        }

        public Object stringToObject(String stringValue) {
            if (MIN_STRING_VALUE.equals(stringValue))
                return Long.MIN_VALUE;

            char prefix = stringValue.charAt(0);
            long num = Long.parseLong(stringValue.substring(1), RADIX);
            return (prefix == NEGATIVE_PREFIX) ? num - Long.MAX_VALUE - 1 : num;
        }

        /**
         * Converts a long to a String suitable for indexing.
         */
        public static String longToString(long num) {

            if (num == Long.MIN_VALUE) {
                // special case, because long is not symetric around zero
                return MIN_STRING_VALUE;
            }

            StringBuilder buf = new StringBuilder(STR_SIZE);

            if (num < 0) {
                buf.append(NEGATIVE_PREFIX);
                num = Long.MAX_VALUE + num + 1;
            } else {
                buf.append(POSITIVE_PREFIX);
            }
            String numStr = Long.toString(num, RADIX);

            int padLen = STR_SIZE - numStr.length() - buf.length();
            while (padLen-- > 0) {
                buf.append('0');
            }
            buf.append(numStr);

            return buf.toString();
        }

    }

    private static class TextBridge implements TwoWayStringBridge {
        public String objectToString(Object object) {
            return ((Text) object).getValue();
        }

        public Object stringToObject(String stringValue) {
            return new Text(stringValue);
        }
    }

    private static class PhoneNumberBridge implements TwoWayStringBridge {
        public String objectToString(Object object) {
            return ((PhoneNumber) object).getNumber();
        }

        public Object stringToObject(String stringValue) {
            return new PhoneNumber(stringValue);
        }
    }

    private static class PostalAddressBridge implements TwoWayStringBridge {
        public String objectToString(Object object) {
            return ((PostalAddress) object).getAddress();
        }

        public Object stringToObject(String stringValue) {
            return new PostalAddress(stringValue);
        }
    }

    private static class EmailBridge implements TwoWayStringBridge {
        public String objectToString(Object object) {
            return ((Email) object).getEmail();
        }

        public Object stringToObject(String stringValue) {
            return new Email(stringValue);
        }
    }

    private static class UserBridge implements TwoWayStringBridge {
        public String objectToString(Object object) {
            return ((User) object).getEmail();    // TODO: add other properties
        }

        public Object stringToObject(String stringValue) {
            return new User(stringValue, null);
        }
    }

    private static class LinkBridge implements TwoWayStringBridge {
        public String objectToString(Object object) {
            return ((Link) object).getValue();
        }

        public Object stringToObject(String stringValue) {
            return new Link(stringValue);
        }
    }

    private static class KeyBridge implements TwoWayStringBridge {
        private GAEKeyTransformer keyTransformer = new GAEKeyTransformer();

        public String objectToString(Object object) {
            return keyTransformer.toString((Key) object);
        }

        public Object stringToObject(String stringValue) {
            return keyTransformer.fromString(stringValue);
        }
    }

    private static class RatingBridge implements TwoWayStringBridge {
        private LongBridge longBridge = new LongBridge();

        public String objectToString(Object object) {
            int rating = Rating.class.cast(object).getRating();
            return longBridge.objectToString(rating);
        }

        public Object stringToObject(String stringValue) {
            return new Rating(Number.class.cast(longBridge.stringToObject(stringValue)).intValue());
        }
    }

    private static class GeoPtBridge implements TwoWayStringBridge {
        private FloatBridge floatBridge;

        public String objectToString(Object object) {
            floatBridge = new FloatBridge();
            return floatBridge.objectToString(((GeoPt) object).getLatitude()) + ";" + floatBridge.objectToString(((GeoPt) object).getLongitude());
        }

        public Object stringToObject(String stringValue) {
            String[] pair = stringValue.split(";");
            float latitude = (Float)floatBridge.stringToObject(pair[0]);
            float longitude = (Float)floatBridge.stringToObject(pair[1]);
            return new GeoPt(latitude, longitude);
        }
    }

    private static class CategoryBridge implements TwoWayStringBridge {
        public String objectToString(Object object) {
            return ((Category) object).getCategory();
        }

        public Object stringToObject(String stringValue) {
            return new Category(stringValue);
        }
    }

    private static class IMHandleBridge implements TwoWayStringBridge {
        public String objectToString(Object object) {
            return ((IMHandle) object).getProtocol() + " " + ((IMHandle) object).getAddress();
        }

        public Object stringToObject(String stringValue) {
            int spaceIndex = stringValue.indexOf(' ');
            return new IMHandle(IMHandle.Scheme.valueOf(stringValue.substring(0, spaceIndex)), stringValue.substring(spaceIndex+1));
        }
    }

    private static class BlobKeyBridge implements TwoWayStringBridge {
        public String objectToString(Object object) {
            return ((BlobKey) object).getKeyString();
        }

        public Object stringToObject(String stringValue) {
            return new BlobKey(stringValue);
        }
    }

    private static class BlobBridge implements TwoWayStringBridge {
        public String objectToString(Object object) {
            byte[] bytes = ((Blob) object).getBytes();
            return bytesToString(bytes);
        }

        public Object stringToObject(String stringValue) {
            return new Blob(stringToBytes(stringValue));
        }
    }

    private static class ShortBlobBridge implements TwoWayStringBridge {
        public String objectToString(Object object) {
            byte[] bytes = ((ShortBlob) object).getBytes();
            return new String(bytes);
        }

        public Object stringToObject(String stringValue) {
            return new ShortBlob(stringValue.getBytes());
        }
    }

    // TODO -- check this
    private static class EmbeddedEntityBridge implements TwoWayStringBridge {
        public static final String EMBEDDED_TOKEN = "__capedwarf___EMBEDDED___";

        public Object stringToObject(String stringValue) {
            return null;
        }

        public String objectToString(Object object) {
            return EMBEDDED_TOKEN;
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

    private static byte[] stringToBytes(String string) {
        byte[] bytes = new byte[string.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int j = i * 2;
            String hex = string.substring(j, j + 2);
            bytes[i] = (byte) Integer.parseInt(hex, 16);
        }
        return bytes;
    }

    private static class DateBridge implements TwoWayStringBridge {

        private LongBridge longBridge = new LongBridge();

        @Override
        public Object stringToObject(String stringValue) {
            long value = (Long) longBridge.stringToObject(stringValue);
            return new Date(value / 1000L);
        }

        @Override
        public String objectToString(Object object) {
            return longBridge.objectToString(((Date)object).getTime() * 1000L);
        }
    }

    private static class OrderingWrapper implements TwoWayStringBridge {

        public static final int ORDER_PREFIX_LENGTH = 3;

        private final String orderPrefix;
        private final TwoWayStringBridge bridge;

        public OrderingWrapper(String orderPrefix, TwoWayStringBridge bridge) {
            if (orderPrefix.length() != ORDER_PREFIX_LENGTH) {
                throw new IllegalArgumentException("invalid length, orderPrefix=" + orderPrefix);
            }
            this.orderPrefix = orderPrefix;
            this.bridge = bridge;
        }

        @Override
        public Object stringToObject(String stringValue) {
            return bridge.stringToObject(removeOrderingPrefix(stringValue));
        }

        @Override
        public String objectToString(Object object) {
            return addOrderingPrefix(bridge.objectToString(object));
        }

        private String addOrderingPrefix(String str) {
            return orderPrefix + ":" + str;
        }

        private String removeOrderingPrefix(String stringValue) {
            return stringValue.substring(ORDER_PREFIX_LENGTH + 1);
        }

    }
}

