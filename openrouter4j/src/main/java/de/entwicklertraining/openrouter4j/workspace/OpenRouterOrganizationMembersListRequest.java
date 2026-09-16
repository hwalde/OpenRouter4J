package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to list the members of the organization:
 * GET https://openrouter.ai/api/v1/organization/members
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 * <p>The user ids returned here are the ones accepted by the workspace members
 * add/remove endpoints and the guardrail member assignments.
 */
public final class OpenRouterOrganizationMembersListRequest extends OpenRouterRequest<OpenRouterOrganizationMembersListResponse> {

    private final OpenRouterClient client;
    private final Integer offset;
    private final Integer limit;

    private OpenRouterOrganizationMembersListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.offset = builder.offset;
        this.limit = builder.limit;
    }


    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/organization/members");
        boolean first = true;
        if (offset != null) {
            sb.append(first ? '?' : '&').append("offset=").append(offset);
            first = false;
        }
        if (limit != null) {
            sb.append(first ? '?' : '&').append("limit=").append(limit);
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
    public OpenRouterOrganizationMembersListResponse createResponse(String responseBody) {
        return new OpenRouterOrganizationMembersListResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterOrganizationMembersListRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterOrganizationMembersListRequest> {

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
     * Sets the query key {@code offset}.
     *
     * @param offset the value for the query key
     * @return this builder
     */
    public Builder offset(Integer offset) {
        this.offset = offset;
        return this;
    }
    /**
     * Sets the query key {@code limit}.
     *
     * @param limit the value for the query key
     * @return this builder
     */
    public Builder limit(Integer limit) {
        this.limit = limit;
        return this;
    }
        @Override
        public OpenRouterOrganizationMembersListRequest build() {
            return new OpenRouterOrganizationMembersListRequest(this);
        }


    @Override
    public OpenRouterOrganizationMembersListResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterOrganizationMembersListResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterOrganizationMembersListResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
