package de.entwicklertraining.openrouter4j.byok;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of PATCH /byok/{id}: the updated credential.
 */
public final class OpenRouterByokUpdateResponse extends OpenRouterResponse<OpenRouterByokUpdateRequest> {


    OpenRouterByokUpdateResponse(JSONObject json, OpenRouterByokUpdateRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data} - the updated credential.
 */
    public OpenRouterByokKey data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterByokKey(data) : null;
    }
}
