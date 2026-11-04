package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultRoutingTable;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Response of GET /vault/interns/{internId}/connections: the connections
 * in scope for the intern plus the vault's routing table for it.
 *
 * <p>{@link #routing()} and {@link #routingText()} are for the intern's
 * <b>own</b> API key only - {@code null} for any other caller and when the
 * vault could not build the table in time; the connection keys
 * ({@link #connections()}) still list in that case.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention.
 */
public final class OpenRouterInternConnectionsResponse
        extends OpenRouterResponse<OpenRouterInternConnectionsRequest> {

    OpenRouterInternConnectionsResponse(JSONObject json, OpenRouterInternConnectionsRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data[]} - the connections in scope for the intern.
     * Non-object entries are dropped.
     *
     * @return the connection rows, empty when absent
     */
    public List<OpenRouterInternConnection> connections() {
        List<OpenRouterInternConnection> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = data.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterInternConnection(row));
                    }
                }
            }
        } catch (Exception e) {
            // swallow-and-return-empty convention
        }
        return result;
    }

    /**
     * JSON path: {@code routing} - the vault's routing table for the
     * intern: the credentials it holds and what it would inject each for.
     * The intern's own API key only - {@code null} for any other caller or
     * when the vault could not build it in time. Never credentials.
     *
     * @return the routing table view, or {@code null} when absent, not an
     *         object or JSON {@code null}
     */
    public OpenRouterVaultRoutingTable routing() {
        try {
            JSONObject routing = json.optJSONObject("routing");
            return routing != null ? new OpenRouterVaultRoutingTable(routing) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code routing_text} - the routing table as the plain
     * lines the intern reads in its instructions' Connections section, or a
     * notice that it could not be read; {@code null} for a caller other
     * than the intern.
     *
     * @return the text, or {@code null} when absent, not a string or JSON
     *         {@code null}
     */
    public String routingText() {
        Object value = json.opt("routing_text");
        return value instanceof String text ? text : null;
    }
}
