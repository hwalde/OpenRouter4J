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
 */
public final class OpenRouterFileContentRequest
        extends OpenRouterRequest<OpenRouterFileContentResponse> {

    private final OpenRouterClient client;
    private final String fileId;

    private OpenRouterFileContentRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.fileId = builder.fileId;
    }

    /** @return the file id whose content is downloaded */
    public String fileId() {
        return fileId;
    }

    @Override
    public String getRelativeUrl() {
        return "/files/" + encode(fileId) + "/content";
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
