package de.entwicklertraining.openrouter4j.audio;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A request to synthesize speech from text:
 * POST https://openrouter.ai/api/v1/audio/speech
 *
 * <p>The {@code model} and the {@code input} are required; the {@code voice}
 * is provider-specific (e.g. {@code en_paul_neutral}) and has a minimum
 * length of one character. The response is a raw audio bytestream - the
 * request is a binary endpoint, so the typed response holds the bytes
 * (Content-Type: {@code audio/mpeg} for mp3, {@code audio/pcm} for pcm -
 * 16-bit little-endian).
 *
 * <p>Trap: {@code speed} is only used by models that support it (e.g. OpenAI
 * TTS) and ignored by other providers.
 */
public final class OpenRouterSpeechRequest extends OpenRouterRequest<OpenRouterSpeechResponse> {

    private final OpenRouterClient client;
    private final String model;
    private final String input;
    private final String voice;
    private final String responseFormat;
    private final Double speed;
    private final List<JSONObject> inputReferences;
    private final String user;
    private final String sessionId;
    private final OpenRouterTraceConfig trace;
    private final JSONObject providerOptions;

    private OpenRouterSpeechRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.model = builder.model;
        this.input = builder.input;
        this.voice = builder.voice;
        this.responseFormat = builder.responseFormat;
        this.speed = builder.speed;
        this.inputReferences = builder.inputReferences == null
                ? null : List.copyOf(builder.inputReferences);
        this.user = builder.user;
        this.sessionId = builder.sessionId;
        this.trace = builder.trace;
        this.providerOptions = builder.providerOptions == null
                ? null : new JSONObject(builder.providerOptions.toString());
    }

    /** @return the TTS model id (e.g. {@code mistralai/voxtral-mini-tts-2603}) */
    public String model() {
        return model;
    }

    /** @return the text to synthesize */
    public String input() {
        return input;
    }

    /**
     * The {@code user} end-user identifier, or {@code null} when unset (the
     * key is not sent). Forwarded to Broadcast and private logging as the
     * end-user id; never sent to the provider.
     *
     * @return the end-user identifier, or {@code null}
     */
    public String user() {
        return user;
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
     * The configured {@code provider.options} passthrough entries keyed by
     * provider slug, or {@code null} when none were set.
     *
     * @return the provider options object, or {@code null}
     */
    public JSONObject providerOptions() {
        return providerOptions;
    }

    @Override
    public String getRelativeUrl() {
        return "/audio/speech";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * This endpoint returns raw audio bytes, not JSON - this override makes
     * api-base deliver the body as bytes.
     *
     * @return always {@code true}
     */
    @Override
    public boolean isBinaryResponse() {
        return true;
    }

    /**
     * JSON path: the request body - {@code model} and {@code input}
     * (required), {@code voice}, {@code response_format}, {@code speed},
     * {@code input_references} (all omitted when unset), {@code user} and
     * {@code session_id} (omitted when unset).
     *
     * @return the JSON body of this request
     */
    @Override
    public String getBody() {
        JSONObject root = new JSONObject();
        root.put("model", model);
        root.put("input", input);
        if (voice != null) {
            root.put("voice", voice);
        }
        if (responseFormat != null) {
            root.put("response_format", responseFormat);
        }
        if (speed != null) {
            root.put("speed", speed);
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
        if (sessionId != null) {
            root.put("session_id", sessionId);
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

    /**
     * Stores the delivered audio bytes in the typed response.
     *
     * @param contentBytes the raw audio bytes
     * @return the typed response holding the bytes
     */
    @Override
    public OpenRouterSpeechResponse createResponse(byte[] contentBytes) {
        return new OpenRouterSpeechResponse(contentBytes, this);
    }

    /**
     * Defensive fallback for a text body on this binary endpoint: wraps the
     * UTF-8 bytes of the string. api-base only calls this when the response
     * was not flagged binary (HTTP error handling paths raise exceptions
     * before this is reached).
     *
     * @param responseBody the text body
     * @return the typed response holding the UTF-8 encoded bytes
     */
    @Override
    public OpenRouterSpeechResponse createResponse(String responseBody) {
        return new OpenRouterSpeechResponse(
                responseBody == null ? null : responseBody.getBytes(StandardCharsets.UTF_8), this);
    }

    /**
     * Starting point for building a {@link OpenRouterSpeechRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterSpeechRequest> {

        private final OpenRouterClient client;
        private String model;
        private String input;
        private String voice;
        private String responseFormat;
        private Double speed;
        private List<JSONObject> inputReferences;
        private String user;
        private String sessionId;
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
         * Sets the required JSON field {@code model} - the TTS model id
         * (e.g. {@code mistralai/voxtral-mini-tts-2603}).
         *
         * @param model the TTS model id
         * @return this builder
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets the required JSON field {@code input} - the text to
         * synthesize.
         *
         * @param input the text to synthesize
         * @return this builder
         */
        public Builder input(String input) {
            this.input = input;
            return this;
        }

        /**
         * Sets the JSON field {@code voice} - the provider-specific voice
         * identifier (e.g. {@code en_paul_neutral}); minimum length of one
         * character when set.
         *
         * @param voice the voice identifier
         * @return this builder
         */
        public Builder voice(String voice) {
            if (voice != null && voice.isEmpty()) {
                throw new IllegalArgumentException("voice must not be empty when set");
            }
            this.voice = voice;
            return this;
        }

        /**
         * Sets the JSON field {@code response_format} - the audio output
         * format: {@code mp3} or {@code pcm} (the API default is
         * {@code pcm}, 16-bit little-endian).
         *
         * @param responseFormat {@code mp3} or {@code pcm}
         * @return this builder
         */
        public Builder responseFormat(String responseFormat) {
            this.responseFormat = responseFormat;
            return this;
        }

        /**
         * Sets the JSON field {@code speed} - the playback speed multiplier.
         * Trap: only used by models that support it (e.g. OpenAI TTS),
         * ignored by other providers.
         *
         * @param speed the playback speed multiplier
         * @return this builder
         */
        public Builder speed(Double speed) {
            this.speed = speed;
            return this;
        }

        /**
         * Sets the JSON field {@code user} - a unique identifier representing
         * your end-user. Forwarded to Broadcast and private logging as the
         * end-user id; never sent to the provider. Omitted when unset.
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
         * Adds a provider-specific passthrough option to the JSON field
         * {@code provider.options[providerSlug]}, e.g.
         * {@code providerOption("openai", new JSONObject())}. Only the
         * options of the provider that serves the request are forwarded.
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
         * Adds a voice-cloning reference audio to the JSON field
         * {@code input_references} as an {@code input_audio} part (for
         * stateless voice cloning; the sample may be a base64 data URI or
         * raw base64 - max 20 MiB of base64 / 15 MiB decoded audio). Only
         * routed to endpoints that support voice cloning.
         *
         * @param base64OrDataUrl the reference audio, base64-encoded
         * @param format the optional audio format of the reference audio
         *               (e.g. {@code wav}); most providers detect the format
         *               from the audio bytes, so {@code null} is allowed
         * @return this builder
         */
        public Builder addVoiceReferenceAudio(String base64OrDataUrl, String format) {
            if (base64OrDataUrl == null || base64OrDataUrl.isEmpty()) {
                throw new IllegalArgumentException("voice reference audio must not be null or empty");
            }
            if (inputReferences == null) {
                inputReferences = new ArrayList<>();
            }
            JSONObject part = new JSONObject();
            part.put("type", "input_audio");
            JSONObject inputAudio = new JSONObject();
            inputAudio.put("data", base64OrDataUrl);
            if (format != null && !format.isEmpty()) {
                inputAudio.put("format", format);
            }
            part.put("input_audio", inputAudio);
            inputReferences.add(part);
            return this;
        }

        /**
         * Adds the transcript of the accompanying reference audio to the
         * JSON field {@code input_references} as a {@code text} part (max
         * 10000 characters).
         *
         * @param transcript the transcript of the reference audio
         * @return this builder
         */
        public Builder addVoiceReferenceText(String transcript) {
            if (transcript == null || transcript.isEmpty()) {
                throw new IllegalArgumentException("voice reference transcript must not be null or empty");
            }
            if (transcript.length() > 10_000) {
                throw new IllegalArgumentException(
                        "voice reference transcript exceeds 10000 characters, got: " + transcript.length());
            }
            if (inputReferences == null) {
                inputReferences = new ArrayList<>();
            }
            JSONObject part = new JSONObject();
            part.put("type", "text");
            part.put("text", transcript);
            inputReferences.add(part);
            return this;
        }

        @Override
        public OpenRouterSpeechRequest build() {
            if (model == null || model.isEmpty()) {
                throw new IllegalStateException("model is required for a speech request");
            }
            if (input == null || input.isEmpty()) {
                throw new IllegalStateException("input is required for a speech request");
            }
            return new OpenRouterSpeechRequest(this);
        }

        @Override
        public OpenRouterSpeechResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterSpeechResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterSpeechResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
