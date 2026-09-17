package de.entwicklertraining.openrouter4j.containers;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Reads the metadata of one file of a code-execution container:
 * GET https://openrouter.ai/api/v1/containers/{container_id}/files/{file_id}
 *
 * <p>The container id is the canonical id exactly as returned in a bash/shell
 * server-tool result (e.g. {@code sess_abc123}); the file id has the shape
 * {@code cfile_} + base64url of the file path. Both path segments are
 * URL-encoded. For the bytes themselves see
 * {@link OpenRouterContainerFileContentRequest}; for a listing see
 * {@link OpenRouterContainerFileListRequest}.
 */
public final class OpenRouterContainerFileGetRequest
        extends OpenRouterRequest<OpenRouterContainerFileGetResponse> {

    private final OpenRouterClient client;
    private final String containerId;
    private final String fileId;

    private OpenRouterContainerFileGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.containerId = builder.containerId;
        this.fileId = builder.fileId;
    }

    /** @return the container id ({@code sess_...}) holding the file */
    public String containerId() {
        return containerId;
    }

    /** @return the container file id ({@code cfile_...}) read by this request */
    public String fileId() {
        return fileId;
    }

    @Override
    public String getRelativeUrl() {
        return "/containers/" + encode(containerId) + "/files/" + encode(fileId);
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

    @Override
    public OpenRouterContainerFileGetResponse createResponse(String responseBody) {
        return new OpenRouterContainerFileGetResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterContainerFileGetRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterContainerFileGetRequest> {

        private final OpenRouterClient client;
        private final String containerId;
        private final String fileId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param containerId the container id ({@code sess_...}) holding the file
         * @param fileId the container file id ({@code cfile_...}) to read
         */
        public Builder(OpenRouterClient client, String containerId, String fileId) {
            super(client);
            this.client = client;
            this.containerId = containerId;
            this.fileId = fileId;
        }

        @Override
        public OpenRouterContainerFileGetRequest build() {
            if (containerId == null || containerId.isEmpty()) {
                throw new IllegalStateException("containerId is required for a container file request");
            }
            if (fileId == null || fileId.isEmpty()) {
                throw new IllegalStateException("fileId is required for a container file request");
            }
            return new OpenRouterContainerFileGetRequest(this);
        }

        @Override
        public OpenRouterContainerFileGetResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterContainerFileGetResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterContainerFileGetResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
