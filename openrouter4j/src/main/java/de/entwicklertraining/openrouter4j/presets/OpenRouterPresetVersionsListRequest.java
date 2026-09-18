package de.entwicklertraining.openrouter4j.presets;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A request to list the versions of one preset:
 * GET https://openrouter.ai/api/v1/presets/{slug}/versions
 */
public final class OpenRouterPresetVersionsListRequest extends OpenRouterRequest<OpenRouterPresetVersionsListResponse> {

    private final OpenRouterClient client;
    private final String slug;
    private final Integer offset;
    private final Integer limit;

    private OpenRouterPresetVersionsListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.slug = builder.slug;
        this.offset = builder.offset;
        this.limit = builder.limit;
    }

    /** @return the URL-encoded slug path segment */
    public String slug() {
        return URLEncoder.encode(slug, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder url = new StringBuilder("/presets/")
                .append(URLEncoder.encode(slug, StandardCharsets.UTF_8))
                .append("/versions");
        String separator = "?";
        if (offset != null) {
            url.append(separator).append("offset=").append(offset);
            separator = "&";
        }
        if (limit != null) {
            url.append(separator).append("limit=").append(limit);
        }
        return url.toString();
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
    public OpenRouterPresetVersionsListResponse createResponse(String responseBody) {
        return new OpenRouterPresetVersionsListResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterPresetVersionsListRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterPresetVersionsListRequest> {

        private final OpenRouterClient client;
        private final String slug;
        private Integer offset;
        private Integer limit;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         * @param slug the preset slug
         */
        public Builder(OpenRouterClient client, String slug) {
            super(client);
            this.client = client;
            this.slug = slug;
        }

        /**
         * Sets the {@code offset} query parameter - the number of entries to
         * skip (pagination).
         *
         * @param offset the pagination offset
         * @return this builder
         */
        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

        /**
         * Sets the {@code limit} query parameter - the maximum number of
         * entries to return (pagination).
         *
         * @param limit the page size
         * @return this builder
         */
        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        @Override
        public OpenRouterPresetVersionsListRequest build() {
            if (slug == null || slug.isEmpty()) {
                throw new IllegalStateException("slug is required to list preset versions");
            }
            return new OpenRouterPresetVersionsListRequest(this);
        }

        @Override
        public OpenRouterPresetVersionsListResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPresetVersionsListResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPresetVersionsListResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
