package org.jboss.test.capedwarf.tasks.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.jboss.capedwarf.common.io.IOUtils;


/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class RequestData {

    private byte[] body;
    private Map<String, String> headers = new HashMap<String, String>();

    public RequestData(HttpServletRequest req) throws IOException {
        storeHeaders(req);
        storeBody(req);
    }

    private void storeHeaders(HttpServletRequest req) {
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            headers.put(header, req.getHeader(header));
        }
    }

    private void storeBody(HttpServletRequest req) throws IOException {
        ServletInputStream in = req.getInputStream();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copyStream(in, baos);
            body = baos.toByteArray();
        } finally {
            in.close();
        }
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public byte[] getBody() {
        return body;
    }
}
