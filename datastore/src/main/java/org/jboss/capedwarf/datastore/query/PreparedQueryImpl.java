package org.jboss.capedwarf.datastore.query;

import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.QueryResultList;
import org.infinispan.query.CacheQuery;

import static com.google.appengine.api.datastore.FetchOptions.Builder.withDefaults;

/**
 * JBoss GAE PreparedQuery
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class PreparedQueryImpl implements PreparedQuery {

    private Query gaeQuery;     // currently here only for debugging purposes
    private CacheQuery cacheQuery;

    public PreparedQueryImpl(Query gaeQuery, CacheQuery cacheQuery) {
        this.gaeQuery = gaeQuery;
        this.cacheQuery = cacheQuery;
    }

    public List<Entity> asList(FetchOptions fetchOptions) {
        return asQueryResultList(fetchOptions);
    }

    @SuppressWarnings("unchecked")
    public QueryResultList<Entity> asQueryResultList(FetchOptions fetchOptions) {
        apply(fetchOptions, cacheQuery);
        List<?> objects = cacheQuery.list();
        return new QueryResultListImpl<Entity>((List<Entity>) objects, JBossCursorHelper.createListCursor(fetchOptions));
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

    @SuppressWarnings("unchecked")
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
        final Integer offset = fetchOptions.getOffset();
        if (offset != null) {
            cacheQuery.firstResult(offset);
        }
        final Integer limit = fetchOptions.getLimit();
        if (limit != null) {
            cacheQuery.maxResults(limit);
        }
        final Cursor start = fetchOptions.getStartCursor();
        if (start != null) {
            JBossCursorHelper.applyStartCursor(start, cacheQuery);
        }
        final Cursor end = fetchOptions.getEndCursor();
        if (end != null) {
            JBossCursorHelper.applyEndCursor(end, cacheQuery, start);
        }
    }

    private Iterator createQueryIterator(FetchOptions fetchOptions) {
        apply(fetchOptions, cacheQuery);

        Integer chunkSize = fetchOptions.getChunkSize();
        if (chunkSize == null) {
            return cacheQuery.iterator();
        } else {
            return cacheQuery.iterator(chunkSize);
        }
    }


}
