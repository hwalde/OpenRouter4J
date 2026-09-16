package de.entwicklertraining.openrouter4j.keys;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to get a single API key by hash:
 * GET https://openrouter.ai/api/v1/keys/{hash}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected (observed live
 * 2026-09-16 as HTTP 401 "Invalid API key"; the schema documents 403).
 * The {@code hash} is the identifier returned by the list/create endpoints.
 */
public final class OpenRouterKeyGetRequest extends OpenRouterRequest<OpenRouterKeyGetResponse> {

    private final OpenRouterClient client;
    private final String hash;

    private OpenRouterKeyGetRequest(Builder builder) {
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
    public OpenRouterKeyGetResponse createResponse(String responseBody) {
        return new OpenRouterKeyGetResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterKeyGetRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterKeyGetRequest> {

        private final OpenRouterClient client;
        private final String hash;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param hash the hash identifier of the API key
         */
        public Builder(OpenRouterClient client, String hash) {
            super(client);
            this.client = client;
            this.hash = hash;
        }

        @Override
        public OpenRouterKeyGetRequest build() {
            if (hash == null || hash.isEmpty()) {
                throw new IllegalStateException("hash is required to get an API key");
            }
            return new OpenRouterKeyGetRequest(this);
        }

        @Override
        public OpenRouterKeyGetResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterKeyGetResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterKeyGetResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
