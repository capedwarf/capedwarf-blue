package org.jboss.capedwarf.common.singlethread;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SingleThreadFilter implements Filter {
    private static ThreadLocal<Object> reentered = new ThreadLocal<Object>();
    private Semaphore semaphore;

    public void init(FilterConfig filterConfig) throws ServletException {
        String mcr = filterConfig.getInitParameter("max-concurrent-requests");
        int maxConcurrentRequests = (mcr != null) ? Integer.parseInt(mcr) : 1;
        semaphore = new Semaphore(maxConcurrentRequests);
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        if (isChannelRequest(servletRequest)) {
            chain.doFilter(servletRequest, servletResponse);
        } else {
            doFilterWithSemaphore(servletRequest, servletResponse, chain);
        }

    }

    private boolean isChannelRequest(ServletRequest servletRequest) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        return request.getRequestURI().startsWith("/_ah/channel");
    }

    private void doFilterWithSemaphore(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        final boolean isNew = (reentered.get() == null);
        try {
            // make the app process one request at a time
            if (isNew) {
                semaphore.acquire();
            }
            try {
                if (isNew) {
                    reentered.set(this);
                }
                try {
                    chain.doFilter(servletRequest, servletResponse);
                } finally {
                    if (isNew) {
                        reentered.remove();
                    }
                }
            } finally {
                if (isNew) {
                    semaphore.release();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    public void destroy() {
    }
}
