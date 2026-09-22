package de.entwicklertraining.openrouter4j.vault;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.json.JSONObject;

/**
 * A request to copy named workspace secrets into an intern's scope:
 * POST https://openrouter.ai/api/v1/vault/interns/{internId}/secrets/copy
 *
 * <p>Body: {@code names} - 1 to 100 distinct secret names (schema
 * {@code VaultSecretCopyRequest}, validated loudly here, duplicates
 * rejected). The response carries one metadata entry per requested name.
 *
 * <p>Traps: every requested name must exist in the workspace scope -
 * otherwise the API answers 404 and <b>nothing</b> is copied; a 409 conflict
 * is answered when a source secret has {@code hosts: null} (a legacy row
 * written before host binding was required) or the intern is mid-transfer.
 * The copies are independent values in the intern vault: their fingerprints
 * are keyed with the intern vault's data key and differ from the workspace
 * originals' even for identical values, so a later rotation of the workspace
 * secret does not propagate - copy again to refresh.
 */
public final class OpenRouterVaultSecretCopyRequest
        extends OpenRouterRequest<OpenRouterVaultSecretCopyResponse> {

    private final OpenRouterClient client;
    private final String internId;
    private final List<String> names;

    private OpenRouterVaultSecretCopyRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.internId = builder.internId;
        this.names = builder.names;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String internId() {
        return URLEncoder.encode(internId, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/vault/interns/"
                + URLEncoder.encode(internId, StandardCharsets.UTF_8)
                + "/secrets/copy";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
    }

    /**
     * The JSON body: {@code names} (required).
     *
     * @return the JSON body string
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("names", new org.json.JSONArray(names));
        return body.toString();
    }

    @Override
    public OpenRouterVaultSecretCopyResponse createResponse(String responseBody) {
        return new OpenRouterVaultSecretCopyResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterVaultSecretCopyRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterVaultSecretCopyRequest> {

        private final OpenRouterClient client;
        private final String internId;
        private List<String> names;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param internId the id (UUID) of the intern
         */
        public Builder(OpenRouterClient client, String internId) {
            super(client);
            this.client = client;
            this.internId = internId;
        }

        /**
         * Sets the JSON field {@code names} - the workspace secret names to
         * copy (1-100 distinct entries, validated loudly). Replaces a
         * previously set list.
         *
         * @param names the secret names
         * @return this builder
         */
        public Builder names(List<String> names) {
            OpenRouterVaultValidation.requireValidCopyNames(names);
            LinkedHashSet<String> distinct = new LinkedHashSet<>(names);
            if (distinct.size() != names.size()) {
                throw new IllegalArgumentException("secret names must be distinct");
            }
            this.names = new ArrayList<>(distinct);
            return this;
        }

        /**
         * Sets the JSON field {@code names} - see {@link #names(List)}.
         * Replaces a previously set list.
         *
         * @param names the secret names
         * @return this builder
         */
        public Builder names(String... names) {
            List<String> list = new ArrayList<>();
            for (String name : names) {
                if (name != null && !name.isEmpty()) {
                    list.add(name);
                }
            }
            return names(list);
        }

        @Override
        public OpenRouterVaultSecretCopyRequest build() {
            if (names == null) {
                throw new IllegalStateException("names is required");
            }
            return new OpenRouterVaultSecretCopyRequest(this);
        }

        @Override
        public OpenRouterVaultSecretCopyResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterVaultSecretCopyResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterVaultSecretCopyResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
