package de.entwicklertraining.openrouter4j.guardrails;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /guardrails/{id}: a single guardrail.
 */
public final class OpenRouterGuardrailGetResponse extends OpenRouterResponse<OpenRouterGuardrailGetRequest> {


    OpenRouterGuardrailGetResponse(JSONObject json, OpenRouterGuardrailGetRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data} - the guardrail.
 */
    public OpenRouterGuardrail data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterGuardrail(data) : null;
    }
}
