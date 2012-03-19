package org.jboss.test.capedwarf.cluster;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.tasks.support.PrintListener;
import org.jboss.test.capedwarf.tasks.support.PrintServlet;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
public class TasksTest {

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
        sleep();
    }

    @InSequence(20)
    @Test
    @OperateOnDeployment("dep2")
    public void testSmokeOnDepB() throws Exception {
        System.out.println(">>> testSmokeOnDepB");
        final Queue queue = QueueFactory.getQueue("default");
        queue.add(TaskOptions.Builder.withUrl(URL));
        sleep();
    }


    // we wait for JMS to kick-in
    private static void sleep() throws InterruptedException {
        Thread.sleep(3000L); // sleep for 3secs
    }

    @Deployment (name = "dep1") @TargetsContainer("container-1")
    public static WebArchive getDeploymentA() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(PrintServlet.class, PrintListener.class)
                .setWebXML(new StringAsset(WEB_XML))
                .addAsWebInfResource("appengine-web.xml");
    }

    @Deployment(name = "dep2") @TargetsContainer("container-2")
    public static WebArchive getDeploymentB() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(PrintServlet.class, PrintListener.class)
                .setWebXML(new StringAsset(WEB_XML))
                .addAsWebInfResource("appengine-web.xml");
    }

    protected static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(PrintServlet.class, PrintListener.class)
            .setWebXML(new StringAsset(WEB_XML))
            .addAsWebInfResource("appengine-web.xml");
    }
}
