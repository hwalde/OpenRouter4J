package de.entwicklertraining.openrouter4j.containers;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.files.OpenRouterFile;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Promotes one file of a code-execution container into the workspace's
 * durable document storage:
 * POST https://openrouter.ai/api/v1/containers/{container_id}/files/{file_id}/promote
 *
 * <p>The request carries no JSON body.
 * <p>
 * Trap: promotion copies the file out of the container sandbox so it outlives
 * the container - but the copy counts against the workspace's storage quota.
 * Unlike a direct upload, promoted files are downloadable. The response is
 * the new document in the FILES API shape (see {@link OpenRouterFile}); use
 * its id with the file endpoints of the {@code files} package for further
 * handling.
 */
public final class OpenRouterContainerFilePromoteRequest
        extends OpenRouterRequest<OpenRouterContainerFilePromoteResponse> {

    private final OpenRouterClient client;
    private final String containerId;
    private final String fileId;

    private OpenRouterContainerFilePromoteRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.containerId = builder.containerId;
        this.fileId = builder.fileId;
    }

    /** @return the container id ({@code sess_...}) holding the file */
    public String containerId() {
        return containerId;
    }

    /** @return the container file id ({@code cfile_...}) promoted by this request */
    public String fileId() {
        return fileId;
    }

    @Override
    public String getRelativeUrl() {
        return "/containers/" + encode(containerId) + "/files/" + encode(fileId) + "/promote";
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
        return "POST";
    }

    /**
     * POST with no JSON body - everything travels in the path.
     *
     * @return an empty body
     */
    @Override
    public String getBody() {
        return "";
    }

    @Override
    public OpenRouterContainerFilePromoteResponse createResponse(String responseBody) {
        JSONObject json;
        try {
            json = new JSONObject(responseBody);
        } catch (Exception ignored) {
            // tolerate a non-object body (e.g. plain text or an array):
            // the accessors then behave as if the body were empty
            json = new JSONObject();
        }
        return new OpenRouterContainerFilePromoteResponse(json, this);
    }

    /**
     * Starting point for building a {@link OpenRouterContainerFilePromoteRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterContainerFilePromoteRequest> {

        private final OpenRouterClient client;
        private final String containerId;
        private final String fileId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param containerId the container id ({@code sess_...}) holding the file
         * @param fileId the container file id ({@code cfile_...}) to promote
         */
        public Builder(OpenRouterClient client, String containerId, String fileId) {
            super(client);
            this.client = client;
            this.containerId = containerId;
            this.fileId = fileId;
        }

        @Override
        public OpenRouterContainerFilePromoteRequest build() {
            if (containerId == null || containerId.isEmpty()) {
                throw new IllegalStateException("containerId is required for a container file promote request");
            }
            if (fileId == null || fileId.isEmpty()) {
                throw new IllegalStateException("fileId is required for a container file promote request");
            }
            return new OpenRouterContainerFilePromoteRequest(this);
        }

        @Override
        public OpenRouterContainerFilePromoteResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterContainerFilePromoteResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterContainerFilePromoteResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
