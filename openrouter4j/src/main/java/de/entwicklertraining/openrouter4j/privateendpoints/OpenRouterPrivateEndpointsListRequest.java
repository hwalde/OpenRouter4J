package de.entwicklertraining.openrouter4j.privateendpoints;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to list the organization's private endpoints, including drafts:
 * GET https://openrouter.ai/api/v1/private-endpoints
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * and the private endpoints entitlement for this route. The response is one
 * array of {@code PrivateEndpointSummary} rows with no pagination and no
 * total count - the whole list arrives in one answer.
 *
 * <p>Traps: drafts are listed alongside routable endpoints (check
 * {@code status}); the summary rows carry no pricing, settings or
 * parameters - read those with GET /private-endpoints/{id}.
 */
public final class OpenRouterPrivateEndpointsListRequest
        extends OpenRouterRequest<OpenRouterPrivateEndpointsListResponse<OpenRouterPrivateEndpointsListRequest>> {

    private final OpenRouterClient client;

    private OpenRouterPrivateEndpointsListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
    }

    @Override
    public String getRelativeUrl() {
        return "/private-endpoints";
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
    public OpenRouterPrivateEndpointsListResponse<OpenRouterPrivateEndpointsListRequest> createResponse(
            String responseBody) {
        return new OpenRouterPrivateEndpointsListResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterPrivateEndpointsListRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterPrivateEndpointsListRequest> {

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
        public OpenRouterPrivateEndpointsListRequest build() {
            return new OpenRouterPrivateEndpointsListRequest(this);
        }

        @Override
        public OpenRouterPrivateEndpointsListResponse<OpenRouterPrivateEndpointsListRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPrivateEndpointsListResponse<OpenRouterPrivateEndpointsListRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPrivateEndpointsListResponse<OpenRouterPrivateEndpointsListRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
