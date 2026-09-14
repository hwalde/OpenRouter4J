package de.entwicklertraining.openrouter4j.generation;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /generation: the request/usage metadata of one generation.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}
 * convention; the numbers are exposed as boxed types because OpenRouter
 * returns {@code null} for many of them (e.g. metadata of a request that never
 * reached a provider). Use {@link #data()} for the raw object.
 */
public final class OpenRouterGenerationResponse extends OpenRouterResponse<OpenRouterGenerationRequest> {

    OpenRouterGenerationResponse(JSONObject json, OpenRouterGenerationRequest request) {
        super(json, request);
    }

    /**
     * @return the raw {@code data} object of the response, or {@code null} when absent
     */
    public JSONObject data() {
        try {
            return json.optJSONObject("data");
        } catch (Exception e) {
            return null;
        }
    }

    private Long longOf(String key) {
        JSONObject data = data();
        if (data == null || !data.has(key) || data.isNull(key)) {
            return null;
        }
        return data.optLong(key);
    }

    private Double doubleOf(String key) {
        JSONObject data = data();
        if (data == null || !data.has(key) || data.isNull(key)) {
            return null;
        }
        return data.optDouble(key);
    }

    private Boolean boolOf(String key) {
        JSONObject data = data();
        if (data == null || !data.has(key) || data.isNull(key)) {
            return null;
        }
        return data.optBoolean(key);
    }

    private String stringOf(String key) {
        JSONObject data = data();
        if (data == null || data.isNull(key)) {
            return null;
        }
        return data.optString(key, null);
    }

    /**
     * JSON path: {@code data.id} - the generation id ({@code gen-...}).
     *
     * @return the value, or {@code null} when absent
     */
    public String id() {
        return stringOf("id");
    }

    /**
     * JSON path: {@code data.model} - the model that was requested.
     *
     * @return the value, or {@code null} when absent
     */
    public String model() {
        return stringOf("model");
    }

    /**
     * JSON path: {@code data.provider_name} - which provider served the request.
     *
     * @return the value, or {@code null} when absent
     */
    public String providerName() {
        return stringOf("provider_name");
    }

    /**
     * JSON path: {@code data.router} - the router that selected the endpoint
     * (e.g. {@code openrouter/auto} or the requested model slug).
     *
     * @return the value, or {@code null} when absent
     */
    public String router() {
        return stringOf("router");
    }

    /**
     * JSON path: {@code data.api_type} - the API that served the generation
     * ({@code completions}, {@code embeddings}, {@code rerank}, ...).
     *
     * @return the value, or {@code null} when absent
     */
    public String apiType() {
        return stringOf("api_type");
    }

    /**
     * JSON path: {@code data.finish_reason} - the normalised finish reason.
     *
     * @return the value, or {@code null} when absent
     */
    public String finishReason() {
        return stringOf("finish_reason");
    }

    /**
     * JSON path: {@code data.native_finish_reason} - the provider-native
     * finish reason next to the normalised one.
     *
     * @return the value, or {@code null} when absent
     */
    public String nativeFinishReason() {
        return stringOf("native_finish_reason");
    }

    /**
     * JSON path: {@code data.tokens_prompt} - prompt tokens billed by OpenRouter.
     *
     * @return the value, or {@code null} when absent
     */
    public Long tokensPrompt() {
        return longOf("tokens_prompt");
    }

    /**
     * JSON path: {@code data.tokens_completion} - completion tokens billed by OpenRouter.
     *
     * @return the value, or {@code null} when absent
     */
    public Long tokensCompletion() {
        return longOf("tokens_completion");
    }

    /**
     * JSON path: {@code data.native_tokens_prompt} - prompt tokens as reported by the provider.
     *
     * @return the value, or {@code null} when absent
     */
    public Long nativeTokensPrompt() {
        return longOf("native_tokens_prompt");
    }

    /**
     * JSON path: {@code data.native_tokens_completion} - completion tokens as reported by the provider.
     *
     * @return the value, or {@code null} when absent
     */
    public Long nativeTokensCompletion() {
        return longOf("native_tokens_completion");
    }

    /**
     * JSON path: {@code data.native_tokens_reasoning} - reasoning tokens as reported by the provider.
     *
     * @return the value, or {@code null} when absent
     */
    public Long nativeTokensReasoning() {
        return longOf("native_tokens_reasoning");
    }

    /**
     * JSON path: {@code data.native_tokens_cached} - cached tokens as reported by the provider.
     *
     * @return the value, or {@code null} when absent
     */
    public Long nativeTokensCached() {
        return longOf("native_tokens_cached");
    }

    /**
     * JSON path: {@code data.total_cost} - total cost of the generation, USD
     * (the amount deducted from credits).
     *
     * @return the value, or {@code null} when absent
     */
    public Double totalCost() {
        return doubleOf("total_cost");
    }

    /**
     * JSON path: {@code data.usage} - usage cost, USD.
     *
     * @return the value, or {@code null} when absent
     */
    public Double usage() {
        return doubleOf("usage");
    }

    /**
     * JSON path: {@code data.upstream_inference_cost} - the provider-side inference cost, USD.
     *
     * @return the value, or {@code null} when absent
     */
    public Double upstreamInferenceCost() {
        return doubleOf("upstream_inference_cost");
    }

    /**
     * JSON path: {@code data.cache_discount} - discount applied due to caching, USD.
     *
     * @return the value, or {@code null} when absent
     */
    public Double cacheDiscount() {
        return doubleOf("cache_discount");
    }

    /**
     * JSON path: {@code data.latency} - total latency, milliseconds.
     *
     * @return the value, or {@code null} when absent
     */
    public Long latency() {
        return longOf("latency");
    }

    /**
     * JSON path: {@code data.generation_time} - time spent generating, milliseconds.
     *
     * @return the value, or {@code null} when absent
     */
    public Long generationTime() {
        return longOf("generation_time");
    }

    /**
     * JSON path: {@code data.moderation_latency} - moderation overhead, milliseconds.
     *
     * @return the value, or {@code null} when absent
     */
    public Long moderationLatency() {
        return longOf("moderation_latency");
    }

    /**
     * JSON path: {@code data.streamed} - whether the generation was streamed.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean streamed() {
        return boolOf("streamed");
    }

    /**
     * JSON path: {@code data.cancelled} - whether the generation was cancelled.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean cancelled() {
        return boolOf("cancelled");
    }

    /**
     * JSON path: {@code data.is_byok} - whether the request used bring-your-own-key.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean isByok() {
        return boolOf("is_byok");
    }

    /**
     * JSON path: {@code data.created_at} - ISO 8601 timestamp of the generation.
     *
     * @return the value, or {@code null} when absent
     */
    public String createdAt() {
        return stringOf("created_at");
    }

    /**
     * JSON path: {@code data.data_region} - the data region serving the request
     * ({@code global}, {@code europe}, {@code us}).
     *
     * @return the value, or {@code null} when absent
     */
    public String dataRegion() {
        return stringOf("data_region");
    }

    /**
     * JSON path: {@code data.service_tier} - the capacity tier that served the request.
     *
     * @return the value, or {@code null} when absent
     */
    public String serviceTier() {
        return stringOf("service_tier");
    }

    /**
     * JSON path: {@code data.upstream_id} - the provider-side id of the generation
     * (e.g. the {@code chatcmpl-...} of an OpenAI-compatible provider).
     *
     * @return the value, or {@code null} when absent
     */
    public String upstreamId() {
        return stringOf("upstream_id");
    }

    /**
     * JSON path: {@code data.request_id} - the request id ({@code req-...}).
     *
     * @return the value, or {@code null} when absent
     */
    public String requestId() {
        return stringOf("request_id");
    }

    /**
     * JSON path: {@code data.session_id} - the session id sent with the request, when any.
     *
     * @return the value, or {@code null} when absent
     */
    public String sessionId() {
        return stringOf("session_id");
    }

    /**
     * JSON path: {@code data.preset_id} - the preset applied to the request, when any.
     *
     * @return the value, or {@code null} when absent
     */
    public String presetId() {
        return stringOf("preset_id");
    }

    /**
     * JSON path: {@code data.app_id} - id of the app that made the request.
     *
     * @return the value, or {@code null} when absent
     */
    public Long appId() {
        return longOf("app_id");
    }

    /**
     * JSON path: {@code data.external_user} - the {@code user} identifier sent with the request.
     *
     * @return the value, or {@code null} when absent
     */
    public String externalUser() {
        return stringOf("external_user");
    }

    /**
     * JSON path: {@code data.web_search_engine} - the web-search engine used, when any.
     *
     * @return the value, or {@code null} when absent
     */
    public String webSearchEngine() {
        return stringOf("web_search_engine");
    }

    /**
     * JSON path: {@code data.num_search_results} - number of web-search results used.
     *
     * @return the value, or {@code null} when absent
     */
    public Long numSearchResults() {
        return longOf("num_search_results");
    }
}
