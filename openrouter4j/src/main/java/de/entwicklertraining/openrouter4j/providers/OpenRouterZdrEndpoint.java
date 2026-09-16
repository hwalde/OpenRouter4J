package de.entwicklertraining.openrouter4j.providers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A typed view of one endpoint row of GET /endpoints/zdr ({@code data[]}):
 * a serving endpoint that remains available under zero-data-retention
 * routing.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterZdrEndpoint {

    private final JSONObject json;

    OpenRouterZdrEndpoint(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw endpoint row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code name} - the endpoint display name (e.g. {@code "OpenAI: GPT-4"}).
     *
     * @return the value, or {@code null} when absent
     */
    public String name() {
        return json.optString("name", null);
    }

    /**
     * JSON path: {@code model_id} - the model id served (e.g. {@code openai/gpt-4}).
     *
     * @return the value, or {@code null} when absent
     */
    public String modelId() {
        return json.optString("model_id", null);
    }

    /**
     * JSON path: {@code model_name} - the human-readable model name.
     *
     * @return the value, or {@code null} when absent
     */
    public String modelName() {
        return json.optString("model_name", null);
    }

    /**
     * JSON path: {@code provider_name} - the provider serving this endpoint.
     *
     * @return the value, or {@code null} when absent
     */
    public String providerName() {
        return json.optString("provider_name", null);
    }

    /**
     * JSON path: {@code tag} - the provider tag/slug.
     *
     * @return the value, or {@code null} when absent
     */
    public String tag() {
        return json.optString("tag", null);
    }

    /**
     * JSON path: {@code context_length} - the endpoint context length in tokens.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long contextLength() {
        if (!json.has("context_length") || json.isNull("context_length")) {
            return null;
        }
        return json.optLong("context_length");
    }

    /**
     * JSON path: {@code max_prompt_tokens}.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long maxPromptTokens() {
        if (!json.has("max_prompt_tokens") || json.isNull("max_prompt_tokens")) {
            return null;
        }
        return json.optLong("max_prompt_tokens");
    }

    /**
     * JSON path: {@code max_completion_tokens}.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long maxCompletionTokens() {
        if (!json.has("max_completion_tokens") || json.isNull("max_completion_tokens")) {
            return null;
        }
        return json.optLong("max_completion_tokens");
    }

    /**
     * JSON path: {@code quantization} - e.g. {@code fp16}, {@code fp8}, {@code int4}.
     *
     * @return the value, or {@code null} when absent
     */
    public String quantization() {
        return json.optString("quantization", null);
    }

    /**
     * JSON path: {@code status} - the endpoint status code (0 means up).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Integer status() {
        if (!json.has("status") || json.isNull("status")) {
            return null;
        }
        return (int) json.optLong("status");
    }

    /**
     * JSON path: {@code pricing} - the per-token prices as strings
     * ({@code prompt}, {@code completion}, {@code request}, {@code image}).
     *
     * @return the raw pricing object, or {@code null} when absent
     */
    public JSONObject pricing() {
        return json.optJSONObject("pricing");
    }

    /**
     * JSON path: {@code supported_parameters[]} - the parameters this endpoint
     * supports (the data behind the {@code requireParameters(true)} pitfall).
     *
     * @return the parameter names, empty when absent
     */
    public List<String> supportedParameters() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray parameters = json.optJSONArray("supported_parameters");
            if (parameters != null) {
                for (int i = 0; i < parameters.length(); i++) {
                    String value = parameters.optString(i, null);
                    if (value != null) {
                        result.add(value);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * JSON path: {@code supports_implicit_caching}.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean supportsImplicitCaching() {
        if (!json.has("supports_implicit_caching") || json.isNull("supports_implicit_caching")) {
            return null;
        }
        return json.optBoolean("supports_implicit_caching");
    }

    /**
     * JSON path: {@code supports_voice_cloning}.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean supportsVoiceCloning() {
        if (!json.has("supports_voice_cloning") || json.isNull("supports_voice_cloning")) {
            return null;
        }
        return json.optBoolean("supports_voice_cloning");
    }

    /**
     * JSON path: {@code uptime_last_5m} - uptime percentage over the last five minutes.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double uptimeLast5m() {
        return optDouble("uptime_last_5m");
    }

    /**
     * JSON path: {@code uptime_last_30m} - uptime percentage over the last thirty minutes.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double uptimeLast30m() {
        return optDouble("uptime_last_30m");
    }

    /**
     * JSON path: {@code uptime_last_1d} - uptime percentage over the last day.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double uptimeLast1d() {
        return optDouble("uptime_last_1d");
    }

    /**
     * JSON path: {@code latency_last_30m.p50} - median latency (seconds) over the last thirty minutes.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double latencyP50() {
        return optPercentile("latency_last_30m", "p50");
    }

    /**
     * JSON path: {@code latency_last_30m.p75}.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double latencyP75() {
        return optPercentile("latency_last_30m", "p75");
    }

    /**
     * JSON path: {@code latency_last_30m.p90}.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double latencyP90() {
        return optPercentile("latency_last_30m", "p90");
    }

    /**
     * JSON path: {@code latency_last_30m.p99}.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double latencyP99() {
        return optPercentile("latency_last_30m", "p99");
    }

    /**
     * JSON path: {@code throughput_last_30m.p50} - median throughput (tokens/s)
     * over the last thirty minutes.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double throughputP50() {
        return optPercentile("throughput_last_30m", "p50");
    }

    /**
     * JSON path: {@code throughput_last_30m.p75}.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double throughputP75() {
        return optPercentile("throughput_last_30m", "p75");
    }

    /**
     * JSON path: {@code throughput_last_30m.p90}.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double throughputP90() {
        return optPercentile("throughput_last_30m", "p90");
    }

    /**
     * JSON path: {@code throughput_last_30m.p99}.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double throughputP99() {
        return optPercentile("throughput_last_30m", "p99");
    }

    private Double optDouble(String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        return json.optDouble(key);
    }

    private Double optPercentile(String objectKey, String percentileKey) {
        JSONObject object = json.optJSONObject(objectKey);
        if (object == null || !object.has(percentileKey) || object.isNull(percentileKey)) {
            return null;
        }
        return object.optDouble(percentileKey);
    }
}
