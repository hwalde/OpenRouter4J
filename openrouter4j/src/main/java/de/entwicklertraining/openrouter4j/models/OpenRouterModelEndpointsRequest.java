package de.entwicklertraining.openrouter4j.models;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to list the serving endpoints of one model:
 * GET https://openrouter.ai/api/v1/models/{author}/{slug}/endpoints
 *
 * <p>This endpoint gives the most complete per-provider picture of a model
 * (provider names, quantizations, tags and status live in the endpoint
 * objects; see {@link OpenRouterModelEndpoint#json()}).
 */
public final class OpenRouterModelEndpointsRequest extends OpenRouterRequest<OpenRouterModelEndpointsResponse> {

    private final OpenRouterClient client;
    private final String author;
    private final String slug;

    private OpenRouterModelEndpointsRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.author = builder.author;
        this.slug = builder.slug;
    }

    /**
     * @return the author segment of the path
     */
    public String author() {
        return author;
    }

    /**
     * @return the slug segment of the path
     */
    public String slug() {
        return slug;
    }

    @Override
    public String getRelativeUrl() {
        return "/models/" + encode(author) + "/" + encode(slug) + "/endpoints";
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
    public OpenRouterModelEndpointsResponse createResponse(String responseBody) {
        return new OpenRouterModelEndpointsResponse(new JSONObject(responseBody), this);
    }

    private static String encode(String segment) {
        if (segment == null) {
            return "";
        }
        return URLEncoder.encode(segment, StandardCharsets.UTF_8);
    }

    /**
     * Starting point for building a {@link OpenRouterModelEndpointsRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterModelEndpointsRequest> {

        private final OpenRouterClient client;
        private final String author;
        private final String slug;

        /**
         * Creates a builder bound to the given client and model id.
         *
         * @param client the client used to send the request
         * @param author the author part of the model id (before the slash)
         * @param slug the slug part of the model id (after the slash)
         */
        public Builder(OpenRouterClient client, String author, String slug) {
            super(client);
            if (author == null || author.isEmpty()) {
                throw new IllegalArgumentException("author is required");
            }
            if (slug == null || slug.isEmpty()) {
                throw new IllegalArgumentException("slug is required");
            }
            this.client = client;
            this.author = author;
            this.slug = slug;
        }

        @Override
        public OpenRouterModelEndpointsRequest build() {
            return new OpenRouterModelEndpointsRequest(this);
        }

        @Override
        public OpenRouterModelEndpointsResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterModelEndpointsResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterModelEndpointsResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
