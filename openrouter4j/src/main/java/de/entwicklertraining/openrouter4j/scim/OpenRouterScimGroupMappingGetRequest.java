package de.entwicklertraining.openrouter4j.scim;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to get one SCIM group-to-workspace mapping by id:
 * GET https://openrouter.ai/api/v1/scim/group-mappings/{id}
 *
 * <p>OpenRouter requires a management key for every SCIM endpoint (see the
 * package javadoc of {@link OpenRouterScimGroupMappingsListRequest}).
 */
public final class OpenRouterScimGroupMappingGetRequest extends OpenRouterRequest<OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingGetRequest>> {

    private final OpenRouterClient client;
    private final String id;

    private OpenRouterScimGroupMappingGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
    }

    /** @return the URL-encoded mapping id path segment */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/scim/group-mappings/" + URLEncoder.encode(id, StandardCharsets.UTF_8);
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
    public OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingGetRequest> createResponse(String responseBody) {
        return new OpenRouterScimGroupMappingMutationResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterScimGroupMappingGetRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterScimGroupMappingGetRequest> {

        private final OpenRouterClient client;
        private final String id;

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

        @Override
        public OpenRouterScimGroupMappingGetRequest build() {
            if (id == null || id.isEmpty()) {
                throw new IllegalStateException("id is required to get a SCIM group mapping");
            }
            return new OpenRouterScimGroupMappingGetRequest(this);
        }

        @Override
        public OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingGetRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingGetRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingGetRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
