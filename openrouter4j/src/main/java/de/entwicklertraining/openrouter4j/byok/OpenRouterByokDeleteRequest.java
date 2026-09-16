package de.entwicklertraining.openrouter4j.byok;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * A request to delete a BYOK provider credential:
 * DELETE https://openrouter.ai/api/v1/byok/{id}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>Deletion is permanent.
 */
public final class OpenRouterByokDeleteRequest extends OpenRouterRequest<OpenRouterByokDeleteResponse> {

    private final OpenRouterClient client;
    private final String id;

    private OpenRouterByokDeleteRequest(Builder builder) {
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
    public OpenRouterByokDeleteResponse createResponse(String responseBody) {
        return new OpenRouterByokDeleteResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterByokDeleteRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterByokDeleteRequest> {

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
        public OpenRouterByokDeleteRequest build() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("id is required");
        }
            return new OpenRouterByokDeleteRequest(this);
        }


    @Override
    public OpenRouterByokDeleteResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterByokDeleteResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterByokDeleteResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
