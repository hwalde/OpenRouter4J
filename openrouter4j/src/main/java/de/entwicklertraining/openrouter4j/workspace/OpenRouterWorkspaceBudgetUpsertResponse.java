package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of PUT /workspaces/{ref}/budgets/{interval}: the upserted budget.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterWorkspaceBudgetUpsertResponse extends OpenRouterResponse<OpenRouterWorkspaceBudgetUpsertRequest> {


    OpenRouterWorkspaceBudgetUpsertResponse(JSONObject json, OpenRouterWorkspaceBudgetUpsertRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data} - the upserted budget.
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
