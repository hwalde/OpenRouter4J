package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

/**
 * A server-side OpenRouter plugin attached to a chat completion request via the
 * {@code plugins} request array.
 * <p>
 * OpenRouter executes plugins server-side before or while the model answers; the
 * best-known one is the {@code web} plugin (web search), whose results reach the
 * model as tool calls ({@code server_tool_calls}).
 * <p>
 * Implementations: {@link OpenRouterWebSearchPlugin} for the typed {@code web}
 * plugin and {@link OpenRouterGenericPlugin} as escape hatch for every other
 * plugin id (or for ids OpenRouter adds after this library was released).
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins">OpenRouter plugins</a>
 */
public interface OpenRouterPlugin {

    /**
     * The plugin discriminator sent as the {@code id} field, e.g. {@code "web"}.
     */
    String id();

    /**
     * The JSON object emitted into the {@code plugins} request array.
     * The returned object must contain at least the {@code id} field.
     */
    JSONObject toJson();

    /**
     * Creates a generic plugin that emits {@code {"id": id}} plus the given extra
     * fields verbatim. Use this for plugin ids without a typed implementation.
     *
     * @param id the plugin discriminator
     * @return a generic plugin emitting only the id field
     */
    static OpenRouterGenericPlugin of(String id) {
        return new OpenRouterGenericPlugin(id);
    }
}
