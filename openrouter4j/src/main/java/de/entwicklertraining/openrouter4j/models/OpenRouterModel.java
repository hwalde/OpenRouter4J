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

    /**
     * JSON path: {@code alias_target} - the concrete model a
     * tilde-latest alias (e.g. {@code ~anthropic/claude-sonnet-latest})
     * resolves to.
     *
     * @return the raw {@code alias_target} object, or {@code null} when the
     *         model is not an alias
     */
    public JSONObject aliasTarget() {
        return json.optJSONObject("alias_target");
    }

    /**
     * JSON path: {@code alias_target.slug} - the routable model id of the
     * concrete alias target, matching that model row's {@code id}.
     *
     * @return the value, or {@code null} when the model is not an alias
     */
    public String aliasTargetSlug() {
        return nestedString("alias_target", "slug");
    }

    /**
     * JSON path: {@code alias_target.name} - the human-readable name of the
     * concrete alias target.
     *
     * @return the value, or {@code null} when the model is not an alias
     */
    public String aliasTargetName() {
        return nestedString("alias_target", "name");
    }

    /**
     * JSON path: {@code links} - related API endpoints and resources of the
     * model. The schema currently documents a single {@code details} field
     * (see {@link #detailsLink()}); the raw object is the escape hatch for
     * fields OpenRouter adds later.
     *
     * @return the raw {@code links} object, or {@code null} when absent
     */
    public JSONObject links() {
        return json.optJSONObject("links");
    }

    /**
     * JSON path: {@code links.details} - URL for the model
     * details/endpoints API (a relative {@code /api/v1/...} path).
     *
     * @return the value, or {@code null} when absent
     */
    public String detailsLink() {
        return nestedString("links", "details");
    }

    /**
     * JSON path: {@code default_parameters} - the default parameter values of
     * the model (e.g. {@code temperature}, {@code top_p}, {@code top_k},
     * {@code repetition_penalty}). Deliberately a raw {@link JSONObject}
     * passthrough: it is a loose bag of numeric defaults whose useful form is
     * a per-key read, not a typed view of fixed fields.
     *
     * @return the raw {@code default_parameters} object, or {@code null} when absent
     */
    public JSONObject defaultParameters() {
        return json.optJSONObject("default_parameters");
    }

    /**
     * JSON path: {@code per_request_limits} - the per-request rate limits of
     * the model for BYOK keys.
     *
     * @return the raw {@code per_request_limits} object, or {@code null} when absent
     */
    public JSONObject perRequestLimits() {
        return json.optJSONObject("per_request_limits");
    }

    /**
     * JSON path: {@code per_request_limits.prompt_tokens} - the maximum
     * prompt tokens per request.
     *
     * @return the value, or {@code null} when absent
     */
    public Double perRequestLimitPromptTokens() {
        return nestedDouble("per_request_limits", "prompt_tokens");
    }

    /**
     * JSON path: {@code per_request_limits.completion_tokens} - the maximum
     * completion tokens per request.
     *
     * @return the value, or {@code null} when absent
     */
    public Double perRequestLimitCompletionTokens() {
        return nestedDouble("per_request_limits", "completion_tokens");
    }

    /**
     * JSON path: {@code reasoning} - the reasoning capability flags of the
     * model. Omitted by the API for non-reasoning and dynamic router models.
     *
     * @return the typed view, or {@code null} when absent
     */
    public Reasoning reasoning() {
        JSONObject reasoning = json.optJSONObject("reasoning");
        return reasoning != null ? new Reasoning(reasoning) : null;
    }

    /**
     * JSON path: {@code benchmarks} - third-party benchmark rankings of the
     * model (Artificial Analysis indices, Design Arena ELO rows). Omitted by
     * the API when no benchmark data is available.
     *
     * @return the typed view, or {@code null} when absent
     */
    public Benchmarks benchmarks() {
        JSONObject benchmarks = json.optJSONObject("benchmarks");
        return benchmarks != null ? new Benchmarks(benchmarks) : null;
    }

    private String nestedString(String object, String key) {
        JSONObject nested = json.optJSONObject(object);
        return nested != null ? nested.optString(key, null) : null;
    }

    private Double nestedDouble(String object, String key) {
        JSONObject nested = json.optJSONObject(object);
        if (nested == null || !nested.has(key) || nested.isNull(key)) {
            return null;
        }
        Object value = nested.opt(key);
        return value instanceof Number number ? number.doubleValue() : null;
    }

    /**
     * Typed view of the {@code reasoning} object of a catalog entry: the
     * reasoning-effort configuration of the model.
     */
    public static final class Reasoning {

        private final JSONObject json;

        private Reasoning(JSONObject json) {
            this.json = json;
        }

        /**
         * @return the raw {@code reasoning} object
         */
        public JSONObject json() {
            return json;
        }

        /**
         * JSON path: {@code reasoning.default_effort} - the effort applied
         * when the client enables reasoning without specifying one; maps to
         * {@code reasoning.effort} in chat requests (e.g. {@code medium}).
         * When {@code none}, prefer omitting effort unless explicitly
         * disabling reasoning.
         *
         * @return the value, or {@code null} when absent
         */
        public String defaultEffort() {
            return json.optString("default_effort", null);
        }

        /**
         * JSON path: {@code reasoning.default_enabled}.
         *
         * @return the value, or {@code null} when absent
         */
        public Boolean defaultEnabled() {
            return optBoolean("default_enabled");
        }

        /**
         * JSON path: {@code reasoning.mandatory} - when {@code true},
         * reasoning cannot be disabled and effort {@code none} is rejected.
         *
         * @return the value, or {@code null} when absent
         */
        public Boolean mandatory() {
            return optBoolean("mandatory");
        }

        /**
         * JSON path: {@code reasoning.supported_efforts} - the allowed
         * effort values in descending effort order. {@code null} in the API
         * means no allowlist (all effort values are accepted) and yields an
         * empty list here.
         *
         * @return the values, empty when absent
         */
        public List<String> supportedEfforts() {
            return stringList(json.optJSONArray("supported_efforts"));
        }

        /**
         * JSON path: {@code reasoning.supports_max_tokens} - present and
         * {@code true} when the model accepts {@code reasoning.max_tokens}
         * (Anthropic-style) instead of or in addition to
         * {@code reasoning.effort}.
         *
         * @return the value, or {@code null} when the field is absent
         */
        public Boolean supportsMaxTokens() {
            return optBoolean("supports_max_tokens");
        }

        private Boolean optBoolean(String key) {
            if (!json.has(key) || json.isNull(key)) {
                return null;
            }
            return json.optBoolean(key);
        }
    }

    /**
     * Typed view of the {@code benchmarks} object of a catalog entry:
     * third-party benchmark rankings for the model.
     */
    public static final class Benchmarks {

        private final JSONObject json;

        private Benchmarks(JSONObject json) {
            this.json = json;
        }

        /**
         * @return the raw {@code benchmarks} object
         */
        public JSONObject json() {
            return json;
        }

        /**
         * JSON path: {@code benchmarks.artificial_analysis.intelligence_index}.
         *
         * @return the value, or {@code null} when absent
         */
        public Double intelligenceIndex() {
            return aaIndex("intelligence_index");
        }

        /**
         * JSON path: {@code benchmarks.artificial_analysis.coding_index}.
         *
         * @return the value, or {@code null} when absent
         */
        public Double codingIndex() {
            return aaIndex("coding_index");
        }

        /**
         * JSON path: {@code benchmarks.artificial_analysis.agentic_index}.
         *
         * @return the value, or {@code null} when absent
         */
        public Double agenticIndex() {
            return aaIndex("agentic_index");
        }

        /**
         * JSON path: {@code benchmarks.design_arena[]} - the Design Arena
         * ELO rows across arena+category pairs.
         *
         * @return the entries, empty when absent
         */
        public List<DesignArenaEntry> designArenaEntries() {
            List<DesignArenaEntry> result = new ArrayList<>();
            JSONArray array = json.optJSONArray("design_arena");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject entry = array.optJSONObject(i);
                    if (entry != null) {
                        result.add(new DesignArenaEntry(entry));
                    }
                }
            }
            return result;
        }

        private Double aaIndex(String key) {
            JSONObject aa = json.optJSONObject("artificial_analysis");
            if (aa == null || !aa.has(key) || aa.isNull(key)) {
                return null;
            }
            Object value = aa.opt(key);
            return value instanceof Number number ? number.doubleValue() : null;
        }
    }

    /**
     * Typed view of one Design Arena benchmark entry (one arena+category
     * pair) inside {@code benchmarks.design_arena[]}.
     */
    public static final class DesignArenaEntry {

        private final JSONObject json;

        private DesignArenaEntry(JSONObject json) {
            this.json = json;
        }

        /**
         * @return the raw entry object
         */
        public JSONObject json() {
            return json;
        }

        /**
         * JSON path: {@code arena} - the arena type (e.g. {@code models},
         * {@code builders}, {@code agents}).
         *
         * @return the value, or {@code null} when absent
         */
        public String arena() {
            return json.optString("arena", null);
        }

        /**
         * JSON path: {@code category} - the category within the arena
         * (e.g. {@code website}, {@code gamedev}, {@code uicomponent}).
         *
         * @return the value, or {@code null} when absent
         */
        public String category() {
            return json.optString("category", null);
        }

        /**
         * JSON path: {@code elo} - the ELO rating from head-to-head arena
         * battles.
         *
         * @return the value, or {@code null} when absent
         */
        public Double elo() {
            return optDouble("elo");
        }

        /**
         * JSON path: {@code rank} - the rank within this arena+category
         * among models on OpenRouter (1 = highest ELO).
         *
         * @return the value, or {@code null} when absent
         */
        public Long rank() {
            if (!json.has("rank") || json.isNull("rank")) {
                return null;
            }
            Object value = json.opt("rank");
            return value instanceof Number number ? number.longValue() : null;
        }

        /**
         * JSON path: {@code win_rate} - the win-rate percentage in arena
         * battles.
         *
         * @return the value, or {@code null} when absent
         */
        public Double winRate() {
            return optDouble("win_rate");
        }

        private Double optDouble(String key) {
            if (!json.has(key) || json.isNull(key)) {
                return null;
            }
            Object value = json.opt(key);
            return value instanceof Number number ? number.doubleValue() : null;
        }
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
