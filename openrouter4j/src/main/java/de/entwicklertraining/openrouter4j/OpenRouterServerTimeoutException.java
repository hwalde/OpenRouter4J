package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.api.base.ApiErrorContext;

/**
 * Thrown when OpenRouter answers HTTP 504 (gateway timeout).
 *
 * <p>Enriched form of {@code ApiClient.HTTP_504_ServerTimeoutException}: catch clauses
 * for the api-base type keep working, and the error-body accessors of
 * {@link OpenRouterErrorMetadata} ride along. The Intern API answers its own timeouts
 * as {@code 408} (seen with {@code reason: "timeout", retryable: true}) - that status has
 * no dedicated type here and arrives as {@code ApiClient.ApiClientException}, readable
 * via {@link OpenRouterErrorMetadata#of(Throwable)}. Not retried by the client.
 *
 * <p>Registered by {@link OpenRouterClient} for HTTP 504 (client-side retry of this
 * status: no). Created by the transport with the composed message
 * {@code "<registered message>: <raw error body>"} and the {@link ApiErrorContext} of
 * the failed exchange.
 */
public final class OpenRouterServerTimeoutException extends ApiClient.HTTP_504_ServerTimeoutException implements OpenRouterErrorMetadata {

    /**
     * Creates the exception with its composed detail message.
     *
     * @param message the detail message (the registered message plus the raw error body)
     */
    public OpenRouterServerTimeoutException(String message) {
        super(message);
    }

    /**
     * Creates the exception with its detail message and the HTTP error context of the
     * failed exchange.
     *
     * @param message the detail message
     * @param apiErrorContext the status code, error body and response headers of the failed exchange
     */
    public OpenRouterServerTimeoutException(String message, ApiErrorContext apiErrorContext) {
        super(message, apiErrorContext);
    }
}
