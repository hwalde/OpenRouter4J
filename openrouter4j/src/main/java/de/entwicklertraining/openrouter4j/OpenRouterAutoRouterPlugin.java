package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Typed implementation of the OpenRouter {@code auto-router} plugin: routes the
 * request automatically to the model best ranked for the classified task type,
 * filtered by the configured model patterns and cost band. Emitted into the
 * {@code plugins} request array as {@code {"id": "auto-router", ...}} with only
 * the explicitly configured fields.
 * <p>
 * JSON field: {@code plugins[].id = "auto-router"}. Default: no plugin is sent.
 * <p>
 * Traps: {@code cost_tier} takes precedence over the deprecated numeric
 * {@code cost_quality_tradeoff} when both are provided (the deprecated field
 * defaults to 9 when no cost setting is given). Model patterns support
 * wildcards ({@code "anthropic/*"} matches all Anthropic models).
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins">Plugins</a>
 */
public final class OpenRouterAutoRouterPlugin implements OpenRouterPlugin {

    /** The plugin discriminator emitted as {@code plugins[].id}. */
    public static final String PLUGIN_ID = "auto-router";

    private final List<String> allowedModels;
    private final List<String> excludedModels;
    private final String costTier;
    private final Integer costQualityTradeoff;
    private final Boolean pinModel;
    private final Boolean enabled;
    private final Map<String, Object> extraOptions;

    private OpenRouterAutoRouterPlugin(Builder builder) {
        this.allowedModels = builder.allowedModels == null ? null : List.copyOf(builder.allowedModels);
        this.excludedModels = builder.excludedModels == null ? null : List.copyOf(builder.excludedModels);
        this.costTier = builder.costTier;
        this.costQualityTradeoff = builder.costQualityTradeoff;
        this.pinModel = builder.pinModel;
        this.enabled = builder.enabled;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code auto-router} plugin.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the plugin discriminator {@code "auto-router"} ({@code plugins[].id}). */
    @Override
    public String id() {
        return PLUGIN_ID;
    }

    /**
     * Returns the configured {@code allowed_models} patterns (wildcards
     * supported, e.g. {@code "anthropic/*"}), empty when unset (never {@code null}).
     */
    public List<String> allowedModels() {
        return allowedModels == null ? List.of() : allowedModels;
    }

    /**
     * Returns the configured {@code excluded_models} patterns (applied after
     * {@code allowed_models}, so an excluded pattern wins), empty when unset
     * (never {@code null}).
     */
    public List<String> excludedModels() {
        return excludedModels == null ? List.of() : excludedModels;
    }

    /**
     * Returns the configured {@code cost_tier} value
     * ({@code "low"}, {@code "medium"}, {@code "high"}, {@code "xhigh"} or
     * {@code "max"}), or {@code null} when unset. Takes precedence over the
     * deprecated {@code cost_quality_tradeoff}.
     */
    public String costTier() {
        return costTier;
    }

    /**
     * Returns the deprecated numeric {@code cost_quality_tradeoff} value
     * (0-10; higher favours cheaper models), or {@code null} when unset.
     *
     * @deprecated the API replaced it with {@link #costTier()}; kept for
     *             callers that still send it.
     */
    @Deprecated
    public Integer costQualityTradeoff() {
        return costQualityTradeoff;
    }

    /**
     * Returns the configured {@code pin_model} value: when {@code true}, the
     * model from the most recent assistant message's {@code model} attribute is
     * reused for subsequent turns; or {@code null} when unset (defaults to
     * {@code false}).
     */
    public Boolean pinModel() {
        return pinModel;
    }

    /**
     * Returns the configured {@code enabled} value, or {@code null} when unset
     * (the key is not sent and {@code true} applies).
     */
    public Boolean enabled() {
        return enabled;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", PLUGIN_ID);
        if (allowedModels != null && !allowedModels.isEmpty()) {
            json.put("allowed_models", new JSONArray(allowedModels));
        }
        if (excludedModels != null && !excludedModels.isEmpty()) {
            json.put("excluded_models", new JSONArray(excludedModels));
        }
        if (costTier != null) {
            json.put("cost_tier", costTier);
        }
        if (costQualityTradeoff != null) {
            json.put("cost_quality_tradeoff", costQualityTradeoff);
        }
        if (pinModel != null) {
            json.put("pin_model", pinModel);
        }
        if (enabled != null) {
            json.put("enabled", enabled);
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            json.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return json;
    }

    /**
     * Builder for the {@code auto-router} plugin. Only explicitly configured
     * fields are emitted; a verbatim {@link #option(String, Object)} escape
     * hatch covers keys this library does not know yet.
     */
    public static final class Builder {

        private List<String> allowedModels;
        private List<String> excludedModels;
        private String costTier;
        private Integer costQualityTradeoff;
        private Boolean pinModel;
        private Boolean enabled;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code allowed_models}: model patterns the auto-router may route
         * between (wildcards supported, e.g. {@code "anthropic/*"}; up to 1024
         * patterns, each at most 1024 chars).
         */
        public Builder allowedModels(List<String> patterns) {
            this.allowedModels = patterns;
            return this;
        }

        /**
         * Sets {@code excluded_models}: model patterns excluded from
         * auto-router selection (applied after {@code allowed_models}, so an
         * excluded pattern wins).
         */
        public Builder excludedModels(List<String> patterns) {
            this.excludedModels = patterns;
            return this;
        }

        /**
         * Sets {@code cost_tier}: named cost/quality setting. Tiers select
         * cost-percentile bands: {@code low} = [0, 20), {@code medium} = [20, 40),
         * {@code high} = [40, 60), {@code xhigh} = [60, 80), {@code max} = [80, 100].
         * Takes precedence over the deprecated {@code cost_quality_tradeoff}.
         *
         * @param tier one of {@code low}, {@code medium}, {@code high}, {@code xhigh}, {@code max}
         */
        public Builder costTier(String tier) {
            Objects.requireNonNull(tier, "tier must not be null");
            this.costTier = tier;
            return this;
        }

        /**
         * Sets the deprecated {@code cost_quality_tradeoff} (0-10; higher
         * favours cheaper models; defaults to 9 when no cost setting is given).
         * Prefer {@link #costTier(String)}.
         *
         * @deprecated the API replaced it with {@code cost_tier}; the field
         *             remains supported but {@code cost_tier} takes precedence.
         */
        @Deprecated
        public Builder costQualityTradeoff(Integer value) {
            this.costQualityTradeoff = value;
            return this;
        }

        /**
         * Sets {@code pin_model}: when {@code true}, reuses the model from the
         * most recent assistant message's {@code model} attribute for
         * subsequent turns. Default: unset ({@code false} applies).
         */
        public Builder pinModel(Boolean pinModel) {
            this.pinModel = pinModel;
            return this;
        }

        /**
         * Sets {@code enabled}: set to {@code false} to disable the plugin for
         * this request. Default: unset ({@code true} applies).
         */
        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Adds a plugin field verbatim - escape hatch for keys this library
         * does not know yet. The key must not be {@code "id"} (set from the
         * plugin type). Null values are emitted as JSON {@code null}.
         */
        public Builder option(String key, Object value) {
            if ("id".equals(key)) {
                throw new IllegalArgumentException("The 'id' field is set from the plugin type and must not be set via option()");
            }
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the {@link OpenRouterAutoRouterPlugin} value type.
         */
        public OpenRouterAutoRouterPlugin build() {
            return new OpenRouterAutoRouterPlugin(this);
        }
    }
}
