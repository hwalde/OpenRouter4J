package de.entwicklertraining.openrouter4j.activity;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A request to query the account's usage analytics:
 * POST https://openrouter.ai/api/v1/analytics/query
 *
 * <p>The endpoint is a metric/dimension query engine: you select metrics, at
 * most two dimensions to group by, optional filters, and an optional time
 * range. The response rows are free-form objects whose keys follow the
 * requested metrics and dimensions, so {@link OpenRouterAnalyticsQueryResponse#rows()}
 * exposes them as raw {@link JSONObject}s. Use {@code GET /analytics/meta} to
 * discover the available metrics and dimensions.
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint.
 */
public final class OpenRouterAnalyticsQueryRequest extends OpenRouterRequest<OpenRouterAnalyticsQueryResponse> {

    private final OpenRouterClient client;
    private final List<String> metrics;
    private final List<String> dimensions;
    private final List<JSONObject> filters;
    private final String granularity;
    private final String timeRangeStart;
    private final String timeRangeEnd;
    private final Integer limit;
    private final Integer groupLimit;
    private final String orderByField;
    private final String orderByDirection;
    private final String classifierDimensionsId;
    private final List<String> classifierDimensionNames;
    private final Boolean classifierIncludeNulls;
    private final String classifierFiltersId;
    private final List<JSONObject> classifierFilterEntries;
    private final List<JSONObject> extraOptions;

    private OpenRouterAnalyticsQueryRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.metrics = List.copyOf(builder.metrics);
        this.dimensions = List.copyOf(builder.dimensions);
        this.filters = List.copyOf(builder.filters);
        this.granularity = builder.granularity;
        this.timeRangeStart = builder.timeRangeStart;
        this.timeRangeEnd = builder.timeRangeEnd;
        this.limit = builder.limit;
        this.groupLimit = builder.groupLimit;
        this.orderByField = builder.orderByField;
        this.orderByDirection = builder.orderByDirection;
        this.classifierDimensionsId = builder.classifierDimensionsId;
        this.classifierDimensionNames = List.copyOf(builder.classifierDimensionNames);
        this.classifierIncludeNulls = builder.classifierIncludeNulls;
        this.classifierFiltersId = builder.classifierFiltersId;
        this.classifierFilterEntries = List.copyOf(builder.classifierFilterEntries);
        this.extraOptions = List.copyOf(builder.extraOptions);
    }

    @Override
    public String getRelativeUrl() {
        return "/analytics/query";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * JSON path: the request body - {@code metrics} (required),
     * {@code dimensions} (max 2), {@code filters}, {@code granularity},
     * {@code time_range}, {@code limit}, {@code group_limit},
     * {@code order_by}, {@code classifier_dimensions}
     * ({@code classifier_id} / {@code dimension_names} / {@code include_nulls}),
     * {@code classifier_filters} ({@code classifier_id} / {@code filters}),
     * and any options added via {@link Builder#option(String, Object)}.
     * Each key is emitted only when configured.
     *
     * @return the JSON body of this request
     */
    @Override
    public String getBody() {
        JSONObject root = new JSONObject();

        JSONArray metricsArr = new JSONArray();
        for (String metric : metrics) {
            metricsArr.put(metric);
        }
        root.put("metrics", metricsArr);

        if (!dimensions.isEmpty()) {
            JSONArray dimensionsArr = new JSONArray();
            for (String dimension : dimensions) {
                dimensionsArr.put(dimension);
            }
            root.put("dimensions", dimensionsArr);
        }

        if (!filters.isEmpty()) {
            JSONArray filtersArr = new JSONArray();
            for (JSONObject filter : filters) {
                filtersArr.put(filter);
            }
            root.put("filters", filtersArr);
        }

        if (granularity != null) {
            root.put("granularity", granularity);
        }

        if (timeRangeStart != null || timeRangeEnd != null) {
            JSONObject timeRange = new JSONObject();
            if (timeRangeStart != null) {
                timeRange.put("start", timeRangeStart);
            }
            if (timeRangeEnd != null) {
                timeRange.put("end", timeRangeEnd);
            }
            root.put("time_range", timeRange);
        }

        if (limit != null) {
            root.put("limit", limit);
        }

        if (groupLimit != null) {
            root.put("group_limit", groupLimit);
        }

        if (orderByField != null) {
            JSONObject orderBy = new JSONObject();
            orderBy.put("field", orderByField);
            orderBy.put("direction", orderByDirection != null ? orderByDirection : "desc");
            root.put("order_by", orderBy);
        }

        if (classifierDimensionsId != null) {
            JSONObject classifierDimensions = new JSONObject();
            classifierDimensions.put("classifier_id", classifierDimensionsId);
            if (!classifierDimensionNames.isEmpty()) {
                JSONArray namesArr = new JSONArray();
                for (String name : classifierDimensionNames) {
                    namesArr.put(name);
                }
                classifierDimensions.put("dimension_names", namesArr);
            }
            if (classifierIncludeNulls != null) {
                classifierDimensions.put("include_nulls", classifierIncludeNulls);
            }
            root.put("classifier_dimensions", classifierDimensions);
        }

        if (classifierFiltersId != null) {
            JSONObject classifierFilters = new JSONObject();
            classifierFilters.put("classifier_id", classifierFiltersId);
            JSONArray classifierFiltersArr = new JSONArray();
            for (JSONObject filter : classifierFilterEntries) {
                classifierFiltersArr.put(filter);
            }
            classifierFilters.put("filters", classifierFiltersArr);
            root.put("classifier_filters", classifierFilters);
        }

        for (JSONObject option : extraOptions) {
            for (String key : option.keySet()) {
                root.put(key, option.get(key));
            }
        }

        return root.toString();
    }

    /**
     * @return the metrics requested
     */
    public List<String> metrics() {
        return metrics;
    }

    /**
     * @return the dimensions grouped by
     */
    public List<String> dimensions() {
        return dimensions;
    }

    /**
     * @return the raw filter objects sent with the request
     */
    public List<JSONObject> filters() {
        return filters;
    }

    /**
     * @return the {@code classifier_dimensions.classifier_id}, or {@code null} when unset
     */
    public String classifierDimensionsId() {
        return classifierDimensionsId;
    }

    /**
     * @return the {@code classifier_dimensions.dimension_names} (at most 2), empty when unset
     */
    public List<String> classifierDimensionNames() {
        return classifierDimensionNames;
    }

    /**
     * @return the {@code classifier_dimensions.include_nulls} flag, or {@code null} when unset
     */
    public Boolean classifierIncludeNulls() {
        return classifierIncludeNulls;
    }

    /**
     * @return the {@code classifier_filters.classifier_id}, or {@code null} when unset
     */
    public String classifierFiltersId() {
        return classifierFiltersId;
    }

    /**
     * @return the raw {@code classifier_filters.filters} entry objects
     */
    public List<JSONObject> classifierFilters() {
        return classifierFilterEntries;
    }

    @Override
    public OpenRouterAnalyticsQueryResponse createResponse(String responseBody) {
        return new OpenRouterAnalyticsQueryResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterAnalyticsQueryRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterAnalyticsQueryRequest> {

        private static final int MAX_DIMENSIONS = 2;
        private static final int MAX_CLASSIFIER_DIMENSION_NAMES = 2;
        private static final int MAX_CLASSIFIER_FILTERS = 10;

        private final OpenRouterClient client;
        private final List<String> metrics = new ArrayList<>();
        private final List<String> dimensions = new ArrayList<>();
        private final List<JSONObject> filters = new ArrayList<>();
        private final List<JSONObject> extraOptions = new ArrayList<>();
        private final List<String> classifierDimensionNames = new ArrayList<>();
        private final List<JSONObject> classifierFilterEntries = new ArrayList<>();
        private String granularity;
        private String timeRangeStart;
        private String timeRangeEnd;
        private Integer limit;
        private Integer groupLimit;
        private String orderByField;
        private String orderByDirection;
        private String classifierDimensionsId;
        private Boolean classifierIncludeNulls;
        private String classifierFiltersId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         */
        public Builder(OpenRouterClient client) {
            super(client);
            this.client = client;
        }

        /**
         * Adds JSON field(s) to {@code metrics} (required, at least one): the
         * metric names to aggregate (e.g. {@code request_count}). Use
         * {@code GET /analytics/meta} to discover the available metrics.
         *
         * @param metrics the metric names
         * @return this builder
         */
        public Builder metrics(String... metrics) {
            if (metrics != null) {
                for (String metric : metrics) {
                    if (metric != null && !metric.isEmpty()) {
                        this.metrics.add(metric);
                    }
                }
            }
            return this;
        }

        /**
         * Adds JSON field(s) to {@code dimensions} (at most 2): the fields to
         * group by (e.g. {@code model}). Use {@code GET /analytics/meta} to
         * discover the available dimensions.
         *
         * @param dimensions the dimension names
         * @return this builder
         * @throws IllegalArgumentException when more than two dimensions are configured
         */
        public Builder dimensions(String... dimensions) {
            if (dimensions != null) {
                for (String dimension : dimensions) {
                    if (dimension != null && !dimension.isEmpty()) {
                        if (this.dimensions.size() >= MAX_DIMENSIONS) {
                            throw new IllegalArgumentException(
                                    "At most " + MAX_DIMENSIONS + " dimensions are supported");
                        }
                        this.dimensions.add(dimension);
                    }
                }
            }
            return this;
        }

        /**
         * Adds a JSON object to {@code filters} with a scalar value:
         * {@code {"field": field, "operator": operator, "value": value}}.
         * Filters must use the underlying id of enriched dimensions (e.g.
         * the permaslug for {@code model}, the workspace UUID for
         * {@code workspace}), not the human-readable label.
         *
         * @param field the dimension to filter on
         * @param operator the filter operator (e.g. {@code eq}, {@code neq}, {@code gt}, {@code in})
         * @param value the filter value
         * @return this builder
         */
        public Builder filter(String field, String operator, String value) {
            if (field != null && operator != null && value != null) {
                JSONObject filter = new JSONObject();
                filter.put("field", field);
                filter.put("operator", operator);
                filter.put("value", value);
                filters.add(filter);
            }
            return this;
        }

        /**
         * Adds a JSON object to {@code filters} with an array value (for set
         * operators such as {@code in} / {@code not_in}):
         * {@code {"field": field, "operator": operator, "value": [values...]}}.
         *
         * @param field the dimension to filter on
         * @param operator the filter operator ({@code in} or {@code not_in})
         * @param values the filter values
         * @return this builder
         */
        public Builder filterIn(String field, String operator, List<String> values) {
            if (field != null && operator != null && values != null && !values.isEmpty()) {
                JSONObject filter = new JSONObject();
                filter.put("field", field);
                filter.put("operator", operator);
                JSONArray arr = new JSONArray();
                for (String value : values) {
                    if (value != null) {
                        arr.put(value);
                    }
                }
                filter.put("value", arr);
                filters.add(filter);
            }
            return this;
        }

        /**
         * Sets the JSON field {@code granularity} - the time granularity of
         * the series (e.g. {@code day}); omit for totals instead of a series.
         *
         * @param granularity the granularity
         * @return this builder
         */
        public Builder granularity(String granularity) {
            this.granularity = granularity;
            return this;
        }

        /**
         * Sets the JSON field {@code time_range} - the ISO 8601 UTC window of
         * the query. Both bounds must include seconds
         * ({@code YYYY-MM-DDTHH:MM:SSZ}); minute-precision timestamps are
         * rejected by the API.
         *
         * @param start the window start
         * @param end the window end
         * @return this builder
         */
        public Builder timeRange(String start, String end) {
            this.timeRangeStart = start;
            this.timeRangeEnd = end;
            return this;
        }

        /**
         * Sets the JSON field {@code limit} - maximum total rows returned
         * (API default 1000).
         *
         * @param limit the row limit
         * @return this builder
         */
        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        /**
         * Sets the JSON field {@code group_limit} - maximum rows per distinct
         * dimension combination. On time-series queries it is auto-computed
         * when omitted; an explicit lower value may truncate time buckets.
         *
         * @param groupLimit the per-group row limit
         * @return this builder
         */
        public Builder groupLimit(Integer groupLimit) {
            this.groupLimit = groupLimit;
            return this;
        }

        /**
         * Sets the JSON field {@code order_by} - the field to order by (a
         * requested metric, {@code request_count}, a requested dimension or
         * {@code date}) and the direction ({@code asc} or {@code desc};
         * {@code desc} is sent when {@code null}).
         *
         * @param field the field to order by
         * @param direction {@code asc} or {@code desc}
         * @return this builder
         */
        public Builder orderBy(String field, String direction) {
            this.orderByField = field;
            this.orderByDirection = direction;
            return this;
        }

        /**
         * Sets the JSON object {@code classifier_dimensions} - group results by
         * custom classifier tags. Required inside it:
         * {@code classifier_id} (the UUID of the classifier whose tags to group
         * by); optional {@code dimension_names} (snake_case identifiers, at most
         * 2 - each name becomes its own column key in the response; with no
         * names the response uses {@code clf_dimension_name} /
         * {@code clf_dimension_value} columns). Requires an active classifier
         * on the workspace. Trap: when combined with
         * {@link #classifierFilters(String)} / {@link #classifierFilter(String, String, String)}
         * the two must carry the same {@code classifier_id} - the API rejects a
         * mismatch, and {@link #build()} does too.
         *
         * @param classifierId the classifier UUID (required, non-empty)
         * @param dimensionNames at most two classifier dimension names
         * @return this builder
         * @throws IllegalArgumentException when the id is empty or more than two names are given
         */
        public Builder classifierDimensions(String classifierId, String... dimensionNames) {
            if (classifierId == null || classifierId.isEmpty()) {
                throw new IllegalArgumentException("classifier_id is required for classifier_dimensions");
            }
            if (this.classifierDimensionsId != null && !this.classifierDimensionsId.equals(classifierId)) {
                throw new IllegalArgumentException(
                        "classifier_dimensions.classifier_id is already set to " + this.classifierDimensionsId);
            }
            this.classifierDimensionsId = classifierId;
            if (dimensionNames != null) {
                for (String name : dimensionNames) {
                    if (name != null && !name.isEmpty()) {
                        if (this.classifierDimensionNames.size() >= MAX_CLASSIFIER_DIMENSION_NAMES) {
                            throw new IllegalArgumentException(
                                    "At most " + MAX_CLASSIFIER_DIMENSION_NAMES
                                            + " classifier dimension_names are supported");
                        }
                        this.classifierDimensionNames.add(name);
                    }
                }
            }
            return this;
        }

        /**
         * Sets the JSON field {@code classifier_dimensions.include_nulls} -
         * when {@code true}, generations without any tag of this classifier are
         * included in the result. API default {@code false} (only classified
         * generations). {@code false} is a set option and is emitted. Only
         * emitted together with {@link #classifierDimensions(String, String...)}
         * (see the loud check in {@link #build()}).
         *
         * @param includeNulls the flag, {@code null} to leave unset
         * @return this builder
         */
        public Builder classifierIncludeNulls(Boolean includeNulls) {
            this.classifierIncludeNulls = includeNulls;
            return this;
        }

        /**
         * Sets the JSON field {@code classifier_filters.classifier_id} - the
         * UUID of the classifier whose tag values
         * {@link #classifierFilter(String, String, String)} restricts the result
         * to. Required (with at least one filter entry) whenever classifier
         * filters are used; may also be combined with
         * {@link #classifierDimensions(String, String...)}, but only with the
         * same {@code classifier_id}.
         *
         * @param classifierId the classifier UUID (required, non-empty)
         * @return this builder
         * @throws IllegalArgumentException when the id is empty or already set to a different value
         */
        public Builder classifierFilters(String classifierId) {
            if (classifierId == null || classifierId.isEmpty()) {
                throw new IllegalArgumentException("classifier_id is required for classifier_filters");
            }
            if (this.classifierFiltersId != null && !this.classifierFiltersId.equals(classifierId)) {
                throw new IllegalArgumentException(
                        "classifier_filters.classifier_id is already set to " + this.classifierFiltersId);
            }
            this.classifierFiltersId = classifierId;
            return this;
        }

        /**
         * Adds one entry to {@code classifier_filters.filters} with a scalar
         * string tag value:
         * {@code {"field": field, "operator": operator, "value": value}}.
         * Only {@code eq} and {@code neq} for scalar values; array values with
         * {@code in} / {@code not_in} belong to
         * {@link #classifierFilterIn(String, String, Collection)}. Ordered
         * comparisons are not available because classification values are
         * strings. Requires {@link #classifierFilters(String)} to set the
         * classifier id (order of the calls does not matter). At most 10
         * entries.
         *
         * @param field the classifier dimension name to filter on (snake_case)
         * @param operator the filter operator ({@code eq} or {@code neq})
         * @param value the scalar tag value
         * @return this builder
         * @throws IllegalArgumentException when any argument is empty, the entry limit is exceeded, the operator does not match the scalar value shape or the value type is wrong
         */
        public Builder classifierFilter(String field, String operator, String value) {
            return addClassifierFilter(field, operator, value);
        }

        /**
         * Adds one entry to {@code classifier_filters.filters} with a scalar
         * numeric tag value (the schema also accepts numbers alongside
         * strings). Only {@code eq} and {@code neq} for scalar values; array
         * values with {@code in} / {@code not_in} belong to
         * {@link #classifierFilterIn(String, String, Collection)}.
         *
         * @param field the classifier dimension name to filter on (snake_case)
         * @param operator the filter operator ({@code eq} or {@code neq})
         * @param value the scalar numeric tag value
         * @return this builder
         * @throws IllegalArgumentException when any argument is empty, the entry limit is exceeded, the operator does not match the scalar value shape or the value type is wrong
         */
        public Builder classifierFilter(String field, String operator, Number value) {
            return addClassifierFilter(field, operator, value);
        }

        /**
         * Adds one entry to {@code classifier_filters.filters} with an array
         * value (for the set operators):
         * {@code {"field": field, "operator": operator, "value": [values...]}}.
         * Only {@code in} and {@code not_in} for array values; scalars with
         * {@code eq} / {@code neq} belong to
         * {@link #classifierFilter(String, String, String)}. Element types may
         * be strings or numbers, matching the schema.
         *
         * @param field the classifier dimension name to filter on (snake_case)
         * @param operator the filter operator ({@code in} or {@code not_in})
         * @param values the tag values (strings or numbers, non-empty)
         * @return this builder
         * @throws IllegalArgumentException when any argument is empty, the entry limit is exceeded, the operator does not match the array value shape or an element type is wrong
         */
        public Builder classifierFilterIn(String field, String operator, Collection<?> values) {
            if (values == null || values.isEmpty()) {
                throw new IllegalArgumentException("classifier filter values must not be empty");
            }
            JSONArray arr = new JSONArray();
            for (Object value : values) {
                if (!(value instanceof String) && !(value instanceof Number)) {
                    throw new IllegalArgumentException(
                            "classifier filter array elements must be strings or numbers");
                }
                arr.put(value);
            }
            return addClassifierFilter(field, operator, arr);
        }

        private Builder addClassifierFilter(String field, String operator, Object value) {
            if (field == null || field.isEmpty()) {
                throw new IllegalArgumentException("classifier filter field is required");
            }
            if (operator == null || operator.isEmpty()) {
                throw new IllegalArgumentException("classifier filter operator is required");
            }
            if (classifierFilterEntries.size() >= MAX_CLASSIFIER_FILTERS) {
                throw new IllegalArgumentException(
                        "At most " + MAX_CLASSIFIER_FILTERS + " classifier_filters.filters entries are supported");
            }
            boolean isArrayValue = value instanceof JSONArray;
            if (isArrayValue) {
                if (!"in".equals(operator) && !"not_in".equals(operator)) {
                    throw new IllegalArgumentException(
                            "classifier filter array values require operator in or not_in, got " + operator);
                }
            } else if (!"eq".equals(operator) && !"neq".equals(operator)) {
                throw new IllegalArgumentException(
                        "classifier filter scalar values require operator eq or neq, got " + operator);
            }
            JSONObject filter = new JSONObject();
            filter.put("field", field);
            filter.put("operator", operator);
            filter.put("value", value);
            classifierFilterEntries.add(filter);
            return this;
        }

        /**
         * Adds any other documented body key verbatim - anything OpenRouter
         * adds beyond the typed surface. Applied last, so a verbatim key
         * overwrites the typed field of the same name. {@code null} values are
         * ignored.
         *
         * @param name the body key
         * @param value the body value (usually a {@link JSONObject})
         * @return this builder
         */
        public Builder option(String name, Object value) {
            if (name != null && !name.isEmpty() && value != null) {
                extraOptions.add(new JSONObject().put(name, value));
            }
            return this;
        }

        /**
         * Validates the required metric and the classifier combination (the
         * same {@code classifier_id} for {@code classifier_dimensions} and
         * {@code classifier_filters}, no half-configured classifier object).
         *
         * @return the built request
         * @throws IllegalStateException when no metric is configured or a classifier combination is incomplete or the two {@code classifier_id}s mismatch
         */
        @Override
        public OpenRouterAnalyticsQueryRequest build() {
            if (metrics.isEmpty()) {
                throw new IllegalStateException("At least one metric is required");
            }
            if (classifierIncludeNulls != null && classifierDimensionsId == null) {
                throw new IllegalStateException(
                        "classifierIncludeNulls requires classifierDimensions(classifierId, ...)");
            }
            if (classifierFiltersId != null && classifierFilterEntries.isEmpty()) {
                throw new IllegalStateException(
                        "classifierFilters(classifierId) requires at least one classifierFilter entry");
            }
            if (!classifierFilterEntries.isEmpty() && classifierFiltersId == null) {
                throw new IllegalStateException(
                        "classifierFilter entries require classifierFilters(classifierId)");
            }
            if (classifierDimensionsId != null && classifierFiltersId != null
                    && !classifierDimensionsId.equals(classifierFiltersId)) {
                throw new IllegalStateException(
                        "classifier_dimensions and classifier_filters must use the same classifier_id, got "
                                + classifierDimensionsId + " and " + classifierFiltersId);
            }
            return new OpenRouterAnalyticsQueryRequest(this);
        }

        @Override
        public OpenRouterAnalyticsQueryResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterAnalyticsQueryResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterAnalyticsQueryResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
