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
 *
 * <p>Storage scope: in a multi-workspace or multi-provider setup the optional
 * {@code workspace_id} / {@code provider} query parameters address a file in
 * a non-default scope. Trap: without them the request always hits the
 * default scope, so a file stored in another workspace or with another
 * provider answers 404 here even though it exists.
 */
public final class OpenRouterFileGetRequest
        extends OpenRouterRequest<OpenRouterFileGetResponse> {

    private final OpenRouterClient client;
    private final String fileId;
    private final String workspaceId;
    private final String provider;

    private OpenRouterFileGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.fileId = builder.fileId;
        this.workspaceId = builder.workspaceId;
        this.provider = builder.provider;
    }

    /** @return the file id ({@code or_file_...} or {@code file-...}) whose metadata is read */
    public String fileId() {
        return fileId;
    }

    /**
     * JSON/query key: {@code workspace_id} (GET /files/{file_id}).
     *
     * @return the workspace scope, or {@code null} when unset (default scope)
     */
    public String workspaceId() {
        return workspaceId;
    }

    /**
     * JSON/query key: {@code provider} (GET /files/{file_id}).
     *
     * @return the storage-provider scope, or {@code null} when unset (default scope)
     */
    public String provider() {
        return provider;
    }

    @Override
    public String getRelativeUrl() {
        String url = "/files/" + encode(fileId);
        String query = query();
        return query.isEmpty() ? url : url + "?" + query;
    }

    private String query() {
        StringBuilder sb = new StringBuilder();
        appendQueryParam(sb, "workspace_id", workspaceId);
        appendQueryParam(sb, "provider", provider);
        return sb.toString();
    }

    private static void appendQueryParam(StringBuilder sb, String key, String value) {
        if (value == null) {
            return;
        }
        if (sb.length() > 0) {
            sb.append('&');
        }
        sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
        sb.append('=');
        sb.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
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
        private String workspaceId;
        private String provider;

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

        /**
         * Sets the query key {@code workspace_id} - reads the file metadata in
         * this workspace's storage scope instead of the default scope.
         * Mirrors the semantics of the upload/list operations
         * ({@code client.files().upload()} / {@code client.files().list()}).
         * The parameter is appended to the query string only when set.
         *
         * @param workspaceId the workspace id
         * @return this builder
         */
        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        /**
         * Sets the query key {@code provider} - reads the file metadata in the
         * scope of this storage provider (the API documents {@code openai}
         * and {@code anthropic}, but the value is free-form: unknown values
         * are not rejected by the builder and are passed through verbatim).
         * Mirrors the semantics of the upload/list operations. The parameter
         * is appended to the query string only when set.
         *
         * @param provider the storage provider
         * @return this builder
         */
        public Builder provider(String provider) {
            this.provider = provider;
            return this;
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
