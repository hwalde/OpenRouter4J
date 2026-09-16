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
 * A request to read the top public apps ranked by token usage:
 * GET https://openrouter.ai/api/v1/datasets/app-rankings
 *
 * <p>Matches the public apps marketplace; token totals are prompt plus
 * completion tokens. {@code sort=popular} ranks by window volume,
 * {@code sort=trending} by absolute excess token growth (apps without growth
 * are omitted, so fewer than {@code limit} rows may be returned). Filtering
 * re-numbers ranks 1..N while paging keeps the absolute {@code rank}.
 * Works with any valid OpenRouter API key; rate-limited. Data is licensed
 * under CC BY 4.0 - republish with attribution to OpenRouter.
 */
public final class OpenRouterAppRankingsRequest extends OpenRouterRequest<OpenRouterAppRankingsResponse> {

    private final OpenRouterClient client;
    private final Map<String, String> queryParams;

    private OpenRouterAppRankingsRequest(Builder builder) {
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
        return appendQuery("/datasets/app-rankings");
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
    public OpenRouterAppRankingsResponse createResponse(String responseBody) {
        return new OpenRouterAppRankingsResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterAppRankingsRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterAppRankingsRequest> {

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
         * Sets the query key {@code category} - marketplace category group to
         * filter by ({@code coding}, {@code creative}, {@code productivity},
         * {@code entertainment}).
         *
         * @param category the category group
         * @return this builder
         */
        public Builder category(String category) {
            return queryParam("category", category);
        }

        /**
         * Sets the query key {@code subcategory} - marketplace subcategory to
         * filter by (e.g. {@code cli-agent}, {@code ide-extension},
         * {@code cloud-agent}, {@code creative-writing}, {@code roleplay});
         * takes precedence over {@code category}, and when both are supplied
         * the pair must be consistent.
         *
         * @param subcategory the subcategory
         * @return this builder
         */
        public Builder subcategory(String subcategory) {
            return queryParam("subcategory", subcategory);
        }

        /**
         * Sets the query key {@code sort} - {@code popular} (default) ranks
         * by total token volume inside the window, {@code trending} by
         * absolute excess token growth.
         *
         * @param sort the sort key
         * @return this builder
         */
        public Builder sort(String sort) {
            return queryParam("sort", sort);
        }

        /**
         * Sets the query key {@code start_date} - start of the date window
         * (YYYY-MM-DD UTC, inclusive); defaults to 30 days before
         * {@code end_date}. The dataset begins at 2025-01-01; earlier values
         * are clamped forward.
         *
         * @param startDate the window start
         * @return this builder
         */
        public Builder startDate(String startDate) {
            return queryParam("start_date", startDate);
        }

        /**
         * Sets the query key {@code end_date} - end of the date window
         * (YYYY-MM-DD UTC, inclusive); defaults to the most recent completed
         * UTC day. Must be on or after 2025-01-01, earlier values are
         * rejected with a 400.
         *
         * @param endDate the window end
         * @return this builder
         */
        public Builder endDate(String endDate) {
            return queryParam("end_date", endDate);
        }

        /**
         * Sets the query key {@code limit} - maximum number of apps (1-100,
         * default 50).
         *
         * @param limit the maximum number of apps
         * @return this builder
         */
        public Builder limit(Integer limit) {
            return queryParam("limit", limit);
        }

        /**
         * Sets the query key {@code offset} - number of ranked apps to skip
         * (0-100, default 0); {@code rank} stays absolute.
         *
         * @param offset the number of rows to skip
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
        public OpenRouterAppRankingsRequest build() {
            return new OpenRouterAppRankingsRequest(this);
        }

        @Override
        public OpenRouterAppRankingsResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterAppRankingsResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterAppRankingsResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
