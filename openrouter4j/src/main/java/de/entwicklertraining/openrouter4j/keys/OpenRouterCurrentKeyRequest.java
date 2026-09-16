package de.entwicklertraining.openrouter4j.keys;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to read the data of the API key making the call:
 * GET https://openrouter.ai/api/v1/key
 *
 * <p>Unlike the other key-management endpoints this works with a normal
 * inference key - it answers "what can the key I am using do?" (limits,
 * usage, whether it is a management key).
 */
public final class OpenRouterCurrentKeyRequest extends OpenRouterRequest<OpenRouterCurrentKeyResponse> {

    private final OpenRouterClient client;

    private OpenRouterCurrentKeyRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
    }

    @Override
    public String getRelativeUrl() {
        return "/key";
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
    public OpenRouterCurrentKeyResponse createResponse(String responseBody) {
        return new OpenRouterCurrentKeyResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterCurrentKeyRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterCurrentKeyRequest> {

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
        public OpenRouterCurrentKeyRequest build() {
            return new OpenRouterCurrentKeyRequest(this);
        }

        @Override
        public OpenRouterCurrentKeyResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterCurrentKeyResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterCurrentKeyResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
