package de.entwicklertraining.openrouter4j.guardrails;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one guardrail of the management endpoints
 * (GET /guardrails, GET /guardrails/{id}, the {@code data} of POST and PATCH
 * /guardrails/{id}): routing restrictions, data regions, spend limit, content
 * filters and zero-data-retention enforcement.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 *
 * <p>Management endpoints require a management key; see the request classes.
 */
public final class OpenRouterGuardrail {

    private final JSONObject json;

    OpenRouterGuardrail(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw JSON row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
 * JSON path: {@code id} - unique identifier (UUID) of the guardrail; the
 * path segment of GET/PATCH/DELETE /guardrails/{id}.
 */
    public String id() {
        return json.optString("id", null);
    }
    /**
 * JSON path: {@code name} - display name of the guardrail.
 */
    public String name() {
        return json.optString("name", null);
    }
    /**
 * JSON path: {@code description} - human-readable description, or {@code null}
 * when unset.
 */
    public String description() {
        return json.optString("description", null);
    }
    /**
 * JSON path: {@code allowed_models} - model identifiers (slug or
 * canonical_slug) the guardrail restricts traffic to, empty when absent or null.
 */
    public List<String> allowedModels() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("allowed_models");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    String entry = arr.optString(i, null);
                    if (entry != null) {
                        result.add(entry);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
    /**
 * JSON path: {@code ignored_models} - model identifiers excluded from routing,
 * empty when absent or null.
 */
    public List<String> ignoredModels() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("ignored_models");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    String entry = arr.optString(i, null);
                    if (entry != null) {
                        result.add(entry);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
    /**
 * JSON path: {@code allowed_providers} - provider ids the guardrail restricts
 * routing to, empty when absent or null.
 */
    public List<String> allowedProviders() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("allowed_providers");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    String entry = arr.optString(i, null);
                    if (entry != null) {
                        result.add(entry);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
    /**
 * JSON path: {@code ignored_providers} - provider ids excluded from routing,
 * empty when absent or null.
 */
    public List<String> ignoredProviders() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("ignored_providers");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    String entry = arr.optString(i, null);
                    if (entry != null) {
                        result.add(entry);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
    /**
 * JSON path: {@code allowed_data_regions} - data regions requests governed by
 * this guardrail must arrive through ({@code global}, {@code europe}, {@code us}),
 * empty when absent or null. The effective regions are the intersection of every
 * non-null value when several guardrails apply.
 */
    public List<String> allowedDataRegions() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("allowed_data_regions");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    String entry = arr.optString(i, null);
                    if (entry != null) {
                        result.add(entry);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
    /**
 * JSON path: {@code content_filters} - the custom regex content filters as raw
 * JSON objects ({@code action}, {@code pattern}, optional {@code label}), empty
 * when absent or null.
 */
    public List<JSONObject> contentFilters() {
        List<JSONObject> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("content_filters");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject entry = arr.optJSONObject(i);
                    if (entry != null) {
                        result.add(entry);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
    /**
 * JSON path: {@code content_filter_builtins} - the builtin content filters as
 * raw JSON objects ({@code slug}, {@code action}, optional {@code label}), empty
 * when absent or null.
 */
    public List<JSONObject> contentFilterBuiltins() {
        List<JSONObject> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("content_filter_builtins");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject entry = arr.optJSONObject(i);
                    if (entry != null) {
                        result.add(entry);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
    /**
 * JSON path: {@code limit_usd} - spending limit in USD, or {@code null} when no
 * limit is set. Always paired with {@link #resetInterval()}.
 */
    public Double limitUsd() {
        if (!json.has("limit_usd") || json.isNull("limit_usd")) {
            return null;
        }
        return json.optDouble("limit_usd");
    }
    /**
 * JSON path: {@code reset_interval} - interval at which the limit resets
 * ({@code daily}, {@code weekly}, {@code monthly}), or {@code null} when absent.
 */
    public String resetInterval() {
        return json.optString("reset_interval", null);
    }
    /**
 * JSON path: {@code include_byok_in_budgets} - whether BYOK spend counts toward
 * {@link #limitUsd()}.
 */
    public Boolean includeByokInBudgets() {
        if (!json.has("include_byok_in_budgets") || json.isNull("include_byok_in_budgets")) {
            return null;
        }
        return json.optBoolean("include_byok_in_budgets");
    }
    /**
 * JSON path: {@code enforce_zdr} - deprecated blanket zero-data-retention
 * switch; the server copies it into the per-provider fields. Prefer the
 * per-provider accessors.
 */
    public Boolean enforceZdr() {
        if (!json.has("enforce_zdr") || json.isNull("enforce_zdr")) {
            return null;
        }
        return json.optBoolean("enforce_zdr");
    }
    /**
 * JSON path: {@code enforce_zdr_anthropic} - whether zero data retention is
 * enforced for Anthropic models.
 */
    public Boolean enforceZdrAnthropic() {
        if (!json.has("enforce_zdr_anthropic") || json.isNull("enforce_zdr_anthropic")) {
            return null;
        }
        return json.optBoolean("enforce_zdr_anthropic");
    }
    /**
 * JSON path: {@code enforce_zdr_google} - whether zero data retention is enforced
 * for Google models.
 */
    public Boolean enforceZdrGoogle() {
        if (!json.has("enforce_zdr_google") || json.isNull("enforce_zdr_google")) {
            return null;
        }
        return json.optBoolean("enforce_zdr_google");
    }
    /**
 * JSON path: {@code enforce_zdr_openai} - whether zero data retention is enforced
 * for OpenAI models.
 */
    public Boolean enforceZdrOpenai() {
        if (!json.has("enforce_zdr_openai") || json.isNull("enforce_zdr_openai")) {
            return null;
        }
        return json.optBoolean("enforce_zdr_openai");
    }
    /**
 * JSON path: {@code enforce_zdr_xai} - whether zero data retention is enforced
 * for xAI models.
 */
    public Boolean enforceZdrXai() {
        if (!json.has("enforce_zdr_xai") || json.isNull("enforce_zdr_xai")) {
            return null;
        }
        return json.optBoolean("enforce_zdr_xai");
    }
    /**
 * JSON path: {@code enforce_zdr_other} - whether zero data retention is enforced
 * for models of other providers.
 */
    public Boolean enforceZdrOther() {
        if (!json.has("enforce_zdr_other") || json.isNull("enforce_zdr_other")) {
            return null;
        }
        return json.optBoolean("enforce_zdr_other");
    }
    /**
 * JSON path: {@code enable_free_model_publication} - whether free endpoints that
 * publish prompts are allowed.
 */
    public Boolean enableFreeModelPublication() {
        if (!json.has("enable_free_model_publication") || json.isNull("enable_free_model_publication")) {
            return null;
        }
        return json.optBoolean("enable_free_model_publication");
    }
    /**
 * JSON path: {@code enable_free_model_training} - whether free endpoints that
 * train on request data are allowed.
 */
    public Boolean enableFreeModelTraining() {
        if (!json.has("enable_free_model_training") || json.isNull("enable_free_model_training")) {
            return null;
        }
        return json.optBoolean("enable_free_model_training");
    }
    /**
 * JSON path: {@code enable_paid_model_training} - whether paid endpoints that
 * train on request data are allowed.
 */
    public Boolean enablePaidModelTraining() {
        if (!json.has("enable_paid_model_training") || json.isNull("enable_paid_model_training")) {
            return null;
        }
        return json.optBoolean("enable_paid_model_training");
    }
    /**
 * JSON path: {@code model_catalog} - the catalog presentation policy for
 * {@code GET /api/v1/models/user} as a raw JSON object ({@code models}, {@code sort},
 * {@code include_private_models}, ...), or {@code null} when no policy is set.
 */
    public JSONObject modelCatalog() {
        return json.optJSONObject("model_catalog");
    }
    /**
 * JSON path: {@code workspace_id} - the workspace the guardrail belongs to, or
 * {@code null} for an unscoped legacy guardrail. Workspace membership organizes
 * the guardrail; it does not apply it to the workspace's traffic.
 */
    public String workspaceId() {
        return json.optString("workspace_id", null);
    }
    /**
 * JSON path: {@code created_at} - ISO 8601 creation timestamp.
 */
    public String createdAt() {
        return json.optString("created_at", null);
    }
    /**
 * JSON path: {@code updated_at} - ISO 8601 last-update timestamp, or {@code null}
 * when absent.
 */
    public String updatedAt() {
        return json.optString("updated_at", null);
    }
}
