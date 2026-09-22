package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to download the content of one file:
 * GET https://openrouter.ai/api/v1/files/{file_id}/content
 *
 * <p>The body is the raw file bytes proxied from the upstream storage. This
 * is a binary endpoint: the request overrides {@code isBinaryResponse()}, so
 * api-base hands the raw bytes to {@link #createResponse(byte[])} instead of
 * trying to parse JSON.
 * <p>
 * Trap: the file must be downloadable (see
 * {@link OpenRouterFile#downloadable()}); a missing or non-downloadable file
 * fails with HTTP 400/404.
 *
 * <p>Storage scope: the optional {@code workspace_id} / {@code provider}
 * query parameters download a file from a non-default scope. Trap: without
 * them the request always hits the default scope, so a file stored in
 * another workspace or with another provider answers 404 here even though
 * it exists.
 */
public final class OpenRouterFileContentRequest
        extends OpenRouterRequest<OpenRouterFileContentResponse> {

    private final OpenRouterClient client;
    private final String fileId;
    private final String workspaceId;
    private final String provider;

    private OpenRouterFileContentRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.fileId = builder.fileId;
        this.workspaceId = builder.workspaceId;
        this.provider = builder.provider;
    }

    /** @return the file id whose content is downloaded */
    public String fileId() {
        return fileId;
    }

    /**
     * JSON/query key: {@code workspace_id} (GET /files/{file_id}/content).
     *
     * @return the workspace scope, or {@code null} when unset (default scope)
     */
    public String workspaceId() {
        return workspaceId;
    }

    /**
     * JSON/query key: {@code provider} (GET /files/{file_id}/content).
     *
     * @return the storage-provider scope, or {@code null} when unset (default scope)
     */
    public String provider() {
        return provider;
    }

    @Override
    public String getRelativeUrl() {
        String url = "/files/" + encode(fileId) + "/content";
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

    /**
     * This endpoint returns binary file bytes, not JSON - this override
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
    public OpenRouterFileContentResponse createResponse(byte[] contentBytes) {
        return new OpenRouterFileContentResponse(contentBytes, this);
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
    public OpenRouterFileContentResponse createResponse(String responseBody) {
        return new OpenRouterFileContentResponse(
                responseBody == null ? null : responseBody.getBytes(StandardCharsets.UTF_8), this);
    }

    /**
     * Starting point for building a {@link OpenRouterFileContentRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterFileContentRequest> {

        private final OpenRouterClient client;
        private final String fileId;
        private String workspaceId;
        private String provider;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param fileId the file id whose content to download
         */
        public Builder(OpenRouterClient client, String fileId) {
            super(client);
            this.client = client;
            this.fileId = fileId;
        }

        /**
         * Sets the query key {@code workspace_id} - downloads the file from
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
         * Sets the query key {@code provider} - downloads the file from the
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
        public OpenRouterFileContentRequest build() {
            if (fileId == null || fileId.isEmpty()) {
                throw new IllegalStateException("fileId is required to download a file");
            }
            return new OpenRouterFileContentRequest(this);
        }

        @Override
        public OpenRouterFileContentResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterFileContentResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterFileContentResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
