package de.entwicklertraining.openrouter4j.workspace;

import org.json.JSONObject;

/**
 * A typed view of one workspace membership (GET /workspaces/{id}/members and
 * the {@code data} of POST /workspaces/{id}/members/add): member, role and
 * timestamps.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 */
public final class OpenRouterWorkspaceMember {

    private final JSONObject json;

    OpenRouterWorkspaceMember(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw JSON row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
 * JSON path: {@code id} - unique identifier of the workspace membership.
 */
    public String id() {
        return json.optString("id", null);
    }
    /**
 * JSON path: {@code workspace_id} - id of the workspace.
 */
    public String workspaceId() {
        return json.optString("workspace_id", null);
    }
    /**
 * JSON path: {@code user_id} - Clerk user id of the member; the same id is
 * used by the members add/remove request bodies.
 */
    public String userId() {
        return json.optString("user_id", null);
    }
    /**
 * JSON path: {@code role} - role of the member in the workspace ({@code admin}
 * or {@code member}); members inherit their organization role when added.
 */
    public String role() {
        return json.optString("role", null);
    }
    /**
 * JSON path: {@code created_at} - ISO 8601 timestamp of when the membership was
 * created.
 */
    public String createdAt() {
        return json.optString("created_at", null);
    }
}
