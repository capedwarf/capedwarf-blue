package org.jboss.capedwarf.appidentity;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class CapedwarfHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private int contentLength;

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

}
