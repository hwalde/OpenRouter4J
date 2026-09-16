package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to delete a workspace:
 * DELETE https://openrouter.ai/api/v1/workspaces/{id}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 * <p>Deletion is permanent. Deleting the default workspace requires the query
 * parameter {@code confirm_default_workspace_deletion=true}
 */
public final class OpenRouterWorkspaceDeleteRequest extends OpenRouterRequest<OpenRouterWorkspaceDeleteResponse> {

    private final OpenRouterClient client;
    private final String id;
    private final Boolean confirmDefaultWorkspaceDeletion;

    private OpenRouterWorkspaceDeleteRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.confirmDefaultWorkspaceDeletion = builder.confirmDefaultWorkspaceDeletion;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/workspaces/" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "");
        boolean first = true;
        if (confirmDefaultWorkspaceDeletion != null) {
            sb.append(first ? '?' : '&').append("confirm_default_workspace_deletion=").append(confirmDefaultWorkspaceDeletion);
            first = false;
        }
        return sb.toString();
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
    public OpenRouterWorkspaceDeleteResponse createResponse(String responseBody) {
        return new OpenRouterWorkspaceDeleteResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterWorkspaceDeleteRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterWorkspaceDeleteRequest> {

        private final OpenRouterClient client;
    private final String id;
    private Boolean confirmDefaultWorkspaceDeletion;


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
     * Sets the query key {@code confirm_default_workspace_deletion} -
     * confirms the deletion of the account's <em>default</em> workspace: the
     * API rejects deleting that workspace unless this query parameter is set
     * to {@code true}. Leave unset for any other workspace.
     *
     * @param confirmDefaultWorkspaceDeletion whether to confirm the default-workspace deletion
     * @return this builder
     */
    public Builder confirmDefaultWorkspaceDeletion(Boolean confirmDefaultWorkspaceDeletion) {
        this.confirmDefaultWorkspaceDeletion = confirmDefaultWorkspaceDeletion;
        return this;
    }
        @Override
        public OpenRouterWorkspaceDeleteRequest build() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("id is required");
        }
            return new OpenRouterWorkspaceDeleteRequest(this);
        }


    @Override
    public OpenRouterWorkspaceDeleteResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterWorkspaceDeleteResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterWorkspaceDeleteResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
