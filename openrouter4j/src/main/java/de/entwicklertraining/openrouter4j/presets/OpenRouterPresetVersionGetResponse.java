package de.entwicklertraining.openrouter4j.presets;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The response of a {@link OpenRouterPresetVersionGetRequest}: one specific
 * version of a preset.
 *
 * <p>All accessors follow the swallow-and-return-null convention.
 */
public final class OpenRouterPresetVersionGetResponse extends OpenRouterResponse<OpenRouterPresetVersionGetRequest> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterPresetVersionGetResponse(JSONObject json, OpenRouterPresetVersionGetRequest request) {
        super(json, request);
    }

    /** @return the preset version, or {@code null} when the {@code data} object is absent */
    public OpenRouterPresetVersion version() {
        JSONObject data = json.optJSONObject("data");
        return data == null ? null : new OpenRouterPresetVersion(data);
    }
}
