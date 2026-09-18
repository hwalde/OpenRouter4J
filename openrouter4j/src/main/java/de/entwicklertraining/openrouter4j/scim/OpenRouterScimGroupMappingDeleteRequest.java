package de.entwicklertraining.openrouter4j.scim;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to delete a SCIM group-to-workspace mapping:
 * DELETE https://openrouter.ai/api/v1/scim/group-mappings/{id}
 *
 * <p>Query parameter {@code keep_members}: when {@code true} (the API
 * default), removing the mapping keeps the workspace members that arrived
 * through the SCIM group; when {@code false}, those members lose their
 * workspace access.
 */
public final class OpenRouterScimGroupMappingDeleteRequest extends OpenRouterRequest<OpenRouterScimGroupMappingDeleteResponse> {

    private final OpenRouterClient client;
    private final String id;
    private final Boolean keepMembers;

    private OpenRouterScimGroupMappingDeleteRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.keepMembers = builder.keepMembers;
    }

    /** @return the URL-encoded mapping id path segment */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder url = new StringBuilder("/scim/group-mappings/")
                .append(URLEncoder.encode(id, StandardCharsets.UTF_8));
        if (keepMembers != null) {
            url.append("?keep_members=").append(keepMembers);
        }
        return url.toString();
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
    public OpenRouterScimGroupMappingDeleteResponse createResponse(String responseBody) {
        return new OpenRouterScimGroupMappingDeleteResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterScimGroupMappingDeleteRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterScimGroupMappingDeleteRequest> {

        private final OpenRouterClient client;
        private final String id;
        private Boolean keepMembers;

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
         * Sets the {@code keep_members} query parameter - whether the
         * workspace members that arrived through the SCIM group keep their
         * access after the mapping is deleted (API default {@code true}).
         *
         * @param keepMembers whether members keep their access
         * @return this builder
         */
        public Builder keepMembers(Boolean keepMembers) {
            this.keepMembers = keepMembers;
            return this;
        }

        @Override
        public OpenRouterScimGroupMappingDeleteRequest build() {
            if (id == null || id.isEmpty()) {
                throw new IllegalStateException("id is required to delete a SCIM group mapping");
            }
            return new OpenRouterScimGroupMappingDeleteRequest(this);
        }

        @Override
        public OpenRouterScimGroupMappingDeleteResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterScimGroupMappingDeleteResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterScimGroupMappingDeleteResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
