package org.jboss.capedwarf.common.singlethread;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SingleThreadFilter implements Filter {
    private Semaphore semaphore;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String mcr = filterConfig.getInitParameter("max-concurrent-requests");
        int maxConcurrentRequests = (mcr != null) ? Integer.parseInt(mcr) : 1;
        semaphore = new Semaphore(maxConcurrentRequests);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        try {
            // make the app process one request at a time
            semaphore.acquire();
            try {
                chain.doFilter(servletRequest, servletResponse);
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    @Override
    public void destroy() {
        semaphore = null;
    }
}
