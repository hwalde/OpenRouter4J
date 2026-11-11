package de.entwicklertraining.openrouter4j.guardrailpolicies;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The authoring configuration of a budget guardrail policy (spec
 * {@code GuardrailBudgetPolicyAuthoringConfig}): a per-subject USD spending
 * limit that resets on an interval. Budgets are per-subject - a member or key
 * subject to several budget policies is checked against every one of them
 * separately, there is no shared pool.
 * <p>
 * JSON field: {@code config} of the budget policy create/update requests and
 * of the {@code budget} leg of
 * {@link OpenRouterGuardrailPolicyRequest}. The schema declares
 * {@code additionalProperties: false}, so only the three typed keys exist.
 * All three are required on the wire; {@link Builder#build()} enforces that
 * loudly. {@code limit_usd} and {@code reset_interval} are nullable on the
 * wire - {@code null} is emitted as an explicit JSON {@code null} ("no
 * limit"), which is different from leaving a setter unset (the build then
 * fails for the missing required field).
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/guardrails/policies">Guardrail policies</a>
 */
public final class OpenRouterGuardrailBudgetPolicyConfig {

    private final Map<String, Object> values;

    private OpenRouterGuardrailBudgetPolicyConfig(Map<String, Object> values) {
        this.values = values;
    }

    /**
     * Creates a new builder for the budget policy authoring config. All three
     * fields must be set before {@link Builder#build()} succeeds.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the raw option value for {@code key}, or {@code null} when
     * unset - the read path for the three typed keys.
     */
    public Object option(String key) {
        return values.get(key);
    }

    /**
     * Returns the JSON object emitted as the {@code config} request field.
     * Set fields are emitted in their wire names; explicit {@code null}
     * values become JSON {@code null}s.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Object> e : values.entrySet()) {
            json.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return json;
    }

    /** Builder for {@link OpenRouterGuardrailBudgetPolicyConfig}. */
    public static final class Builder {

        private final Map<String, Object> values = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code limit_usd} (required): the spending limit in USD
         * ({@code > 0}, or {@code null} for "no limit" - the wire value is
         * then an explicit JSON {@code null}).
         */
        public Builder limitUsd(Double limitUsd) {
            if (limitUsd != null && limitUsd.doubleValue() <= 0) {
                throw new IllegalArgumentException("limitUsd must be greater than 0, was: " + limitUsd);
            }
            values.put("limit_usd", limitUsd);
            return this;
        }

        /**
         * Sets {@code reset_interval} (required): the interval at which the
         * limit resets - {@code "daily"}, {@code "weekly"} or {@code
         * "monthly"} (unknown spellings are passed through verbatim, the
         * schema allows unknown values), or {@code null} for no reset (the
         * wire value is then an explicit JSON {@code null}).
         */
        public Builder resetInterval(String resetInterval) {
            values.put("reset_interval", resetInterval);
            return this;
        }

        /**
         * Sets {@code include_byok_in_budgets} (required): whether BYOK
         * (bring-your-own-key) inference spend counts toward this policy's
         * {@code limit_usd} in addition to OpenRouter credit spend (the API
         * default is {@code false}, but the field is required on the wire).
         */
        public Builder includeByokInBudgets(Boolean includeByokInBudgets) {
            values.put("include_byok_in_budgets", includeByokInBudgets);
            return this;
        }

        /**
         * Builds the config value.
         *
         * @throws IllegalStateException when a required field is missing
         */
        public OpenRouterGuardrailBudgetPolicyConfig build() {
            if (!values.containsKey("limit_usd")) {
                throw new IllegalStateException("limit_usd is required (null is a valid value for \"no limit\")");
            }
            if (!values.containsKey("reset_interval")) {
                throw new IllegalStateException("reset_interval is required (null is a valid value for \"no reset\")");
            }
            if (!values.containsKey("include_byok_in_budgets")) {
                throw new IllegalStateException("include_byok_in_budgets is required");
            }
            return new OpenRouterGuardrailBudgetPolicyConfig(new LinkedHashMap<>(values));
        }
    }
}
