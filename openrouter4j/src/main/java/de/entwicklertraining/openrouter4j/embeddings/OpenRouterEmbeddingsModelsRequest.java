package de.entwicklertraining.openrouter4j.embeddings;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to list the embedding models of the OpenRouter catalog:
 * GET https://openrouter.ai/api/v1/embeddings/models
 *
 * <p>The response has the same {@code data[]} shape as the general model
 * catalog, so the accessors are inherited from
 * {@link de.entwicklertraining.openrouter4j.models.OpenRouterModelsListResponse}
 * and the entries are {@link de.entwicklertraining.openrouter4j.models.OpenRouterModel}
 * views.
 */
public final class OpenRouterEmbeddingsModelsRequest
        extends OpenRouterRequest<OpenRouterEmbeddingsModelsResponse> {

    private final OpenRouterClient client;
    private final Integer offset;
    private final Integer limit;

    private OpenRouterEmbeddingsModelsRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.offset = builder.offset;
        this.limit = builder.limit;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder url = new StringBuilder("/embeddings/models");
        String separator = "?";
        if (offset != null) {
            url.append(separator)
               .append("offset=").append(URLEncoder.encode(String.valueOf(offset), StandardCharsets.UTF_8));
            separator = "&";
        }
        if (limit != null) {
            url.append(separator)
               .append("limit=").append(URLEncoder.encode(String.valueOf(limit), StandardCharsets.UTF_8));
        }
        return url.toString();
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
    public OpenRouterEmbeddingsModelsResponse createResponse(String responseBody) {
        return new OpenRouterEmbeddingsModelsResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterEmbeddingsModelsRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterEmbeddingsModelsRequest> {

        private final OpenRouterClient client;
        private Integer offset;
        private Integer limit;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         */
        public Builder(OpenRouterClient client) {
            super(client);
            this.client = client;
        }

        /**
         * Sets the query key {@code offset} - number of records to skip for
         * pagination. When both offset and limit are omitted, the full list
         * is returned.
         *
         * @param offset number of records to skip
         * @return this builder
         */
        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

        /**
         * Sets the query key {@code limit} - maximum number of records to
         * return (API default 500, max 1000). When both offset and limit are
         * omitted, the full list is returned.
         *
         * @param limit maximum number of records
         * @return this builder
         */
        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        @Override
        public OpenRouterEmbeddingsModelsRequest build() {
            return new OpenRouterEmbeddingsModelsRequest(this);
        }

        @Override
        public OpenRouterEmbeddingsModelsResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterEmbeddingsModelsResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterEmbeddingsModelsResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
