package de.entwicklertraining.openrouter4j.keys;

import org.json.JSONObject;

/**
 * A typed view of one API key of the management endpoints
 * (GET /keys, GET /keys/{hash}, the {@code data} of PATCH /keys/{hash} and
 * POST /keys): label, limits, usage and metadata.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 *
 * <p>The plaintext key of a create response never travels in this view - it
 * is only available via
 * {@link OpenRouterKeyCreateResponse#key()} and must be treated as a secret.
 */
public final class OpenRouterApiKey {

    private final JSONObject json;

    OpenRouterApiKey(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw key row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code hash} - unique hash identifier of the API key; the
     * path segment of GET/PATCH/DELETE /keys/{hash}.
     *
     * @return the value, or {@code null} when absent
     */
    public String hash() {
        return json.optString("hash", null);
    }

    /**
     * JSON path: {@code name} - the name of the API key.
     *
     * @return the value, or {@code null} when absent
     */
    public String name() {
        return json.optString("name", null);
    }

    /**
     * JSON path: {@code label} - the masked, human-readable label
     * (e.g. {@code sk-or-v1-0e6...1c96}).
     *
     * @return the value, or {@code null} when absent
     */
    public String label() {
        return json.optString("label", null);
    }

    /**
     * JSON path: {@code disabled} - whether the key is disabled.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean disabled() {
        if (!json.has("disabled") || json.isNull("disabled")) {
            return null;
        }
        return json.optBoolean("disabled");
    }

    /**
     * JSON path: {@code limit} - spending limit in USD ({@code null} on the
     * wire when unlimited).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double limit() {
        return optDouble("limit");
    }

    /**
     * JSON path: {@code limit_remaining} - remaining spending limit in USD.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double limitRemaining() {
        return optDouble("limit_remaining");
    }

    /**
     * JSON path: {@code limit_reset} - reset type ({@code daily},
     * {@code weekly}, {@code monthly}, or {@code null} for no reset).
     *
     * @return the value, or {@code null} when absent
     */
    public String limitReset() {
        return json.optString("limit_reset", null);
    }

    /**
     * JSON path: {@code include_byok_in_limit} - whether BYOK usage counts
     * into the limit.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean includeByokInLimit() {
        if (!json.has("include_byok_in_limit") || json.isNull("include_byok_in_limit")) {
            return null;
        }
        return json.optBoolean("include_byok_in_limit");
    }

    /**
     * JSON path: {@code usage} - total OpenRouter credit usage in USD.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double usage() {
        return optDouble("usage");
    }

    /**
     * JSON path: {@code usage_daily} - usage for the current UTC day.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double usageDaily() {
        return optDouble("usage_daily");
    }

    /**
     * JSON path: {@code usage_weekly} - usage for the current UTC week
     * (Monday-Sunday).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double usageWeekly() {
        return optDouble("usage_weekly");
    }

    /**
     * JSON path: {@code usage_monthly} - usage for the current UTC month.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double usageMonthly() {
        return optDouble("usage_monthly");
    }

    /**
     * JSON path: {@code byok_usage} - total external BYOK usage in USD.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double byokUsage() {
        return optDouble("byok_usage");
    }

    /**
     * JSON path: {@code byok_usage_daily} - BYOK usage for the current UTC day.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double byokUsageDaily() {
        return optDouble("byok_usage_daily");
    }

    /**
     * JSON path: {@code byok_usage_weekly} - BYOK usage for the current UTC week.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double byokUsageWeekly() {
        return optDouble("byok_usage_weekly");
    }

    /**
     * JSON path: {@code byok_usage_monthly} - BYOK usage for the current UTC month.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double byokUsageMonthly() {
        return optDouble("byok_usage_monthly");
    }

    /**
     * JSON path: {@code created_at} - ISO 8601 creation timestamp.
     *
     * @return the value, or {@code null} when absent
     */
    public String createdAt() {
        return json.optString("created_at", null);
    }

    /**
     * JSON path: {@code updated_at} - ISO 8601 last-update timestamp.
     *
     * @return the value, or {@code null} when absent
     */
    public String updatedAt() {
        return json.optString("updated_at", null);
    }

    /**
     * JSON path: {@code expires_at} - ISO 8601 UTC expiration timestamp,
     * {@code null} when the key does not expire.
     *
     * @return the value, or {@code null} when absent
     */
    public String expiresAt() {
        return json.optString("expires_at", null);
    }

    /**
     * JSON path: {@code external_user} - the partner's end-user identifier
     * used for attribution.
     *
     * @return the value, or {@code null} when absent
     */
    public String externalUser() {
        return json.optString("external_user", null);
    }

    /**
     * JSON path: {@code creator_user_id} - the user ID of the key creator
     * (for organization-owned keys, the member who created the key).
     *
     * @return the value, or {@code null} when absent
     */
    public String creatorUserId() {
        return json.optString("creator_user_id", null);
    }

    /**
     * JSON path: {@code workspace_id} - the workspace the key belongs to.
     *
     * @return the value, or {@code null} when absent
     */
    public String workspaceId() {
        return json.optString("workspace_id", null);
    }

    private Double optDouble(String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        return json.optDouble(key);
    }
}
