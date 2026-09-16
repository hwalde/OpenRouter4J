package de.entwicklertraining.openrouter4j.guardrails;

import org.json.JSONObject;

/**
 * A typed view of one guardrail key assignment (GET /guardrails/assignments/keys
 * and GET /guardrails/{id}/assignments/keys): which API key is governed by which
 * guardrail.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 */
public final class OpenRouterGuardrailKeyAssignment {

    private final JSONObject json;

    OpenRouterGuardrailKeyAssignment(JSONObject json) {
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
 * JSON path: {@code key_hash} - hash of the assigned API key.
 */
    public String keyHash() {
        return json.optString("key_hash", null);
    }
    /**
 * JSON path: {@code key_label} - masked label of the assigned API key, or
 * {@code null} when absent.
 */
    public String keyLabel() {
        return json.optString("key_label", null);
    }
    /**
 * JSON path: {@code key_name} - name of the assigned API key, or {@code null}
 * when absent.
 */
    public String keyName() {
        return json.optString("key_name", null);
    }
    /**
 * JSON path: {@code guardrail_id} - id of the guardrail.
 */
    public String guardrailId() {
        return json.optString("guardrail_id", null);
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
