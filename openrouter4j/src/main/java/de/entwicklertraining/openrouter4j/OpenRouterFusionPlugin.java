package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Typed implementation of the OpenRouter {@code fusion} plugin: configures a
 * Fusion run - a panel of analysis models researches the prompt in parallel
 * (each with web search and fetch), then an analyst model summarizes their
 * output as structured JSON. Emitted into the {@code plugins} request array as
 * {@code {"id": "fusion", ...}} with only the explicitly configured fields.
 * <p>
 * JSON field: {@code plugins[].id = "fusion"}. Default: no plugin is sent.
 * <p>
 * Traps: {@code analysis_models} is capped at 8 models (cost amplification);
 * {@code max_tool_calls} defaults to 4 and is capped at 16. Explicitly
 * provided {@code analysis_models} / {@code model} take precedence over the
 * curated {@code preset} configuration. Fusion is normally started via the
 * {@code openrouter/fusion} model slug or the {@code openrouter:fusion}
 * server tool ({@code OpenRouterFusionServerTool}).
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins">Plugins</a>
 * @see OpenRouterFusionServerTool
 */
public final class OpenRouterFusionPlugin implements OpenRouterPlugin {

    /** The plugin discriminator emitted as {@code plugins[].id}. */
    public static final String PLUGIN_ID = "fusion";

    private final Boolean enabled;
    private final List<String> analysisModels;
    private final String model;
    private final String preset;
    private final Integer maxToolCalls;
    private final Map<String, Object> extraOptions;

    private OpenRouterFusionPlugin(Builder builder) {
        this.enabled = builder.enabled;
        this.analysisModels = builder.analysisModels == null ? null : List.copyOf(builder.analysisModels);
        this.model = builder.model;
        this.preset = builder.preset;
        this.maxToolCalls = builder.maxToolCalls;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code fusion} plugin.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the plugin discriminator {@code "fusion"} ({@code plugins[].id}). */
    @Override
    public String id() {
        return PLUGIN_ID;
    }

    /**
     * Returns the configured {@code enabled} value, or {@code null} when unset
     * (the key is not sent and {@code true} applies).
     */
    public Boolean enabled() {
        return enabled;
    }

    /**
     * Returns the configured {@code analysis_models} panel slugs (1-8 models;
     * each receives the prompt with web search + fetch enabled), empty when
     * unset (never {@code null}).
     */
    public List<String> analysisModels() {
        return analysisModels == null ? List.of() : analysisModels;
    }

    /**
     * Returns the configured {@code model} slug - the analyst model performing
     * both the analysis step and the final synthesis - or {@code null} when
     * unset (defaults to the first model of the Quality preset).
     */
    public String model() {
        return model;
    }

    /**
     * Returns the configured curated {@code preset} slug
     * ({@code "general-high"}, {@code "general-budget"} or
     * {@code "general-fast"}), or {@code null} when unset.
     */
    public String preset() {
        return preset;
    }

    /**
     * Returns the configured {@code max_tool_calls} value (1-16, default 4) -
     * the agentic research-loop budget per panelist and the analyst - or
     * {@code null} when unset.
     */
    public Integer maxToolCalls() {
        return maxToolCalls;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", PLUGIN_ID);
        if (enabled != null) {
            json.put("enabled", enabled);
        }
        if (analysisModels != null && !analysisModels.isEmpty()) {
            json.put("analysis_models", new JSONArray(analysisModels));
        }
        if (model != null) {
            json.put("model", model);
        }
        if (preset != null) {
            json.put("preset", preset);
        }
        if (maxToolCalls != null) {
            json.put("max_tool_calls", maxToolCalls);
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            json.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return json;
    }

    /**
     * Builder for the {@code fusion} plugin. Only explicitly configured fields
     * are emitted; a verbatim {@link #option(String, Object)} escape hatch
     * covers keys this library does not know yet.
     */
    public static final class Builder {

        private Boolean enabled;
        private List<String> analysisModels;
        private String model;
        private String preset;
        private Integer maxToolCalls;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code enabled}: set to {@code false} to disable the Fusion
         * configuration for this run. Default: unset ({@code true} applies).
         */
        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets {@code analysis_models}: slugs of the parallel expert panel
         * (1-8 models). Explicitly provided models take precedence over the
         * curated {@link #preset(String)} configuration.
         */
        public Builder analysisModels(List<String> modelSlugs) {
            this.analysisModels = modelSlugs;
            return this;
        }

        /**
         * Sets {@code model}: the slug of the analyst model performing both
         * the analysis step and the final synthesis. Explicitly provided
         * models take precedence over the curated {@link #preset(String)}
         * configuration.
         */
        public Builder model(String modelSlug) {
            this.model = modelSlug;
            return this;
        }

        /**
         * Sets {@code preset}: a curated OpenRouter Fusion preset (slugs
         * follow {@code <task>-<tier>}; schema values {@code "general-high"},
         * {@code "general-budget"}, {@code "general-fast"}). Expands
         * server-side into the preset's panel and analyst model.
         */
        public Builder preset(String presetSlug) {
            this.preset = presetSlug;
            return this;
        }

        /**
         * Sets {@code max_tool_calls}: the agentic research-loop budget per
         * panelist and the analyst (1-16, default 4).
         */
        public Builder maxToolCalls(Integer maxToolCalls) {
            this.maxToolCalls = maxToolCalls;
            return this;
        }

        /**
         * Adds a plugin field verbatim - escape hatch for keys this library
         * does not know yet. The key must not be {@code "id"}. Null values are
         * emitted as JSON {@code null}.
         */
        public Builder option(String key, Object value) {
            if ("id".equals(key)) {
                throw new IllegalArgumentException("The 'id' field is set from the plugin type and must not be set via option()");
            }
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the {@link OpenRouterFusionPlugin} value type.
         */
        public OpenRouterFusionPlugin build() {
            return new OpenRouterFusionPlugin(this);
        }
    }
}
