package de.entwicklertraining.openrouter4j.presets;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The response of a {@link OpenRouterPresetVersionsListRequest}: the
 * paginated version list plus {@code total_count}.
 *
 * <p>All accessors follow the swallow-and-return-null convention.
 */
public final class OpenRouterPresetVersionsListResponse extends OpenRouterResponse<OpenRouterPresetVersionsListRequest> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterPresetVersionsListResponse(JSONObject json, OpenRouterPresetVersionsListRequest request) {
        super(json, request);
    }

    /** @return the versions of the response, empty when absent */
    public List<OpenRouterPresetVersion> versions() {
        JSONArray array = json.optJSONArray("data");
        if (array == null) {
            return List.of();
        }
        List<OpenRouterPresetVersion> versions = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.optJSONObject(i);
            if (entry != null) {
                versions.add(new OpenRouterPresetVersion(entry));
            }
        }
        return versions;
    }

    /** @return the JSON field {@code total_count} */
    public Long totalCount() {
        if (!json.has("total_count") || json.isNull("total_count")) {
            return null;
        }
        Object value = json.get("total_count");
        return value instanceof Number number ? number.longValue() : null;
    }
}
