package de.entwicklertraining.openrouter4j.keys;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to update an existing API key:
 * PATCH https://openrouter.ai/api/v1/keys/{hash}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected (observed live
 * 2026-09-16 as HTTP 401 "Invalid API key"; the schema documents 403).
 * Only explicitly configured fields are sent; every field is optional.
 */
public final class OpenRouterKeyUpdateRequest extends OpenRouterRequest<OpenRouterKeyUpdateResponse> {

    private final OpenRouterClient client;
    private final String hash;
    private final String name;
    private final Double limit;
    private final String limitReset;
    private final Boolean disabled;
    private final Boolean includeByokInLimit;

    private OpenRouterKeyUpdateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.hash = builder.hash;
        this.name = builder.name;
        this.limit = builder.limit;
        this.limitReset = builder.limitReset;
        this.disabled = builder.disabled;
        this.includeByokInLimit = builder.includeByokInLimit;
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
        return "PATCH";
    }

    /**
     * Builds the JSON body with only the explicitly configured fields
     * (an unset option never appears in the JSON).
     *
     * @return the JSON body
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        if (name != null) {
            body.put("name", name);
        }
        if (limit != null) {
            body.put("limit", limit);
        }
        if (limitReset != null) {
            body.put("limit_reset", limitReset);
        }
        if (disabled != null) {
            body.put("disabled", disabled);
        }
        if (includeByokInLimit != null) {
            body.put("include_byok_in_limit", includeByokInLimit);
        }
        return body.toString();
    }

    @Override
    public OpenRouterKeyUpdateResponse createResponse(String responseBody) {
        return new OpenRouterKeyUpdateResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterKeyUpdateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterKeyUpdateRequest> {

        private final OpenRouterClient client;
        private final String hash;
        private String name;
        private Double limit;
        private String limitReset;
        private Boolean disabled;
        private Boolean includeByokInLimit;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param hash the hash identifier of the API key to update
         */
        public Builder(OpenRouterClient client, String hash) {
            super(client);
            this.client = client;
            this.hash = hash;
        }

        /**
         * Sets the body field {@code name} (optional) - new name for the key.
         *
         * @param name the new name
         * @return this builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the body field {@code limit} (optional) - new spending limit
         * in USD.
         *
         * @param limit the new spending limit
         * @return this builder
         */
        public Builder limit(Double limit) {
            this.limit = limit;
            return this;
        }

        /**
         * Sets the body field {@code limit_reset} (optional) - new reset type
         * ({@code daily}, {@code weekly}, {@code monthly}); resets happen
         * automatically at midnight UTC, weeks are Monday through Sunday.
         *
         * @param limitReset the new reset type
         * @return this builder
         */
        public Builder limitReset(String limitReset) {
            this.limitReset = limitReset;
            return this;
        }

        /**
         * Sets the body field {@code disabled} (optional) - whether to
         * disable the key.
         *
         * @param disabled whether to disable the key
         * @return this builder
         */
        public Builder disabled(Boolean disabled) {
            this.disabled = disabled;
            return this;
        }

        /**
         * Sets the body field {@code include_byok_in_limit} (optional) -
         * whether BYOK usage counts into the limit.
         *
         * @param includeByokInLimit whether to include BYOK usage
         * @return this builder
         */
        public Builder includeByokInLimit(Boolean includeByokInLimit) {
            this.includeByokInLimit = includeByokInLimit;
            return this;
        }

        @Override
        public OpenRouterKeyUpdateRequest build() {
            if (hash == null || hash.isEmpty()) {
                throw new IllegalStateException("hash is required to update an API key");
            }
            return new OpenRouterKeyUpdateRequest(this);
        }

        @Override
        public OpenRouterKeyUpdateResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterKeyUpdateResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterKeyUpdateResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
