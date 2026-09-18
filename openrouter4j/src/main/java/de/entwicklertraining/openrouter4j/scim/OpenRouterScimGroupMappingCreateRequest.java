package de.entwicklertraining.openrouter4j.scim;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to create a SCIM group-to-workspace mapping:
 * POST https://openrouter.ai/api/v1/scim/group-mappings
 *
 * <p>Body (all required, validated loudly here): {@code role}
 * ({@code admin} or {@code member}), {@code scim_group_id} and
 * {@code workspace_id} (both UUIDs).
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * - a normal inference key is rejected with an authorization error (the
 * schema documents HTTP 401; observed rejection codes vary).
 *
 * <p><b>409 trap:</b> re-creating the same mapping with the same role
 * succeeds and re-applies it; creating an existing mapping with a
 * <em>different</em> role is rejected with HTTP 409 Conflict. Use
 * {@link OpenRouterScimGroupMappingUpdateRequest} to change a mapping's
 * role.
 */
public final class OpenRouterScimGroupMappingCreateRequest extends OpenRouterRequest<OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingCreateRequest>> {

    private final OpenRouterClient client;
    private final String role;
    private final String scimGroupId;
    private final String workspaceId;

    private OpenRouterScimGroupMappingCreateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.role = builder.role;
        this.scimGroupId = builder.scimGroupId;
        this.workspaceId = builder.workspaceId;
    }

    /** @return the configured role */
    public String role() {
        return role;
    }

    /** @return the configured {@code scim_group_id} */
    public String scimGroupId() {
        return scimGroupId;
    }

    /** @return the configured {@code workspace_id} */
    public String workspaceId() {
        return workspaceId;
    }

    @Override
    public String getRelativeUrl() {
        return "/scim/group-mappings";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * Builds the JSON body with only the explicitly configured fields
     * (an unset option never appears in the JSON).
     *
     * @return the JSON body
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        if (role != null) {
            body.put("role", role);
        }
        if (scimGroupId != null) {
            body.put("scim_group_id", scimGroupId);
        }
        if (workspaceId != null) {
            body.put("workspace_id", workspaceId);
        }
        return body.toString();
    }

    @Override
    public OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingCreateRequest> createResponse(String responseBody) {
        return new OpenRouterScimGroupMappingMutationResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterScimGroupMappingCreateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterScimGroupMappingCreateRequest> {

        private final OpenRouterClient client;
        private String role;
        private String scimGroupId;
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
         * Sets the body field {@code role} (required) - the role the SCIM
         * group receives in the workspace ({@code admin} or {@code member};
         * validated loudly).
         *
         * @param role the mapping role
         * @return this builder
         */
        public Builder role(String role) {
            if (role != null && !"admin".equals(role) && !"member".equals(role)) {
                throw new IllegalArgumentException("role must be \"admin\" or \"member\", got: " + role);
            }
            this.role = role;
            return this;
        }

        /**
         * Sets the body field {@code scim_group_id} (required) - the UUID of
         * the SCIM group (see {@link OpenRouterScimGroupsListRequest}).
         *
         * @param scimGroupId the SCIM group UUID
         * @return this builder
         */
        public Builder scimGroupId(String scimGroupId) {
            this.scimGroupId = scimGroupId;
            return this;
        }

        /**
         * Sets the body field {@code workspace_id} (required) - the UUID of
         * the workspace.
         *
         * @param workspaceId the workspace UUID
         * @return this builder
         */
        public Builder workspaceId(String workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        @Override
        public OpenRouterScimGroupMappingCreateRequest build() {
            if (role == null || role.isEmpty()) {
                throw new IllegalStateException("role is required to create a SCIM group mapping");
            }
            if (scimGroupId == null || scimGroupId.isEmpty()) {
                throw new IllegalStateException("scimGroupId is required to create a SCIM group mapping");
            }
            if (workspaceId == null || workspaceId.isEmpty()) {
                throw new IllegalStateException("workspaceId is required to create a SCIM group mapping");
            }
            return new OpenRouterScimGroupMappingCreateRequest(this);
        }

        @Override
        public OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingCreateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingCreateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingCreateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
