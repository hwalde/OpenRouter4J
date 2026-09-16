package de.entwicklertraining.openrouter4j.observability;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to read a single observability destination:
 * GET https://openrouter.ai/api/v1/observability/destinations/{id}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>Secrets inside {@code config} arrive masked by the API.
 */
public final class OpenRouterObservabilityDestinationGetRequest extends OpenRouterRequest<OpenRouterObservabilityDestinationGetResponse> {

    private final OpenRouterClient client;
    private final String id;

    private OpenRouterObservabilityDestinationGetRequest(Builder builder) {
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
    public OpenRouterObservabilityDestinationGetResponse createResponse(String responseBody) {
        return new OpenRouterObservabilityDestinationGetResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterObservabilityDestinationGetRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterObservabilityDestinationGetRequest> {

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
        public OpenRouterObservabilityDestinationGetRequest build() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("id is required");
        }
            return new OpenRouterObservabilityDestinationGetRequest(this);
        }


    @Override
    public OpenRouterObservabilityDestinationGetResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterObservabilityDestinationGetResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterObservabilityDestinationGetResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
