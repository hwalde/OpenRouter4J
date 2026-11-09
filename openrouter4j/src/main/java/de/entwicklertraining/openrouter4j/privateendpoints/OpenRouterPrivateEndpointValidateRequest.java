package de.entwicklertraining.openrouter4j.privateendpoints;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to run a live validation of a draft private endpoint:
 * POST https://openrouter.ai/api/v1/private-endpoints/{id}/validate
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * and the private endpoints entitlement for this route. The route sends a
 * live test request to the draft endpoint using the given workspace's BYOK
 * credential for its provider; a passing validation is required before
 * {@code POST /private-endpoints/{id}/activate}.
 *
 * <p>Traps: failed checks are <b>not</b> an error - the route answers HTTP
 * 200 with {@code passed: false} and the failed checks. The body's
 * {@code workspace_id} is required (a UUID; the workspace must belong to
 * your account - otherwise 404 {@code workspace_not_found} or a check
 * failing with that reason). The optional {@code parameters} probes are
 * absent on a failed run and for providers that run no parameter probes;
 * supported ones are recorded in the endpoint's {@code tested_parameters}.
 */
public final class OpenRouterPrivateEndpointValidateRequest
        extends OpenRouterRequest<OpenRouterPrivateEndpointValidationResponse<OpenRouterPrivateEndpointValidateRequest>> {

    private final OpenRouterClient client;
    private final String id;
    private final String workspaceId;

    private OpenRouterPrivateEndpointValidateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.workspaceId = builder.workspaceId;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    /**
     * @return the workspace id whose BYOK credential performs the live test
     *         call
     */
    public String workspaceId() {
        return workspaceId;
    }

    @Override
    public String getRelativeUrl() {
        return "/private-endpoints/" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "/validate";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * Builds the JSON body: the required {@code workspace_id}.
     *
     * @return the JSON body
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("workspace_id", workspaceId);
        return body.toString();
    }

    @Override
    public OpenRouterPrivateEndpointValidationResponse<OpenRouterPrivateEndpointValidateRequest> createResponse(
            String responseBody) {
        return new OpenRouterPrivateEndpointValidationResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterPrivateEndpointValidateRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterPrivateEndpointValidateRequest> {

        private final OpenRouterClient client;
        private final String id;
        private final String workspaceId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param id the id (UUID) of the private endpoint (validated loudly;
         *        URL-encoded into the path)
         * @param workspaceId the workspace id (UUID) whose BYOK credential
         *        performs the live test call (required body field,
         *        validated loudly; the workspace must belong to your account)
         */
        public Builder(OpenRouterClient client, String id, String workspaceId) {
            super(client);
            this.client = client;
            OpenRouterPrivateEndpointRules.requireNonBlank(id, "id");
            OpenRouterPrivateEndpointRules.requireNonBlank(workspaceId, "workspace_id");
            this.id = id;
            this.workspaceId = workspaceId;
        }

        @Override
        public OpenRouterPrivateEndpointValidateRequest build() {
            return new OpenRouterPrivateEndpointValidateRequest(this);
        }

        @Override
        public OpenRouterPrivateEndpointValidationResponse<OpenRouterPrivateEndpointValidateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPrivateEndpointValidationResponse<OpenRouterPrivateEndpointValidateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPrivateEndpointValidationResponse<OpenRouterPrivateEndpointValidateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
