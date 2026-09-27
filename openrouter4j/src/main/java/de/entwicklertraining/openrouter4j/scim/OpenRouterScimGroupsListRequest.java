package de.entwicklertraining.openrouter4j.scim;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to list the SCIM groups of the organization:
 * GET https://openrouter.ai/api/v1/scim/groups
 *
 * <p>The returned group ids are the {@code scim_group_id} values the
 * mapping endpoints accept.
 *
 * <p>Query parameters: {@code offset} / {@code limit} (pagination) plus the
 * optional exact-match filters {@code display_name} and {@code external_id}.
 * Both filters are exact matches, not substring searches; pass the raw value,
 * it is URL-encoded for you. An unset filter is omitted from the query string
 * entirely; a blank filter value is rejected loudly because the API treats an
 * empty value as no filter at all and would silently answer with the
 * unfiltered list.
 *
 * <p>OpenRouter requires a management key for every SCIM endpoint (see the
 * class javadoc of {@link OpenRouterScimGroupMappingsListRequest}).
 */
public final class OpenRouterScimGroupsListRequest extends OpenRouterRequest<OpenRouterScimGroupsListResponse> {

    private final OpenRouterClient client;
    private final Integer offset;
    private final Integer limit;
    private final String displayName;
    private final String externalId;

    private OpenRouterScimGroupsListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.offset = builder.offset;
        this.limit = builder.limit;
        this.displayName = builder.displayName;
        this.externalId = builder.externalId;
    }

    /**
     * @return the {@code display_name} filter, or {@code null} when unset
     */
    public String displayName() {
        return displayName;
    }

    /**
     * @return the {@code external_id} filter, or {@code null} when unset
     */
    public String externalId() {
        return externalId;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder url = new StringBuilder("/scim/groups");
        String separator = "?";
        if (offset != null) {
            url.append(separator).append("offset=").append(offset);
            separator = "&";
        }
        if (limit != null) {
            url.append(separator).append("limit=").append(limit);
            separator = "&";
        }
        if (displayName != null) {
            url.append(separator).append("display_name=")
                    .append(URLEncoder.encode(displayName, StandardCharsets.UTF_8));
            separator = "&";
        }
        if (externalId != null) {
            url.append(separator).append("external_id=")
                    .append(URLEncoder.encode(externalId, StandardCharsets.UTF_8));
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
    public OpenRouterScimGroupsListResponse createResponse(String responseBody) {
        return new OpenRouterScimGroupsListResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterScimGroupsListRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterScimGroupsListRequest> {

        private final OpenRouterClient client;
        private Integer offset;
        private Integer limit;
        private String displayName;
        private String externalId;

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

        /**
         * Sets the {@code display_name} query parameter - filters the list
         * to groups whose display name matches exactly (not a substring
         * search). Pass the raw value, it is URL-encoded for you.
         * {@code null} unsets a previously set filter and omits the
         * parameter; a blank value is rejected loudly because the API treats
         * an empty value as no filter at all and would silently answer with
         * the unfiltered list.
         *
         * @param displayName the exact group display name to filter for
         * @return this builder
         */
        public Builder displayName(String displayName) {
            if (displayName == null) {
                this.displayName = null;
                return this;
            }
            if (displayName.isBlank()) {
                throw new IllegalArgumentException("display_name must not be blank");
            }
            this.displayName = displayName;
            return this;
        }

        /**
         * Sets the {@code external_id} query parameter - filters the list to
         * groups whose external identity-provider id matches exactly (not a
         * substring search). Pass the raw value, it is URL-encoded for you.
         * {@code null} unsets a previously set filter and omits the
         * parameter; a blank value is rejected loudly because the API treats
         * an empty value as no filter at all and would silently answer with
         * the unfiltered list.
         *
         * @param externalId the exact external id to filter for
         * @return this builder
         */
        public Builder externalId(String externalId) {
            if (externalId == null) {
                this.externalId = null;
                return this;
            }
            if (externalId.isBlank()) {
                throw new IllegalArgumentException("external_id must not be blank");
            }
            this.externalId = externalId;
            return this;
        }

        @Override
        public OpenRouterScimGroupsListRequest build() {
            return new OpenRouterScimGroupsListRequest(this);
        }

        @Override
        public OpenRouterScimGroupsListResponse execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterScimGroupsListResponse executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterScimGroupsListResponse executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
