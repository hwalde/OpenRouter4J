package de.entwicklertraining.openrouter4j.models;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to fetch a single model from the catalog:
 * GET https://openrouter.ai/api/v1/model/{author}/{slug}
 */
public final class OpenRouterModelRequest extends OpenRouterRequest<OpenRouterModelResponse> {

    private final OpenRouterClient client;
    private final String author;
    private final String slug;

    private OpenRouterModelRequest(Builder builder) {
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
        return "/model/" + encode(author) + "/" + encode(slug);
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
    public OpenRouterModelResponse createResponse(String responseBody) {
        return new OpenRouterModelResponse(new JSONObject(responseBody), this);
    }

    private static String encode(String segment) {
        if (segment == null) {
            return "";
        }
        // URLEncoder is form-encoding; path segments must keep "/" out anyway,
        // so encoding it is the correct behaviour for a slugged id.
        return URLEncoder.encode(segment, StandardCharsets.UTF_8);
    }

    /**
     * Starting point for building a {@link OpenRouterModelRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterModelRequest> {

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
        public OpenRouterModelRequest build() {
            return new OpenRouterModelRequest(this);
        }

        @Override
        public OpenRouterModelResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterModelResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterModelResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
