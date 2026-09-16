package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of DELETE /workspaces/{id}: the deletion confirmation.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterWorkspaceDeleteResponse extends OpenRouterResponse<OpenRouterWorkspaceDeleteRequest> {


    OpenRouterWorkspaceDeleteResponse(JSONObject json, OpenRouterWorkspaceDeleteRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code deleted} - confirmation that the workspace was deleted.
 */
    public Boolean deleted() {
        if (!json.has("deleted") || json.isNull("deleted")) {
            return null;
        }
        return json.optBoolean("deleted");
    }
}
