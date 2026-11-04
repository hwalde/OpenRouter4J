package de.entwicklertraining.openrouter4j.vault;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one entry of {@code mcp_origins[]} in the vault routing
 * table: an MCP server origin the vault injects credentials for.
 *
 * <p>{@link #status()} is the vault's decision for a request to this
 * server; it is {@code null} for members' own accounts ({@link
 * #isPersonal()}), which are decided per acting member.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} / -empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultMcpOrigin {

    private final JSONObject json;

    OpenRouterVaultMcpOrigin(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw MCP-origin object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code host} - the MCP server host.
     *
     * @return the host, or {@code null} when absent or not a string
     */
    public String host() {
        Object value = json.opt("host");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code account_key} - the account key this origin routes
     * with.
     *
     * @return the account key, or {@code null} when absent, not a string
     *         or JSON {@code null}
     */
    public String accountKey() {
        Object value = json.opt("account_key");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code is_personal} - members' own accounts, matched to
     * the acting member per turn.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean isPersonal() {
        Object value = json.opt("is_personal");
        return value instanceof Boolean flag ? flag : null;
    }

    /**
     * JSON path: {@code routes[]} - the routes this server answers under.
     * Non-object entries are dropped.
     *
     * @return the routes, empty when absent or not an array
     */
    public List<OpenRouterVaultRoute> routes() {
        List<OpenRouterVaultRoute> result = new ArrayList<>();
        JSONArray routes = json.optJSONArray("routes");
        if (routes == null) {
            return result;
        }
        for (int i = 0; i < routes.length(); i++) {
            JSONObject entry = routes.optJSONObject(i);
            if (entry != null) {
                result.add(new OpenRouterVaultRoute(entry));
            }
        }
        return result;
    }

    /**
     * JSON path: {@code status} - the vault's decision for a request to
     * this server; {@code null} for members' own accounts, decided per
     * acting member.
     *
     * @return the status view, or {@code null} when absent, not an object
     *         or JSON {@code null}
     */
    public OpenRouterVaultOriginStatus status() {
        JSONObject status = json.optJSONObject("status");
        return status != null ? new OpenRouterVaultOriginStatus(status) : null;
    }
}
