package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Typed implementation of the {@code openrouter:subagent} server tool:
 * delegates self-contained tasks to a smaller, cheaper, faster worker model
 * mid-generation and returns its outcome. Emitted into the {@code tools}
 * request array as {@code {"type": "openrouter:subagent"}} plus a
 * {@code parameters} object holding only the explicitly configured fields.
 * <p>
 * Multiple subagent entries are allowed - one per named instance; the model
 * then sees one tool per named subagent (plus one default for an unnamed
 * entry). Names must be unique across subagent entries.
 * <p>
 * Traps: {@code inherit_functions} and {@code inherited_function_names} are
 * experimental and supported on the Responses API only - other APIs reject
 * them with a 400. {@code max_tool_calls} is capped at 25 and only relevant
 * when the subagent is given tools.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools/subagent">Subagent server tool</a>
 */
public final class OpenRouterSubagentServerTool implements OpenRouterServerTool {

    /** The server-tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "openrouter:subagent";

    private final String model;
    private final String name;
    private final String instructions;
    private final Integer maxCompletionTokens;
    private final Integer maxToolCalls;
    private final Boolean inheritFunctions;
    private final List<String> inheritedFunctionNames;
    private final String reasoningEffort;
    private final Integer reasoningMaxTokens;
    private final Double temperature;
    private final Map<String, Object> extraOptions;

    private OpenRouterSubagentServerTool(Builder builder) {
        this.model = builder.model;
        this.name = builder.name;
        this.instructions = builder.instructions;
        this.maxCompletionTokens = builder.maxCompletionTokens;
        this.maxToolCalls = builder.maxToolCalls;
        this.inheritFunctions = builder.inheritFunctions;
        this.inheritedFunctionNames = builder.inheritedFunctionNames == null
                ? null : List.copyOf(builder.inheritedFunctionNames);
        this.reasoningEffort = builder.reasoningEffort;
        this.reasoningMaxTokens = builder.reasoningMaxTokens;
        this.temperature = builder.temperature;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code openrouter:subagent} server tool.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the server-tool discriminator {@code "openrouter:subagent"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /**
     * Returns the configured {@code parameters.model} slug (any OpenRouter
     * model, typically a smaller/cheaper one than the delegating model; when
     * omitted, the outer request's model is used), or {@code null} when unset.
     */
    public String model() {
        return model;
    }

    /**
     * Returns the configured {@code parameters.name} (the name the model sees
     * for this subagent; unique across subagent entries), or {@code null} when
     * unset.
     */
    public String name() {
        return name;
    }

    /**
     * Returns the configured {@code parameters.instructions} (system
     * instructions for the subagent), or {@code null} when unset.
     */
    public String instructions() {
        return instructions;
    }

    /**
     * Returns the configured {@code parameters.max_completion_tokens} value
     * (output tokens including reasoning), or {@code null} when unset.
     */
    public Integer maxCompletionTokens() {
        return maxCompletionTokens;
    }

    /**
     * Returns the configured {@code parameters.max_tool_calls} value (1-25,
     * only relevant when the subagent is given tools), or {@code null} when
     * unset.
     */
    public Integer maxToolCalls() {
        return maxToolCalls;
    }

    /**
     * Returns the configured {@code parameters.inherit_functions} value: when
     * {@code true}, the subagent inherits every client function from the
     * request's top-level {@code tools} list; or {@code null} when unset.
     * <p>
     * Trap: experimental, Responses API only - other APIs reject it with 400.
     */
    public Boolean inheritFunctions() {
        return inheritFunctions;
    }

    /**
     * Returns the configured {@code parameters.inherited_function_names}
     * (names of the top-level function tools the subagent inherits), empty
     * when unset (never {@code null}).
     * <p>
     * Trap: experimental, Responses API only - other APIs reject it with 400.
     */
    public List<String> inheritedFunctionNames() {
        return inheritedFunctionNames == null ? List.of() : inheritedFunctionNames;
    }

    /**
     * Returns the configured {@code parameters.reasoning.effort} value
     * ({@code max}, {@code xhigh}, {@code high}, {@code medium}, {@code low},
     * {@code minimal}, {@code none}), or {@code null} when unset.
     */
    public String reasoningEffort() {
        return reasoningEffort;
    }

    /**
     * Returns the configured {@code parameters.reasoning.max_tokens} value
     * (reasoning budget for the subagent), or {@code null} when unset.
     */
    public Integer reasoningMaxTokens() {
        return reasoningMaxTokens;
    }

    /**
     * Returns the configured {@code parameters.temperature} value forwarded to
     * the subagent call, or {@code null} when unset.
     */
    public Double temperature() {
        return temperature;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "openrouter:subagent"}} plus a {@code parameters} object
     * when at least one option is set.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);

        JSONObject parameters = new JSONObject();
        if (model != null) {
            parameters.put("model", model);
        }
        if (name != null) {
            parameters.put("name", name);
        }
        if (instructions != null) {
            parameters.put("instructions", instructions);
        }
        if (maxCompletionTokens != null) {
            parameters.put("max_completion_tokens", maxCompletionTokens);
        }
        if (maxToolCalls != null) {
            parameters.put("max_tool_calls", maxToolCalls);
        }
        if (inheritFunctions != null) {
            parameters.put("inherit_functions", inheritFunctions);
        }
        if (inheritedFunctionNames != null && !inheritedFunctionNames.isEmpty()) {
            parameters.put("inherited_function_names", new JSONArray(inheritedFunctionNames));
        }
        if (reasoningEffort != null || reasoningMaxTokens != null) {
            JSONObject reasoning = new JSONObject();
            if (reasoningEffort != null) {
                reasoning.put("effort", reasoningEffort);
            }
            if (reasoningMaxTokens != null) {
                reasoning.put("max_tokens", reasoningMaxTokens);
            }
            parameters.put("reasoning", reasoning);
        }
        if (temperature != null) {
            parameters.put("temperature", temperature);
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            parameters.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }

        if (!parameters.isEmpty()) {
            tool.put("parameters", parameters);
        }
        return tool;
    }

    /**
     * Builder for the {@code openrouter:subagent} server tool. Only explicitly
     * configured fields are emitted; a verbatim {@link #option(String, Object)}
     * escape hatch covers configuration keys this library does not know yet.
     */
    public static final class Builder {

        private String model;
        private String name;
        private String instructions;
        private Integer maxCompletionTokens;
        private Integer maxToolCalls;
        private Boolean inheritFunctions;
        private List<String> inheritedFunctionNames;
        private String reasoningEffort;
        private Integer reasoningMaxTokens;
        private Double temperature;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code parameters.model}: the worker model slug (any
         * OpenRouter model; typically a smaller, cheaper, faster model than
         * the one delegating). When omitted, the outer request's model is
         * used. Trap: the subagent tool itself cannot be the subagent model.
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets {@code parameters.name}: the subagent's name. The model sees
         * one tool per named subagent (and one default for an unnamed entry);
         * names must be unique across subagent entries (letters, digits,
         * spaces, underscores, dashes; 1-64 chars).
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets {@code parameters.instructions}: system instructions for the
         * subagent. When omitted, the subagent responds with no system prompt
         * of its own.
         */
        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        /**
         * Sets {@code parameters.max_completion_tokens}: maximum output tokens
         * (including reasoning) the subagent may produce.
         */
        public Builder maxCompletionTokens(Integer maxCompletionTokens) {
            this.maxCompletionTokens = maxCompletionTokens;
            return this;
        }

        /**
         * Sets {@code parameters.max_tool_calls}: maximum agentic-loop steps
         * for the subagent (1-25, only relevant when the subagent is given
         * tools).
         */
        public Builder maxToolCalls(Integer maxToolCalls) {
            this.maxToolCalls = maxToolCalls;
            return this;
        }

        /**
         * Sets {@code parameters.inherit_functions}: when {@code true}, the
         * subagent inherits every client function from the request's top-level
         * {@code tools} list. Trap: experimental, Responses API only - other
         * APIs reject it with a 400.
         */
        public Builder inheritFunctions(Boolean inheritFunctions) {
            this.inheritFunctions = inheritFunctions;
            return this;
        }

        /**
         * Sets {@code parameters.inherited_function_names}: names of the
         * top-level function tools the subagent inherits (copied fully into
         * the subagent's tools array; a whitespace-only name is rejected with
         * a 400). Does nothing when {@link #inheritFunctions(Boolean)} is
         * {@code true}. Trap: experimental, Responses API only.
         */
        public Builder inheritedFunctionNames(List<String> names) {
            this.inheritedFunctionNames = names;
            return this;
        }

        /**
         * Sets {@code parameters.reasoning.effort}: reasoning effort level for
         * the subagent call ({@code max}, {@code xhigh}, {@code high},
         * {@code medium}, {@code low}, {@code minimal}, {@code none}).
         */
        public Builder reasoningEffort(String effort) {
            this.reasoningEffort = effort;
            return this;
        }

        /**
         * Sets {@code parameters.reasoning.max_tokens}: maximum reasoning
         * tokens the subagent may use.
         */
        public Builder reasoningMaxTokens(Integer maxTokens) {
            this.reasoningMaxTokens = maxTokens;
            return this;
        }

        /**
         * Sets {@code parameters.temperature}: sampling temperature forwarded
         * to the subagent call.
         */
        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        /**
         * Adds a {@code parameters} entry verbatim - escape hatch for
         * configuration keys this library does not know yet. Null values are
         * emitted as JSON {@code null}.
         */
        public Builder option(String key, Object value) {
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the {@link OpenRouterSubagentServerTool}.
         */
        public OpenRouterSubagentServerTool build() {
            return new OpenRouterSubagentServerTool(this);
        }
    }
}
