package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /interns/{internId}/invoke - the 202 answer that says
 * only that the run was accepted.
 *
 * <p>JSON shape: {@code session_id} plus {@code status} ({@code started} or
 * {@code steered}). The run continues on the intern after this response;
 * progress never comes back here. Submission success is not run success.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 */
public final class OpenRouterInternInvokeResponse
        extends OpenRouterResponse<OpenRouterInternInvokeRequest> {

    OpenRouterInternInvokeResponse(JSONObject json, OpenRouterInternInvokeRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code session_id} - the session this run belongs to. Send
     * it back as {@code session_id} on a follow-up invoke to continue the
     * conversation. Only the caller it was issued to may use it.
     *
     * @return the session id, or {@code null} when absent
     */
    public String sessionId() {
        return json.optString("session_id", null);
    }

    /**
     * JSON path: {@code status} - {@code started} (a new run was started) or
     * {@code steered} (the prompt was delivered into a run already going on
     * this session).
     *
     * @return the status, or {@code null} when absent
     */
    public String status() {
        return json.optString("status", null);
    }

    /**
     * @return {@code true} when the prompt was steered into a run already
     *         going on this session ({@code status} is {@code steered})
     */
    public boolean isSteered() {
        return "steered".equals(status());
    }

    /**
     * @return {@code true} when a new run was started ({@code status} is
     *         {@code started})
     */
    public boolean isStarted() {
        return "started".equals(status());
    }
}
