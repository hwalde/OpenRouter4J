package de.entwicklertraining.openrouter4j.containers;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Downloads the content of one file of a code-execution container:
 * GET https://openrouter.ai/api/v1/containers/{container_id}/files/{file_id}/content
 *
 * <p>The body is the raw file bytes. This is a binary endpoint: the request
 * overrides {@code isBinaryResponse()}, so api-base hands the raw bytes to
 * {@link #createResponse(byte[])} instead of trying to parse JSON.
 *
 * <p>The container id is the canonical id exactly as returned in a bash/shell
 * server-tool result (e.g. {@code sess_abc123}); the file id has the shape
 * {@code cfile_} + base64url of the file path. Both path segments are
 * URL-encoded.
 * <p>
 * Trap: the content of a file that is still being written by the executing
 * code may be incomplete - list the container files first (see
 * {@link OpenRouterContainerFileListRequest}) and only download once the
 * file's {@code created_at} has settled (or poll until it stops changing).
 */
public final class OpenRouterContainerFileContentRequest
        extends OpenRouterRequest<OpenRouterContainerFileContentResponse> {

    private final OpenRouterClient client;
    private final String containerId;
    private final String fileId;

    private OpenRouterContainerFileContentRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.containerId = builder.containerId;
        this.fileId = builder.fileId;
    }

    /** @return the container id ({@code sess_...}) holding the file */
    public String containerId() {
        return containerId;
    }

    /** @return the container file id ({@code cfile_...}) whose content is downloaded */
    public String fileId() {
        return fileId;
    }

    @Override
    public String getRelativeUrl() {
        return "/containers/" + encode(containerId) + "/files/" + encode(fileId) + "/content";
    }

    private static String encode(String segment) {
        if (segment == null) {
            return "";
        }
        // URLEncoder is form-encoding; path segments must keep "/" out anyway,
        // so encoding it is the correct behaviour for a container or file id.
        return URLEncoder.encode(segment, StandardCharsets.UTF_8);
    }

    @Override
    public String getHttpMethod() {
        return "GET";
    }

    /**
     * GET requests carry no body.
     *
     * @return always {@code null}
     */
    @Override
    public String getBody() {
        return null;
    }

    /**
     * This endpoint returns the raw file bytes, not JSON - this override
     * makes api-base deliver the body as bytes.
     *
     * @return always {@code true}
     */
    @Override
    public boolean isBinaryResponse() {
        return true;
    }

    /**
     * Stores the delivered file bytes in the typed response.
     *
     * @param contentBytes the raw file bytes
     * @return the typed response holding the bytes
     */
    @Override
    public OpenRouterContainerFileContentResponse createResponse(byte[] contentBytes) {
        return new OpenRouterContainerFileContentResponse(contentBytes, this);
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
    public OpenRouterContainerFileContentResponse createResponse(String responseBody) {
        return new OpenRouterContainerFileContentResponse(
                responseBody == null ? null : responseBody.getBytes(StandardCharsets.UTF_8), this);
    }

    /**
     * Starting point for building a {@link OpenRouterContainerFileContentRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterContainerFileContentRequest> {

        private final OpenRouterClient client;
        private final String containerId;
        private final String fileId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param containerId the container id ({@code sess_...}) holding the file
         * @param fileId the container file id ({@code cfile_...}) whose content to download
         */
        public Builder(OpenRouterClient client, String containerId, String fileId) {
            super(client);
            this.client = client;
            this.containerId = containerId;
            this.fileId = fileId;
        }

        @Override
        public OpenRouterContainerFileContentRequest build() {
            if (containerId == null || containerId.isEmpty()) {
                throw new IllegalStateException("containerId is required for a container file content request");
            }
            if (fileId == null || fileId.isEmpty()) {
                throw new IllegalStateException("fileId is required for a container file content request");
            }
            return new OpenRouterContainerFileContentRequest(this);
        }

        @Override
        public OpenRouterContainerFileContentResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterContainerFileContentResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterContainerFileContentResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
