package de.entwicklertraining.openrouter4j.vault;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one entry of {@code secrets[]} in the vault routing
 * table: a secret the vault would inject, by name and scope. Metadata only
 * - the value is never part of the table.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultRoutingSecret {

    private final JSONObject json;

    OpenRouterVaultRoutingSecret(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw secret entry behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code name} - the secret name.
     *
     * @return the name, or {@code null} when absent or not a string
     */
    public String name() {
        Object value = json.opt("name");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code hosts} - the hostnames the secret is released to,
     * or {@code null} for any request. Non-string entries are dropped.
     *
     * @return the bound hosts, or {@code null} when absent, JSON
     *         {@code null} or not an array
     */
    public List<String> hosts() {
        if (!json.has("hosts") || json.isNull("hosts")) {
            return null;
        }
        JSONArray hosts = json.optJSONArray("hosts");
        if (hosts == null) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < hosts.length(); i++) {
            Object value = hosts.opt(i);
            if (value instanceof String host) {
                result.add(host);
            }
        }
        return result;
    }

    /**
     * JSON path: {@code scope} - where the secret is stored:
     * {@code intern} for the intern's own vault, {@code attached} for a
     * vault attached to the intern, {@code workspace} for the workspace
     * vault; unknown values are passed through verbatim.
     *
     * @return the scope, or {@code null} when absent or not a string
     */
    public String scope() {
        Object value = json.opt("scope");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code created_at} - creation time of the secret.
     *
     * @return the creation time, or {@code null} when absent or not a
     *         string
     */
    public String createdAt() {
        Object value = json.opt("created_at");
        return value instanceof String text ? text : null;
    }
}
