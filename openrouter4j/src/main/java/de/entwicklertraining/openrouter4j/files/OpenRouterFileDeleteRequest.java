package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to delete a file:
 * DELETE https://openrouter.ai/api/v1/files/{file_id}
 *
 * <p>Trap: deletion is irreversible - the file content can no longer be
 * downloaded afterwards and cannot be restored.
 */
public final class OpenRouterFileDeleteRequest
        extends OpenRouterRequest<OpenRouterFileDeleteResponse> {

    private final OpenRouterClient client;
    private final String fileId;

    private OpenRouterFileDeleteRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.fileId = builder.fileId;
    }

    /** @return the file id to delete */
    public String fileId() {
        return fileId;
    }

    @Override
    public String getRelativeUrl() {
        return "/files/" + encode(fileId);
    }

    private static String encode(String segment) {
        if (segment == null) {
            return "";
        }
        // URLEncoder is form-encoding; path segments must keep "/" out anyway,
        // so encoding it is the correct behaviour for a file id.
        return URLEncoder.encode(segment, StandardCharsets.UTF_8);
    }

    @Override
    public String getHttpMethod() {
        return "DELETE";
    }

    /**
     * DELETE requests carry no body.
     *
     * @return always {@code null}
     */
    @Override
    public String getBody() {
        return null;
    }

    @Override
    public OpenRouterFileDeleteResponse createResponse(String responseBody) {
        return new OpenRouterFileDeleteResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterFileDeleteRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterFileDeleteRequest> {

        private final OpenRouterClient client;
        private final String fileId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param fileId the file id to delete (deletion is irreversible)
         */
        public Builder(OpenRouterClient client, String fileId) {
            super(client);
            this.client = client;
            this.fileId = fileId;
        }

        @Override
        public OpenRouterFileDeleteRequest build() {
            if (fileId == null || fileId.isEmpty()) {
                throw new IllegalStateException("fileId is required to delete a file");
            }
            return new OpenRouterFileDeleteRequest(this);
        }

        @Override
        public OpenRouterFileDeleteResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterFileDeleteResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterFileDeleteResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
