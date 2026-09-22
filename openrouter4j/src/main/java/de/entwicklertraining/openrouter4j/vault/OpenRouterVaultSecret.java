package de.entwicklertraining.openrouter4j.vault;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one stored secret's metadata (schema {@code VaultSecret}):
 * name, bound hosts, fingerprint and creation time.
 *
 * <p><b>Metadata only:</b> the plaintext value is encrypted at rest and never
 * returned by any vault response - it cannot be retrieved after the store
 * call. Treat the value given to the store request as a write-only secret.
 *
 * <p>{@link #fingerprint()} is an HMAC-SHA-256 of the value keyed with that
 * vault's own data key, so it is comparable only within one vault: equal
 * fingerprints in one vault mean equal values, and rewriting the same value
 * keeps its fingerprint. The same value stored in two vaults (for example a
 * workspace secret and its intern copy) carries different fingerprints, so
 * comparing fingerprints across vaults cannot show that a copy matches or
 * that a rotation propagated.
 *
 * <p>{@link #hosts()} and {@link #fingerprint()} are {@code null} only for
 * legacy rows written before host binding was required; storing the secret
 * again assigns hosts.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultSecret {

    private final JSONObject json;

    OpenRouterVaultSecret(JSONObject json) {
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
     * @return the name, or {@code null} when absent
     */
    public String name() {
        return json.optString("name", null);
    }

    /**
     * JSON path: {@code hosts} - the exact DNS hostnames the secret may be
     * released to (already normalized: lowercased, trailing dot removed).
     * Matching at release time is exact: a secret bound to
     * {@code api.example.com} is never released to {@code example.com} or
     * any other hostname.
     *
     * @return the bound hosts, or {@code null} for a legacy row written
     *         before host binding was required
     */
    public List<String> hosts() {
        if (!json.has("hosts") || json.isNull("hosts")) {
            return null;
        }
        List<String> result = new ArrayList<>();
        JSONArray hosts = json.optJSONArray("hosts");
        if (hosts != null) {
            for (int i = 0; i < hosts.length(); i++) {
                String host = hosts.optString(i, null);
                if (host != null) {
                    result.add(host);
                }
            }
        }
        return result;
    }

    /**
     * JSON path: {@code fingerprint} - {@code sha256:...} fingerprint of the
     * value (HMAC-SHA-256 keyed with the vault's data key; comparable only
     * within one vault).
     *
     * @return the fingerprint, or {@code null} when absent (legacy row)
     */
    public String fingerprint() {
        return json.optString("fingerprint", null);
    }

    /**
     * JSON path: {@code created_at} - ISO 8601 creation time.
     *
     * @return the creation time, or {@code null} when absent
     */
    public String createdAt() {
        return json.optString("created_at", null);
    }
}
