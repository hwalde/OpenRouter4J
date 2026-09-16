package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to list the budgets of a workspace:
 * GET https://openrouter.ai/api/v1/workspaces/{ref}/budgets
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 * <p>The reference can be the workspace id or its slug. Budgets guard the same
 * spend as guardrail limits, per interval (daily, weekly, monthly, lifetime).
 */
public final class OpenRouterWorkspaceBudgetsListRequest extends OpenRouterRequest<OpenRouterWorkspaceBudgetsListResponse> {

    private final OpenRouterClient client;
    private final String ref;

    private OpenRouterWorkspaceBudgetsListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.ref = builder.ref;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String ref() {
        return URLEncoder.encode(ref, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/workspaces/" + URLEncoder.encode(ref, StandardCharsets.UTF_8) + "/budgets";
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
    public OpenRouterWorkspaceBudgetsListResponse createResponse(String responseBody) {
        return new OpenRouterWorkspaceBudgetsListResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterWorkspaceBudgetsListRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterWorkspaceBudgetsListRequest> {

        private final OpenRouterClient client;
    private final String ref;


    /**
     * Creates a builder bound to the given client.
     *
     * @param client the client used to send the request
     * @param ref the workspace id or slug (the {@code workspace_ref} path segment)
     */
    public Builder(OpenRouterClient client, String ref) {
        super(client);
        this.client = client;
        this.ref = ref;
    }

        @Override
        public OpenRouterWorkspaceBudgetsListRequest build() {
        if (ref == null || ref.isEmpty()) {
            throw new IllegalStateException("ref is required");
        }
            return new OpenRouterWorkspaceBudgetsListRequest(this);
        }


    @Override
    public OpenRouterWorkspaceBudgetsListResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterWorkspaceBudgetsListResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterWorkspaceBudgetsListResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
