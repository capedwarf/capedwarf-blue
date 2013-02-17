package org.jboss.capedwarf.appidentity;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jboss.capedwarf.blobstore.ExposedBlobstoreService;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class CapedwarfHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private int contentLength;
    private String blobKey;
    private String blobRange;

    public CapedwarfHttpServletResponseWrapper(HttpServletResponse res) {
        super(res);
    }

    @Override
    public void setContentLength(int len) {
        this.contentLength = len;
        super.setContentLength(len);
    }

    public int getContentLength() {
        return contentLength;
    }

    @Override
    public void addHeader(String name, String value) {
        if (handleBlobHeaders(name, value))
            return;
        super.addHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        if (handleBlobHeaders(name, value))
            return;
        super.setHeader(name, value);
    }

    private boolean handleBlobHeaders(String name, String value) {
        if (ExposedBlobstoreService.BLOB_KEY_HEADER.equals(name)) {
            blobKey = value;
            return true;
        } else if (ExposedBlobstoreService.BLOB_RANGE_HEADER.equals(name)) {
            blobRange = value;
            return true;
        }
        return false;
    }

    public String getBlobKey() {
        return blobKey;
    }

    public String getBlobRange() {
        return blobRange;
    }
}
