package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.api.base.ApiErrorContext;

/**
 * Thrown when OpenRouter answers HTTP 500 (upstream server error).
 *
 * <p>Enriched form of {@code ApiClient.HTTP_500_ServerErrorException}: catch clauses
 * for the api-base type keep working, and the error-body accessors of
 * {@link OpenRouterErrorMetadata} ride along. Trap (from the reference): when a request
 * fails with a 500, the message is replaced with a generic string and
 * {@code provider_code} is omitted, while {@code error_type} stays ({@code "server"}) -
 * {@link #providerCode()} therefore answers {@code null} on this status and carries the
 * upstream's own code only on non-500 provider errors. The client retries this status.
 *
 * <p>Registered by {@link OpenRouterClient} for HTTP 500 (client-side retry of this
 * status: yes). Created by the transport with the composed message
 * {@code "<registered message>: <raw error body>"} and the {@link ApiErrorContext} of
 * the failed exchange.
 */
public final class OpenRouterServerErrorException extends ApiClient.HTTP_500_ServerErrorException implements OpenRouterErrorMetadata {

    /**
     * Creates the exception with its composed detail message.
     *
     * @param message the detail message (the registered message plus the raw error body)
     */
    public OpenRouterServerErrorException(String message) {
        super(message);
    }

    /**
     * Creates the exception with its detail message and the HTTP error context of the
     * failed exchange.
     *
     * @param message the detail message
     * @param apiErrorContext the status code, error body and response headers of the failed exchange
     */
    public OpenRouterServerErrorException(String message, ApiErrorContext apiErrorContext) {
        super(message, apiErrorContext);
    }
}
