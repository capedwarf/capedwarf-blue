package org.jboss.capedwarf.common.apiproxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletRequest;

import com.google.apphosting.api.ApiProxy;
import org.jboss.capedwarf.common.threads.ExecutorFactory;

/**
 * JBoss Delegate impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapedwarfDelegate implements ApiProxy.Delegate<ApiProxy.Environment> {

    public static final CapedwarfDelegate INSTANCE = new CapedwarfDelegate();

    private final Logger log = Logger.getLogger(getClass().getName()); 
    private final Map<Thread, ServletRequest> threads = new ConcurrentHashMap<Thread, ServletRequest>();

    private CapedwarfDelegate() {
    }

    public byte[] makeSyncCall(ApiProxy.Environment environment, String packageName, String methodName, byte[] bytes) throws ApiProxy.ApiProxyException {
        return bytes;
    }

    public Future<byte[]> makeAsyncCall(ApiProxy.Environment environment, String packageName, String methodName, final byte[] bytes, ApiProxy.ApiConfig apiConfig) {
        return ExecutorFactory.wrap(new Callable<byte[]>() {
            public byte[] call() throws Exception {
                return bytes;
            }
        });
    }

    public void log(ApiProxy.Environment environment, ApiProxy.LogRecord logRecord) {
        log.log(toLevel(logRecord.getLevel()), logRecord.getMessage());
    }

    public void flushLogs(ApiProxy.Environment environment) {
    }

    public List<Thread> getRequestThreads(ApiProxy.Environment environment) {
        return new ArrayList<Thread>(threads.keySet());
    }

    public void addRequest(ServletRequest request) {
        threads.put(Thread.currentThread(), request);
    }

    public void removeRequest() {
        threads.remove(Thread.currentThread());
    }

    public ServletRequest getServletRequest() {
        return threads.get(Thread.currentThread());
    }

    private Level toLevel(ApiProxy.LogRecord.Level level) {
        switch (level) {
            case debug: return Level.FINE;
            case error: return Level.SEVERE;
            case fatal: return Level.SEVERE;
            case info: return Level.INFO;
            case warn: return Level.WARNING;
        }
        return Level.OFF;
    }
}
