package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed implementation of the OpenRouter {@code armor} plugin (Beta since
 * October 2026, schema {@code ArmorPlugin}): evaluates every client tool call
 * the model emits against a rule stack ({@code hard_deny}, {@code soft_deny},
 * {@code allow}) before it reaches the caller, merging any workspace guardrail,
 * this plugin and any Anthropic {@code safeguards}. Emitted into the
 * {@code plugins} request array as {@code {"id": "armor", ...}} with only the
 * explicitly configured fields.
 * <p>
 * JSON field: {@code plugins[].id = "armor"} (supported on Chat Completions,
 * Responses and Anthropic Messages). Default: no plugin is sent.
 * <p>
 * Traps:
 * <ul>
 *   <li><b>Beta caveat:</b> Armor is in beta; its configuration fields, the
 *       response {@code safeguard_results} array and the
 *       {@code openrouter:armor_blocked} output item shape may still change.
 *       Evaluation is not billed during the beta.</li>
 *   <li><b>{@code safeguard_results} is independent of
 *       {@code openrouter_metadata}:</b> every response Armor evaluated
 *       carries {@code safeguard_results} (even when nothing was flagged),
 *       without needing the {@code metadataInResponse(true)} opt-in.</li>
 *   <li><b>{@code output_mode} is API-specific</b> (requesting a mode on an
 *       API that cannot carry it returns HTTP 400):
 *       <ul>
 *         <li>{@code "text"} (default on Chat Completions and Responses): a
 *             blocked call is withheld and replaced by an
 *             {@code <openrouter_armor_blocked>} XML marker in assistant
 *             text.</li>
 *         <li>{@code "custom_item"} (Responses API only): a blocked call is
 *             withheld and replaced by an {@code openrouter:armor_blocked}
 *             output item in {@code output[]} carrying {@code id},
 *             {@code call_id}, {@code name}, {@code arguments},
 *             {@code reason} and {@code explanation}. Echo that item back in
 *             {@code input[]} unchanged on the next turn; do not write a
 *             {@code function_call_output} for it.</li>
 *         <li>{@code "claude_code_compatibility"} (Messages API only; default
 *             when the request carries a {@code dangerous_tool_use} safeguard;
 *             cannot be combined with {@code mode: "audit"}): the flagged
 *             {@code tool_use} block stays in {@code content[]} and its
 *             verdict is {@code "flagged"} in {@code safeguard_results}.
 *             <b>Warning:</b> under {@code "claude_code_compatibility"} the
 *             caller's application is what stops the tool call - a client that
 *             ignores {@code safeguard_results} will execute a flagged call.
 *             Send {@code outputMode("text")} on Messages if you want
 *             OpenRouter to withhold blocked calls automatically.</li>
 *       </ul>
 *   </li>
 *   <li><b>Rule tier precedence in {@code classifier_context}:</b> a
 *       {@code hard_deny} match always blocks the call; a {@code soft_deny}
 *       match blocks the call unless an {@code allow} rule also matches or the
 *       user explicitly asked for that exact operation on that target in the
 *       conversation; {@code allow} rules only clear {@code soft_deny} matches
 *       (default rule thresholds: {@code 0.6} for deny rules, {@code 0.7} for
 *       allow rules).</li>
 *   <li><b>HIPAA and regional routing:</b> requests that enable Armor are
 *       rejected with HTTP 403 when the workspace has HIPAA enabled or a
 *       data-region restriction applies. Zero Data Retention
 *       ({@code provider.zdr: true} or workspace ZDR) is supported (the
 *       evaluator runs on a zero-retention tenancy).</li>
 *   <li><b>Automatic tool loop:</b> when tools carry handlers, the chat
 *       {@code execute()} loop runs every tool call the response contains and
 *       returns only the final turn. In {@code mode("audit")} (or
 *       {@code onError("log")}) a flagged call is therefore executed by this
 *       library, and {@code safeguardResults()} reports only the final turn's
 *       verdicts. Use {@code mode("enforce")} to have blocked calls withheld,
 *       or use the Responses API to inspect each turn's verdicts before
 *       running tools.</li>
 * </ul>
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins/armor">Armor plugin guide</a>
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins">OpenRouter plugins</a>
 * @see OpenRouterSafeguardResult
 */
public final class OpenRouterArmorPlugin implements OpenRouterPlugin {

    /** The plugin discriminator emitted as {@code plugins[].id}. */
    public static final String PLUGIN_ID = "armor";

    private final JSONObject classifierContext;
    private final String mode;
    private final String onError;
    private final String outputMode;
    private final Boolean evaluateNativeTools;
    private final Boolean useCcStyleDefaultRules;
    private final Boolean enabled;
    private final Map<String, Object> extraOptions;

    private OpenRouterArmorPlugin(Builder builder) {
        this.classifierContext = builder.classifierContext == null
                ? null
                : new JSONObject(builder.classifierContext.toString());
        this.mode = builder.mode;
        this.onError = builder.onError;
        this.outputMode = builder.outputMode;
        this.evaluateNativeTools = builder.evaluateNativeTools;
        this.useCcStyleDefaultRules = builder.useCcStyleDefaultRules;
        this.enabled = builder.enabled;
        this.extraOptions = copyExtraOptions(builder.extraOptions);
    }

    private static Map<String, Object> copyExtraOptions(Map<String, Object> source) {
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            Object v = entry.getValue();
            if (v instanceof JSONObject jo) {
                copy.put(entry.getKey(), new JSONObject(jo.toString()));
            } else if (v instanceof JSONArray ja) {
                copy.put(entry.getKey(), new JSONArray(ja.toString()));
            } else {
                copy.put(entry.getKey(), v);
            }
        }
        return copy;
    }

    /**
     * Creates the {@code armor} plugin with no extra fields ({@code {"id": "armor"}}).
     */
    public OpenRouterArmorPlugin() {
        this(new Builder());
    }

    /**
     * Creates a new builder for the {@code armor} plugin.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the plugin discriminator {@code "armor"} ({@code plugins[].id}). */
    @Override
    public String id() {
        return PLUGIN_ID;
    }

    /**
     * Returns a detached copy of the configured {@code classifier_context}
     * object (schema {@code ArmorContext}), or {@code null} when unset.
     *
     * @return a detached copy of the classifier context, or {@code null}
     */
    public JSONObject classifierContext() {
        return classifierContext == null ? null : new JSONObject(classifierContext.toString());
    }

    /**
     * Returns the configured {@code mode} ({@code "enforce"} or
     * {@code "audit"}), or {@code null} when unset (the API default
     * {@code "enforce"} applies).
     *
     * @return the mode, or {@code null} when unset
     */
    public String mode() {
        return mode;
    }

    /**
     * Returns the configured {@code on_error} policy ({@code "block"} or
     * {@code "log"} when the classifier is {@code "unavailable"}), or
     * {@code null} when unset (the API default {@code "block"} applies).
     *
     * @return the on_error policy, or {@code null} when unset
     */
    public String onError() {
        return onError;
    }

    /**
     * Returns the configured {@code output_mode} ({@code "text"},
     * {@code "custom_item"} or {@code "claude_code_compatibility"}), or
     * {@code null} when unset (the API default for the target endpoint
     * applies).
     *
     * @return the output mode, or {@code null} when unset
     */
    public String outputMode() {
        return outputMode;
    }

    /**
     * Returns the configured {@code evaluate_native_tools} flag, or
     * {@code null} when unset (the API default {@code false} applies).
     *
     * @return the evaluate_native_tools flag, or {@code null} when unset
     */
    public Boolean evaluateNativeTools() {
        return evaluateNativeTools;
    }

    /**
     * Returns the configured {@code use_cc_style_default_rules} flag, or
     * {@code null} when unset (defaults to {@code true} on Anthropic Messages
     * and {@code false} on Chat Completions and Responses).
     *
     * @return the use_cc_style_default_rules flag, or {@code null} when unset
     */
    public Boolean useCcStyleDefaultRules() {
        return useCcStyleDefaultRules;
    }

    /**
     * Returns the configured {@code enabled} flag, or {@code null} when unset
     * (the API default {@code true} applies).
     *
     * @return the enabled flag, or {@code null} when unset
     */
    public Boolean enabled() {
        return enabled;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", PLUGIN_ID);
        if (classifierContext != null) {
            json.put("classifier_context", new JSONObject(classifierContext.toString()));
        }
        if (mode != null) {
            json.put("mode", mode);
        }
        if (onError != null) {
            json.put("on_error", onError);
        }
        if (outputMode != null) {
            json.put("output_mode", outputMode);
        }
        if (evaluateNativeTools != null) {
            json.put("evaluate_native_tools", evaluateNativeTools);
        }
        if (useCcStyleDefaultRules != null) {
            json.put("use_cc_style_default_rules", useCcStyleDefaultRules);
        }
        if (enabled != null) {
            json.put("enabled", enabled);
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            Object v = e.getValue();
            if (v == null) {
                json.put(e.getKey(), JSONObject.NULL);
            } else if (v instanceof JSONObject jo) {
                json.put(e.getKey(), new JSONObject(jo.toString()));
            } else if (v instanceof JSONArray ja) {
                json.put(e.getKey(), new JSONArray(ja.toString()));
            } else {
                json.put(e.getKey(), v);
            }
        }
        return json;
    }

    /**
     * Builder for {@link OpenRouterArmorPlugin}. Only explicitly configured
     * fields are emitted; a verbatim {@link #option(String, Object)} escape
     * hatch covers keys this library does not know yet.
     */
    public static final class Builder {

        private JSONObject classifierContext;
        private String mode;
        private String onError;
        private String outputMode;
        private Boolean evaluateNativeTools;
        private Boolean useCcStyleDefaultRules;
        private Boolean enabled;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code plugins[].classifier_context} (schema
         * {@code ArmorContext}): the caller's rules and environment facts,
         * stored and emitted verbatim as a detached copy. {@code null} unsets
         * the field.
         * <p>
         * Documented {@code ArmorContext} shape (not enforced client-side so
         * future context revisions pass through unchanged):
         * {@code {"v": 1, "auto_mode": {"environment": [...], "hard_deny":
         * [...], "soft_deny": [...], "allow": [...]}}}, where each rule is a
         * plain instruction string or an object
         * {@code {"id": "...", "instructions": "...", "name"?: "...",
         * "threshold"?: 0.6}}, and each environment entry is a
         * {@code "Name: value"} string or {@code {"name": "...", "value":
         * "..."}}.
         *
         * @param classifierContext the classifier context object, or
         *                          {@code null} to unset
         * @return this builder
         */
        public Builder classifierContext(JSONObject classifierContext) {
            this.classifierContext = classifierContext == null
                    ? null
                    : new JSONObject(classifierContext.toString());
            return this;
        }

        /**
         * Sets {@code plugins[].mode}: {@code "enforce"} (withholds blocked
         * tool calls; the API default when omitted) or {@code "audit"}
         * (evaluates and reports the same verdicts in
         * {@code safeguard_results} without withholding any calls, regardless
         * of {@code on_error}; cannot be combined with
         * {@code output_mode: "claude_code_compatibility"}). Validated loudly
         * against the two documented modes; {@code null} unsets the field (use
         * {@link #option(String, Object)} if OpenRouter adds another mode
         * before this library types it).
         * <p>
         * Trap: when used with the chat automatic tool-call loop, released
         * calls in {@code "audit"} mode are dispatched to your
         * {@code OpenRouterToolsCallback} regardless of their verdict; inspect
         * {@code safeguardResults()} on a single-turn / Responses call or use
         * {@code "enforce"} with {@code onError("block")}.
         *
         * @param mode {@code "enforce"}, {@code "audit"}, or {@code null} to
         *             unset
         * @return this builder
         */
        public Builder mode(String mode) {
            if (mode == null) {
                this.mode = null;
                return this;
            }
            if (!"enforce".equals(mode) && !"audit".equals(mode)) {
                throw new IllegalArgumentException(
                        "mode must be \"enforce\" or \"audit\", was: " + mode);
            }
            this.mode = mode;
            return this;
        }

        /**
         * Sets {@code plugins[].on_error}: what happens when the classifier
         * cannot answer (timeout, upstream error, context too long) and the
         * call is reported {@code "unavailable"} - {@code "block"} (rejects
         * the call; the API default when omitted) or {@code "log"} (releases
         * the call). Validated loudly against the two documented values;
         * {@code null} unsets the field (use {@link #option(String, Object)}
         * if OpenRouter adds another value before this library types it).
         * <p>
         * Trap: under {@code output_mode: "claude_code_compatibility"} an
         * {@code "unavailable"} call is always released with its verdict
         * visible so the client can fall back to local evaluation. When used
         * with the chat automatic tool-call loop, calls released under
         * {@code "log"} are dispatched to your
         * {@code OpenRouterToolsCallback} regardless of their verdict; use
         * {@code "block"} if unavailable calls must not run automatically.
         *
         * @param onError {@code "block"}, {@code "log"}, or {@code null} to
         *                unset
         * @return this builder
         */
        public Builder onError(String onError) {
            if (onError == null) {
                this.onError = null;
                return this;
            }
            if (!"block".equals(onError) && !"log".equals(onError)) {
                throw new IllegalArgumentException(
                        "onError must be \"block\" or \"log\", was: " + onError);
            }
            this.onError = onError;
            return this;
        }

        /**
         * Sets {@code plugins[].output_mode}: how a blocked tool call reaches
         * the caller. Validated loudly against the three closed-enum values
         * from the documentation ({@code null} unsets the field; use
         * {@link #option(String, Object)} if OpenRouter adds another mode
         * before this library types it):
         * <ul>
         *   <li>{@code "text"} (default on Chat Completions and Responses):
         *       the blocked call is withheld and replaced by an
         *       {@code <openrouter_armor_blocked>} XML marker in assistant
         *       text.</li>
         *   <li>{@code "custom_item"} (Responses API only): the blocked call
         *       is withheld and replaced by an {@code openrouter:armor_blocked}
         *       output item in {@code output[]} (reachable via
         *       {@link de.entwicklertraining.openrouter4j.responses.OpenRouterResponsesResponse#output()}).</li>
         *   <li>{@code "claude_code_compatibility"} (Messages API only; default
         *       when {@code safeguards} carries {@code dangerous_tool_use}):
         *       the {@code tool_use} block stays in {@code content[]} with
         *       {@code "flagged"} in {@code safeguard_results}, and the caller
         *       must decline to execute it.</li>
         * </ul>
         * Trap: requesting an {@code output_mode} on an API that cannot carry
         * it returns HTTP 400.
         *
         * @param outputMode {@code "text"}, {@code "custom_item"},
         *                   {@code "claude_code_compatibility"}, or
         *                   {@code null} to unset
         * @return this builder
         */
        public Builder outputMode(String outputMode) {
            if (outputMode == null) {
                this.outputMode = null;
                return this;
            }
            if (!"text".equals(outputMode)
                    && !"custom_item".equals(outputMode)
                    && !"claude_code_compatibility".equals(outputMode)) {
                throw new IllegalArgumentException(
                        "outputMode must be \"text\", \"custom_item\" or \"claude_code_compatibility\", was: "
                                + outputMode);
            }
            this.outputMode = outputMode;
            return this;
        }

        /**
         * Sets {@code plugins[].evaluate_native_tools}: when {@code true},
         * forces provider-native server tools (web search, code execution, ...)
         * through the agent loop so Armor evaluates them too. Default: unset
         * (the API default {@code false} applies: native passthrough stands
         * and such calls are reported {@code "skipped"}). {@code false} is a
         * set option and is emitted; {@code null} unsets the field.
         *
         * @param evaluateNativeTools {@code true}, {@code false}, or
         *                            {@code null} to unset
         * @return this builder
         */
        public Builder evaluateNativeTools(Boolean evaluateNativeTools) {
            this.evaluateNativeTools = evaluateNativeTools;
            return this;
        }

        /**
         * Sets {@code plugins[].use_cc_style_default_rules}: when
         * {@code true}, prepends Claude Code's built-in auto-mode rule set (1
         * hard-deny, 72 soft-deny, 17 allow, 21 environment slots) ahead of
         * every configured rule. Default: unset (defaults to {@code true} on
         * the Anthropic Messages API and {@code false} on Chat Completions and
         * Responses). {@code false} is a set option and is emitted;
         * {@code null} unsets the field.
         *
         * @param useCcStyleDefaultRules {@code true}, {@code false}, or
         *                               {@code null} to unset
         * @return this builder
         */
        public Builder useCcStyleDefaultRules(Boolean useCcStyleDefaultRules) {
            this.useCcStyleDefaultRules = useCcStyleDefaultRules;
            return this;
        }

        /**
         * Sets {@code plugins[].enabled}: set to {@code false} to disable
         * Armor for this request. Default: unset (the API default {@code true}
         * applies). {@code false} is a set option and is emitted; {@code null}
         * unsets the field.
         *
         * @param enabled {@code true}, {@code false}, or {@code null} to unset
         * @return this builder
         */
        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Adds a plugin field verbatim - escape hatch for keys this library
         * does not know yet. The key must not be {@code null} or {@code "id"}
         * (set from the plugin type). Null values are emitted as JSON
         * {@code null}; {@link JSONObject} and {@link JSONArray} values are
         * stored as detached copies.
         *
         * @param key   the field name (must not be {@code "id"})
         * @param value the field value
         * @return this builder
         */
        public Builder option(String key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("option key must not be null");
            }
            if ("id".equals(key)) {
                throw new IllegalArgumentException(
                        "The 'id' field is set from the plugin type and must not be set via option()");
            }
            if (value instanceof JSONObject jo) {
                this.extraOptions.put(key, new JSONObject(jo.toString()));
            } else if (value instanceof JSONArray ja) {
                this.extraOptions.put(key, new JSONArray(ja.toString()));
            } else {
                this.extraOptions.put(key, value);
            }
            return this;
        }

        /**
         * Builds the {@link OpenRouterArmorPlugin} value type.
         *
         * @return the built plugin
         */
        public OpenRouterArmorPlugin build() {
            return new OpenRouterArmorPlugin(this);
        }
    }
}
