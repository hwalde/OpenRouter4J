package de.entwicklertraining.openrouter4j.guardrailpolicies;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A typed view of one workspace-wide (account-level) guardrail policy (spec
 * {@code BudgetAccountGuardrailPolicy} and its model-availability /
 * moderation siblings - the {@code data} object of the account policy
 * responses and the {@code data[]} rows of the account policy list): the
 * {@link OpenRouterGuardrailPolicy} core plus the account-level workspace
 * coverage and the v1-guardrail sync markers.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/-empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterGuardrailAccountPolicy extends OpenRouterGuardrailPolicy {

    /**
     * Wraps one account policy object. A {@code null} argument is read as an
     * empty object.
     *
     * @param json the account policy object
     */
    public OpenRouterGuardrailAccountPolicy(JSONObject json) {
        super(json);
    }

    /**
     * JSON path: {@code created_at} - the creation timestamp (ISO-8601).
     *
     * @return the timestamp, or {@code null} when absent or not a string
     */
    public String createdAt() {
        return optString("created_at");
    }

    /**
     * JSON path: {@code updated_at} - the last update timestamp (ISO-8601),
     * {@code null} when never updated.
     *
     * @return the timestamp, or {@code null} when absent or not a string
     */
    public String updatedAt() {
        return optString("updated_at");
    }

    /**
     * JSON path: {@code is_default} - whether the policy covers every
     * workspace in the account except {@link #optedOutWorkspaceIds()}.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean isDefault() {
        return optBoolean("is_default");
    }

    /**
     * JSON path: {@code workspace_count} - the number of workspaces the
     * policy covers.
     *
     * @return the count, or {@code null} when absent or not a number
     */
    public Integer workspaceCount() {
        return optInt("workspace_count");
    }

    /**
     * JSON path: {@code workspace_ids} - the workspaces the policy covers.
     *
     * @return the ids, empty when absent (non-string entries dropped)
     */
    public List<String> workspaceIds() {
        return stringList("workspace_ids");
    }

    /**
     * JSON path: {@code opted_out_workspace_ids} - the workspaces left out of
     * a default policy.
     *
     * @return the ids, empty when absent (non-string entries dropped)
     */
    public List<String> optedOutWorkspaceIds() {
        return stringList("opted_out_workspace_ids");
    }

    /**
     * JSON path: {@code is_synced} - whether the policy is kept in sync with
     * a v1 guardrail (saving changes creates an account copy).
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean isSynced() {
        return optBoolean("is_synced");
    }

    /**
     * JSON path: {@code is_workspace_default_synced} - whether the policy was
     * synced from a v1 workspace default guardrail.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean isWorkspaceDefaultSynced() {
        return optBoolean("is_workspace_default_synced");
    }

    private List<String> stringList(String key) {
        List<String> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray(key);
            if (arr == null) {
                return result;
            }
            for (int i = 0; i < arr.length(); i++) {
                Object value = arr.opt(i);
                if (value instanceof String s) {
                    result.add(s);
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
}
