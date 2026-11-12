package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterErrorMetadata;
import de.entwicklertraining.openrouter4j.OpenRouterRateLimitOrQuotaException;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates the typed error surface of the library.
 *
 * <p>OpenRouter error responses carry structured metadata
 * ({@code error.metadata.reason}, {@code retryable}, {@code error_type},
 * {@code provider_code}, {@code limit_source}) and decision-relevant response headers
 * ({@code Retry-After}, {@code X-RateLimit-*}). Both travel on the thrown exception:
 * the registered status codes arrive as enriched exceptions that extend the api-base
 * types (existing catch clauses keep working), and any other status arrives as
 * {@code ApiClient.ApiClientException} - readable through
 * {@link OpenRouterErrorMetadata#of(Throwable)}. Every accessor swallows malformed
 * data and answers {@code null}.
 */
public class OpenRouterErrorHandlingExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        try {
            OpenRouterChatCompletionResponse response = client.chat().completion()
                    .model("openai/gpt-4o-mini")
                    .addMessage("user", "Say hello.")
                    .execute();
            System.out.println("Answer: " + response.assistantMessage());
            System.out.println("Generation id (X-Generation-Id): " + response.generationIdHeader());
        } catch (OpenRouterRateLimitOrQuotaException rateLimited) {
            // 429 - enriched: error-body metadata and the error response's headers
            System.err.println("Rate limited. reason=" + rateLimited.errorReason()
                    + " retryable=" + rateLimited.isRetryable());
            System.err.println("Retry after " + rateLimited.retryAfterSeconds()
                    + "s (raw Retry-After: " + rateLimited.retryAfter() + ")");
            if ("openrouter_in_flight_budget".equals(rateLimited.limitSource())) {
                System.err.println("This is an in-flight-budget rejection - wait and retry, "
                        + "not an out-of-credit rejection.");
            }
            rateLimited.getApiErrorContext().responseHeaders().forEach((name, value) ->
                    System.err.println("  " + name + ": " + value));
        } catch (ApiClient.ApiClientException other) {
            // every other failure (incl. the unregistered 401/402/408/422 statuses)
            OpenRouterErrorMetadata meta = OpenRouterErrorMetadata.of(other);
            if (meta != null) {
                System.err.println("HTTP " + meta.getApiErrorContext().statusCode()
                        + ": reason=" + meta.errorReason()
                        + " retryable=" + meta.isRetryable()
                        + " error_type=" + meta.errorType()
                        + " provider_code=" + meta.providerCode());
                // moderation blocks carry their flag categories on the raw metadata object:
                System.err.println("metadata: " + meta.errorMetadata());
            } else {
                System.err.println("Transport-level failure: " + other.getMessage());
            }
        }
    }
}
