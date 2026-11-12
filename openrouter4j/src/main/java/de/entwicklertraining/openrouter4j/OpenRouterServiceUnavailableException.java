package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.api.base.ApiErrorContext;

/**
 * Thrown when OpenRouter answers HTTP 503 (service unavailable).
 *
 * <p>Enriched form of {@code ApiClient.HTTP_503_ServerUnavailableException}: catch
 * clauses for the api-base type keep working, and the error-body accessors of
 * {@link OpenRouterErrorMetadata} ride along. The client retries this status; a
 * {@code Retry-After} response header ({@link #retryAfter()}) is honored only when the
 * retry policy opts in (see {@link OpenRouterErrorMetadata}).
 *
 * <p>Registered by {@link OpenRouterClient} for HTTP 503 (client-side retry of this
 * status: yes). Created by the transport with the composed message
 * {@code "<registered message>: <raw error body>"} and the {@link ApiErrorContext} of
 * the failed exchange.
 */
public final class OpenRouterServiceUnavailableException extends ApiClient.HTTP_503_ServerUnavailableException implements OpenRouterErrorMetadata {

    /**
     * Creates the exception with its composed detail message.
     *
     * @param message the detail message (the registered message plus the raw error body)
     */
    public OpenRouterServiceUnavailableException(String message) {
        super(message);
    }

    /**
     * Creates the exception with its detail message and the HTTP error context of the
     * failed exchange.
     *
     * @param message the detail message
     * @param apiErrorContext the status code, error body and response headers of the failed exchange
     */
    public OpenRouterServiceUnavailableException(String message, ApiErrorContext apiErrorContext) {
        super(message, apiErrorContext);
    }
}
