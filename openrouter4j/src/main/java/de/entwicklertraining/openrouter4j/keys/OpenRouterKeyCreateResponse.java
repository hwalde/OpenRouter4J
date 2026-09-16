package de.entwicklertraining.openrouter4j.keys;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /keys: the created key and its plaintext secret.
 *
 * <p>The plaintext key is returned only in this response and cannot be
 * retrieved later - treat it as a write-only secret. {@link #toString()} is
 * overridden to never print the body, so an accidental log statement cannot
 * leak the key.
 */
public final class OpenRouterKeyCreateResponse extends OpenRouterResponse<OpenRouterKeyCreateRequest> {

    OpenRouterKeyCreateResponse(JSONObject json, OpenRouterKeyCreateRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code key} - the plaintext API key, shown only once. Treat
     * this as a secret: never log it, never put it in an error message.
     *
     * @return the plaintext key, or {@code null} when absent
     */
    public String key() {
        return json.optString("key", null);
    }

    /**
     * JSON path: {@code data} - the created key's metadata (without the
     * plaintext).
     *
     * @return the key view, or {@code null} when absent
     */
    public OpenRouterApiKey data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterApiKey(data) : null;
    }

    /**
     * Deliberately does not include the response body: the body carries the
     * plaintext API key.
     *
     * @return a body-free description of this response
     */
    @Override
    public String toString() {
        return "OpenRouterKeyCreateResponse{key=<redacted>}";
    }
}
