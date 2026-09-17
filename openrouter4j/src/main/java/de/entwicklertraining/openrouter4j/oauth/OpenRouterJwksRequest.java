package de.entwicklertraining.openrouter4j.oauth;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to read the public signing keys of OpenRouter access tokens:
 * GET https://openrouter.ai/api/v1/oauth/jwks
 *
 * <p>The response is an RFC 7517 JWK Set - the counterpart needed to verify
 * the access tokens the OAuth exchange endpoints hand out (notably the
 * workload-identity federation, which returns a short-lived access token
 * whose signature can only be checked against these keys).
 *
 * <p>No request body, no special authentication beyond the normal client
 * transport.
 */
public final class OpenRouterJwksRequest extends OpenRouterRequest<OpenRouterJwksResponse> {

    private final OpenRouterClient client;

    private OpenRouterJwksRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
    }

    @Override
    public String getRelativeUrl() {
        return "/oauth/jwks";
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
    public OpenRouterJwksResponse createResponse(String responseBody) {
        return new OpenRouterJwksResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterJwksRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterJwksRequest> {

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
        public OpenRouterJwksRequest build() {
            return new OpenRouterJwksRequest(this);
        }

        @Override
        public OpenRouterJwksResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterJwksResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterJwksResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
