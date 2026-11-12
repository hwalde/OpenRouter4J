package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiErrorContext;
import org.json.JSONObject;

/**
 * Typed accessors over the error body and error response headers of a failed OpenRouter
 * call. Implemented by every enriched status-code exception
 * {@link OpenRouterClient} throws (e.g. {@link OpenRouterRateLimitOrQuotaException}),
 * and available for any other thrown error through {@link #of(Throwable)}.
 *
 * <p><strong>What travels in the error body.</strong> OpenRouter error responses carry
 * {@code {"error": {"message", "code", "metadata": {...}}}}; the accessors read the
 * {@code metadata} object. Which keys appear where (from the API reference's
 * errors-and-debugging page and the published OpenAPI schemas):
 * <ul>
 *   <li>{@code reason} / {@code retryable} - the retry decision pair. The Intern API
 *       answers {@code 408} with {@code reason: "timeout", retryable: true} and
 *       {@code 404} with {@code reason: "not_found", retryable: false}; the same pair
 *       appears across the management surfaces.</li>
 *   <li>{@code error_type} - a canonical error type, exposed on moderation blocks and
 *       provider errors; {@code provider_code} carries the upstream provider's own
 *       error code.</li>
 *   <li>{@code limit_source} - on {@code 402} answers, {@code "openrouter_in_flight_budget"}
 *       is the documented signal that this is a wait-and-retry rejection (together with a
 *       {@code Retry-After} response header) rather than a hard out-of-credit rejection.
 *       A {@code 402} is currently <em>not</em> registered as its own exception type in
 *       {@link OpenRouterClient}, so it arrives as {@code ApiClient.ApiClientException} -
 *       read it with {@link #of(Throwable)}.</li>
 *   <li>Moderation-flag metadata - a flagged input answers with flag categories in
 *       {@code metadata}; read them off the raw {@link #errorMetadata()} object.</li>
 * </ul>
 *
 * <p><strong>Conventions.</strong> Every accessor swallows malformed data and answers
 * {@code null}: a body that is not JSON, an {@code error} or {@code metadata} value of
 * the wrong shape, or a field of the wrong type all look like an absent field, so a
 * malformed error response never breaks the catch block that inspects it. {@code null}
 * from {@link #isRetryable()} means "the error body said nothing about retryability" -
 * it does not mean "not retryable".
 *
 * <p><strong>Headers.</strong> {@link #retryAfter()} / {@link #retryAfterSeconds()} read
 * the {@code Retry-After} response header of the failed exchange - the error-side
 * counterpart of {@link OpenRouterResponse#retryAfter()}. The synchronous and the
 * streaming retry loops only honor it when the client opts in
 * ({@code ApiClientSettings.builder().honorRetryAfter(true)}; OpenRouterClient leaves
 * this off, so a caller that wants the documented "honor Retry-After when present"
 * behaviour decides per call).
 */
public interface OpenRouterErrorMetadata {

    /**
     * Returns the raw {@code error.metadata} object of the error body, or {@code null}
     * when the body carries none or is malformed.
     *
     * @return the metadata object, or {@code null}
     */
    default JSONObject errorMetadata() {
        ApiErrorContext context = getApiErrorContext();
        if (context == null) {
            return null;
        }
        JSONObject body = parseBody(context.responseBody());
        if (body == null) {
            return null;
        }
        JSONObject error = body.optJSONObject("error");
        return error == null ? null : error.optJSONObject("metadata");
    }

    /**
     * Returns {@code error.metadata.reason} (e.g. {@code "timeout"} on an Intern API
     * 408, {@code "not_found"} on a 404), or {@code null} when absent or not a string.
     *
     * @return the reason, or {@code null}
     */
    default String errorReason() {
        JSONObject metadata = errorMetadata();
        return metadata == null ? null : optString(metadata, "reason");
    }

    /**
     * Returns {@code error.metadata.retryable} ({@code true} when the request may be
     * retried as-is), or {@code null} when absent or not a boolean. {@code null} is
     * "undocumented", not "not retryable".
     *
     * @return the retryable flag, or {@code null}
     */
    default Boolean isRetryable() {
        JSONObject metadata = errorMetadata();
        if (metadata == null) {
            return null;
        }
        Object value = metadata.opt("retryable");
        return value instanceof Boolean ? (Boolean) value : null;
    }

    /**
     * Returns {@code error.metadata.error_type} (the canonical error type exposed on
     * moderation blocks and provider errors), or {@code null} when absent or not a
     * string.
     *
     * @return the error type, or {@code null}
     */
    default String errorType() {
        JSONObject metadata = errorMetadata();
        return metadata == null ? null : optString(metadata, "error_type");
    }

    /**
     * Returns {@code error.metadata.provider_code} (the upstream provider's own error
     * code), or {@code null} when absent or not a string.
     *
     * @return the provider code, or {@code null}
     */
    default String providerCode() {
        JSONObject metadata = errorMetadata();
        return metadata == null ? null : optString(metadata, "provider_code");
    }

    /**
     * Returns {@code error.metadata.limit_source} - on a 402, the documented
     * {@code "openrouter_in_flight_budget"} says this is a wait-and-retry rejection (see
     * {@link #retryAfter()}), not a hard out-of-credit rejection. Answers {@code null}
     * when absent or not a string.
     *
     * @return the limit source, or {@code null}
     */
    default String limitSource() {
        JSONObject metadata = errorMetadata();
        return metadata == null ? null : optString(metadata, "limit_source");
    }

    /**
     * Returns the raw {@code Retry-After} response header of the failed exchange, or
     * {@code null} when the error response carried none.
     *
     * @return the raw header value, or {@code null}
     */
    default String retryAfter() {
        ApiErrorContext context = getApiErrorContext();
        return context == null ? null : context.header("Retry-After");
    }

    /**
     * Returns the {@code Retry-After} delay in seconds (delta-seconds form, or the
     * seconds until the HTTP-date form elapses - a date in the past answers {@code 0}),
     * or {@code null} when the header is absent or unparsable.
     *
     * @return the delay in seconds, or {@code null}
     */
    default Long retryAfterSeconds() {
        return OpenRouterResponse.parseRetryAfterSeconds(retryAfter());
    }

    /**
     * Returns the HTTP error context (status code, body, headers) of the failed
     * exchange, or {@code null} when the exception carries none.
     *
     * @return the error context, or {@code null}
     */
    ApiErrorContext getApiErrorContext();

    /**
     * Returns the error-metadata view of any thrown error, or {@code null} when the
     * throwable carries no {@link ApiErrorContext} (raised without an HTTP exchange, or
     * created by hand). Use this for the errors {@link OpenRouterClient} does not
     * register a dedicated exception type for (401, 402, 408, 422, ...) and for
     * streaming errors, which the async execution layer wraps into other exceptions.
     *
     * @param throwable the thrown error
     * @return the view, or {@code null}
     */
    static OpenRouterErrorMetadata of(Throwable throwable) {
        ApiErrorContext context = ApiErrorContext.of(throwable);
        if (context == null) {
            return null;
        }
        return new OpenRouterErrorMetadata() {
            @Override
            public ApiErrorContext getApiErrorContext() {
                return context;
            }
        };
    }

    private static JSONObject parseBody(String responseBody) {
        if (responseBody == null) {
            return null;
        }
        try {
            return new JSONObject(responseBody);
        } catch (RuntimeException malformedBody) {
            return null;
        }
    }

    private static String optString(JSONObject object, String key) {
        Object value = object.opt(key);
        return value instanceof String ? (String) value : null;
    }
}
