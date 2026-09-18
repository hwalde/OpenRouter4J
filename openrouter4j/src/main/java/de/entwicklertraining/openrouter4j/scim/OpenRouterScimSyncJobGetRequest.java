package de.entwicklertraining.openrouter4j.scim;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to poll the status of a SCIM directory sync:
 * GET https://openrouter.ai/api/v1/scim/sync-jobs/{id}
 *
 * <p>OpenRouter requires a management key for every SCIM endpoint (see the
 * package javadoc of {@link OpenRouterScimGroupMappingsListRequest}).
 */
public final class OpenRouterScimSyncJobGetRequest extends OpenRouterRequest<OpenRouterScimSyncJobResponse<OpenRouterScimSyncJobGetRequest>> {

    private final OpenRouterClient client;
    private final String id;

    private OpenRouterScimSyncJobGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
    }

    /** @return the URL-encoded sync-job id path segment */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/scim/sync-jobs/" + URLEncoder.encode(id, StandardCharsets.UTF_8);
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
    public OpenRouterScimSyncJobResponse<OpenRouterScimSyncJobGetRequest> createResponse(String responseBody) {
        return new OpenRouterScimSyncJobResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterScimSyncJobGetRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterScimSyncJobGetRequest> {

        private final OpenRouterClient client;
        private final String id;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param id the sync-job id (UUID)
         */
        public Builder(OpenRouterClient client, String id) {
            super(client);
            this.client = client;
            this.id = id;
        }

        @Override
        public OpenRouterScimSyncJobGetRequest build() {
            if (id == null || id.isEmpty()) {
                throw new IllegalStateException("id is required to get a SCIM sync job");
            }
            return new OpenRouterScimSyncJobGetRequest(this);
        }

        @Override
        public OpenRouterScimSyncJobResponse<OpenRouterScimSyncJobGetRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterScimSyncJobResponse<OpenRouterScimSyncJobGetRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterScimSyncJobResponse<OpenRouterScimSyncJobGetRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
