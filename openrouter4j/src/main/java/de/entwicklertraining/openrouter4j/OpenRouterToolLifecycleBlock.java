package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.Objects;

/**
 * A mid-conversation tool lifecycle content block for the Anthropic Messages
 * API - {@code tool_addition} (load a previously deferred tool) or
 * {@code tool_removal} (remove a tool from the conversation). Both block types
 * carry a {@link OpenRouterToolReference} and an optional Anthropic-style
 * {@code cache_control} breakpoint.
 * <p>
 * Wire shapes (from the OpenAPI schema):
 * <ul>
 *   <li>{@code {"type":"tool_addition","tool":{...}[, "cache_control":{...}]}}</li>
 *   <li>{@code {"type":"tool_removal","tool":{...}[, "cache_control":{...}]}}</li>
 * </ul>
 * <p>
 * <b>Traps:</b> both block types are only valid in {@code role: "system"}
 * messages. {@code tool_addition} requires the referenced tool to have been
 * declared with {@code defer_loading: true} in the request's {@code tools}
 * array. The schema documents that these blocks are not supported on Claude
 * Sonnet 5 or models older than Claude Opus 4.8.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/tool-calling">Tool calling</a>
 */
public final class OpenRouterToolLifecycleBlock {

    private final JSONObject json;

    private OpenRouterToolLifecycleBlock(JSONObject json) {
        this.json = json;
    }

    /**
     * A {@code tool_addition} block that loads a previously deferred tool
     * ({@code {"type":"tool_addition","tool":{...}}}).
     *
     * @param ref the tool reference
     * @return the lifecycle block
     */
    public static OpenRouterToolLifecycleBlock toolAddition(OpenRouterToolReference ref) {
        return toolAddition(ref, null);
    }

    /**
     * A {@code tool_addition} block with an optional Anthropic-style
     * {@code cache_control} breakpoint.
     *
     * @param ref the tool reference
     * @param marker the cache marker ({@code null} for none); only the
     *        Anthropic-style {@code cacheControl()} form is accepted - the
     *        schema defines no {@code prompt_cache_breakpoint} on these blocks
     * @return the lifecycle block
     */
    public static OpenRouterToolLifecycleBlock toolAddition(OpenRouterToolReference ref, OpenRouterCacheMarker marker) {
        return build("tool_addition", ref, marker);
    }

    /**
     * A {@code tool_removal} block that removes a tool from the conversation
     * ({@code {"type":"tool_removal","tool":{...}}}).
     *
     * @param ref the tool reference
     * @return the lifecycle block
     */
    public static OpenRouterToolLifecycleBlock toolRemoval(OpenRouterToolReference ref) {
        return toolRemoval(ref, null);
    }

    /**
     * A {@code tool_removal} block with an optional Anthropic-style
     * {@code cache_control} breakpoint.
     *
     * @param ref the tool reference
     * @param marker the cache marker ({@code null} for none); only the
     *        Anthropic-style {@code cacheControl()} form is accepted - the
     *        schema defines no {@code prompt_cache_breakpoint} on these blocks
     * @return the lifecycle block
     */
    public static OpenRouterToolLifecycleBlock toolRemoval(OpenRouterToolReference ref, OpenRouterCacheMarker marker) {
        return build("tool_removal", ref, marker);
    }

    private static OpenRouterToolLifecycleBlock build(String type, OpenRouterToolReference ref, OpenRouterCacheMarker marker) {
        Objects.requireNonNull(ref, "tool reference must not be null");
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("tool", ref.toJson());
        if (marker != null) {
            if (marker.cacheControlType() == null) {
                throw new IllegalArgumentException(
                        "tool_addition/tool_removal blocks accept only the Anthropic-style cacheControl() marker; "
                                + "promptCacheBreakpoint() is not defined on these block types");
            }
            JSONObject markerJson = marker.toJson();
            for (String key : markerJson.keySet()) {
                json.put(key, markerJson.get(key));
            }
        }
        return new OpenRouterToolLifecycleBlock(json);
    }

    /**
     * The JSON object emitted as a content block in a {@code role: "system"}
     * message.
     */
    public JSONObject toJson() {
        return new JSONObject(json.toString());
    }

    @Override
    public String toString() {
        return json.toString();
    }
}
