package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Typed implementation of the {@code openrouter:fusion} server tool: fans out
 * the user prompt to a panel of analysis models, then asks an analyst model to
 * summarize their collective output as structured JSON the outer model can
 * synthesize from. Emitted into the {@code tools} request array as
 * {@code {"type": "openrouter:fusion"}} plus a {@code parameters} object
 * holding only the explicitly configured fields.
 * <p>
 * Traps: {@code analysis_models} is capped at 8 models (cost amplification);
 * {@code max_tool_calls} defaults to 4 and is capped at 16; the analyst always
 * runs at temperature 0 regardless of {@code temperature}. When
 * {@code tools} is omitted, panelists default to web search + fetch; an
 * empty array (via the verbatim {@code option(key, value)} escape hatch)
 * disables tools entirely.
 *
 * @see OpenRouterFusionPlugin
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools/fusion">Fusion server tool</a>
 */
public final class OpenRouterFusionServerTool implements OpenRouterServerTool {

    /** The server-tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "openrouter:fusion";

    private final List<String> analysisModels;
    private final String model;
    private final Integer maxToolCalls;
    private final Integer maxCompletionTokens;
    private final String reasoningEffort;
    private final Integer reasoningMaxTokens;
    private final Double temperature;
    private final Map<String, Object> extraOptions;

    private OpenRouterFusionServerTool(Builder builder) {
        this.analysisModels = builder.analysisModels == null ? null : List.copyOf(builder.analysisModels);
        this.model = builder.model;
        this.maxToolCalls = builder.maxToolCalls;
        this.maxCompletionTokens = builder.maxCompletionTokens;
        this.reasoningEffort = builder.reasoningEffort;
        this.reasoningMaxTokens = builder.reasoningMaxTokens;
        this.temperature = builder.temperature;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code openrouter:fusion} server tool.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the server-tool discriminator {@code "openrouter:fusion"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /**
     * Returns the configured {@code parameters.analysis_models} panel slugs
     * (1-8 models; each receives the prompt with web search + fetch enabled),
     * empty when unset (never {@code null}).
     */
    public List<String> analysisModels() {
        return analysisModels == null ? List.of() : analysisModels;
    }

    /**
     * Returns the configured {@code parameters.model} slug - the analyst model
     * producing the structured analysis JSON - or {@code null} when unset
     * (defaults to the outer request's model).
     */
    public String model() {
        return model;
    }

    /**
     * Returns the configured {@code parameters.max_tool_calls} value (1-16,
     * default 4) - the agentic research-loop budget per panelist and the
     * analyst - or {@code null} when unset.
     */
    public Integer maxToolCalls() {
        return maxToolCalls;
    }

    /**
     * Returns the configured {@code parameters.max_completion_tokens} value
     * (output budget per inner call, default 16000), or {@code null} when
     * unset.
     */
    public Integer maxCompletionTokens() {
        return maxCompletionTokens;
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
     * (reasoning budget per panelist and the analyst), or {@code null} when
     * unset.
     */
    public Integer reasoningMaxTokens() {
        return reasoningMaxTokens;
    }

    /**
     * Returns the configured {@code parameters.temperature} value forwarded to
     * panelist inner calls (the analyst always runs at temperature 0), or
     * {@code null} when unset.
     */
    public Double temperature() {
        return temperature;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "openrouter:fusion"}} plus a {@code parameters} object
     * when at least one option is set.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);

        JSONObject parameters = new JSONObject();
        if (analysisModels != null && !analysisModels.isEmpty()) {
            parameters.put("analysis_models", new JSONArray(analysisModels));
        }
        if (model != null) {
            parameters.put("model", model);
        }
        if (maxToolCalls != null) {
            parameters.put("max_tool_calls", maxToolCalls);
        }
        if (maxCompletionTokens != null) {
            parameters.put("max_completion_tokens", maxCompletionTokens);
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
     * Builder for the {@code openrouter:fusion} server tool. Only explicitly
     * configured fields are emitted; a verbatim {@link #option(String, Object)}
     * escape hatch covers configuration keys this library does not know yet
     * (e.g. the inner {@code tools} list or {@code cache_control}).
     */
    public static final class Builder {

        private List<String> analysisModels;
        private String model;
        private Integer maxToolCalls;
        private Integer maxCompletionTokens;
        private String reasoningEffort;
        private Integer reasoningMaxTokens;
        private Double temperature;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code parameters.analysis_models}: slugs of the parallel
         * expert panel (1-8 models; each receives the user prompt with web
         * search + fetch enabled). Defaults to the Quality preset from the
         * /labs/fusion UI when unset.
         */
        public Builder analysisModels(List<String> modelSlugs) {
            this.analysisModels = modelSlugs;
            return this;
        }

        /**
         * Sets {@code parameters.model}: the slug of the analyst model
         * producing the structured analysis JSON. Defaults to the model used
         * in the outer API request when unset.
         */
        public Builder model(String modelSlug) {
            this.model = modelSlug;
            return this;
        }

        /**
         * Sets {@code parameters.max_tool_calls}: the agentic research-loop
         * budget per panelist and the analyst (1-16, default 4).
         */
        public Builder maxToolCalls(Integer maxToolCalls) {
            this.maxToolCalls = maxToolCalls;
            return this;
        }

        /**
         * Sets {@code parameters.max_completion_tokens}: maximum output tokens
         * (including reasoning) each panelist and the analyst may produce per
         * inner call (default 16000).
         */
        public Builder maxCompletionTokens(Integer maxCompletionTokens) {
            this.maxCompletionTokens = maxCompletionTokens;
            return this;
        }

        /**
         * Sets {@code parameters.reasoning.effort}: reasoning effort level for
         * panelist and analyst inner calls ({@code max}, {@code xhigh},
         * {@code high}, {@code medium}, {@code low}, {@code minimal},
         * {@code none}).
         */
        public Builder reasoningEffort(String effort) {
            this.reasoningEffort = effort;
            return this;
        }

        /**
         * Sets {@code parameters.reasoning.max_tokens}: maximum reasoning
         * tokens per panelist and the analyst - bounds the cost of
         * chain-of-thought-heavy models.
         */
        public Builder reasoningMaxTokens(Integer maxTokens) {
            this.reasoningMaxTokens = maxTokens;
            return this;
        }

        /**
         * Sets {@code parameters.temperature}: sampling temperature forwarded
         * to panelist inner calls. Trap: the analyst always runs at
         * temperature 0 regardless of this value.
         */
        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        /**
         * Adds a {@code parameters} entry verbatim - escape hatch for
         * configuration keys this library does not know yet (e.g. the inner
         * {@code tools} list or {@code cache_control}). Null values are
         * emitted as JSON {@code null}.
         */
        public Builder option(String key, Object value) {
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the {@link OpenRouterFusionServerTool}.
         */
        public OpenRouterFusionServerTool build() {
            return new OpenRouterFusionServerTool(this);
        }
    }
}
