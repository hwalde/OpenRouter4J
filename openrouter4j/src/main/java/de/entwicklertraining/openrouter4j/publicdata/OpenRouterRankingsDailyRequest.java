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
 * A request to read the daily token totals for the top 50 public models:
 * GET https://openrouter.ai/api/v1/datasets/rankings-daily
 *
 * <p>Each row is a distinct {@code (date, model_permaslug)} pair; the
 * aggregated {@code other} row uses the reserved permaslug {@code other} and
 * is returned last within its date. Optional filters: {@code period} sets the
 * time grain; {@code modality} and {@code context_bucket} narrow the exact
 * dataset; {@code category} and {@code language_type} read a sampled,
 * upsampled dataset (weekly estimates) and cannot be combined with each other
 * or with the exact filters - they reject {@code period=day} with a 400.
 * Works with any valid OpenRouter API key; rate-limited. Data is licensed
 * under CC BY 4.0 - republish with attribution to OpenRouter.
 */
public final class OpenRouterRankingsDailyRequest extends OpenRouterRequest<OpenRouterRankingsDailyResponse> {

    private final OpenRouterClient client;
    private final Map<String, String> queryParams;

    private OpenRouterRankingsDailyRequest(Builder builder) {
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
        return appendQuery("/datasets/rankings-daily");
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
    public OpenRouterRankingsDailyResponse createResponse(String responseBody) {
        return new OpenRouterRankingsDailyResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterRankingsDailyRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterRankingsDailyRequest> {

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
         * Sets the query key {@code period} - the time grain of each row:
         * {@code day} (default), {@code week} (ISO week start) or
         * {@code month}. With {@code category} or {@code language_type} only
         * {@code week}/{@code month} are available.
         *
         * @param period the time grain
         * @return this builder
         */
        public Builder period(String period) {
            return queryParam("period", period);
        }

        /**
         * Sets the query key {@code modality} - restrict to a modality
         * surface: {@code text} / {@code image_output} match output
         * modality, {@code image} / {@code audio} match input modality,
         * {@code tool_calling} keeps only rows with at least one tool call.
         * Exact dataset - cannot be combined with {@code category} or
         * {@code language_type}.
         *
         * @param modality the modality surface
         * @return this builder
         */
        public Builder modality(String modality) {
            return queryParam("modality", modality);
        }

        /**
         * Sets the query key {@code context_bucket} - restrict to requests
         * whose context length falls in the bucket ({@code 1K}, {@code 10K},
         * {@code 100K}, {@code 1M}, {@code 10M}). Exact dataset - cannot be
         * combined with {@code category} or {@code language_type}.
         *
         * @param contextBucket the context bucket
         * @return this builder
         */
        public Builder contextBucket(String contextBucket) {
            return queryParam("context_bucket", contextBucket);
        }

        /**
         * Sets the query key {@code category} - read the sampled,
         * upsampled category dataset (weekly-grain estimates). Cannot be
         * combined with {@code modality}, {@code context_bucket} or
         * {@code language_type}.
         *
         * @param category the category
         * @return this builder
         */
        public Builder category(String category) {
            return queryParam("category", category);
        }

        /**
         * Sets the query key {@code language_type} - restrict to
         * natural-language ({@code natural}) or programming-language
         * ({@code programming}) tagged activity; sampled dataset, totals are
         * estimates. Cannot be combined with {@code modality},
         * {@code context_bucket} or {@code category}.
         *
         * @param languageType the language type
         * @return this builder
         */
        public Builder languageType(String languageType) {
            return queryParam("language_type", languageType);
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
        public OpenRouterRankingsDailyRequest build() {
            return new OpenRouterRankingsDailyRequest(this);
        }

        @Override
        public OpenRouterRankingsDailyResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterRankingsDailyResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterRankingsDailyResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
