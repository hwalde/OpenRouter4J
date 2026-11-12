package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiResponse;
import org.json.JSONObject;

/**
 * Abstract base for OpenRouter-specific responses; extends
 * {@code ApiResponse<OpenRouterRequest<?>>} from the api-base library.
 *
 * <p>Carries the HTTP response headers of the exchange the response was parsed from:
 * {@link #generationIdHeader()} reads the {@code X-Generation-Id} generation id (the
 * same id {@code client.generation(id)} / {@code client.generationFeedback()} work
 * with), {@link #header(String)} is the raw escape hatch, and
 * {@link #retryAfter()} / {@link #retryAfterSeconds()} read {@code Retry-After}.
 * All of them swallow and answer {@code null} when the header is absent or unparsable.
 *
 * <p>Traps: successful inference responses deliberately carry no
 * {@code X-RateLimit-Limit} / {@code X-RateLimit-Remaining} / {@code X-RateLimit-Reset}
 * headers - those arrive only on a 429 error response, whose exception exposes them
 * through {@link OpenRouterErrorMetadata#getApiErrorContext()} instead. On a streaming
 * call the response is synthesized from the accumulated chunks after the fact and
 * carries the response headers of the last completed HTTP turn of the tool-call loop
 * (the last turn's {@code X-Generation-Id} is the one that identifies the final
 * generation); per-turn headers of intermediate turns are not retained.
 *
 * @param <T> the request type this response belongs to
 */
public abstract class OpenRouterResponse<T extends OpenRouterRequest<?>> extends ApiResponse<T> {

    /** The parsed response body. Subclasses read their accessors out of this. */
    protected final JSONObject json;

    protected OpenRouterResponse(JSONObject json, T request) {
        super(request);
        this.json = json;
    }

    public JSONObject getJson() {
        return json;
    }

    /**
     * Returns the value of the given response header of the underlying HTTP exchange
     * (case-insensitive name match), or {@code null} when the header was not sent or
     * {@code name} is {@code null}.
     *
     * @param name the header name, matched case-insensitively
     * @return the header value, or {@code null}
     */
    public String header(String name) {
        return getResponseHeader(name);
    }

    /**
     * Returns the {@code X-Generation-Id} response header - the generation id of this
     * response, feedable to {@code client.generation(id)} and
     * {@code client.generationFeedback()} - or {@code null} when the response carried
     * none.
     *
     * @return the generation id, or {@code null}
     */
    public String generationIdHeader() {
        return getResponseHeader("X-Generation-Id");
    }

    /**
     * Returns the raw {@code Retry-After} response header, or {@code null} when the
     * response carried none. See {@link #retryAfterSeconds()} for the parsed form.
     *
     * @return the raw header value, or {@code null}
     */
    public String retryAfter() {
        return getResponseHeader("Retry-After");
    }

    /**
     * Returns the {@code Retry-After} delay in seconds (delta-seconds form, or the
     * seconds until the HTTP-date form elapses - a date in the past answers {@code 0}),
     * or {@code null} when the header is absent or unparsable. {@code Retry-After} is
     * normally sent on 429/5xx error responses, not on successful ones - on a success
     * this accessor usually answers {@code null}.
     *
     * @return the delay in seconds, or {@code null}
     */
    public Long retryAfterSeconds() {
        return parseRetryAfterSeconds(retryAfter());
    }

    /**
     * Parses a {@code Retry-After} header value: delta-seconds, or the HTTP-date form
     * (answered as the seconds until that date, a date in the past as {@code 0}).
     * Unparsable values answer {@code null}.
     */
    static Long parseRetryAfterSeconds(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            long seconds = Long.parseLong(trimmed);
            return seconds < 0 ? null : seconds;
        } catch (NumberFormatException notDeltaSeconds) {
            // fall through to the HTTP-date form
        }
        try {
            java.time.ZonedDateTime when = java.time.ZonedDateTime.parse(
                    trimmed, java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME);
            long seconds = java.time.Duration.between(java.time.Instant.now(), when.toInstant()).toSeconds();
            return seconds < 0 ? 0L : seconds;
        } catch (java.time.DateTimeException notHttpDate) {
            return null;
        }
    }
}
