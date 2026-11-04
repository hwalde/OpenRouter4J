package de.entwicklertraining.openrouter4j.vault;

import org.json.JSONObject;

/**
 * A typed view of one entry of {@code custom_origins[]} in the vault
 * routing table: a custom destination the vault injects for.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultCustomOrigin {

    private final JSONObject json;

    OpenRouterVaultCustomOrigin(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw custom-origin object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code host} - the destination host.
     *
     * @return the host, or {@code null} when absent or not a string
     */
    public String host() {
        Object value = json.opt("host");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code path_prefix} - the path prefix of this
     * destination.
     *
     * @return the prefix, or {@code null} when absent, not a string or
     *         JSON {@code null}
     */
    public String pathPrefix() {
        Object value = json.opt("path_prefix");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code status} - the vault injection decision for this
     * destination, at the table's {@code read_at}.
     *
     * @return the status view, or {@code null} when absent, not an object
     *         or JSON {@code null}
     */
    public OpenRouterVaultOriginStatus status() {
        JSONObject status = json.optJSONObject("status");
        return status != null ? new OpenRouterVaultOriginStatus(status) : null;
    }
}
