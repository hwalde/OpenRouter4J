package de.entwicklertraining.openrouter4j.privateendpoints;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to read one private endpoint with its upstream configuration and
 * current pricing:
 * GET https://openrouter.ai/api/v1/private-endpoints/{id}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * and the private endpoints entitlement for this route. The answer is the
 * full {@code PrivateEndpoint} shape (settings and {@code tested_parameters}
 * included) - this is also the read-back for {@code declared_parameters} and
 * {@code tested_parameters}, which the create response does not echo.
 */
public final class OpenRouterPrivateEndpointGetRequest
        extends OpenRouterRequest<OpenRouterPrivateEndpointResponse<OpenRouterPrivateEndpointGetRequest>> {

    private final OpenRouterClient client;
    private final String id;

    private OpenRouterPrivateEndpointGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/private-endpoints/" + URLEncoder.encode(id, StandardCharsets.UTF_8);
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
    public OpenRouterPrivateEndpointResponse<OpenRouterPrivateEndpointGetRequest> createResponse(
            String responseBody) {
        return new OpenRouterPrivateEndpointResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterPrivateEndpointGetRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterPrivateEndpointGetRequest> {

        private final OpenRouterClient client;
        private final String id;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param id the id (UUID) of the private endpoint (validated loudly;
         *        URL-encoded into the path)
         */
        public Builder(OpenRouterClient client, String id) {
            super(client);
            this.client = client;
            OpenRouterPrivateEndpointRules.requireNonBlank(id, "id");
            this.id = id;
        }

        @Override
        public OpenRouterPrivateEndpointGetRequest build() {
            return new OpenRouterPrivateEndpointGetRequest(this);
        }

        @Override
        public OpenRouterPrivateEndpointResponse<OpenRouterPrivateEndpointGetRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPrivateEndpointResponse<OpenRouterPrivateEndpointGetRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPrivateEndpointResponse<OpenRouterPrivateEndpointGetRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
