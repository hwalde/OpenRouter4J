package de.entwicklertraining.openrouter4j.privateendpoints;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to make a validated draft private endpoint routable:
 * POST https://openrouter.ai/api/v1/private-endpoints/{id}/activate
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * and the private endpoints entitlement for this route. The answer is 200
 * with the {@code ManagedPrivateEndpoint} now in status {@code active}.
 *
 * <p>Traps: the draft must have a passing validation first -
 * {@code POST /private-endpoints/{id}/validate}. The route answers 409 when
 * the endpoint was never validated, when the validation is stale (the
 * upstream configuration or the declarations changed after the run) or when
 * it is already active.
 */
public final class OpenRouterPrivateEndpointActivateRequest
        extends OpenRouterRequest<OpenRouterManagedPrivateEndpointResponse<OpenRouterPrivateEndpointActivateRequest>> {

    private final OpenRouterClient client;
    private final String id;

    private OpenRouterPrivateEndpointActivateRequest(Builder builder) {
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
        return "/private-endpoints/" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "/activate";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * The route takes no request body.
     *
     * @return always {@code null}
     */
    @Override
    public String getBody() {
        return null;
    }

    @Override
    public OpenRouterManagedPrivateEndpointResponse<OpenRouterPrivateEndpointActivateRequest> createResponse(
            String responseBody) {
        return new OpenRouterManagedPrivateEndpointResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterPrivateEndpointActivateRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterPrivateEndpointActivateRequest> {

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
            OpenRouterPrivateEndpointRules.requirePathSegment(id, "id");
            this.id = id;
        }

        @Override
        public OpenRouterPrivateEndpointActivateRequest build() {
            return new OpenRouterPrivateEndpointActivateRequest(this);
        }

        @Override
        public OpenRouterManagedPrivateEndpointResponse<OpenRouterPrivateEndpointActivateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterManagedPrivateEndpointResponse<OpenRouterPrivateEndpointActivateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterManagedPrivateEndpointResponse<OpenRouterPrivateEndpointActivateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
