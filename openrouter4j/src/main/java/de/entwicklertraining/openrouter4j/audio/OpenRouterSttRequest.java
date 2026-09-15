package de.entwicklertraining.openrouter4j.audio;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
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
    }

    /** @return the STT model id (e.g. {@code openai/whisper-large-v3}) */
    public String model() {
        return model;
    }

    /** @return whether this request travels as multipart/form-data */
    public boolean isMultipart() {
        return fileBytes != null;
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
        if (!isMultipart()) {
            return root.toString();
        }
        return "multipart/form-data body: model=" + model
                + (language != null ? ", language=" + language : "")
                + (responseFormat != null ? ", response_format=" + responseFormat : "")
                + (temperature != null ? ", temperature=" + temperature : "")
                + (timestampGranularities != null && !timestampGranularities.isEmpty()
                        ? ", timestamp_granularities=" + timestampGranularities : "")
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

        @Override
        public OpenRouterSttRequest build() {
            if (model == null || model.isEmpty()) {
                throw new IllegalStateException("model is required for a transcription request");
            }
            if (fileBytes == null && (audioData == null || audioFormat == null)) {
                throw new IllegalStateException(
                        "audio is required for a transcription request - use audioByBase64, audioByPath or audioByFile");
            }
            return new OpenRouterSttRequest(this);
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
