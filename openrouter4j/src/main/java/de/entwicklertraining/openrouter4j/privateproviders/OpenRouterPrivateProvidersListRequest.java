package de.entwicklertraining.openrouter4j.privateproviders;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to list the organization's private providers:
 * GET https://openrouter.ai/api/v1/private-providers
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint and the private endpoints entitlement - a normal
 * inference key is rejected (the schema documents HTTP 401 for missing
 * credentials and 403 for keys without management scope). The route carries
 * no query parameters and the answer is one unwrapped page of providers -
 * no pagination and no total count.
 *
 * <p>Traps: a private provider is the bring-your-own-key container under
 * which private endpoints are created ({@code POST /private-endpoints} with
 * its {@code slug} as {@code provider_slug}); its {@code name} and
 * {@code slug} are fixed at creation and cannot be changed later, and the
 * data policy is fixed by OpenRouter - a private provider serves only your
 * organization, so its endpoints are recorded as retaining no prompts,
 * training on none and publishing none, and pass every data-policy filter
 * your account or guardrails apply. Not yet live-verified (management route;
 * probing mutates account state).
 */
public final class OpenRouterPrivateProvidersListRequest
        extends OpenRouterRequest<OpenRouterPrivateProvidersListResponse<OpenRouterPrivateProvidersListRequest>> {

    private final OpenRouterClient client;

    private OpenRouterPrivateProvidersListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
    }

    @Override
    public String getRelativeUrl() {
        return "/private-providers";
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
    public OpenRouterPrivateProvidersListResponse<OpenRouterPrivateProvidersListRequest> createResponse(
            String responseBody) {
        return new OpenRouterPrivateProvidersListResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterPrivateProvidersListRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterPrivateProvidersListRequest> {

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
        public OpenRouterPrivateProvidersListRequest build() {
            return new OpenRouterPrivateProvidersListRequest(this);
        }

        @Override
        public OpenRouterPrivateProvidersListResponse<OpenRouterPrivateProvidersListRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPrivateProvidersListResponse<OpenRouterPrivateProvidersListRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPrivateProvidersListResponse<OpenRouterPrivateProvidersListRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
