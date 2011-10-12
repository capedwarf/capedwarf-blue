package org.jboss.capedwarf.datastore.query;

import com.google.appengine.api.datastore.*;
import org.infinispan.Cache;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.QueryIterator;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;

import java.util.Iterator;
import java.util.List;

/**
 * JBoss GAE PreparedQuery
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class InfinispanPreparedQuery implements PreparedQuery {

    private CacheQuery cacheQuery;

    public InfinispanPreparedQuery(Cache<Key, Entity> cache, Query query) {
        SearchManager searchManager = Search.getSearchManager(cache);
        cacheQuery = searchManager.getQuery(convertQuery(query, searchManager), Entity.class);
    }

    private org.apache.lucene.search.Query convertQuery(Query query, SearchManager searchManager) {
        return new InfinispanQueryConverter(searchManager, query).convert();
    }

    public List<Entity> asList(FetchOptions fetchOptions) {
//        return cacheQuery.list();
        return null;
    }

    public QueryResultList<Entity> asQueryResultList(FetchOptions fetchOptions) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterable<Entity> asIterable(FetchOptions fetchOptions) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public QueryResultIterable<Entity> asQueryResultIterable(FetchOptions fetchOptions) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterable<Entity> asIterable() {
        return new Iterable<Entity>() {
            public Iterator<Entity> iterator() {
                return cacheQuery.iterator();
            }
        };
    }

    public QueryResultIterable<Entity> asQueryResultIterable() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterator<Entity> asIterator(FetchOptions fetchOptions) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterator<Entity> asIterator() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public QueryResultIterator<Entity> asQueryResultIterator(FetchOptions fetchOptions) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public QueryResultIterator<Entity> asQueryResultIterator() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Entity asSingleEntity() throws TooManyResultsException {
        if (cacheQuery.getResultSize() > 1) {
            throw new TooManyResultsException();
        }
        QueryIterator iterator = cacheQuery.iterator();
        return iterator.hasNext() ? (Entity) iterator.next() : null;
    }

    public int countEntities(FetchOptions fetchOptions) {
        return cacheQuery.getResultSize();
    }

    public int countEntities() {
        return cacheQuery.getResultSize();
    }
}
