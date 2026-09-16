package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to delete a budget of a workspace:
 * DELETE https://openrouter.ai/api/v1/workspaces/{ref}/budgets/{interval}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 * <p>The answer is {@code deleted: true} also when the budget did not exist.
 */
public final class OpenRouterWorkspaceBudgetDeleteRequest extends OpenRouterRequest<OpenRouterWorkspaceBudgetDeleteResponse> {

    private final OpenRouterClient client;
    private final String ref;
    private final String interval;

    private OpenRouterWorkspaceBudgetDeleteRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.ref = builder.ref;
        this.interval = builder.interval;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String ref() {
        return URLEncoder.encode(ref, StandardCharsets.UTF_8);
    }
    /**
     * @return the URL-encoded path segment
     */
    public String interval() {
        return URLEncoder.encode(interval, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/workspaces/" + URLEncoder.encode(ref, StandardCharsets.UTF_8) + "/budgets/" + URLEncoder.encode(interval, StandardCharsets.UTF_8) + "";
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
    public OpenRouterWorkspaceBudgetDeleteResponse createResponse(String responseBody) {
        return new OpenRouterWorkspaceBudgetDeleteResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterWorkspaceBudgetDeleteRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterWorkspaceBudgetDeleteRequest> {

        private final OpenRouterClient client;
    private final String ref;
    private final String interval;


    /**
     * Creates a builder bound to the given client.
     *
     * @param client the client used to send the request
     * @param ref the workspace id or slug (the {@code workspace_ref} path segment)
     * @param interval the budget interval ({@code daily}, {@code weekly}, {@code monthly} or {@code lifetime})
     */
    public Builder(OpenRouterClient client, String ref, String interval) {
        super(client);
        this.client = client;
        this.ref = ref;
        this.interval = interval;
    }

        @Override
        public OpenRouterWorkspaceBudgetDeleteRequest build() {
        if (ref == null || ref.isEmpty()) {
            throw new IllegalStateException("ref is required");
        }
        if (interval == null || interval.isEmpty()) {
            throw new IllegalStateException("interval is required");
        }
            return new OpenRouterWorkspaceBudgetDeleteRequest(this);
        }


    @Override
    public OpenRouterWorkspaceBudgetDeleteResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterWorkspaceBudgetDeleteResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterWorkspaceBudgetDeleteResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
