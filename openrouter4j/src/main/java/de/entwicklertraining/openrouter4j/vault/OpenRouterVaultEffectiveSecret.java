package de.entwicklertraining.openrouter4j.vault;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one entry of {@code data[]} on the effective-secrets list
 * (GET /vault/interns/{internId}/effective-secrets, schema
 * {@code VaultEffectiveSecret}): the metadata of the one secret the
 * intern's outbound requests receive under this name.
 *
 * <p><b>Resolution:</b> where several vaults hold the same name, the entry
 * is the one that wins, in the order intern, attached, workspace. The same
 * resolution decides what the intern's outbound requests receive, so this
 * list and the intern's requests agree.
 *
 * <p><b>Metadata only:</b> the secret value is never returned.
 *
 * <p>{@link #hosts()}: the intern receives the secret only on requests to
 * a hostname in {@code hosts}, or on any request when {@code hosts} is
 * {@code null}; a request to any other hostname receives no secret under
 * this name, even when another vault holds one.
 *
 * <p>{@link #fingerprint()} is comparable only within one vault - equal
 * fingerprints in one vault mean equal values, but the same value in
 * another vault carries a different fingerprint.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultEffectiveSecret {

    private final JSONObject json;

    OpenRouterVaultEffectiveSecret(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw JSON row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code name} - the secret name (1-255 chars, lowercase
     * start, only lowercase letters, digits and single underscores, no
     * trailing underscore, no {@code __}).
     *
     * @return the name, or {@code null} when absent or not a string
     */
    public String name() {
        Object value = json.opt("name");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code hosts} - the hostnames the secret is released to
     * (1-100 entries of at most 254 chars). {@code null} means the intern
     * receives the secret on any request; a list limits it to requests to
     * exactly those hostnames. Non-string entries are dropped.
     *
     * @return the bound hosts, {@code null} when absent, JSON {@code null}
     *         or not an array (e.g. a legacy row without host binding)
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
     * JSON path: {@code fingerprint} - {@code sha256:...} fingerprint of the
     * delivered value. Comparable only within one vault.
     *
     * @return the fingerprint, or {@code null} when absent, not a string or
     *         JSON {@code null}
     */
    public String fingerprint() {
        Object value = json.opt("fingerprint");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code created_at} - ISO 8601 creation time of the
     * delivered secret.
     *
     * @return the creation time, or {@code null} when absent or not a
     *         string
     */
    public String createdAt() {
        Object value = json.opt("created_at");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code scope} - where the delivered secret is stored:
     * {@code intern} for the intern's own vault, {@code attached} for a
     * vault attached to the intern, {@code workspace} for the workspace
     * vault. Unknown values are passed through verbatim.
     *
     * @return the scope, or {@code null} when absent or not a string
     */
    public String scope() {
        Object value = json.opt("scope");
        return value instanceof String text ? text : null;
    }
}
