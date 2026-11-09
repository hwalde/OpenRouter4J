package de.entwicklertraining.openrouter4j.privateendpoints;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to delete a private endpoint and stop routing to it:
 * DELETE https://openrouter.ai/api/v1/private-endpoints/{id}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * and the private endpoints entitlement for this route. The answer carries
 * the deleted {@code id}.
 *
 * <p>Traps: with {@link Builder#draftOnly(Boolean)} the route refuses (409)
 * when the endpoint has ever been activated - use it when only a draft may
 * disappear. Without it the delete is unconditional and the endpoint stops
 * routing immediately.
 */
public final class OpenRouterPrivateEndpointDeleteRequest
        extends OpenRouterRequest<OpenRouterPrivateEndpointDeleteResponse<OpenRouterPrivateEndpointDeleteRequest>> {

    private final OpenRouterClient client;
    private final String id;
    private final Boolean draftOnly;

    private OpenRouterPrivateEndpointDeleteRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.draftOnly = builder.draftOnly;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    /**
     * @return the {@code draft_only} query flag, or {@code null} when unset
     *         (the delete is unconditional)
     */
    public Boolean draftOnly() {
        return draftOnly;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/private-endpoints/")
                .append(URLEncoder.encode(id, StandardCharsets.UTF_8));
        if (draftOnly != null) {
            sb.append("?draft_only=").append(draftOnly);
        }
        return sb.toString();
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
    public OpenRouterPrivateEndpointDeleteResponse<OpenRouterPrivateEndpointDeleteRequest> createResponse(
            String responseBody) {
        return new OpenRouterPrivateEndpointDeleteResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterPrivateEndpointDeleteRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterPrivateEndpointDeleteRequest> {

        private final OpenRouterClient client;
        private final String id;
        private Boolean draftOnly;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param id the id (UUID) of the private endpoint (validated loudly;
         *        URL-encoded into the path)
         */
        public Builder(OpenRouterClient client, String id) {
            super(client);
            this.client = client;
            OpenRouterPrivateEndpointRules.requirePathSegment(id, "id");
            this.id = id;
        }

        /**
         * Sets the query flag {@code draft_only} - when {@code true}, only
         * delete the endpoint if it is still a draft (409 otherwise).
         * {@code false} is a set option and is emitted; {@code null} unsets
         * the flag (unconditional delete).
         *
         * @param draftOnly the flag, {@code null} to unset
         * @return this builder
         */
        public Builder draftOnly(Boolean draftOnly) {
            this.draftOnly = draftOnly;
            return this;
        }

        @Override
        public OpenRouterPrivateEndpointDeleteRequest build() {
            return new OpenRouterPrivateEndpointDeleteRequest(this);
        }

        @Override
        public OpenRouterPrivateEndpointDeleteResponse<OpenRouterPrivateEndpointDeleteRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPrivateEndpointDeleteResponse<OpenRouterPrivateEndpointDeleteRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPrivateEndpointDeleteResponse<OpenRouterPrivateEndpointDeleteRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
