package de.entwicklertraining.openrouter4j.batches;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to poll one batch for its current status and, once completed,
 * its inline results:
 * GET https://openrouter.ai/api/v1/batches/:id
 *
 * <p>There is no separate results-download endpoint - a completed batch
 * carries its {@code results} array in this answer. List items
 * ({@code GET /api/v1/batches}) never do, so this is the call to make when
 * you need the results. Poll until the batch reaches a terminal status
 * ({@code completed}, {@code failed}, {@code expired}, {@code cancelled}).
 */
public final class OpenRouterBatchGetRequest
        extends OpenRouterRequest<OpenRouterBatchResponse<OpenRouterBatchGetRequest>> {

    private final OpenRouterClient client;
    private final String batchId;

    private OpenRouterBatchGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.batchId = builder.batchId;
    }

    /**
     * @return the batch id (URL-encoded in the path)
     */
    public String batchId() {
        return batchId;
    }

    @Override
    public String getRelativeUrl() {
        return "/batches/" + URLEncoder.encode(batchId, StandardCharsets.UTF_8);
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
    public OpenRouterBatchResponse<OpenRouterBatchGetRequest> createResponse(String responseBody) {
        return new OpenRouterBatchResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterBatchGetRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterBatchGetRequest> {

        private final OpenRouterClient client;
        private final String batchId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param batchId the id of the batch to poll
         */
        public Builder(OpenRouterClient client, String batchId) {
            super(client);
            this.client = client;
            this.batchId = batchId;
        }

        @Override
        public OpenRouterBatchGetRequest build() {
            if (batchId == null || batchId.isEmpty()) {
                throw new IllegalStateException("batchId is required to poll a batch");
            }
            return new OpenRouterBatchGetRequest(this);
        }

        @Override
        public OpenRouterBatchResponse<OpenRouterBatchGetRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterBatchResponse<OpenRouterBatchGetRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterBatchResponse<OpenRouterBatchGetRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
