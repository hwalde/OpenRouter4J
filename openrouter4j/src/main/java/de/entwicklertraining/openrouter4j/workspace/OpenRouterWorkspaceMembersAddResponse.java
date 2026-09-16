package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Response of POST /workspaces/{id}/members/add: the memberships created or
 * updated.
 *
 * <p>Follows the swallow-and-return-empty convention.
 */
public final class OpenRouterWorkspaceMembersAddResponse extends OpenRouterResponse<OpenRouterWorkspaceMembersAddRequest> {


    OpenRouterWorkspaceMembersAddResponse(JSONObject json, OpenRouterWorkspaceMembersAddRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code added_count} - number of memberships created or updated.
 */
    public Integer addedCount() {
        if (!json.has("added_count") || json.isNull("added_count")) {
            return null;
        }
        return (int) json.optLong("added_count");
    }
    /**
 * JSON path: {@code data[]} - the added memberships as typed views, empty when
 * absent.
 */
    public List<OpenRouterWorkspaceMember> items() {
        List<OpenRouterWorkspaceMember> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterWorkspaceMember(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
}
