package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.api.base.ApiErrorContext;

/**
 * Thrown when OpenRouter answers HTTP 403 (the key may not perform this call).
 *
 * <p>Enriched form of {@code ApiClient.HTTP_403_PermissionDeniedException}: catch
 * clauses for the api-base type keep working, and the error-body accessors of
 * {@link OpenRouterErrorMetadata} ride along. The management surfaces document the
 * {@code reason} / {@code retryable} metadata pair on their error answers
 * ({@link #errorReason()} / {@link #isRetryable()}). Not retried by the client.
 *
 * <p>Registered by {@link OpenRouterClient} for HTTP 403 (client-side retry of this
 * status: no). Created by the transport with the composed message
 * {@code "<registered message>: <raw error body>"} and the {@link ApiErrorContext} of
 * the failed exchange.
 */
public final class OpenRouterPermissionDeniedException extends ApiClient.HTTP_403_PermissionDeniedException implements OpenRouterErrorMetadata {

    /**
     * Creates the exception with its composed detail message.
     *
     * @param message the detail message (the registered message plus the raw error body)
     */
    public OpenRouterPermissionDeniedException(String message) {
        super(message);
    }

    /**
     * Creates the exception with its detail message and the HTTP error context of the
     * failed exchange.
     *
     * @param message the detail message
     * @param apiErrorContext the status code, error body and response headers of the failed exchange
     */
    public OpenRouterPermissionDeniedException(String message, ApiErrorContext apiErrorContext) {
        super(message, apiErrorContext);
    }
}
