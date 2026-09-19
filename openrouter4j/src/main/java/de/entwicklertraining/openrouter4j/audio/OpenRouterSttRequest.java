package de.entwicklertraining.openrouter4j.audio;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * A request to transcribe audio to text:
 * POST https://openrouter.ai/api/v1/audio/transcriptions
 *
 * <p>The {@code model} and the audio are required. Two wire forms are
 * supported, exactly as the API schema defines them:
 * <ul>
 *   <li><b>JSON body</b> (default): the audio travels as
 *   {@code input_audio.data} - raw base64 bytes, <i>not</i> a data URI - plus
 *   {@code input_audio.format}. Built with
 *   {@link Builder#audioByBase64(String, String)} or
 *   {@link Builder#audioByPath(Path, String)}.</li>
 *   <li><b>multipart/form-data</b>: the audio travels as a {@code file} part
 *   (max 25 MB; larger files must use the base64 JSON form). Built with
 *   {@link Builder#audioByFile(Path)} - the format is derived from the file
 *   extension server-side.</li>
 * </ul>
 *
 * <p>Trap: the segment/word timestamp fields are only meaningful with
 * {@code responseFormat("verbose_json")} and are ignored by non-OpenAI-compatible
 * providers.
 */
public final class OpenRouterSttRequest extends OpenRouterRequest<OpenRouterSttResponse> {

    private static final String MULTIPART_CRLF = "\r\n";

    private final OpenRouterClient client;
    private final String model;
    private final String audioData;
    private final String audioFormat;
    private final byte[] fileBytes;
    private final String fileName;
    private final String language;
    private final String responseFormat;
    private final Double temperature;
    private final List<String> timestampGranularities;
    private final String user;
    private final String sessionId;
    private final OpenRouterTraceConfig trace;
    private final JSONObject providerOptions;

    private final String boundary;

    private OpenRouterSttRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.boundary = "openrouter4j-" + UUID.randomUUID();
        this.model = builder.model;
        this.audioData = builder.audioData;
        this.audioFormat = builder.audioFormat;
        this.fileBytes = builder.fileBytes;
        this.fileName = builder.fileName;
        this.language = builder.language;
        this.responseFormat = builder.responseFormat;
        this.temperature = builder.temperature;
        this.timestampGranularities = builder.timestampGranularities == null
                ? null : List.copyOf(builder.timestampGranularities);
        this.user = builder.user;
        this.sessionId = builder.sessionId;
        this.trace = builder.trace;
        this.providerOptions = builder.providerOptions == null
                ? null : new JSONObject(builder.providerOptions.toString());
    }

    /** @return the STT model id (e.g. {@code openai/whisper-large-v3}) */
    public String model() {
        return model;
    }

    /** @return whether this request travels as multipart/form-data */
    public boolean isMultipart() {
        return fileBytes != null;
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
     * never sent to the provider. In multipart mode it travels as a plain
     * {@code session_id} form field (like {@code user}, it is a plain string,
     * so no JSON encoding is needed).
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
     * provider slug, or {@code null} when none were set. JSON mode only -
     * the multipart form has no {@code provider} field.
     *
     * @return the provider options object, or {@code null}
     */
    public JSONObject providerOptions() {
        return providerOptions;
    }

    @Override
    public String getRelativeUrl() {
        return "/audio/transcriptions";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * In multipart mode this override makes api-base send the byte body;
     * in JSON mode the inherited {@code application/json} default applies.
     *
     * @return the multipart content type with the boundary, or the inherited
     *         JSON default
     */
    @Override
    public String getContentType() {
        if (isMultipart()) {
            return "multipart/form-data; boundary=" + boundary();
        }
        return super.getContentType();
    }

    private String boundary() {
        return boundary;
    }

    /**
     * JSON path: the request body - {@code model} (required),
     * {@code input_audio} with {@code data} (raw base64, not a data URI) and
     * {@code format} (required in the JSON form), {@code language},
     * {@code response_format}, {@code temperature},
     * {@code timestamp_granularities} (all omitted when unset).
     *
     * <p>In multipart mode this method returns a human-readable summary
     * instead - the real body is the multipart byte stream of
     * {@link #getBodyBytes()}. In JSON mode this method returns the JSON
     * body.
     *
     * @return the JSON body (JSON mode) or a summary (multipart mode)
     */
    @Override
    public String getBody() {
        JSONObject root = new JSONObject();
        root.put("model", model);
        JSONObject inputAudio = new JSONObject();
        inputAudio.put("data", audioData == null ? "" : audioData);
        inputAudio.put("format", audioFormat == null ? "" : audioFormat);
        root.put("input_audio", inputAudio);
        if (language != null) {
            root.put("language", language);
        }
        if (responseFormat != null) {
            root.put("response_format", responseFormat);
        }
        if (temperature != null) {
            root.put("temperature", temperature);
        }
        if (timestampGranularities != null && !timestampGranularities.isEmpty()) {
            JSONArray arr = new JSONArray();
            for (String g : timestampGranularities) {
                arr.put(g);
            }
            root.put("timestamp_granularities", arr);
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
        if (!isMultipart()) {
            return root.toString();
        }
        return "multipart/form-data body: model=" + model
                + (language != null ? ", language=" + language : "")
                + (responseFormat != null ? ", response_format=" + responseFormat : "")
                + (temperature != null ? ", temperature=" + temperature : "")
                + (timestampGranularities != null && !timestampGranularities.isEmpty()
                        ? ", timestamp_granularities=" + timestampGranularities : "")
                + (user != null ? ", user=" + user : "")
                + (sessionId != null ? ", session_id=" + sessionId : "")
                + (trace != null ? ", trace=" + trace.toJson() : "")
                + ", file=" + fileName;
    }

    /**
     * The multipart byte body with the {@code file} part and the plain form
     * fields. Only called by api-base in multipart mode (the content type
     * starts with {@code multipart/form-data} there).
     *
     * @return the full multipart body including file bytes
     */
    @Override
    public byte[] getBodyBytes() {
        if (!isMultipart()) {
            return super.getBodyBytes();
        }
        String boundary = boundary();
        StringBuilder sb = new StringBuilder();
        addFormField(sb, boundary, "model", model);
        if (language != null) {
            addFormField(sb, boundary, "language", language);
        }
        if (responseFormat != null) {
            addFormField(sb, boundary, "response_format", responseFormat);
        }
        if (temperature != null) {
            addFormField(sb, boundary, "temperature", String.valueOf(temperature));
        }
        if (timestampGranularities != null) {
            for (String granularity : timestampGranularities) {
                addFormField(sb, boundary, "timestamp_granularities[]", granularity);
            }
        }
        if (user != null) {
            addFormField(sb, boundary, "user", user);
        }
        if (sessionId != null) {
            addFormField(sb, boundary, "session_id", sessionId);
        }
        if (trace != null) {
            // The multipart form carries trace as a JSON-encoded string that
            // must decode to a JSON object (the API schema's multipart shape).
            addFormField(sb, boundary, "trace", trace.toJson().toString());
        }
        sb.append("--").append(boundary).append(MULTIPART_CRLF);
        sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(fileName).append('"').append(MULTIPART_CRLF);
        sb.append("Content-Type: application/octet-stream").append(MULTIPART_CRLF);
        sb.append(MULTIPART_CRLF);

        byte[] head = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] tail = (MULTIPART_CRLF + "--" + boundary + "--" + MULTIPART_CRLF)
                .getBytes(StandardCharsets.UTF_8);

        byte[] body = new byte[head.length + fileBytes.length + tail.length];
        System.arraycopy(head, 0, body, 0, head.length);
        System.arraycopy(fileBytes, 0, body, head.length, fileBytes.length);
        System.arraycopy(tail, 0, body, head.length + fileBytes.length, tail.length);
        return body;
    }

    private void addFormField(StringBuilder sb, String boundary, String name, String value) {
        sb.append("--").append(boundary).append(MULTIPART_CRLF);
        sb.append("Content-Disposition: form-data; name=\"").append(name).append('"')
                .append(MULTIPART_CRLF);
        sb.append(MULTIPART_CRLF);
        sb.append(value).append(MULTIPART_CRLF);
    }

    @Override
    public OpenRouterSttResponse createResponse(String responseBody) {
        return new OpenRouterSttResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterSttRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterSttRequest> {

        private final OpenRouterClient client;
        private String model;
        private String audioData;
        private String audioFormat;
        private byte[] fileBytes;
        private String fileName;
        private String language;
        private String responseFormat;
        private Double temperature;
        private List<String> timestampGranularities;
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
         * Sets the required JSON field {@code model} - the STT model id
         * (e.g. {@code openai/whisper-large-v3}).
         *
         * @param model the STT model id
         * @return this builder
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Provides the audio as raw base64 for the JSON body field
         * {@code input_audio.data} - raw base64 bytes, <b>not</b> a data URI.
         * Clears a previously set file
         * ({@link #audioByFile(Path)}).
         *
         * @param base64 the base64-encoded audio bytes
         * @param format the audio format (e.g. {@code wav}, {@code mp3},
         *               {@code flac}, {@code m4a}, {@code ogg}, {@code webm},
         *               {@code aac}); supported formats vary by provider
         * @return this builder
         */
        public Builder audioByBase64(String base64, String format) {
            if (base64 == null || base64.isEmpty()) {
                throw new IllegalArgumentException("base64 audio data must not be null or empty");
            }
            if (format == null || format.isEmpty()) {
                throw new IllegalArgumentException("audio format must not be null or empty");
            }
            this.audioData = base64;
            this.audioFormat = format;
            this.fileBytes = null;
            this.fileName = null;
            return this;
        }

        /**
         * Reads a local audio file and provides it as raw base64 for the
         * JSON body (the JSON form has no 25 MB file limit, but the request
         * JSON grows to roughly 4/3 of the file size). The format must be
         * given explicitly - it is not derived from the extension. Clears a
         * previously set file ({@link #audioByFile(Path)}).
         *
         * @param path the local audio file
         * @param format the audio format (e.g. {@code wav})
         * @return this builder
         * @throws UncheckedIOException when the file cannot be read
         */
        public Builder audioByPath(Path path, String format) {
            if (path == null) {
                throw new IllegalArgumentException("audio path must not be null");
            }
            try {
                return audioByBase64(Base64.getEncoder().encodeToString(Files.readAllBytes(path)), format);
            } catch (IOException e) {
                throw new UncheckedIOException("cannot read audio file " + path, e);
            }
        }

        /**
         * Provides the audio as a multipart {@code file} part (max 25 MB per
         * the API; send larger files via
         * {@link #audioByPath(Path, String)}). The format is derived from
         * the filename extension or the file part content type server-side.
         * Clears previously set base64 audio.
         *
         * @param path the local audio file
         * @return this builder
         * @throws UncheckedIOException when the file cannot be read
         */
        public Builder audioByFile(Path path) {
            if (path == null) {
                throw new IllegalArgumentException("audio file path must not be null");
            }
            try {
                this.fileBytes = Files.readAllBytes(path);
            } catch (IOException e) {
                throw new UncheckedIOException("cannot read audio file " + path, e);
            }
            this.fileName = path.getFileName() != null ? path.getFileName().toString() : "audio";
            this.audioData = null;
            this.audioFormat = null;
            return this;
        }

        /**
         * Sets the JSON field {@code language} - the ISO-639-1 language code
         * (e.g. {@code en}, {@code ja}); auto-detected when omitted.
         *
         * @param language the language code
         * @return this builder
         */
        public Builder language(String language) {
            this.language = language;
            return this;
        }

        /**
         * Sets the JSON field {@code response_format} - {@code json}
         * (default) returns {@code text} and {@code usage};
         * {@code verbose_json} additionally returns {@code task},
         * {@code language}, {@code duration} and segment-level timestamps -
         * only supported by OpenAI-compatible providers.
         *
         * @param responseFormat {@code json} or {@code verbose_json}
         * @return this builder
         */
        public Builder responseFormat(String responseFormat) {
            this.responseFormat = responseFormat;
            return this;
        }

        /**
         * Sets the JSON field {@code temperature} - the sampling temperature
         * for the transcription.
         *
         * @param temperature the sampling temperature
         * @return this builder
         */
        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        /**
         * Adds one JSON field {@code timestamp_granularities} entry -
         * {@code segment} returns segment-level timestamps, {@code word}
         * additionally returns word-level timestamps in the {@code words}
         * array. Ignored unless {@code response_format} is
         * {@code verbose_json}.
         *
         * @param granularity {@code segment} or {@code word}
         * @return this builder
         */
        public Builder addTimestampGranularity(String granularity) {
            if (!"segment".equals(granularity) && !"word".equals(granularity)) {
                throw new IllegalArgumentException(
                        "timestamp granularity must be \"segment\" or \"word\", got: " + granularity);
            }
            if (timestampGranularities == null) {
                timestampGranularities = new ArrayList<>();
            }
            if (!timestampGranularities.contains(granularity)) {
                timestampGranularities.add(granularity);
            }
            return this;
        }

        /**
         * Sets the JSON field {@code user} (multipart form field {@code user})
         * - a unique identifier representing your end-user. Forwarded to
         * Broadcast and private logging as the end-user id; never sent to the
         * provider. Omitted when unset.
         *
         * @param user the end-user identifier
         * @return this builder
         */
        public Builder user(String user) {
            this.user = user;
            return this;
        }

        /**
         * Sets the JSON field {@code session_id} (multipart form field
         * {@code session_id}) - a unique identifier for grouping related
         * requests (a conversation or agent workflow). Used for observability
         * grouping in Broadcast and private logging; never sent to the
         * provider. Maximum 256 characters (API-side limit, not validated
         * here). Omitted when unset.
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
         * {@link OpenRouterTraceConfig#builder()}. In multipart mode the
         * schema carries it as a JSON-encoded string; the library performs
         * that encoding. Omitted when unset.
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
         * {@code provider.options[providerSlug]} (JSON body mode only - the
         * multipart form schema has no {@code provider} field), e.g.
         * {@code providerOption("openai", new JSONObject().put("prompt", "..."))}.
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

        @Override
        public OpenRouterSttRequest build() {
            if (model == null || model.isEmpty()) {
                throw new IllegalStateException("model is required for a transcription request");
            }
            // CR/LF in an interpolated multipart line would split the header
            // (a malformed body that only fails server-side) - reject loudly.
            rejectLineBreaks("model", model);
            rejectLineBreaks("language", language);
            rejectLineBreaks("responseFormat", responseFormat);
            rejectLineBreaks("fileName", fileName);
            rejectLineBreaks("user", user);
            rejectLineBreaks("sessionId", sessionId);
            if (fileBytes == null && (audioData == null || audioFormat == null)) {
                throw new IllegalStateException(
                        "audio is required for a transcription request - use audioByBase64, audioByPath or audioByFile");
            }
            return new OpenRouterSttRequest(this);
        }

        private void rejectLineBreaks(String field, String value) {
            if (value != null && (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0)) {
                throw new IllegalArgumentException(
                        field + " must not contain CR/LF characters");
            }
        }

        @Override
        public OpenRouterSttResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterSttResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterSttResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
