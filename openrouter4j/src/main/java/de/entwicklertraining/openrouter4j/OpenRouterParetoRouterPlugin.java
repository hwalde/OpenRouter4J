package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed implementation of the OpenRouter {@code pareto-router} plugin: routes
 * to the best-scoring frontier coding model on the Pareto frontier of quality
 * and price. Emitted into the {@code plugins} request array as
 * {@code {"id": "pareto-router", ...}} with only the explicitly configured
 * fields.
 * <p>
 * JSON field: {@code plugins[].id = "pareto-router"}. Default: no plugin is
 * sent.
 * <p>
 * Traps: when {@code max_price} is set, quality-tier selection
 * ({@code min_coding_score}) is bypassed - price-based selection takes over.
 * The API returns 404 when no candidate satisfies the cap.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins">Plugins</a>
 */
public final class OpenRouterParetoRouterPlugin implements OpenRouterPlugin {

    /** The plugin discriminator emitted as {@code plugins[].id}. */
    public static final String PLUGIN_ID = "pareto-router";

    private final Boolean enabled;
    private final Double maxPrice;
    private final Double minCodingScore;
    private final String priceSource;
    private final Map<String, Object> extraOptions;

    private OpenRouterParetoRouterPlugin(Builder builder) {
        this.enabled = builder.enabled;
        this.maxPrice = builder.maxPrice;
        this.minCodingScore = builder.minCodingScore;
        this.priceSource = builder.priceSource;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code pareto-router} plugin.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the plugin discriminator {@code "pareto-router"} ({@code plugins[].id}). */
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
     * Returns the configured {@code max_price} value (maximum input price in
     * USD per million tokens; bypasses {@code min_coding_score} when set), or
     * {@code null} when unset.
     */
    public Double maxPrice() {
        return maxPrice;
    }

    /**
     * Returns the configured {@code min_coding_score} value (0-1; maps to
     * internal quality tiers; defaults to the highest tier), or {@code null}
     * when unset. Not used when {@code max_price} is set.
     */
    public Double minCodingScore() {
        return minCodingScore;
    }

    /**
     * Returns the configured {@code price_source} value
     * ({@code "prompt"} for catalog list price or {@code "weighted_avg"} for
     * traffic-weighted effective input price), or {@code null} when unset
     * (defaults to {@code "prompt"}).
     */
    public String priceSource() {
        return priceSource;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", PLUGIN_ID);
        if (enabled != null) {
            json.put("enabled", enabled);
        }
        if (maxPrice != null) {
            json.put("max_price", maxPrice);
        }
        if (minCodingScore != null) {
            json.put("min_coding_score", minCodingScore);
        }
        if (priceSource != null) {
            json.put("price_source", priceSource);
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            json.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return json;
    }

    /**
     * Builder for the {@code pareto-router} plugin. Only explicitly configured
     * fields are emitted; a verbatim {@link #option(String, Object)} escape
     * hatch covers keys this library does not know yet.
     */
    public static final class Builder {

        private Boolean enabled;
        private Double maxPrice;
        private Double minCodingScore;
        private String priceSource;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
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
         * Sets {@code max_price}: maximum input price in USD per million
         * tokens. When set, quality-tier selection is bypassed and the router
         * falls back through cheaper frontier models; the API returns 404 when
         * no candidate satisfies the cap.
         */
        public Builder maxPrice(Double maxPrice) {
            this.maxPrice = maxPrice;
            return this;
        }

        /**
         * Sets {@code min_coding_score}: minimum coding quality score between
         * 0 and 1 ({@code >= 0.66} high, {@code >= 0.33} medium, lower low;
         * omitted defaults to the highest tier). Not used when
         * {@link #maxPrice(Double)} is set.
         */
        public Builder minCodingScore(Double minCodingScore) {
            this.minCodingScore = minCodingScore;
            return this;
        }

        /**
         * Sets {@code price_source}: the price axis of the Pareto frontier and
         * the enforcement source for {@code max_price}. {@code "prompt"} uses
         * catalog list price; {@code "weighted_avg"} uses traffic-weighted
         * effective input price. Default: unset ({@code "prompt"} applies).
         */
        public Builder priceSource(String priceSource) {
            this.priceSource = priceSource;
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
         * Builds the {@link OpenRouterParetoRouterPlugin} value type.
         */
        public OpenRouterParetoRouterPlugin build() {
            return new OpenRouterParetoRouterPlugin(this);
        }
    }
}
