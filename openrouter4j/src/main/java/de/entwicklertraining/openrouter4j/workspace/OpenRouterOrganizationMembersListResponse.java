package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Response of GET /organization/members: the members of the organization.
 *
 * <p>Follows the swallow-and-return-empty convention.
 */
public final class OpenRouterOrganizationMembersListResponse extends OpenRouterResponse<OpenRouterOrganizationMembersListRequest> {


    OpenRouterOrganizationMembersListResponse(JSONObject json, OpenRouterOrganizationMembersListRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data[]} - the organization members as typed views, empty
 * when absent.
 */
    public List<OpenRouterOrganizationMember> items() {
        List<OpenRouterOrganizationMember> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterOrganizationMember(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
    /**
 * JSON path: {@code total_count} - total number of organization members.
 */
    public Integer totalCount() {
        if (!json.has("total_count") || json.isNull("total_count")) {
            return null;
        }
        return (int) json.optLong("total_count");
    }
}
