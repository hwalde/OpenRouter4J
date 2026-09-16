package de.entwicklertraining.openrouter4j.keys;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /key: the data of the API key making the call.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterCurrentKeyResponse extends OpenRouterResponse<OpenRouterCurrentKeyRequest> {

    OpenRouterCurrentKeyResponse(JSONObject json, OpenRouterCurrentKeyRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the raw key object.
     *
     * @return the value, or {@code null} when absent
     */
    public JSONObject data() {
        return json.optJSONObject("data");
    }

    /**
     * JSON path: {@code data.label} - the masked, human-readable label.
     *
     * @return the value, or {@code null} when absent
     */
    public String label() {
        return optDataString("label");
    }

    /**
     * JSON path: {@code data.limit} - spending limit in USD.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double limit() {
        return optDataDouble("limit");
    }

    /**
     * JSON path: {@code data.limit_remaining} - remaining spending limit in USD.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double limitRemaining() {
        return optDataDouble("limit_remaining");
    }

    /**
     * JSON path: {@code data.limit_reset} - reset type ({@code daily},
     * {@code weekly}, {@code monthly}).
     *
     * @return the value, or {@code null} when absent
     */
    public String limitReset() {
        return optDataString("limit_reset");
    }

    /**
     * JSON path: {@code data.include_byok_in_limit} - whether BYOK usage
     * counts into the limit.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean includeByokInLimit() {
        return optDataBoolean("include_byok_in_limit");
    }

    /**
     * JSON path: {@code data.usage} - total OpenRouter credit usage in USD.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double usage() {
        return optDataDouble("usage");
    }

    /**
     * JSON path: {@code data.usage_daily} - usage for the current UTC day.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double usageDaily() {
        return optDataDouble("usage_daily");
    }

    /**
     * JSON path: {@code data.usage_weekly} - usage for the current UTC week.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double usageWeekly() {
        return optDataDouble("usage_weekly");
    }

    /**
     * JSON path: {@code data.usage_monthly} - usage for the current UTC month.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double usageMonthly() {
        return optDataDouble("usage_monthly");
    }

    /**
     * JSON path: {@code data.byok_usage} - total external BYOK usage in USD.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double byokUsage() {
        return optDataDouble("byok_usage");
    }

    /**
     * JSON path: {@code data.byok_usage_daily} - BYOK usage for the current UTC day.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double byokUsageDaily() {
        return optDataDouble("byok_usage_daily");
    }

    /**
     * JSON path: {@code data.byok_usage_weekly} - BYOK usage for the current UTC week.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double byokUsageWeekly() {
        return optDataDouble("byok_usage_weekly");
    }

    /**
     * JSON path: {@code data.byok_usage_monthly} - BYOK usage for the current UTC month.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double byokUsageMonthly() {
        return optDataDouble("byok_usage_monthly");
    }

    /**
     * JSON path: {@code data.is_free_tier} - whether this is a free tier key.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean isFreeTier() {
        return optDataBoolean("is_free_tier");
    }

    /**
     * JSON path: {@code data.is_management_key} - whether this is a
     * management key (the requirement for most management endpoints).
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean isManagementKey() {
        return optDataBoolean("is_management_key");
    }

    /**
     * JSON path: {@code data.is_provisioning_key} - legacy alias for the
     * management-key flag (deprecated in the API).
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean isProvisioningKey() {
        return optDataBoolean("is_provisioning_key");
    }

    /**
     * JSON path: {@code data.expires_at} - ISO 8601 UTC expiration timestamp,
     * {@code null} when the key does not expire.
     *
     * @return the value, or {@code null} when absent
     */
    public String expiresAt() {
        return optDataString("expires_at");
    }

    /**
     * JSON path: {@code data.creator_user_id} - the user ID of the key creator.
     *
     * @return the value, or {@code null} when absent
     */
    public String creatorUserId() {
        return optDataString("creator_user_id");
    }

    private String optDataString(String key) {
        try {
            JSONObject data = json.optJSONObject("data");
            if (data != null) {
                return data.optString(key, null);
            }
        } catch (Exception ignored) {
            // swallow
        }
        return null;
    }

    private Double optDataDouble(String key) {
        try {
            JSONObject data = json.optJSONObject("data");
            if (data != null && data.has(key) && !data.isNull(key)) {
                return data.optDouble(key);
            }
        } catch (Exception ignored) {
            // swallow
        }
        return null;
    }

    private Boolean optDataBoolean(String key) {
        try {
            JSONObject data = json.optJSONObject("data");
            if (data != null && data.has(key) && !data.isNull(key)) {
                return data.optBoolean(key);
            }
        } catch (Exception ignored) {
            // swallow
        }
        return null;
    }
}
