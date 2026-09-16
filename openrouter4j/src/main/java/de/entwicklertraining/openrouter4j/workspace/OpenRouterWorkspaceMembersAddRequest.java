package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A request to add members to a workspace:
 * POST https://openrouter.ai/api/v1/workspaces/{id}/members/add
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 * <p>Members are assigned the same role they hold in the organization; they
 * must already be organization members. At most 100 user ids per request.
 */
public final class OpenRouterWorkspaceMembersAddRequest extends OpenRouterRequest<OpenRouterWorkspaceMembersAddResponse> {

    private final OpenRouterClient client;
    private final String id;
    private final List<String> userIds;

    private OpenRouterWorkspaceMembersAddRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.userIds = builder.userIds;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/workspaces/" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "/members/add";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
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
        body.put("user_ids", new JSONArray(userIds));
        return body.toString();
    }

    @Override
    public OpenRouterWorkspaceMembersAddResponse createResponse(String responseBody) {
        return new OpenRouterWorkspaceMembersAddResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterWorkspaceMembersAddRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterWorkspaceMembersAddRequest> {

        private final OpenRouterClient client;
    private final String id;
    private List<String> userIds;


    /**
     * Creates a builder bound to the given client.
     *
     * @param client the client used to send the request
     * @param id the id (UUID) of the workspace
     */
    public Builder(OpenRouterClient client, String id) {
        super(client);
        this.client = client;
        this.id = id;
    }

    /**
 * Adds a user id to the body field {@code user_ids} (required, at least one,
 * max 100) - the Clerk user id of an organization member to add to the
 * workspace.
 */
    public Builder userIds(List<String> userIds) {
        this.userIds = userIds;
        return this;
    }
    /**
     * Adds one user id to the list.
     *
     * <p>Adds one user id to the body field {@code user_ids} (required, at least
one, max 100) - the Clerk user id of an organization member to add to the
workspace.
     *
     * @param addUserId the user id to add
     * @return this builder
     */
    public Builder addUserId(String addUserId) {
        if (this.userIds == null) {
            this.userIds = new ArrayList<>();
        }
        this.userIds.add(addUserId);
        return this;
    }

        @Override
        public OpenRouterWorkspaceMembersAddRequest build() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("id is required");
        }
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalStateException("userIds must contain at least one user id to add members");
        }
        if (userIds != null && userIds.size() > 100) {
            throw new IllegalStateException("userIds accepts at most 100 entries");
        }
            return new OpenRouterWorkspaceMembersAddRequest(this);
        }


    @Override
    public OpenRouterWorkspaceMembersAddResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterWorkspaceMembersAddResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterWorkspaceMembersAddResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
