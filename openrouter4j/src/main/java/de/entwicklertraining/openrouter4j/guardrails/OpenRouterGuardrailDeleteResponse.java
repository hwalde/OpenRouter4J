package de.entwicklertraining.openrouter4j.guardrails;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of DELETE /guardrails/{id}: the deletion confirmation.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterGuardrailDeleteResponse extends OpenRouterResponse<OpenRouterGuardrailDeleteRequest> {


    OpenRouterGuardrailDeleteResponse(JSONObject json, OpenRouterGuardrailDeleteRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code deleted} - confirmation that the guardrail was deleted.
 */
    public Boolean deleted() {
        if (!json.has("deleted") || json.isNull("deleted")) {
            return null;
        }
        return json.optBoolean("deleted");
    }
}
