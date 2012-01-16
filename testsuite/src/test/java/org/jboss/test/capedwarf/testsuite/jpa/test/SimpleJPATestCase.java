package org.jboss.test.capedwarf.testsuite.jpa.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.capedwarf.testsuite.jpa.Client;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
public class SimpleJPATestCase extends AbstractJPATest {

    @Deployment
    public static WebArchive getDeployment() {
        final WebArchive war = ShrinkWrap.create(WebArchive.class, "jpa.war");
        war.addPackage(Client.class.getPackage());
        war.setWebXML(new StringAsset("<web/>")).addAsWebInfResource("appengine-web.xml");
        war.addAsWebInfResource("jpa/persistence.xml", "classes/META-INF/persistence.xml");
        TestUtils.addLibraries(war);
        war.addClass(AbstractJPATest.class);
        return war;
    }

    @Test
    public void testSaveAndQuery() throws Throwable {
        final Client client = new Client();
        EMAction<Long> ema1 = new EMAction<Long>() {
            public Long go(EntityManager em) throws Throwable {
                client.setUsername("alesj");
                client.setPassword("password");
                client.setEmail("aj@jboss.com");
                em.persist(client);
                return client.getId();
            }
        };
        run(ema1);
        final Long id = client.getId();
        Assert.assertNotNull("Null client id", id);

        EMAction<Client> ema2 = new EMAction<Client>() {
            public Client go(EntityManager em) throws Throwable {
                Query q = em.createQuery("select from Client c where c.username = :username");
                q.setParameter("username", "alesj");
                @SuppressWarnings("unchecked")
                List<Client> clients = q.getResultList();
                return (clients.isEmpty()) ? null : clients.get(0);
            }
        };
        Client c = run(ema2);
        Assert.assertNotNull(c);
        Assert.assertEquals(id, c.getId());
        Assert.assertEquals("alesj", c.getUsername());
        Assert.assertEquals("password", c.getPassword());

        EMAction<Client> ema3 = new EMAction<Client>() {
            public Client go(EntityManager em) throws Throwable {
                return em.find(Client.class, id);
            }
        };
        c = run(ema3);
        Assert.assertNotNull(c);
        Assert.assertEquals(id, c.getId());
        Assert.assertEquals("alesj", c.getUsername());
        Assert.assertEquals("password", c.getPassword());
    }
}
