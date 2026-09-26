package de.entwicklertraining.openrouter4j.batches;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * A request to list the batches of the workspace of the authenticating API
 * key, newest first:
 * GET https://openrouter.ai/api/v1/batches
 *
 * <p>Query parameters: {@code limit} (1-100, default 20), {@code after} (the
 * previous page's {@code last_id} - cursor pagination, no offsets and no
 * {@code before}), the repeatable {@code status} filter ({@code
 * validating}, {@code in_progress}, {@code completed}, {@code failed},
 * {@code expired}, {@code cancelled} - the transient {@code finalizing} and
 * {@code cancelling} states are NOT accepted as filters and are rejected
 * loudly) and {@code created_after} / {@code created_before} (Unix seconds
 * or an ISO-8601 date/datetime; {@code created_after} must be earlier than
 * {@code created_before} when both are present). Batches are scoped to the
 * workspace, not the key: every key of the same workspace sees the same
 * list. List items are metadata-only ({@code results} is always
 * {@code null}).
 */
public final class OpenRouterBatchListRequest
        extends OpenRouterRequest<OpenRouterBatchListResponse> {

    private final OpenRouterClient client;
    private final Map<String, List<String>> queryParams;

    private OpenRouterBatchListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        Map<String, List<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : builder.queryParams.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        this.queryParams = Collections.unmodifiableMap(copy);
    }

    /**
     * @return the query parameters this request will send (typed ones
     *         included, all values verbatim; a repeated key like
     *         {@code status} carries one entry per value)
     */
    public Map<String, List<String>> queryParams() {
        return queryParams;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/batches");
        if (!queryParams.isEmpty()) {
            boolean first = true;
            for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                for (String value : entry.getValue()) {
                    sb.append(first ? '?' : '&');
                    first = false;
                    sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                            .append('=')
                            .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String getHttpMethod() {
        return "GET";
    }

    /**
     * GET requests carry no body.
     *
     * @return always {@code null}
     */
    @Override
    public String getBody() {
        return null;
    }

    @Override
    public OpenRouterBatchListResponse createResponse(String responseBody) {
        return new OpenRouterBatchListResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterBatchListRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterBatchListRequest> {

        private final OpenRouterClient client;
        private final Map<String, List<String>> queryParams = new LinkedHashMap<>();

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
         * Sets the query key {@code limit} - number of batches to return,
         * 1 through 100 (validated loudly; the API default is 20).
         *
         * @param limit the page size
         * @return this builder
         */
        public Builder limit(Integer limit) {
            if (limit != null && (limit < 1 || limit > 100)) {
                throw new IllegalArgumentException("limit must be between 1 and 100");
            }
            return queryParam("limit", limit);
        }

        /**
         * Sets the query key {@code after} - continue strictly after this
         * batch id. Pass the previous page's {@code last_id}; pagination
         * does not use offsets or a {@code before} parameter.
         *
         * @param after the pagination cursor (a batch id)
         * @return this builder
         */
        public Builder after(String after) {
            return queryParam("after", after);
        }

        /**
         * Sets the query key {@code status} - the public statuses to include.
         * Repeat the filter for more than one status (the API takes one
         * {@code status} parameter per value, so this emits repeated keys,
         * not a comma list). Documented values: {@code validating},
         * {@code in_progress}, {@code completed}, {@code failed},
         * {@code expired}, {@code cancelled}. The transient {@code
         * finalizing} and {@code cancelling} states are not accepted as list
         * filters and are rejected loudly. Unknown values are passed through
         * verbatim. Replaces a previously set list.
         *
         * @param statuses the statuses to filter for
         * @return this builder
         */
        public Builder status(List<String> statuses) {
            if (statuses == null) {
                return queryParam("status", null);
            }
            LinkedHashSet<String> distinct = new LinkedHashSet<>();
            for (String status : statuses) {
                if (status != null && !status.isBlank()) {
                    distinct.add(status);
                }
            }
            if (distinct.isEmpty()) {
                return queryParam("status", null);
            }
            for (String status : distinct) {
                if ("finalizing".equals(status) || "cancelling".equals(status)) {
                    throw new IllegalArgumentException(
                            "the transient status '" + status
                                    + "' is not accepted as a list filter");
                }
            }
            queryParams.put("status", new ArrayList<>(distinct));
            return this;
        }

        /**
         * Sets the query key {@code status} - see {@link #status(List)}.
         * Replaces a previously set list.
         *
         * @param statuses the statuses to filter for
         * @return this builder
         */
        public Builder status(String... statuses) {
            return status(statuses == null ? null : java.util.Arrays.asList(statuses));
        }

        /**
         * Sets the query key {@code created_after} - include batches created
         * strictly after this time, as Unix seconds or an ISO-8601 date or
         * datetime (e.g. {@code 2026-08-20} or
         * {@code 2026-08-20T00:00:00Z}). Must be earlier than
         * {@code created_before} when both are present (the API rejects the
         * pair otherwise). Creation-time filtering keeps subsecond precision
         * while {@code created_at} is whole seconds - use the {@code after}
         * cursor, not timestamps, when paginating without gaps or overlaps.
         *
         * @param createdAfter the lower bound (Unix seconds or ISO-8601)
         * @return this builder
         */
        public Builder createdAfter(String createdAfter) {
            return queryParam("created_after", createdAfter);
        }

        /**
         * Sets the query key {@code created_after} - see
         * {@link #createdAfter(String)}; this overload takes Unix seconds.
         *
         * @param createdAfter the lower bound in Unix seconds
         * @return this builder
         */
        public Builder createdAfter(long createdAfter) {
            return createdAfter(Long.toString(createdAfter));
        }

        /**
         * Sets the query key {@code created_before} - include batches created
         * strictly before this time, as Unix seconds or an ISO-8601 date or
         * datetime. Must be later than {@code created_after} when both are
         * present (the API rejects the pair otherwise).
         *
         * @param createdBefore the upper bound (Unix seconds or ISO-8601)
         * @return this builder
         */
        public Builder createdBefore(String createdBefore) {
            return queryParam("created_before", createdBefore);
        }

        /**
         * Sets the query key {@code created_before} - see
         * {@link #createdBefore(String)}; this overload takes Unix seconds.
         *
         * @param createdBefore the upper bound in Unix seconds
         * @return this builder
         */
        public Builder createdBefore(long createdBefore) {
            return createdBefore(Long.toString(createdBefore));
        }

        /**
         * Sets any additional query parameter verbatim, for filters without
         * a typed method above and for any parameter OpenRouter adds later.
         * The value is sent URL-encoded; {@code null} values remove the
         * parameter again. Calling this replaces every previous value of the
         * same name (a repeated key like {@code status} keeps only the
         * latest value) - set such parameters with their typed method.
         *
         * @param name the query parameter name
         * @param value the query parameter value
         * @return this builder
         */
        public Builder queryParam(String name, Object value) {
            if (name == null || name.isEmpty()) {
                return this;
            }
            if (value == null) {
                queryParams.remove(name);
            } else {
                queryParams.put(name, List.of(String.valueOf(value)));
            }
            return this;
        }

        @Override
        public OpenRouterBatchListRequest build() {
            return new OpenRouterBatchListRequest(this);
        }

        @Override
        public OpenRouterBatchListResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterBatchListResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterBatchListResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
