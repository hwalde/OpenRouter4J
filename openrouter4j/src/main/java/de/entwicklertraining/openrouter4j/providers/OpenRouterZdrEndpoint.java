package de.entwicklertraining.openrouter4j.providers;

import de.entwicklertraining.openrouter4j.OpenRouterPercentileStats;
import de.entwicklertraining.openrouter4j.OpenRouterToolChoiceSupport;
import de.entwicklertraining.openrouter4j.OpenRouterWorkloadPerf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A typed view of one endpoint row of GET /endpoints/zdr ({@code data[]}):
 * a serving endpoint that remains available under zero-data-retention
 * routing. The row follows the same {@code PublicEndpoint} schema as the
 * endpoints of GET /models/{author}/{slug}/endpoints (typed as
 * {@code OpenRouterModelEndpoint} in the {@code models} package), plus the
 * ZDR-specific {@link #safetyRetention()} flag.
 *
 * <p>The reliability and performance numbers ({@link #uptimeLast5m()},
 * {@link #latencyP50()} and siblings, {@link #perfLast30mByWorkload()}) are
 * only visible when authenticated with an API key or cookie;
 * unauthenticated requests read {@code null}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} / -empty
 * convention; use {@link #json()} for fields without a typed accessor. The
 * flat {@code latencyP50()} / {@code throughputP50()} and siblings are the
 * shipped form of the two legacy percentile objects on this view.
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
     * @return the value, or {@code null} when absent or not a string
     */
    public String name() {
        return optString("name");
    }

    /**
     * JSON path: {@code model_id} - the model id served (e.g. {@code openai/gpt-4}).
     *
     * @return the value, or {@code null} when absent or not a string
     */
    public String modelId() {
        return optString("model_id");
    }

    /**
     * JSON path: {@code model_name} - the human-readable model name.
     *
     * @return the value, or {@code null} when absent or not a string
     */
    public String modelName() {
        return optString("model_name");
    }

    /**
     * JSON path: {@code provider_name} - the provider serving this endpoint.
     *
     * @return the value, or {@code null} when absent or not a string
     */
    public String providerName() {
        return optString("provider_name");
    }

    /**
     * JSON path: {@code tag} - the provider tag/slug.
     *
     * @return the value, or {@code null} when absent or not a string
     */
    public String tag() {
        return optString("tag");
    }

    /**
     * JSON path: {@code context_length} - the endpoint context length in tokens.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long contextLength() {
        return optLong("context_length");
    }

    /**
     * JSON path: {@code max_prompt_tokens}.
     *
     * @return the value, or {@code null} when absent, not a number or JSON
     *         {@code null}
     */
    public Long maxPromptTokens() {
        return optLong("max_prompt_tokens");
    }

    /**
     * JSON path: {@code max_completion_tokens}. Input and output tokens
     * share the context window, so the effective maximum output for a
     * request is further limited by the context remaining after input
     * tokens.
     *
     * @return the value, or {@code null} when absent, not a number or JSON
     *         {@code null}
     */
    public Long maxCompletionTokens() {
        return optLong("max_completion_tokens");
    }

    /**
     * JSON path: {@code quantization} - e.g. {@code fp16}, {@code fp8}, {@code int4};
     * unknown values are passed through verbatim.
     *
     * @return the value, or {@code null} when absent, not a string or JSON
     *         {@code null}
     */
    public String quantization() {
        return optString("quantization");
    }

    /**
     * JSON path: {@code status} - the endpoint status code (0 means up).
     * The schema declares {@code 0}, {@code -1}, {@code -2}, {@code -3},
     * {@code -5} and {@code -10} and passes unknown values through
     * verbatim.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Integer status() {
        Object value = json.opt("status");
        return value instanceof Number number ? number.intValue() : null;
    }

    /**
     * JSON path: {@code pricing} - the per-token prices as strings
     * ({@code prompt}, {@code completion}, {@code request}, {@code image}) -
     * do not parse to double, use {@code BigDecimal}.
     *
     * @return the raw pricing object, or {@code null} when absent
     */
    public JSONObject pricing() {
        return json.optJSONObject("pricing");
    }

    /**
     * JSON path: {@code supported_parameters[]} - the parameters this endpoint
     * supports (the data behind the {@code requireParameters(true)} pitfall).
     * Non-string entries are dropped.
     *
     * @return the parameter names, empty when absent or not an array
     */
    public List<String> supportedParameters() {
        List<String> result = new ArrayList<>();
        JSONArray parameters = json.optJSONArray("supported_parameters");
        if (parameters == null) {
            return result;
        }
        for (int i = 0; i < parameters.length(); i++) {
            Object value = parameters.opt(i);
            if (value instanceof String name) {
                result.add(name);
            }
        }
        return result;
    }

    /**
     * JSON path: {@code supports_implicit_caching}.
     *
     * @return the value, or {@code null} when absent or not a boolean
     */
    public Boolean supportsImplicitCaching() {
        return optBoolean("supports_implicit_caching");
    }

    /**
     * JSON path: {@code supports_voice_cloning} - whether this TTS endpoint
     * accepts inline reference audio ({@code input_references}) for
     * stateless voice cloning. Requests carrying reference audio are only
     * routed to endpoints where this is {@code true}.
     *
     * @return the value, or {@code null} when absent or not a boolean
     */
    public Boolean supportsVoiceCloning() {
        return optBoolean("supports_voice_cloning");
    }

    /**
     * JSON path: {@code supports_image_reference} - whether this TTS
     * endpoint accepts an {@code image_url} reference describing the desired
     * voice. Requests carrying an image reference are only routed to
     * endpoints where this is {@code true}.
     *
     * @return the value, or {@code null} when absent or not a boolean
     */
    public Boolean supportsImageReference() {
        return optBoolean("supports_image_reference");
    }

    /**
     * JSON path: {@code supports_multiple_audio_references} - whether this
     * TTS endpoint accepts more than one {@code input_audio} reference clip
     * per request. Requests carrying several clips are only routed to
     * endpoints where this is {@code true}.
     *
     * @return the value, or {@code null} when absent or not a boolean
     */
    public Boolean supportsMultipleAudioReferences() {
        return optBoolean("supports_multiple_audio_references");
    }

    /**
     * JSON path: {@code supports_tool_choice} - per-variant
     * {@code tool_choice} support; {@code tool_choice} in
     * {@link #supportedParameters()} only says the parameter is accepted,
     * these flags say which of its values passed testing.
     *
     * @return the support flags, or {@code null} when absent or not an
     *         object
     */
    public OpenRouterToolChoiceSupport supportsToolChoice() {
        JSONObject support = json.optJSONObject("supports_tool_choice");
        return support != null ? new OpenRouterToolChoiceSupport(support) : null;
    }

    /**
     * JSON path: {@code service_tier} - the service tier this endpoint
     * sells, or {@code null} for a standard endpoint. Always the canonical
     * tier: {@code priority} covers the fast request alias and the turbo,
     * highspeed and prime provider model-id spellings. The schema declares
     * {@code flex}, {@code priority} and {@code ultrafast} and passes
     * unknown values through verbatim.
     *
     * @return the value, or {@code null} when absent, not a string or JSON
     *         {@code null} (a standard endpoint)
     */
    public String serviceTier() {
        return optString("service_tier");
    }

    /**
     * JSON path: {@code supports_diarization} - whether this STT endpoint
     * can label each transcript segment with a speaker (speaker
     * diarization). The API answers {@code false} when the endpoint has not
     * declared support.
     *
     * @return the value, or {@code null} when absent or not a boolean
     */
    public Boolean supportsDiarization() {
        return optBoolean("supports_diarization");
    }

    /**
     * JSON path: {@code native_tools} - the server tools this endpoint
     * accepts as the provider's own built-in tool ({@code engine: "native"})
     * instead of an OpenRouter engine, keyed by canonical
     * {@code openrouter:*} name. Each value names the provider tool type the
     * request is translated to (its {@code type} field). Non-object entries
     * are dropped.
     *
     * @return the native tool entries as raw objects, empty when absent or
     *         not an object
     */
    public Map<String, JSONObject> nativeTools() {
        Map<String, JSONObject> result = new LinkedHashMap<>();
        JSONObject tools = json.optJSONObject("native_tools");
        if (tools == null) {
            return result;
        }
        for (String key : tools.keySet()) {
            JSONObject entry = tools.optJSONObject(key);
            if (entry != null) {
                result.put(key, entry);
            }
        }
        return result;
    }

    /**
     * JSON path: {@code safety_retention} - whether the endpoint uses Safety
     * Retention: prompts are not retained by default, but the provider may
     * retain prompts its safety classifiers flag. A ZDR-enforced request
     * reaches these endpoints only when the account allows Safety Retention
     * and no applied guardrail disallows it.
     *
     * @return the value, or {@code null} when absent or not a boolean
     */
    public Boolean safetyRetention() {
        return optBoolean("safety_retention");
    }

    /**
     * JSON path: {@code uptime_last_5m} - uptime percentage over the last five minutes:
     * the share of minutes in which at least 80% of provider attempts
     * succeeded, counting only minutes with 10 or more attempts.
     * Rate-limited and caller-caused failures are excluded.
     *
     * @return the value, or {@code null} when absent, not a number or when
     *         no minute had enough traffic
     */
    public Double uptimeLast5m() {
        return optDouble("uptime_last_5m");
    }

    /**
     * JSON path: {@code uptime_last_30m} - uptime percentage over the last
     * thirty minutes (same rule as {@link #uptimeLast5m()}).
     *
     * @return the value, or {@code null} when absent, not a number or when
     *         no minute had enough traffic
     */
    public Double uptimeLast30m() {
        return optDouble("uptime_last_30m");
    }

    /**
     * JSON path: {@code uptime_last_1d} - uptime percentage over the last
     * day (same rule as {@link #uptimeLast5m()}).
     *
     * @return the value, or {@code null} when absent, not a number or when
     *         no minute had enough traffic
     */
    public Double uptimeLast1d() {
        return optDouble("uptime_last_1d");
    }

    /**
     * JSON path: {@code latency_last_30m.p50} - median latency (milliseconds,
     * time to first token) over the last thirty minutes.
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

    /**
     * JSON path: {@code perf_last_30m_by_workload} - performance over the
     * last thirty minutes per kind of request served, keyed by workload
     * ({@code text_generation}, {@code image_generation}, {@code embeddings},
     * {@code rerank}, {@code stt}, {@code tts}, {@code video_generation},
     * {@code decisions}, {@code unknown}; unknown keys are passed through).
     * Additive to the legacy singular {@link #latencyP50()} /
     * {@link #throughputP50()} fields. Non-object entries are dropped.
     *
     * @return the per-workload entries, empty when absent or not an object
     */
    public Map<String, OpenRouterWorkloadPerf> perfLast30mByWorkload() {
        Map<String, OpenRouterWorkloadPerf> result = new LinkedHashMap<>();
        JSONObject workloads = json.optJSONObject("perf_last_30m_by_workload");
        if (workloads == null) {
            return result;
        }
        for (String key : workloads.keySet()) {
            JSONObject entry = workloads.optJSONObject(key);
            if (entry != null) {
                result.put(key, new OpenRouterWorkloadPerf(entry));
            }
        }
        return result;
    }

    private String optString(String key) {
        Object value = json.opt(key);
        return value instanceof String text ? text : null;
    }

    private Long optLong(String key) {
        Object value = json.opt(key);
        return value instanceof Number number ? number.longValue() : null;
    }

    private Double optDouble(String key) {
        Object value = json.opt(key);
        return value instanceof Number number ? number.doubleValue() : null;
    }

    private Boolean optBoolean(String key) {
        Object value = json.opt(key);
        return value instanceof Boolean flag ? flag : null;
    }

    private Double optPercentile(String objectKey, String percentileKey) {
        JSONObject object = json.optJSONObject(objectKey);
        if (object == null) {
            return null;
        }
        Object value = object.opt(percentileKey);
        return value instanceof Number number ? number.doubleValue() : null;
    }
}
