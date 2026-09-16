package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /workspaces/{id}: a single workspace.
 */
public final class OpenRouterWorkspaceGetResponse extends OpenRouterResponse<OpenRouterWorkspaceGetRequest> {


    OpenRouterWorkspaceGetResponse(JSONObject json, OpenRouterWorkspaceGetRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data} - the workspace.
 */
    public OpenRouterWorkspace data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterWorkspace(data) : null;
    }
}
