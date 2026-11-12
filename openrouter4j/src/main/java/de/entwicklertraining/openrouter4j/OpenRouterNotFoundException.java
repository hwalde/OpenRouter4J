package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.api.base.ApiErrorContext;

/**
 * Thrown when OpenRouter answers HTTP 404 (no such resource).
 *
 * <p>Enriched form of {@code ApiClient.HTTP_404_NotFoundException}: catch clauses for
 * the api-base type keep working, and the error-body accessors of
 * {@link OpenRouterErrorMetadata} ride along. The Intern API documents
 * {@code reason: "not_found", retryable: false} here. Not retried by the client.
 *
 * <p>Registered by {@link OpenRouterClient} for HTTP 404 (client-side retry of this
 * status: no). Created by the transport with the composed message
 * {@code "<registered message>: <raw error body>"} and the {@link ApiErrorContext} of
 * the failed exchange.
 */
public final class OpenRouterNotFoundException extends ApiClient.HTTP_404_NotFoundException implements OpenRouterErrorMetadata {

    /**
     * Creates the exception with its composed detail message.
     *
     * @param message the detail message (the registered message plus the raw error body)
     */
    public OpenRouterNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates the exception with its detail message and the HTTP error context of the
     * failed exchange.
     *
     * @param message the detail message
     * @param apiErrorContext the status code, error body and response headers of the failed exchange
     */
    public OpenRouterNotFoundException(String message, ApiErrorContext apiErrorContext) {
        super(message, apiErrorContext);
    }
}
