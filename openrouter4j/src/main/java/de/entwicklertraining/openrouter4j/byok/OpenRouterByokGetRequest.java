package de.entwicklertraining.openrouter4j.byok;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to read a single BYOK provider credential:
 * GET https://openrouter.ai/api/v1/byok/{id}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>The raw credential is never part of any response - only the masked label.
 */
public final class OpenRouterByokGetRequest extends OpenRouterRequest<OpenRouterByokGetResponse> {

    private final OpenRouterClient client;
    private final String id;

    private OpenRouterByokGetRequest(Builder builder) {
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
        return "/byok/" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "";
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
    public OpenRouterByokGetResponse createResponse(String responseBody) {
        return new OpenRouterByokGetResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterByokGetRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterByokGetRequest> {

        private final OpenRouterClient client;
    private final String id;


    /**
     * Creates a builder bound to the given client.
     *
     * @param client the client used to send the request
     * @param id the id (UUID) of the BYOK credential
     */
    public Builder(OpenRouterClient client, String id) {
        super(client);
        this.client = client;
        this.id = id;
    }

        @Override
        public OpenRouterByokGetRequest build() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("id is required");
        }
            return new OpenRouterByokGetRequest(this);
        }


    @Override
    public OpenRouterByokGetResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterByokGetResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterByokGetResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
