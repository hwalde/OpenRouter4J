package de.entwicklertraining.openrouter4j.scim;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The response of a {@link OpenRouterScimGroupsListRequest}: the paginated
 * group list plus {@code total_count}.
 *
 * <p>All accessors follow the swallow-and-return-null convention.
 */
public final class OpenRouterScimGroupsListResponse extends OpenRouterResponse<OpenRouterScimGroupsListRequest> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterScimGroupsListResponse(JSONObject json, OpenRouterScimGroupsListRequest request) {
        super(json, request);
    }

    /** @return the SCIM groups of the response, empty when absent */
    public List<OpenRouterScimGroup> groups() {
        JSONArray array = json.optJSONArray("data");
        if (array == null) {
            return List.of();
        }
        List<OpenRouterScimGroup> groups = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject entry = array.optJSONObject(i);
            if (entry != null) {
                groups.add(new OpenRouterScimGroup(entry));
            }
        }
        return groups;
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
