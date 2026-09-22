package de.entwicklertraining.openrouter4j.video;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;

import java.net.URLEncoder;
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
 * {@link OpenRouterVideoGenerationResponse#awaitCompletion(OpenRouterClient)}).
 *
 * <p>Multi-output jobs: the optional {@code index} query parameter selects
 * which of the job's generated videos is downloaded when the generation
 * produced more than one output (API default 0). The parameter matters only
 * for multi-output jobs - a single-output job ignores it and always returns
 * its only video.
 */
public final class OpenRouterVideoContentRequest
        extends OpenRouterRequest<OpenRouterVideoContentResponse> {

    private final OpenRouterClient client;
    private final String jobId;
    private final Integer index;

    private OpenRouterVideoContentRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.jobId = builder.jobId;
        this.index = builder.index;
    }

    /** @return the job id ({@code job-...}) whose content is downloaded */
    public String jobId() {
        return jobId;
    }

    /**
     * JSON/query key: {@code index} (GET /videos/{jobId}/content).
     *
     * @return the selected output index, or {@code null} when unset (the API
     *         then uses its default 0)
     */
    public Integer index() {
        return index;
    }

    @Override
    public String getRelativeUrl() {
        String url = "/videos/" + encode(jobId) + "/content";
        if (index != null) {
            url += "?index=" + URLEncoder.encode(String.valueOf(index), StandardCharsets.UTF_8);
        }
        return url;
    }

    private static String encode(String segment) {
        if (segment == null) {
            return "";
        }
        // URLEncoder is form-encoding; path segments must keep "/" out anyway,
        // so encoding it is the correct behaviour for a job id.
        return URLEncoder.encode(segment, StandardCharsets.UTF_8);
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
        private Integer index;

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

        /**
         * Sets the query key {@code index} - the zero-based index of the
         * generated video to download when the job produced more than one
         * output (API default 0, minimum 0, rejected loudly below that).
         * Trap: a single-output job ignores the parameter and always returns
         * its only video.
         *
         * @param index the zero-based output index
         * @return this builder
         */
        public Builder index(Integer index) {
            if (index != null && index < 0) {
                throw new IllegalArgumentException("index must be >= 0, got: " + index);
            }
            this.index = index;
            return this;
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
