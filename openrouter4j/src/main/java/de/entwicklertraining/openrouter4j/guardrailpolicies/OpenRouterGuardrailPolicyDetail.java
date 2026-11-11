package de.entwicklertraining.openrouter4j.guardrailpolicies;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A typed view of one guardrail policy detail (spec
 * {@code BudgetGuardrailPolicyDetail} and its model-availability /
 * moderation siblings - the {@code data} object of the typed get responses):
 * the {@link OpenRouterGuardrailPolicy} core plus the assignment lists and
 * the v1-guardrail migration markers.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/-empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterGuardrailPolicyDetail extends OpenRouterGuardrailPolicy {

    /**
     * Wraps one detail object. A {@code null} argument is read as an empty
     * object.
     *
     * @param json the detail object
     */
    public OpenRouterGuardrailPolicyDetail(JSONObject json) {
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
     * JSON path: {@code member_ids} - the organization member user ids the
     * policy is assigned to.
     *
     * @return the ids, empty when absent (non-string entries dropped)
     */
    public List<String> memberIds() {
        return stringList("member_ids");
    }

    /**
     * JSON path: {@code api_key_hashes} - the API key hashes the policy is
     * assigned to.
     *
     * @return the hashes, empty when absent (non-string entries dropped)
     */
    public List<String> apiKeyHashes() {
        return stringList("api_key_hashes");
    }

    /**
     * JSON path: {@code opted_out_member_ids} - the members that do not
     * inherit the policy when it is the workspace default.
     *
     * @return the ids, empty when absent (non-string entries dropped)
     */
    public List<String> optedOutMemberIds() {
        return stringList("opted_out_member_ids");
    }

    /**
     * JSON path: {@code opted_out_api_key_hashes} - the API keys that do not
     * inherit the policy when it is the workspace default.
     *
     * @return the hashes, empty when absent (non-string entries dropped)
     */
    public List<String> optedOutApiKeyHashes() {
        return stringList("opted_out_api_key_hashes");
    }

    /**
     * JSON path: {@code overridden_member_ids} - the members that have a
     * different policy of this type assigned directly.
     *
     * @return the ids, empty when absent (non-string entries dropped)
     */
    public List<String> overriddenMemberIds() {
        return stringList("overridden_member_ids");
    }

    /**
     * JSON path: {@code overridden_api_key_hashes} - the API keys that have a
     * different policy of this type assigned directly.
     *
     * @return the hashes, empty when absent (non-string entries dropped)
     */
    public List<String> overriddenApiKeyHashes() {
        return stringList("overridden_api_key_hashes");
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
     * JSON path: {@code reference} - {@code "legacy_mapping"} when the policy
     * is synced from a v1 guardrail, {@code "account_default"} when it is an
     * account-wide default, otherwise {@code null}; unknown values are read
     * verbatim.
     *
     * @return the reference, or {@code null} when absent or not a string
     */
    public String reference() {
        return optString("reference");
    }

    /**
     * JSON path: {@code replaces_legacy_default} - whether this workspace
     * copy replaced a v1 workspace default guardrail that still exists (it
     * cannot be deleted).
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean replacesLegacyDefault() {
        return optBoolean("replaces_legacy_default");
    }

    /**
     * JSON path: {@code is_account_policy} - whether this is an account-owned
     * policy rather than a workspace policy (present on the moderation
     * detail shape in the current spec; {@code null} elsewhere).
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean isAccountPolicy() {
        return optBoolean("is_account_policy");
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
