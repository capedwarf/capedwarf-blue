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

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Category;
import com.google.appengine.api.datastore.Email;
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
import com.google.common.collect.Sets;
import org.hibernate.search.bridge.TwoWayStringBridge;

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

    STRING("040", new StringBridge()),
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

    private OrderingPrefixer orderingPrefixer;
    private BridgeSpi bridge;

    private Bridge(String orderPrefix, BridgeSpi bridge) {
        this.orderingPrefixer = new OrderingPrefixer(orderPrefix);
        this.bridge = bridge;
    }

    public String objectToString(Object object) {
        String str = bridge.objectToString(object);
        return orderingPrefixer.addOrderingPrefix(str);
    }

    public Object stringToObject(String stringValue) {
        stringValue = orderingPrefixer.removeOrderingPrefix(stringValue);
        return bridge.stringToObject(stringValue);
    }

    public static Bridge getBridge(Class<?> type) {
        for (Bridge bridge : values()) {
            Set<Class<?>> types = bridge.bridge.types();
            if (types != null && types.contains(type)) {
                return bridge;
            }
        }
        throw new IllegalArgumentException("No bridge found for type " + type);
    }

    public boolean isAssignableTo(Class<?> type) {
        Set<Class<?>> types = bridge.types();
        if (types == null) {
            return true;
        }
        for (Class<?> bt : types) {
            if (type.isAssignableFrom(bt)) {
                return true;
            }
        }
        return false;
    }

    public static Bridge matchBridge(Object value) {
        if (value == null) {
            return NULL;
        }

        if (value instanceof Collection) {
            return COLLECTION;
        } else {
            return getBridge(value.getClass());
        }
    }

    public Object convertValue(Object value) {
        return bridge.convertValue(value);
    }

    public Object getValue(String value) {
        value = orderingPrefixer.removeOrderingPrefix(value);
        return bridge.getValue(value);
    }

    public static void checkType(Object value, Class<?> clazz) {
        if (!clazz.isInstance(value)) {
            throw new IllegalArgumentException("Type mismatch");
        }
    }

    public static class NullBridge implements BridgeSpi {
        public static final String NULL_TOKEN = "__capedwarf___NULL___";

        public Set<Class<?>> types() {
            return null;
        }

        public Object stringToObject(String stringValue) {
            return null;
        }

        public String objectToString(Object object) {
            return NULL_TOKEN;
        }

        @Override
        public Object getValue(String value) {
            return null;
        }

        @Override
        public Object convertValue(Object value) {
            return null;
        }
    }

    protected abstract static class BuiltInBridge implements BridgeSpi {
        private TwoWayStringBridge bridge;

        protected BuiltInBridge(TwoWayStringBridge bridge) {
            this.bridge = bridge;
        }

        public Object stringToObject(String stringValue) {
            return bridge.stringToObject(stringValue);
        }

        public String objectToString(Object object) {
            return bridge.objectToString(object);
        }
    }

    public static class BooleanBridge extends BuiltInBridge {
        public BooleanBridge() {
            super(new org.hibernate.search.bridge.builtin.BooleanBridge());
        }

        public Set<Class<?>> types() {
            return Collections.<Class<?>>singleton(Boolean.class);
        }

        @Override
        public Object getValue(String value) {
            return stringToObject(value);
        }

        @Override
        public Object convertValue(Object value) {
            checkType(value, Boolean.class);
            return value;
        }
    }

    public static class FloatBridge extends BuiltInBridge {
        public FloatBridge() {
            super(new org.hibernate.search.bridge.builtin.FloatBridge());
        }

        public Set<Class<?>> types() {
            return Sets.<Class<?>>newHashSet(Float.class, Double.class);
        }

        @Override
        public Object getValue(String value) {
            return null;
        }

        @Override
        public Object convertValue(Object value) {
            return null;
        }
    }

    public static class StringBridge extends BuiltInBridge {
        public StringBridge() {
            super(org.hibernate.search.bridge.builtin.StringBridge.INSTANCE);
        }

        public Set<Class<?>> types() {
            return Collections.<Class<?>>singleton(String.class);
        }

        public Object getValue(String value) {
            return toUTF8ByteArray(value);
        }

        public Object convertValue(Object value) {
            checkType(value, byte[].class);
            return stringToObject(fromUTF8ByteArray((byte[]) value));
        }

        public static byte[] toUTF8ByteArray(String string) {
            try {
                return string.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new InternalError("UTF-8 not supported on this platform");
            }
        }

        public static String fromUTF8ByteArray(byte[] array) {
            try {
                return new String(array, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new InternalError("UTF-8 not supported on this platform");
            }
        }
    }

    private static class CollectionBridge implements BridgeSpi {
        public Set<Class<?>> types() {
            return null; // return null, as this is checked against elements
        }

        public Object stringToObject(String stringValue) {
            return null; // TODO
        }

        public String objectToString(Object object) {
            return object.toString();
        }

        @Override
        public Object getValue(String value) {
            return null;
        }

        @Override
        public Object convertValue(Object value) {
            return null;
        }
    }

    private static class DoubleBridge implements BridgeSpi {
        public Set<Class<?>> types() {
            return Sets.<Class<?>>newHashSet(Float.class, Double.class);
        }

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

        @Override
        public Object getValue(String value) {
            return stringToObject(value);
        }

        @Override
        public Object convertValue(Object value) {
            checkType(value, Double.class);
            return value;
        }
    }

    private static class LongBridge implements BridgeSpi {

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

        public Set<Class<?>> types() {
            return Sets.<Class<?>>newHashSet(Long.class, Integer.class, Short.class, Byte.class);
        }
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

        @Override
        public Object getValue(String value) {
            return stringToObject(value);
        }

        @Override
        public Object convertValue(Object value) {
            checkType(value, Long.class);
            return value;
        }
    }

    private static class TextBridge extends StringTypeBasedBridge {
        public Class<?> type() {
            return Text.class;
        }

        public String objectToString(Object object) {
            return ((Text) object).getValue();
        }

        public Object stringToObject(String stringValue) {
            return new Text(stringValue);
        }
    }

    private static abstract class StringTypeBasedBridge extends AbstractBridgeSpi {

        public Object getValue(String value) {
            return Bridge.StringBridge.toUTF8ByteArray(value);
        }

        public Object convertValue(Object value) {
            checkType(value, byte[].class);
            return stringToObject(Bridge.StringBridge.fromUTF8ByteArray((byte[]) value));
        }
    }

    private static class PhoneNumberBridge extends StringTypeBasedBridge {
        public Class<?> type() {
            return PhoneNumber.class;
        }

        public String objectToString(Object object) {
            return ((PhoneNumber) object).getNumber();
        }

        public Object stringToObject(String stringValue) {
            return new PhoneNumber(stringValue);
        }
    }

    private static class PostalAddressBridge extends StringTypeBasedBridge {
        public Class<?> type() {
            return PostalAddress.class;
        }

        public String objectToString(Object object) {
            return ((PostalAddress) object).getAddress();
        }

        public Object stringToObject(String stringValue) {
            return new PostalAddress(stringValue);
        }
    }

    private static class EmailBridge extends StringTypeBasedBridge {
        public Class<?> type() {
            return Email.class;
        }

        public String objectToString(Object object) {
            return ((Email) object).getEmail();
        }

        public Object stringToObject(String stringValue) {
            return new Email(stringValue);
        }
    }

    private static class UserBridge extends AbstractBridgeSpi {
        public Class<?> type() {
            return User.class;
        }

        public String objectToString(Object object) {
            return ((User) object).getEmail();    // TODO: add other properties
        }

        public Object stringToObject(String stringValue) {
            return new User(stringValue, "gmail.com");
        }
    }

    private static class LinkBridge extends StringTypeBasedBridge {
        public Class<?> type() {
            return Link.class;
        }

        public String objectToString(Object object) {
            return ((Link) object).getValue();
        }

        public Object stringToObject(String stringValue) {
            return new Link(stringValue);
        }
    }

    private static class KeyBridge extends AbstractBridgeSpi {
        public Class<?> type() {
            return Key.class;
        }

        public String objectToString(Object object) {
            return GAEKeyTransformer.to(object);
        }

        public Object stringToObject(String stringValue) {
            return GAEKeyTransformer.from(stringValue);
        }
    }

    private static class RatingBridge extends LongTypeBasedBridge<Rating> {

        public Class<Rating> type() {
            return Rating.class;
        }

        @Override
        protected long toLong(Rating value) {
            return value.getRating();
        }

        @Override
        protected Rating fromLong(long value) {
            return new Rating((int)value);
        }
    }

    private static class GeoPtBridge extends AbstractBridgeSpi {
        private FloatBridge floatBridge = new FloatBridge();

        public Class<?> type() {
            return GeoPt.class;
        }

        public String objectToString(Object object) {
            return floatBridge.objectToString(((GeoPt) object).getLatitude()) + ";" + floatBridge.objectToString(((GeoPt) object).getLongitude());
        }

        public Object stringToObject(String stringValue) {
            String[] pair = stringValue.split(";");
            float latitude = (Float)floatBridge.stringToObject(pair[0]);
            float longitude = (Float)floatBridge.stringToObject(pair[1]);
            return new GeoPt(latitude, longitude);
        }
    }

    private static class CategoryBridge extends StringTypeBasedBridge {
        public Class<?> type() {
            return Category.class;
        }

        public String objectToString(Object object) {
            return ((Category) object).getCategory();
        }

        public Object stringToObject(String stringValue) {
            return new Category(stringValue);
        }
    }

    private static class IMHandleBridge extends StringTypeBasedBridge {
        public Class<?> type() {
            return IMHandle.class;
        }

        public String objectToString(Object object) {
            return ((IMHandle) object).getProtocol() + " " + ((IMHandle) object).getAddress();
        }

        public Object stringToObject(String stringValue) {
            int spaceIndex = stringValue.indexOf(' ');
            return new IMHandle(IMHandle.Scheme.valueOf(stringValue.substring(0, spaceIndex)), stringValue.substring(spaceIndex+1));
        }
    }

    private static class BlobKeyBridge extends StringTypeBasedBridge {
        public Class<?> type() {
            return BlobKey.class;
        }

        public String objectToString(Object object) {
            return ((BlobKey) object).getKeyString();
        }

        public Object stringToObject(String stringValue) {
            return new BlobKey(stringValue);
        }
    }

    private static class BlobBridge extends StringTypeBasedBridge {
        public Class<?> type() {
            return Blob.class;
        }

        public String objectToString(Object object) {
            byte[] bytes = ((Blob) object).getBytes();
            return bytesToString(bytes);
        }

        public Object stringToObject(String stringValue) {
            return new Blob(stringToBytes(stringValue));
        }

        private String bytesToString(byte bytes[]) {
            // TODO: This impl is temporary. Find better one.
            StringBuilder sbuf = new StringBuilder();
            for (byte aByte : bytes) {
                String hex = Integer.toString(aByte, 16);
                String twoCharHex = (hex.length() == 1 ? "0" : "") + hex;
                sbuf.append(twoCharHex);
            }
            return sbuf.toString();
        }

        private byte[] stringToBytes(String string) {
            byte[] bytes = new byte[string.length() / 2];
            for (int i = 0; i < bytes.length; i++) {
                int j = i * 2;
                String hex = string.substring(j, j + 2);
                bytes[i] = (byte) Integer.parseInt(hex, 16);
            }
            return bytes;
        }
    }

    private static class ShortBlobBridge extends StringTypeBasedBridge {
        public Class<?> type() {
            return ShortBlob.class;
        }

        public String objectToString(Object object) {
            byte[] bytes = ((ShortBlob) object).getBytes();
            return new String(bytes);
        }

        public Object stringToObject(String stringValue) {
            return new ShortBlob(stringValue.getBytes());
        }
    }

    // TODO -- check this
    private static class EmbeddedEntityBridge extends AbstractBridgeSpi {
        public static final String EMBEDDED_TOKEN = "__capedwarf___EMBEDDED___";

        public Class<?> type() {
            return Object.class;
        }

        public Object stringToObject(String stringValue) {
            return null;
        }

        public String objectToString(Object object) {
            return EMBEDDED_TOKEN;
        }
    }

    private static abstract class LongTypeBasedBridge<T> extends AbstractBridgeSpi {
        private LongBridge longBridge = new LongBridge();

        @Override
        protected abstract Class<T> type();

        @Override
        public Object getValue(String value) {
            return longBridge.stringToObject(value);
        }

        @Override
        public Object convertValue(Object value) {
            checkType(value, Long.class);
            return fromLong(Long.class.cast(value));
        }

        @SuppressWarnings("unchecked")
        @Override
        public String objectToString(Object object) {
            return longBridge.objectToString(toLong((T) object));
        }

        @Override
        public Object stringToObject(String stringValue) {
            return convertValue(getValue(stringValue));
        }

        protected abstract long toLong(T value);

        protected abstract T fromLong(long value);
    }

    private static class DateBridge extends LongTypeBasedBridge<Date> {

        public Class<Date> type() {
            return Date.class;
        }

        protected long toLong(Date date) {
            return date.getTime() * 1000L;
        }

        @Override
        protected Date fromLong(long usec) {
            return new Date(usec / 1000L);
        }
    }

    private class OrderingPrefixer {

        public static final int ORDER_PREFIX_LENGTH = 3;

        private final String orderPrefix;

        public OrderingPrefixer(String orderPrefix) {
            if (orderPrefix.length() != ORDER_PREFIX_LENGTH) {
                throw new IllegalArgumentException("invalid length, orderPrefix=" + orderPrefix);
            }
            this.orderPrefix = orderPrefix;
        }

        private String addOrderingPrefix(String str) {
            return orderPrefix + ":" + str;
        }

        private String removeOrderingPrefix(String stringValue) {
            return stringValue.substring(ORDER_PREFIX_LENGTH + 1);
        }

    }
}

