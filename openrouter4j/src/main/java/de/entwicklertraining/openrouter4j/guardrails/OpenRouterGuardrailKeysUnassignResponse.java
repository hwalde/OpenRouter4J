package de.entwicklertraining.openrouter4j.guardrails;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /guardrails/{id}/assignments/keys/remove: the unassignment
 * count.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterGuardrailKeysUnassignResponse extends OpenRouterResponse<OpenRouterGuardrailKeysUnassignRequest> {


    OpenRouterGuardrailKeysUnassignResponse(JSONObject json, OpenRouterGuardrailKeysUnassignRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code unassigned_count} - number of keys successfully unassigned.
 */
    public Integer unassignedCount() {
        if (!json.has("unassigned_count") || json.isNull("unassigned_count")) {
            return null;
        }
        return (int) json.optLong("unassigned_count");
    }
}
