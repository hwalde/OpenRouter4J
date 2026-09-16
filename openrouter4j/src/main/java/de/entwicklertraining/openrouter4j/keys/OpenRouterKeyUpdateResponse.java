package de.entwicklertraining.openrouter4j.keys;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of PATCH /keys/{hash}: the updated API key.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterKeyUpdateResponse extends OpenRouterResponse<OpenRouterKeyUpdateRequest> {

    OpenRouterKeyUpdateResponse(JSONObject json, OpenRouterKeyUpdateRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the updated API key.
     *
     * @return the key view, or {@code null} when absent
     */
    public OpenRouterApiKey data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterApiKey(data) : null;
    }
}
