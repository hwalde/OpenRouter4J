package de.entwicklertraining.openrouter4j.presets;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to list the presets of the account:
 * GET https://openrouter.ai/api/v1/presets
 *
 * <p>This is the read surface for presets - distinct from the
 * {@code preset} body field on inference requests (shipped 1.15.0). The
 * documented way to check a slug's existence before sending: an unknown
 * preset slug on inference is silently ignored, so the request is served
 * without the preset.
 */
public final class OpenRouterPresetsListRequest extends OpenRouterRequest<OpenRouterPresetsListResponse> {

    private final OpenRouterClient client;
    private final Integer offset;
    private final Integer limit;

    private OpenRouterPresetsListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.offset = builder.offset;
        this.limit = builder.limit;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder url = new StringBuilder("/presets");
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
    public OpenRouterPresetsListResponse createResponse(String responseBody) {
        return new OpenRouterPresetsListResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterPresetsListRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterPresetsListRequest> {

        private final OpenRouterClient client;
        private Integer offset;
        private Integer limit;

        /**
         * Creates a builder bound to the given client.
         *
         * @param client the client used to send the request
         */
        public Builder(OpenRouterClient client) {
            super(client);
            this.client = client;
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
        public OpenRouterPresetsListRequest build() {
            return new OpenRouterPresetsListRequest(this);
        }

        @Override
        public OpenRouterPresetsListResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterPresetsListResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterPresetsListResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
