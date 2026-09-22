package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

/**
 * A request to list the interns visible to the API key, newest first:
 * GET https://openrouter.ai/api/v1/interns
 *
 * <p>Query parameters: {@code workspace_id} (must match the API key
 * workspace), the lifecycle {@code status} filter (comma-separated, at most
 * 8), {@code limit} (1-500) and {@code starting_after} (the opaque
 * {@code next_cursor} of the previous page; a malformed cursor is a 400).
 * There is no default workspace fallback; regional hostnames are refused.
 *
 * <p>Trap: every path answers 404 for keys outside the interns programme.
 */
public final class OpenRouterInternsListRequest
        extends OpenRouterRequest<OpenRouterInternsListResponse> {

    private final OpenRouterClient client;
    private final Map<String, String> queryParams;

    private OpenRouterInternsListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.queryParams = Collections.unmodifiableMap(new LinkedHashMap<>(builder.queryParams));
    }

    /**
     * @return the query parameters this request will send (typed ones
     *         included, all values verbatim)
     */
    public Map<String, String> queryParams() {
        return queryParams;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/interns");
        if (!queryParams.isEmpty()) {
            sb.append('?');
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) {
                    sb.append('&');
                }
                first = false;
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                        .append('=')
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
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
    public OpenRouterInternsListResponse createResponse(String responseBody) {
        return new OpenRouterInternsListResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterInternsListRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterInternsListRequest> {

        private final OpenRouterClient client;
        private final Map<String, String> queryParams = new LinkedHashMap<>();

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
         * Sets the query key {@code limit} - maximum number of interns to
         * return, 1 through 500 (validated loudly).
         *
         * @param limit the page size
         * @return this builder
         */
        public Builder limit(Integer limit) {
            if (limit != null && (limit < 1 || limit > 500)) {
                throw new IllegalArgumentException("limit must be between 1 and 500");
            }
            return queryParam("limit", limit);
        }

        /**
         * Sets the query key {@code status} - the lifecycle statuses to
         * include (at most 8, validated loudly; repeats are collapsed).
         * Documented values: {@code awaiting_slack_install}, {@code queued},
         * {@code provisioning}, {@code running}, {@code failed},
         * {@code stopped}, {@code destroying}, {@code destroy_failed}.
         * Unknown values are passed through verbatim (the API allows them).
         * Replaces a previously set list.
         *
         * @param statuses the lifecycle statuses to filter for
         * @return this builder
         */
        public Builder status(List<String> statuses) {
            if (statuses == null) {
                queryParams.remove("status");
                return this;
            }
            LinkedHashSet<String> distinct = new LinkedHashSet<>();
            for (String status : statuses) {
                if (status != null && !status.isBlank()) {
                    distinct.add(status);
                }
            }
            if (distinct.size() > 8) {
                throw new IllegalArgumentException("at most 8 status values are allowed");
            }
            if (distinct.isEmpty()) {
                queryParams.remove("status");
                return this;
            }
            return queryParam("status", String.join(",", distinct));
        }

        /**
         * Sets the query key {@code status} - see {@link #status(List)}.
         * Replaces a previously set list.
         *
         * @param statuses the lifecycle statuses to filter for
         * @return this builder
         */
        public Builder status(String... statuses) {
            List<String> list = new ArrayList<>();
            for (String status : statuses) {
                if (status != null && !status.isBlank()) {
                    list.add(status);
                }
            }
            return status(list);
        }

        /**
         * Sets the query key {@code starting_after} - the opaque
         * {@code next_cursor} of the previous page. A malformed cursor is
         * rejected with 400.
         *
         * @param startingAfter the pagination cursor
         * @return this builder
         */
        public Builder startingAfter(String startingAfter) {
            return queryParam("starting_after", startingAfter);
        }

        /**
         * Sets the query key {@code workspace_id} - only return interns in
         * this workspace. It must match the API key workspace.
         *
         * @param workspaceId the workspace id (UUID)
         * @return this builder
         */
        public Builder workspaceId(String workspaceId) {
            return queryParam("workspace_id", workspaceId);
        }

        /**
         * Sets any additional query parameter verbatim, for filters without
         * a typed method above and for any parameter OpenRouter adds later.
         * The value is sent URL-encoded; {@code null} values are ignored.
         *
         * @param name the query parameter name
         * @param value the query parameter value
         * @return this builder
         */
        public Builder queryParam(String name, Object value) {
            if (name != null && !name.isEmpty() && value != null) {
                queryParams.put(name, String.valueOf(value));
            }
            return this;
        }

        @Override
        public OpenRouterInternsListRequest build() {
            return new OpenRouterInternsListRequest(this);
        }

        @Override
        public OpenRouterInternsListResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterInternsListResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterInternsListResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
