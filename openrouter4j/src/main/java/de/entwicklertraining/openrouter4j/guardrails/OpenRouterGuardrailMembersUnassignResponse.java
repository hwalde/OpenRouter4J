package de.entwicklertraining.openrouter4j.guardrails;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /guardrails/{id}/assignments/members/remove: the
 * unassignment count.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterGuardrailMembersUnassignResponse extends OpenRouterResponse<OpenRouterGuardrailMembersUnassignRequest> {


    OpenRouterGuardrailMembersUnassignResponse(JSONObject json, OpenRouterGuardrailMembersUnassignRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code unassigned_count} - number of members successfully
 * unassigned.
 */
    public Integer unassignedCount() {
        if (!json.has("unassigned_count") || json.isNull("unassigned_count")) {
            return null;
        }
        return (int) json.optLong("unassigned_count");
    }
}
