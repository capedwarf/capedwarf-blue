/**
 * Original code taken from https://code.google.com/p/gql4j .
 *
 * Distributed under EPL 1.0 license.
 */

package org.jboss.capedwarf.gql4j;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.User;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.jboss.capedwarf.gql4j.antlr.GQLLexer;
import org.jboss.capedwarf.gql4j.antlr.GQLParser;

/**
 * @author Max Zhu (thebbsky@gmail.com)
 * @author Ales Justin (or the one who copy/pasted this here :-)
 */
public class GqlQuery {
    private Query query;
    private FetchOptions fetchOptions;

    public GqlQuery(String queryStr, Map<String, Object> context) {
        build(queryStr, context);
    }

    public GqlQuery(String queryStr, Object... params) {
        // evaluation context
        Map<String, Object> context = Maps.newHashMap();
        for (int i = 0; i < params.length; i++) {
            context.put(String.valueOf(i + 1), params[i]);
        }
        build(queryStr, context);
    }

    private void build(String queryStr, Map<String, Object> context) {
        Preconditions.checkNotNull(queryStr);

        ParseResult r = parse(queryStr);

        // from clause
        if (r.from == null) {
            this.query = new Query();
        } else {
            this.query = new Query(r.from.kind);
        }

        // select clause
        if (r.select.isKeyOnly()) {
            this.query.setKeysOnly();
        }

        // where clause
        if (r.where != null) {
            List<Query.Filter> filters = new ArrayList<>();
            for (Condition c : r.where.conditions) {
                filters.add(new Query.FilterPredicate(c.propertyName, c.operator, c.e.evaluate(context)));
            }
            if (filters.size() == 1) {
                this.query.setFilter(filters.get(0));
            } else if (filters.size() > 1) {
                // TODO -- always AND?
                this.query.setFilter(new Query.CompositeFilter(Query.CompositeFilterOperator.AND, filters));
            }

            // set ancester
            if (r.where.ancestor != null) {
                this.query.setAncestor(r.where.ancestor.ancestorKey(context));
            }
        }

        // order by
        if (r.orderBy != null) {
            for (OrderByItem o : r.orderBy.items) {
                this.query.addSort(o.propertyName, o.direction);
            }
        }

        // limit
        if (r.limit != null) {
            this.fetchOptions = FetchOptions.Builder.withLimit(r.limit.limit);
        }

        if (r.offset != null) {
            if (this.fetchOptions == null) {
                this.fetchOptions = FetchOptions.Builder.withDefaults();
            }

            this.fetchOptions.offset(r.offset.offset);
        }
    }

    static ParseResult parse(String queryStr) {
        try {
            CharStream input = new ANTLRStringStream(queryStr);
            GQLLexer lexer = new GQLLexer(input);
            TokenStream tokens = new CommonTokenStream(lexer);
            GQLParser parser = new GQLParser(tokens);
            return parser.query().r;
        } catch (RecognitionException e) {
            throw new GqlQueryException("GQL syntax error");
        }
    }

    public Query query() {
        return query;
    }

    public FetchOptions fetchOptions() {
        if (fetchOptions == null) {
            fetchOptions = FetchOptions.Builder.withDefaults();
        }
        return fetchOptions;
    }

    public static class GqlQueryException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public GqlQueryException() {
            super();
        }

        public GqlQueryException(String message, Throwable cause) {
            super(message, cause);
        }

        public GqlQueryException(String message) {
            super(message);
        }

        public GqlQueryException(Throwable cause) {
            super(cause);
        }
    }

    public static class ParseResult {
        private Select select;

        private From from;

        private Where where;

        private OrderBy orderBy;

        private Limit limit;

        private Offset offset;

        public Select getSelect() {
            return select;
        }

        public ParseResult setSelect(Select select) {
            this.select = select;
            return this;
        }

        public From getFrom() {
            return from;
        }

        public ParseResult setFrom(From from) {
            this.from = from;
            return this;
        }

        public Where getWhere() {
            return where;
        }

        public ParseResult setWhere(Where where) {
            this.where = where;
            return this;
        }

        public OrderBy getOrderBy() {
            return orderBy;
        }

        public ParseResult setOrderBy(OrderBy orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        public Limit getLimit() {
            return limit;
        }

        public ParseResult setLimit(Limit limit) {
            this.limit = limit;
            return this;
        }

        public Offset getOffset() {
            return offset;
        }

        public ParseResult setOffset(Offset offset) {
            this.offset = offset;
            return this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((from == null) ? 0 : from.hashCode());
            result = prime * result + ((limit == null) ? 0 : limit.hashCode());
            result = prime * result + ((offset == null) ? 0 : offset.hashCode());
            result = prime * result + ((orderBy == null) ? 0 : orderBy.hashCode());
            result = prime * result + ((select == null) ? 0 : select.hashCode());
            result = prime * result + ((where == null) ? 0 : where.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ParseResult other = (ParseResult) obj;
            if (from == null) {
                if (other.from != null)
                    return false;
            } else if (!from.equals(other.from))
                return false;
            if (limit == null) {
                if (other.limit != null)
                    return false;
            } else if (!limit.equals(other.limit))
                return false;
            if (offset == null) {
                if (other.offset != null)
                    return false;
            } else if (!offset.equals(other.offset))
                return false;
            if (orderBy == null) {
                if (other.orderBy != null)
                    return false;
            } else if (!orderBy.equals(other.orderBy))
                return false;
            if (select == null) {
                if (other.select != null)
                    return false;
            } else if (!select.equals(other.select))
                return false;
            if (where == null) {
                if (other.where != null)
                    return false;
            } else if (!where.equals(other.where))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "ParseResult [select=" + select + ", from=" + from + ", where=" + where + ", orderBy=" + orderBy
                    + ", limit=" + limit + ", offset=" + offset + "]";
        }
    }

    @SuppressWarnings("RedundantIfStatement")
    public static class Select {
        /**
         * select item, only * and __key__ is allowed (case sensitive)
         */
        private final boolean keyOnly;

        public Select(boolean keyOnly) {
            super();
            this.keyOnly = keyOnly;
        }

        public boolean isKeyOnly() {
            return this.keyOnly;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (keyOnly ? 1231 : 1237);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Select other = (Select) obj;
            if (keyOnly != other.keyOnly)
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Select [keyOnly=" + keyOnly + "]";
        }
    }

    public static class From {
        private final String kind;

        public From(String kind) {
            super();
            this.kind = kind;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((kind == null) ? 0 : kind.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            From other = (From) obj;
            if (kind == null) {
                if (other.kind != null)
                    return false;
            } else if (!kind.equals(other.kind))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "From [kind=" + kind + "]";
        }
    }

    public static class Where {
        private final List<Condition> conditions;

        private Ancestor ancestor;

        public Where() {
            conditions = Lists.newLinkedList();
        }

        public Where withCondition(Condition condition) {
            this.conditions.add(condition);
            return this;
        }

        public Where withAncestor(Evaluator e) {
            ancestor = new Ancestor(e);
            return this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((ancestor == null) ? 0 : ancestor.hashCode());
            result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Where other = (Where) obj;
            if (ancestor == null) {
                if (other.ancestor != null)
                    return false;
            } else if (!ancestor.equals(other.ancestor))
                return false;
            if (conditions == null) {
                if (other.conditions != null)
                    return false;
            } else if (!conditions.equals(other.conditions))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Where [conditions=" + conditions + ", ancestor=" + ancestor + "]";
        }
    }

    public static class Ancestor {

        private final Evaluator e;

        public Ancestor(Evaluator e) {
            super();
            this.e = e;
        }

        public Key ancestorKey(Map<String, Object> context) {
            Object val = e.evaluate(context);
            if (val instanceof Key) {
                return (Key) val;
            } else if (val instanceof Entity) {
                return ((Entity) val).getKey();
            } else {
                throw new GqlQueryException("Invalid GQL query string. ANCESTOR IS must be followed by Key or Entity");
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((e == null) ? 0 : e.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Ancestor other = (Ancestor) obj;
            if (e == null) {
                if (other.e != null)
                    return false;
            } else if (!e.equals(other.e))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Ancestor [e=" + e + "]";
        }
    }

    public static class OrderBy {
        private final List<OrderByItem> items;

        public OrderBy() {
            items = Lists.newLinkedList();
        }

        public OrderBy withItem(OrderByItem item) {
            this.items.add(item);
            return this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((items == null) ? 0 : items.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            OrderBy other = (OrderBy) obj;
            if (items == null) {
                if (other.items != null)
                    return false;
            } else if (!items.equals(other.items))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "OrderBy [items=" + items + "]";
        }
    }

    public static class Limit {
        private final Integer limit;

        public Limit(Integer limit) {
            super();
            this.limit = limit;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((limit == null) ? 0 : limit.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Limit other = (Limit) obj;
            if (limit == null) {
                if (other.limit != null)
                    return false;
            } else if (!limit.equals(other.limit))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Limit [limit=" + limit + "]";
        }
    }

    public static class Offset {
        private final Integer offset;

        public Offset(Integer offset) {
            super();
            this.offset = offset;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((offset == null) ? 0 : offset.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Offset other = (Offset) obj;
            if (offset == null) {
                if (other.offset != null)
                    return false;
            } else if (!offset.equals(other.offset))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Offset [offset=" + offset + "]";
        }
    }

    /**
     * where condition
     *
     * @author Max Zhu (thebbsky@gmail.com)
     */
    public static class Condition {
        private String propertyName;

        private FilterOperator operator;

        private Evaluator e;

        public Condition(String propertyName, FilterOperator operator, Evaluator e) {
            super();

            Preconditions.checkNotNull(propertyName);
            Preconditions.checkNotNull(operator);
            Preconditions.checkNotNull(e);

            this.propertyName = propertyName;
            this.operator = operator;
            this.e = e;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((e == null) ? 0 : e.hashCode());
            result = prime * result + ((operator == null) ? 0 : operator.hashCode());
            result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Condition other = (Condition) obj;
            if (e == null) {
                if (other.e != null)
                    return false;
            } else if (!e.equals(other.e))
                return false;
            if (operator != other.operator)
                return false;
            if (propertyName == null) {
                if (other.propertyName != null)
                    return false;
            } else if (!propertyName.equals(other.propertyName))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Condition [propertyName=" + propertyName + ", operator=" + operator + ", e=" + e + "]";
        }
    }

    /**
     * @author Max Zhu (thebbsky@gmail.com)
     */
    public static class OrderByItem {

        private String propertyName;

        private SortDirection direction;

        public OrderByItem(String propertyName) {
            super();
            this.propertyName = propertyName;
            this.direction = SortDirection.ASCENDING;
        }

        public OrderByItem setDirection(boolean ascending) {
            this.direction = ascending ? SortDirection.ASCENDING : SortDirection.DESCENDING;
            return this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((direction == null) ? 0 : direction.hashCode());
            result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            OrderByItem other = (OrderByItem) obj;
            if (direction != other.direction)
                return false;
            if (propertyName == null) {
                if (other.propertyName != null)
                    return false;
            } else if (!propertyName.equals(other.propertyName))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "OrderByItem [propertyName=" + propertyName + ", direction=" + direction + "]";
        }
    }

    public static interface Evaluator {
        public Object evaluate(Map<String, Object> context);
    }

    public static class NullEvaluator implements Evaluator {

        private static NullEvaluator singleton;

        public static synchronized NullEvaluator get() {
            if (singleton == null) {
                singleton = new NullEvaluator();
            }
            return singleton;
        }

        private NullEvaluator() {
        }

        @Override
        public Object evaluate(Map<String, Object> context) {
            return null;
        }
    }

    public static class DecimalEvaluator implements Evaluator {
        private final BigDecimal payload;

        public DecimalEvaluator(String strNumber) {
            payload = new BigDecimal(strNumber);
        }

        @Override
        public Object evaluate(Map<String, Object> context) {
            if ((double) payload.longValue() == payload.doubleValue()) {
                return this.payload.longValue();
            } else {
                return this.payload.doubleValue();
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((payload == null) ? 0 : payload.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DecimalEvaluator other = (DecimalEvaluator) obj;
            if (payload == null) {
                if (other.payload != null)
                    return false;
            } else if (!payload.equals(other.payload))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "DecimalEvaluator [payload=" + payload + "]";
        }
    }

    public static class StringEvaluator implements Evaluator {
        private final String payload;

        public StringEvaluator(String rawText) {
            super();
            // remove single quote
            String withoutQuote = rawText.substring(1, rawText.length() - 1);

            // replace \' with '
            this.payload = withoutQuote.replace("\\'", "'");
        }

        @Override
        public Object evaluate(Map<String, Object> context) {
            return this.payload;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((payload == null) ? 0 : payload.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StringEvaluator other = (StringEvaluator) obj;
            if (payload == null) {
                if (other.payload != null)
                    return false;
            } else if (!payload.equals(other.payload))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "StringEvaluator [payload=" + payload + "]";
        }
    }

    public static class BooleanEvaluator implements Evaluator {
        private final Boolean payload;

        public BooleanEvaluator(String input) {
            this.payload = Boolean.valueOf(input);
        }

        @Override
        public Object evaluate(Map<String, Object> context) {
            return this.payload;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((payload == null) ? 0 : payload.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            BooleanEvaluator other = (BooleanEvaluator) obj;
            if (payload == null) {
                if (other.payload != null)
                    return false;
            } else if (!payload.equals(other.payload))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "BooleanEvaluator [payload=" + payload + "]";
        }
    }

    @SuppressWarnings("RedundantIfStatement")
    public static class FunctionEvaluator implements Evaluator {

        public enum Type {
            DATETIME,
            DATE,
            TIME,
            KEY,
            USER,
            GEOPT
        }

        private final Type type;

        private final List<Evaluator> ops;

        public FunctionEvaluator(String type, Evaluator... ops) {
            this.type = Type.valueOf(type.toUpperCase());
            this.ops = Lists.newArrayList(ops);
        }

        public FunctionEvaluator(String type, List<Evaluator> ops) {
            this.type = Type.valueOf(type.toUpperCase());
            this.ops = ops;
        }

        public FunctionEvaluator withParam(Evaluator e) {
            this.ops.add(e);
            return this;
        }

        @Override
        public Object evaluate(Map<String, Object> context) {
            switch (this.type) {
                case DATETIME:
                    return datetime(context);
                case DATE:
                    return date(context);
                case TIME:
                    return time(context);
                case GEOPT:
                    return geopt(context);
                case KEY:
                    return key(context);
                case USER:
                    return user(context);
                default:
                    // not supposed to happen
                    throw new GqlQueryException("Invalid function type " + this.type);
            }
        }

        private static final String DEFAULT_AUTH_DOMAIN = "gmail.com";

        private Object user(Map<String, Object> context) {
            if (this.ops.size() == 1) {
                Object val = this.ops.get(0).evaluate(context);
                if (val instanceof String) {
                    return new User((String) val, DEFAULT_AUTH_DOMAIN);
                } else {
                    throw new GqlQueryException("Invalid GQL query string. Function key: invalid input");
                }
            } else {
                throw new GqlQueryException("Invalid GQL query string. Function key: wrong number of arguments");
            }
        }

        private Object key(Map<String, Object> context) {
            if (this.ops.isEmpty()) {
                throw new GqlQueryException("Invalid GQL query string. Function key: wrong number of arguments");
            } else if (this.ops.size() == 1) {
                // KEY('encoded key')
                String keyString = (String) this.ops.get(0).evaluate(context);
                return KeyFactory.stringToKey(keyString);
            } else if (this.ops.size() % 2 == 0) {
                // KEY('kind', 'name'/ID [, 'kind', 'name'/ID...])
                try {
                    Key key = null;
                    Iterator<Evaluator> i = this.ops.iterator();
                    while (i.hasNext()) {
                        String kind = (String) i.next().evaluate(context);
                        Object nameId = i.next().evaluate(context);

                        if (nameId instanceof String) {
                            key = KeyFactory.createKey(key, kind, (String) nameId);
                        } else if (nameId instanceof Long) {
                            key = KeyFactory.createKey(key, kind, (Long) nameId);
                        }
                    }

                    return key;
                } catch (ClassCastException e) {
                    throw new GqlQueryException("Invalid GQL query string. Function key: invalid input", e);
                }
            } else {
                throw new GqlQueryException("Invalid GQL query string. Function key: wrong number of arguments");
            }
        }

        @SuppressWarnings("UnusedParameters")
        private Object geopt(Map<String, Object> context) {
            return null; // TODO
        }

        private static DateFormat timeFmter = new SimpleDateFormat("HH:mm:ss");

        private Object time(Map<String, Object> context) {
            if (this.ops.size() == 1) {
                // TIME('HH:MM:SS')
                Object r = this.ops.get(0).evaluate(context);
                if (r instanceof String) {
                    try {
                        return timeFmter.parse((String) r);
                    } catch (ParseException e) {
                        throw new GqlQueryException("Invalid GQL query string. Function time: invalid input", e);
                    }
                } else {
                    throw new GqlQueryException("Invalid GQL query string. Function time: invalid input");
                }
            } else if (this.ops.size() == 3) {
                // TIME(hour, minute, second)
                try {
                    int hour = ((Number) this.ops.get(0).evaluate(context)).intValue();
                    int minute = ((Number) this.ops.get(1).evaluate(context)).intValue();
                    int second = ((Number) this.ops.get(2).evaluate(context)).intValue();

                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.HOUR, hour);
                    c.set(Calendar.MINUTE, minute);
                    c.set(Calendar.SECOND, second);
                    return c.getTime();
                } catch (ClassCastException e) {
                    throw new GqlQueryException("Invalid GQL query string. Function time: invalid input", e);
                }
            } else {
                throw new GqlQueryException("Invalid GQL query string. Function time: wrong number of arguments");
            }
        }

        private static DateFormat dateFmter = new SimpleDateFormat("yyyy-MM-dd");

        private Object date(Map<String, Object> context) {
            if (this.ops.size() == 1) {
                // DATE('YYYY-MM-DD')
                Object r = this.ops.get(0).evaluate(context);
                if (r instanceof String) {
                    try {
                        return dateFmter.parse((String) r);
                    } catch (ParseException e) {
                        throw new GqlQueryException("Invalid GQL query string. Function date: invalid input", e);
                    }
                } else {
                    throw new GqlQueryException("Invalid GQL query string. Function date: invalid input");
                }
            } else if (this.ops.size() == 3) {
                // DATE(year, month, day)
                try {
                    int year = ((Number) this.ops.get(0).evaluate(context)).intValue();
                    int month = ((Number) this.ops.get(1).evaluate(context)).intValue();
                    int day = ((Number) this.ops.get(2).evaluate(context)).intValue();

                    return createDate(year, month, day, 0, 0, 0);
                } catch (ClassCastException e) {
                    throw new GqlQueryException("Invalid GQL query string. Function date: invalid input", e);
                }
            } else {
                throw new GqlQueryException("Invalid GQL query string. Function date: wrong number of arguments");
            }
        }

        private static DateFormat datetimeFmter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        private Object datetime(Map<String, Object> context) {
            if (this.ops.size() == 1) {
                // DATETIME('YYYY-MM-DD HH:MM:SS')
                Object r = this.ops.get(0).evaluate(context);
                if (r instanceof String) {
                    try {
                        return datetimeFmter.parse((String) r);
                    } catch (ParseException e) {
                        throw new GqlQueryException("Invalid GQL query string. Function datetime: invalid input", e);
                    }
                } else {
                    throw new GqlQueryException("Invalid GQL query string. Function datetime: invalid input");
                }
            } else if (this.ops.size() == 6) {
                // DATETIME(year, month, day, hour, minute, second)
                try {
                    int year = ((Number) this.ops.get(0).evaluate(context)).intValue();
                    int month = ((Number) this.ops.get(1).evaluate(context)).intValue();
                    int day = ((Number) this.ops.get(2).evaluate(context)).intValue();
                    int hour = ((Number) this.ops.get(3).evaluate(context)).intValue();
                    int minute = ((Number) this.ops.get(4).evaluate(context)).intValue();
                    int second = ((Number) this.ops.get(5).evaluate(context)).intValue();

                    return createDate(year, month, day, hour, minute, second);
                } catch (ClassCastException e) {
                    throw new GqlQueryException("Invalid GQL query string. Function datetime: invalid input", e);
                }
            } else {
                throw new GqlQueryException("Invalid GQL query string. Function datetime: wrong number of arguments");
            }
        }

        private Date createDate(int year, int month, int day, int hour, int minute, int second) {
            Calendar c = Calendar.getInstance();
            c.clear();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DATE, day);
            c.set(Calendar.HOUR, hour);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, second);
            return c.getTime();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((ops == null) ? 0 : ops.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FunctionEvaluator other = (FunctionEvaluator) obj;
            if (ops == null) {
                if (other.ops != null)
                    return false;
            } else if (!ops.equals(other.ops))
                return false;
            if (type != other.type)
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "FunctionEvaluator [type=" + type + ", ops=" + ops + "]";
        }
    }

    public static class ParamEvaluator implements Evaluator {

        private final String paramName;

        public ParamEvaluator(String paramName) {
            super();
            this.paramName = paramName;
        }

        @Override
        public Object evaluate(Map<String, Object> context) {
            return context.get(paramName);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((paramName == null) ? 0 : paramName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ParamEvaluator other = (ParamEvaluator) obj;
            if (paramName == null) {
                if (other.paramName != null)
                    return false;
            } else if (!paramName.equals(other.paramName))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "ParamEvaluator [paramName=" + paramName + "]";
        }
    }

    public static class ListEvaluator implements Evaluator {

        private final List<Evaluator> evaluators;

        public ListEvaluator(Evaluator... evaluators) {
            super();
            this.evaluators = Lists.newArrayList(evaluators);
        }

        public ListEvaluator(List<Evaluator> evaluators) {
            super();
            this.evaluators = evaluators;
        }

        @Override
        public Object evaluate(Map<String, Object> context) {
            List<Object> result = new ArrayList<Object>(evaluators.size());
            for (Evaluator e : evaluators) {
                result.add(e.evaluate(context));
            }
            return result;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((evaluators == null) ? 0 : evaluators.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ListEvaluator other = (ListEvaluator) obj;
            if (evaluators == null) {
                if (other.evaluators != null)
                    return false;
            } else if (!evaluators.equals(other.evaluators))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "ListEvaluator [evaluators=" + evaluators + "]";
        }
    }
}