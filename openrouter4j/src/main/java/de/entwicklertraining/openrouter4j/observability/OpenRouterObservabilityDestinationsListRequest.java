package de.entwicklertraining.openrouter4j.observability;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to list the observability destinations of the account:
 * GET https://openrouter.ai/api/v1/observability/destinations
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>Destinations are where traces are broadcast to; requests opt into tracing
 * via the {@code trace} request object ({@code OpenRouterTraceConfig}).
 */
public final class OpenRouterObservabilityDestinationsListRequest extends OpenRouterRequest<OpenRouterObservabilityDestinationsListResponse> {

    private final OpenRouterClient client;
    private final Integer offset;
    private final Integer limit;
    private final String workspaceId;

    private OpenRouterObservabilityDestinationsListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.offset = builder.offset;
        this.limit = builder.limit;
        this.workspaceId = builder.workspaceId;
    }


    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/observability/destinations");
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
    public OpenRouterObservabilityDestinationsListResponse createResponse(String responseBody) {
        return new OpenRouterObservabilityDestinationsListResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterObservabilityDestinationsListRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterObservabilityDestinationsListRequest> {

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
        public OpenRouterObservabilityDestinationsListRequest build() {
            return new OpenRouterObservabilityDestinationsListRequest(this);
        }


    @Override
    public OpenRouterObservabilityDestinationsListResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterObservabilityDestinationsListResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterObservabilityDestinationsListResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
