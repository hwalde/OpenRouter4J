package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of PATCH /workspaces/{id}: the updated workspace.
 */
public final class OpenRouterWorkspaceUpdateResponse extends OpenRouterResponse<OpenRouterWorkspaceUpdateRequest> {


    OpenRouterWorkspaceUpdateResponse(JSONObject json, OpenRouterWorkspaceUpdateRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data} - the updated workspace.
 */
    public OpenRouterWorkspace data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterWorkspace(data) : null;
    }
}
