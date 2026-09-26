package de.entwicklertraining.openrouter4j.batches;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to delete a terminal batch and purge every request and result
 * artifact OpenRouter holds for it:
 * DELETE https://openrouter.ai/api/v1/batches/:id
 *
 * <p>Deletion is only allowed once the batch is terminal ({@code completed},
 * {@code failed}, {@code expired}, {@code cancelled}) - an in-flight batch
 * answers {@code 409}. It is not cancellation (there is no documented
 * cancel operation) and does not just skip the 30-day retention window: a
 * {@code 200} means every applicable cleanup has completed and a later GET
 * or DELETE of the same id answers {@code 404}. A partial cleanup failure
 * comes back as a retryable {@code 5xx}; repeating the request resumes where
 * it left off. Deleting a BYOK batch that needs upstream cleanup requires
 * the provider key it was submitted with to still be enabled, or the
 * request answers {@code 409} and leaves the batch untouched. Billing,
 * generation and audit records are retained.
 */
public final class OpenRouterBatchDeleteRequest
        extends OpenRouterRequest<OpenRouterBatchDeleteResponse> {

    private final OpenRouterClient client;
    private final String batchId;

    private OpenRouterBatchDeleteRequest(Builder builder) {
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
    public OpenRouterBatchDeleteResponse createResponse(String responseBody) {
        return new OpenRouterBatchDeleteResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterBatchDeleteRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterBatchDeleteRequest> {

        private final OpenRouterClient client;
        private final String batchId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param batchId the id of the batch to delete
         */
        public Builder(OpenRouterClient client, String batchId) {
            super(client);
            this.client = client;
            this.batchId = batchId;
        }

        @Override
        public OpenRouterBatchDeleteRequest build() {
            if (batchId == null || batchId.isEmpty()) {
                throw new IllegalStateException("batchId is required to delete a batch");
            }
            return new OpenRouterBatchDeleteRequest(this);
        }

        @Override
        public OpenRouterBatchDeleteResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterBatchDeleteResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterBatchDeleteResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
