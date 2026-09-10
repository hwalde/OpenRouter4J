package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

/**
 * A per-content-part prompt-cache marker for a chat message's text content part.
 * Two interchangeable styles exist in the OpenRouter API, and OpenRouter converts
 * between them based on the provider serving the request:
 *
 * <ul>
 *   <li><b>Anthropic-style</b> {@code cache_control} - {@code {"type":"ephemeral"[,"ttl":"1h"]}}.
 *       Marks an explicit cache breakpoint on the content part carrying it; everything
 *       through that part becomes the candidate cached prefix. Native on Anthropic,
 *       Google, Alibaba and OpenAI explicit-caching models (converted automatically).</li>
 *   <li><b>OpenAI-style</b> {@code prompt_cache_breakpoint} - {@code {"mode":"explicit"}}.
 *       The same boundary in OpenAI's format; converted to a default 5-minute
 *       {@code cache_control} when routed to Anthropic or Google. Carries no {@code ttl}.</li>
 * </ul>
 *
 * <p>Interaction with the request-level controls: the request-root
 * {@code cache_control} ({@code OpenRouterChatCompletionRequest.Builder#cacheControl()})
 * enables <em>automatic</em> caching and is unaffected by these markers. The request-root
 * {@code prompt_cache_options} ({@code ...promptCacheOptions(String, String)}) with
 * {@code mode: "explicit"} <em>disables</em> OpenAI-managed breakpoints so that only
 * content parts carrying a {@code prompt_cache_breakpoint} marker participate in
 * caching - this value type is how such a part is marked.
 *
 * <p>Docs traps: explicit breakpoints are limited to four per request and should be
 * reserved for large stable blocks; TTLs are <em>not</em> translated between the two
 * styles (a {@code cache_control} {@code ttl} is dropped toward OpenAI). The OpenAPI
 * schema defines the markers on <b>text</b> content parts only.
 *
 * @see <a href="https://openrouter.ai/docs/guides/best-practices/prompt-caching">Prompt caching</a>
 */
public final class OpenRouterCacheMarker {

    private final String cacheControlType;
    private final String cacheControlTtl;
    private final String breakpointMode;

    private OpenRouterCacheMarker(String cacheControlType, String cacheControlTtl, String breakpointMode) {
        this.cacheControlType = cacheControlType;
        this.cacheControlTtl = cacheControlTtl;
        this.breakpointMode = breakpointMode;
    }

    /**
     * An Anthropic-style {@code cache_control: {"type":"ephemeral"}} marker - an
     * explicit cache breakpoint with the default 5-minute TTL.
     *
     * @return the marker
     */
    public static OpenRouterCacheMarker cacheControl() {
        return new OpenRouterCacheMarker("ephemeral", null, null);
    }

    /**
     * An Anthropic-style {@code cache_control: {"type":"ephemeral","ttl":...}} marker
     * with an explicit TTL (the API supports {@code "5m"} default and {@code "1h"}).
     * Trap: the TTL is dropped when OpenRouter converts the marker toward an
     * OpenAI explicit-caching model.
     *
     * @param ttl the cache TTL (e.g. {@code "5m"}, {@code "1h"})
     * @return the marker
     */
    public static OpenRouterCacheMarker cacheControl(String ttl) {
        return new OpenRouterCacheMarker("ephemeral", ttl, null);
    }

    /**
     * An OpenAI-style {@code prompt_cache_breakpoint: {"mode":"explicit"}} marker.
     * Everything through the content part carrying it becomes the candidate cached
     * prefix; converted to a default 5-minute {@code cache_control} on Anthropic or
     * Google providers. Carries no TTL (the schema defines none); to control the
     * cache duration use the request-root {@code prompt_cache_options} TTL instead.
     *
     * @return the marker
     */
    public static OpenRouterCacheMarker promptCacheBreakpoint() {
        return new OpenRouterCacheMarker(null, null, "explicit");
    }

    /** The {@code cache_control.type} value ({@code "ephemeral"}), or {@code null} for a breakpoint marker. */
    public String cacheControlType() {
        return cacheControlType;
    }

    /** The {@code cache_control.ttl} value, or {@code null} when unset or for a breakpoint marker. */
    public String cacheControlTtl() {
        return cacheControlTtl;
    }

    /** The {@code prompt_cache_breakpoint.mode} value ({@code "explicit"}), or {@code null} for a cache_control marker. */
    public String breakpointMode() {
        return breakpointMode;
    }

    /**
     * Emits the marker's JSON: {@code {"cache_control":{...}}} for the Anthropic
     * style, {@code {"prompt_cache_breakpoint":{...}}} for the OpenAI style.
     *
     * @return a fresh JSON object with exactly one key
     */
    public JSONObject toJson() {
        JSONObject marker = new JSONObject();
        if (cacheControlType != null) {
            JSONObject cacheControl = new JSONObject();
            cacheControl.put("type", cacheControlType);
            if (cacheControlTtl != null) {
                cacheControl.put("ttl", cacheControlTtl);
            }
            marker.put("cache_control", cacheControl);
        } else {
            JSONObject breakpoint = new JSONObject();
            breakpoint.put("mode", breakpointMode);
            marker.put("prompt_cache_breakpoint", breakpoint);
        }
        return marker;
    }
}
