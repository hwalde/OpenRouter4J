package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Response of GET /workspaces/{ref}/budgets: the budgets of the workspace.
 *
 * <p>Follows the swallow-and-return-empty convention.
 */
public final class OpenRouterWorkspaceBudgetsListResponse extends OpenRouterResponse<OpenRouterWorkspaceBudgetsListRequest> {


    OpenRouterWorkspaceBudgetsListResponse(JSONObject json, OpenRouterWorkspaceBudgetsListRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data[]} - the budgets as typed views, empty when absent.
 */
    public List<OpenRouterWorkspaceBudget> items() {
        List<OpenRouterWorkspaceBudget> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterWorkspaceBudget(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
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
