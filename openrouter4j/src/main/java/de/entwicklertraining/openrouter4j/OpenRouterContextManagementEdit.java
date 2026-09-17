package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

/**
 * One strategy entry of the Anthropic {@code context_management} request
 * object on the Messages endpoint: server-side context editing that sheds old
 * tool results, thinking blocks or compacts the conversation before the
 * context window is exhausted. The wire form is the {@code edits} array of
 * {@code context_management}; every entry is discriminated by its
 * {@code type} string:
 * <ul>
 *   <li>{@code clear_tool_uses_20250919} - {@link OpenRouterClearToolUsesEdit}</li>
 *   <li>{@code clear_thinking_20251015} - {@link OpenRouterClearThinkingEdit}</li>
 *   <li>{@code compact_20260112} - {@link OpenRouterCompactEdit}</li>
 * </ul>
 * Create instances through the typed classes above or the verbatim escape
 * hatch {@link #raw(JSONObject)} for strategy types Anthropic adds after this
 * library was released.
 *
 * @see <a href="https://docs.claude.com/en/api/messages">Anthropic Messages API</a>
 */
public interface OpenRouterContextManagementEdit {

    /**
     * The JSON object emitted into the {@code context_management.edits}
     * request array.
     *
     * @return the strategy JSON
     */
    JSONObject toJson();

    /**
     * Verbatim escape hatch: emits the given edit object unchanged. Use it
     * for strategy types Anthropic adds after this library was released.
     *
     * @param edit the raw edit JSON; must contain a {@code type} field
     * @return the context-management edit
     */
    static OpenRouterContextManagementEdit raw(JSONObject edit) {
        if (edit == null) {
            throw new IllegalArgumentException("edit must not be null");
        }
        if (!edit.has("type")) {
            throw new IllegalArgumentException(
                    "A context_management edit must carry a 'type' field");
        }
        return () -> new JSONObject(edit.toString());
    }
}
