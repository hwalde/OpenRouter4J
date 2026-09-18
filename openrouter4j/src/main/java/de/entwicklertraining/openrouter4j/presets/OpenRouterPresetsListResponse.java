package de.entwicklertraining.openrouter4j.presets;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The response of a {@link OpenRouterPresetsListRequest}: the paginated
 * preset list plus {@code total_count}.
 *
 * <p>All accessors follow the swallow-and-return-null convention.
 */
public final class OpenRouterPresetsListResponse extends OpenRouterResponse<OpenRouterPresetsListRequest> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterPresetsListResponse(JSONObject json, OpenRouterPresetsListRequest request) {
        super(json, request);
    }

    /** @return the presets of the response, empty when absent */
    public List<OpenRouterPreset> presets() {
        return listOfObjects("data").stream().map(OpenRouterPreset::new).toList();
    }

    /** @return the JSON field {@code total_count} */
    public Long totalCount() {
        return optLong("total_count");
    }

    List<JSONObject> listOfObjects(String key) {
        JSONArray array = json.optJSONArray(key);
        if (array == null) {
            return List.of();
        }
        List<JSONObject> entries = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.optJSONObject(i);
            if (entry != null) {
                entries.add(entry);
            }
        }
        return entries;
    }

    Long optLong(String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        Object value = json.get(key);
        return value instanceof Number number ? number.longValue() : null;
    }
}
