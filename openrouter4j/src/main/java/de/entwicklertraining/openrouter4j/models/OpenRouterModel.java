package de.entwicklertraining.openrouter4j.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A typed view of one model entry of the OpenRouter model catalog (the
 * {@code data[]} items of GET /models and GET /models/user, or the
 * {@code data} object of GET /model/{author}/{slug}).
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}
 * convention; list accessors return an empty list when the field is absent.
 * Use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterModel {

    private final JSONObject json;

    OpenRouterModel(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw catalog entry behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code id} - unique model identifier (e.g. {@code openai/gpt-4}).
     *
     * @return the value, or {@code null} when absent
     */
    public String id() {
        return json.optString("id", null);
    }

    /**
     * JSON path: {@code name} - display name of the model.
     *
     * @return the value, or {@code null} when absent
     */
    public String name() {
        return json.optString("name", null);
    }

    /**
     * JSON path: {@code canonical_slug} - canonical permaslug of the model.
     *
     * @return the value, or {@code null} when absent
     */
    public String canonicalSlug() {
        return json.optString("canonical_slug", null);
    }

    /**
     * JSON path: {@code created} - Unix timestamp (seconds) of creation.
     *
     * @return the value, or {@code null} when absent
     */
    public Long created() {
        if (!json.has("created") || json.isNull("created")) {
            return null;
        }
        return json.optLong("created");
    }

    /**
     * JSON path: {@code description} - human-readable description.
     *
     * @return the value, or {@code null} when absent
     */
    public String description() {
        return json.optString("description", null);
    }

    /**
     * JSON path: {@code context_length} - maximum context length in tokens.
     *
     * @return the value, or {@code null} when absent
     */
    public Long contextLength() {
        if (!json.has("context_length") || json.isNull("context_length")) {
            return null;
        }
        return json.optLong("context_length");
    }

    /**
     * JSON path: {@code hugging_face_id}.
     *
     * @return the value, or {@code null} when absent
     */
    public String huggingFaceId() {
        return json.optString("hugging_face_id", null);
    }

    /**
     * JSON path: {@code expiration_date} - ISO 8601 date after which the model
     * may be removed, or {@code null} for no expiration.
     *
     * @return the value, or {@code null} when absent
     */
    public String expirationDate() {
        return json.optString("expiration_date", null);
    }

    /**
     * JSON path: {@code knowledge_cutoff} - ISO 8601 date up to which the model
     * was trained, or {@code null} when unknown.
     *
     * @return the value, or {@code null} when absent
     */
    public String knowledgeCutoff() {
        return json.optString("knowledge_cutoff", null);
    }

    /**
     * JSON path: {@code pricing.prompt} - USD per prompt (input) token, as the
     * string the API returns (e.g. {@code "0.00003"}).
     *
     * @return the value, or {@code null} when absent
     */
    public String pricingPrompt() {
        return pricingValue("prompt");
    }

    /**
     * JSON path: {@code pricing.completion} - USD per completion (output) token.
     *
     * @return the value, or {@code null} when absent
     */
    public String pricingCompletion() {
        return pricingValue("completion");
    }

    /**
     * JSON path: {@code pricing.request} - USD per request.
     *
     * @return the value, or {@code null} when absent
     */
    public String pricingRequest() {
        return pricingValue("request");
    }

    /**
     * JSON path: {@code pricing.image} - USD per input image.
     *
     * @return the value, or {@code null} when absent
     */
    public String pricingImage() {
        return pricingValue("image");
    }

    /**
     * JSON path: {@code pricing.audio} - USD per audio input token.
     *
     * @return the value, or {@code null} when absent
     */
    public String pricingAudio() {
        return pricingValue("audio");
    }

    /**
     * JSON path: {@code pricing.input_cache_read} - USD per cached input token (read).
     *
     * @return the value, or {@code null} when absent
     */
    public String pricingInputCacheRead() {
        return pricingValue("input_cache_read");
    }

    /**
     * JSON path: {@code pricing.input_cache_write} - USD per cache-write token
     * (default 5-minute TTL rate).
     *
     * @return the value, or {@code null} when absent
     */
    public String pricingInputCacheWrite() {
        return pricingValue("input_cache_write");
    }

    /**
     * @return the raw {@code pricing} object, or {@code null} when absent
     */
    public JSONObject pricing() {
        return json.optJSONObject("pricing");
    }

    private String pricingValue(String key) {
        JSONObject pricing = json.optJSONObject("pricing");
        if (pricing == null || !pricing.has(key) || pricing.isNull(key)) {
            return null;
        }
        Object value = pricing.opt(key);
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * JSON path: {@code architecture.modality} - primary modality (e.g. {@code text->text}).
     *
     * @return the value, or {@code null} when absent
     */
    public String modality() {
        JSONObject architecture = json.optJSONObject("architecture");
        return architecture != null ? architecture.optString("modality", null) : null;
    }

    /**
     * JSON path: {@code architecture.input_modalities}.
     *
     * @return the values, empty when absent
     */
    public List<String> inputModalities() {
        return stringList(nestedArray("input_modalities"));
    }

    /**
     * JSON path: {@code architecture.output_modalities}.
     *
     * @return the values, empty when absent
     */
    public List<String> outputModalities() {
        return stringList(nestedArray("output_modalities"));
    }

    /**
     * JSON path: {@code architecture.tokenizer} (e.g. {@code GPT}).
     *
     * @return the value, or {@code null} when absent
     */
    public String tokenizer() {
        JSONObject architecture = json.optJSONObject("architecture");
        return architecture != null ? architecture.optString("tokenizer", null) : null;
    }

    /**
     * JSON path: {@code architecture.instruct_type}.
     *
     * @return the value, or {@code null} when absent
     */
    public String instructType() {
        JSONObject architecture = json.optJSONObject("architecture");
        return architecture != null ? architecture.optString("instruct_type", null) : null;
    }

    /**
     * JSON path: {@code supported_parameters} - the parameters this model
     * supports. The data behind the {@code requireParameters(true)} pitfall:
     * when {@code response_format} / structured outputs support is missing
     * here, a schema is silently dropped unless
     * {@code provider.require_parameters} forces a capable endpoint.
     *
     * @return the values, empty when absent
     */
    public List<String> supportedParameters() {
        return stringList(json.optJSONArray("supported_parameters"));
    }

    /**
     * JSON path: {@code top_provider.context_length}.
     *
     * @return the value, or {@code null} when absent
     */
    public Long topProviderContextLength() {
        JSONObject topProvider = json.optJSONObject("top_provider");
        if (topProvider == null || !topProvider.has("context_length") || topProvider.isNull("context_length")) {
            return null;
        }
        return topProvider.optLong("context_length");
    }

    /**
     * JSON path: {@code top_provider.max_completion_tokens}.
     *
     * @return the value, or {@code null} when absent
     */
    public Long topProviderMaxCompletionTokens() {
        JSONObject topProvider = json.optJSONObject("top_provider");
        if (topProvider == null || !topProvider.has("max_completion_tokens") || topProvider.isNull("max_completion_tokens")) {
            return null;
        }
        return topProvider.optLong("max_completion_tokens");
    }

    /**
     * JSON path: {@code top_provider.is_moderated}.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean topProviderIsModerated() {
        JSONObject topProvider = json.optJSONObject("top_provider");
        if (topProvider == null || !topProvider.has("is_moderated") || topProvider.isNull("is_moderated")) {
            return null;
        }
        return topProvider.optBoolean("is_moderated");
    }

    /**
     * JSON path: {@code supported_voices} - voice identifiers of TTS models.
     *
     * @return the values, empty when absent
     */
    public List<String> supportedVoices() {
        return stringList(json.optJSONArray("supported_voices"));
    }

    private JSONArray nestedArray(String key) {
        JSONObject architecture = json.optJSONObject("architecture");
        return architecture != null ? architecture.optJSONArray(key) : null;
    }

    private static List<String> stringList(JSONArray array) {
        List<String> result = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                String value = array.optString(i, null);
                if (value != null) {
                    result.add(value);
                }
            }
        }
        return result;
    }
}
