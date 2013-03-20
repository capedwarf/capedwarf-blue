package org.jboss.test.capedwarf.cluster.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.cluster.support.PrintListener;
import org.jboss.test.capedwarf.cluster.support.PrintServlet;
import org.jboss.test.capedwarf.common.support.JBoss;
import org.jboss.test.capedwarf.common.test.TestBase;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskAlreadyExistsException;
import com.google.appengine.api.taskqueue.TaskOptions;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
@Category(JBoss.class)
public class TasksTest extends TestBase {

    private static final String URL = "/_ah/test";
    private static final String WEB_XML =
            "<web>" +
            " <listener>" +
            "  <listener-class>" + PrintListener.class.getName() + "</listener-class>" +
            " </listener>" +
            " <servlet>" +
            "  <servlet-name>PrintServlet</servlet-name>" +
            "  <servlet-class>" + PrintServlet.class.getName() + "</servlet-class>" +
            " </servlet>" +
            " <servlet-mapping>" +
            "  <servlet-name>PrintServlet</servlet-name>" +
            "  <url-pattern>" + URL + "</url-pattern>" +
            " </servlet-mapping>" +
            "</web>";

    @InSequence(10)
    @Test
    @OperateOnDeployment("dep1")
    public void testSmokeOnDepA() throws Exception {
        System.out.println(">>> testSmokeOnDepA");
        final Queue queue = QueueFactory.getQueue("default");
        queue.add(TaskOptions.Builder.withUrl(URL));
        sync();
    }

    @InSequence(20)
    @Test
    @OperateOnDeployment("dep2")
    public void testSmokeOnDepB() throws Exception {
        System.out.println(">>> testSmokeOnDepB");
        final Queue queue = QueueFactory.getQueue("default");
        queue.add(TaskOptions.Builder.withUrl(URL));
        sync();
    }


    @InSequence(30)
    @Test
    @OperateOnDeployment("dep1")
    public void testOnDepA() throws Exception {
        final Queue queue = QueueFactory.getQueue("default");
        queue.add(TaskOptions.Builder.withUrl(URL));
        sync();
    }

    @InSequence(100)
    @Test
    @OperateOnDeployment("dep1")
    public void testDuplicateNameDepA() throws Exception {
        final Queue queue = QueueFactory.getDefaultQueue();
        queue.add(TaskOptions.Builder.withTaskName("foo").countdownMillis(10 * 1000L));

        sync();
    }

    @InSequence(110)
    @Test (expected = TaskAlreadyExistsException.class)
    @OperateOnDeployment("dep2")
    public void testDuplicateNameDepB() throws Exception {
        final Queue queue = QueueFactory.getDefaultQueue();
        queue.add(TaskOptions.Builder.withTaskName("foo"));
        sync();
    }

    @InSequence(1000)
    @Test
    @OperateOnDeployment("dep2")
    public void shuttingDown() throws Exception {
    }


    @Deployment (name = "dep1") @TargetsContainer("container-1")
    public static WebArchive getDeploymentA() {
        return getDeployment();
    }

    @Deployment(name = "dep2") @TargetsContainer("container-2")
    public static WebArchive getDeploymentB() {
        return getDeployment();
    }

    protected static WebArchive getDeployment() {
        final TestContext context = TestContext.asDefault().setWebXmlContent(WEB_XML);
        final WebArchive war = getCapedwarfDeployment(context);
        war.addClasses(PrintServlet.class, PrintListener.class);
        return war;
    }
}
