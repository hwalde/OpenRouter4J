package de.entwicklertraining.openrouter4j.vault;

import org.json.JSONObject;

/**
 * A typed view of one entry of {@code host_rules[]} in the vault routing
 * table: a built-in host the vault injects a named secret for, and whether
 * that secret is stored.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultHostRule {

    private final JSONObject json;

    OpenRouterVaultHostRule(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw host-rule object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code host} - the built-in host the rule applies to.
     *
     * @return the host, or {@code null} when absent or not a string
     */
    public String host() {
        Object value = json.opt("host");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code secret_name} - the name of the secret injected for
     * this host.
     *
     * @return the secret name, or {@code null} when absent or not a string
     */
    public String secretName() {
        Object value = json.opt("secret_name");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code holds_secret} - whether that secret is stored.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean holdsSecret() {
        Object value = json.opt("holds_secret");
        return value instanceof Boolean flag ? flag : null;
    }
}
