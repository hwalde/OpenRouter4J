package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /interns/{internId}/chat/completions.
 *
 * <p>The endpoint streams, so on the normal path the typed response is not
 * the interesting part - the chunks flow through the installed
 * {@link OpenRouterInternChatAccumulator}. This response type exists for the
 * documented non-streamed answer: when a new user message is sent on a
 * session whose turn is still running, the API can answer 202 with
 * {@code {"status":"steered","session_id":...}} - the message was delivered
 * into the already running turn and its effect is streamed there, not here.
 * Refusals before the stream opens raise api-base exceptions instead.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 */
public final class OpenRouterInternChatResponse
        extends OpenRouterResponse<OpenRouterInternChatRequest> {

    OpenRouterInternChatResponse(JSONObject json, OpenRouterInternChatRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code status} - {@code steered} when the message was
     * handed to the turn already running on this session.
     *
     * @return the status, or {@code null} when absent
     */
    public String status() {
        return json.optString("status", null);
    }

    /**
     * @return {@code true} when the message was steered into an already
     *         running turn ({@code status} is {@code steered})
     */
    public boolean isSteered() {
        return "steered".equals(status());
    }

    /**
     * JSON path: {@code session_id} - the session of the running turn the
     * message was delivered to (on a steered answer), or the daemon session
     * to continue with (on a chunk-shaped body).
     *
     * @return the session id, or {@code null} when absent
     */
    public String sessionId() {
        return json.optString("session_id", null);
    }

    /**
     * JSON path: {@code id} - the completion id, on a chunk-shaped body.
     *
     * @return the id, or {@code null} when absent
     */
    public String id() {
        return json.optString("id", null);
    }

    /**
     * JSON path: {@code model} - the runtime's identifier for the model the
     * intern is running, on a chunk-shaped body. It is not an OpenRouter
     * model slug.
     *
     * @return the model identifier, or {@code null} when absent
     */
    public String model() {
        return json.optString("model", null);
    }
}
