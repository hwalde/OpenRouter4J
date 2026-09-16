package de.entwicklertraining.openrouter4j.guardrails;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of PATCH /guardrails/{id}: the updated guardrail.
 */
public final class OpenRouterGuardrailUpdateResponse extends OpenRouterResponse<OpenRouterGuardrailUpdateRequest> {


    OpenRouterGuardrailUpdateResponse(JSONObject json, OpenRouterGuardrailUpdateRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data} - the updated guardrail.
 */
    public OpenRouterGuardrail data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterGuardrail(data) : null;
    }
}
