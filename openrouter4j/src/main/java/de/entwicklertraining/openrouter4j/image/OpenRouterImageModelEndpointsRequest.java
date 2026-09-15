package de.entwicklertraining.openrouter4j.image;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * Lists the serving endpoints of one image generation model:
 * GET https://openrouter.ai/api/v1/images/models/{author}/{slug}/endpoints
 *
 * <p>This is the definitive per-endpoint record behind the coarse
 * {@code supported_parameters} union of the discovery listing: each endpoint
 * carries its own supported parameter set, its pricing lines and whether it
 * supports native SSE streaming.
 */
public final class OpenRouterImageModelEndpointsRequest
        extends OpenRouterRequest<OpenRouterImageModelEndpointsResponse> {

    private final OpenRouterClient client;
    private final String author;
    private final String slug;

    private OpenRouterImageModelEndpointsRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.author = builder.author;
        this.slug = builder.slug;
    }

    /** @return the model author/organization part of the URL */
    public String author() {
        return author;
    }

    /** @return the model slug part of the URL */
    public String slug() {
        return slug;
    }

    @Override
    public String getRelativeUrl() {
        return "/images/models/" + author + "/" + slug + "/endpoints";
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
    public OpenRouterImageModelEndpointsResponse createResponse(String responseBody) {
        return new OpenRouterImageModelEndpointsResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a
     * {@link OpenRouterImageModelEndpointsRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterImageModelEndpointsRequest> {

        private final OpenRouterClient client;
        private final String author;
        private final String slug;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param author the model author/organization
         * @param slug the model slug
         */
        public Builder(OpenRouterClient client, String author, String slug) {
            super(client);
            this.client = client;
            this.author = author;
            this.slug = slug;
        }

        @Override
        public OpenRouterImageModelEndpointsRequest build() {
            if (author == null || author.isEmpty() || slug == null || slug.isEmpty()) {
                throw new IllegalStateException(
                        "author and slug are required for an image model endpoints request");
            }
            return new OpenRouterImageModelEndpointsRequest(this);
        }

        @Override
        public OpenRouterImageModelEndpointsResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterImageModelEndpointsResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterImageModelEndpointsResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
