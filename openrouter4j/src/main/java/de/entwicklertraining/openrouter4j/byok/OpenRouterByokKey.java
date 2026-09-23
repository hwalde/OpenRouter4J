package de.entwicklertraining.openrouter4j.byok;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one BYOK provider credential of the management endpoints
 * (GET /byok, GET /byok/{id}, the {@code data} of POST and PATCH /byok/{id}):
 * provider, masked label, allowlists and fallback behaviour.
 *
 * <p>The raw credential never travels in this view - only the masked
 * {@link #label()}; it cannot be retrieved after creation or rotation.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterByokKey {

    private final JSONObject json;

    OpenRouterByokKey(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw JSON row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
 * JSON path: {@code id} - stable public identifier (UUID) of the credential;
 * the path segment of GET/PATCH/DELETE /byok/{id}.
 */
    public String id() {
        return json.optString("id", null);
    }
    /**
 * JSON path: {@code provider} - the upstream provider slug the credential
 * authenticates against (e.g. {@code openai}, {@code anthropic},
 * {@code amazon-bedrock}).
 */
    public String provider() {
        return json.optString("provider", null);
    }
    /**
 * JSON path: {@code label} - short masked snippet of the key (e.g.
 * {@code sk-...AbCd}) used to identify it; never the full credential.
 */
    public String label() {
        return json.optString("label", null);
    }
    /**
 * JSON path: {@code name} - human-readable name of the credential, or
 * {@code null} when unset.
 */
    public String name() {
        return json.optString("name", null);
    }
    /**
 * JSON path: {@code disabled} - whether the credential is currently disabled.
 */
    public Boolean disabled() {
        if (!json.has("disabled") || json.isNull("disabled")) {
            return null;
        }
        return json.optBoolean("disabled");
    }
    /**
 * JSON path: {@code is_fallback} - whether the credential is used only after
 * non-fallback keys for the same provider have been tried. Cannot be combined
 * with {@link #isByokOnly()}.
 */
    public Boolean isFallback() {
        if (!json.has("is_fallback") || json.isNull("is_fallback")) {
            return null;
        }
        return json.optBoolean("is_fallback");
    }
    /**
 * JSON path: {@code is_required} - whether OpenRouter's shared endpoints on
 * this provider are removed for the models this credential applies to; requests
 * for those models run only on your own keys.
 */
    public Boolean isRequired() {
        if (!json.has("is_required") || json.isNull("is_required")) {
            return null;
        }
        return json.optBoolean("is_required");
    }
    /**
 * JSON path: {@code is_byok_only} - whether the provider's shared endpoints are
 * removed for every model and the provider is skipped instead of spending
 * OpenRouter credits. Only valid on non-fallback credentials.
 */
    public Boolean isByokOnly() {
        if (!json.has("is_byok_only") || json.isNull("is_byok_only")) {
            return null;
        }
        return json.optBoolean("is_byok_only");
    }
    /**
 * JSON path: {@code declared_zdr} - your self-declaration of whether the
 * upstream provider account behind this credential has zero data retention
 * ({@code null} = inherit OpenRouter's data policy, {@code true} = the account
 * is ZDR so ZDR-routed requests may use this credential, {@code false} =
 * non-ZDR so they never do). Self-declared and not verified by OpenRouter.
 */
    public Boolean declaredZdr() {
        if (!json.has("declared_zdr") || json.isNull("declared_zdr")) {
            return null;
        }
        return json.optBoolean("declared_zdr");
    }
    /**
 * JSON path: {@code allowed_models} - model slugs this credential may be used
 * for, empty when absent or null (no restriction).
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
 * JSON path: {@code allowed_api_key_hashes} - OpenRouter API key hashes that
 * may use this credential, empty when absent or null (no restriction).
 */
    public List<String> allowedApiKeyHashes() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("allowed_api_key_hashes");
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
 * JSON path: {@code allowed_user_ids} - user ids that may use this credential,
 * empty when absent or null (no restriction).
 */
    public List<String> allowedUserIds() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("allowed_user_ids");
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
 * JSON path: {@code sort_order} - position within the provider; credentials
 * are tried in ascending sort order.
 */
    public Integer sortOrder() {
        if (!json.has("sort_order") || json.isNull("sort_order")) {
            return null;
        }
        return (int) json.optLong("sort_order");
    }
    /**
 * JSON path: {@code workspace_id} - the workspace this credential is scoped
 * to, or {@code null} when it is global (usable across every workspace of the
 * account; a {@code null} value does not mean the default workspace).
 */
    public String workspaceId() {
        return json.optString("workspace_id", null);
    }
    /**
 * JSON path: {@code created_at} - ISO 8601 timestamp of when the credential
 * was created.
 */
    public String createdAt() {
        return json.optString("created_at", null);
    }
}
