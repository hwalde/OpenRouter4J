package de.entwicklertraining.openrouter4j.oauth;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /auth/keys: the fresh OpenRouter API key created by the
 * OAuth authorization-code exchange.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}
 * convention: a field that is absent (or a malformed body) yields
 * {@code null} instead of an exception. Use {@link #getJson()} to inspect
 * the raw response.
 */
public final class OpenRouterAuthorizationCodeExchangeResponse
        extends OpenRouterResponse<OpenRouterAuthorizationCodeExchangeRequest> {

    OpenRouterAuthorizationCodeExchangeResponse(JSONObject json,
                                                OpenRouterAuthorizationCodeExchangeRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code key} - the API key to use for OpenRouter requests
     * (the {@code sk-or-v1-...} value). Trap: this is a long-lived secret -
     * store it securely, never log it, and treat this response object like
     * a credential.
     *
     * @return the value, or {@code null} when absent
     */
    public String key() {
        try {
            return json.optString("key", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code user_id} - the user ID associated with the API key.
     *
     * @return the value, or {@code null} when absent
     */
    public String userId() {
        try {
            return json.optString("user_id", null);
        } catch (Exception e) {
            return null;
        }
    }
}
