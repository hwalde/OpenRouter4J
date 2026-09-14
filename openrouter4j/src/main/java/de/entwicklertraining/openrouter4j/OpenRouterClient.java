package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.api.base.ApiClientSettings;
import de.entwicklertraining.api.base.ApiHttpConfiguration;
import de.entwicklertraining.openrouter4j.activity.OpenRouterActivityRequest;
import de.entwicklertraining.openrouter4j.activity.OpenRouterAnalyticsQueryRequest;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest;
import de.entwicklertraining.openrouter4j.credits.OpenRouterCreditsRequest;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsModelsRequest;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsRequest;
import de.entwicklertraining.openrouter4j.generation.OpenRouterGenerationContentRequest;
import de.entwicklertraining.openrouter4j.generation.OpenRouterGenerationFeedbackRequest;
import de.entwicklertraining.openrouter4j.generation.OpenRouterGenerationRequest;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelEndpointsRequest;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelRequest;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelsCountRequest;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelsListRequest;
import de.entwicklertraining.openrouter4j.models.OpenRouterUserModelsRequest;
import de.entwicklertraining.openrouter4j.oauth.OpenRouterAuthorizationCodeExchangeRequest;
import de.entwicklertraining.openrouter4j.oauth.OpenRouterCreateAuthorizationCodeRequest;
import de.entwicklertraining.openrouter4j.rerank.OpenRouterRerankRequest;

// Import exception classes
import static de.entwicklertraining.api.base.ApiClient.HTTP_400_RequestRejectedException;
import static de.entwicklertraining.api.base.ApiClient.HTTP_403_PermissionDeniedException;
import static de.entwicklertraining.api.base.ApiClient.HTTP_404_NotFoundException;
import static de.entwicklertraining.api.base.ApiClient.HTTP_429_RateLimitOrQuotaException;
import static de.entwicklertraining.api.base.ApiClient.HTTP_500_ServerErrorException;
import static de.entwicklertraining.api.base.ApiClient.HTTP_503_ServerUnavailableException;
import static de.entwicklertraining.api.base.ApiClient.HTTP_504_ServerTimeoutException;

/**
 * Ein Client für die OpenRouter-API mit integrierter Rate-Limit-Prüfung.
 * OpenRouter verwendet Bearer Token Authentication im Authorization Header.
 *
 * <p>The client automatically reads the API key from the OPENROUTER_API_KEY environment variable
 * if not explicitly provided via {@link ApiHttpConfiguration}.
 */
public final class OpenRouterClient extends ApiClient {

    private static final String DEFAULT_BASE_URL = "https://openrouter.ai/api/v1";

    private volatile OpenRouterAppAttribution appAttribution;

    /**
     * Creates a new OpenRouterClient with default settings.
     * The API key is read from the OPENROUTER_API_KEY environment variable.
     */
    public OpenRouterClient() {
        this(ApiClientSettings.builder().build(), null, DEFAULT_BASE_URL);
    }

    /**
     * Creates a new OpenRouterClient with custom settings.
     * The API key is read from the OPENROUTER_API_KEY environment variable.
     *
     * @param settings Client settings for retry behavior and timeouts
     */
    public OpenRouterClient(ApiClientSettings settings) {
        this(settings, null, DEFAULT_BASE_URL);
    }

    /**
     * Creates a new OpenRouterClient with custom settings and HTTP configuration.
     *
     * @param settings Client settings for retry behavior and timeouts
     * @param httpConfig HTTP configuration including authentication headers
     */
    public OpenRouterClient(ApiClientSettings settings, ApiHttpConfiguration httpConfig) {
        this(settings, httpConfig, DEFAULT_BASE_URL);
    }

    /**
     * Creates a new OpenRouterClient with custom settings, HTTP configuration, and base URL.
     *
     * @param settings Client settings for retry behavior and timeouts
     * @param httpConfig HTTP configuration including authentication headers (can be null)
     * @param customBaseUrl Custom base URL for the API
     */
    public OpenRouterClient(ApiClientSettings settings, ApiHttpConfiguration httpConfig, String customBaseUrl) {
        super(settings, buildHttpConfig(httpConfig));

        setBaseUrl(customBaseUrl);

        // Register OpenRouter-specific HTTP status code exceptions
        registerStatusCodeException(400, HTTP_400_RequestRejectedException.class, "HTTP 400 (Bad Request)", false);
        registerStatusCodeException(403, HTTP_403_PermissionDeniedException.class, "HTTP 403 (Forbidden)", false);
        registerStatusCodeException(404, HTTP_404_NotFoundException.class, "HTTP 404 (Not Found)", false);
        registerStatusCodeException(429, HTTP_429_RateLimitOrQuotaException.class, "HTTP 429 (Rate Limited)", true);
        registerStatusCodeException(500, HTTP_500_ServerErrorException.class, "HTTP 500 (Internal Server Error)", true);
        registerStatusCodeException(503, HTTP_503_ServerUnavailableException.class, "HTTP 503 (Service Unavailable)", true);
        registerStatusCodeException(504, HTTP_504_ServerTimeoutException.class, "HTTP 504 (Gateway Timeout)", false);
    }

    /**
     * Builds the HTTP configuration, adding the API key from environment variable if not already set.
     */
    private static ApiHttpConfiguration buildHttpConfig(ApiHttpConfiguration existingConfig) {
        // Check if we already have an Authorization header
        if (existingConfig != null && existingConfig.getGlobalHeaders().containsKey("Authorization")) {
            return existingConfig;
        }

        // Try to get API key from environment variable
        String apiKey = System.getenv("OPENROUTER_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            // No API key available - return existing config or empty config
            return existingConfig != null ? existingConfig : new ApiHttpConfiguration();
        }

        // Build new config with API key
        ApiHttpConfiguration.Builder builder = existingConfig != null
            ? existingConfig.toBuilder()
            : ApiHttpConfiguration.builder();

        return builder
            .header("Authorization", "Bearer " + apiKey)
            .build();
    }

    public OpenRouterChat chat() {
        return new OpenRouterChat(this);
    }

    /**
     * Reads the credit balance of the account:
     * GET /credits (management key required).
     *
     * @return the starting point for the request
     */
    public OpenRouterCreditsRequest.Builder credits() {
        return new OpenRouterCreditsRequest.Builder(this);
    }

    /**
     * Lists the full OpenRouter model catalog:
     * GET /models.
     *
     * @return the starting point for the request
     */
    public OpenRouterModelsListRequest.Builder models() {
        return new OpenRouterModelsListRequest.Builder(this);
    }

    /**
     * Counts the models in the catalog:
     * GET /models/count.
     *
     * @return the starting point for the request
     */
    public OpenRouterModelsCountRequest.Builder modelsCount() {
        return new OpenRouterModelsCountRequest.Builder(this);
    }

    /**
     * Lists the models the authenticated account may use:
     * GET /models/user.
     *
     * @return the starting point for the request
     */
    public OpenRouterUserModelsRequest.Builder userModels() {
        return new OpenRouterUserModelsRequest.Builder(this);
    }

    /**
     * Fetches one model from the catalog:
     * GET /model/{author}/{slug}.
     *
     * @param modelId the full model id ({@code author/slug}); the first slash splits it
     * @return the starting point for the request
     */
    public OpenRouterModelRequest.Builder model(String modelId) {
        int slash = modelId != null ? modelId.indexOf('/') : -1;
        if (modelId == null || modelId.isEmpty() || slash <= 0 || slash == modelId.length() - 1) {
            throw new IllegalArgumentException(
                    "modelId must be of the form \"author/slug\", got: " + modelId);
        }
        return new OpenRouterModelRequest.Builder(this,
                modelId.substring(0, slash), modelId.substring(slash + 1));
    }

    /**
     * Lists the serving endpoints of one model:
     * GET /models/{author}/{slug}/endpoints.
     *
     * @param modelId the full model id ({@code author/slug}); the first slash splits it
     * @return the starting point for the request
     */
    public OpenRouterModelEndpointsRequest.Builder modelEndpoints(String modelId) {
        int slash = modelId != null ? modelId.indexOf('/') : -1;
        if (modelId == null || modelId.isEmpty() || slash <= 0 || slash == modelId.length() - 1) {
            throw new IllegalArgumentException(
                    "modelId must be of the form \"author/slug\", got: " + modelId);
        }
        return new OpenRouterModelEndpointsRequest.Builder(this,
                modelId.substring(0, slash), modelId.substring(slash + 1));
    }

    /**
     * Fetches the request/usage metadata of one generation:
     * GET /generation?id=... (management key required). OpenRouter needs a few
     * seconds after a completion before the metadata is queryable - querying
     * earlier fails with HTTP 404.
     *
     * @param generationId the generation id ({@code gen-...})
     * @return the starting point for the request
     */
    public OpenRouterGenerationRequest.Builder generation(String generationId) {
        return new OpenRouterGenerationRequest.Builder(this, generationId);
    }

    /**
     * Fetches the stored prompt and completion of one generation:
     * GET /generation/content?id=... (management key required). OpenRouter
     * needs a few seconds after a completion before the content is queryable -
     * querying earlier fails with HTTP 404.
     *
     * @param generationId the generation id ({@code gen-...})
     * @return the starting point for the request
     */
    public OpenRouterGenerationContentRequest.Builder generationContent(String generationId) {
        return new OpenRouterGenerationContentRequest.Builder(this, generationId);
    }

    /**
     * Submits structured feedback on one generation:
     * POST /generation/feedback (management key required).
     *
     * @return the starting point for the request
     */
    public OpenRouterGenerationFeedbackRequest.Builder generationFeedback() {
        return new OpenRouterGenerationFeedbackRequest.Builder(this);
    }

    /**
     * Reads the usage activity of the account:
     * GET /activity (management key required).
     *
     * @return the starting point for the request
     */
    public OpenRouterActivityRequest.Builder activity() {
        return new OpenRouterActivityRequest.Builder(this);
    }

    /**
     * Queries the account's usage analytics:
     * POST /analytics/query (management key required).
     *
     * @return the starting point for the request
     */
    public OpenRouterAnalyticsQueryRequest.Builder analyticsQuery() {
        return new OpenRouterAnalyticsQueryRequest.Builder(this);
    }

    /**
     * Creates embedding vectors:
     * POST /embeddings.
     *
     * @return the starting point for the request
     */
    public OpenRouterEmbeddingsRequest.Builder embeddings() {
        return new OpenRouterEmbeddingsRequest.Builder(this);
    }

    /**
     * Lists the embedding models of the catalog:
     * GET /embeddings/models.
     *
     * @return the starting point for the request
     */
    public OpenRouterEmbeddingsModelsRequest.Builder embeddingsModels() {
        return new OpenRouterEmbeddingsModelsRequest.Builder(this);
    }

    /**
     * Reranks documents against a query:
     * POST /rerank.
     *
     * @return the starting point for the request
     */
    public OpenRouterRerankRequest.Builder rerank() {
        return new OpenRouterRerankRequest.Builder(this);
    }

    /**
     * Creates an OAuth authorization code:
     * POST /auth/keys/code. Step one of the OAuth authorization-code flow
     * with PKCE; pair it with {@link de.entwicklertraining.openrouter4j.oauth.OpenRouterPkce}.
     *
     * @return the starting point for the request
     */
    public OpenRouterCreateAuthorizationCodeRequest.Builder createAuthorizationCode() {
        return new OpenRouterCreateAuthorizationCodeRequest.Builder(this);
    }

    /**
     * Exchanges an OAuth authorization code for an API key:
     * POST /auth/keys. Final step of the OAuth authorization-code flow with
     * PKCE.
     *
     * @return the starting point for the request
     */
    public OpenRouterAuthorizationCodeExchangeRequest.Builder exchangeAuthorizationCode() {
        return new OpenRouterAuthorizationCodeExchangeRequest.Builder(this);
    }

    /**
     * Configures app attribution headers (app URL, display name, marketplace
     * categories) once on the client; they are then sent with every request.
     * <p>
     * Per-request configuration via the
     * {@code httpReferer}/{@code appTitle}/{@code appCategories} builder methods
     * wins over this client-level default for that single request.
     * <p>
     * JSON fields: none - the values travel as the {@code HTTP-Referer},
     * {@code X-OpenRouter-Title} (legacy alias {@code X-Title}) and
     * {@code X-OpenRouter-Categories} (max 2 categories) HTTP headers.
     *
     * @param attribution the attribution data, or {@code null} to remove a previously
     *        configured client-level attribution
     * @return this client instance
     */
    public OpenRouterClient appAttribution(OpenRouterAppAttribution attribution) {
        this.appAttribution = attribution;
        return this;
    }

    /**
     * @return the client-level app attribution, or {@code null} when none is configured
     */
    public OpenRouterAppAttribution appAttribution() {
        return appAttribution;
    }

    public static class OpenRouterChat {
        private final OpenRouterClient client;

        public OpenRouterChat(OpenRouterClient client) {
            this.client = client;
        }

        public OpenRouterChatCompletionRequest.Builder completion() {
            return OpenRouterChatCompletionRequest.builder(client);
        }
    }

}
