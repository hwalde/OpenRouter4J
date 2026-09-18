package de.entwicklertraining.openrouter4j.scim;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to update a SCIM group-to-workspace mapping:
 * PATCH https://openrouter.ai/api/v1/scim/group-mappings/{id}
 *
 * <p>Body: {@code role} only ({@code admin} or {@code member}; required and
 * validated loudly). The mapping id stays the same; the role is what
 * changes. Trap: changing an existing mapping's role via re-creating it
 * (POST) is rejected with 409 - use this endpoint instead.
 */
public final class OpenRouterScimGroupMappingUpdateRequest extends OpenRouterRequest<OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingUpdateRequest>> {

    private final OpenRouterClient client;
    private final String id;
    private final String role;

    private OpenRouterScimGroupMappingUpdateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.role = builder.role;
    }

    /** @return the URL-encoded mapping id path segment */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    /** @return the configured role */
    public String role() {
        return role;
    }

    @Override
    public String getRelativeUrl() {
        return "/scim/group-mappings/" + URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getHttpMethod() {
        return "PATCH";
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
        return body.toString();
    }

    @Override
    public OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingUpdateRequest> createResponse(String responseBody) {
        return new OpenRouterScimGroupMappingMutationResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterScimGroupMappingUpdateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterScimGroupMappingUpdateRequest> {

        private final OpenRouterClient client;
        private final String id;
        private String role;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param id the mapping id (UUID)
         */
        public Builder(OpenRouterClient client, String id) {
            super(client);
            this.client = client;
            this.id = id;
        }

        /**
         * Sets the body field {@code role} (required) - the new role of the
         * mapping ({@code admin} or {@code member}; validated loudly).
         *
         * @param role the new mapping role
         * @return this builder
         */
        public Builder role(String role) {
            if (role != null && !"admin".equals(role) && !"member".equals(role)) {
                throw new IllegalArgumentException("role must be \"admin\" or \"member\", got: " + role);
            }
            this.role = role;
            return this;
        }

        @Override
        public OpenRouterScimGroupMappingUpdateRequest build() {
            if (id == null || id.isEmpty()) {
                throw new IllegalStateException("id is required to update a SCIM group mapping");
            }
            if (role == null || role.isEmpty()) {
                throw new IllegalStateException("role is required to update a SCIM group mapping");
            }
            return new OpenRouterScimGroupMappingUpdateRequest(this);
        }

        @Override
        public OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingUpdateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingUpdateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingUpdateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
