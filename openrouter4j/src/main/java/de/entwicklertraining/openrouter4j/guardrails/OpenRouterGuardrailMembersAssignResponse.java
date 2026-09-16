package de.entwicklertraining.openrouter4j.guardrails;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /guardrails/{id}/assignments/members: the assignment count.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterGuardrailMembersAssignResponse extends OpenRouterResponse<OpenRouterGuardrailMembersAssignRequest> {


    OpenRouterGuardrailMembersAssignResponse(JSONObject json, OpenRouterGuardrailMembersAssignRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code assigned_count} - number of members successfully assigned.
 */
    public Integer assignedCount() {
        if (!json.has("assigned_count") || json.isNull("assigned_count")) {
            return null;
        }
        return (int) json.optLong("assigned_count");
    }
}
