package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to delete an intern - the safe teardown of the intern, its
 * runtime and its vault:
 * DELETE https://openrouter.ai/api/v1/interns/{internId}
 *
 * <p>Answers 202 with {@code deleting}. The optional
 * {@code acknowledge_workspace_loss} consent (default {@code false}) refuses
 * the teardown while a workspace archive is missing - set it with
 * {@link Builder#acknowledgeWorkspaceLoss(boolean)} to delete anyway.
 */
public final class OpenRouterInternDeleteRequest
        extends OpenRouterRequest<OpenRouterInternLifecycleResponse<OpenRouterInternDeleteRequest>> {

    private final OpenRouterClient client;
    private final String internId;
    private final Boolean acknowledgeWorkspaceLoss;

    private OpenRouterInternDeleteRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.internId = builder.internId;
        this.acknowledgeWorkspaceLoss = builder.acknowledgeWorkspaceLoss;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String internId() {
        return URLEncoder.encode(internId, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/interns/" + URLEncoder.encode(internId, StandardCharsets.UTF_8);
    }

    @Override
    public String getHttpMethod() {
        return "DELETE";
    }

    /**
     * The JSON body: {@code acknowledge_workspace_loss} only when explicitly
     * configured.
     *
     * @return the JSON body string, or {@code null} when no consent was set
     */
    @Override
    public String getBody() {
        if (acknowledgeWorkspaceLoss == null) {
            return null;
        }
        return new JSONObject()
                .put("acknowledge_workspace_loss", acknowledgeWorkspaceLoss)
                .toString();
    }

    @Override
    public OpenRouterInternLifecycleResponse<OpenRouterInternDeleteRequest> createResponse(
            String responseBody) {
        return new OpenRouterInternLifecycleResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterInternDeleteRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterInternDeleteRequest> {

        private final OpenRouterClient client;
        private final String internId;
        private Boolean acknowledgeWorkspaceLoss;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param internId the id (UUID) of the intern
         */
        public Builder(OpenRouterClient client, String internId) {
            super(client);
            this.client = client;
            this.internId = internId;
        }

        /**
         * Sets the JSON field {@code acknowledge_workspace_loss} - delete
         * even though the workspace archive was not confirmed. Default
         * {@code false}, which refuses the teardown while a workspace
         * archive is missing.
         *
         * @param acknowledge true to delete despite a missing archive
         * @return this builder
         */
        public Builder acknowledgeWorkspaceLoss(boolean acknowledge) {
            this.acknowledgeWorkspaceLoss = acknowledge;
            return this;
        }

        @Override
        public OpenRouterInternDeleteRequest build() {
            return new OpenRouterInternDeleteRequest(this);
        }

        @Override
        public OpenRouterInternLifecycleResponse<OpenRouterInternDeleteRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterInternLifecycleResponse<OpenRouterInternDeleteRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterInternLifecycleResponse<OpenRouterInternDeleteRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
