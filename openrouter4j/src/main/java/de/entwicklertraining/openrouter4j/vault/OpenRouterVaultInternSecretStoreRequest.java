package de.entwicklertraining.openrouter4j.vault;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/**
 * A request to store (create or replace) an intern secret:
 * PUT https://openrouter.ai/api/v1/vault/interns/{internId}/secrets/{name}
 *
 * <p>Same body and semantics as the workspace store
 * ({@link OpenRouterVaultSecretStoreRequest}): {@code value} and
 * {@code hosts}, both required; the value is a write-only secret and never
 * returned.
 *
 * <p>Traps: writes can answer 503 while vault writes are disabled per
 * account; the body must stay under 425,000 bytes (413 above); a 409
 * conflict is possible while the intern is mid-transfer; every vault route
 * answers 404 outside the Intern API programme.
 */
public final class OpenRouterVaultInternSecretStoreRequest
        extends OpenRouterRequest<OpenRouterVaultSecretMutationResponse<OpenRouterVaultInternSecretStoreRequest>> {

    private final OpenRouterClient client;
    private final String internId;
    private final String name;
    private final String value;
    private final List<String> hosts;

    private OpenRouterVaultInternSecretStoreRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.internId = builder.internId;
        this.name = builder.name;
        this.value = builder.value;
        this.hosts = builder.hosts;
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
        return "PUT";
    }

    /**
     * The JSON body: {@code value} and {@code hosts} (both required).
     *
     * @return the JSON body string
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("value", value);
        body.put("hosts", new org.json.JSONArray(hosts));
        return body.toString();
    }

    @Override
    public OpenRouterVaultSecretMutationResponse<OpenRouterVaultInternSecretStoreRequest> createResponse(
            String responseBody) {
        return new OpenRouterVaultSecretMutationResponse<>(new JSONObject(responseBody), this);
    }

    /**
     * Deliberately does not include the request body: the body carries the
     * plaintext secret value.
     *
     * @return a body-free description of this request
     */
    @Override
    public String toString() {
        return "OpenRouterVaultInternSecretStoreRequest{internId=" + internId
                + ", name=" + name + ", value=<redacted>}";
    }

    /**
     * Starting point for building a
     * {@link OpenRouterVaultInternSecretStoreRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterVaultInternSecretStoreRequest> {

        private final OpenRouterClient client;
        private final String internId;
        private final String name;
        private String value;
        private List<String> hosts;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param internId the id (UUID) of the intern
         * @param name the secret name (1-255 chars, lowercase start, only
         *             lowercase letters, digits and single underscores, no
         *             trailing underscore, no {@code __}; validated loudly)
         */
        public Builder(OpenRouterClient client, String internId, String name) {
            super(client);
            this.client = client;
            this.internId = internId;
            this.name = OpenRouterVaultValidation.requireValidSecretName(name);
        }

        /**
         * Sets the JSON field {@code value} - the secret value (1-65,536
         * characters, validated loudly). Write-only: it is encrypted at rest
         * and never returned by any vault response.
         *
         * @param value the plaintext secret value
         * @return this builder
         */
        public Builder value(String value) {
            this.value = OpenRouterVaultValidation.requireValidValue(value);
            return this;
        }

        /**
         * Sets the JSON field {@code hosts} - the exact DNS hostnames the
         * secret may be released to (1-100 entries; see
         * {@link OpenRouterVaultSecretStoreRequest} for the API's
         * normalization and exact-match semantics). Replaces a previously
         * set list.
         *
         * @param hosts the hostnames
         * @return this builder
         */
        public Builder hosts(List<String> hosts) {
            this.hosts = new ArrayList<>(
                    OpenRouterVaultValidation.requireValidHosts(hosts));
            return this;
        }

        /**
         * Sets the JSON field {@code hosts} - see {@link #hosts(List)}.
         * Replaces a previously set list.
         *
         * @param hosts the hostnames
         * @return this builder
         */
        public Builder hosts(String... hosts) {
            List<String> list = new ArrayList<>();
            for (String host : hosts) {
                if (host != null && !host.isEmpty()) {
                    list.add(host);
                }
            }
            return hosts(list);
        }

        @Override
        public OpenRouterVaultInternSecretStoreRequest build() {
            if (value == null) {
                throw new IllegalStateException("value is required");
            }
            if (hosts == null) {
                throw new IllegalStateException("hosts is required");
            }
            return new OpenRouterVaultInternSecretStoreRequest(this);
        }

        @Override
        public OpenRouterVaultSecretMutationResponse<OpenRouterVaultInternSecretStoreRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterVaultSecretMutationResponse<OpenRouterVaultInternSecretStoreRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterVaultSecretMutationResponse<OpenRouterVaultInternSecretStoreRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
