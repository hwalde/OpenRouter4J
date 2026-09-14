package de.entwicklertraining.openrouter4j.credits;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * A request to read the credit balance of the authenticated account:
 * GET https://openrouter.ai/api/v1/credits
 *
 * <p>Returns the total credits purchased and the total credits used, both in
 * USD. OpenRouter requires a <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint; a normal inference key is rejected with HTTP 403
 * (surfaced as api-base's {@code HTTP_403_PermissionDeniedException}).
 */
public final class OpenRouterCreditsRequest extends OpenRouterRequest<OpenRouterCreditsResponse> {

    private final OpenRouterClient client;

    private OpenRouterCreditsRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
    }

    @Override
    public String getRelativeUrl() {
        return "/credits";
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
    public OpenRouterCreditsResponse createResponse(String responseBody) {
        return new OpenRouterCreditsResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterCreditsRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterCreditsRequest> {

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
        public OpenRouterCreditsRequest build() {
            return new OpenRouterCreditsRequest(this);
        }

        @Override
        public OpenRouterCreditsResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterCreditsResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterCreditsResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
