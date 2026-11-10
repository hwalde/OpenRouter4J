package de.entwicklertraining.openrouter4j.image;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A request to generate an image:
 * POST https://openrouter.ai/api/v1/images
 *
 * <p>The {@code model} and the {@code prompt} are required; everything else is
 * optional and emitted only when explicitly set - an unset option never
 * appears in the JSON. The {@code provider} routing object is emitted only
 * when at least one provider option is set.
 *
 * <p>Trap: provider support varies. {@code n} &gt; 1 is rejected by
 * single-image providers, {@code background: "transparent"} requires an
 * {@code output_format} with alpha support (png or webp), and a provider
 * without a knob (quality, output_compression, ...) silently ignores it.
 * With {@code stream(true)}, partial images are streamed as SSE events - only
 * providers with native streaming support it; others buffer and return the
 * plain response.
 */
public final class OpenRouterImageGenerationRequest
        extends OpenRouterRequest<OpenRouterImageGenerationResponse> {

    private final OpenRouterClient client;
    private final String model;
    private final String prompt;
    private final String aspectRatio;
    private final String background;
    private final String quality;
    private final String resolution;
    private final String size;
    private final Integer n;
    private final String outputFormat;
    private final Integer outputCompression;
    private final Integer frameCount;
    private final Integer frameDurationMs;
    private final JSONObject markup;
    private final Long seed;
    private final String user;
    private final String sessionId;
    private final OpenRouterTraceConfig trace;
    private final boolean stream;
    private final List<JSONObject> inputReferences;
    private final List<String> providerOrder;
    private final List<String> providerOnly;
    private final List<String> providerIgnore;
    private final Boolean allowFallbacks;
    private final JSONObject providerOptions;

    private OpenRouterImageGenerationRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.model = builder.model;
        this.prompt = builder.prompt;
        this.aspectRatio = builder.aspectRatio;
        this.background = builder.background;
        this.quality = builder.quality;
        this.resolution = builder.resolution;
        this.size = builder.size;
        this.n = builder.n;
        this.outputFormat = builder.outputFormat;
        this.outputCompression = builder.outputCompression;
        this.frameCount = builder.frameCount;
        this.frameDurationMs = builder.frameDurationMs;
        this.markup = builder.markup == null ? null : new JSONObject(builder.markup.toString());
        this.seed = builder.seed;
        this.user = builder.user;
        this.sessionId = builder.sessionId;
        this.trace = builder.trace;
        this.stream = builder.streamRequested();
        this.inputReferences = builder.inputReferences == null
                ? null : List.copyOf(builder.inputReferences);
        this.providerOrder = builder.providerOrder == null ? null : List.copyOf(builder.providerOrder);
        this.providerOnly = builder.providerOnly == null ? null : List.copyOf(builder.providerOnly);
        this.providerIgnore = builder.providerIgnore == null ? null : List.copyOf(builder.providerIgnore);
        this.allowFallbacks = builder.allowFallbacks;
        this.providerOptions = builder.providerOptions == null ? null : new JSONObject(builder.providerOptions.toString());
    }

    /** @return the image generation model id (e.g. {@code bytedance-seed/seedream-4.5}) */
    public String model() {
        return model;
    }

    /** @return the text prompt describing the desired image */
    public String prompt() {
        return prompt;
    }

    /**
     * @return whether SSE streaming of partial images was requested on the
     *         body ({@code stream} field)
     */
    public boolean streamRequested() {
        return stream;
    }

    /**
     * The {@code session_id} grouping key, or {@code null} when unset (the
     * key is not sent). Groups related requests (a conversation or agent
     * workflow) for observability grouping in Broadcast and private logging;
     * never sent to the provider.
     *
     * @return the session identifier, or {@code null}
     */
    public String sessionId() {
        return sessionId;
    }

    /**
     * The {@code trace} observability configuration, or {@code null} when
     * unset (the key is not sent). Forwarded to configured broadcast
     * destinations (Langfuse, Datadog, Weave, ...).
     *
     * @return the trace configuration, or {@code null}
     */
    public OpenRouterTraceConfig trace() {
        return trace;
    }

    /**
     * The {@code frame_count} animation frame count, or {@code null} when
     * unset (the key is not sent).
     *
     * @return the animation frame count, or {@code null}
     */
    public Integer frameCount() {
        return frameCount;
    }

    /**
     * The {@code frame_duration_ms} display time per animation frame, or
     * {@code null} when unset (the key is not sent).
     *
     * @return the frame display time in milliseconds, or {@code null}
     */
    public Integer frameDurationMs() {
        return frameDurationMs;
    }

    /**
     * The {@code markup} content part as a detached JSON copy, or
     * {@code null} when unset (the key is not sent).
     *
     * @return a detached copy of the markup part, or {@code null}
     */
    public JSONObject markup() {
        return markup == null ? null : new JSONObject(markup.toString());
    }

    @Override
    public String getRelativeUrl() {
        return "/images";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * JSON path: the request body - {@code model} and {@code prompt}
     * (required), {@code aspect_ratio}, {@code background}, {@code quality},
     * {@code resolution}, {@code size}, {@code n}, {@code output_format},
     * {@code output_compression}, {@code frame_count},
     * {@code frame_duration_ms}, {@code seed}, {@code user},
     * {@code session_id}, {@code stream},
     * {@code input_references}, {@code markup} (all omitted when unset) and
     * the {@code provider} object (omitted unless any provider option is set).
     *
     * @return the JSON body of this request
     */
    @Override
    public String getBody() {
        JSONObject root = new JSONObject();
        root.put("model", model);
        root.put("prompt", prompt);
        if (aspectRatio != null) {
            root.put("aspect_ratio", aspectRatio);
        }
        if (background != null) {
            root.put("background", background);
        }
        if (quality != null) {
            root.put("quality", quality);
        }
        if (resolution != null) {
            root.put("resolution", resolution);
        }
        if (size != null) {
            root.put("size", size);
        }
        if (n != null) {
            root.put("n", n);
        }
        if (outputFormat != null) {
            root.put("output_format", outputFormat);
        }
        if (outputCompression != null) {
            root.put("output_compression", outputCompression);
        }
        if (frameCount != null) {
            root.put("frame_count", frameCount);
        }
        if (frameDurationMs != null) {
            root.put("frame_duration_ms", frameDurationMs);
        }
        if (seed != null) {
            root.put("seed", seed);
        }
        if (user != null) {
            root.put("user", user);
        }
        if (sessionId != null) {
            root.put("session_id", sessionId);
        }
        if (trace != null) {
            root.put("trace", trace.toJson());
        }
        if (stream) {
            root.put("stream", true);
        }
        if (inputReferences != null && !inputReferences.isEmpty()) {
            JSONArray refs = new JSONArray();
            for (JSONObject ref : inputReferences) {
                refs.put(new JSONObject(ref.toString()));
            }
            root.put("input_references", refs);
        }
        if (markup != null) {
            root.put("markup", new JSONObject(markup.toString()));
        }

        // The provider object is emitted whenever any provider option is set,
        // so an unset option never appears in the JSON.
        boolean hasOrder = providerOrder != null && !providerOrder.isEmpty();
        boolean hasOnly = providerOnly != null && !providerOnly.isEmpty();
        boolean hasIgnore = providerIgnore != null && !providerIgnore.isEmpty();
        boolean hasOptions = providerOptions != null && providerOptions.length() > 0;
        if (hasOrder || hasOnly || hasIgnore || hasOptions || allowFallbacks != null) {
            JSONObject providerObj = new JSONObject();
            if (hasOrder) {
                JSONArray orderArr = new JSONArray();
                for (String p : providerOrder) {
                    orderArr.put(p);
                }
                providerObj.put("order", orderArr);
            }
            if (hasOnly) {
                JSONArray onlyArr = new JSONArray();
                for (String p : providerOnly) {
                    onlyArr.put(p);
                }
                providerObj.put("only", onlyArr);
            }
            if (hasIgnore) {
                JSONArray ignoreArr = new JSONArray();
                for (String p : providerIgnore) {
                    ignoreArr.put(p);
                }
                providerObj.put("ignore", ignoreArr);
            }
            if (hasOptions) {
                providerObj.put("options", new JSONObject(providerOptions.toString()));
            }
            if (allowFallbacks != null) {
                providerObj.put("allow_fallbacks", allowFallbacks);
            }
            root.put("provider", providerObj);
        }
        return root.toString();
    }

    @Override
    public OpenRouterImageGenerationResponse createResponse(String responseBody) {
        return new OpenRouterImageGenerationResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterImageGenerationRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterImageGenerationRequest> {

        private static final int MAX_INPUT_REFERENCES = 16;

        private final OpenRouterClient client;
        private String model;
        private String prompt;
        private String aspectRatio;
        private String background;
        private String quality;
        private String resolution;
        private String size;
        private Integer n;
        private String outputFormat;
        private Integer outputCompression;
        private Integer frameCount;
        private Integer frameDurationMs;
        private JSONObject markup;
        private Long seed;
        private String user;
        private String sessionId;
        private OpenRouterTraceConfig trace;
        private boolean streamEnabled;
        private List<JSONObject> inputReferences;
        private List<String> providerOrder;
        private List<String> providerOnly;
        private List<String> providerIgnore;
        private Boolean allowFallbacks;
        private JSONObject providerOptions;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         */
        public Builder(OpenRouterClient client) {
            super(client);
            this.client = client;
        }

        /**
         * Sets the required JSON field {@code model} - the image generation
         * model id (e.g. {@code bytedance-seed/seedream-4.5}). The discovery
         * listing is available via {@code client.images().models()}.
         *
         * @param model the image generation model id
         * @return this builder
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets the required JSON field {@code prompt} - the text description
         * of the desired image.
         *
         * @param prompt the text prompt
         * @return this builder
         */
        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        /**
         * Sets the JSON field {@code aspect_ratio} - the normalized aspect
         * ratio of the generated image (e.g. {@code 16:9}, {@code 1:1},
         * {@code 21:9} or {@code auto}). Providers clamp to their supported
         * subset. Only emitted when set.
         *
         * @param aspectRatio the aspect ratio string
         * @return this builder
         */
        public Builder aspectRatio(String aspectRatio) {
            this.aspectRatio = aspectRatio;
            return this;
        }

        /**
         * Sets the JSON field {@code background} - the background treatment:
         * {@code auto}, {@code transparent} or {@code opaque}. Trap:
         * {@code transparent} requires an {@code output_format} that supports
         * alpha (png or webp).
         *
         * @param background the background treatment
         * @return this builder
         */
        public Builder background(String background) {
            this.background = background;
            return this;
        }

        /**
         * Sets the JSON field {@code quality} - the rendering quality:
         * {@code auto}, {@code low}, {@code medium}, {@code high},
         * {@code xhigh} or {@code max}. Ignored by providers without a
         * quality knob.
         *
         * @param quality the rendering quality
         * @return this builder
         */
        public Builder quality(String quality) {
            this.quality = quality;
            return this;
        }

        /**
         * Sets the JSON field {@code resolution} - the normalized resolution
         * tier: {@code 512}, {@code 1K}, {@code 2K} or {@code 4K}. Concrete
         * pixel dimensions are derived per provider.
         *
         * @param resolution the resolution tier
         * @return this builder
         */
        public Builder resolution(String resolution) {
            this.resolution = resolution;
            return this;
        }

        /**
         * Sets the JSON field {@code size} - a convenience shorthand for the
         * output dimensions: pass a tier ({@code 2K}, {@code 4K}) or explicit
         * pixels ({@code 2048x2048}). A tier size is equivalent to setting
         * {@code resolution} and combines with {@code aspect_ratio}; an
         * explicit pixel size is authoritative and a mismatched
         * {@code resolution} or {@code aspect_ratio} alongside it is rejected
         * with HTTP 400.
         *
         * @param size the tier or explicit pixel size
         * @return this builder
         */
        public Builder size(String size) {
            this.size = size;
            return this;
        }

        /**
         * Sets the JSON field {@code n} - the upper bound on the number of
         * images to generate (1-10). Trap: providers may return fewer images,
         * and providers that only support single-image generation reject
         * {@code n} &gt; 1.
         *
         * @param n the maximum image count
         * @return this builder
         */
        public Builder n(Integer n) {
            if (n != null && (n < 1 || n > 10)) {
                throw new IllegalArgumentException("n must be between 1 and 10, got: " + n);
            }
            this.n = n;
            return this;
        }

        /**
         * Sets the JSON field {@code output_format} - the encoding of the
         * returned image bytes: {@code png}, {@code jpeg}, {@code webp},
         * {@code gif} or {@code svg} (SVG comes from vectorization models and
         * is UTF-8 base64-encoded in {@code b64_json}; {@code gif} produces
         * an animation on models that support it - see
         * {@link #frameCount(Integer)}).
         *
         * @param outputFormat the output format
         * @return this builder
         */
        public Builder outputFormat(String outputFormat) {
            this.outputFormat = outputFormat;
            return this;
        }

        /**
         * Sets the JSON field {@code output_compression} - the compression
         * level (0-100) for webp/jpeg output. Ignored for png and by
         * providers without a compression knob.
         *
         * @param outputCompression the compression level
         * @return this builder
         */
        public Builder outputCompression(Integer outputCompression) {
            this.outputCompression = outputCompression;
            return this;
        }

        /**
         * Sets the JSON field {@code frame_count} - the number of animation
         * frames (2-64, validated loudly against these outer bounds) when
         * {@code output_format} is {@code gif}. Trap: only honored by models
         * that produce animations - each accepts its own sub-range and rejects
         * values outside it server-side - and ignored otherwise (a non-gif
         * request silently produces a still image); the capability is not yet
         * listed in the per-endpoint {@code supported_parameters}, so
         * discovery cannot tell which models honor it. Unset omits the field.
         *
         * @param frameCount the animation frame count (2-64)
         * @return this builder
         * @throws IllegalArgumentException when a non-null value is outside
         *         the spec's outer bounds 2-64
         */
        public Builder frameCount(Integer frameCount) {
            if (frameCount != null && (frameCount < 2 || frameCount > 64)) {
                throw new IllegalArgumentException(
                        "frame_count must be between 2 and 64, got: " + frameCount);
            }
            this.frameCount = frameCount;
            return this;
        }

        /**
         * Sets the JSON field {@code frame_duration_ms} - the display time of
         * each animation frame in milliseconds (20-2000, validated loudly
         * against these outer bounds) when {@code output_format} is
         * {@code gif}. Same gif coupling as {@link #frameCount(Integer)}:
         * honored only by models that produce animations (per-model sub-ranges
         * are enforced server-side), ignored otherwise. Unset omits the field.
         *
         * @param frameDurationMs the frame display time in milliseconds
         * @return this builder
         * @throws IllegalArgumentException when a non-null value is outside
         *         the spec's outer bounds 20-2000
         */
        public Builder frameDurationMs(Integer frameDurationMs) {
            if (frameDurationMs != null && (frameDurationMs < 20 || frameDurationMs > 2000)) {
                throw new IllegalArgumentException(
                        "frame_duration_ms must be between 20 and 2000, got: " + frameDurationMs);
            }
            this.frameDurationMs = frameDurationMs;
            return this;
        }

        /**
         * Sets the JSON field {@code seed} - repeated requests with the same
         * seed and parameters should return the same result. Determinism is
         * not guaranteed for all providers.
         *
         * @param seed the deterministic seed
         * @return this builder
         */
        public Builder seed(Long seed) {
            this.seed = seed;
            return this;
        }

        /**
         * Sets the JSON field {@code user} - a stable identifier for your
         * end-user, used for abuse detection. Never sent to providers
         * verbatim: it is folded into a hashed, per-account upstream user
         * identifier for providers whose data policy requires user ids.
         *
         * @param user the end-user identifier
         * @return this builder
         */
        public Builder user(String user) {
            this.user = user;
            return this;
        }

        /**
         * Sets the JSON field {@code session_id} - a unique identifier for
         * grouping related requests (a conversation or agent workflow). Used
         * for observability grouping in Broadcast and private logging; never
         * sent to the provider. Maximum 256 characters (API-side limit, not
         * validated here). Omitted when unset.
         *
         * @param sessionId the session grouping identifier
         * @return this builder
         */
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        /**
         * Sets the JSON field {@code trace} - observability metadata that
         * OpenRouter forwards to configured broadcast destinations (Langfuse,
         * Datadog, Weave, ...). Build it with
         * {@link OpenRouterTraceConfig#builder()}. Omitted when unset.
         *
         * @param trace the trace configuration
         * @return this builder
         */
        public Builder trace(OpenRouterTraceConfig trace) {
            this.trace = trace;
            return this;
        }

        /**
         * Sets the JSON field {@code stream} - partial images are streamed as
         * SSE events as they become available. Only providers with native
         * streaming support it (currently OpenAI); non-streaming providers
         * ignore the flag and return a buffered response. Consume the events
         * with the inherited {@code stream(StreamingResponseHandler)} - every
         * event JSON arrives as the raw string on
         * {@code StreamingResponseHandler.onData}; the typed response of the
         * final buffered image is the non-streaming shape.
         *
         * @param stream true to request partial-image streaming
         * @return this builder
         */
        public Builder stream(boolean stream) {
            this.streamEnabled = stream;
            return this;
        }

        private boolean streamRequested() {
            return streamEnabled || (streamingInfo != null && streamingInfo.isEnabled());
        }

        /**
         * Adds one image-to-image reference to the JSON field
         * {@code input_references} as a {@code image_url} content part with
         * an HTTP(S) URL. At most 16 references are accepted by the API.
         *
         * @param url the reference image URL
         * @return this builder
         */
        public Builder addInputReferenceByUrl(String url) {
            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("input reference url must not be null or empty");
            }
            if (inputReferences == null) {
                inputReferences = new ArrayList<>();
            }
            if (inputReferences.size() >= MAX_INPUT_REFERENCES) {
                throw new IllegalStateException(
                        "at most " + MAX_INPUT_REFERENCES + " input references are allowed");
            }
            JSONObject part = new JSONObject();
            part.put("type", "image_url");
            part.put("image_url", new JSONObject().put("url", url));
            inputReferences.add(part);
            return this;
        }

        /**
         * Adds one image-to-image reference to the JSON field
         * {@code input_references} as a {@code image_url} content part with a
         * base64 data URL (e.g. {@code data:image/png;base64,....}). At most
         * 16 references are accepted by the API.
         *
         * @param dataUrl the base64 data URL of the reference image
         * @return this builder
         */
        public Builder addInputReferenceByBase64(String dataUrl) {
            if (dataUrl == null || dataUrl.isEmpty()) {
                throw new IllegalArgumentException("input reference data URL must not be null or empty");
            }
            return addInputReferenceByUrl(dataUrl);
        }

        /**
         * Sets the JSON field {@code markup} - a transparent PNG overlay
         * (arrows, circles, annotations) with the same dimensions as the
         * first reference image, as a base64 data URL or HTTP(S) URL, guiding
         * where an edit applies. Builds the {@code image_url} content part
         * {@code {"type": "image_url", "image_url": {"url": ...}}} from the
         * URL. Traps: image-to-image requests only (pair it with
         * {@link #addInputReferenceByUrl(String)} /
         * {@link #addInputReferenceByBase64(String)}), honored only by models
         * with markup-guided editing (the OpenAI {@code gpt-image-2.6}
         * family, e.g. {@code openai/gpt-image-2.6-sunburst}) and ignored
         * otherwise - the capability is not yet listed in the per-endpoint
         * {@code supported_parameters}, so discovery cannot tell which models
         * honor it. {@code null} unsets the field and omits it. The
         * verbatim escape hatch for content-part shapes beyond the URL
         * convenience is {@link #markupRaw(JSONObject)} - deliberately a
         * distinct name, so a bare {@code markup(null)} stays unambiguous at
         * compile time (the {@code addInput}/{@code addInputItem} precedent).
         *
         * @param url the markup overlay URL (base64 data URL or HTTP(S))
         * @return this builder
         * @throws IllegalArgumentException when a non-null value is empty
         */
        public Builder markup(String url) {
            if (url == null) {
                this.markup = null;
                return this;
            }
            if (url.isEmpty()) {
                throw new IllegalArgumentException("markup url must not be empty");
            }
            JSONObject part = new JSONObject();
            part.put("type", "image_url");
            part.put("image_url", new JSONObject().put("url", url));
            this.markup = part;
            return this;
        }

        /**
         * Sets the JSON field {@code markup} verbatim from a raw JSON object
         * (a {@code ContentPartImage} shape) - the escape hatch for part
         * shapes beyond the URL convenience of {@link #markup(String)}. Same
         * traps as {@link #markup(String)}: image-to-image only (pair it with
         * {@link #addInputReferenceByUrl(String)}), honored only by models
         * with markup-guided editing (the OpenAI {@code gpt-image-2.6}
         * family, e.g. {@code openai/gpt-image-2.6-sunburst}) - silently
         * ignored otherwise, and not discoverable via
         * {@code supported_parameters}. The object is stored as a detached
         * copy, so later mutations of the caller's instance cannot change the
         * request. {@code null} unsets the field and omits it.
         *
         * @param markup the raw markup content part
         * @return this builder
         */
        public Builder markupRaw(JSONObject markup) {
            this.markup = markup == null ? null : new JSONObject(markup.toString());
            return this;
        }

        /**
         * Sets the JSON field {@code provider.order} - the preferred serving
         * providers in priority order. Only emitted when at least one
         * provider option is set.
         *
         * @param providers the provider slugs in priority order
         * @return this builder
         */
        public Builder providerOrder(String... providers) {
            this.providerOrder = toNonEmptyList(providers);
            return this;
        }

        /**
         * Sets the JSON field {@code provider.only} - the only providers
         * allowed to serve the request. Only emitted when at least one
         * provider option is set.
         *
         * @param providers the provider slugs
         * @return this builder
         */
        public Builder providerOnly(String... providers) {
            this.providerOnly = toNonEmptyList(providers);
            return this;
        }

        /**
         * Sets the JSON field {@code provider.ignore} - providers excluded
         * from serving the request. Only emitted when at least one provider
         * option is set.
         *
         * @param providers the provider slugs to ignore
         * @return this builder
         */
        public Builder providerIgnore(String... providers) {
            this.providerIgnore = toNonEmptyList(providers);
            return this;
        }

        /**
         * Sets the JSON field {@code provider.allow_fallbacks} - when
         * {@code false}, the request fails with the upstream error instead of
         * falling back to providers outside the configured preferences.
         *
         * @param allowFallbacks the fallback flag
         * @return this builder
         */
        public Builder allowFallbacks(Boolean allowFallbacks) {
            this.allowFallbacks = allowFallbacks;
            return this;
        }

        /**
         * Adds a provider-specific passthrough option to the JSON field
         * {@code provider.options[providerSlug]} - e.g.
         * {@code providerOption("black-forest-labs", new JSONObject().put("steps", 40))}.
         * Only the options of the provider that serves the request are
         * forwarded; unrecognized keys are silently dropped upstream.
         *
         * @param providerSlug the provider slug key
         * @param options the provider-specific options object
         * @return this builder
         */
        public Builder providerOption(String providerSlug, JSONObject options) {
            if (providerSlug == null || providerSlug.isEmpty()) {
                throw new IllegalArgumentException("providerSlug must not be null or empty");
            }
            if (options == null) {
                throw new IllegalArgumentException("options must not be null");
            }
            if (providerOptions == null) {
                providerOptions = new JSONObject();
            }
            providerOptions.put(providerSlug, new JSONObject(options.toString()));
            return this;
        }

        private List<String> toNonEmptyList(String... values) {
            List<String> list = new ArrayList<>();
            for (String v : values) {
                if (v != null && !v.isEmpty()) {
                    list.add(v);
                }
            }
            return list;
        }

        @Override
        public OpenRouterImageGenerationRequest build() {
            if (model == null || model.isEmpty()) {
                throw new IllegalStateException("model is required for an image generation request");
            }
            if (prompt == null || prompt.isEmpty()) {
                throw new IllegalStateException("prompt is required for an image generation request");
            }
            return new OpenRouterImageGenerationRequest(this);
        }

        @Override
        public OpenRouterImageGenerationResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterImageGenerationResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterImageGenerationResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
