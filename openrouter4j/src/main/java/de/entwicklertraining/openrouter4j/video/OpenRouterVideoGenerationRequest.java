package de.entwicklertraining.openrouter4j.video;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A request to submit an async video generation job:
 * POST https://openrouter.ai/api/v1/videos
 *
 * <p>The {@code model} is required; the {@code prompt} is optional for models
 * that can generate a video from image input alone and required by all other
 * models. The submission returns {@code 202} with the job id and a polling
 * URL - the job is then tracked with {@code client.videos().job(jobId)}
 * (or {@link OpenRouterVideoGenerationResponse#awaitCompletion(OpenRouterClient)} on the
 * response).
 *
 * <p>Trap: {@code creativity} and {@code upscale_factor} are only supported
 * by video upscaling models, not by video generation models.
 */
public final class OpenRouterVideoGenerationRequest
        extends OpenRouterRequest<OpenRouterVideoGenerationResponse<OpenRouterVideoGenerationRequest>> {

    private final String model;
    private final String prompt;
    private final String aspectRatio;
    private final String resolution;
    private final String size;
    private final Integer duration;
    private final Boolean generateAudio;
    private final Long seed;
    private final String callbackUrl;
    private final Integer creativity;
    private final Double upscaleFactor;
    private final List<JSONObject> frameImages;
    private final List<JSONObject> inputReferences;
    private final String user;
    private final OpenRouterTraceConfig trace;
    private final JSONObject providerOptions;

    private OpenRouterVideoGenerationRequest(Builder builder) {
        super(builder);
        this.model = builder.model;
        this.prompt = builder.prompt;
        this.aspectRatio = builder.aspectRatio;
        this.resolution = builder.resolution;
        this.size = builder.size;
        this.duration = builder.duration;
        this.generateAudio = builder.generateAudio;
        this.seed = builder.seed;
        this.callbackUrl = builder.callbackUrl;
        this.creativity = builder.creativity;
        this.upscaleFactor = builder.upscaleFactor;
        this.frameImages = builder.frameImages == null ? null : List.copyOf(builder.frameImages);
        this.inputReferences = builder.inputReferences == null ? null : List.copyOf(builder.inputReferences);
        this.user = builder.user;
        this.trace = builder.trace;
        this.providerOptions = builder.providerOptions == null ? null : new JSONObject(builder.providerOptions.toString());
    }

    /** @return the video generation model id (e.g. {@code google/veo-3.1}) */
    public String model() {
        return model;
    }

    /** @return the text prompt describing the video, or {@code null} when unset */
    public String prompt() {
        return prompt;
    }

    /**
     * The {@code user} end-user identifier, or {@code null} when unset (the
     * key is not sent). Used by OpenRouter for abuse monitoring and cost
     * isolation.
     *
     * @return the end-user identifier, or {@code null}
     */
    public String user() {
        return user;
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

    @Override
    public String getRelativeUrl() {
        return "/videos";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * JSON path: the request body - {@code model} (required), {@code prompt},
     * {@code aspect_ratio}, {@code resolution}, {@code size}, {@code duration},
     * {@code generate_audio}, {@code seed}, {@code callback_url},
     * {@code creativity}, {@code upscale_factor}, {@code frame_images},
     * {@code input_references} (all omitted when unset) and the
     * {@code provider.options} object (omitted unless a passthrough option is
     * set).
     *
     * @return the JSON body of this request
     */
    @Override
    public String getBody() {
        JSONObject root = new JSONObject();
        root.put("model", model);
        if (prompt != null) {
            root.put("prompt", prompt);
        }
        if (aspectRatio != null) {
            root.put("aspect_ratio", aspectRatio);
        }
        if (resolution != null) {
            root.put("resolution", resolution);
        }
        if (size != null) {
            root.put("size", size);
        }
        if (duration != null) {
            root.put("duration", duration);
        }
        if (generateAudio != null) {
            root.put("generate_audio", generateAudio);
        }
        if (seed != null) {
            root.put("seed", seed);
        }
        if (callbackUrl != null) {
            root.put("callback_url", callbackUrl);
        }
        if (creativity != null) {
            root.put("creativity", creativity);
        }
        if (upscaleFactor != null) {
            root.put("upscale_factor", upscaleFactor);
        }
        if (frameImages != null && !frameImages.isEmpty()) {
            JSONArray frames = new JSONArray();
            for (JSONObject frame : frameImages) {
                frames.put(new JSONObject(frame.toString()));
            }
            root.put("frame_images", frames);
        }
        if (inputReferences != null && !inputReferences.isEmpty()) {
            JSONArray refs = new JSONArray();
            for (JSONObject ref : inputReferences) {
                refs.put(new JSONObject(ref.toString()));
            }
            root.put("input_references", refs);
        }
        if (user != null) {
            root.put("user", user);
        }
        if (trace != null) {
            root.put("trace", trace.toJson());
        }
        if (providerOptions != null && providerOptions.length() > 0) {
            root.put("provider", new JSONObject().put("options",
                    new JSONObject(providerOptions.toString())));
        }
        return root.toString();
    }

    @Override
    public OpenRouterVideoGenerationResponse<OpenRouterVideoGenerationRequest> createResponse(String responseBody) {
        return new OpenRouterVideoGenerationResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterVideoGenerationRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterVideoGenerationRequest> {

        private final OpenRouterClient client;
        private String model;
        private String prompt;
        private String aspectRatio;
        private String resolution;
        private String size;
        private Integer duration;
        private Boolean generateAudio;
        private Long seed;
        private String callbackUrl;
        private Integer creativity;
        private Double upscaleFactor;
        private List<JSONObject> frameImages;
        private List<JSONObject> inputReferences;
        private String user;
        private OpenRouterTraceConfig trace;
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
         * Sets the required JSON field {@code model} - the video generation
         * model id (e.g. {@code google/veo-3.1}). The discovery listing is
         * available via {@code client.videos().models()}.
         *
         * @param model the video generation model id
         * @return this builder
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets the JSON field {@code prompt} - the text prompt describing the
         * video to generate. Optional for models that support generating a
         * video from image input alone; required by all other models.
         *
         * @param prompt the text prompt
         * @return this builder
         */
        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        /**
         * Sets the JSON field {@code aspect_ratio} - the aspect ratio of the
         * generated video (e.g. {@code 16:9}, {@code 9:16}, {@code 1:1}).
         * Interchangeable with {@code size}.
         *
         * @param aspectRatio the aspect ratio string
         * @return this builder
         */
        public Builder aspectRatio(String aspectRatio) {
            this.aspectRatio = aspectRatio;
            return this;
        }

        /**
         * Sets the JSON field {@code resolution} - the resolution of the
         * generated video: {@code 480p}, {@code 720p}, {@code 768p},
         * {@code 1080p}, {@code 1K}, {@code 2K} or {@code 4K}.
         * Interchangeable with {@code size}.
         *
         * @param resolution the resolution tier
         * @return this builder
         */
        public Builder resolution(String resolution) {
            this.resolution = resolution;
            return this;
        }

        /**
         * Sets the JSON field {@code size} - the exact pixel dimensions of
         * the generated video in {@code WIDTHxHEIGHT} format (e.g.
         * {@code 1280x720}). Interchangeable with
         * {@code resolution} + {@code aspect_ratio}.
         *
         * @param size the explicit pixel size
         * @return this builder
         */
        public Builder size(String size) {
            this.size = size;
            return this;
        }

        /**
         * Sets the JSON field {@code duration} - the duration of the
         * generated video in seconds (API minimum 1). Providers support only
         * a subset; the supported durations of a model are in the
         * {@code /videos/models} listing.
         *
         * @param duration the duration in seconds
         * @return this builder
         */
        public Builder duration(Integer duration) {
            if (duration != null && duration < 1) {
                throw new IllegalArgumentException("duration must be at least 1 second, got: " + duration);
            }
            this.duration = duration;
            return this;
        }

        /**
         * Sets the JSON field {@code generate_audio} - whether to generate
         * audio alongside the video. Defaults to the endpoint's
         * {@code generate_audio} capability flag, {@code false} if not set.
         *
         * @param generateAudio the audio generation flag
         * @return this builder
         */
        public Builder generateAudio(Boolean generateAudio) {
            this.generateAudio = generateAudio;
            return this;
        }

        /**
         * Sets the JSON field {@code seed} - repeated requests with the same
         * seed and parameters should return the same result. Determinism is
         * not guaranteed for all providers; the {@code /videos/models}
         * listing reports whether a model supports seeding.
         *
         * @param seed the deterministic seed
         * @return this builder
         */
        public Builder seed(Long seed) {
            this.seed = seed;
            return this;
        }

        /**
         * Sets the JSON field {@code callback_url} - the HTTPS URL that
         * receives a webhook notification when the job completes. Overrides
         * the workspace-level default callback URL.
         *
         * @param callbackUrl the HTTPS webhook URL
         * @return this builder
         */
        public Builder callbackUrl(String callbackUrl) {
            if (callbackUrl != null && !callbackUrl.startsWith("https://")) {
                throw new IllegalArgumentException("callback_url must be HTTPS, got: " + callbackUrl);
            }
            this.callbackUrl = callbackUrl;
            return this;
        }

        /**
         * Sets the JSON field {@code creativity} - the creativity level.
         * Trap: only supported by video upscaling models, not by video
         * generation models.
         *
         * @param creativity the creativity level
         * @return this builder
         */
        public Builder creativity(Integer creativity) {
            this.creativity = creativity;
            return this;
        }

        /**
         * Sets the JSON field {@code upscale_factor} - the upscale factor.
         * Trap: only supported by video upscaling models, not by video
         * generation models.
         *
         * @param upscaleFactor the upscale factor
         * @return this builder
         */
        public Builder upscaleFactor(Double upscaleFactor) {
            this.upscaleFactor = upscaleFactor;
            return this;
        }

        /**
         * Adds the JSON field {@code frame_images} entry {@code first_frame}
         * - the image used as the first frame of the generated video.
         *
         * @param url the image URL
         * @return this builder
         */
        public Builder addFirstFrameByUrl(String url) {
            return addFrameImage("first_frame", url);
        }

        /**
         * Adds the JSON field {@code frame_images} entry {@code first_frame}
         * with a base64 data URL as the image source.
         *
         * @param dataUrl the base64 data URL of the image
         * @return this builder
         */
        public Builder addFirstFrameByBase64(String dataUrl) {
            return addFrameImage("first_frame", dataUrl);
        }

        /**
         * Adds the JSON field {@code frame_images} entry {@code last_frame}
         * - the image used as the last frame of the generated video.
         *
         * @param url the image URL
         * @return this builder
         */
        public Builder addLastFrameByUrl(String url) {
            return addFrameImage("last_frame", url);
        }

        /**
         * Adds the JSON field {@code frame_images} entry {@code last_frame}
         * with a base64 data URL as the image source.
         *
         * @param dataUrl the base64 data URL of the image
         * @return this builder
         */
        public Builder addLastFrameByBase64(String dataUrl) {
            return addFrameImage("last_frame", dataUrl);
        }

        /**
         * Adds a reference asset to the JSON field {@code input_references}
         * as an {@code image_url} part - image references are supported by
         * all providers.
         *
         * @param url the reference image URL
         * @return this builder
         */
        public Builder addInputReferenceImage(String url) {
            return addInputReference("image_url", "image_url", url);
        }

        /**
         * Adds a reference asset to the JSON field {@code input_references}
         * as an {@code audio_url} part. Trap: audio references are only
         * honored by providers that support them (including BytePlus
         * Seedance generation 2 and newer); other providers use image
         * references and ignore the rest.
         *
         * @param url the reference audio URL
         * @return this builder
         */
        public Builder addInputReferenceAudio(String url) {
            return addInputReference("audio_url", "audio_url", url);
        }

        /**
         * Adds a reference asset to the JSON field {@code input_references}
         * as a {@code video_url} part. Trap: video references are only
         * honored by providers that support them (including BytePlus
         * Seedance generation 2 and newer); other providers use image
         * references and ignore the rest.
         *
         * @param url the reference video URL
         * @return this builder
         */
        public Builder addInputReferenceVideo(String url) {
            return addInputReference("video_url", "video_url", url);
        }

        /**
         * Adds a provider-specific passthrough option to the JSON field
         * {@code provider.options[providerSlug]} - e.g.
         * {@code providerOption("google-vertex", new JSONObject().put("output_config", new JSONObject().put("effort", "low")))}.
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

        /**
         * Sets the JSON field {@code user} - a unique identifier for the
         * end-user, used by OpenRouter for abuse monitoring. Omitted when
         * unset.
         *
         * @param user the end-user identifier
         * @return this builder
         */
        public Builder user(String user) {
            this.user = user;
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

        private Builder addFrameImage(String frameType, String url) {
            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("frame image url must not be null or empty");
            }
            if (frameImages == null) {
                frameImages = new ArrayList<>();
            }
            JSONObject part = new JSONObject();
            part.put("type", "image_url");
            part.put("frame_type", frameType);
            part.put("image_url", new JSONObject().put("url", url));
            frameImages.add(part);
            return this;
        }

        private Builder addInputReference(String type, String wrapperKey, String url) {
            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("input reference url must not be null or empty");
            }
            if (inputReferences == null) {
                inputReferences = new ArrayList<>();
            }
            JSONObject part = new JSONObject();
            part.put("type", type);
            part.put(wrapperKey, new JSONObject().put("url", url));
            inputReferences.add(part);
            return this;
        }

        @Override
        public OpenRouterVideoGenerationRequest build() {
            if (model == null || model.isEmpty()) {
                throw new IllegalStateException("model is required for a video generation request");
            }
            return new OpenRouterVideoGenerationRequest(this);
        }

        @Override
        public OpenRouterVideoGenerationResponse<OpenRouterVideoGenerationRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterVideoGenerationResponse<OpenRouterVideoGenerationRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterVideoGenerationResponse<OpenRouterVideoGenerationRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }

        /**
         * Submits the job and then polls
         * {@code GET /videos/{jobId}} until it reaches a terminal state
         * (blocking) - the same as
         * {@code execute().awaitCompletion(client)} with the default poll
         * interval and timeout.
         *
         * @return the final job response
         */
        public OpenRouterVideoGenerationResponse<OpenRouterVideoGenerationRequest> executeAndAwaitCompletion() {
            return execute().awaitCompletion(
                    client,
                    OpenRouterVideoGenerationResponse.DEFAULT_POLL_INTERVAL_MILLIS,
                    OpenRouterVideoGenerationResponse.DEFAULT_TIMEOUT_MILLIS);
        }
    }
}
