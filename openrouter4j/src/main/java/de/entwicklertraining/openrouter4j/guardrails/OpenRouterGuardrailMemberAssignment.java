package de.entwicklertraining.openrouter4j.guardrails;

import org.json.JSONObject;

/**
 * A typed view of one guardrail member assignment (GET
 * /guardrails/assignments/members and GET
 * /guardrails/{id}/assignments/members): which member is governed by which
 * guardrail.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 */
public final class OpenRouterGuardrailMemberAssignment {

    private final JSONObject json;

    OpenRouterGuardrailMemberAssignment(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw JSON row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
 * JSON path: {@code id} - unique identifier of the assignment.
 */
    public String id() {
        return json.optString("id", null);
    }
    /**
 * JSON path: {@code user_id} - Clerk user id of the assigned member.
 */
    public String userId() {
        return json.optString("user_id", null);
    }
    /**
 * JSON path: {@code guardrail_id} - id of the guardrail.
 */
    public String guardrailId() {
        return json.optString("guardrail_id", null);
    }
    /**
 * JSON path: {@code organization_id} - id of the organization.
 */
    public String organizationId() {
        return json.optString("organization_id", null);
    }
    /**
 * JSON path: {@code assigned_by} - user id of who made the assignment, or
 * {@code null} when absent.
 */
    public String assignedBy() {
        return json.optString("assigned_by", null);
    }
    /**
 * JSON path: {@code created_at} - ISO 8601 timestamp of when the assignment was
 * created.
 */
    public String createdAt() {
        return json.optString("created_at", null);
    }
}
