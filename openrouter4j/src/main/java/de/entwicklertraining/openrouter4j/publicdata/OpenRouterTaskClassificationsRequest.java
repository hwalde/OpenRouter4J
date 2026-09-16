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
 * A request to read the task-classification market share:
 * GET https://openrouter.ai/api/v1/classifications/task
 *
 * <p>Breaks OpenRouter traffic down by task classification (code generation,
 * web search, summarization, ...) over a trailing time window; shares are
 * fractions between 0 and 1 of classified sampled requests/tokens, the
 * unclassified {@code other} bucket is excluded. Works with any valid
 * OpenRouter API key; rate-limited. Data is licensed under CC BY 4.0 -
 * republish with attribution to OpenRouter.
 */
public final class OpenRouterTaskClassificationsRequest
        extends OpenRouterRequest<OpenRouterTaskClassificationsResponse> {

    private final OpenRouterClient client;
    private final Map<String, String> queryParams;

    private OpenRouterTaskClassificationsRequest(Builder builder) {
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
        return appendQuery("/classifications/task");
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
    public OpenRouterTaskClassificationsResponse createResponse(String responseBody) {
        return new OpenRouterTaskClassificationsResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterTaskClassificationsRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterTaskClassificationsRequest> {

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
         * Sets the query key {@code window} - the trailing time window.
         * Currently only {@code 7d} (trailing 7 days) is supported.
         *
         * @param window the window
         * @return this builder
         */
        public Builder window(String window) {
            return queryParam("window", window);
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
        public OpenRouterTaskClassificationsRequest build() {
            return new OpenRouterTaskClassificationsRequest(this);
        }

        @Override
        public OpenRouterTaskClassificationsResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterTaskClassificationsResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterTaskClassificationsResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
