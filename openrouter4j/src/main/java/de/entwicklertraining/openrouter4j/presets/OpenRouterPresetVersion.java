package de.entwicklertraining.openrouter4j.presets;

import org.json.JSONObject;

/**
 * A shared view of one version of an OpenRouter preset (schema
 * {@code PresetDesignatedVersion}).
 *
 * <p>A version carries the stored request defaults: the {@code config}
 * object (whatever request fields were stored - e.g. {@code model},
 * {@code temperature}) and the optional {@code system_prompt}.
 */
public final class OpenRouterPresetVersion {

    private final JSONObject json;

    /**
     * Creates the view.
     *
     * @param json the raw version object
     */
    public OpenRouterPresetVersion(JSONObject json) {
        this.json = json;
    }

    /** @return the raw version object */
    public JSONObject json() {
        return json;
    }

    /** @return the JSON field {@code id} (UUID) */
    public String id() {
        return json.optString("id", null);
    }

    /** @return the JSON field {@code preset_id} (UUID) */
    public String presetId() {
        return json.optString("preset_id", null);
    }

    /** @return the JSON field {@code version} (1-based version number) */
    public Integer version() {
        if (!json.has("version") || json.isNull("version")) {
            return null;
        }
        Object value = json.get("version");
        return value instanceof Number number ? number.intValue() : null;
    }

    /** @return the raw {@code config} object (the stored request defaults), or {@code null} when absent */
    public JSONObject config() {
        return json.optJSONObject("config");
    }

    /** @return the JSON field {@code system_prompt}, or {@code null} when unset */
    public String systemPrompt() {
        return json.optString("system_prompt", null);
    }

    /** @return the JSON field {@code creator_id}, or {@code null} when unset */
    public String creatorId() {
        return json.optString("creator_id", null);
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
