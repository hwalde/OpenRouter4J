package de.entwicklertraining.openrouter4j.guardrails;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /guardrails: the created guardrail.
 */
public final class OpenRouterGuardrailCreateResponse extends OpenRouterResponse<OpenRouterGuardrailCreateRequest> {


    OpenRouterGuardrailCreateResponse(JSONObject json, OpenRouterGuardrailCreateRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data} - the created guardrail.
 */
    public OpenRouterGuardrail data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterGuardrail(data) : null;
    }
}
