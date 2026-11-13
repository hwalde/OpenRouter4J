package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Typed implementation of the OpenRouter {@code jev-router} plugin (schema
 * {@code JevRouterPlugin}): configures server-side model-pool filtering and
 * cost-tier routing for the Jev (TypeSafe) router ({@code typesafe/jev-router}
 * or {@code <author>/auto}). Emitted into the {@code plugins} request array as
 * {@code {"id": "jev-router", ...}} with only the explicitly configured fields.
 * <p>
 * JSON field: {@code plugins[].id = "jev-router"} (supported on Chat
 * Completions, Responses and Anthropic Messages). Default: no plugin is sent.
 * <p>
 * Traps:
 * <ul>
 *   <li><b>Include preferences with fallback vs. hard exclusions:</b>
 *       {@code models} and {@code allowed_models} (an alias matching the
 *       {@code auto-router} field name; entries from both fields are combined
 *       server-side when both are sent) are <i>include preferences with
 *       fallback</i>, not hard allowlists. Models outside the router's current
 *       pool are ignored; if the include lists match catalog models but leave
 *       no routable pool candidate, Jev relaxes the include lists and retries
 *       against the full pool (reporting
 *       {@code list_fallback: "models_ignored"} in
 *       {@code openrouter_metadata.pipeline}). However, when <b>no</b> entry in
 *       {@code models} / {@code allowed_models} matches any public text model
 *       in the catalog (for example a misspelled slug), the request fails with
 *       HTTP 404 instead of falling back. {@code excluded_models} is a
 *       <b>hard restriction</b> applied after {@code models} /
 *       {@code allowed_models} and is never relaxed; if exclusions leave no
 *       eligible model, the request fails with HTTP 404.</li>
 *   <li><b>Pattern syntax:</b> each entry in {@code models},
 *       {@code allowed_models} and {@code excluded_models} may be an exact
 *       model slug (e.g. {@code "openai/gpt-5.6-sol"}), a dated revision, a
 *       case-sensitive wildcard pattern (e.g. {@code "anthropic/*"} or
 *       {@code "*flash*"}), or a {@code ~author/family-latest} alias matching
 *       every revision of that family. Each list accepts up to 1,024 patterns,
 *       each at most 1,024 characters (65,536 characters total per list).</li>
 *   <li><b>{@code cost_tier} override semantics:</b> selects {@code "low"},
 *       {@code "medium"}, {@code "high"} or an operator-configured cost tier
 *       (also {@code "xhigh"} / {@code "max"} where configured). Overrides the
 *       live-config tier and replaces the shared routing policy with that tier
 *       (selecting routing policy, not the model's reasoning effort; omit to
 *       use the live configuration). Model exclusions and the router kill
 *       switch still apply; an unrecognized tier name returns HTTP 400.</li>
 *   <li><b>Reasoning and availability:</b> {@code reasoning.max_tokens} and
 *       {@code reasoning.mode: "pro"} are not supported with Jev Router and
 *       return HTTP 400 (use {@code reasoning.effort} as a hint instead). Jev
 *       Router is available only in the global data region; regional routing or
 *       combining multiple router models in one request returns HTTP 400.</li>
 * </ul>
 *
 * @see <a href="https://openrouter.ai/docs/guides/routing/routers/jev-router">Jev Router guide</a>
 * @see <a href="https://openrouter.ai/docs/guides/community/jev">Jev community guide</a>
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins">OpenRouter plugins</a>
 */
public final class OpenRouterJevRouterPlugin implements OpenRouterPlugin {

    /** The plugin discriminator emitted as {@code plugins[].id}. */
    public static final String PLUGIN_ID = "jev-router";

    private final List<String> models;
    private final List<String> allowedModels;
    private final List<String> excludedModels;
    private final String costTier;
    private final Map<String, Object> extraOptions;

    private OpenRouterJevRouterPlugin(Builder builder) {
        this.models = builder.models == null ? null : List.copyOf(builder.models);
        this.allowedModels = builder.allowedModels == null ? null : List.copyOf(builder.allowedModels);
        this.excludedModels = builder.excludedModels == null ? null : List.copyOf(builder.excludedModels);
        this.costTier = builder.costTier;
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
     * Creates the {@code jev-router} plugin with no extra fields
     * ({@code {"id": "jev-router"}}).
     */
    public OpenRouterJevRouterPlugin() {
        this(new Builder());
    }

    /**
     * Creates a new builder for the {@code jev-router} plugin.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the plugin discriminator {@code "jev-router"} ({@code plugins[].id}). */
    @Override
    public String id() {
        return PLUGIN_ID;
    }

    /**
     * Returns the configured {@code models} patterns (model slugs, wildcards
     * such as {@code "anthropic/*"}, or {@code ~author/family-latest} aliases),
     * empty when unset (never {@code null}).
     *
     * @return the configured models patterns, or an empty list when unset
     */
    public List<String> models() {
        return models == null ? List.of() : models;
    }

    /**
     * Returns the configured {@code allowed_models} patterns (alias of
     * {@code models}; entries from both fields are combined server-side),
     * empty when unset (never {@code null}).
     *
     * @return the configured allowed_models patterns, or an empty list when unset
     */
    public List<String> allowedModels() {
        return allowedModels == null ? List.of() : allowedModels;
    }

    /**
     * Returns the configured {@code excluded_models} patterns (hard exclusions
     * applied after {@code models} and {@code allowed_models}), empty when
     * unset (never {@code null}).
     *
     * @return the configured excluded_models patterns, or an empty list when unset
     */
    public List<String> excludedModels() {
        return excludedModels == null ? List.of() : excludedModels;
    }

    /**
     * Returns the configured {@code cost_tier} value ({@code "low"},
     * {@code "medium"}, {@code "high"} or an operator-configured tier), or
     * {@code null} when unset (the router's live configuration applies).
     *
     * @return the cost tier, or {@code null} when unset
     */
    public String costTier() {
        return costTier;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", PLUGIN_ID);
        if (models != null && !models.isEmpty()) {
            json.put("models", new JSONArray(models));
        }
        if (allowedModels != null && !allowedModels.isEmpty()) {
            json.put("allowed_models", new JSONArray(allowedModels));
        }
        if (excludedModels != null && !excludedModels.isEmpty()) {
            json.put("excluded_models", new JSONArray(excludedModels));
        }
        if (costTier != null) {
            json.put("cost_tier", costTier);
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
     * Builder for {@link OpenRouterJevRouterPlugin}. Only explicitly configured
     * fields are emitted; empty lists clear previously set patterns and omit
     * the key (matching {@link OpenRouterAutoRouterPlugin}). A verbatim
     * {@link #option(String, Object)} escape hatch covers keys this library
     * does not know yet.
     */
    public static final class Builder {

        private List<String> models;
        private List<String> allowedModels;
        private List<String> excludedModels;
        private String costTier;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code plugins[].models}: model slugs, wildcard patterns (e.g.
         * {@code "anthropic/*"}) or {@code ~author/family-latest} aliases to
         * restrict the router to (matched against the current pool; models
         * outside the pool are ignored, and if no pool model matches the list
         * is relaxed unless no entry matches any catalog model, which fails
         * with HTTP 404). Combined server-side with {@link #allowedModels(List)}
         * when both are set.
         * <p>
         * Passing {@code null} or an empty list clears previously configured
         * patterns and omits the {@code models} key from the emitted JSON
         * (matching {@link OpenRouterAutoRouterPlugin}). Because both
         * {@code models(List)} and {@code models(String...)} exist, a bare
         * {@code models(null)} call is ambiguous at compile time - use
         * {@code models(List.of())}, {@code models()} or
         * {@code models((List<String>) null)}.
         *
         * @param patterns the model patterns (up to 1,024 entries), or
         *                 {@code null} / empty to clear
         * @return this builder
         */
        public Builder models(List<String> patterns) {
            this.models = copyNonEmptyList(patterns, "models");
            return this;
        }

        /**
         * Varargs overload of {@link #models(List)}. Passing no arguments or a
         * {@code null} array clears previously configured patterns and omits
         * the {@code models} key.
         *
         * @param patterns the model patterns
         * @return this builder
         */
        public Builder models(String... patterns) {
            if (patterns == null || patterns.length == 0) {
                this.models = null;
                return this;
            }
            return models(Arrays.asList(patterns));
        }

        /**
         * Sets {@code plugins[].allowed_models}: alias of {@link #models(List)}
         * matching the {@code auto-router} field name. Settable independently
         * of {@code models}; when both {@code models} and
         * {@code allowed_models} are configured, both keys are emitted and
         * their entries are combined server-side.
         * <p>
         * Passing {@code null} or an empty list clears previously configured
         * patterns and omits the {@code allowed_models} key (matching
         * {@link OpenRouterAutoRouterPlugin}). For a literal {@code null},
         * cast to {@code (List<String>) null} or call {@code allowedModels()}
         * / {@code allowedModels(List.of())}.
         *
         * @param patterns the allowed model patterns (up to 1,024 entries), or
         *                 {@code null} / empty to clear
         * @return this builder
         */
        public Builder allowedModels(List<String> patterns) {
            this.allowedModels = copyNonEmptyList(patterns, "allowedModels");
            return this;
        }

        /**
         * Varargs overload of {@link #allowedModels(List)}. Passing no
         * arguments or a {@code null} array clears previously configured
         * patterns and omits the {@code allowed_models} key.
         *
         * @param patterns the allowed model patterns
         * @return this builder
         */
        public Builder allowedModels(String... patterns) {
            if (patterns == null || patterns.length == 0) {
                this.allowedModels = null;
                return this;
            }
            return allowedModels(Arrays.asList(patterns));
        }

        /**
         * Sets {@code plugins[].excluded_models}: hard exclusions removed from
         * the router pool (exact model slugs, wildcards such as
         * {@code "xiaomi/*"}, or {@code ~author/family-latest} aliases).
         * Applied after {@code models} and {@code allowed_models}, so an
         * excluded pattern always wins; when exclusions leave no eligible
         * model, the request fails with HTTP 404 rather than routing outside
         * them.
         * <p>
         * Passing {@code null} or an empty list clears previously configured
         * patterns and omits the {@code excluded_models} key (matching
         * {@link OpenRouterAutoRouterPlugin}). For a literal {@code null},
         * cast to {@code (List<String>) null} or call
         * {@code excludedModels()} / {@code excludedModels(List.of())}.
         *
         * @param patterns the excluded model patterns (up to 1,024 entries),
         *                 or {@code null} / empty to clear
         * @return this builder
         */
        public Builder excludedModels(List<String> patterns) {
            this.excludedModels = copyNonEmptyList(patterns, "excludedModels");
            return this;
        }

        /**
         * Varargs overload of {@link #excludedModels(List)}. Passing no
         * arguments or a {@code null} array clears previously configured
         * patterns and omits the {@code excluded_models} key.
         *
         * @param patterns the excluded model patterns
         * @return this builder
         */
        public Builder excludedModels(String... patterns) {
            if (patterns == null || patterns.length == 0) {
                this.excludedModels = null;
                return this;
            }
            return excludedModels(Arrays.asList(patterns));
        }

        private static List<String> copyNonEmptyList(List<String> patterns, String fieldName) {
            if (patterns == null || patterns.isEmpty()) {
                return null;
            }
            for (String p : patterns) {
                if (p == null || p.isEmpty()) {
                    throw new IllegalArgumentException(fieldName + " entries must not be null or empty");
                }
            }
            return List.copyOf(patterns);
        }

        /**
         * Sets {@code plugins[].cost_tier}: selects {@code "low"},
         * {@code "medium"}, {@code "high"}, or a cost tier configured by the
         * operator (e.g. {@code "xhigh"}, {@code "max"} or a custom operator
         * tier; passed through verbatim without hard-restricting to the
         * built-in names). Overrides the live-config tier and replaces the
         * shared routing policy with that tier. Default: unset (the live
         * configuration applies); {@code null} unsets the field.
         *
         * @param tier the cost tier name (non-empty), or {@code null} to unset
         * @return this builder
         */
        public Builder costTier(String tier) {
            if (tier == null) {
                this.costTier = null;
                return this;
            }
            if (tier.isEmpty()) {
                throw new IllegalArgumentException("costTier must not be empty");
            }
            this.costTier = tier;
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
         * Builds the {@link OpenRouterJevRouterPlugin} value type.
         *
         * @return the built plugin
         */
        public OpenRouterJevRouterPlugin build() {
            return new OpenRouterJevRouterPlugin(this);
        }
    }
}
