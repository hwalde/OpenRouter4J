package de.entwicklertraining.openrouter4j.presets;

import org.json.JSONObject;

/**
 * A shared view of an OpenRouter preset (schema {@code Preset} /
 * {@code PresetWithDesignatedVersion}).
 *
 * <p>A preset stores versioned request defaults that inference requests
 * reference via the {@code preset} body field or a {@code @preset/} model
 * id. All accessors follow the swallow-and-return-null convention.
 */
public final class OpenRouterPreset {

    private final JSONObject json;

    /**
     * Creates the view.
     *
     * @param json the raw preset object
     */
    public OpenRouterPreset(JSONObject json) {
        this.json = json;
    }

    /** @return the raw preset object */
    public JSONObject json() {
        return json;
    }

    /** @return the JSON field {@code id} (UUID) */
    public String id() {
        return json.optString("id", null);
    }

    /** @return the JSON field {@code name} */
    public String name() {
        return json.optString("name", null);
    }

    /** @return the JSON field {@code slug} - the identifier inference requests reference */
    public String slug() {
        return json.optString("slug", null);
    }

    /** @return the JSON field {@code description}, or {@code null} when unset */
    public String description() {
        return json.optString("description", null);
    }

    /** @return the JSON field {@code status} (e.g. {@code active}) */
    public String status() {
        return json.optString("status", null);
    }

    /** @return the JSON field {@code designated_version_id}, or {@code null} when unset */
    public String designatedVersionId() {
        return json.optString("designated_version_id", null);
    }

    /** @return the JSON field {@code creator_user_id}, or {@code null} when unset */
    public String creatorUserId() {
        return json.optString("creator_user_id", null);
    }

    /** @return the JSON field {@code workspace_id}, or {@code null} when unset */
    public String workspaceId() {
        return json.optString("workspace_id", null);
    }

    /** @return the JSON field {@code created_at} */
    public String createdAt() {
        return json.optString("created_at", null);
    }

    /** @return the JSON field {@code updated_at} */
    public String updatedAt() {
        return json.optString("updated_at", null);
    }

    /** @return the JSON field {@code status_updated_at}, or {@code null} when unset */
    public String statusUpdatedAt() {
        return json.optString("status_updated_at", null);
    }

    /**
     * @return the embedded {@code designated_version} object of a
     *         {@code PresetWithDesignatedVersion} payload, or {@code null}
     *         when the view was built from a plain {@code Preset} (e.g. in
     *         the list response)
     */
    public OpenRouterPresetVersion designatedVersion() {
        JSONObject version = json.optJSONObject("designated_version");
        return version == null ? null : new OpenRouterPresetVersion(version);
    }
}
