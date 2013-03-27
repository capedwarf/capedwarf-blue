package org.jboss.capedwarf.datastore.query;

import java.util.Arrays;
import java.util.Collection;

import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.RangeMatchingContext;
import org.hibernate.search.query.dsl.TermMatchingContext;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class LuceneQueryBuilder {

    private QueryBuilder queryBuilder;

    public LuceneQueryBuilder(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public Query matchAll() {
        return queryBuilder.all().createQuery();
    }

    public Query all(Collection<Query> subQueries) {
        BooleanJunction<BooleanJunction> bool = queryBuilder.bool();
        for (Query subQuery : subQueries) {
            bool.must(subQuery);
        }
        return bool.createQuery();
    }

    public Query any(Query... subQueries) {
        return any(Arrays.asList(subQueries));
    }

    public Query any(Collection<Query> subQueries) {
        BooleanJunction<BooleanJunction> bool = queryBuilder.bool();
        for (Query subQuery : subQueries) {
            bool.should(subQuery);
        }
        return bool.createQuery();
    }

    public Query in(String fieldName, Collection<?> values) {
        BooleanJunction<BooleanJunction> bool = queryBuilder.bool();
        for (Object value : values) {
            bool.should(equal(fieldName, value));
        }
        return bool.createQuery();
    }

    public Query notEqual(String fieldName, Object value) {
        return any(
            lessThan(fieldName, value),
            greaterThan(fieldName, value));
    }

    public Query not(Query query) {
        return queryBuilder.bool().must(query).not().createQuery();
    }

    public Query equal(String fieldName, Object value) {
        return equal(fieldName, convertToString(value));
    }

    public Query equal(String fieldName, String stringValue) {
        return keywordOnField(fieldName)
            .matching(stringValue)
            .createQuery();
    }

    private TermMatchingContext keywordOnField(String fieldName) {
        return queryBuilder
            .keyword().onField(fieldName)
            .ignoreFieldBridge()
            .ignoreAnalyzer();
    }

    public Query greaterThan(String fieldName, Object value) {
        return rangeOnField(fieldName)
            .above(convertToString(value)).excludeLimit()
            .createQuery();
    }

    public Query greaterThanOrEqual(String fieldName, Object value) {
        return rangeOnField(fieldName)
            .above(convertToString(value))
            .createQuery();
    }

    public Query lessThan(String fieldName, Object value) {
        return rangeOnField(fieldName)
            .below(convertToString(value)).excludeLimit()
            .createQuery();
    }

    public Query lessThanOrEqual(String fieldName, Object value) {
        return rangeOnField(fieldName)
            .below(convertToString(value))
            .createQuery();
    }

    public RangeMatchingContext rangeOnField(String fieldName) {
        return queryBuilder
            .range().onField(fieldName)
            .ignoreFieldBridge()
            .ignoreAnalyzer();
    }

    private String convertToString(Object value) {
        return BridgeUtils.matchBridge(value).objectToString(value);
    }
}
