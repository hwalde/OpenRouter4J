package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.api.base.ApiErrorContext;

/**
 * Thrown when OpenRouter answers HTTP 429 (rate limited or over quota).
 *
 * <p>Enriched form of {@code ApiClient.HTTP_429_RateLimitOrQuotaException}: catch
 * clauses for the api-base type keep working, and the error-body accessors of
 * {@link OpenRouterErrorMetadata} ride along. This is also the answer that carries the
 * {@code X-RateLimit-Limit} / {@code X-RateLimit-Remaining} / {@code X-RateLimit-Reset}
 * response headers (deliberately not sent on successful inference responses) and
 * usually a {@code Retry-After} delay ({@link #retryAfter()}). The client retries this
 * status; the {@code retryable} metadata flag is the server's own verdict for the
 * caller, not the client's registration flag. Compare the {@code 402} in-flight-budget
 * rejection on {@link OpenRouterErrorMetadata#limitSource()}.
 *
 * <p>Registered by {@link OpenRouterClient} for HTTP 429 (client-side retry of this
 * status: yes). Created by the transport with the composed message
 * {@code "<registered message>: <raw error body>"} and the {@link ApiErrorContext} of
 * the failed exchange.
 */
public final class OpenRouterRateLimitOrQuotaException extends ApiClient.HTTP_429_RateLimitOrQuotaException implements OpenRouterErrorMetadata {

    /**
     * Creates the exception with its composed detail message.
     *
     * @param message the detail message (the registered message plus the raw error body)
     */
    public OpenRouterRateLimitOrQuotaException(String message) {
        super(message);
    }

    /**
     * Creates the exception with its detail message and the HTTP error context of the
     * failed exchange.
     *
     * @param message the detail message
     * @param apiErrorContext the status code, error body and response headers of the failed exchange
     */
    public OpenRouterRateLimitOrQuotaException(String message, ApiErrorContext apiErrorContext) {
        super(message, apiErrorContext);
    }
}
