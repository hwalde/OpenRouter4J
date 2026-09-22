package de.entwicklertraining.openrouter4j.vault;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to delete a workspace secret:
 * DELETE https://openrouter.ai/api/v1/vault/secrets/{name}
 *
 * <p>Deletion is permanent and answers 204 with no body; an unknown name
 * answers 404.
 *
 * <p>Traps: the scope is selected by the API key's active workspace (no
 * workspace parameter); every vault route answers 404 outside the Intern API
 * programme.
 */
public final class OpenRouterVaultSecretDeleteRequest
        extends OpenRouterRequest<OpenRouterVaultSecretDeleteResponse<OpenRouterVaultSecretDeleteRequest>> {

    private final OpenRouterClient client;
    private final String name;

    private OpenRouterVaultSecretDeleteRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.name = builder.name;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String name() {
        return URLEncoder.encode(name, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/vault/secrets/" + URLEncoder.encode(name, StandardCharsets.UTF_8);
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
    public OpenRouterVaultSecretDeleteResponse<OpenRouterVaultSecretDeleteRequest> createResponse(String responseBody) {
        return new OpenRouterVaultSecretDeleteResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building a {@link OpenRouterVaultSecretDeleteRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterVaultSecretDeleteRequest> {

        private final OpenRouterClient client;
        private final String name;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param name the secret name (validated loudly against the schema's
         *             name pattern)
         */
        public Builder(OpenRouterClient client, String name) {
            super(client);
            this.client = client;
            this.name = OpenRouterVaultValidation.requireValidSecretName(name);
        }

        @Override
        public OpenRouterVaultSecretDeleteRequest build() {
            return new OpenRouterVaultSecretDeleteRequest(this);
        }

        @Override
        public OpenRouterVaultSecretDeleteResponse<OpenRouterVaultSecretDeleteRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterVaultSecretDeleteResponse<OpenRouterVaultSecretDeleteRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterVaultSecretDeleteResponse<OpenRouterVaultSecretDeleteRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
