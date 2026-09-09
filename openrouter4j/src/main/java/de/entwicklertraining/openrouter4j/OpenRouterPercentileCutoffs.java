package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Percentile-specific performance cutoffs used inside the provider preferences
 * {@code preferred_max_latency} and {@code preferred_min_throughput} when a
 * plain number is not specific enough. Each percentile key is emitted only
 * when explicitly configured; the object is emitted only when at least one
 * percentile is set.
 * <p>
 * Semantics (OpenRouter routing): endpoints beyond the threshold are still
 * usable but deprioritized - never excluded. With fallback models, a
 * better-performing fallback may therefore be chosen over the primary model.
 *
 * @see <a href="https://openrouter.ai/docs/guides/routing/provider-selection">Provider selection</a>
 */
public final class OpenRouterPercentileCutoffs {

    private final Double p50;
    private final Double p75;
    private final Double p90;
    private final Double p99;

    private OpenRouterPercentileCutoffs(Double p50, Double p75, Double p90, Double p99) {
        this.p50 = p50;
        this.p75 = p75;
        this.p90 = p90;
        this.p99 = p99;
    }

    /**
     * Creates a new, empty builder for percentile cutoffs.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the configured 50th-percentile cutoff, or {@code null} when unset.
     */
    public Double p50() {
        return p50;
    }

    /**
     * Returns the configured 75th-percentile cutoff, or {@code null} when unset.
     */
    public Double p75() {
        return p75;
    }

    /**
     * Returns the configured 90th-percentile cutoff, or {@code null} when unset.
     */
    public Double p90() {
        return p90;
    }

    /**
     * Returns the configured 99th-percentile cutoff, or {@code null} when unset.
     */
    public Double p99() {
        return p99;
    }

    /**
     * Returns the JSON object emitted as the value of
     * {@code provider.preferred_max_latency} or
     * {@code provider.preferred_min_throughput}. Percentile keys are emitted
     * only when set; the object is empty when nothing is configured.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        if (p50 != null) {
            json.put("p50", p50);
        }
        if (p75 != null) {
            json.put("p75", p75);
        }
        if (p90 != null) {
            json.put("p90", p90);
        }
        if (p99 != null) {
            json.put("p99", p99);
        }
        return json;
    }

    /** Builder for {@link OpenRouterPercentileCutoffs}. */
    public static final class Builder {

        private Double p50;
        private Double p75;
        private Double p90;
        private Double p99;

        private Builder() {
        }

        /**
         * Sets the 50th-percentile cutoff.
         */
        public Builder p50(Double value) {
            this.p50 = value;
            return this;
        }

        /**
         * Sets the 75th-percentile cutoff.
         */
        public Builder p75(Double value) {
            this.p75 = value;
            return this;
        }

        /**
         * Sets the 90th-percentile cutoff.
         */
        public Builder p90(Double value) {
            this.p90 = value;
            return this;
        }

        /**
         * Sets the 99th-percentile cutoff.
         */
        public Builder p99(Double value) {
            this.p99 = value;
            return this;
        }

        /**
         * Builds the {@link OpenRouterPercentileCutoffs} value type.
         */
        public OpenRouterPercentileCutoffs build() {
            return new OpenRouterPercentileCutoffs(p50, p75, p90, p99);
        }
    }
}
