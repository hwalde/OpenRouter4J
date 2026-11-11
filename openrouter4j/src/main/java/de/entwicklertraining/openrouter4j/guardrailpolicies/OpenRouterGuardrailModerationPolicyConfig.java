package de.entwicklertraining.openrouter4j.guardrailpolicies;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The authoring configuration of a moderation guardrail policy (spec
 * {@code GuardrailModerationPolicyConfig}): content filters, PII filters and
 * the Model Armor / Lakera injection-protection switches evaluated on the
 * request path. Combining several moderation policies follows the documented
 * rule: the filters of every applied policy are unioned and {@code block}
 * beats {@code redact}.
 * <p>
 * JSON field: {@code config} of the moderation policy create/update requests
 * and of the {@code moderation} leg of
 * {@link OpenRouterGuardrailPolicyRequest}. The schema declares
 * {@code additionalProperties: false}, so only the typed keys exist - there
 * is deliberately no verbatim hatch on the config object itself. Every field
 * is optional and emitted only when set ({@code null} unsets it again).
 * <p>
 * The entry lists are emitted as JSON arrays of entry objects; the typed
 * {@code add*} methods build the documented entry shapes, and the raw
 * {@code add*Raw(JSONObject)} variants cover entries verbatim (each entry
 * declares {@code additionalProperties: false} as well, so unknown keys are
 * the API's business).
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/guardrails/policies">Guardrail policies</a>
 */
public final class OpenRouterGuardrailModerationPolicyConfig {

    private final Map<String, Object> values;

    private OpenRouterGuardrailModerationPolicyConfig(Map<String, Object> values) {
        this.values = values;
    }

    /**
     * Creates a new, empty builder for the moderation policy config.
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

    /** Builder for {@link OpenRouterGuardrailModerationPolicyConfig}. */
    public static final class Builder {

        private final Map<String, Object> values = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code content_filter_blocks_requests}: whether a
         * {@code block} content filter rejects the request (otherwise it is
         * withheld and replaced). {@code null} unsets.
         */
        public Builder contentFilterBlocksRequests(Boolean contentFilterBlocksRequests) {
            set("content_filter_blocks_requests", contentFilterBlocksRequests);
            return this;
        }

        /**
         * Adds one {@code content_filter_builtins[]} entry: a built-in
         * content filter named by {@code slug} (the documented slugs are
         * {@code email}, {@code phone}, {@code ssn}, {@code credit-card},
         * {@code ip-address}, {@code secrets}, {@code person-name},
         * {@code address} and {@code regex-prompt-injection}; unknown
         * spellings pass through verbatim).
         *
         * @param slug the built-in filter slug
         * @param action {@code "redact"}, {@code "block"} or {@code "flag"}
         *        (unknown spellings pass through verbatim)
         */
        public Builder addContentFilterBuiltin(String slug, String action) {
            return addContentFilterBuiltinRaw(new JSONObject().put("slug", slug).put("action", action));
        }

        /**
         * Adds one {@code content_filter_builtins[]} entry in full - the
         * {@code label} (max 100 characters) and {@code scan_scope}
         * ({@code "user_only"} / {@code "all_messages"}, unknown spellings
         * verbatim) fields included.
         */
        public Builder addContentFilterBuiltin(String slug, String action, String label, String scanScope) {
            checkLabel(label);
            JSONObject entry = new JSONObject().put("slug", slug).put("action", action);
            if (label != null) {
                entry.put("label", label);
            }
            if (scanScope != null) {
                entry.put("scan_scope", scanScope);
            }
            return addContentFilterBuiltinRaw(entry);
        }

        /**
         * Adds one {@code content_filter_builtins[]} entry verbatim - the
         * API rejects entries without the required {@code slug} and
         * {@code action} keys.
         */
        public Builder addContentFilterBuiltinRaw(JSONObject entry) {
            return addEntry("content_filter_builtins", entry);
        }

        /**
         * Adds one {@code content_filters[]} entry: a regex content filter
         * ({@code pattern} 1-100,000 characters, {@code action}).
         */
        public Builder addContentFilter(String pattern, String action) {
            if (pattern == null || pattern.isEmpty() || pattern.length() > 100_000) {
                throw new IllegalArgumentException("pattern must be 1-100,000 characters");
            }
            return addContentFilterRaw(new JSONObject().put("pattern", pattern).put("action", action));
        }

        /**
         * Adds one {@code content_filters[]} entry with its optional
         * {@code label} (max 100 characters).
         */
        public Builder addContentFilter(String pattern, String action, String label) {
            checkLabel(label);
            if (pattern == null || pattern.isEmpty() || pattern.length() > 100_000) {
                throw new IllegalArgumentException("pattern must be 1-100,000 characters");
            }
            JSONObject entry = new JSONObject().put("pattern", pattern).put("action", action);
            if (label != null) {
                entry.put("label", label);
            }
            return addContentFilterRaw(entry);
        }

        /**
         * Adds one {@code content_filters[]} entry verbatim - the API
         * rejects entries without the required {@code pattern} and
         * {@code action} keys.
         */
        public Builder addContentFilterRaw(JSONObject entry) {
            return addEntry("content_filters", entry);
        }

        /**
         * Adds one {@code pii_filters[]} entry: a PII entity filter
         * ({@code entity_type} + {@code action}, optional
         * {@code score_threshold} 0-1).
         */
        public Builder addPiiFilter(String entityType, String action, Double scoreThreshold) {
            if (scoreThreshold != null && (scoreThreshold.doubleValue() < 0 || scoreThreshold.doubleValue() > 1)) {
                throw new IllegalArgumentException("scoreThreshold must be between 0 and 1, was: " + scoreThreshold);
            }
            JSONObject entry = new JSONObject().put("entity_type", entityType).put("action", action);
            if (scoreThreshold != null) {
                entry.put("score_threshold", scoreThreshold.doubleValue());
            }
            return addPiiFilterRaw(entry);
        }

        /**
         * Adds one {@code pii_filters[]} entry verbatim - the API rejects
         * entries without the required {@code entity_type} and
         * {@code action} keys.
         */
        public Builder addPiiFilterRaw(JSONObject entry) {
            return addEntry("pii_filters", entry);
        }

        /**
         * Sets {@code content_filter_patterns}: regex patterns without a
         * typed action entry (the API's shorthand list). {@code null} unsets;
         * a set list replaces the previous one.
         */
        public Builder contentFilterPatterns(List<String> contentFilterPatterns) {
            set("content_filter_patterns", contentFilterPatterns == null ? null : List.copyOf(contentFilterPatterns));
            return this;
        }

        /** Sets {@code enable_model_armor_injection_protection}. {@code null} unsets. */
        public Builder enableModelArmorInjectionProtection(Boolean enableModelArmorInjectionProtection) {
            set("enable_model_armor_injection_protection", enableModelArmorInjectionProtection);
            return this;
        }

        /**
         * Sets {@code model_armor_injection_protection_mode}: {@code "block"}
         * or {@code "flag"} (unknown spellings verbatim), or {@code null} to
         * unset.
         */
        public Builder modelArmorInjectionProtectionMode(String mode) {
            set("model_armor_injection_protection_mode", mode);
            return this;
        }

        /**
         * Sets {@code model_armor_injection_confidence_level}: {@code "low"},
         * {@code "medium"} or {@code "high"} (unknown spellings verbatim).
         * {@code null} unsets.
         */
        public Builder modelArmorInjectionConfidenceLevel(String level) {
            set("model_armor_injection_confidence_level", level);
            return this;
        }

        /**
         * Sets {@code lakera_injection_protection_mode}: {@code "block"} or
         * {@code "flag"} (unknown spellings verbatim), or {@code null} to
         * unset.
         */
        public Builder lakeraInjectionProtectionMode(String mode) {
            set("lakera_injection_protection_mode", mode);
            return this;
        }

        /** Sets {@code broadcast_model_armor_flagged_requests}. {@code null} unsets. */
        public Builder broadcastModelArmorFlaggedRequests(Boolean broadcastModelArmorFlaggedRequests) {
            set("broadcast_model_armor_flagged_requests", broadcastModelArmorFlaggedRequests);
            return this;
        }

        /**
         * Sets {@code armor} verbatim - the Armor classifier context (spec
         * {@code ArmorContext}): {@code {"v": 1, "auto_mode": {"hard_deny":
         * [...], "soft_deny": [...], "allow": [...], "environment": [...]}}}
         * where the rules are strings or
         * {@code {"id", "instructions", "threshold?"}} objects and the
         * environment entries {@code "Name: value"} strings or
         * {@code {"name", "value"}} objects ({@code v} is required). The
         * shape is carried unchanged; {@code null} unsets.
         */
        public Builder armor(JSONObject armor) {
            set("armor", armor == null ? null : new JSONObject(armor.toString()));
            return this;
        }

        private static void checkLabel(String label) {
            if (label != null && label.length() > 100) {
                throw new IllegalArgumentException("label must be at most 100 characters");
            }
        }

        /** Sets {@code platform_moderated_providers}. {@code null} unsets. */
        public Builder platformModeratedProviders(List<String> platformModeratedProviders) {
            set("platform_moderated_providers", platformModeratedProviders == null ? null : List.copyOf(platformModeratedProviders));
            return this;
        }

        /** Sets {@code platform_image_moderated_providers}. {@code null} unsets. */
        public Builder platformImageModeratedProviders(List<String> platformImageModeratedProviders) {
            set("platform_image_moderated_providers", platformImageModeratedProviders == null ? null : List.copyOf(platformImageModeratedProviders));
            return this;
        }

        @SuppressWarnings("unchecked")
        private Builder addEntry(String key, JSONObject entry) {
            if (entry == null) {
                throw new IllegalArgumentException("entry must not be null");
            }
            List<JSONObject> entries = (List<JSONObject>) values.get(key);
            if (entries == null) {
                entries = new ArrayList<>();
                values.put(key, entries);
            }
            entries.add(new JSONObject(entry.toString()));
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
         * valid and means "no moderation".
         */
        public OpenRouterGuardrailModerationPolicyConfig build() {
            Map<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<String, Object> e : values.entrySet()) {
                Object value = e.getValue();
                copy.put(e.getKey(), value instanceof List<?> list ? new ArrayList<>(list) : value);
            }
            return new OpenRouterGuardrailModerationPolicyConfig(copy);
        }
    }
}
