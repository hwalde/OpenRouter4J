package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import org.json.JSONObject;

/**
 * A request to update the settings of the organization associated with the
 * authenticated management key:
 * PATCH https://openrouter.ai/api/v1/organization/settings
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>The body is a single required boolean, {@code is_filtered_model_catalog_enabled}:
 * when {@code true}, the signed-in dashboard lists only the models the
 * organization can use. Both {@code true} and {@code false} are set options and
 * are always emitted - there is no unset state on this request (the field is
 * required by the schema).
 *
 * <p>Trap: {@code GET /api/v1/models} called with an API key always returns
 * only the models that key can use, whatever this setting says; anonymous
 * requests receive the public catalog. The setting controls the model list of
 * the signed-in dashboard, not the API's.
 */
public final class OpenRouterOrganizationSettingsUpdateRequest extends OpenRouterRequest<OpenRouterOrganizationSettingsUpdateResponse> {

    private final OpenRouterClient client;
    private final boolean isFilteredModelCatalogEnabled;

    private OpenRouterOrganizationSettingsUpdateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.isFilteredModelCatalogEnabled = builder.isFilteredModelCatalogEnabled;
    }


    @Override
    public String getRelativeUrl() {
        return "/organization/settings";
    }

    @Override
    public String getHttpMethod() {
        return "PATCH";
    }

    /**
     * Builds the JSON body with the single required boolean (always emitted).
     *
     * @return the JSON body
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        body.put("is_filtered_model_catalog_enabled", isFilteredModelCatalogEnabled);
        return body.toString();
    }

    /**
     * @return the value of the required body field
     *         {@code is_filtered_model_catalog_enabled}
     */
    public boolean isFilteredModelCatalogEnabled() {
        return isFilteredModelCatalogEnabled;
    }

    @Override
    public OpenRouterOrganizationSettingsUpdateResponse createResponse(String responseBody) {
        return new OpenRouterOrganizationSettingsUpdateResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterOrganizationSettingsUpdateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterOrganizationSettingsUpdateRequest> {

        private final OpenRouterClient client;
        private final boolean isFilteredModelCatalogEnabled;

    /**
     * Creates a builder bound to the given client.
     *
     * @param client the client used to send the request
     * @param isFilteredModelCatalogEnabled the required body field
     *        {@code is_filtered_model_catalog_enabled} - both {@code true} and
     *        {@code false} are set options and are emitted
     */
    public Builder(OpenRouterClient client, boolean isFilteredModelCatalogEnabled) {
        super(client);
        this.client = client;
        this.isFilteredModelCatalogEnabled = isFilteredModelCatalogEnabled;
    }

        @Override
        public OpenRouterOrganizationSettingsUpdateRequest build() {
            return new OpenRouterOrganizationSettingsUpdateRequest(this);
        }


    @Override
    public OpenRouterOrganizationSettingsUpdateResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterOrganizationSettingsUpdateResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterOrganizationSettingsUpdateResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
