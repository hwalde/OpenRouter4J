package de.entwicklertraining.openrouter4j.activity;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to read the metrics, dimensions, filter operators and
 * granularities the analytics query engine accepts:
 * GET https://openrouter.ai/api/v1/analytics/meta
 *
 * <p>This is the discovery companion of
 * {@link OpenRouterAnalyticsQueryRequest}: it lists the names that
 * {@code metrics(...)}, {@code dimensions(...)}, {@code filter(field, ...)}
 * and {@code granularity(...)} accept. OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint.
 *
 * <p>Trap carried over from the query endpoint: analytics filters must use the
 * underlying <b>id</b> of enriched dimensions (permaslug for {@code model},
 * workspace UUID for {@code workspace}), not the display label. The meta
 * endpoint makes that mapping queryable - read a dimension's {@code name}
 * here and feed it the id form it expects.
 */
public final class OpenRouterAnalyticsMetaRequest extends OpenRouterRequest<OpenRouterAnalyticsMetaResponse> {

    private final OpenRouterClient client;

    private OpenRouterAnalyticsMetaRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
    }

    @Override
    public String getRelativeUrl() {
        return "/analytics/meta";
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
    public OpenRouterAnalyticsMetaResponse createResponse(String responseBody) {
        return new OpenRouterAnalyticsMetaResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterAnalyticsMetaRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterAnalyticsMetaRequest> {

        private final OpenRouterClient client;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         */
        public Builder(OpenRouterClient client) {
            super(client);
            this.client = client;
        }

        @Override
        public OpenRouterAnalyticsMetaRequest build() {
            return new OpenRouterAnalyticsMetaRequest(this);
        }

        @Override
        public OpenRouterAnalyticsMetaResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterAnalyticsMetaResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterAnalyticsMetaResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
