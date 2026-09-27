package de.entwicklertraining.openrouter4j.keys;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Response of GET /key: the data of the API key making the call.
 *
 * <p>Follows the swallow-and-return-null convention. The legacy
 * {@code rate_limit} object of the same response is documented deprecated and
 * always answers {@code -1} - it is deliberately not typed; read it from
 * {@link #data()} if you must.
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
     * JSON path: {@code data.allowed_data_regions} - the data regions
     * permitted for this key by the guardrail policies on the key and the
     * account regional-routing entitlement; empty when no region is permitted
     * or the field is absent. Reflects region policy only - other key
     * restrictions (e.g. a management key blocked from inference) still apply
     * independently. Non-string array entries are dropped.
     *
     * @return the regions, empty when absent or not an array
     */
    public List<String> allowedDataRegions() {
        try {
            JSONObject data = json.optJSONObject("data");
            if (data == null) {
                return new ArrayList<>();
            }
            JSONArray regions = data.optJSONArray("allowed_data_regions");
            if (regions == null) {
                return new ArrayList<>();
            }
            List<String> result = new ArrayList<>();
            for (int i = 0; i < regions.length(); i++) {
                Object value = regions.opt(i);
                if (value instanceof String region) {
                    result.add(region);
                }
            }
            return result;
        } catch (Exception ignored) {
            // swallow
        }
        return new ArrayList<>();
    }

    /**
     * JSON path: {@code data.free_model_daily_requests} - the free-model
     * ({@code :free} variant) daily request quota of the account that owns
     * the key; see {@link OpenRouterFreeModelDailyRequests} for the quota
     * semantics and the "tier policy, not a real budget" trap.
     *
     * @return the typed view, or {@code null} when absent or not an object
     */
    public OpenRouterFreeModelDailyRequests freeModelDailyRequests() {
        try {
            JSONObject data = json.optJSONObject("data");
            if (data == null) {
                return null;
            }
            JSONObject quota = data.optJSONObject("free_model_daily_requests");
            return quota == null ? null : new OpenRouterFreeModelDailyRequests(quota);
        } catch (Exception ignored) {
            // swallow
        }
        return null;
    }

    /**
     * JSON path: {@code data.organization_id} - the owning organization,
     * {@code null} for personal keys.
     *
     * @return the value, or {@code null} when absent or not a string
     */
    public String organizationId() {
        return optDataStringStrict("organization_id");
    }

    /**
     * JSON path: {@code data.workspace_id} - the key's workspace scope.
     *
     * @return the value, or {@code null} when absent or not a string
     */
    public String workspaceId() {
        return optDataStringStrict("workspace_id");
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

    private String optDataStringStrict(String key) {
        try {
            JSONObject data = json.optJSONObject("data");
            if (data != null) {
                Object value = data.opt(key);
                return value instanceof String text ? text : null;
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
