package de.entwicklertraining.openrouter4j.video;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;

import java.nio.charset.StandardCharsets;

/**
 * Downloads the generated video content of one job:
 * GET https://openrouter.ai/api/v1/videos/{jobId}/content
 *
 * <p>The body is the raw video bytes proxied from the upstream provider and
 * the Content-Type reflects the provider media type (typically
 * {@code video/mp4}). This is a binary endpoint: the request overrides
 * {@code isBinaryResponse()}, so api-base hands the raw bytes to
 * {@link #createResponse(byte[])} instead of trying to parse JSON.
 * <p>
 * Trap: a job whose content is not ready (or already expired) fails with
 * HTTP 400/404/409 - poll the job to {@code completed} first (see
 * {@link OpenRouterVideoGenerationResponse#awaitCompletion()}).
 */
public final class OpenRouterVideoContentRequest
        extends OpenRouterRequest<OpenRouterVideoContentResponse> {

    private final OpenRouterClient client;
    private final String jobId;

    private OpenRouterVideoContentRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.jobId = builder.jobId;
    }

    /** @return the job id ({@code job-...}) whose content is downloaded */
    public String jobId() {
        return jobId;
    }

    @Override
    public String getRelativeUrl() {
        return "/videos/" + jobId + "/content";
    }

    @Override
    public String getHttpMethod() {
        return "GET";
    }

    /**
     * JSON path: none - a GET request with no query parameters.
     *
     * @return an empty body
     */
    @Override
    public String getBody() {
        return "";
    }

    /**
     * This endpoint returns binary video bytes, not JSON - this override
     * makes api-base deliver the body as bytes.
     *
     * @return always {@code true}
     */
    @Override
    public boolean isBinaryResponse() {
        return true;
    }

    /**
     * Stores the delivered video bytes in the typed response.
     *
     * @param contentBytes the raw video bytes
     * @return the typed response holding the bytes
     */
    @Override
    public OpenRouterVideoContentResponse createResponse(byte[] contentBytes) {
        return new OpenRouterVideoContentResponse(contentBytes, this);
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
    public OpenRouterVideoContentResponse createResponse(String responseBody) {
        return new OpenRouterVideoContentResponse(
                responseBody == null ? null : responseBody.getBytes(StandardCharsets.UTF_8), this);
    }

    /**
     * Starting point for building a {@link OpenRouterVideoContentRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterVideoContentRequest> {

        private final OpenRouterClient client;
        private final String jobId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param jobId the job id ({@code job-...}) whose content to download
         */
        public Builder(OpenRouterClient client, String jobId) {
            super(client);
            this.client = client;
            this.jobId = jobId;
        }

        @Override
        public OpenRouterVideoContentRequest build() {
            if (jobId == null || jobId.isEmpty()) {
                throw new IllegalStateException("jobId is required for a video content request");
            }
            return new OpenRouterVideoContentRequest(this);
        }

        @Override
        public OpenRouterVideoContentResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterVideoContentResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterVideoContentResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
