package de.entwicklertraining.openrouter4j.publicdata;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A request to read the aggregated cost per session by harness and model:
 * GET https://openrouter.ai/api/v1/datasets/session-cost
 *
 * <p>Weekly refreshed, privacy-preserving aggregation of per-session USD
 * spend for the published harnesses; sessions are never pooled across apps.
 * Filtering by {@code model} alone works across apps for
 * harness-vs-harness comparison at a fixed model. Works with any valid
 * OpenRouter API key; rate-limited. Data is licensed under CC BY 4.0 -
 * republish with attribution to OpenRouter.
 */
public final class OpenRouterSessionCostRequest extends OpenRouterRequest<OpenRouterSessionCostResponse> {

    private final OpenRouterClient client;
    private final Map<String, String> queryParams;

    private OpenRouterSessionCostRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.queryParams = Collections.unmodifiableMap(new LinkedHashMap<>(builder.queryParams));
    }

    /**
     * @return the query parameters this request sends, in insertion order
     */
    public Map<String, String> queryParams() {
        return queryParams;
    }

    @Override
    public String getRelativeUrl() {
        return appendQuery("/datasets/session-cost");
    }

    private String appendQuery(String path) {
        if (queryParams.isEmpty()) {
            return path;
        }
        StringBuilder sb = new StringBuilder(path);
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            sb.append(sb.indexOf("?") < 0 ? '?' : '&');
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
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
    public OpenRouterSessionCostResponse createResponse(String responseBody) {
        return new OpenRouterSessionCostResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterSessionCostRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterSessionCostRequest> {

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
         * Sets the query key {@code app_slug} - filter to one published
         * harness slug.
         *
         * @param appSlug the harness slug
         * @return this builder
         */
        public Builder appSlug(String appSlug) {
            return queryParam("app_slug", appSlug);
        }

        /**
         * Sets the query key {@code model} - exact model permaslug filter;
         * works across all harness apps.
         *
         * @param model the model permaslug
         * @return this builder
         */
        public Builder model(String model) {
            return queryParam("model", model);
        }

        /**
         * Sets the query key {@code turn_range} - filter by the inclusive
         * number of turns in a session ({@code 1-turn}, {@code 2-9-turns},
         * {@code 10-49-turns}, {@code 50-plus-turns}).
         *
         * @param turnRange the turn range
         * @return this builder
         */
        public Builder turnRange(String turnRange) {
            return queryParam("turn_range", turnRange);
        }

        /**
         * Sets the query key {@code limit} - maximum number of cells to
         * return (1-500, default 100).
         *
         * @param limit the maximum number of cells
         * @return this builder
         */
        public Builder limit(Integer limit) {
            return queryParam("limit", limit);
        }

        /**
         * Sets the query key {@code offset} - number of sorted cells to skip
         * (0-5000, default 0).
         *
         * @param offset the number of cells to skip
         * @return this builder
         */
        public Builder offset(Integer offset) {
            return queryParam("offset", offset);
        }

        /**
         * Adds any documented query parameter verbatim, for parameters
         * OpenRouter adds later. The value is sent URL-encoded; {@code null}
         * values are ignored.
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
        public OpenRouterSessionCostRequest build() {
            return new OpenRouterSessionCostRequest(this);
        }

        @Override
        public OpenRouterSessionCostResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterSessionCostResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterSessionCostResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
