package de.entwicklertraining.openrouter4j.interns;

import org.json.JSONObject;

/**
 * A typed view of one entry of {@code data[]} on the intern-connections
 * list (GET /vault/interns/{internId}/connections): one connection in
 * scope for the intern.
 *
 * <p>The {@link #key()} is the connection's key on the dashboard's
 * Connections tab: {@code github}, {@code slack}, {@code google}, a
 * catalog MCP server such as {@code linear} or {@code notion}, or
 * {@code mcp:<host>} for a custom MCP server. A connection is listed when
 * it is stored, enabled, and not turned off or hidden for this intern -
 * whether its last test passed is not part of the rule.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterInternConnection {

    private final JSONObject json;

    OpenRouterInternConnection(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw connection row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code key} - the connection key.
     *
     * @return the key, or {@code null} when absent or not a string
     */
    public String key() {
        Object value = json.opt("key");
        return value instanceof String text ? text : null;
    }
}
