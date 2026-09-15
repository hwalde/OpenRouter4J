package de.entwicklertraining.openrouter4j.image;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * Lists the image generation models of the catalog:
 * GET https://openrouter.ai/api/v1/images/models
 *
 * <p>This is the discovery aid for POST /images: each entry carries the
 * architecture (input/output modalities), the coarse union of supported
 * parameters, whether any endpoint supports native SSE streaming and the
 * relative URL of the per-endpoint listing.
 */
public final class OpenRouterImageModelsRequest
        extends OpenRouterRequest<OpenRouterImageModelsResponse> {

    private final OpenRouterClient client;

    private OpenRouterImageModelsRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
    }

    @Override
    public String getRelativeUrl() {
        return "/images/models";
    }

    @Override
    public String getHttpMethod() {
        return "GET";
    }

    /**
     * JSON path: none - a GET request with no query parameters.
     *
     * @return an empty body
     */
    @Override
    public String getBody() {
        return "";
    }

    @Override
    public OpenRouterImageModelsResponse createResponse(String responseBody) {
        return new OpenRouterImageModelsResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterImageModelsRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterImageModelsRequest> {

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
        public OpenRouterImageModelsRequest build() {
            return new OpenRouterImageModelsRequest(this);
        }

        @Override
        public OpenRouterImageModelsResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterImageModelsResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterImageModelsResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
