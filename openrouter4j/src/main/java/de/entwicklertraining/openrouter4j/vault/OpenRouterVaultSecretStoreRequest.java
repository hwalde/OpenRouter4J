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
 * A request to store (create or replace) a workspace secret:
 * PUT https://openrouter.ai/api/v1/vault/secrets/{name}
 *
 * <p>Body: {@code value} and {@code hosts}, both required (schema
 * {@code VaultSecretWriteRequest}). Storing the same name again replaces the
 * value and the host binding.
 *
 * <p><b>Write-only value:</b> the value is encrypted at rest and never
 * returned by any vault response - treat it as a write-only secret, like the
 * plaintext of a created API key. The response's
 * {@link OpenRouterVaultSecret#fingerprint() fingerprint} is the only way to
 * confirm what was stored (comparable only within one vault).
 *
 * <p>Hosts are 1-100 exact DNS hostnames the secret may be released to. The
 * API lowercases each entry and removes a trailing dot (so
 * {@code API.Example.com.} is stored as {@code api.example.com}); schemes,
 * ports, paths, wildcards and empty values are rejected by the API, and
 * duplicates after normalization are collapsed. Matching at release time is
 * exact: a secret bound to {@code api.example.com} is never released to
 * {@code example.com} or any other hostname. This builder validates the
 * cheap local rules loudly (non-empty entries, 1-100 of them); everything
 * else is enforced server-side.
 *
 * <p>Traps: the scope is selected by the API key's active workspace (no
 * workspace parameter); writes can answer 503 while vault writes are
 * disabled per account; the body must stay under 425,000 bytes (413 above);
 * regional hostnames are refused with 403; every vault route answers 404
 * outside the Intern API programme.
 */
public final class OpenRouterVaultSecretStoreRequest
        extends OpenRouterRequest<OpenRouterVaultSecretMutationResponse<OpenRouterVaultSecretStoreRequest>> {

    private final OpenRouterClient client;
    private final String name;
    private final String value;
    private final List<String> hosts;

    private OpenRouterVaultSecretStoreRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.name = builder.name;
        this.value = builder.value;
        this.hosts = builder.hosts;
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
    public OpenRouterVaultSecretMutationResponse<OpenRouterVaultSecretStoreRequest> createResponse(
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
        return "OpenRouterVaultSecretStoreRequest{name=" + name + ", value=<redacted>}";
    }

    /**
     * Starting point for building a {@link OpenRouterVaultSecretStoreRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterVaultSecretStoreRequest> {

        private final OpenRouterClient client;
        private final String name;
        private String value;
        private List<String> hosts;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param name the secret name (1-255 chars, lowercase start, only
         *             lowercase letters, digits and single underscores, no
         *             trailing underscore, no {@code __}; validated loudly)
         */
        public Builder(OpenRouterClient client, String name) {
            super(client);
            this.client = client;
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
         * secret may be released to (1-100 entries, validated loudly for the
         * cheap rules; see the class javadoc for the API's normalization and
         * exact-match semantics). Replaces a previously set list.
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
        public OpenRouterVaultSecretStoreRequest build() {
            if (value == null) {
                throw new IllegalStateException("value is required");
            }
            if (hosts == null) {
                throw new IllegalStateException("hosts is required");
            }
            return new OpenRouterVaultSecretStoreRequest(this);
        }

        @Override
        public OpenRouterVaultSecretMutationResponse<OpenRouterVaultSecretStoreRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterVaultSecretMutationResponse<OpenRouterVaultSecretStoreRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterVaultSecretMutationResponse<OpenRouterVaultSecretStoreRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
