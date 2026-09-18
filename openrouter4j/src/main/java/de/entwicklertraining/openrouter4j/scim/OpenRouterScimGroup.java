package de.entwicklertraining.openrouter4j.scim;

import org.json.JSONObject;

/**
 * A shared view of a SCIM group of the organization (schema
 * {@code ScimGroup}).
 *
 * <p>All accessors follow the swallow-and-return-null convention.
 */
public final class OpenRouterScimGroup {

    private final JSONObject json;

    /**
     * Creates the view.
     *
     * @param json the raw group object
     */
    public OpenRouterScimGroup(JSONObject json) {
        this.json = json;
    }

    /** @return the raw group object */
    public JSONObject json() {
        return json;
    }

    /** @return the JSON field {@code id} (UUID) - the {@code scim_group_id} of the mapping endpoints */
    public String id() {
        return json.optString("id", null);
    }

    /** @return the JSON field {@code organization_id} */
    public String organizationId() {
        return json.optString("organization_id", null);
    }

    /** @return the JSON field {@code display_name} */
    public String displayName() {
        return json.optString("display_name", null);
    }

    /** @return the JSON field {@code external_id}, or {@code null} when unset */
    public String externalId() {
        return json.optString("external_id", null);
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
