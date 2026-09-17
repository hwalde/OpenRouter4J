package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A request to upload a file:
 * POST https://openrouter.ai/api/v1/files
 *
 * <p>The body is {@code multipart/form-data} with the single required part
 * {@code file} ({@code application/octet-stream}) - this is not a JSON
 * endpoint. Operation-level options travel as query parameters, not in the
 * body: {@code workspace_id} and {@code provider} (both optional).
 *
 * <p>Traps: empty files are rejected by the API with HTTP 413 - the builder
 * rejects them already. The API accepts at most 100 MB per file; larger
 * files are rejected at {@code build()} time. The {@code provider} query
 * parameter is free-form: the API documents {@code openai} and
 * {@code anthropic} but accepts unknown values, so the builder does not
 * validate against an enum.
 */
public final class OpenRouterFileUploadRequest
        extends OpenRouterRequest<OpenRouterFileUploadResponse> {

    private static final String MULTIPART_CRLF = "\r\n";
    private static final long MAX_FILE_BYTES = 100L * 1024L * 1024L;

    private final OpenRouterClient client;
    private final byte[] fileBytes;
    private final String fileName;
    private final Map<String, String> queryParams;
    private final String boundary;

    private OpenRouterFileUploadRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.fileBytes = builder.fileBytes;
        this.fileName = builder.fileName;
        this.queryParams = Collections.unmodifiableMap(new LinkedHashMap<>(builder.queryParams));
        this.boundary = "openrouter4j-" + UUID.randomUUID();
    }

    /** @return the raw bytes of the file part */
    public byte[] fileBytes() {
        return fileBytes;
    }

    /** @return the file name sent in the {@code file} part */
    public String fileName() {
        return fileName;
    }

    /**
     * @return the query parameters this request sends, in insertion order
     */
    public Map<String, String> queryParams() {
        return queryParams;
    }

    @Override
    public String getRelativeUrl() {
        return appendQuery("/files");
    }

    private String appendQuery(String path) {
        if (queryParams.isEmpty()) {
            return path;
        }
        StringBuilder sb = new StringBuilder(path);
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            sb.append(sb.indexOf("?") < 0 ? '?' : '&');
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * This endpoint always travels as multipart/form-data - the content type
     * override makes api-base send the byte body of
     * {@link #getBodyBytes()} with the correct boundary.
     *
     * @return the multipart content type including the boundary
     */
    @Override
    public String getContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    /**
     * Multipart mode has no JSON body. This method returns a human-readable
     * summary instead - the real body is the multipart byte stream of
     * {@link #getBodyBytes()}.
     *
     * @return a summary of the multipart body
     */
    @Override
    public String getBody() {
        return "multipart/form-data body: file=" + fileName
                + (queryParams.get("workspace_id") != null
                        ? ", workspace_id=" + queryParams.get("workspace_id") : "")
                + (queryParams.get("provider") != null
                        ? ", provider=" + queryParams.get("provider") : "");
    }

    /**
     * The multipart byte body with the single {@code file} part.
     *
     * @return the full multipart body including the file bytes
     */
    @Override
    public byte[] getBodyBytes() {
        StringBuilder sb = new StringBuilder();
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

    @Override
    public OpenRouterFileUploadResponse createResponse(String responseBody) {
        return new OpenRouterFileUploadResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterFileUploadRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterFileUploadRequest> {

        private final OpenRouterClient client;
        private final Map<String, String> queryParams = new LinkedHashMap<>();
        private byte[] fileBytes;
        private String fileName;

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
         * Reads a local file and provides it as the required multipart
         * {@code file} part; the file name is taken from the path.
         * Clears a previously set file
         * ({@link #fileByBytes(byte[], String)}).
         *
         * @param path the local file to upload
         * @return this builder
         * @throws UncheckedIOException when the file cannot be read
         */
        public Builder fileByPath(Path path) {
            if (path == null) {
                throw new IllegalArgumentException("file path must not be null");
            }
            try {
                this.fileBytes = Files.readAllBytes(path);
            } catch (IOException e) {
                throw new UncheckedIOException("cannot read file " + path, e);
            }
            this.fileName = path.getFileName() != null ? path.getFileName().toString() : "file";
            return this;
        }

        /**
         * Provides the required multipart {@code file} part from raw bytes.
         * Clears a previously set file ({@link #fileByPath(Path)}).
         *
         * @param bytes the file content
         * @param filename the file name sent in the {@code file} part
         * @return this builder
         */
        public Builder fileByBytes(byte[] bytes, String filename) {
            if (bytes == null || bytes.length == 0) {
                throw new IllegalArgumentException("file bytes must not be null or empty");
            }
            if (filename == null || filename.isEmpty()) {
                throw new IllegalArgumentException("filename must not be null or empty");
            }
            this.fileBytes = bytes;
            this.fileName = filename;
            return this;
        }

        /**
         * Sets the query key {@code workspace_id} - the workspace the file
         * belongs to. Omitted when unset.
         *
         * @param workspaceId the workspace id
         * @return this builder
         */
        public Builder workspaceId(String workspaceId) {
            return queryParam("workspace_id", workspaceId);
        }

        /**
         * Sets the query key {@code provider} - the storage provider the
         * file is uploaded to. The API documents {@code openai} and
         * {@code anthropic}, but the value is free-form: unknown values are
         * not rejected by the builder and are passed through verbatim.
         * Omitted when unset.
         *
         * @param provider the storage provider
         * @return this builder
         */
        public Builder provider(String provider) {
            return queryParam("provider", provider);
        }

        /**
         * Adds any query parameter verbatim, for operation-level options
         * OpenRouter adds later. The value is sent URL-encoded; {@code null}
         * values are ignored.
         *
         * @param name the query parameter name
         * @param value the query parameter value
         * @return this builder
         */
        public Builder queryParam(String name, Object value) {
            if (name != null && !name.isEmpty() && value != null) {
                queryParams.put(name, String.valueOf(value));
            }
            return this;
        }

        @Override
        public OpenRouterFileUploadRequest build() {
            if (fileBytes == null) {
                throw new IllegalStateException(
                        "file is required for an upload request - use fileByPath or fileByBytes");
            }
            if (fileBytes.length == 0) {
                throw new IllegalStateException(
                        "file must not be empty - the API rejects empty files with HTTP 413");
            }
            if (fileBytes.length > MAX_FILE_BYTES) {
                throw new IllegalArgumentException(
                        "file exceeds the API maximum of 100 MB, got " + fileBytes.length + " bytes");
            }
            if (fileName == null || fileName.isEmpty()) {
                throw new IllegalStateException("filename is required for an upload request");
            }
            // CR/LF in the interpolated Content-Disposition line would split
            // the header (a malformed body that only fails server-side) -
            // reject loudly.
            rejectLineBreaks("fileName", fileName);
            return new OpenRouterFileUploadRequest(this);
        }

        private void rejectLineBreaks(String field, String value) {
            if (value != null && (value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0)) {
                throw new IllegalArgumentException(
                        field + " must not contain CR/LF characters");
            }
        }

        @Override
        public OpenRouterFileUploadResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterFileUploadResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterFileUploadResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
