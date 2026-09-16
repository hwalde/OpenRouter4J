package de.entwicklertraining.openrouter4j.byok;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /byok: the created credential (metadata only - the raw
 * provider key is never returned).
 */
public final class OpenRouterByokCreateResponse extends OpenRouterResponse<OpenRouterByokCreateRequest> {


    OpenRouterByokCreateResponse(JSONObject json, OpenRouterByokCreateRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data} - the created credential.
 */
    public OpenRouterByokKey data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterByokKey(data) : null;
    }
}
