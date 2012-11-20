package org.jboss.test.capedwarf.cluster.test;

import java.lang.reflect.Method;
import java.util.logging.Handler;
import java.util.logging.Logger;

import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.BaseTest;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Matej Lazar
 */
@RunWith(Arquillian.class)
public class LoggingTestCase extends BaseTest {

    @Deployment(name = "dep1") @TargetsContainer("container-1")
    public static WebArchive getDeploymentA() {
        return getDeployment();
    }

    @Deployment(name = "dep2") @TargetsContainer("container-2")
    public static WebArchive getDeploymentB() {
        return getDeployment();
    }

    public static WebArchive getDeployment() {
        return getCapedwarfDeployment(new TestContext());
    }

    @InSequence(10)
    @Test
    @OperateOnDeployment("dep1")
    public void writeLogOnDep1() {
        clear(LogServiceFactory.getLogService());

        assertLogDoesntContain("hello");

        Logger log = Logger.getLogger(LoggingTestCase.class.getName());
        log.info("hello");
        waitForSync();
        flush(log);
        waitForSync();
        assertLogContains("hello");
    }

    @InSequence(20)
    @Test
    @OperateOnDeployment("dep2")
    public void readLogOnDep2() {
        waitForSync();
        assertLogContains("hello");
    }

    protected void clear(LogService service) {
        final Class<? extends LogService> clazz = service.getClass();
        if (clazz.getName().contains("JBossLogService")) {
            try {
                Method clearLog = clazz.getMethod("clearLog");
                clearLog.invoke(service);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void assertLogDoesntContain(String text) {
        assertFalse("log should not contain '" + text + "', but it does", logContains(text));
    }

    private void assertLogContains(String text) {
        assertTrue("log should contain '" + text + "', but it does not", logContains(text));
    }

    private boolean logContains(String text) {
        Iterable<RequestLogs> iterable = LogServiceFactory.getLogService().fetch(new LogQuery().includeAppLogs(true));
        for (RequestLogs logs : iterable) {
            for (AppLogLine logLine : logs.getAppLogLines()) {
                if (logLine.getLogMessage().contains(text)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void flush(Logger log) {
        for (Handler handler : log.getHandlers()) {
            handler.flush();
        }
    }

    private void waitForSync() {
        sync();
    }
}
