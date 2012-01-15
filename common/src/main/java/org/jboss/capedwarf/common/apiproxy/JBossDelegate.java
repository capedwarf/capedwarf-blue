package org.jboss.capedwarf.common.apiproxy;

import com.google.apphosting.api.ApiProxy;
import org.jboss.capedwarf.common.threads.ExecutorFactory;

import javax.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JBoss Delegate impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossDelegate implements ApiProxy.Delegate<ApiProxy.Environment> {

    public static final JBossDelegate INSTANCE = new JBossDelegate();

    private final Logger log = Logger.getLogger(getClass().getName()); 
    private final Map<Thread, ServletRequest> threads = new ConcurrentHashMap<Thread, ServletRequest>();

    private JBossDelegate() {
    }

    public byte[] makeSyncCall(ApiProxy.Environment environment, String packageName, String methodName, byte[] bytes) throws ApiProxy.ApiProxyException {
        return bytes;
    }

    public Future<byte[]> makeAsyncCall(ApiProxy.Environment environment, String packageName, String methodName, final byte[] bytes, ApiProxy.ApiConfig apiConfig) {
        final FutureTask<byte[]> task = new FutureTask<byte[]>(new Callable<byte[]>() {
            public byte[] call() throws Exception {
                return bytes;
            }
        });
        executeTask(task);
        return task;
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
    
    private void executeTask(FutureTask<?> task) {
        final Executor executor = ExecutorFactory.getInstance();
        executor.execute(task);
    }
}
