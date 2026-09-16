package de.entwicklertraining.openrouter4j.observability;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to delete an observability destination:
 * DELETE https://openrouter.ai/api/v1/observability/destinations/{id}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>Deletion is permanent.
 */
public final class OpenRouterObservabilityDestinationDeleteRequest extends OpenRouterRequest<OpenRouterObservabilityDestinationDeleteResponse> {

    private final OpenRouterClient client;
    private final String id;

    private OpenRouterObservabilityDestinationDeleteRequest(Builder builder) {
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
        return "/observability/destinations/" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "";
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
    public OpenRouterObservabilityDestinationDeleteResponse createResponse(String responseBody) {
        return new OpenRouterObservabilityDestinationDeleteResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterObservabilityDestinationDeleteRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterObservabilityDestinationDeleteRequest> {

        private final OpenRouterClient client;
    private final String id;


    /**
     * Creates a builder bound to the given client.
     *
     * @param client the client used to send the request
     * @param id the id (UUID) of the observability destination
     */
    public Builder(OpenRouterClient client, String id) {
        super(client);
        this.client = client;
        this.id = id;
    }

        @Override
        public OpenRouterObservabilityDestinationDeleteRequest build() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("id is required");
        }
            return new OpenRouterObservabilityDestinationDeleteRequest(this);
        }


    @Override
    public OpenRouterObservabilityDestinationDeleteResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterObservabilityDestinationDeleteResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterObservabilityDestinationDeleteResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
