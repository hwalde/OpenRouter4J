package de.entwicklertraining.openrouter4j.byok;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /byok/{id}: a single credential.
 */
public final class OpenRouterByokGetResponse extends OpenRouterResponse<OpenRouterByokGetRequest> {


    OpenRouterByokGetResponse(JSONObject json, OpenRouterByokGetRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data} - the credential.
 */
    public OpenRouterByokKey data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterByokKey(data) : null;
    }
}
