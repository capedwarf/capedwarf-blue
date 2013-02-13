package org.jboss.test.capedwarf.testsuite.jpa.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.jboss.test.capedwarf.testsuite.TestsuiteTestBase;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class JPATestBase extends TestsuiteTestBase {

    private EntityManagerFactory emf;

    protected EntityManagerFactory getEMF() {
        if (emf == null)
            emf = Persistence.createEntityManagerFactory("test");
        return emf;
    }

    protected <T> T run(EMAction<T> action) throws Throwable {
        return run(action, true);
    }

    protected <T> T run(EMAction<T> action, final boolean useTx) throws Throwable {
        final EntityManager em = getEMF().createEntityManager();
        try {
            EntityTransaction tx = null;
            if (useTx)
                tx = em.getTransaction();

            try {
                if (useTx)
                    tx.begin();

                return action.go(em);
            } catch (Throwable t) {
                if (useTx)
                    tx.setRollbackOnly();

                throw t;
            } finally {
                if (useTx) {
                    if (tx.getRollbackOnly())
                        tx.rollback();
                    else
                        tx.commit();
                }
            }
        } finally {
            em.close();
        }
    }

    protected interface EMAction<T> {
        T go(EntityManager em) throws Throwable;
    }
}
