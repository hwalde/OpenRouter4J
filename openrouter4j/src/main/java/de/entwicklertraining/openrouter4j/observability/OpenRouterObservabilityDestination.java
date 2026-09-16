package de.entwicklertraining.openrouter4j.observability;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one observability destination of the management endpoints
 * (GET /observability/destinations, GET /observability/destinations/{id}, the
 * {@code data} of POST and PATCH /observability/destinations/{id}): where traces
 * are broadcast to (Langfuse, Datadog, Weave, ...).
 *
 * <p>Destinations receive the traces that requests opt into via the
 * {@code trace} request object ({@code OpenRouterTraceConfig}).
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterObservabilityDestination {

    private final JSONObject json;

    OpenRouterObservabilityDestination(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw JSON row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
 * JSON path: {@code id} - unique identifier (UUID) of the destination; the
 * path segment of GET/PATCH/DELETE /observability/destinations/{id}.
 */
    public String id() {
        return json.optString("id", null);
    }
    /**
 * JSON path: {@code type} - the destination type (langfuse, datadog, weave,
 * sentry, s3, webhook, ...).
 */
    public String type() {
        return json.optString("type", null);
    }
    /**
 * JSON path: {@code name} - human-readable name of the destination.
 */
    public String name() {
        return json.optString("name", null);
    }
    /**
 * JSON path: {@code enabled} - whether the destination is enabled.
 */
    public Boolean enabled() {
        if (!json.has("enabled") || json.isNull("enabled")) {
            return null;
        }
        return json.optBoolean("enabled");
    }
    /**
 * JSON path: {@code regions} - the data regions this destination applies to
 * ({@code global}, {@code europe}, {@code us}; {@code eu} is normalized to
 * {@code europe}), empty when absent.
 */
    public List<String> regions() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("regions");
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
 * JSON path: {@code sampling_rate} - sampling rate between 0.0001 and 1
 * (1 = 100%), or {@code null} when absent.
 */
    public Double samplingRate() {
        if (!json.has("sampling_rate") || json.isNull("sampling_rate")) {
            return null;
        }
        return json.optDouble("sampling_rate");
    }
    /**
 * JSON path: {@code privacy_mode} - whether request/response bodies are
 * withheld and only metadata is forwarded.
 */
    public Boolean privacyMode() {
        if (!json.has("privacy_mode") || json.isNull("privacy_mode")) {
            return null;
        }
        return json.optBoolean("privacy_mode");
    }
    /**
 * JSON path: {@code broadcast_generation_cost} - whether cost and billing
 * generation metadata are included.
 */
    public Boolean broadcastGenerationCost() {
        if (!json.has("broadcast_generation_cost") || json.isNull("broadcast_generation_cost")) {
            return null;
        }
        return json.optBoolean("broadcast_generation_cost");
    }
    /**
 * JSON path: {@code broadcast_generation_identity} - whether identity
 * generation metadata is included.
 */
    public Boolean broadcastGenerationIdentity() {
        if (!json.has("broadcast_generation_identity") || json.isNull("broadcast_generation_identity")) {
            return null;
        }
        return json.optBoolean("broadcast_generation_identity");
    }
    /**
 * JSON path: {@code broadcast_generation_request_context} - whether
 * request-context generation metadata is included.
 */
    public Boolean broadcastGenerationRequestContext() {
        if (!json.has("broadcast_generation_request_context") || json.isNull("broadcast_generation_request_context")) {
            return null;
        }
        return json.optBoolean("broadcast_generation_request_context");
    }
    /**
 * JSON path: {@code api_key_hashes} - the OpenRouter API key hashes whose
 * traffic is forwarded, empty when absent or null (all keys).
 */
    public List<String> apiKeyHashes() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("api_key_hashes");
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
 * JSON path: {@code filter_rules} - the structured filter rules as a raw JSON
 * object ({@code enabled}, {@code groups}), or {@code null} when none are set.
 */
    public JSONObject filterRules() {
        return json.optJSONObject("filter_rules");
    }
    /**
 * JSON path: {@code config} - the destination-type-specific configuration as a
 * raw JSON object (e.g. {@code baseUrl}/{@code publicKey}/{@code secretKey} for
 * langfuse); the shape depends on {@link #type()} and may contain masked
 * secrets.
 */
    public JSONObject config() {
        return json.optJSONObject("config");
    }
    /**
 * JSON path: {@code workspace_id} - the workspace the destination belongs to.
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
 * JSON path: {@code updated_at} - ISO 8601 last-update timestamp.
 */
    public String updatedAt() {
        return json.optString("updated_at", null);
    }
}
