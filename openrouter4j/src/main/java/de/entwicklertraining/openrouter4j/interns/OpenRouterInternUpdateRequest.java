package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to update an intern:
 * PATCH https://openrouter.ai/api/v1/interns/{internId}
 *
 * <p>Body: {@code name}, {@code description}, {@code instructions} and
 * {@code model}, all optional - omitted fields stay unchanged. Every field
 * is emitted only when explicitly configured.
 *
 * <p>Trap: the {@code model} change applies to what the intern runs; the
 * chat endpoint's {@code model} field (accepted for OpenAI compatibility)
 * never changes it.
 */
public final class OpenRouterInternUpdateRequest
        extends OpenRouterRequest<OpenRouterInternResponse<OpenRouterInternUpdateRequest>> {

    private final OpenRouterClient client;
    private final String internId;
    private final String name;
    private final String description;
    private final String instructions;
    private final String model;

    private OpenRouterInternUpdateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.internId = builder.internId;
        this.name = builder.name;
        this.description = builder.description;
        this.instructions = builder.instructions;
        this.model = builder.model;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String internId() {
        return URLEncoder.encode(internId, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/interns/" + URLEncoder.encode(internId, StandardCharsets.UTF_8);
    }

    @Override
    public String getHttpMethod() {
        return "PATCH";
    }

    /**
     * The JSON body: the configured fields, each present only when set.
     *
     * @return the JSON body string
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        if (name != null) {
            body.put("name", name);
        }
        if (description != null) {
            body.put("description", description);
        }
        if (instructions != null) {
            body.put("instructions", instructions);
        }
        if (model != null) {
            body.put("model", model);
        }
        return body.toString();
    }

    @Override
    public OpenRouterInternResponse<OpenRouterInternUpdateRequest> createResponse(
            String responseBody) {
        return new OpenRouterInternResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterInternUpdateRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterInternUpdateRequest> {

        private final OpenRouterClient client;
        private final String internId;
        private String name;
        private String description;
        private String instructions;
        private String model;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param internId the id (UUID) of the intern
         */
        public Builder(OpenRouterClient client, String internId) {
            super(client);
            this.client = client;
            this.internId = internId;
        }

        /**
         * Sets the JSON field {@code name} (2-17 chars, pattern
         * {@code ^[a-z][a-z0-9]*(-[a-z0-9]+)*$}, unique per creator within
         * the workspace; validated loudly). Omit to keep the current name.
         *
         * @param name the new name
         * @return this builder
         */
        public Builder name(String name) {
            if (name != null
                    && (name.length() < 2 || name.length() > 17
                            || !name.matches("^[a-z][a-z0-9]*(-[a-z0-9]+)*$"))) {
                throw new IllegalArgumentException(
                        "intern name must be 2 to 17 characters matching"
                                + " ^[a-z][a-z0-9]*(-[a-z0-9]+)*$: " + name);
            }
            this.name = name;
            return this;
        }

        /**
         * Sets the JSON field {@code description} (at most 2,000 characters,
         * validated loudly). Omit to keep the current description.
         *
         * @param description the new description
         * @return this builder
         */
        public Builder description(String description) {
            if (description != null && description.length() > 2_000) {
                throw new IllegalArgumentException(
                        "description must be at most 2000 characters");
            }
            this.description = description;
            return this;
        }

        /**
         * Sets the JSON field {@code instructions} (at most 100,000
         * characters, validated loudly). Omit to keep the current
         * instructions.
         *
         * @param instructions the new standing instructions
         * @return this builder
         */
        public Builder instructions(String instructions) {
            if (instructions != null && instructions.length() > 100_000) {
                throw new IllegalArgumentException(
                        "instructions must be at most 100000 characters");
            }
            this.instructions = instructions;
            return this;
        }

        /**
         * Sets the JSON field {@code model} - the OpenRouter model slug the
         * intern runs. Omit to keep the current model.
         *
         * @param model the model slug
         * @return this builder
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        @Override
        public OpenRouterInternUpdateRequest build() {
            return new OpenRouterInternUpdateRequest(this);
        }

        @Override
        public OpenRouterInternResponse<OpenRouterInternUpdateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterInternResponse<OpenRouterInternUpdateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterInternResponse<OpenRouterInternUpdateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
