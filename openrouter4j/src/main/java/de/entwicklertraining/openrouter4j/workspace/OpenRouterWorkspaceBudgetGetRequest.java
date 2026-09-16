package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to read a single budget of a workspace:
 * GET https://openrouter.ai/api/v1/workspaces/{ref}/budgets/{interval}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 */
public final class OpenRouterWorkspaceBudgetGetRequest extends OpenRouterRequest<OpenRouterWorkspaceBudgetGetResponse> {

    private final OpenRouterClient client;
    private final String ref;
    private final String interval;

    private OpenRouterWorkspaceBudgetGetRequest(Builder builder) {
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
    public OpenRouterWorkspaceBudgetGetResponse createResponse(String responseBody) {
        return new OpenRouterWorkspaceBudgetGetResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterWorkspaceBudgetGetRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterWorkspaceBudgetGetRequest> {

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
        public OpenRouterWorkspaceBudgetGetRequest build() {
        if (ref == null || ref.isEmpty()) {
            throw new IllegalStateException("ref is required");
        }
        if (interval == null || interval.isEmpty()) {
            throw new IllegalStateException("interval is required");
        }
            return new OpenRouterWorkspaceBudgetGetRequest(this);
        }


    @Override
    public OpenRouterWorkspaceBudgetGetResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterWorkspaceBudgetGetResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterWorkspaceBudgetGetResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
