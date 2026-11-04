package de.entwicklertraining.openrouter4j.vault;

import org.json.JSONObject;

/**
 * A typed view of one entry of {@code mcp_origins[].routes[]} in the vault
 * routing table: a route the MCP server answers under.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultRoute {

    private final JSONObject json;

    OpenRouterVaultRoute(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw route object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code host} - the host this route answers under.
     *
     * @return the host, or {@code null} when absent or not a string
     */
    public String host() {
        Object value = json.opt("host");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code path_prefix} - the path prefix of this route.
     *
     * @return the prefix, or {@code null} when absent, not a string or
     *         JSON {@code null}
     */
    public String pathPrefix() {
        Object value = json.opt("path_prefix");
        return value instanceof String text ? text : null;
    }
}
