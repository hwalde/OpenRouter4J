package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to read a single workspace:
 * GET https://openrouter.ai/api/v1/workspaces/{id}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 */
public final class OpenRouterWorkspaceGetRequest extends OpenRouterRequest<OpenRouterWorkspaceGetResponse> {

    private final OpenRouterClient client;
    private final String id;

    private OpenRouterWorkspaceGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/workspaces/" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "";
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
    public OpenRouterWorkspaceGetResponse createResponse(String responseBody) {
        return new OpenRouterWorkspaceGetResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterWorkspaceGetRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterWorkspaceGetRequest> {

        private final OpenRouterClient client;
    private final String id;


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

        @Override
        public OpenRouterWorkspaceGetRequest build() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("id is required");
        }
            return new OpenRouterWorkspaceGetRequest(this);
        }


    @Override
    public OpenRouterWorkspaceGetResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterWorkspaceGetResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterWorkspaceGetResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
