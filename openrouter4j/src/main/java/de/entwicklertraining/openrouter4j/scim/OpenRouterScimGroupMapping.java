package de.entwicklertraining.openrouter4j.scim;

import org.json.JSONObject;

/**
 * A shared view of a SCIM group-to-workspace mapping (schema
 * {@code ScimGroupMapping}).
 *
 * <p>All accessors follow the swallow-and-return-null convention.
 */
public final class OpenRouterScimGroupMapping {

    private final JSONObject json;

    /**
     * Creates the view.
     *
     * @param json the raw mapping object
     */
    public OpenRouterScimGroupMapping(JSONObject json) {
        this.json = json;
    }

    /** @return the raw mapping object */
    public JSONObject json() {
        return json;
    }

    /** @return the JSON field {@code id} (UUID) - the identifier the get/update/delete endpoints use */
    public String id() {
        return json.optString("id", null);
    }

    /** @return the JSON field {@code organization_id} */
    public String organizationId() {
        return json.optString("organization_id", null);
    }

    /** @return the JSON field {@code scim_group_id} (UUID) */
    public String scimGroupId() {
        return json.optString("scim_group_id", null);
    }

    /** @return the JSON field {@code workspace_id} (UUID) */
    public String workspaceId() {
        return json.optString("workspace_id", null);
    }

    /** @return the JSON field {@code role} ({@code admin} or {@code member}) */
    public String role() {
        return json.optString("role", null);
    }

    /** @return the JSON field {@code created_at} */
    public String createdAt() {
        return json.optString("created_at", null);
    }

    /** @return the JSON field {@code updated_at} */
    public String updatedAt() {
        return json.optString("updated_at", null);
    }
}
