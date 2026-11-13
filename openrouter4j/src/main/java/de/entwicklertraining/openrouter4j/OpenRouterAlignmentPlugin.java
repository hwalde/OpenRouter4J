package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Typed implementation of the OpenRouter {@code alignment} plugin (Beta since
 * October 2026, schema {@code AlignmentPlugin}): states the configured rules
 * to the model (when {@code instruct} is {@code true}) and evaluates every
 * assistant message of the request against them using a server-side evaluator
 * model. For each rule, the evaluator assigns a break-probability in
 * {@code [0, 1]}; a rule is broken when its probability is at or above
 * {@code threshold}, and the message outcome is {@code "allowed"} or
 * {@code "blocked"} (or {@code "unavailable"} when the evaluator could not
 * check the message). Emitted into the {@code plugins} request array as
 * {@code {"id": "alignment", "rules": [...], ...}} with only the explicitly
 * configured optional fields.
 * <p>
 * JSON field: {@code plugins[].id = "alignment"} (supported on Chat
 * Completions, Responses and Anthropic Messages). Default: no plugin is sent.
 * <p>
 * Traps:
 * <ul>
 *   <li><b>Beta caveat:</b> until the Alignment plugin is generally available,
 *       its configuration fields, the response {@code alignment} object and
 *       the error shapes ({@code error.metadata.alignment} carrying
 *       {@code calls} and {@code rejected}) may change.</li>
 *   <li><b>Assistant-only evaluation:</b> the evaluator reads only the
 *       assistant message ({@code content}, {@code tool_calls},
 *       {@code refusal}, {@code images}, {@code audio.transcript}), never the
 *       user prompt or external context. A rule that watches only
 *       {@code "content"} is not applicable to a tool-call-only turn whose
 *       {@code content} is {@code null} ({@code applicable: false},
 *       {@code probability: null}, {@code broken: false}). Use workspace
 *       guardrails to filter user input.</li>
 *   <li><b>{@code mode} semantics:</b> {@code "audit"} (the default) evaluates
 *       every message and delivers it alongside {@code alignment.calls};
 *       {@code "enforce"} withholds a blocked message, retries the model call
 *       at the same provider up to {@code max_retries} times (default 1), and
 *       returns a {@code 403 permission_denied} error when no retries remain
 *       (or {@code 503 provider_overloaded} immediately, with no retry, when
 *       the outcome is {@code "unavailable"}). On streaming requests in
 *       {@code enforce} mode, OpenRouter holds the events of a message until
 *       the check finishes (sending {@code : OPENROUTER PROCESSING} SSE
 *       keep-alive comments on Chat Completions).</li>
 *   <li><b>Billing:</b> the evaluator call itself is free, but in
 *       {@code "enforce"} mode every retry is a billed model call (response
 *       {@code usage} sums all model calls of the request), and when
 *       {@code instruct} is {@code true} (the default) the injected system
 *       message listing the rules counts toward prompt tokens on every model
 *       call.</li>
 *   <li><b>Request rejections:</b> OpenRouter rejects requests with more than
 *       one {@code alignment} plugin entry (HTTP 400), requests sent to a
 *       regional In-Region Routing hostname (HTTP 400), requests from a
 *       workspace not admitted to the beta (HTTP 400), and requests from a
 *       workspace with HIPAA enabled or unknown HIPAA status (HTTP 403). Zero
 *       Data Retention ({@code provider.zdr: true} or workspace ZDR) is
 *       supported (the evaluator runs on a zero-retention tenancy).</li>
 *   <li><b>Automatic tool loop:</b> on a chat request whose tools carry
 *       handlers, each loop iteration is a separate request;
 *       {@code alignment()} / {@code alignmentCalls()} on the returned
 *       response cover only the final turn, not the tool-call turns before
 *       it.</li>
 * </ul>
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins/alignment">Alignment plugin guide</a>
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins">OpenRouter plugins</a>
 */
public final class OpenRouterAlignmentPlugin implements OpenRouterPlugin {

    /** The plugin discriminator emitted as {@code plugins[].id}. */
    public static final String PLUGIN_ID = "alignment";

    /** The maximum number of rules allowed by the {@code AlignmentPlugin} schema. */
    public static final int MAX_RULES = 128;

    private final List<Rule> rules;
    private final String mode;
    private final Double threshold;
    private final Boolean instruct;
    private final Integer maxRetries;
    private final Map<String, Object> extraOptions;

    private OpenRouterAlignmentPlugin(Builder builder) {
        this.rules = List.copyOf(builder.rules);
        this.mode = builder.mode;
        this.threshold = builder.threshold;
        this.instruct = builder.instruct;
        this.maxRetries = builder.maxRetries;
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
     * Creates a new builder for the {@code alignment} plugin.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the plugin discriminator {@code "alignment"} ({@code plugins[].id}). */
    @Override
    public String id() {
        return PLUGIN_ID;
    }

    /**
     * Returns the configured {@code rules} list (1-128 entries; immutable).
     *
     * @return the configured alignment rules (never {@code null})
     */
    public List<Rule> rules() {
        return rules;
    }

    /**
     * Returns the configured {@code mode} ({@code "audit"} or
     * {@code "enforce"}), or {@code null} when unset (the API default
     * {@code "audit"} applies).
     *
     * @return the mode, or {@code null} when unset
     */
    public String mode() {
        return mode;
    }

    /**
     * Returns the configured break-probability {@code threshold}
     * ({@code 0.01} to {@code 1.0}), or {@code null} when unset (the API
     * default {@code 0.7} applies).
     *
     * @return the threshold, or {@code null} when unset
     */
    public Double threshold() {
        return threshold;
    }

    /**
     * Returns the configured {@code instruct} flag (whether the rules are
     * injected as a system message on every provider call), or {@code null}
     * when unset (the API default {@code true} applies).
     *
     * @return the instruct flag, or {@code null} when unset
     */
    public Boolean instruct() {
        return instruct;
    }

    /**
     * Returns the configured {@code max_retries} count (provider calls made
     * again after a blocked turn in {@code "enforce"} mode), or {@code null}
     * when unset (the API default {@code 1} applies).
     *
     * @return the max retries count, or {@code null} when unset
     */
    public Integer maxRetries() {
        return maxRetries;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", PLUGIN_ID);
        if (!rules.isEmpty()) {
            JSONArray rulesArr = new JSONArray();
            for (Rule r : rules) {
                rulesArr.put(r.toJson());
            }
            json.put("rules", rulesArr);
        }
        if (mode != null) {
            json.put("mode", mode);
        }
        if (threshold != null) {
            json.put("threshold", threshold);
        }
        if (instruct != null) {
            json.put("instruct", instruct);
        }
        if (maxRetries != null) {
            json.put("max_retries", maxRetries);
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
     * One rule entry in {@code plugins[alignment].rules[]} (schema
     * {@code AlignmentRule}): a {@code rule} instruction (1-200 characters)
     * paired with 1-5 unique {@code watch} assistant-message field names
     * (documented values: {@code "content"}, {@code "tool_calls"},
     * {@code "refusal"}, {@code "images"}, {@code "audio"}).
     */
    public static final class Rule {

        /** Maximum length of {@code rule} in Unicode code points (schema {@code maxLength: 200}). */
        public static final int MAX_RULE_LENGTH = 200;

        /** Maximum number of watched fields per rule (schema {@code maxItems: 5}). */
        public static final int MAX_WATCH_ITEMS = 5;

        private final String rule;
        private final List<String> watch;

        private Rule(String rule, List<String> watch) {
            if (rule == null || rule.isEmpty()) {
                throw new IllegalArgumentException("rule must not be null or empty");
            }
            int codePoints = rule.codePointCount(0, rule.length());
            if (codePoints > MAX_RULE_LENGTH) {
                throw new IllegalArgumentException(
                        "rule must be 1-" + MAX_RULE_LENGTH + " characters, was: " + codePoints);
            }
            if (watch == null || watch.isEmpty()) {
                throw new IllegalArgumentException("watch must contain at least one assistant-message field");
            }
            if (watch.size() > MAX_WATCH_ITEMS) {
                throw new IllegalArgumentException(
                        "watch must contain at most " + MAX_WATCH_ITEMS + " fields, was: " + watch.size());
            }
            Set<String> seen = new LinkedHashSet<>();
            for (String field : watch) {
                if (field == null || field.isEmpty()) {
                    throw new IllegalArgumentException("watch entries must not be null or empty");
                }
                if (!seen.add(field)) {
                    throw new IllegalArgumentException("watch entries must be unique; duplicate: " + field);
                }
            }
            this.rule = rule;
            this.watch = List.copyOf(seen);
        }

        /**
         * Creates an alignment rule watching the given assistant-message
         * fields.
         *
         * @param rule  what the assistant reply must or must not do (1-200
         *              characters)
         * @param watch 1-5 unique assistant-message fields the rule applies to
         *              (documented values: {@code "content"},
         *              {@code "tool_calls"}, {@code "refusal"},
         *              {@code "images"}, {@code "audio"}; unknown spellings
         *              are passed through verbatim per the schema's
         *              {@code x-speakeasy-unknown-values: allow})
         * @return the validated rule
         */
        public static Rule of(String rule, String... watch) {
            if (watch == null) {
                throw new IllegalArgumentException("watch must not be null");
            }
            return new Rule(rule, Arrays.asList(watch));
        }

        /**
         * List-based overload of {@link #of(String, String...)}.
         *
         * @param rule  what the assistant reply must or must not do (1-200
         *              characters)
         * @param watch 1-5 unique assistant-message fields the rule applies to
         * @return the validated rule
         */
        public static Rule of(String rule, List<String> watch) {
            return new Rule(rule, watch);
        }

        /**
         * Returns the rule text ({@code rules[].rule}).
         *
         * @return the rule text
         */
        public String rule() {
            return rule;
        }

        /**
         * Returns the watched assistant-message fields ({@code rules[].watch},
         * immutable).
         *
         * @return the watched fields
         */
        public List<String> watch() {
            return watch;
        }

        /**
         * Serializes this rule as {@code {"rule": ..., "watch": [...]}}.
         *
         * @return the JSON representation
         */
        public JSONObject toJson() {
            return new JSONObject()
                    .put("rule", rule)
                    .put("watch", new JSONArray(watch));
        }
    }

    /**
     * Builder for {@link OpenRouterAlignmentPlugin}. At least one rule is
     * required by the {@code AlignmentPlugin} schema; optional fields are
     * emitted only when explicitly configured. A verbatim
     * {@link #option(String, Object)} escape hatch covers keys this library
     * does not type yet.
     */
    public static final class Builder {

        private final List<Rule> rules = new ArrayList<>();
        private String mode;
        private Double threshold;
        private Boolean instruct;
        private Integer maxRetries;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Appends a rule to {@code plugins[].rules} (1-128 rules required).
         * Each rule is evaluated on its own against assistant messages that
         * carry at least one of the {@code watch} fields.
         * <p>
         * Documented {@code watch} values: {@code "content"},
         * {@code "tool_calls"}, {@code "refusal"}, {@code "images"},
         * {@code "audio"} (1-5 unique fields per rule; unknown field names are
         * passed through verbatim per the schema).
         *
         * @param rule  text stating what the reply must or must not do (1-200
         *              characters, validated loudly)
         * @param watch 1-5 unique assistant-message fields the rule applies to
         * @return this builder
         */
        public Builder rule(String rule, String... watch) {
            return addRule(Rule.of(rule, watch));
        }

        /**
         * List-based overload of {@link #rule(String, String...)}; appends a
         * rule to {@code plugins[].rules}.
         *
         * @param rule  text stating what the reply must or must not do (1-200
         *              characters, validated loudly)
         * @param watch 1-5 unique assistant-message fields the rule applies to
         * @return this builder
         */
        public Builder rule(String rule, List<String> watch) {
            return addRule(Rule.of(rule, watch));
        }

        /**
         * Appends a pre-built {@link Rule} to {@code plugins[].rules}
         * (maximum 128 rules).
         *
         * @param rule the rule to append (must not be {@code null})
         * @return this builder
         */
        public Builder addRule(Rule rule) {
            if (rule == null) {
                throw new IllegalArgumentException("rule must not be null");
            }
            if (this.rules.size() >= MAX_RULES) {
                throw new IllegalArgumentException("rules must contain at most " + MAX_RULES + " entries");
            }
            this.rules.add(rule);
            return this;
        }

        /**
         * Replaces the configured {@code plugins[].rules} list. Passing
         * {@code null} or an empty list clears previously added rules (note
         * that {@link #build()} requires at least one rule unless {@code rules}
         * is supplied via {@link #option(String, Object)}).
         *
         * @param rules the rules to set (up to 128 non-null entries), or
         *              {@code null} / empty to clear
         * @return this builder
         */
        public Builder rules(List<Rule> rules) {
            this.rules.clear();
            if (rules == null || rules.isEmpty()) {
                return this;
            }
            if (rules.size() > MAX_RULES) {
                throw new IllegalArgumentException(
                        "rules must contain at most " + MAX_RULES + " entries, was: " + rules.size());
            }
            for (Rule r : rules) {
                if (r == null) {
                    throw new IllegalArgumentException("rules entries must not be null");
                }
                this.rules.add(r);
            }
            return this;
        }

        /**
         * Sets {@code plugins[].mode}: {@code "audit"} (every turn is
         * evaluated and returned alongside its {@code alignment} outcome; the
         * API default when omitted) or {@code "enforce"} (a turn that breaks a
         * rule is withheld, retried up to {@code max_retries} times, and
         * finally returned as an error). Validated loudly against the two
         * documented modes; {@code null} unsets the field (use
         * {@link #option(String, Object)} if OpenRouter adds another mode
         * before this library types it).
         *
         * @param mode {@code "audit"}, {@code "enforce"}, or {@code null} to
         *             unset
         * @return this builder
         */
        public Builder mode(String mode) {
            if (mode == null) {
                this.mode = null;
                return this;
            }
            if (!"audit".equals(mode) && !"enforce".equals(mode)) {
                throw new IllegalArgumentException(
                        "mode must be \"audit\" or \"enforce\", was: " + mode);
            }
            this.mode = mode;
            return this;
        }

        /**
         * Sets {@code plugins[].threshold}: the break-probability cutoff in
         * {@code [0.01, 1.0]} at or above which a rule is marked broken.
         * Default: unset (the API default {@code 0.7} applies); {@code null}
         * unsets the field.
         * <p>
         * Validated loudly against {@code [0.01, 1.0]} per the schema and API
         * contract (OpenRouter rejects values below {@code 0.01}, which would
         * mark every applicable rule broken on every message).
         *
         * @param threshold probability cutoff in {@code [0.01, 1.0]}, or
         *                  {@code null} to unset
         * @return this builder
         */
        public Builder threshold(Double threshold) {
            if (threshold == null) {
                this.threshold = null;
                return this;
            }
            if (!Double.isFinite(threshold) || threshold < 0.01 || threshold > 1.0) {
                throw new IllegalArgumentException(
                        "threshold must be between 0.01 and 1.0, was: " + threshold);
            }
            this.threshold = threshold;
            return this;
        }

        /**
         * Sets {@code plugins[].instruct}: whether OpenRouter inserts the
         * rules into the conversation as a system message on every provider
         * call. Default: unset (the API default {@code true} applies).
         * {@code false} is a set option and is emitted (keeps the rules out of
         * the model-visible prompt while still evaluating the reply);
         * {@code null} unsets the field.
         *
         * @param instruct {@code true}, {@code false}, or {@code null} to
         *                 unset
         * @return this builder
         */
        public Builder instruct(Boolean instruct) {
            this.instruct = instruct;
            return this;
        }

        /**
         * Sets {@code plugins[].max_retries}: how many times OpenRouter calls
         * the model again after a blocked turn in {@code "enforce"} mode
         * (documented server range: {@code 0} to {@code 3}; with {@code 0} the
         * first blocked turn immediately returns an error). Default: unset
         * (the API default {@code 1} applies); {@code null} unsets the field.
         * Negative values are rejected loudly.
         *
         * @param maxRetries non-negative retry count (e.g. {@code 0}..{@code 3}),
         *                   or {@code null} to unset
         * @return this builder
         */
        public Builder maxRetries(Integer maxRetries) {
            if (maxRetries == null) {
                this.maxRetries = null;
                return this;
            }
            if (maxRetries < 0) {
                throw new IllegalArgumentException(
                        "maxRetries must be >= 0, was: " + maxRetries);
            }
            this.maxRetries = maxRetries;
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
         * Builds the {@link OpenRouterAlignmentPlugin} value type.
         *
         * @return the built plugin
         * @throws IllegalStateException when no rules have been configured
         *                               (and {@code "rules"} was not supplied
         *                               via {@link #option(String, Object)})
         */
        public OpenRouterAlignmentPlugin build() {
            if (rules.isEmpty() && !extraOptions.containsKey("rules")) {
                throw new IllegalStateException(
                        "alignment plugin requires at least one rule (use rule(...) or rules(...))");
            }
            return new OpenRouterAlignmentPlugin(this);
        }
    }
}
