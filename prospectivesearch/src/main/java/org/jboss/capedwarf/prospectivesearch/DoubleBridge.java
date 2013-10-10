package org.jboss.capedwarf.prospectivesearch;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class DoubleBridge {

    public static final DoubleBridge INSTANCE = new DoubleBridge();

    public String objectToString(Object object) {
        return double2sortableStr(((Number) object).doubleValue());
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
}

