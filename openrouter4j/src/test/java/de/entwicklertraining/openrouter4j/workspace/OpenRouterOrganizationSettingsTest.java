package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the organization settings requests (URL shape, body emission of the
 * single required boolean) and the response accessors against recorded JSON
 * shapes of the OpenRouter /organization/settings endpoints.
 */
class OpenRouterOrganizationSettingsTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void settingsGetRequestHitsTheExpectedRoute() {
        OpenRouterOrganizationSettingsGetRequest request = client().organization().settings().get().build();

        assertThat(request.getRelativeUrl()).isEqualTo("/organization/settings");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void settingsUpdateRequestAlwaysEmitsTheRequiredBoolean() {
        // true and false are both set options and are emitted as JSON booleans
        OpenRouterOrganizationSettingsUpdateRequest enable =
                client().organization().settings().update(true).build();
        assertThat(enable.getRelativeUrl()).isEqualTo("/organization/settings");
        assertThat(enable.getHttpMethod()).isEqualTo("PATCH");
        JSONObject enableBody = new JSONObject(enable.getBody());
        assertThat(enableBody.keySet()).containsExactly("is_filtered_model_catalog_enabled");
        assertThat(enableBody.get("is_filtered_model_catalog_enabled")).isEqualTo(true);
        assertThat(enable.isFilteredModelCatalogEnabled()).isTrue();

        OpenRouterOrganizationSettingsUpdateRequest disable =
                client().organization().settings().update(false).build();
        JSONObject disableBody = new JSONObject(disable.getBody());
        assertThat(disableBody.keySet()).containsExactly("is_filtered_model_catalog_enabled");
        assertThat(disableBody.get("is_filtered_model_catalog_enabled")).isEqualTo(false);
        assertThat(disable.isFilteredModelCatalogEnabled()).isFalse();
    }

    @Test
    void settingsResponseSurfacesTypedFields() {
        OpenRouterOrganizationSettingsGetResponse response = new OpenRouterOrganizationSettingsGetResponse(new JSONObject("""
                {
                  "data": {
                    "id": "org_2dHFtVWx2n56w6HkM0000000000",
                    "is_filtered_model_catalog_enabled": true
                  }
                }
                """), null);

        OpenRouterOrganizationSettings settings = response.data();
        assertThat(settings.id()).isEqualTo("org_2dHFtVWx2n56w6HkM0000000000");
        assertThat(settings.isFilteredModelCatalogEnabled()).isTrue();
        assertThat(settings.json().getString("id")).isEqualTo("org_2dHFtVWx2n56w6HkM0000000000");

        OpenRouterOrganizationSettingsUpdateResponse updated = new OpenRouterOrganizationSettingsUpdateResponse(new JSONObject("""
                {
                  "data": {
                    "id": "org_2dHFtVWx2n56w6HkM0000000000",
                    "is_filtered_model_catalog_enabled": false
                  }
                }
                """), null);
        assertThat(updated.data().isFilteredModelCatalogEnabled()).isFalse();
    }

    @Test
    void settingsResponsesSwallowMalformedBodies() {
        // absent and non-object data answer null
        assertThat(new OpenRouterOrganizationSettingsGetResponse(new JSONObject("{}"), null).data()).isNull();
        assertThat(new OpenRouterOrganizationSettingsGetResponse(
                new JSONObject("{\"data\": \"nope\"}"), null).data()).isNull();

        // malformed fields inside present data are swallowed to null
        OpenRouterOrganizationSettings malformed = new OpenRouterOrganizationSettingsGetResponse(new JSONObject("""
                {
                  "data": {
                    "id": 42,
                    "is_filtered_model_catalog_enabled": "yes"
                  }
                }
                """), null).data();
        assertThat(malformed.id()).isNull();
        assertThat(malformed.isFilteredModelCatalogEnabled()).isNull();

        // the same wrapper shape on the update response
        assertThat(new OpenRouterOrganizationSettingsUpdateResponse(new JSONObject("{}"), null).data()).isNull();
    }
}
