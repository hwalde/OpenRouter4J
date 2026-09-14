package de.entwicklertraining.openrouter4j.generation;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * A request to submit structured feedback on one generation:
 * POST https://openrouter.ai/api/v1/generation/feedback
 *
 * <p>The generation id is the response {@code id} of a chat completion (the
 * {@code gen-...} value). OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint.
 */
public final class OpenRouterGenerationFeedbackRequest extends OpenRouterRequest<OpenRouterGenerationFeedbackResponse> {

    private final OpenRouterClient client;
    private final String generationId;
    private final String category;
    private final String comment;

    private OpenRouterGenerationFeedbackRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.generationId = builder.generationId;
        this.category = builder.category;
        this.comment = builder.comment;
    }

    @Override
    public String getRelativeUrl() {
        return "/generation/feedback";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * JSON path: the request body - {@code generation_id} (required),
     * {@code category} (required; the API accepts at least
     * {@code latency}, {@code incoherence}, {@code incorrect_response},
     * {@code formatting}, {@code billing}, {@code api_error}, {@code other};
     * unknown values are forwarded verbatim) and {@code comment} (optional,
     * max 1000 characters, omitted when unset).
     *
     * @return the JSON body of this request
     */
    @Override
    public String getBody() {
        JSONObject root = new JSONObject();
        root.put("generation_id", generationId);
        root.put("category", category);
        if (comment != null) {
            root.put("comment", comment);
        }
        return root.toString();
    }

    /**
     * @return the generation id the feedback refers to
     */
    public String generationId() {
        return generationId;
    }

    /**
     * @return the feedback category
     */
    public String category() {
        return category;
    }

    /**
     * @return the free-text comment, or {@code null} when unset
     */
    public String comment() {
        return comment;
    }

    @Override
    public OpenRouterGenerationFeedbackResponse createResponse(String responseBody) {
        return new OpenRouterGenerationFeedbackResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGenerationFeedbackRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGenerationFeedbackRequest> {

        private final OpenRouterClient client;
        private String generationId;
        private String category;
        private String comment;

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
         * Sets the JSON field {@code generation_id} (required): the generation
         * ({@code gen-...}) to submit feedback on.
         *
         * @param generationId the generation id
         * @return this builder
         */
        public Builder generationId(String generationId) {
            this.generationId = generationId;
            return this;
        }

        /**
         * Sets the JSON field {@code category} (required). Documented values:
         * {@code latency}, {@code incoherence}, {@code incorrect_response},
         * {@code formatting}, {@code billing}, {@code api_error}, {@code other};
         * the API allows unknown values and they are forwarded verbatim.
         *
         * @param category the feedback category
         * @return this builder
         */
        public Builder category(String category) {
            this.category = category;
            return this;
        }

        /**
         * Sets the JSON field {@code comment} (optional): a free-text comment,
         * at most 1000 characters. The key is omitted when unset. Empty-string
         * comments are also omitted.
         *
         * @param comment the comment text
         * @return this builder
         */
        public Builder comment(String comment) {
            this.comment = (comment != null && !comment.isEmpty()) ? comment : null;
            return this;
        }

        @Override
        public OpenRouterGenerationFeedbackRequest build() {
            if (generationId == null || generationId.isEmpty()) {
                throw new IllegalStateException("generationId is required");
            }
            if (category == null || category.isEmpty()) {
                throw new IllegalStateException("category is required");
            }
            return new OpenRouterGenerationFeedbackRequest(this);
        }

        @Override
        public OpenRouterGenerationFeedbackResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterGenerationFeedbackResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterGenerationFeedbackResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
