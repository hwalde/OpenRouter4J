package de.entwicklertraining.openrouter4j.guardrailpolicies;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The authoring configuration of a model-availability guardrail policy (spec
 * {@code GuardrailModelAvailabilityPolicyAuthoringConfig}): which models,
 * providers and data regions requests may use, plus the ZDR / Safety
 * Retention / training switches. This is the policy-level counterpart of the
 * same fields on the monolithic guardrail surface
 * ({@code client.guardrails().create()}).
 * <p>
 * JSON field: {@code config} of the model-availability policy create/update
 * requests and of the {@code model_availability} leg of
 * {@link OpenRouterGuardrailPolicyRequest}. The schema declares
 * {@code additionalProperties: false}, so only the typed keys exist - there
 * is deliberately no verbatim hatch. Every field is optional: emitted only
 * when set, {@code null} unsets it again (the wire types are nullable and
 * an absent field means "no restriction"/"inherit"). List values are emitted
 * as JSON arrays verbatim.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/guardrails/policies">Guardrail policies</a>
 */
public final class OpenRouterGuardrailModelAvailabilityPolicyConfig {

    private final Map<String, Object> values;

    private OpenRouterGuardrailModelAvailabilityPolicyConfig(Map<String, Object> values) {
        this.values = values;
    }

    /**
     * Creates a new, empty builder for the model-availability policy config.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the raw option value for {@code key}, or {@code null} when
     * unset - the read path for the typed keys.
     */
    public Object option(String key) {
        return values.get(key);
    }

    /**
     * Returns the JSON object emitted as the {@code config} request field.
     * Set fields are emitted in their wire names; unset fields are absent.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Object> e : values.entrySet()) {
            Object value = e.getValue();
            if (value instanceof List<?> list) {
                JSONArray arr = new JSONArray();
                for (Object item : list) {
                    arr.put(item == null ? JSONObject.NULL : item);
                }
                json.put(e.getKey(), arr);
            } else {
                json.put(e.getKey(), value == null ? JSONObject.NULL : value);
            }
        }
        return json;
    }

    /** Builder for {@link OpenRouterGuardrailModelAvailabilityPolicyConfig}. */
    public static final class Builder {

        private final Map<String, Object> values = new LinkedHashMap<>();

        private Builder() {
        }

        /** Sets {@code allowed_models}: the models requests may use. {@code null} unsets. */
        public Builder allowedModels(List<String> allowedModels) {
            set("allowed_models", allowedModels);
            return this;
        }

        /** Sets {@code ignored_models}: the models requests may not use. {@code null} unsets. */
        public Builder ignoredModels(List<String> ignoredModels) {
            set("ignored_models", ignoredModels);
            return this;
        }

        /** Sets {@code allowed_providers}: the providers requests may use. {@code null} unsets. */
        public Builder allowedProviders(List<String> allowedProviders) {
            set("allowed_providers", allowedProviders);
            return this;
        }

        /** Sets {@code ignored_providers}: the providers requests may not use. {@code null} unsets. */
        public Builder ignoredProviders(List<String> ignoredProviders) {
            set("ignored_providers", ignoredProviders);
            return this;
        }

        /** Sets {@code allowed_data_regions}: the data regions requests may use (min 1 entry). {@code null} unsets. */
        public Builder allowedDataRegions(List<String> allowedDataRegions) {
            set("allowed_data_regions", allowedDataRegions);
            return this;
        }

        /** Sets {@code require_eu_data_region}. {@code null} unsets. */
        public Builder requireEuDataRegion(Boolean requireEuDataRegion) {
            set("require_eu_data_region", requireEuDataRegion);
            return this;
        }

        /** Sets {@code enforce_zdr}: require Zero Data Retention endpoints. {@code null} unsets. */
        public Builder enforceZdr(Boolean enforceZdr) {
            set("enforce_zdr", enforceZdr);
            return this;
        }

        /** Sets {@code enforce_zdr_anthropic}. {@code null} unsets. */
        public Builder enforceZdrAnthropic(Boolean enforceZdrAnthropic) {
            set("enforce_zdr_anthropic", enforceZdrAnthropic);
            return this;
        }

        /** Sets {@code enforce_zdr_google}. {@code null} unsets. */
        public Builder enforceZdrGoogle(Boolean enforceZdrGoogle) {
            set("enforce_zdr_google", enforceZdrGoogle);
            return this;
        }

        /** Sets {@code enforce_zdr_openai}. {@code null} unsets. */
        public Builder enforceZdrOpenai(Boolean enforceZdrOpenai) {
            set("enforce_zdr_openai", enforceZdrOpenai);
            return this;
        }

        /** Sets {@code enforce_zdr_xai}. {@code null} unsets. */
        public Builder enforceZdrXai(Boolean enforceZdrXai) {
            set("enforce_zdr_xai", enforceZdrXai);
            return this;
        }

        /** Sets {@code enforce_zdr_other}: ZDR enforcement outside the named vendors. {@code null} unsets. */
        public Builder enforceZdrOther(Boolean enforceZdrOther) {
            set("enforce_zdr_other", enforceZdrOther);
            return this;
        }

        /**
         * Sets {@code allow_safety_retention_google}: whether ZDR-only
         * requests to {@code google/*} models may also use Safety Retention
         * endpoints (which may retain prompts and responses only to prevent
         * abuse and do not use them for training). {@code null} unsets.
         */
        public Builder allowSafetyRetentionGoogle(Boolean allowSafetyRetentionGoogle) {
            set("allow_safety_retention_google", allowSafetyRetentionGoogle);
            return this;
        }

        /**
         * Sets {@code allow_safety_retention_other}: the same for models
         * outside Google that have Safety Retention endpoints (e.g. open-weight
         * models on Vertex). {@code null} unsets.
         */
        public Builder allowSafetyRetentionOther(Boolean allowSafetyRetentionOther) {
            set("allow_safety_retention_other", allowSafetyRetentionOther);
            return this;
        }

        /** Sets {@code enforce_first_party_harness_models}: coding harnesses list only their vendor's own models. {@code null} unsets. */
        public Builder enforceFirstPartyHarnessModels(Boolean enforceFirstPartyHarnessModels) {
            set("enforce_first_party_harness_models", enforceFirstPartyHarnessModels);
            return this;
        }

        /** Sets {@code enable_free_model_training}. {@code null} unsets. */
        public Builder enableFreeModelTraining(Boolean enableFreeModelTraining) {
            set("enable_free_model_training", enableFreeModelTraining);
            return this;
        }

        /** Sets {@code enable_free_model_publication}. {@code null} unsets. */
        public Builder enableFreeModelPublication(Boolean enableFreeModelPublication) {
            set("enable_free_model_publication", enableFreeModelPublication);
            return this;
        }

        /** Sets {@code enable_paid_model_training}. {@code null} unsets. */
        public Builder enablePaidModelTraining(Boolean enablePaidModelTraining) {
            set("enable_paid_model_training", enablePaidModelTraining);
            return this;
        }

        /** Sets {@code platform_banned_authors}. {@code null} unsets. */
        public Builder platformBannedAuthors(List<String> platformBannedAuthors) {
            set("platform_banned_authors", platformBannedAuthors);
            return this;
        }

        /** Sets {@code platform_banned_models}. {@code null} unsets. */
        public Builder platformBannedModels(List<String> platformBannedModels) {
            set("platform_banned_models", platformBannedModels);
            return this;
        }

        /** Sets {@code platform_banned_providers}. {@code null} unsets. */
        public Builder platformBannedProviders(List<String> platformBannedProviders) {
            set("platform_banned_providers", platformBannedProviders);
            return this;
        }

        private void set(String key, Object value) {
            if (value == null) {
                values.remove(key);
            } else {
                values.put(key, value);
            }
        }

        /**
         * Builds the config value. Nothing is required - an empty config is
         * valid and means "no restriction".
         */
        public OpenRouterGuardrailModelAvailabilityPolicyConfig build() {
            return new OpenRouterGuardrailModelAvailabilityPolicyConfig(new LinkedHashMap<>(values));
        }
    }
}
