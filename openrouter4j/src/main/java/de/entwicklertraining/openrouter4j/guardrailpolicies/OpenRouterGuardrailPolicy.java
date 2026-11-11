package de.entwicklertraining.openrouter4j.guardrailpolicies;

import org.json.JSONObject;

/**
 * A typed view of one guardrail policy: the shared {@code id} / {@code name}
 * / {@code policy_type} / {@code config} core of every policy shape (spec
 * {@code BudgetGuardrailPolicy} and siblings - the typed create/update
 * answers). {@link OpenRouterGuardrailPolicySummary} and
 * {@link OpenRouterGuardrailPolicyDetail} extend this view.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor. The {@code config}
 * object is exposed raw (its shape depends on {@code policyType()}) - the
 * typed authoring builders {@link OpenRouterGuardrailBudgetPolicyConfig},
 * {@link OpenRouterGuardrailModelAvailabilityPolicyConfig} and
 * {@link OpenRouterGuardrailModerationPolicyConfig} document the keys.
 */
public class OpenRouterGuardrailPolicy {

    /** The raw JSON row behind this view. */
    protected final JSONObject json;

    /**
     * Wraps one policy object. A {@code null} argument is read as an empty
     * object - every accessor answers {@code null}.
     *
     * @param json the policy object
     */
    public OpenRouterGuardrailPolicy(JSONObject json) {
        this.json = json != null ? json : new JSONObject();
    }

    /**
     * @return the raw JSON row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code id} - the policy id (UUID).
     *
     * @return the id, or {@code null} when absent or not a string
     */
    public String id() {
        return optString("id");
    }

    /**
     * JSON path: {@code name} - the policy name (1-255 characters).
     *
     * @return the name, or {@code null} when absent or not a string
     */
    public String name() {
        return optString("name");
    }

    /**
     * JSON path: {@code policy_type} - the discriminator ({@code budget} /
     * {@code model_availability} / {@code moderation}), read verbatim.
     *
     * @return the policy type, or {@code null} when absent or not a string
     */
    public String policyType() {
        return optString("policy_type");
    }

    /**
     * JSON path: {@code config} - the policy configuration, raw (the shape
     * depends on {@link #policyType()}).
     *
     * @return the config object, or {@code null} when absent or not an object
     */
    public JSONObject config() {
        return json.optJSONObject("config");
    }

    /**
     * Returns a string field of this row, or {@code null} when absent or not
     * a string.
     *
     * @param key the JSON key
     * @return the value, or {@code null}
     */
    protected String optString(String key) {
        Object value = json.opt(key);
        return value instanceof String s ? s : null;
    }

    /**
     * Returns an integer field of this row, or {@code null} when absent or
     * not a number.
     *
     * @param key the JSON key
     * @return the value, or {@code null}
     */
    protected Integer optInt(String key) {
        Object value = json.opt(key);
        return value instanceof Number n ? n.intValue() : null;
    }

    /**
     * Returns a boolean field of this row, or {@code null} when absent or
     * not a boolean.
     *
     * @param key the JSON key
     * @return the value, or {@code null}
     */
    protected Boolean optBoolean(String key) {
        Object value = json.opt(key);
        return value instanceof Boolean b ? b : null;
    }
}
