package de.entwicklertraining.openrouter4j.presets;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Creates or updates an OpenRouter preset from a chat-completions request
 * body:
 * POST https://openrouter.ai/api/v1/presets/{slug}/chat/completions
 *
 * <p><b>Create-not-infer semantics.</b> This is NOT an inference route: the
 * body (the full {@code ChatRequest} schema) is stored as a new version of
 * the preset and the designated version moves to it; nothing is generated.
 * A management key is required - a normal inference key is rejected with an
 * authorization error (the schema documents HTTP 403; observed rejection
 * codes vary). The {@code messages} and {@code stream} fields are silently
 * ignored when storing.
 *
 * <p>The body is reused verbatim from an
 * {@link OpenRouterChatCompletionRequest} built with the ordinary
 * chat-completions builder - no duplicated typing. Trap: the chat builder's
 * default model is always emitted, so the stored preset gets that model
 * unless {@code .model(...)} is set explicitly.
 */
public final class OpenRouterPresetUpsertFromChatRequest extends OpenRouterRequest<OpenRouterPresetUpsertResponse<OpenRouterPresetUpsertFromChatRequest>> {

    private final OpenRouterClient client;
    private final String slug;
    private final OpenRouterChatCompletionRequest bodyRequest;

    private OpenRouterPresetUpsertFromChatRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.slug = builder.slug;
        this.bodyRequest = builder.bodyRequest;
    }

    /** @return the URL-encoded slug path segment */
    public String slug() {
        return URLEncoder.encode(slug, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/presets/" + URLEncoder.encode(slug, StandardCharsets.UTF_8) + "/chat/completions";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * @return the verbatim body of the wrapped chat-completions request
     */
    @Override
    public String getBody() {
        return bodyRequest == null ? null : bodyRequest.getBody();
    }

    @Override
    public OpenRouterPresetUpsertResponse<OpenRouterPresetUpsertFromChatRequest> createResponse(String responseBody) {
        return new OpenRouterPresetUpsertResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterPresetUpsertFromChatRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterPresetUpsertFromChatRequest> {

        private final OpenRouterClient client;
        private final String slug;
        private OpenRouterChatCompletionRequest bodyRequest;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param slug the preset slug to create or update
         */
        public Builder(OpenRouterClient client, String slug) {
            super(client);
            this.client = client;
            this.slug = slug;
        }

        /**
         * Sets the request body - a chat-completions request built with the
         * ordinary chat builder, whose body is sent verbatim.
         *
         * @param request the chat-completions request supplying the body
         * @return this builder
         */
        public Builder body(OpenRouterChatCompletionRequest request) {
            this.bodyRequest = request;
            return this;
        }

        @Override
        public OpenRouterPresetUpsertFromChatRequest build() {
            if (slug == null || slug.isEmpty()) {
                throw new IllegalStateException("slug is required to upsert a preset");
            }
            if (bodyRequest == null) {
                throw new IllegalStateException(
                        "body is required: supply an OpenRouterChatCompletionRequest via body(...)");
            }
            return new OpenRouterPresetUpsertFromChatRequest(this);
        }

        @Override
        public OpenRouterPresetUpsertResponse<OpenRouterPresetUpsertFromChatRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPresetUpsertResponse<OpenRouterPresetUpsertFromChatRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPresetUpsertResponse<OpenRouterPresetUpsertFromChatRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
