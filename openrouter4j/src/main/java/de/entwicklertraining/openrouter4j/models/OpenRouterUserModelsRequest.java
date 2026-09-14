package de.entwicklertraining.openrouter4j.models;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to list the models the authenticated account is allowed to use:
 * GET https://openrouter.ai/api/v1/models/user
 *
 * <p>Returns the same catalog entry shape as GET /models; the response type is
 * shared with the full catalog listing.
 */
public final class OpenRouterUserModelsRequest extends OpenRouterRequest<OpenRouterModelsListResponse<OpenRouterUserModelsRequest>> {

    private final OpenRouterClient client;
    private final Integer offset;
    private final Integer limit;
    private final String outputModalities;

    private OpenRouterUserModelsRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.offset = builder.offset;
        this.limit = builder.limit;
        this.outputModalities = builder.outputModalities;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/models/user");
        boolean first = true;
        if (offset != null) {
            sb.append("?offset=").append(offset);
            first = false;
        }
        if (limit != null) {
            sb.append(first ? '?' : '&').append("limit=").append(limit);
            first = false;
        }
        if (outputModalities != null) {
            sb.append(first ? '?' : '&').append("output_modalities=").append(outputModalities);
        }
        return sb.toString();
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
    public OpenRouterModelsListResponse<OpenRouterUserModelsRequest> createResponse(String responseBody) {
        return new OpenRouterModelsListResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterUserModelsRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterUserModelsRequest> {

        private final OpenRouterClient client;
        private Integer offset;
        private Integer limit;
        private String outputModalities;

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

        /**
         * Sets the query key {@code output_modalities} - filter by output
         * modality, comma-separated ({@code text}, {@code image}, {@code audio},
         * {@code video}, {@code rerank}, ...).
         *
         * @param modalities the modalities
         * @return this builder
         */
        public Builder outputModalities(String... modalities) {
            if (modalities != null && modalities.length > 0) {
                this.outputModalities = String.join(",", modalities);
            }
            return this;
        }

        @Override
        public OpenRouterUserModelsRequest build() {
            return new OpenRouterUserModelsRequest(this);
        }

        @Override
        public OpenRouterModelsListResponse<OpenRouterUserModelsRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterModelsListResponse<OpenRouterUserModelsRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterModelsListResponse<OpenRouterUserModelsRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
