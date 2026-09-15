package de.entwicklertraining.openrouter4j.video;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * Polls the status of one async video generation job:
 * GET https://openrouter.ai/api/v1/videos/{jobId}
 *
 * <p>The response is the same {@link OpenRouterVideoGenerationResponse} shape
 * as the submission: once {@code status} is {@code completed}, the
 * {@code unsigned_urls} and the {@code usage} are present. The
 * {@link OpenRouterVideoGenerationResponse#awaitCompletion()} helper wraps
 * repeated executions of this request.
 */
public final class OpenRouterVideoJobRequest
        extends OpenRouterRequest<OpenRouterVideoGenerationResponse<OpenRouterVideoJobRequest>> {

    private final String jobId;

    private OpenRouterVideoJobRequest(Builder builder) {
        super(builder);
        this.jobId = builder.jobId;
    }

    /** @return the job id ({@code job-...}) polled by this request */
    public String jobId() {
        return jobId;
    }

    @Override
    public String getRelativeUrl() {
        return "/videos/" + jobId;
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

    @Override
    public OpenRouterVideoGenerationResponse<OpenRouterVideoJobRequest> createResponse(String responseBody) {
        return new OpenRouterVideoGenerationResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterVideoJobRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterVideoJobRequest> {

        private final OpenRouterClient client;
        private final String jobId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param jobId the job id ({@code job-...}) to poll
         */
        public Builder(OpenRouterClient client, String jobId) {
            super(client);
            this.client = client;
            this.jobId = jobId;
        }

        @Override
        public OpenRouterVideoJobRequest build() {
            if (jobId == null || jobId.isEmpty()) {
                throw new IllegalStateException("jobId is required for a video job request");
            }
            return new OpenRouterVideoJobRequest(this);
        }

        @Override
        public OpenRouterVideoGenerationResponse<OpenRouterVideoJobRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterVideoGenerationResponse<OpenRouterVideoJobRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterVideoGenerationResponse<OpenRouterVideoJobRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
