package de.entwicklertraining.openrouter4j.scim;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to start a SCIM directory sync:
 * POST https://openrouter.ai/api/v1/scim/sync-jobs
 *
 * <p>The endpoint answers HTTP 202 with the created job; poll its status
 * with {@link OpenRouterScimSyncJobGetRequest}.
 *
 * <p>OpenRouter requires a management key for every SCIM endpoint (see the
 * package javadoc of {@link OpenRouterScimGroupMappingsListRequest}).
 */
public final class OpenRouterScimSyncJobCreateRequest extends OpenRouterRequest<OpenRouterScimSyncJobResponse<OpenRouterScimSyncJobCreateRequest>> {

    private final OpenRouterClient client;

    private OpenRouterScimSyncJobCreateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
    }

    @Override
    public String getRelativeUrl() {
        return "/scim/sync-jobs";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * The schema defines no body fields; an empty JSON object is sent so the
     * request carries a valid JSON body.
     *
     * @return the JSON body (empty object)
     */
    @Override
    public String getBody() {
        return new JSONObject().toString();
    }

    @Override
    public OpenRouterScimSyncJobResponse<OpenRouterScimSyncJobCreateRequest> createResponse(String responseBody) {
        return new OpenRouterScimSyncJobResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterScimSyncJobCreateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterScimSyncJobCreateRequest> {

        private final OpenRouterClient client;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         */
        public Builder(OpenRouterClient client) {
            super(client);
            this.client = client;
        }

        @Override
        public OpenRouterScimSyncJobCreateRequest build() {
            return new OpenRouterScimSyncJobCreateRequest(this);
        }

        @Override
        public OpenRouterScimSyncJobResponse<OpenRouterScimSyncJobCreateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterScimSyncJobResponse<OpenRouterScimSyncJobCreateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterScimSyncJobResponse<OpenRouterScimSyncJobCreateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
