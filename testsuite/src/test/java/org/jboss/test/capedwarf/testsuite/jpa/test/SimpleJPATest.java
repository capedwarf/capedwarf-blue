package org.jboss.test.capedwarf.testsuite.jpa.test;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.capedwarf.testsuite.jpa.Client;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.TestContext;
import org.jboss.test.capedwarf.testsuite.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class SimpleJPATest extends AbstractJPATest {

    protected static WebArchive getBaseDeployment() {
        final TestContext context = new TestContext().setIgnoreLogging(true);
        final WebArchive war = getCapedwarfDeployment(context);
        war.addPackage(Client.class.getPackage());
        war.addClass(AbstractTest.class);
        war.addClass(AbstractJPATest.class);
        war.addClass(SimpleJPATest.class);
        return war;
    }

    protected static WebArchive getDefaultDeployment() {
        final WebArchive war = getBaseDeployment();
        TestUtils.addPersistenceXml(war, "jpa/default-persistence.xml");
        return war;
    }

    @Test
    public void testSaveAndQuery() throws Throwable {
        try {
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
            try {
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
                Client c = run(ema2, false);
                Assert.assertNotNull(c);
                Assert.assertEquals(id, c.getId());
                Assert.assertEquals("alesj", c.getUsername());
                Assert.assertEquals("password", c.getPassword());

                EMAction<Client> ema3 = new EMAction<Client>() {
                    public Client go(EntityManager em) throws Throwable {
                        return em.find(Client.class, id);
                    }
                };
                c = run(ema3, false);
                Assert.assertNotNull(c);
                Assert.assertEquals(id, c.getId());
                Assert.assertEquals("alesj", c.getUsername());
                Assert.assertEquals("password", c.getPassword());
            } finally {
                EMAction<Integer> delete = new EMAction<Integer>() {
                    public Integer go(EntityManager em) throws Throwable {
                        Query query = em.createQuery("delete from Client c where c.id = :id");
                        query.setParameter("id", client.getId());
                        return query.executeUpdate();
                    }
                };
                Assert.assertTrue(1 == run(delete));
            }
        } finally {
            cleanup();
        }
    }
}
