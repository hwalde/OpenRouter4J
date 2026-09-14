package de.entwicklertraining.openrouter4j.generation;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to fetch the request/usage metadata of one generation:
 * GET https://openrouter.ai/api/v1/generation?id={id}
 *
 * <p>The {@code id} is the response {@code id} of a chat completion (the
 * {@code gen-...} value, not the per-request {@code request_id}). OpenRouter
 * requires a <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint.
 *
 * <p>This is the post-mortem surface: which provider served the request, the
 * native finish reason, and the token/cost breakdown of a generation that
 * already happened.
 */
public final class OpenRouterGenerationRequest extends OpenRouterRequest<OpenRouterGenerationResponse> {

    private final OpenRouterClient client;
    private final String id;

    private OpenRouterGenerationRequest(Builder builder) {
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
        return "/generation?id=" + java.net.URLEncoder.encode(id, java.nio.charset.StandardCharsets.UTF_8);
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
    public OpenRouterGenerationResponse createResponse(String responseBody) {
        return new OpenRouterGenerationResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGenerationRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGenerationRequest> {

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
        public OpenRouterGenerationRequest build() {
            return new OpenRouterGenerationRequest(this);
        }

        @Override
        public OpenRouterGenerationResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterGenerationResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterGenerationResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
