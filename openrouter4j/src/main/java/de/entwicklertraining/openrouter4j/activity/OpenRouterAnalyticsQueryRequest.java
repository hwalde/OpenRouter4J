package de.entwicklertraining.openrouter4j.activity;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
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
     * {@code order_by} and any options added via
     * {@link Builder#option(String, Object)} (e.g. the
     * {@code classifier_dimensions} / {@code classifier_filters} objects).
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

    @Override
    public OpenRouterAnalyticsQueryResponse createResponse(String responseBody) {
        return new OpenRouterAnalyticsQueryResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterAnalyticsQueryRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterAnalyticsQueryRequest> {

        private static final int MAX_DIMENSIONS = 2;

        private final OpenRouterClient client;
        private final List<String> metrics = new ArrayList<>();
        private final List<String> dimensions = new ArrayList<>();
        private final List<JSONObject> filters = new ArrayList<>();
        private final List<JSONObject> extraOptions = new ArrayList<>();
        private String granularity;
        private String timeRangeStart;
        private String timeRangeEnd;
        private Integer limit;
        private Integer groupLimit;
        private String orderByField;
        private String orderByDirection;

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
         * Adds any other documented body key verbatim - the classifier
         * extensions ({@code classifier_dimensions}, {@code classifier_filters},
         * each an object) and anything OpenRouter adds later. {@code null}
         * values are ignored.
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

        @Override
        public OpenRouterAnalyticsQueryRequest build() {
            if (metrics.isEmpty()) {
                throw new IllegalStateException("At least one metric is required");
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
