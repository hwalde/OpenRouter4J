package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.api.base.ApiErrorContext;

/**
 * Thrown when OpenRouter answers HTTP 400 (the request was rejected).
 *
 * <p>Enriched form of {@code ApiClient.HTTP_400_RequestRejectedException}: catch
 * clauses for the api-base type keep working, and the error-body accessors of
 * {@link OpenRouterErrorMetadata} ride along. A flagged input arrives here with
 * moderation-flag metadata in {@code error.metadata} (the flag categories) - read it
 * off {@link #errorMetadata()}. Not retried by the client (the request must change).
 *
 * <p>Registered by {@link OpenRouterClient} for HTTP 400 (client-side retry of this
 * status: no). Created by the transport with the composed message
 * {@code "<registered message>: <raw error body>"} and the {@link ApiErrorContext} of
 * the failed exchange.
 */
public final class OpenRouterRequestRejectedException extends ApiClient.HTTP_400_RequestRejectedException implements OpenRouterErrorMetadata {

    /**
     * Creates the exception with its composed detail message.
     *
     * @param message the detail message (the registered message plus the raw error body)
     */
    public OpenRouterRequestRejectedException(String message) {
        super(message);
    }

    /**
     * Creates the exception with its detail message and the HTTP error context of the
     * failed exchange.
     *
     * @param message the detail message
     * @param apiErrorContext the status code, error body and response headers of the failed exchange
     */
    public OpenRouterRequestRejectedException(String message, ApiErrorContext apiErrorContext) {
        super(message, apiErrorContext);
    }
}
