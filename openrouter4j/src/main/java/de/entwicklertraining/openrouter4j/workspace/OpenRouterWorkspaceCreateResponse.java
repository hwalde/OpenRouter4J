package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /workspaces: the created workspace.
 */
public final class OpenRouterWorkspaceCreateResponse extends OpenRouterResponse<OpenRouterWorkspaceCreateRequest> {


    OpenRouterWorkspaceCreateResponse(JSONObject json, OpenRouterWorkspaceCreateRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data} - the created workspace.
 */
    public OpenRouterWorkspace data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterWorkspace(data) : null;
    }
}
