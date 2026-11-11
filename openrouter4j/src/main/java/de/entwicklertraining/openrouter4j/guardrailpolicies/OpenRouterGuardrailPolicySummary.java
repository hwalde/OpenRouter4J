package de.entwicklertraining.openrouter4j.guardrailpolicies;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A typed view of one guardrail policy list row (spec
 * {@code BudgetGuardrailPolicySummary} and its model-availability /
 * moderation siblings - the {@code data[]} rows of the typed list
 * responses): the {@link OpenRouterGuardrailPolicy} core plus the list-level
 * counts and the v1-guardrail migration markers.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/-empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterGuardrailPolicySummary extends OpenRouterGuardrailPolicy {

    /**
     * Wraps one list row. A {@code null} argument is read as an empty object.
     *
     * @param json the list row
     */
    public OpenRouterGuardrailPolicySummary(JSONObject json) {
        super(json);
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
     * JSON path: {@code member_count} - the number of members the policy is
     * assigned to.
     *
     * @return the count, or {@code null} when absent or not a number
     */
    public Integer memberCount() {
        return optInt("member_count");
    }

    /**
     * JSON path: {@code api_key_count} - the number of API keys the policy is
     * assigned to.
     *
     * @return the count, or {@code null} when absent or not a number
     */
    public Integer apiKeyCount() {
        return optInt("api_key_count");
    }

    /**
     * JSON path: {@code is_workspace_default} - whether the policy is the
     * workspace default for its type.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean isWorkspaceDefault() {
        return optBoolean("is_workspace_default");
    }

    /**
     * JSON path: {@code is_legacy_mapped} - whether the policy is kept in
     * sync with a v1 guardrail (saving changes creates a copy).
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean isLegacyMapped() {
        return optBoolean("is_legacy_mapped");
    }

    /**
     * JSON path: {@code legacy_guardrail_ids} - the ids of the v1 guardrails
     * this policy was migrated from.
     *
     * @return the ids, empty when absent (non-string entries dropped)
     */
    public List<String> legacyGuardrailIds() {
        return stringList("legacy_guardrail_ids");
    }

    /**
     * JSON path: {@code replaced_legacy_guardrail_ids} - the ids of the v1
     * guardrails this policy replaced.
     *
     * @return the ids, empty when absent (non-string entries dropped)
     */
    public List<String> replacedLegacyGuardrailIds() {
        return stringList("replaced_legacy_guardrail_ids");
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
