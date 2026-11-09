package de.entwicklertraining.openrouter4j.extractions;

import de.entwicklertraining.openrouter4j.OpenRouterPercentileCutoffs;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Provider routing preferences for an Extractions request - the
 * {@code provider} object of {@link OpenRouterExtractionsRequest} (schema
 * {@code ProviderPreferences}, the full set).
 *
 * <p>The {@code provider} object is emitted whenever at least one routing
 * option is set; an unset option never appears in the JSON. The
 * {@code preferred_max_latency} / {@code preferred_min_throughput}
 * thresholds accept either a plain number (applies to p50) or
 * {@link OpenRouterPercentileCutoffs} for percentile-specific cutoffs -
 * endpoints beyond the threshold(s) stay usable but are deprioritized in
 * routing. Money values of {@code max_price} travel as strings
 * (USD per million tokens unless noted otherwise).
 *
 * <p>Traps: {@code sort} has a plain-string form and an object form
 * ({@code {"by": ..., "partition": ...}}); the object form wins when both
 * are set. The same holds for the two forms of the performance thresholds.
 * {@code data_collection} is documented as {@code allow} (default; providers
 * may store and train on the data) or {@code deny} (ZDR-style providers
 * only); unknown spellings pass through verbatim.
 *
 * <p>Alpha: this object is part of the {@code /api/alpha} extractions
 * surface and may change or disappear without a major version of the API.
 */
public final class OpenRouterExtractionProviderPreferences {

    private final List<String> order;
    private final List<String> only;
    private final List<String> ignore;
    private final Boolean requireParameters;
    private final Boolean allowFallbacks;
    private final String dataCollection;
    private final String maxPricePrompt;
    private final String maxPriceCompletion;
    private final String maxPriceImage;
    private final String maxPriceAudio;
    private final String maxPriceRequest;
    private final List<String> quantizations;
    private final String sort;
    private final String sortBy;
    private final String sortPartition;
    private final Boolean enforceDistillableText;
    private final Boolean zdr;
    private final Double preferredMaxLatency;
    private final OpenRouterPercentileCutoffs preferredMaxLatencyCutoffs;
    private final Double preferredMinThroughput;
    private final OpenRouterPercentileCutoffs preferredMinThroughputCutoffs;
    private final JSONObject options;

    private OpenRouterExtractionProviderPreferences(Builder builder) {
        this.order = builder.order.isEmpty() ? null : List.copyOf(builder.order);
        this.only = builder.only.isEmpty() ? null : List.copyOf(builder.only);
        this.ignore = builder.ignore.isEmpty() ? null : List.copyOf(builder.ignore);
        this.requireParameters = builder.requireParameters;
        this.allowFallbacks = builder.allowFallbacks;
        this.dataCollection = builder.dataCollection;
        this.maxPricePrompt = builder.maxPricePrompt;
        this.maxPriceCompletion = builder.maxPriceCompletion;
        this.maxPriceImage = builder.maxPriceImage;
        this.maxPriceAudio = builder.maxPriceAudio;
        this.maxPriceRequest = builder.maxPriceRequest;
        this.quantizations = builder.quantizations.isEmpty() ? null : List.copyOf(builder.quantizations);
        this.sort = builder.sort;
        this.sortBy = builder.sortBy;
        this.sortPartition = builder.sortPartition;
        this.enforceDistillableText = builder.enforceDistillableText;
        this.zdr = builder.zdr;
        this.preferredMaxLatency = builder.preferredMaxLatency;
        this.preferredMaxLatencyCutoffs = builder.preferredMaxLatencyCutoffs;
        this.preferredMinThroughput = builder.preferredMinThroughput;
        this.preferredMinThroughputCutoffs = builder.preferredMinThroughputCutoffs;
        this.options = builder.options == null ? null : new JSONObject(builder.options.toString());
    }

    /**
     * Creates a new, empty builder for the {@code provider} object.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** @return the {@code provider.order} slugs, or {@code null} when unset */
    public List<String> order() {
        return order;
    }

    /** @return the {@code provider.only} slugs, or {@code null} when unset */
    public List<String> only() {
        return only;
    }

    /** @return the {@code provider.ignore} slugs, or {@code null} when unset */
    public List<String> ignore() {
        return ignore;
    }

    /** @return the {@code provider.require_parameters} flag, or {@code null} when unset */
    public Boolean requireParameters() {
        return requireParameters;
    }

    /** @return the {@code provider.allow_fallbacks} flag, or {@code null} when unset */
    public Boolean allowFallbacks() {
        return allowFallbacks;
    }

    /** @return the {@code provider.data_collection} spelling, or {@code null} when unset */
    public String dataCollection() {
        return dataCollection;
    }

    /** @return the {@code provider.max_price.prompt} cap, or {@code null} when unset */
    public String maxPricePrompt() {
        return maxPricePrompt;
    }

    /** @return the {@code provider.max_price.completion} cap, or {@code null} when unset */
    public String maxPriceCompletion() {
        return maxPriceCompletion;
    }

    /** @return the {@code provider.max_price.image} cap, or {@code null} when unset */
    public String maxPriceImage() {
        return maxPriceImage;
    }

    /** @return the {@code provider.max_price.audio} cap, or {@code null} when unset */
    public String maxPriceAudio() {
        return maxPriceAudio;
    }

    /** @return the {@code provider.max_price.request} cap, or {@code null} when unset */
    public String maxPriceRequest() {
        return maxPriceRequest;
    }

    /** @return the {@code provider.quantizations} spellings, or {@code null} when unset */
    public List<String> quantizations() {
        return quantizations;
    }

    /** @return the plain-string {@code provider.sort} form, or {@code null} when unset */
    public String sort() {
        return sort;
    }

    /** @return the {@code provider.sort.by} of the object form, or {@code null} when unset */
    public String sortBy() {
        return sortBy;
    }

    /** @return the {@code provider.sort.partition} of the object form, or {@code null} when unset */
    public String sortPartition() {
        return sortPartition;
    }

    /** @return the {@code provider.enforce_distillable_text} flag, or {@code null} when unset */
    public Boolean enforceDistillableText() {
        return enforceDistillableText;
    }

    /** @return the {@code provider.zdr} flag, or {@code null} when unset */
    public Boolean zdr() {
        return zdr;
    }

    /** @return the plain-number {@code provider.preferred_max_latency} (seconds, p50), or {@code null} when unset */
    public Double preferredMaxLatency() {
        return preferredMaxLatency;
    }

    /** @return the percentile cutoffs form of {@code provider.preferred_max_latency}, or {@code null} when unset */
    public OpenRouterPercentileCutoffs preferredMaxLatencyCutoffs() {
        return preferredMaxLatencyCutoffs;
    }

    /** @return the plain-number {@code provider.preferred_min_throughput} (tokens/s, p50), or {@code null} when unset */
    public Double preferredMinThroughput() {
        return preferredMinThroughput;
    }

    /** @return the percentile cutoffs form of {@code provider.preferred_min_throughput}, or {@code null} when unset */
    public OpenRouterPercentileCutoffs preferredMinThroughputCutoffs() {
        return preferredMinThroughputCutoffs;
    }

    /** @return a copy of the {@code provider.options} object, or {@code null} when unset */
    public JSONObject options() {
        return options == null ? null : new JSONObject(options.toString());
    }

    /**
     * @return the JSON object emitted as the {@code provider} request field;
     *         empty when no routing option is configured
     */
    public JSONObject toJson() {
        JSONObject providerObj = new JSONObject();
        if (order != null) {
            providerObj.put("order", new JSONArray(order));
        }
        if (only != null) {
            providerObj.put("only", new JSONArray(only));
        }
        if (ignore != null) {
            providerObj.put("ignore", new JSONArray(ignore));
        }
        if (requireParameters != null) {
            providerObj.put("require_parameters", requireParameters);
        }
        if (allowFallbacks != null) {
            providerObj.put("allow_fallbacks", allowFallbacks);
        }
        if (dataCollection != null) {
            providerObj.put("data_collection", dataCollection);
        }
        if (maxPricePrompt != null || maxPriceCompletion != null
                || maxPriceImage != null || maxPriceAudio != null || maxPriceRequest != null) {
            JSONObject maxPriceObj = new JSONObject();
            if (maxPricePrompt != null) {
                maxPriceObj.put("prompt", maxPricePrompt);
            }
            if (maxPriceCompletion != null) {
                maxPriceObj.put("completion", maxPriceCompletion);
            }
            if (maxPriceImage != null) {
                maxPriceObj.put("image", maxPriceImage);
            }
            if (maxPriceAudio != null) {
                maxPriceObj.put("audio", maxPriceAudio);
            }
            if (maxPriceRequest != null) {
                maxPriceObj.put("request", maxPriceRequest);
            }
            providerObj.put("max_price", maxPriceObj);
        }
        if (quantizations != null) {
            providerObj.put("quantizations", new JSONArray(quantizations));
        }
        // sort - the object form (with partition) wins over the plain string
        // form when both are set.
        if (sortBy != null) {
            JSONObject sortObj = new JSONObject();
            sortObj.put("by", sortBy);
            sortObj.put("partition", sortPartition);
            providerObj.put("sort", sortObj);
        } else if (sort != null) {
            providerObj.put("sort", sort);
        }
        if (enforceDistillableText != null) {
            providerObj.put("enforce_distillable_text", enforceDistillableText);
        }
        if (zdr != null) {
            providerObj.put("zdr", zdr);
        }
        // Performance thresholds - the plain number form wins over the
        // percentile cutoffs form when both are set.
        if (preferredMaxLatency != null) {
            providerObj.put("preferred_max_latency", preferredMaxLatency);
        } else if (preferredMaxLatencyCutoffs != null) {
            providerObj.put("preferred_max_latency", preferredMaxLatencyCutoffs.toJson());
        }
        if (preferredMinThroughput != null) {
            providerObj.put("preferred_min_throughput", preferredMinThroughput);
        } else if (preferredMinThroughputCutoffs != null) {
            providerObj.put("preferred_min_throughput", preferredMinThroughputCutoffs.toJson());
        }
        if (options != null) {
            providerObj.put("options", new JSONObject(options.toString()));
        }
        return providerObj;
    }

    /** Builder for {@link OpenRouterExtractionProviderPreferences}. */
    public static final class Builder {

        private final List<String> order = new ArrayList<>();
        private final List<String> only = new ArrayList<>();
        private final List<String> ignore = new ArrayList<>();
        private Boolean requireParameters;
        private Boolean allowFallbacks;
        private String dataCollection;
        private String maxPricePrompt;
        private String maxPriceCompletion;
        private String maxPriceImage;
        private String maxPriceAudio;
        private String maxPriceRequest;
        private final List<String> quantizations = new ArrayList<>();
        private String sort;
        private String sortBy;
        private String sortPartition;
        private Boolean enforceDistillableText;
        private Boolean zdr;
        private Double preferredMaxLatency;
        private OpenRouterPercentileCutoffs preferredMaxLatencyCutoffs;
        private Double preferredMinThroughput;
        private OpenRouterPercentileCutoffs preferredMinThroughputCutoffs;
        private JSONObject options;

        private Builder() {
        }

        /**
         * Sets {@code provider.order} - the preferred serving providers in
         * priority order. At least one slug is required.
         *
         * @param providers the provider slugs in priority order
         * @return this builder
         */
        public Builder order(String... providers) {
            this.order.clear();
            this.order.addAll(nonEmptyList(providers));
            return this;
        }

        /**
         * Sets {@code provider.only} - the only providers allowed to serve
         * the request (merged with the account-wide allowed provider
         * settings). At least one slug is required.
         *
         * @param providers the provider slugs
         * @return this builder
         */
        public Builder only(String... providers) {
            this.only.clear();
            this.only.addAll(nonEmptyList(providers));
            return this;
        }

        /**
         * Sets {@code provider.ignore} - providers excluded from serving the
         * request (merged with the account-wide ignored provider settings).
         * At least one slug is required.
         *
         * @param providers the provider slugs to exclude
         * @return this builder
         */
        public Builder ignore(String... providers) {
            this.ignore.clear();
            this.ignore.addAll(nonEmptyList(providers));
            return this;
        }

        /**
         * Sets {@code provider.require_parameters} - reject the request when
         * the serving provider cannot honour every parameter (otherwise
         * providers receive only the parameters they support).
         *
         * @param requireParameters the flag
         * @return this builder
         */
        public Builder requireParameters(Boolean requireParameters) {
            this.requireParameters = requireParameters;
            return this;
        }

        /**
         * Sets {@code provider.allow_fallbacks} - whether backup providers
         * may serve the request when the preferred one is unavailable
         * (default {@code true} on the API side).
         *
         * @param allowFallbacks the flag
         * @return this builder
         */
        public Builder allowFallbacks(Boolean allowFallbacks) {
            this.allowFallbacks = allowFallbacks;
            return this;
        }

        /**
         * Sets {@code provider.data_collection} - {@code allow} (default)
         * permits providers that store user data non-transiently and may
         * train on it; {@code deny} routes only to providers that do not
         * collect user data. Unknown spellings pass through verbatim. If no
         * available provider meets the requirement, the request errors.
         *
         * @param dataCollection {@code allow} or {@code deny}
         * @return this builder
         */
        public Builder dataCollection(String dataCollection) {
            this.dataCollection = dataCollection;
            return this;
        }

        /**
         * Sets {@code provider.max_price.prompt} - the prompt price cap in
         * USD per million prompt tokens (money travels as a string).
         *
         * @param maxPricePrompt the cap
         * @return this builder
         */
        public Builder maxPricePrompt(String maxPricePrompt) {
            this.maxPricePrompt = maxPricePrompt;
            return this;
        }

        /**
         * Sets {@code provider.max_price.completion} - the completion price
         * cap in USD per million completion tokens (money travels as a
         * string).
         *
         * @param maxPriceCompletion the cap
         * @return this builder
         */
        public Builder maxPriceCompletion(String maxPriceCompletion) {
            this.maxPriceCompletion = maxPriceCompletion;
            return this;
        }

        /**
         * Sets {@code provider.max_price.image} - the image price cap in USD
         * per image (money travels as a string).
         *
         * @param maxPriceImage the cap
         * @return this builder
         */
        public Builder maxPriceImage(String maxPriceImage) {
            this.maxPriceImage = maxPriceImage;
            return this;
        }

        /**
         * Sets {@code provider.max_price.audio} - the audio price cap in USD
         * per audio unit (money travels as a string).
         *
         * @param maxPriceAudio the cap
         * @return this builder
         */
        public Builder maxPriceAudio(String maxPriceAudio) {
            this.maxPriceAudio = maxPriceAudio;
            return this;
        }

        /**
         * Sets {@code provider.max_price.request} - the price cap in USD per
         * request (money travels as a string).
         *
         * @param maxPriceRequest the cap
         * @return this builder
         */
        public Builder maxPriceRequest(String maxPriceRequest) {
            this.maxPriceRequest = maxPriceRequest;
            return this;
        }

        /**
         * Sets {@code provider.quantizations} - the accepted quantization
         * levels (e.g. {@code int4}, {@code fp8}). Unknown spellings pass
         * through verbatim. At least one level is required.
         *
         * @param levels the quantization spellings
         * @return this builder
         */
        public Builder quantizations(String... levels) {
            this.quantizations.clear();
            this.quantizations.addAll(nonEmptyList(levels));
            return this;
        }

        /**
         * Sets the plain-string {@code provider.sort} form - the sorting
         * strategy ({@code price}, {@code throughput}, {@code latency} or
         * {@code exacto}; unknown spellings pass through verbatim). The
         * object form set via {@link #sortConfig(String, String)} wins when
         * both are set.
         *
         * @param sort the sorting strategy
         * @return this builder
         */
        public Builder sort(String sort) {
            this.sort = sort;
            return this;
        }

        /**
         * Sets the object form of {@code provider.sort}
         * ({@code {"by": ..., "partition": ...}}). {@code partition} is
         * {@code model} (default; fallback models remain fallbacks) or
         * {@code none} (sort all endpoints together). This form wins over
         * the plain-string {@link #sort(String)} form when both are set.
         *
         * @param by the sorting strategy ({@code price}, {@code throughput},
         *           {@code latency} or {@code exacto})
         * @param partition {@code model} or {@code none}, or {@code null}
         *                  for the default
         * @return this builder
         */
        public Builder sortConfig(String by, String partition) {
            this.sortBy = by;
            this.sortPartition = partition;
            return this;
        }

        /**
         * Sets {@code provider.enforce_distillable_text} - restrict routing
         * to models whose author allows text distillation.
         *
         * @param enforceDistillableText the flag
         * @return this builder
         */
        public Builder enforceDistillableText(Boolean enforceDistillableText) {
            this.enforceDistillableText = enforceDistillableText;
            return this;
        }

        /**
         * Sets {@code provider.zdr} - restrict routing to ZDR (Zero Data
         * Retention) endpoints that do not retain prompts.
         *
         * @param zdr the flag
         * @return this builder
         */
        public Builder zdr(Boolean zdr) {
            this.zdr = zdr;
            return this;
        }

        /**
         * Sets the plain-number {@code provider.preferred_max_latency} in
         * seconds (applies to p50). Endpoints above the threshold may still
         * be used but are deprioritized. The plain number wins over the
         * {@link #preferredMaxLatency(OpenRouterPercentileCutoffs)} form
         * when both are set. A bare {@code preferredMaxLatency(null)} does
         * not compile - the two overloads make it ambiguous; pass a typed
         * {@code null} like {@code preferredMaxLatency((Double) null)} to
         * unset.
         *
         * @param seconds the preferred maximum latency in seconds
         * @return this builder
         */
        public Builder preferredMaxLatency(Double seconds) {
            this.preferredMaxLatency = seconds;
            return this;
        }

        /**
         * Sets the percentile cutoffs form of
         * {@code provider.preferred_max_latency} (seconds per percentile).
         *
         * @param cutoffs the percentile cutoffs
         * @return this builder
         */
        public Builder preferredMaxLatency(OpenRouterPercentileCutoffs cutoffs) {
            this.preferredMaxLatencyCutoffs = cutoffs;
            return this;
        }

        /**
         * Sets the plain-number {@code provider.preferred_min_throughput}
         * in tokens per second (applies to p50). Endpoints below the
         * threshold may still be used but are deprioritized. The plain
         * number wins over the
         * {@link #preferredMinThroughput(OpenRouterPercentileCutoffs)} form
         * when both are set. A bare {@code preferredMinThroughput(null)}
         * does not compile - the two overloads make it ambiguous; pass a
         * typed {@code null} like
         * {@code preferredMinThroughput((Double) null)} to unset.
         *
         * @param tokensPerSecond the preferred minimum throughput
         * @return this builder
         */
        public Builder preferredMinThroughput(Double tokensPerSecond) {
            this.preferredMinThroughput = tokensPerSecond;
            return this;
        }

        /**
         * Sets the percentile cutoffs form of
         * {@code provider.preferred_min_throughput} (tokens per second per
         * percentile).
         *
         * @param cutoffs the percentile cutoffs
         * @return this builder
         */
        public Builder preferredMinThroughput(OpenRouterPercentileCutoffs cutoffs) {
            this.preferredMinThroughputCutoffs = cutoffs;
            return this;
        }

        /**
         * Sets {@code provider.options} - provider-specific options keyed
         * by provider slug, passed through verbatim. The provider segment is
         * case-insensitive; an endpoint tag after {@code /} must match the
         * endpoint slug exactly. Only options for the matched provider are
         * forwarded; unrecognized keys are silently dropped upstream.
         *
         * @param options the provider-slug-keyed options object
         * @return this builder
         */
        public Builder options(JSONObject options) {
            this.options = options;
            return this;
        }

        private static List<String> nonEmptyList(String... values) {
            List<String> list = new ArrayList<>();
            for (String value : values) {
                if (value != null && !value.isEmpty()) {
                    list.add(value);
                }
            }
            if (list.isEmpty()) {
                throw new IllegalArgumentException("at least one provider slug is required");
            }
            return list;
        }

        /**
         * Builds the {@link OpenRouterExtractionProviderPreferences} value type.
         */
        public OpenRouterExtractionProviderPreferences build() {
            return new OpenRouterExtractionProviderPreferences(this);
        }
    }
}
