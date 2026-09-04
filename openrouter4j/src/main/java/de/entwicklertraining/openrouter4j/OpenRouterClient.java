package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.api.base.ApiClientSettings;
import de.entwicklertraining.api.base.ApiHttpConfiguration;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest;

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
