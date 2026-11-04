package de.entwicklertraining.openrouter4j.vault;

import org.json.JSONObject;

/**
 * A typed view of one origin {@code status} object of the vault routing
 * table (shared by {@code mcp_origins[].status} and
 * {@code custom_origins[].status}): the vault's injection decision for a
 * request to that destination, at the table's {@code read_at}.
 *
 * <p>{@code null} on the row for members' own accounts - those are decided
 * per acting member. {@link #failure()} carries the credential failure
 * code when {@link #state()} is {@code failing}; {@code null} otherwise.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterVaultOriginStatus {

    private final JSONObject json;

    OpenRouterVaultOriginStatus(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw status object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code state} - the vault's decision for a request to this
     * destination: {@code usable}, {@code no_credential} or
     * {@code failing}; unknown values are passed through verbatim.
     *
     * @return the state, or {@code null} when absent or not a string
     */
    public String state() {
        Object value = json.opt("state");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code failure} - the credential failure code when
     * {@link #state()} is {@code failing}; {@code null} otherwise.
     *
     * @return the failure code, or {@code null} when absent, not a string
     *         or JSON {@code null}
     */
    public String failure() {
        Object value = json.opt("failure");
        return value instanceof String text ? text : null;
    }
}
