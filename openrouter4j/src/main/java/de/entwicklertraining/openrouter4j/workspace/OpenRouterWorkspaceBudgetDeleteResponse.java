package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of DELETE /workspaces/{ref}/budgets/{interval}: the deletion
 * confirmation.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterWorkspaceBudgetDeleteResponse extends OpenRouterResponse<OpenRouterWorkspaceBudgetDeleteRequest> {


    OpenRouterWorkspaceBudgetDeleteResponse(JSONObject json, OpenRouterWorkspaceBudgetDeleteRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code deleted} - confirmation that the budget was deleted (or did
 * not exist).
 */
    public Boolean deleted() {
        if (!json.has("deleted") || json.isNull("deleted")) {
            return null;
        }
        return json.optBoolean("deleted");
    }
}
