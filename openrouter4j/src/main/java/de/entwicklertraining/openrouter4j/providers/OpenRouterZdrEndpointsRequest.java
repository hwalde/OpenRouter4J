package de.entwicklertraining.openrouter4j.providers;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to preview the impact of zero-data-retention on the available
 * endpoints: GET https://openrouter.ai/api/v1/endpoints/zdr
 *
 * <p>This is the documented way to check, before sending {@code zdr(true)}
 * (zero-data-retention routing, which restricts routing to ZDR endpoints),
 * that enough ZDR endpoints remain for the models in play. The endpoint is
 * read-only and works with a normal inference key (live-verified
 * 2026-09-16, despite the HTTP 403 response the OpenAPI schema lists).
 */
public final class OpenRouterZdrEndpointsRequest extends OpenRouterRequest<OpenRouterZdrEndpointsResponse> {

    private final OpenRouterClient client;

    private OpenRouterZdrEndpointsRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
    }

    @Override
    public String getRelativeUrl() {
        return "/endpoints/zdr";
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
    public OpenRouterZdrEndpointsResponse createResponse(String responseBody) {
        return new OpenRouterZdrEndpointsResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterZdrEndpointsRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterZdrEndpointsRequest> {

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
        public OpenRouterZdrEndpointsRequest build() {
            return new OpenRouterZdrEndpointsRequest(this);
        }

        @Override
        public OpenRouterZdrEndpointsResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterZdrEndpointsResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterZdrEndpointsResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
