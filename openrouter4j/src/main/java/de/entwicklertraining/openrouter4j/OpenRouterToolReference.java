package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.Objects;

/**
 * A tool reference for the Anthropic Messages mid-conversation tool lifecycle
 * blocks ({@code tool_addition} / {@code tool_removal}). Three wire forms exist
 * in the API schema:
 * <ul>
 *   <li>{@code {"type":"tool_reference","name":...}} - a plain tool reference
 *       by name ({@link #tool(String)}).</li>
 *   <li>{@code {"type":"mcp_tool_reference","name":...,"server_name":...}} -
 *       an MCP tool reference by tool name and server name
 *       ({@link #mcpTool(String, String)}).</li>
 *   <li>{@code {"type":"mcp_toolset_reference","server_name":...}} - a
 *       reference to a whole MCP toolset ({@link #mcpToolset(String)}).</li>
 * </ul>
 * Create instances through the typed factories or the verbatim escape hatch
 * {@link #raw(JSONObject)} for reference types the API adds later.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/tool-calling">Tool calling</a>
 */
public final class OpenRouterToolReference {

    private final JSONObject json;

    private OpenRouterToolReference(JSONObject json) {
        this.json = json;
    }

    /**
     * A plain tool reference by name
     * ({@code {"type":"tool_reference","name":...}}).
     *
     * @param name the tool name
     * @return the tool reference
     */
    public static OpenRouterToolReference tool(String name) {
        Objects.requireNonNull(name, "name must not be null");
        JSONObject json = new JSONObject();
        json.put("type", "tool_reference");
        json.put("name", name);
        return new OpenRouterToolReference(json);
    }

    /**
     * An MCP tool reference by tool name and server name
     * ({@code {"type":"mcp_tool_reference","name":...,"server_name":...}}).
     *
     * @param name the MCP tool name
     * @param serverName the MCP server name
     * @return the tool reference
     */
    public static OpenRouterToolReference mcpTool(String name, String serverName) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(serverName, "serverName must not be null");
        JSONObject json = new JSONObject();
        json.put("type", "mcp_tool_reference");
        json.put("name", name);
        json.put("server_name", serverName);
        return new OpenRouterToolReference(json);
    }

    /**
     * A reference to a whole MCP toolset
     * ({@code {"type":"mcp_toolset_reference","server_name":...}}).
     *
     * @param serverName the MCP server name
     * @return the tool reference
     */
    public static OpenRouterToolReference mcpToolset(String serverName) {
        Objects.requireNonNull(serverName, "serverName must not be null");
        JSONObject json = new JSONObject();
        json.put("type", "mcp_toolset_reference");
        json.put("server_name", serverName);
        return new OpenRouterToolReference(json);
    }

    /**
     * Verbatim escape hatch: emits the given reference object unchanged.
     *
     * @param reference the raw reference JSON; must contain a {@code type} field
     * @return the tool reference
     */
    public static OpenRouterToolReference raw(JSONObject reference) {
        Objects.requireNonNull(reference, "reference must not be null");
        if (!reference.has("type")) {
            throw new IllegalArgumentException("A tool reference must carry a 'type' field");
        }
        return new OpenRouterToolReference(new JSONObject(reference.toString()));
    }

    /**
     * The JSON object emitted as the {@code tool} value of a
     * {@code tool_addition} / {@code tool_removal} block.
     */
    public JSONObject toJson() {
        return new JSONObject(json.toString());
    }

    @Override
    public String toString() {
        return json.toString();
    }
}
