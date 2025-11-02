package com.akilisha.oss.roya.api;

/**
 * Builder for constructing vector database filters.
 *
 * Supports metadata filtering for vector searches.
 */
public interface FilterBuilder {
    /**
     * Add equality filter.
     */
    FilterBuilder eq(String field, Object value);

    /**
     * Add inequality filter.
     */
    FilterBuilder ne(String field, Object value);

    /**
     * Add greater than filter.
     */
    FilterBuilder gt(String field, Comparable<?> value);

    /**
     * Add greater than or equal filter.
     */
    FilterBuilder gte(String field, Comparable<?> value);

    /**
     * Add less than filter.
     */
    FilterBuilder lt(String field, Comparable<?> value);

    /**
     * Add less than or equal filter.
     */
    FilterBuilder lte(String field, Comparable<?> value);

    /**
     * Add range filter.
     */
    FilterBuilder range(String field, Comparable<?> min, Comparable<?> max);

    /**
     * Add in filter (value in list).
     */
    FilterBuilder in(String field, Object... values);

    /**
     * Add not in filter.
     */
    FilterBuilder notIn(String field, Object... values);

    /**
     * Add contains filter (for text fields).
     */
    FilterBuilder contains(String field, String value);

    /**
     * Add geo-distance filter.
     */
    FilterBuilder geoDistance(String field, Object location, String distance);

    /**
     * Combine filters with AND.
     */
    FilterBuilder and(FilterBuilder other);

    /**
     * Combine filters with OR.
     */
    FilterBuilder or(FilterBuilder other);
}

