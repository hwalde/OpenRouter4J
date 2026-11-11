package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The request-level {@code deferred_tools} control object that opts a whole
 * conversation into OpenRouter's versioned router-level deferred-tool
 * protocol: the router withholds the deferred tools from the model, serves
 * their discovery with its own {@code tool_search} and call wrappers on any
 * model, keeps the provider-facing tool list stable across the run, and
 * replays assistant reasoning unchanged on continuation turns. It composes
 * with the per-tool {@code defer_loading} flag
 * ({@link OpenRouterToolDefinition.Builder#deferLoading(Boolean)}): the
 * portable profile defers exactly the tools marked that way, the universal
 * profile every wrappable tool (see {@code scope}).
 * <p>
 * JSON field: {@code deferred_tools} on the chat-completions, Responses and
 * Anthropic-Messages requests. Default: unset (the key is not sent and the
 * model sees the full tool list). Required keys on the wire: {@code protocol},
 * {@code profile}, {@code validation}, {@code search}; every other field is
 * emitted only when set ({@code null} unsets it again).
 * <p>
 * <b>Profiles.</b> {@code "portable"} serves the {@code defer_loading} tools
 * with router-built search and call wrappers on any model and keeps the
 * catalog fixed. {@code "universal"} defers every wrappable tool behind the
 * same wrappers so the provider-facing tool list never changes (unless
 * {@code min_deferred_tools} inlines a small catalog) and rebuilds the catalog
 * from each request, so tools can be added or removed mid-conversation. The
 * OpenRouter docs describe only the {@code portable} profile for this release;
 * the published schema additionally declares the {@code universal} fields
 * ({@code announce_catalog_changes}, {@code cascade_mode}, {@code hint},
 * {@code interface}, {@code min_deferred_tools}, {@code scope}) - this class
 * follows the schema's wire shapes.
 * <p>
 * <b>Traps.</b>
 * <ul>
 * <li>The portable profile needs the catalog byte-stable across a
 * continuation - tool order, descriptions and {@code cache_control} markers
 * included; any change is a new conversation.</li>
 * <li>A conversation cannot be enabled partway: once earlier assistant
 * messages carry tool calls, adding {@code deferred_tools} later is rejected -
 * start a new conversation instead.</li>
 * <li>The router answers these errors with "start a new conversation":
 * {@code deferred_tools_state_missing}, {@code deferred_tools_state_invalid},
 * {@code deferred_tools_history_changed}. The fourth code,
 * {@code deferred_tools_catalog_changed}, instead means the request changed
 * {@code tools} / {@code tool_choice} / {@code deferred_tools}
 * mid-conversation.</li>
 * <li>The Batch API rejects {@code deferred_tools} - it is per-conversation
 * state and batches carry none.</li>
 * <li>{@code openrouter:tool_search} in the {@code tools} array is still not
 * accepted by Chat Completions; the {@code deferred_tools} protocol is its
 * replacement there (on the Responses and Anthropic-Messages surfaces the
 * search server tool remains available alongside).</li>
 * <li>The run's true totals arrive in {@code usage.deferred_tools_details}
 * ({@link OpenRouterDeferredToolsDetails}), not in the native usage fields,
 * which report only the final upstream call.</li>
 * </ul>
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools/tool-search">Server tools: tool search</a>
 */
public final class OpenRouterDeferredToolsControl {

    private final String protocol;
    private final String profile;
    private final String validation;
    private final OpenRouterDeferredToolsSearchStrategy search;
    private final Integer minDeferredTools;
    private final String scope;
    private final String cascadeMode;
    private final String interfaceMode;
    private final String hint;
    private final Boolean announceCatalogChanges;
    private final Map<String, Object> options;

    private OpenRouterDeferredToolsControl(Builder builder) {
        this.protocol = builder.protocol;
        this.profile = builder.profile;
        this.validation = builder.validation;
        this.search = builder.search;
        this.minDeferredTools = builder.minDeferredTools;
        this.scope = builder.scope;
        this.cascadeMode = builder.cascadeMode;
        this.interfaceMode = builder.interfaceMode;
        this.hint = builder.hint;
        this.announceCatalogChanges = builder.announceCatalogChanges;
        this.options = new LinkedHashMap<>(builder.options);
    }

    /**
     * Creates a new builder for {@code deferred_tools}. All four required
     * fields ({@code protocol} / {@code profile} / {@code validation} /
     * {@code search}) must be set before {@link Builder#build()} succeeds.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the configured {@code protocol} value, or {@code null} when
     * unset.
     */
    public String protocol() {
        return protocol;
    }

    /**
     * Returns the configured {@code profile} value, or {@code null} when
     * unset.
     */
    public String profile() {
        return profile;
    }

    /**
     * Returns the configured {@code validation} value, or {@code null} when
     * unset.
     */
    public String validation() {
        return validation;
    }

    /**
     * Returns the configured search strategy, or {@code null} when unset.
     */
    public OpenRouterDeferredToolsSearchStrategy search() {
        return search;
    }

    /**
     * Returns the configured {@code min_deferred_tools} value, or {@code null}
     * when unset.
     */
    public Integer minDeferredTools() {
        return minDeferredTools;
    }

    /**
     * Returns the configured {@code scope} value, or {@code null} when unset.
     */
    public String scope() {
        return scope;
    }

    /**
     * Returns the configured {@code cascade_mode} value, or {@code null} when
     * unset.
     */
    public String cascadeMode() {
        return cascadeMode;
    }

    /**
     * Returns the configured {@code interface} value, or {@code null} when
     * unset.
     */
    public String interfaceMode() {
        return interfaceMode;
    }

    /**
     * Returns the configured {@code hint} value, or {@code null} when unset.
     */
    public String hint() {
        return hint;
    }

    /**
     * Returns the configured {@code announce_catalog_changes} value, or
     * {@code null} when unset.
     */
    public Boolean announceCatalogChanges() {
        return announceCatalogChanges;
    }

    /**
     * Returns the raw option value for {@code key}, or {@code null} when
     * unset.
     */
    public Object option(String key) {
        return options.get(key);
    }

    /**
     * Returns the JSON object emitted as the {@code deferred_tools} request
     * field. Set fields are emitted in the typed setters' key names; unset
     * fields are absent ({@code null} is unset, not an explicit JSON
     * {@code null}).
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        if (protocol != null) {
            json.put("protocol", protocol);
        }
        if (profile != null) {
            json.put("profile", profile);
        }
        if (validation != null) {
            json.put("validation", validation);
        }
        if (search != null) {
            json.put("search", search.toJson());
        }
        if (minDeferredTools != null) {
            json.put("min_deferred_tools", minDeferredTools.intValue());
        }
        if (scope != null) {
            json.put("scope", scope);
        }
        if (cascadeMode != null) {
            json.put("cascade_mode", cascadeMode);
        }
        if (interfaceMode != null) {
            json.put("interface", interfaceMode);
        }
        if (hint != null) {
            json.put("hint", hint);
        }
        if (announceCatalogChanges != null) {
            json.put("announce_catalog_changes", announceCatalogChanges.booleanValue());
        }
        for (Map.Entry<String, Object> e : options.entrySet()) {
            json.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return json;
    }

    /** Builder for {@link OpenRouterDeferredToolsControl}. */
    public static final class Builder {

        private String protocol;
        private String profile;
        private String validation;
        private OpenRouterDeferredToolsSearchStrategy search;
        private Integer minDeferredTools;
        private String scope;
        private String cascadeMode;
        private String interfaceMode;
        private String hint;
        private Boolean announceCatalogChanges;
        private final Map<String, Object> options = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code protocol} (required): the deferred-tool protocol
         * version. The schema declares exactly one value - {@code "v1"} - and
         * rejects any other spelling loudly here, consistent with the
         * library's closed-enum contract.
         */
        public Builder protocol(String protocol) {
            if (!"v1".equals(protocol)) {
                throw new IllegalArgumentException("protocol must be \"v1\" (the only value the schema declares), was: " + protocol);
            }
            this.protocol = protocol;
            return this;
        }

        /**
         * Sets {@code profile} (required): {@code "portable"} (the documented
         * profile of this release - router-built search and call wrappers for
         * the {@code defer_loading} tools, fixed catalog) or {@code
         * "universal"} (every wrappable tool deferred, catalog rebuilt per
         * request). Unknown spellings are passed through verbatim - the
         * schema allows unknown values here.
         */
        public Builder profile(String profile) {
            if (profile == null || profile.isEmpty()) {
                throw new IllegalArgumentException("profile must not be null or empty");
            }
            this.profile = profile;
            return this;
        }

        /**
         * Sets {@code validation} (required): where tool-call arguments are
         * checked against the original schema. The schema declares exactly
         * one value - {@code "runtime"} (in OpenRouter) - and any other
         * spelling is rejected loudly here.
         */
        public Builder validation(String validation) {
            if (!"runtime".equals(validation)) {
                throw new IllegalArgumentException("validation must be \"runtime\" (the only value the schema declares), was: " + validation);
            }
            this.validation = validation;
            return this;
        }

        /**
         * Sets {@code search} (required): the discriminated search strategy -
         * {@link OpenRouterDeferredToolsSearchStrategy#regex()},
         * {@link OpenRouterDeferredToolsSearchStrategy#bm25()} or the
         * caller-owned {@link OpenRouterDeferredToolsSearchStrategy#custom}.
         * {@code null} unsets the field (the build then fails for the missing
         * required field).
         */
        public Builder search(OpenRouterDeferredToolsSearchStrategy search) {
            this.search = search;
            return this;
        }

        /**
         * Sets {@code min_deferred_tools} (universal profile only): when
         * fewer tools than this would be deferred, the universal profile
         * defers none of them and sends the client tools unchanged. Counted
         * after the interface exclusions; ignored with a custom search.
         * Absent: every wrappable tool in scope is deferred. {@code null}
         * unsets the field.
         */
        public Builder minDeferredTools(Integer minDeferredTools) {
            this.minDeferredTools = minDeferredTools;
            return this;
        }

        /**
         * Sets {@code scope} (universal profile only): which tools the
         * universal profile defers - {@code "all"} (every wrappable tool
         * outside the coding-harness core set; the schema's default when
         * absent) or {@code "mcp"} (only MCP server tools). Unknown spellings
         * are passed through verbatim ({@code null} unsets the field). Note
         * the task-to-spec drift: some documentation mentions a {@code
         * "client"} scope; the published schema declares only {@code all} /
         * {@code mcp}, so {@code "client"} travels as an unknown spelling at
         * the caller's risk.
         */
        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        /**
         * Sets {@code cascade_mode} (experimental): how a universal
         * {@code tool_search} query reaches a client-owned tool search -
         * {@code "keywords"} (the schema's default when absent: a {@code
         * select:} query becomes the words of the selected names) or
         * {@code "native"} (the query passes through verbatim). Unknown
         * spellings are passed through verbatim; {@code null} unsets the
         * field.
         */
        public Builder cascadeMode(String cascadeMode) {
            this.cascadeMode = cascadeMode;
            return this;
        }

        /**
         * Sets {@code interface} (universal profile only, experimental): how
         * the hidden catalog is exposed to the provider - {@code "wrappers"}
         * (the schema's default when absent: the {@code tool_search} and
         * {@code tool_exec} tools) or {@code "code_mode"} (one
         * {@code tool_code} tool running a short JavaScript program in which
         * {@code search()} and {@code call()} reach the catalog). The wire
         * key is {@code interface}; the method carries the {@code Mode}
         * suffix because {@code interface} is a Java keyword. Unknown
         * spellings are passed through verbatim; {@code null} unsets the
         * field.
         */
        public Builder interfaceMode(String interfaceMode) {
            this.interfaceMode = interfaceMode;
            return this;
        }

        /**
         * Sets {@code hint} (universal profile only): caller text announced to
         * the model with the deferred catalog before its first turn, e.g.
         * which sources the task needs (1-2000 characters after trimming,
         * validated loudly). Repeated only when it changes; recorded in the
         * response's {@code catalog_index} discovery record so it is replayed
         * with the conversation. {@code null} unsets the field.
         */
        public Builder hint(String hint) {
            if (hint == null) {
                this.hint = null;
                return this;
            }
            String trimmed = hint.trim();
            int length = trimmed.codePointCount(0, trimmed.length());
            if (length < 1 || length > 2000) {
                throw new IllegalArgumentException("hint must be 1-2000 characters after trimming, was: " + length);
            }
            this.hint = hint;
            return this;
        }

        /**
         * Sets {@code announce_catalog_changes} (universal profile only): when
         * {@code true}, a catalog whose deferred tools changed between two of
         * the model's own turns is re-announced before the next model turn.
         * {@code false} is a set option and is emitted (the catalog index is
         * then announced on a user turn only); {@code null} unsets the field
         * (the API default applies).
         */
        public Builder announceCatalogChanges(Boolean announceCatalogChanges) {
            this.announceCatalogChanges = announceCatalogChanges;
            return this;
        }

        /**
         * Sets an arbitrary {@code deferred_tools} key verbatim, for fields
         * without a typed setter. The typed keys ({@code protocol},
         * {@code profile}, {@code validation}, {@code search},
         * {@code min_deferred_tools}, {@code scope}, {@code cascade_mode},
         * {@code interface}, {@code hint},
         * {@code announce_catalog_changes}) are rejected here - use their
         * setters. A {@code null} value is emitted as a JSON {@code null}
         * (verbatim hatch semantics).
         *
         * @param key the option key as declared by the schema
         * @param value the option value (String, Number, Boolean, JSONObject
         *        or null)
         */
        public Builder option(String key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("option key must not be null");
            }
            switch (key) {
                case "protocol", "profile", "validation", "search",
                     "min_deferred_tools", "scope", "cascade_mode", "interface",
                     "hint", "announce_catalog_changes" -> throw new IllegalArgumentException(
                        "the '" + key + "' field is set through its typed setter and must not be set via option()");
                default -> options.put(key, value);
            }
            return this;
        }

        /**
         * Builds the {@link OpenRouterDeferredToolsControl} value type.
         *
         * @throws IllegalStateException when a required field
         *         ({@code protocol} / {@code profile} / {@code validation} /
         *         {@code search}) is missing
         */
        public OpenRouterDeferredToolsControl build() {
            if (protocol == null) {
                throw new IllegalStateException("protocol is required (the schema declares \"v1\")");
            }
            if (profile == null) {
                throw new IllegalStateException("profile is required (\"portable\" or \"universal\")");
            }
            if (validation == null) {
                throw new IllegalStateException("validation is required (the schema declares \"runtime\")");
            }
            if (search == null) {
                throw new IllegalStateException("search is required (OpenRouterDeferredToolsSearchStrategy.regex()/bm25()/custom(...))");
            }
            return new OpenRouterDeferredToolsControl(this);
        }
    }
}
