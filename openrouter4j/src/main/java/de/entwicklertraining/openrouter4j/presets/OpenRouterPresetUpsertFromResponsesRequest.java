package de.entwicklertraining.openrouter4j.presets;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.responses.OpenRouterResponsesRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Creates or updates an OpenRouter preset from a Responses-API request body:
 * POST https://openrouter.ai/api/v1/presets/{slug}/responses
 *
 * <p><b>Create-not-infer semantics.</b> This is NOT an inference route: the
 * body (the full {@code ResponsesRequest} schema) is stored as a new version
 * of the preset and the designated version moves to it; nothing is
 * generated. A management key is required - a normal inference key is
 * rejected with an authorization error (the schema documents HTTP 403;
 * observed rejection codes vary). The {@code input} and {@code stream}
 * fields are silently ignored when storing.
 *
 * <p>The body is reused verbatim from an {@link OpenRouterResponsesRequest}
 * built with the ordinary responses builder - no duplicated typing.
 */
public final class OpenRouterPresetUpsertFromResponsesRequest extends OpenRouterRequest<OpenRouterPresetUpsertResponse<OpenRouterPresetUpsertFromResponsesRequest>> {

    private final OpenRouterClient client;
    private final String slug;
    private final OpenRouterResponsesRequest bodyRequest;

    private OpenRouterPresetUpsertFromResponsesRequest(Builder builder) {
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
        return "/presets/" + URLEncoder.encode(slug, StandardCharsets.UTF_8) + "/responses";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * @return the verbatim body of the wrapped responses request
     */
    @Override
    public String getBody() {
        return bodyRequest == null ? null : bodyRequest.getBody();
    }

    @Override
    public OpenRouterPresetUpsertResponse<OpenRouterPresetUpsertFromResponsesRequest> createResponse(String responseBody) {
        return new OpenRouterPresetUpsertResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterPresetUpsertFromResponsesRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterPresetUpsertFromResponsesRequest> {

        private final OpenRouterClient client;
        private final String slug;
        private OpenRouterResponsesRequest bodyRequest;

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
         * Sets the request body - a Responses-API request built with the
         * ordinary responses builder, whose body is sent verbatim.
         *
         * @param request the responses request supplying the body
         * @return this builder
         */
        public Builder body(OpenRouterResponsesRequest request) {
            this.bodyRequest = request;
            return this;
        }

        @Override
        public OpenRouterPresetUpsertFromResponsesRequest build() {
            if (slug == null || slug.isEmpty()) {
                throw new IllegalStateException("slug is required to upsert a preset");
            }
            if (bodyRequest == null) {
                throw new IllegalStateException(
                        "body is required: supply an OpenRouterResponsesRequest via body(...)");
            }
            return new OpenRouterPresetUpsertFromResponsesRequest(this);
        }

        @Override
        public OpenRouterPresetUpsertResponse<OpenRouterPresetUpsertFromResponsesRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPresetUpsertResponse<OpenRouterPresetUpsertFromResponsesRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPresetUpsertResponse<OpenRouterPresetUpsertFromResponsesRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
