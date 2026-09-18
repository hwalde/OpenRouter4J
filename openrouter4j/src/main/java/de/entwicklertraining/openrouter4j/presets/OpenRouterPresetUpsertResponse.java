package de.entwicklertraining.openrouter4j.presets;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The shared response of the preset create/update routes
 * (POST /presets/{slug}/chat/completions, POST /presets/{slug}/messages and
 * POST /presets/{slug}/responses): the created or updated preset with its
 * newly designated version (schema {@code CreatePresetFromInferenceResponse}).
 *
 * <p>All accessors follow the swallow-and-return-null convention.
 */
public final class OpenRouterPresetUpsertResponse<T extends OpenRouterRequest<?>> extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterPresetUpsertResponse(JSONObject json, T request) {
        super(json, request);
    }

    /** @return the created or updated preset, or {@code null} when the {@code data} object is absent */
    public OpenRouterPreset preset() {
        JSONObject data = json.optJSONObject("data");
        return data == null ? null : new OpenRouterPreset(data);
    }
}
