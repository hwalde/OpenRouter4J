package de.entwicklertraining.openrouter4j.guardrailpolicies;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The assignment state of one subject (API key or organization member) for
 * one policy type in one workspace (spec {@code GuardrailPolicyAssignmentState})
 * - both the typed view of the assignment read responses and the request
 * value of the {@code expected_assignment} optimistic-concurrency field of
 * the assignment change requests (the {@code OpenRouterPrivateEndpointPricing}
 * both-directions pattern): the change request fails with HTTP 409 when the
 * stored state changed since this state was read.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/-empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterGuardrailPolicyAssignmentState {

    private final JSONObject json;

    /**
     * Wraps one assignment-state object. A {@code null} argument is read as
     * an empty object - every accessor answers {@code null}/{@code empty}.
     *
     * @param json the {@code GuardrailPolicyAssignmentState} object
     */
    public OpenRouterGuardrailPolicyAssignmentState(JSONObject json) {
        this.json = json != null ? json : new JSONObject();
    }

    /**
     * Builds an assignment-state request value for
     * {@code expected_assignment}.
     *
     * @param policyIds the policies of this type assigned directly to the
     *        subject (may be empty)
     * @param workspaceDefaultPolicyIds the workspace default policies of this
     *        type the subject inherits (may be empty)
     * @param optedOut whether the subject is opted out of the workspace
     *        default
     * @return the state value
     */
    public static OpenRouterGuardrailPolicyAssignmentState of(
            List<String> policyIds, List<String> workspaceDefaultPolicyIds, boolean optedOut) {
        return new OpenRouterGuardrailPolicyAssignmentState(new JSONObject()
                .put("policy_ids", new JSONArray(policyIds == null ? List.of() : policyIds))
                .put("workspace_default_policy_ids",
                        new JSONArray(workspaceDefaultPolicyIds == null ? List.of() : workspaceDefaultPolicyIds))
                .put("opted_out", optedOut));
    }

    /**
     * JSON path: {@code policy_ids} - the policies of this type assigned
     * directly to the subject.
     *
     * @return the ids, empty when absent (non-string entries dropped)
     */
    public List<String> policyIds() {
        return stringList("policy_ids");
    }

    /**
     * JSON path: {@code workspace_default_policy_ids} - the workspace default
     * policies of this type the subject inherits.
     *
     * @return the ids, empty when absent (non-string entries dropped)
     */
    public List<String> workspaceDefaultPolicyIds() {
        return stringList("workspace_default_policy_ids");
    }

    /**
     * JSON path: {@code opted_out} - whether the subject is opted out of the
     * workspace default.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean optedOut() {
        Object value = json.opt("opted_out");
        return value instanceof Boolean b ? b : null;
    }

    /**
     * @return the raw JSON object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * Returns the JSON object emitted as {@code expected_assignment}. The
     * result is a detached copy per call.
     */
    public JSONObject toJson() {
        return new JSONObject(json.toString());
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
