package de.entwicklertraining.openrouter4j.presets;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The response of a {@link OpenRouterPresetGetRequest}: the preset with its
 * currently designated version (schema {@code PresetWithDesignatedVersion}).
 *
 * <p>All accessors follow the swallow-and-return-null convention.
 */
public final class OpenRouterPresetGetResponse extends OpenRouterResponse<OpenRouterPresetGetRequest> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterPresetGetResponse(JSONObject json, OpenRouterPresetGetRequest request) {
        super(json, request);
    }

    /** @return the preset, or {@code null} when the {@code data} object is absent */
    public OpenRouterPreset preset() {
        JSONObject data = json.optJSONObject("data");
        return data == null ? null : new OpenRouterPreset(data);
    }
}
