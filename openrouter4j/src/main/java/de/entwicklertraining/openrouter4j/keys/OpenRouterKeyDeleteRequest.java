package de.entwicklertraining.openrouter4j.keys;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to delete an API key:
 * DELETE https://openrouter.ai/api/v1/keys/{hash}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected (observed live
 * 2026-09-16 as HTTP 401 "Invalid API key"; the schema documents 403).
 * Deletion is permanent: requests authenticated with the deleted key fail
 * afterwards.
 */
public final class OpenRouterKeyDeleteRequest extends OpenRouterRequest<OpenRouterKeyDeleteResponse> {

    private final OpenRouterClient client;
    private final String hash;

    private OpenRouterKeyDeleteRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.hash = builder.hash;
    }

    /**
     * @return the URL-encoded hash path segment
     */
    public String hash() {
        return URLEncoder.encode(hash, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/keys/" + URLEncoder.encode(hash, StandardCharsets.UTF_8);
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
    public OpenRouterKeyDeleteResponse createResponse(String responseBody) {
        return new OpenRouterKeyDeleteResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterKeyDeleteRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterKeyDeleteRequest> {

        private final OpenRouterClient client;
        private final String hash;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param hash the hash identifier of the API key to delete
         */
        public Builder(OpenRouterClient client, String hash) {
            super(client);
            this.client = client;
            this.hash = hash;
        }

        @Override
        public OpenRouterKeyDeleteRequest build() {
            if (hash == null || hash.isEmpty()) {
                throw new IllegalStateException("hash is required to delete an API key");
            }
            return new OpenRouterKeyDeleteRequest(this);
        }

        @Override
        public OpenRouterKeyDeleteResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterKeyDeleteResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterKeyDeleteResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
