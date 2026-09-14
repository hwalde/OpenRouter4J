package de.entwicklertraining.openrouter4j.models;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to count the models in the OpenRouter catalog:
 * GET https://openrouter.ai/api/v1/models/count
 */
public final class OpenRouterModelsCountRequest extends OpenRouterRequest<OpenRouterModelsCountResponse> {

    private final OpenRouterClient client;
    private final String outputModalities;

    private OpenRouterModelsCountRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.outputModalities = builder.outputModalities;
    }

    @Override
    public String getRelativeUrl() {
        return outputModalities != null
                ? "/models/count?output_modalities=" + outputModalities
                : "/models/count";
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
    public OpenRouterModelsCountResponse createResponse(String responseBody) {
        return new OpenRouterModelsCountResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterModelsCountRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterModelsCountRequest> {

        private final OpenRouterClient client;
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
         * Sets the query key {@code output_modalities} - count only models
         * with these output modalities (comma-separated: {@code text},
         * {@code image}, {@code audio}, {@code video}, {@code rerank}, ...).
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
        public OpenRouterModelsCountRequest build() {
            return new OpenRouterModelsCountRequest(this);
        }

        @Override
        public OpenRouterModelsCountResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterModelsCountResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterModelsCountResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
