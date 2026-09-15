package de.entwicklertraining.openrouter4j.video;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * Lists the video generation models of the catalog:
 * GET https://openrouter.ai/api/v1/videos/models
 *
 * <p>This is the discovery aid for POST /videos: each entry carries the
 * supported resolutions, aspect ratios, sizes, durations, frame image types,
 * whether it generates audio and whether it supports seeding.
 */
public final class OpenRouterVideoModelsRequest
        extends OpenRouterRequest<OpenRouterVideoModelsResponse> {

    private final OpenRouterClient client;

    private OpenRouterVideoModelsRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
    }

    @Override
    public String getRelativeUrl() {
        return "/videos/models";
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
    public OpenRouterVideoModelsResponse createResponse(String responseBody) {
        return new OpenRouterVideoModelsResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterVideoModelsRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterVideoModelsRequest> {

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
        public OpenRouterVideoModelsRequest build() {
            return new OpenRouterVideoModelsRequest(this);
        }

        @Override
        public OpenRouterVideoModelsResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterVideoModelsResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterVideoModelsResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
