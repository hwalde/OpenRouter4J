package de.entwicklertraining.openrouter4j.oauth;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /oauth/token (RFC 8693): the short-lived OpenRouter
 * access token and its metadata.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}
 * convention: a field that is absent (or a malformed body) yields
 * {@code null} instead of an exception. Use {@link #getJson()} to inspect
 * the raw response.
 */
public final class OpenRouterWorkloadIdentityExchangeResponse
        extends OpenRouterResponse<OpenRouterWorkloadIdentityExchangeRequest> {

    OpenRouterWorkloadIdentityExchangeResponse(JSONObject json,
                                               OpenRouterWorkloadIdentityExchangeRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code access_token} - the short-lived JWT to send as
     * {@code Authorization: Bearer} to the inference API. Trap: this token
     * expires (at most 15 minutes, see {@link #expiresIn()}) - store it in
     * memory only, never log it, and re-exchange when it lapses.
     *
     * @return the value, or {@code null} when absent
     */
    public String accessToken() {
        try {
            return json.optString("access_token", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code expires_in} - seconds until the access token
     * expires: at most 15 minutes, and never later than the subject token
     * expires.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long expiresIn() {
        try {
            Object value = json.opt("expires_in");
            return value instanceof Number ? ((Number) value).longValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code issued_token_type} - the type of the issued token
     * ({@code urn:ietf:params:oauth:token-type:access_token}).
     *
     * @return the value, or {@code null} when absent
     */
    public String issuedTokenType() {
        try {
            return json.optString("issued_token_type", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code scope} - the scope granted ({@code inference}).
     *
     * @return the value, or {@code null} when absent
     */
    public String scope() {
        try {
            return json.optString("scope", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code token_type} - the token type ({@code Bearer}).
     *
     * @return the value, or {@code null} when absent
     */
    public String tokenType() {
        try {
            return json.optString("token_type", null);
        } catch (Exception e) {
            return null;
        }
    }
}
