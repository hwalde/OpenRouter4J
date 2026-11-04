package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to read the settings of the organization associated with the
 * authenticated management key:
 * GET https://openrouter.ai/api/v1/organization/settings
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>Trap: {@code GET /api/v1/models} called with an API key always returns
 * only the models that key can use, whatever the returned
 * {@code is_filtered_model_catalog_enabled} says; anonymous requests receive
 * the public catalog. The setting controls the model list of the signed-in
 * dashboard, not the API's.
 */
public final class OpenRouterOrganizationSettingsGetRequest extends OpenRouterRequest<OpenRouterOrganizationSettingsGetResponse> {

    private final OpenRouterClient client;

    private OpenRouterOrganizationSettingsGetRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
    }


    @Override
    public String getRelativeUrl() {
        return "/organization/settings";
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
    public OpenRouterOrganizationSettingsGetResponse createResponse(String responseBody) {
        return new OpenRouterOrganizationSettingsGetResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterOrganizationSettingsGetRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterOrganizationSettingsGetRequest> {

        private final OpenRouterClient client;

    /**
     * Creates a builder bound to the given client.
     *
     * @param client the client used to send the request
     */
    public Builder(OpenRouterClient client) {
        super(client);
        this.client = client;
    }

        @Override
        public OpenRouterOrganizationSettingsGetRequest build() {
            return new OpenRouterOrganizationSettingsGetRequest(this);
        }


    @Override
    public OpenRouterOrganizationSettingsGetResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterOrganizationSettingsGetResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterOrganizationSettingsGetResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
