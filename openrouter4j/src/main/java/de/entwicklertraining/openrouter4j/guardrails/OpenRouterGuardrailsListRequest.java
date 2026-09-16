package de.entwicklertraining.openrouter4j.guardrails;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to list the guardrails of the account:
 * GET https://openrouter.ai/api/v1/guardrails
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>Guardrails are OpenRouter's server-side request policy layer (allowed
 * models/providers, data regions, spend limits, content filters, ZDR
 * enforcement); see the
 * <a href="https://openrouter.ai/docs/guides/features/guardrails">guardrails docs</a>.
 */
public final class OpenRouterGuardrailsListRequest extends OpenRouterRequest<OpenRouterGuardrailsListResponse> {

    private final OpenRouterClient client;
    private final Integer offset;
    private final Integer limit;
    private final String workspaceId;

    private OpenRouterGuardrailsListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.offset = builder.offset;
        this.limit = builder.limit;
        this.workspaceId = builder.workspaceId;
    }


    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/guardrails");
        boolean first = true;
        if (offset != null) {
            sb.append(first ? '?' : '&').append("offset=").append(offset);
            first = false;
        }
        if (limit != null) {
            sb.append(first ? '?' : '&').append("limit=").append(limit);
            first = false;
        }
        if (workspaceId != null) {
            sb.append(first ? '?' : '&').append("workspace_id=").append(URLEncoder.encode(workspaceId, StandardCharsets.UTF_8));
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
    public OpenRouterGuardrailsListResponse createResponse(String responseBody) {
        return new OpenRouterGuardrailsListResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGuardrailsListRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGuardrailsListRequest> {

        private final OpenRouterClient client;
    private Integer offset;
    private Integer limit;
    private String workspaceId;


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
    /**
     * Sets the query key {@code workspace_id}.
     *
     * @param workspaceId the value for the query key
     * @return this builder
     */
    public Builder workspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }
        @Override
        public OpenRouterGuardrailsListRequest build() {
            return new OpenRouterGuardrailsListRequest(this);
        }


    @Override
    public OpenRouterGuardrailsListResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterGuardrailsListResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterGuardrailsListResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
