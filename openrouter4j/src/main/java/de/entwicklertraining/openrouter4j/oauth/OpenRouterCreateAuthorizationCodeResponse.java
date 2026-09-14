package de.entwicklertraining.openrouter4j.oauth;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /auth/keys/code: the created authorization code.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}
 * convention: a field that is absent (or a malformed body) yields
 * {@code null} instead of an exception. Use {@link #getJson()} to inspect
 * the raw response.
 */
public final class OpenRouterCreateAuthorizationCodeResponse
        extends OpenRouterResponse<OpenRouterCreateAuthorizationCodeRequest> {

    OpenRouterCreateAuthorizationCodeResponse(JSONObject json,
                                              OpenRouterCreateAuthorizationCodeRequest request) {
        super(json, request);
    }

    /**
     * @return the raw {@code data} object of the response, or {@code null} when absent
     */
    public JSONObject data() {
        try {
            return json.optJSONObject("data");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code data.id} - the authorization code ID. It identifies
     * the code inside the consent redirect and is the {@code code} input of
     * {@link OpenRouterAuthorizationCodeExchangeRequest}.
     *
     * @return the value, or {@code null} when absent
     */
    public String id() {
        try {
            JSONObject data = data();
            return data != null ? data.optString("id", null) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code data.app_id} - the application ID associated with
     * this auth code.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long appId() {
        try {
            JSONObject data = data();
            Object value = data != null ? data.opt("app_id") : null;
            return value instanceof Number ? ((Number) value).longValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code data.created_at} - ISO 8601 timestamp of when the
     * auth code was created.
     *
     * @return the value, or {@code null} when absent
     */
    public String createdAt() {
        try {
            JSONObject data = data();
            return data != null ? data.optString("created_at", null) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
