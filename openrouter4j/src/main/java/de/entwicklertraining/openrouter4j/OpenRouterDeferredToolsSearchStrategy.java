package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

/**
 * The search strategy of the {@code deferred_tools} control object - the
 * discriminated {@code DeferredToolsSearchStrategy} union of the OpenRouter
 * schema: router-local {@code regex} retrieval, router-local {@code bm25}
 * retrieval, or the caller-owned {@code custom} search whose results the
 * client feeds back itself.
 * <p>
 * JSON field: {@code deferred_tools.search}. The {@code type} discriminator is
 * required on the wire ({@code regex} / {@code bm25} / {@code custom}).
 * <p>
 * The universal profile's per-strategy tuning knobs are deliberately not
 * typed here and are reachable through {@link #raw(JSONObject)} with their
 * documented JSON keys: {@code miss_sources} (a search that matches nothing
 * also names the deferred sources it covers; boolean, on {@code regex} and
 * {@code bm25}), {@code siblings} (each result also names the sibling tools of
 * every matched source; boolean, on {@code regex} and {@code bm25}) and
 * {@code spread} (results spread across sources instead of ranking purely by
 * score; boolean, on {@code bm25} only). All three are universal-profile-only
 * and marked experimental in the spec.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools/tool-search">Server tools: tool search</a>
 */
public final class OpenRouterDeferredToolsSearchStrategy {

    private final JSONObject json;

    private OpenRouterDeferredToolsSearchStrategy(JSONObject json) {
        this.json = json;
    }

    /**
     * Router-local versioned safe-regex retrieval over the supplied tool
     * definitions: {@code {"type": "regex"}}.
     */
    public static OpenRouterDeferredToolsSearchStrategy regex() {
        return regex(null);
    }

    /**
     * Router-local safe-regex retrieval with a hit cap:
     * {@code {"type": "regex", "max_results": n}}.
     *
     * @param maxResults the maximum number of tools one search may return
     *        (the docs: defaults to {@code 5}, accepts 1-100 - outside that
     *        range the router answers 400; the schema itself declares no
     *        bounds, so the value is not validated here), or {@code null} to
     *        omit the field (the router default applies)
     */
    public static OpenRouterDeferredToolsSearchStrategy regex(Integer maxResults) {
        JSONObject json = new JSONObject();
        json.put("type", "regex");
        if (maxResults != null) {
            json.put("max_results", maxResults.intValue());
        }
        return new OpenRouterDeferredToolsSearchStrategy(json);
    }

    /**
     * Router-local BM25 keyword retrieval: {@code {"type": "bm25"}}.
     */
    public static OpenRouterDeferredToolsSearchStrategy bm25() {
        return bm25(null);
    }

    /**
     * Router-local BM25 keyword retrieval with a hit cap:
     * {@code {"type": "bm25", "max_results": n}}.
     *
     * @param maxResults the maximum number of tools one search may return
     *        (the docs: defaults to {@code 5}, accepts 1-100 - outside that
     *        range the router answers 400; the schema itself declares no
     *        bounds, so the value is not validated here), or {@code null} to
     *        omit the field (the router default applies)
     */
    public static OpenRouterDeferredToolsSearchStrategy bm25(Integer maxResults) {
        JSONObject json = new JSONObject();
        json.put("type", "bm25");
        if (maxResults != null) {
            json.put("max_results", maxResults.intValue());
        }
        return new OpenRouterDeferredToolsSearchStrategy(json);
    }

    /**
     * The caller-owned tool search: {@code {"type": "custom", "tool_id": ...,
     * "result_key": ...}}. The router sends the search request to the client
     * tool named {@code toolId} and reads the found tool identities back from
     * the {@code resultKey} field of its result.
     *
     * @param toolId the name of the client tool that executes the search
     *        (schema {@code minLength: 1})
     * @param resultKey the result field carrying the found tool identities
     *        (schema {@code minLength: 1})
     * @return the built strategy
     */
    public static OpenRouterDeferredToolsSearchStrategy custom(String toolId, String resultKey) {
        return custom(toolId, resultKey, null);
    }

    /**
     * The caller-owned tool search with a hit cap:
     * {@code {"type": "custom", "tool_id": ..., "result_key": ...,
     * "max_results": n}}.
     *
     * @param toolId the name of the client tool that executes the search
     * @param resultKey the result field carrying the found tool identities
     * @param maxResults the maximum number of tools one search may return
     *        (the docs: defaults to {@code 5}, accepts 1-100; not validated
     *        here - see {@link #regex(Integer)}), or {@code null} to omit the
     *        field
     * @return the built strategy
     */
    public static OpenRouterDeferredToolsSearchStrategy custom(String toolId, String resultKey, Integer maxResults) {
        if (toolId == null || toolId.isEmpty()) {
            throw new IllegalArgumentException("toolId must not be null or empty");
        }
        if (resultKey == null || resultKey.isEmpty()) {
            throw new IllegalArgumentException("resultKey must not be null or empty");
        }
        JSONObject json = new JSONObject();
        json.put("type", "custom");
        json.put("tool_id", toolId);
        json.put("result_key", resultKey);
        if (maxResults != null) {
            json.put("max_results", maxResults.intValue());
        }
        return new OpenRouterDeferredToolsSearchStrategy(json);
    }

    /**
     * Verbatim escape hatch for strategy shapes without a typed factory -
     * e.g. a strategy carrying the universal profile's {@code miss_sources} /
     * {@code siblings} / {@code spread} knobs. The object is stored as a
     * detached copy, so later mutation of the argument cannot change the
     * strategy.
     *
     * @param json the full search-strategy object; must carry the required
     *        {@code type} discriminator
     * @return the verbatim strategy
     */
    public static OpenRouterDeferredToolsSearchStrategy raw(JSONObject json) {
        if (json == null) {
            throw new IllegalArgumentException("json must not be null");
        }
        if (json.isNull("type")) {
            throw new IllegalArgumentException("raw search strategy requires a \"type\" discriminator");
        }
        return new OpenRouterDeferredToolsSearchStrategy(new JSONObject(json.toString()));
    }

    /**
     * JSON path: {@code type} - the discriminator ({@code regex} / {@code bm25} /
     * {@code custom}), read verbatim.
     *
     * @return the discriminator, or {@code null} when absent or not a string
     */
    public String type() {
        Object value = json.opt("type");
        return value instanceof String s ? s : null;
    }

    /**
     * JSON path: {@code max_results} - the hit cap of one search.
     *
     * @return the cap, or {@code null} when absent or not a number
     */
    public Integer maxResults() {
        Object value = json.opt("max_results");
        return value instanceof Number n ? n.intValue() : null;
    }

    /**
     * JSON path: {@code tool_id} - the client tool executing a {@code custom}
     * search.
     *
     * @return the tool name, or {@code null} when absent or not a string
     */
    public String toolId() {
        Object value = json.opt("tool_id");
        return value instanceof String s ? s : null;
    }

    /**
     * JSON path: {@code result_key} - the result field carrying the found tool
     * identities of a {@code custom} search.
     *
     * @return the result key, or {@code null} when absent or not a string
     */
    public String resultKey() {
        Object value = json.opt("result_key");
        return value instanceof String s ? s : null;
    }

    /**
     * Returns the raw option value for {@code key}, or {@code null} when
     * unset - the read path for the untyped knobs ({@code miss_sources},
     * {@code siblings}, {@code spread}).
     */
    public Object option(String key) {
        return json.opt(key);
    }

    /**
     * Returns the JSON object emitted as {@code deferred_tools.search}. The
     * result is a detached copy per call.
     */
    public JSONObject toJson() {
        return new JSONObject(json.toString());
    }
}
