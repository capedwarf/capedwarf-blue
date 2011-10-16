package org.jboss.capedwarf.datastore.query;

import com.google.appengine.api.datastore.*;
import org.infinispan.query.CacheQuery;

import java.util.Iterator;
import java.util.List;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;

/**
 * JBoss GAE PreparedQuery
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class PreparedQueryImpl implements PreparedQuery {

    private CacheQuery cacheQuery;

    public PreparedQueryImpl(CacheQuery cacheQuery) {
        this.cacheQuery = cacheQuery;
    }

    public List<Entity> asList(FetchOptions fetchOptions) {
        return asQueryResultList(fetchOptions);
    }

    public QueryResultList<Entity> asQueryResultList(FetchOptions fetchOptions) {
        apply(fetchOptions, cacheQuery);
        List<?> objects = cacheQuery.list();
        return new QueryResultListImpl<Entity>((List<Entity>) objects);
    }

    public Iterable<Entity> asIterable() {
        return asIterable(withDefaults());
    }

    public Iterable<Entity> asIterable(FetchOptions fetchOptions) {
        return asQueryResultIterable(fetchOptions);
    }

    public QueryResultIterable<Entity> asQueryResultIterable() {
        return asQueryResultIterable(withDefaults());
    }

    public QueryResultIterable<Entity> asQueryResultIterable(FetchOptions fetchOptions) {
        return new QueryResultIterableImpl<Entity>(asQueryResultIterator(fetchOptions));
    }

    public Iterator<Entity> asIterator() {
        return asIterator(withDefaults());
    }

    public Iterator<Entity> asIterator(FetchOptions fetchOptions) {
        return asQueryResultIterator(fetchOptions);
    }

    public QueryResultIterator<Entity> asQueryResultIterator() {
        return asQueryResultIterator(withDefaults());
    }

    public QueryResultIterator<Entity> asQueryResultIterator(FetchOptions fetchOptions) {
        return new QueryResultIteratorImpl<Entity>(createQueryIterator(fetchOptions));
    }

    public Entity asSingleEntity() throws TooManyResultsException {
        Iterator<Entity> iterator = asIterator();
        Entity firstResult = iterator.hasNext() ? iterator.next() : null;
        if (iterator.hasNext()) {
            throw new TooManyResultsException();
        }
        return firstResult;
    }

    public int countEntities() {
        return countEntities(withDefaults());
    }

    public int countEntities(FetchOptions fetchOptions) {
        apply(fetchOptions, cacheQuery);
        return cacheQuery.getResultSize();
    }

    private void apply(FetchOptions fetchOptions, CacheQuery cacheQuery) {
        if (fetchOptions.getOffset() != null) {
            cacheQuery.firstResult(fetchOptions.getOffset());
        }
        if (fetchOptions.getLimit() != null) {
            cacheQuery.maxResults(fetchOptions.getLimit());
        }
    }

    private Iterator<Entity> createQueryIterator(FetchOptions fetchOptions) {
        apply(fetchOptions, cacheQuery);

        Integer chunkSize = fetchOptions.getChunkSize();
        if (chunkSize == null) {
            return cacheQuery.iterator();
        } else {
            return cacheQuery.iterator(chunkSize);
        }
    }


}
