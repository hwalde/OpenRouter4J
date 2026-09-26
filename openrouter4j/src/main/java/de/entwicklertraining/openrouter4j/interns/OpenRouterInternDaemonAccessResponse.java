package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /interns/{internId}/daemon-access - the daemon origin and
 * bearer token that attach {@code ori tui --host} to one intern.
 *
 * <p>The token is a <b>credential</b>: the response is sent with
 * {@code Cache-Control: no-store} and each reveal is logged by caller and
 * intern. {@link #toString()} is overridden to never print the body, so an
 * accidental log statement cannot leak the token (mirroring
 * {@code OpenRouterKeyCreateResponse}).
 */
public final class OpenRouterInternDaemonAccessResponse
        extends OpenRouterResponse<OpenRouterInternDaemonAccessRequest> {

    OpenRouterInternDaemonAccessResponse(JSONObject json,
            OpenRouterInternDaemonAccessRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code origin} - the daemon origin URL to pass to
     * {@code ori tui --host}, e.g.
     * {@code https://research-assistant-7c9e6679.or.bot}.
     *
     * @return the daemon origin, or {@code null} when absent
     */
    public String origin() {
        return json.optString("origin", null);
    }

    /**
     * JSON path: {@code token} - the bearer token for the intern's daemon.
     * This is the one plaintext reveal; treat it as a secret: never log it,
     * never put it in an error message. Each reveal is logged server-side by
     * caller and intern.
     *
     * @return the daemon token, or {@code null} when absent
     */
    public String token() {
        return json.optString("token", null);
    }

    /**
     * Deliberately does not include the response body: the body carries the
     * daemon token.
     *
     * @return a body-free description of this response
     */
    @Override
    public String toString() {
        return "OpenRouterInternDaemonAccessResponse{token=<redacted>}";
    }
}
