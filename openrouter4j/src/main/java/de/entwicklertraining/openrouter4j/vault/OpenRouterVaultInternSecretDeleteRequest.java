package de.entwicklertraining.openrouter4j.vault;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to delete an intern secret:
 * DELETE https://openrouter.ai/api/v1/vault/interns/{internId}/secrets/{name}
 *
 * <p>Deletion is permanent and answers 204 with no body; an unknown name
 * answers 404. Every vault route answers 404 outside the Intern API
 * programme.
 */
public final class OpenRouterVaultInternSecretDeleteRequest
        extends OpenRouterRequest<OpenRouterVaultSecretDeleteResponse<OpenRouterVaultInternSecretDeleteRequest>> {

    private final OpenRouterClient client;
    private final String internId;
    private final String name;

    private OpenRouterVaultInternSecretDeleteRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.internId = builder.internId;
        this.name = builder.name;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String internId() {
        return URLEncoder.encode(internId, StandardCharsets.UTF_8);
    }

    /**
     * @return the URL-encoded path segment
     */
    public String name() {
        return URLEncoder.encode(name, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/vault/interns/"
                + URLEncoder.encode(internId, StandardCharsets.UTF_8)
                + "/secrets/"
                + URLEncoder.encode(name, StandardCharsets.UTF_8);
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

    /**
     * Builds the response. The documented success answer is 204 with no
     * body, so an empty body yields an empty JSON instead of a parse error.
     *
     * @param responseBody the text body (possibly empty)
     * @return the typed response
     */
    @Override
    public OpenRouterVaultSecretDeleteResponse<OpenRouterVaultInternSecretDeleteRequest> createResponse(String responseBody) {
        return new OpenRouterVaultSecretDeleteResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building a
     * {@link OpenRouterVaultInternSecretDeleteRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterVaultInternSecretDeleteRequest> {

        private final OpenRouterClient client;
        private final String internId;
        private final String name;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param internId the id (UUID) of the intern
         * @param name the secret name (validated loudly against the schema's
         *             name pattern)
         */
        public Builder(OpenRouterClient client, String internId, String name) {
            super(client);
            this.client = client;
            this.internId = internId;
            this.name = OpenRouterVaultValidation.requireValidSecretName(name);
        }

        @Override
        public OpenRouterVaultInternSecretDeleteRequest build() {
            return new OpenRouterVaultInternSecretDeleteRequest(this);
        }

        @Override
        public OpenRouterVaultSecretDeleteResponse<OpenRouterVaultInternSecretDeleteRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterVaultSecretDeleteResponse<OpenRouterVaultInternSecretDeleteRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterVaultSecretDeleteResponse<OpenRouterVaultInternSecretDeleteRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
