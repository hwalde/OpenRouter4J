package de.entwicklertraining.openrouter4j.generation;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to fetch the stored prompt and completion content of one
 * generation:
 * GET https://openrouter.ai/api/v1/generation/content?id={id}
 *
 * <p>The {@code id} is the response {@code id} of a chat completion (the
 * {@code gen-...} value). When the generation failed, the response carries the
 * error that was returned to the client instead of stored content. OpenRouter
 * requires a <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint.
 *
 * <p>OpenRouter needs a few seconds after a completion before its stored
 * content is queryable; querying earlier fails with HTTP 404 (api-base's
 * {@code HTTP_404_NotFoundException}) - retry with backoff.
 */
public final class OpenRouterGenerationContentRequest extends OpenRouterRequest<OpenRouterGenerationContentResponse> {

    private final OpenRouterClient client;
    private final String id;

    private OpenRouterGenerationContentRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
    }

    /**
     * @return the generation id this request queries
     */
    public String id() {
        return id;
    }

    @Override
    public String getRelativeUrl() {
        return "/generation/content?id=" + java.net.URLEncoder.encode(id, java.nio.charset.StandardCharsets.UTF_8);
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
    public OpenRouterGenerationContentResponse createResponse(String responseBody) {
        return new OpenRouterGenerationContentResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGenerationContentRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGenerationContentRequest> {

        private final OpenRouterClient client;
        private final String id;

        /**
         * Creates a builder bound to the given client and generation id.
         *
         * @param client the client used to send the request
         * @param id the generation id ({@code gen-...}); must not be null or empty
         */
        public Builder(OpenRouterClient client, String id) {
            super(client);
            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("id is required");
            }
            this.client = client;
            this.id = id;
        }

        @Override
        public OpenRouterGenerationContentRequest build() {
            return new OpenRouterGenerationContentRequest(this);
        }

        @Override
        public OpenRouterGenerationContentResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterGenerationContentResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterGenerationContentResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
