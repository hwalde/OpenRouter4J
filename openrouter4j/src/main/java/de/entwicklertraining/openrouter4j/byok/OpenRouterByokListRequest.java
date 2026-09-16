package de.entwicklertraining.openrouter4j.byok;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to list the BYOK provider credentials of the account:
 * GET https://openrouter.ai/api/v1/byok
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>BYOK lets inference run on your own provider credentials; see the
 * <a href="https://openrouter.ai/docs/guides/overview/auth/byok">BYOK docs</a>.
 */
public final class OpenRouterByokListRequest extends OpenRouterRequest<OpenRouterByokListResponse> {

    private final OpenRouterClient client;
    private final Integer offset;
    private final Integer limit;
    private final String workspaceId;
    private final String provider;

    private OpenRouterByokListRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.offset = builder.offset;
        this.limit = builder.limit;
        this.workspaceId = builder.workspaceId;
        this.provider = builder.provider;
    }


    @Override
    public String getRelativeUrl() {
        StringBuilder sb = new StringBuilder("/byok");
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
        if (provider != null) {
            sb.append(first ? '?' : '&').append("provider=").append(URLEncoder.encode(provider, StandardCharsets.UTF_8));
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
    public OpenRouterByokListResponse createResponse(String responseBody) {
        return new OpenRouterByokListResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterByokListRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterByokListRequest> {

        private final OpenRouterClient client;
    private Integer offset;
    private Integer limit;
    private String workspaceId;
    private String provider;


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
    /**
     * Sets the query key {@code provider}.
     *
     * @param provider the value for the query key
     * @return this builder
     */
    public Builder provider(String provider) {
        this.provider = provider;
        return this;
    }
        @Override
        public OpenRouterByokListRequest build() {
            return new OpenRouterByokListRequest(this);
        }


    @Override
    public OpenRouterByokListResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterByokListResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterByokListResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
