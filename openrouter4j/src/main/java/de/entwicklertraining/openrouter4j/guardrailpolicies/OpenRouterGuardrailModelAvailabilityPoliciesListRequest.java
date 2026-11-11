package de.entwicklertraining.openrouter4j.guardrailpolicies;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to list the model-availability guardrail policies of one workspace:
 * 
 * GET https://openrouter.ai/api/v1/guardrails/policies/model-availability
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 */
public final class OpenRouterGuardrailModelAvailabilityPoliciesListRequest extends OpenRouterRequest<OpenRouterGuardrailPoliciesListResponse<OpenRouterGuardrailModelAvailabilityPoliciesListRequest>> {

    private final OpenRouterClient client;
    private final Integer offset;
    private final Integer limit;
    private final String workspaceId;

    private OpenRouterGuardrailModelAvailabilityPoliciesListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.offset = builder.offset;
        this.limit = builder.limit;
        this.workspaceId = builder.workspaceId;
    }


    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/guardrails/policies/model-availability");
        boolean first = true;
        if (offset != null) {
            sb.append(first ? '?' : '&').append("offset=").append(offset.intValue());
            first = false;
        }
        if (limit != null) {
            sb.append(first ? '?' : '&').append("limit=").append(limit.intValue());
            first = false;
        }
        if (workspaceId != null) {
            sb.append(first ? '?' : '&').append("workspace_id=")
                    .append(URLEncoder.encode(workspaceId, StandardCharsets.UTF_8));
            first = false;
        }
        return sb.toString();
    }

    @Override
    public String getHttpMethod() {
        return "GET";
    }

    /**
     * This request carries no body.
     *
     * @return always {@code null}
     */
    @Override
    public String getBody() {
        return null;
    }
    @Override
    public OpenRouterGuardrailPoliciesListResponse<OpenRouterGuardrailModelAvailabilityPoliciesListRequest> createResponse(String responseBody) {
        return new OpenRouterGuardrailPoliciesListResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGuardrailModelAvailabilityPoliciesListRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGuardrailModelAvailabilityPoliciesListRequest> {

        private final OpenRouterClient client;
        private Integer offset;
        private Integer limit;
        private String workspaceId;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         */
        public Builder(OpenRouterClient client) {
            super(client);
            this.client = client;
        }


    /**
     * * Sets the query parameter {@code offset}: number of records to skip
     *          * for pagination. When both {@code offset} and {@code limit} are
     *          * omitted, the full list is returned. {@code null} unsets the
     *          * parameter.
     */
        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

    /**
     * * Sets the query parameter {@code limit}: maximum number of records
     *          * to return (1-100). When both {@code offset} and {@code limit} are
     *          * omitted, the full list is returned. {@code null} unsets the
     *          * parameter.
     */
        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

    /**
     * * Sets the query parameter {@code workspace_id} (required): the
     *          * workspace the policy belongs to. Validated loudly at
     *          * {@link #build()}.
     */
        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        @Override
        public OpenRouterGuardrailModelAvailabilityPoliciesListRequest build() {
        if (workspaceId == null || workspaceId.isEmpty()) {
            throw new IllegalStateException("workspace_id is required");
        }
        if (limit != null && (limit.intValue() < 1 || limit.intValue() > 100)) {
            throw new IllegalStateException("limit must be between 1 and 100");
        }
        if (offset != null && offset.intValue() < 0) {
            throw new IllegalStateException("offset must be at least 0");
        }
            return new OpenRouterGuardrailModelAvailabilityPoliciesListRequest(this);
        }

        @Override
        public OpenRouterGuardrailPoliciesListResponse<OpenRouterGuardrailModelAvailabilityPoliciesListRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterGuardrailPoliciesListResponse<OpenRouterGuardrailModelAvailabilityPoliciesListRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterGuardrailPoliciesListResponse<OpenRouterGuardrailModelAvailabilityPoliciesListRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
