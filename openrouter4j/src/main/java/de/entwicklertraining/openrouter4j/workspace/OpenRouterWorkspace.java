package de.entwicklertraining.openrouter4j.workspace;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one workspace of the management endpoints
 * (GET /workspaces, GET /workspaces/{id}, the {@code data} of POST and PATCH
 * /workspaces/{id}): name, slug, defaults, I/O-logging and observability
 * settings.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 *
 * <p>Management endpoints require a management key; see the request classes.
 */
public final class OpenRouterWorkspace {

    private final JSONObject json;

    OpenRouterWorkspace(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw JSON row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
 * JSON path: {@code id} - unique identifier (UUID) of the workspace; the
 * path segment of GET/PATCH/DELETE /workspaces/{id}.
 */
    public String id() {
        return json.optString("id", null);
    }
    /**
 * JSON path: {@code name} - display name of the workspace.
 */
    public String name() {
        return json.optString("name", null);
    }
    /**
 * JSON path: {@code slug} - URL-friendly slug of the workspace; usable as
 * the {@code workspace_ref} path segment of the budget endpoints.
 */
    public String slug() {
        return json.optString("slug", null);
    }
    /**
 * JSON path: {@code description} - human-readable description, or {@code null}
 * when unset.
 */
    public String description() {
        return json.optString("description", null);
    }
    /**
 * JSON path: {@code default_text_model} - default text model for this
 * workspace, or {@code null} when unset.
 */
    public String defaultTextModel() {
        return json.optString("default_text_model", null);
    }
    /**
 * JSON path: {@code default_image_model} - default image model for this
 * workspace, or {@code null} when unset.
 */
    public String defaultImageModel() {
        return json.optString("default_image_model", null);
    }
    /**
 * JSON path: {@code default_provider_sort} - default provider sort preference
 * (price, throughput, latency, exacto), or {@code null} when unset.
 */
    public String defaultProviderSort() {
        return json.optString("default_provider_sort", null);
    }
    /**
 * JSON path: {@code is_observability_io_logging_enabled} - whether private
 * logging is enabled for this workspace.
 */
    public Boolean isObservabilityIoLoggingEnabled() {
        if (!json.has("is_observability_io_logging_enabled") || json.isNull("is_observability_io_logging_enabled")) {
            return null;
        }
        return json.optBoolean("is_observability_io_logging_enabled");
    }
    /**
 * JSON path: {@code is_observability_broadcast_enabled} - whether broadcast is
 * enabled for this workspace.
 */
    public Boolean isObservabilityBroadcastEnabled() {
        if (!json.has("is_observability_broadcast_enabled") || json.isNull("is_observability_broadcast_enabled")) {
            return null;
        }
        return json.optBoolean("is_observability_broadcast_enabled");
    }
    /**
 * JSON path: {@code is_data_discount_logging_enabled} - whether data discount
 * logging is enabled for this workspace.
 */
    public Boolean isDataDiscountLoggingEnabled() {
        if (!json.has("is_data_discount_logging_enabled") || json.isNull("is_data_discount_logging_enabled")) {
            return null;
        }
        return json.optBoolean("is_data_discount_logging_enabled");
    }
    /**
 * JSON path: {@code io_logging_sampling_rate} - I/O-logging sampling rate
 * (0.0001-1); 1 means every request is logged.
 */
    public Double ioLoggingSamplingRate() {
        if (!json.has("io_logging_sampling_rate") || json.isNull("io_logging_sampling_rate")) {
            return null;
        }
        return json.optDouble("io_logging_sampling_rate");
    }
    /**
 * JSON path: {@code io_logging_api_key_ids} - the API key ids I/O logging is
 * filtered to, empty when absent; {@code null} on the wire means all keys are
 * logged, which this accessor cannot distinguish from an empty list.
 */
    public List<Integer> ioLoggingApiKeyIds() {
        List<Integer> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("io_logging_api_key_ids");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    if (!arr.isNull(i)) {
                        result.add((int) arr.optLong(i));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
    /**
 * JSON path: {@code include_byok_in_budgets} - whether BYOK spend counts toward
 * this workspace's budgets (set via the workspace budget endpoints).
 */
    public Boolean includeByokInBudgets() {
        if (!json.has("include_byok_in_budgets") || json.isNull("include_byok_in_budgets")) {
            return null;
        }
        return json.optBoolean("include_byok_in_budgets");
    }
    /**
 * JSON path: {@code default_guardrail_id} - deterministic id of the workspace's
 * default guardrail, materialized when its configuration is first written.
 */
    public String defaultGuardrailId() {
        return json.optString("default_guardrail_id", null);
    }
    /**
 * JSON path: {@code created_at} - ISO 8601 creation timestamp.
 */
    public String createdAt() {
        return json.optString("created_at", null);
    }
    /**
 * JSON path: {@code updated_at} - ISO 8601 last-update timestamp, or
 * {@code null} when absent.
 */
    public String updatedAt() {
        return json.optString("updated_at", null);
    }
    /**
 * JSON path: {@code created_by} - user id of the workspace creator, or
 * {@code null} when absent.
 */
    public String createdBy() {
        return json.optString("created_by", null);
    }
}
