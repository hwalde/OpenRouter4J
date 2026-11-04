package de.entwicklertraining.openrouter4j.externalusers;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to list the external-user registrations of the authenticated
 * organization:
 * GET https://openrouter.ai/api/v1/external-users
 *
 * <p>Inactive registrations are excluded by default; pass
 * {@link Builder#includeInactive(Boolean)} to include deactivated ones.
 * The {@code user} filter matches the tracking string exactly.
 *
 * <p>Traps: every route of this family requires a management key. An
 * external id of {@code .} or {@code ..} cannot be addressed by path at
 * all (the path segment normalizes away) - look such a registration up
 * with the {@code user} filter here instead. Pagination is offset-based:
 * {@code total_count} on the response counts the registrations matching
 * the filters before pagination.
 */
public final class OpenRouterExternalUsersListRequest
        extends OpenRouterRequest<OpenRouterExternalUsersListResponse<OpenRouterExternalUsersListRequest>> {

    private final OpenRouterClient client;
    private final Integer offset;
    private final Integer limit;
    private final String user;
    private final Boolean includeInactive;

    private OpenRouterExternalUsersListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.offset = builder.offset;
        this.limit = builder.limit;
        this.user = builder.user;
        this.includeInactive = builder.includeInactive;
    }

    /**
     * @return the number of records to skip, or {@code null} for the API
     *         default (0)
     */
    public Integer offset() {
        return offset;
    }

    /**
     * @return the page size, or {@code null} for the API default (50)
     */
    public Integer limit() {
        return limit;
    }

    /**
     * @return the exact-match tracking-string filter, or {@code null} when
     *         unset
     */
    public String user() {
        return user;
    }

    /**
     * @return whether deactivated registrations are included, or
     *         {@code null} for the API default ({@code false})
     */
    public Boolean includeInactive() {
        return includeInactive;
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/external-users");
        boolean first = true;
        if (offset != null) {
            sb.append(first ? '?' : '&').append("offset=").append(offset);
            first = false;
        }
        if (limit != null) {
            sb.append(first ? '?' : '&').append("limit=").append(limit);
            first = false;
        }
        if (user != null) {
            sb.append(first ? '?' : '&').append("user=")
                    .append(URLEncoder.encode(user, StandardCharsets.UTF_8));
            first = false;
        }
        if (includeInactive != null) {
            sb.append(first ? '?' : '&').append("include_inactive=").append(includeInactive);
            first = false;
        }
        return sb.toString();
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
    public OpenRouterExternalUsersListResponse<OpenRouterExternalUsersListRequest> createResponse(String responseBody) {
        return new OpenRouterExternalUsersListResponse<OpenRouterExternalUsersListRequest>(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building an {@link OpenRouterExternalUsersListRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterExternalUsersListRequest> {

        private final OpenRouterClient client;
        private Integer offset;
        private Integer limit;
        private String user;
        private Boolean includeInactive;

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
         * Sets the query key {@code offset} - the number of records to skip
         * for pagination. The API default is 0; minimum 0 (validated
         * loudly).
         *
         * @param offset the number of records to skip
         * @return this builder
         */
        public Builder offset(Integer offset) {
            if (offset != null && offset < 0) {
                throw new IllegalArgumentException("offset must be at least 0");
            }
            this.offset = offset;
            return this;
        }

        /**
         * Sets the query key {@code limit} - the maximum number of records
         * to return. The API default is 50; accepted range 1-100
         * (validated loudly).
         *
         * @param limit the page size
         * @return this builder
         */
        public Builder limit(Integer limit) {
            if (limit != null && (limit < 1 || limit > 100)) {
                throw new IllegalArgumentException("limit must be between 1 and 100");
            }
            this.limit = limit;
            return this;
        }

        /**
         * Sets the query key {@code user} - the exact-match filter on the
         * tracking string (same contract as the registrations themselves,
         * validated loudly). This is also the lookup path for ids that
         * cannot be addressed by path ({@code .} / {@code ..}).
         * {@code null} unsets a previously set filter.
         *
         * @param user the tracking string to filter on
         * @return this builder
         */
        public Builder user(String user) {
            this.user = user != null ? OpenRouterExternalUserValidation.requireValidUser(user) : null;
            return this;
        }

        /**
         * Sets the query key {@code include_inactive} - include deactivated
         * registrations ({@code false} is a set option and is emitted).
         * The API default is {@code false}; {@code null} unsets the flag.
         *
         * @param includeInactive whether to include deactivated registrations
         * @return this builder
         */
        public Builder includeInactive(Boolean includeInactive) {
            this.includeInactive = includeInactive;
            return this;
        }

        @Override
        public OpenRouterExternalUsersListRequest build() {
            return new OpenRouterExternalUsersListRequest(this);
        }

        @Override
        public OpenRouterExternalUsersListResponse<OpenRouterExternalUsersListRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterExternalUsersListResponse<OpenRouterExternalUsersListRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterExternalUsersListResponse<OpenRouterExternalUsersListRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
