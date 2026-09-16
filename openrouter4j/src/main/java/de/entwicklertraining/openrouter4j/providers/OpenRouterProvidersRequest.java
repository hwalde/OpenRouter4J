package de.entwicklertraining.openrouter4j.providers;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to list all providers integrated on OpenRouter:
 * GET https://openrouter.ai/api/v1/providers
 *
 * <p>This is the discovery counterpart of the provider-preferences routing
 * options ({@code providerOrder(...)}, {@code providerOnly(...)},
 * {@code providerIgnore(...)}): it returns the slugs those options accept.
 * The endpoint is read-only and works with a normal inference key.
 */
public final class OpenRouterProvidersRequest extends OpenRouterRequest<OpenRouterProvidersResponse> {

    private final OpenRouterClient client;

    private OpenRouterProvidersRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
    }

    @Override
    public String getRelativeUrl() {
        return "/providers";
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
    public OpenRouterProvidersResponse createResponse(String responseBody) {
        return new OpenRouterProvidersResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterProvidersRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterProvidersRequest> {

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
        public OpenRouterProvidersRequest build() {
            return new OpenRouterProvidersRequest(this);
        }

        @Override
        public OpenRouterProvidersResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterProvidersResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterProvidersResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
