package org.jboss.test.capedwarf.testsuite.jpa.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AbstractJPATest {

    private EntityManagerFactory emf;

    protected EntityManagerFactory getEMF() {
        if (emf == null)
            emf = Persistence.createEntityManagerFactory("test");
        return emf;
    }

    protected <T> T run(EMAction<T> action) throws Throwable {
        return run(action, true);
    }

    protected <T> T run(EMAction<T> action, boolean useTx) throws Throwable {
        final EntityManager em = getEMF().createEntityManager();
        try {
            EntityTransaction tx = null;
            if (useTx)
                tx = em.getTransaction();

            try {
                if (useTx)
                    tx.begin();

                T result = action.go(em);

                if (useTx)
                    tx.commit();

                return result;
            } catch (Throwable t) {
                if (useTx)
                    tx.rollback();

                throw t;
            }
        } finally {
            em.close();
        }
    }

    protected interface EMAction<T> {
        T go(EntityManager em) throws Throwable;
    }
}
