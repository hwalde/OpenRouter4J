package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to fetch the metadata of one file:
 * GET https://openrouter.ai/api/v1/files/{file_id}
 *
 * <p>The response document carries the negotiated shape (see
 * {@link OpenRouterFile#shape()} for the three shapes).
 */
public final class OpenRouterFileGetRequest
        extends OpenRouterRequest<OpenRouterFileGetResponse> {

    private final OpenRouterClient client;
    private final String fileId;

    private OpenRouterFileGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.fileId = builder.fileId;
    }

    /** @return the file id ({@code or_file_...} or {@code file-...}) whose metadata is read */
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
    public OpenRouterFileGetResponse createResponse(String responseBody) {
        return new OpenRouterFileGetResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterFileGetRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterFileGetRequest> {

        private final OpenRouterClient client;
        private final String fileId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param fileId the file id ({@code or_file_...} or {@code file-...}) whose metadata to read
         */
        public Builder(OpenRouterClient client, String fileId) {
            super(client);
            this.client = client;
            this.fileId = fileId;
        }

        @Override
        public OpenRouterFileGetRequest build() {
            if (fileId == null || fileId.isEmpty()) {
                throw new IllegalStateException("fileId is required to fetch a file");
            }
            return new OpenRouterFileGetRequest(this);
        }

        @Override
        public OpenRouterFileGetResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterFileGetResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterFileGetResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
