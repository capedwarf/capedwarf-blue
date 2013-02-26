package org.jboss.test.capedwarf.tasks.support;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
* @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
*/
public class RequestData {

    private Map<String, String> headers = new HashMap<String, String>();

    public RequestData(HttpServletRequest req) {
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            headers.put(header, req.getHeader(header));
        }
    }

    public String getHeader(String name) {
        return headers.get(name);
    }
}
