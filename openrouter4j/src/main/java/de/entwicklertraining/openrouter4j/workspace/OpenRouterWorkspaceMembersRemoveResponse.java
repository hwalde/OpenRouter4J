package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /workspaces/{id}/members/remove: the removal count.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterWorkspaceMembersRemoveResponse extends OpenRouterResponse<OpenRouterWorkspaceMembersRemoveRequest> {


    OpenRouterWorkspaceMembersRemoveResponse(JSONObject json, OpenRouterWorkspaceMembersRemoveRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code removed_count} - number of members removed.
 */
    public Integer removedCount() {
        if (!json.has("removed_count") || json.isNull("removed_count")) {
            return null;
        }
        return (int) json.optLong("removed_count");
    }
}
