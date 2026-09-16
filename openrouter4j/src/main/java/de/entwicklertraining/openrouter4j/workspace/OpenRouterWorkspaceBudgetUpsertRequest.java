package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to create or update the budget of a workspace for one interval:
 * PUT https://openrouter.ai/api/v1/workspaces/{ref}/budgets/{interval}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 * <p>Use {@code lifetime} as the interval for a one-time budget that never
 * resets. The limit must be greater than 0.
 */
public final class OpenRouterWorkspaceBudgetUpsertRequest extends OpenRouterRequest<OpenRouterWorkspaceBudgetUpsertResponse> {

    private final OpenRouterClient client;
    private final String ref;
    private final String interval;
    private final Double limitUsd;
    private final Boolean includeByokInBudgets;

    private OpenRouterWorkspaceBudgetUpsertRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.ref = builder.ref;
        this.interval = builder.interval;
        this.limitUsd = builder.limitUsd;
        this.includeByokInBudgets = builder.includeByokInBudgets;
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
        return "PUT";
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
        body.put("limit_usd", limitUsd);
        if (includeByokInBudgets != null) {
            body.put("include_byok_in_budgets", includeByokInBudgets);
        }
        return body.toString();
    }

    @Override
    public OpenRouterWorkspaceBudgetUpsertResponse createResponse(String responseBody) {
        return new OpenRouterWorkspaceBudgetUpsertResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterWorkspaceBudgetUpsertRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterWorkspaceBudgetUpsertRequest> {

        private final OpenRouterClient client;
    private final String ref;
    private final String interval;
    private Double limitUsd;
    private Boolean includeByokInBudgets;


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

    /**
 * Sets the body field {@code limit_usd} (required) - spending limit in USD
 * for this interval; must be greater than 0.
 */
    public Builder limitUsd(Double limitUsd) {
        this.limitUsd = limitUsd;
        return this;
    }
    /**
 * Sets the body field {@code include_byok_in_budgets} (optional) - whether
 * BYOK spend is included when enforcing the workspace's budgets. This is a
 * workspace-wide setting: it applies to every budget interval, not just the
 * interval of this request.
 */
    public Builder includeByokInBudgets(Boolean includeByokInBudgets) {
        this.includeByokInBudgets = includeByokInBudgets;
        return this;
    }
        @Override
        public OpenRouterWorkspaceBudgetUpsertRequest build() {
        if (ref == null || ref.isEmpty()) {
            throw new IllegalStateException("ref is required");
        }
        if (interval == null || interval.isEmpty()) {
            throw new IllegalStateException("interval is required");
        }
        if (limitUsd == null) {
            throw new IllegalStateException("limitUsd is required to upsert a workspace budget");
        }
        if (limitUsd != null && limitUsd <= 0) {
            throw new IllegalStateException("limitUsd must be greater than 0");
        }
            return new OpenRouterWorkspaceBudgetUpsertRequest(this);
        }


    @Override
    public OpenRouterWorkspaceBudgetUpsertResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterWorkspaceBudgetUpsertResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterWorkspaceBudgetUpsertResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
