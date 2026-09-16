package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /workspaces/{ref}/budgets/{interval}: a single budget.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterWorkspaceBudgetGetResponse extends OpenRouterResponse<OpenRouterWorkspaceBudgetGetRequest> {


    OpenRouterWorkspaceBudgetGetResponse(JSONObject json, OpenRouterWorkspaceBudgetGetRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data} - the budget.
 */
    public OpenRouterWorkspaceBudget data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterWorkspaceBudget(data) : null;
    }
    /**
 * JSON path: {@code include_byok_in_budgets} - whether BYOK spend is included
 * when enforcing the workspace's budgets.
 */
    public Boolean includeByokInBudgets() {
        if (!json.has("include_byok_in_budgets") || json.isNull("include_byok_in_budgets")) {
            return null;
        }
        return json.optBoolean("include_byok_in_budgets");
    }
}
