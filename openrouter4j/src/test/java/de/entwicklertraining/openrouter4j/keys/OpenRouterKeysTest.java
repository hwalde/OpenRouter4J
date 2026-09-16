package de.entwicklertraining.openrouter4j.keys;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the API key management requests (URL shape, body presence/absence,
 * hash URL-encoding) and the response accessors against recorded JSON shapes
 * of the OpenRouter /key and /keys endpoints.
 */
class OpenRouterKeysTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void currentKeyRequestCarriesNoQueryAndNoBody() {
        OpenRouterCurrentKeyRequest request = new OpenRouterCurrentKeyRequest.Builder(client()).build();
        assertThat(request.getRelativeUrl()).isEqualTo("/key");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void currentKeyResponseSurfacesTypedKeyData() {
        OpenRouterCurrentKeyResponse response = currentKeyResponseOf("""
                {
                  "data": {
                    "byok_usage": 17.38,
                    "byok_usage_daily": 17.38,
                    "byok_usage_monthly": 17.38,
                    "byok_usage_weekly": 17.38,
                    "creator_user_id": "user_2dHFtVWx2n56w6HkM0000000000",
                    "expires_at": "2027-12-31T23:59:59Z",
                    "include_byok_in_limit": false,
                    "is_free_tier": false,
                    "is_management_key": false,
                    "is_provisioning_key": false,
                    "label": "sk-or-v1-au7...890",
                    "limit": 100,
                    "limit_remaining": 74.5,
                    "limit_reset": "monthly",
                    "usage": 25.5,
                    "usage_daily": 25.5,
                    "usage_monthly": 25.5,
                    "usage_weekly": 25.5
                  }
                }
                """);

        assertThat(response.label()).isEqualTo("sk-or-v1-au7...890");
        assertThat(response.limit()).isEqualTo(100.0);
        assertThat(response.limitRemaining()).isEqualTo(74.5);
        assertThat(response.limitReset()).isEqualTo("monthly");
        assertThat(response.includeByokInLimit()).isFalse();
        assertThat(response.usage()).isEqualTo(25.5);
        assertThat(response.usageDaily()).isEqualTo(25.5);
        assertThat(response.usageWeekly()).isEqualTo(25.5);
        assertThat(response.usageMonthly()).isEqualTo(25.5);
        assertThat(response.byokUsage()).isEqualTo(17.38);
        assertThat(response.byokUsageDaily()).isEqualTo(17.38);
        assertThat(response.byokUsageWeekly()).isEqualTo(17.38);
        assertThat(response.byokUsageMonthly()).isEqualTo(17.38);
        assertThat(response.isFreeTier()).isFalse();
        assertThat(response.isManagementKey()).isFalse();
        assertThat(response.isProvisioningKey()).isFalse();
        assertThat(response.expiresAt()).isEqualTo("2027-12-31T23:59:59Z");
        assertThat(response.creatorUserId()).isEqualTo("user_2dHFtVWx2n56w6HkM0000000000");
        assertThat(response.data()).isNotNull();
    }

    @Test
    void currentKeyResponseReturnsNullWhenAbsent() {
        OpenRouterCurrentKeyResponse response = currentKeyResponseOf("{}");
        assertThat(response.label()).isNull();
        assertThat(response.limit()).isNull();
        assertThat(response.isManagementKey()).isNull();
        assertThat(response.data()).isNull();
    }

    @Test
    void listRequestEmitsTypedQueryParameters() {
        OpenRouterKeysListRequest request = new OpenRouterKeysListRequest.Builder(client())
                .includeDisabled(true)
                .offset(10)
                .workspaceId("0df9e665-d932-5740-b2c7-b52af166bc11")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/keys"
                + "?include_disabled=true"
                + "&offset=10"
                + "&workspace_id=0df9e665-d932-5740-b2c7-b52af166bc11");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();

        OpenRouterKeysListRequest plain = new OpenRouterKeysListRequest.Builder(client()).build();
        assertThat(plain.getRelativeUrl()).isEqualTo("/keys");
    }

    @Test
    void listResponseSurfacesTypedKeys() {
        OpenRouterKeysListResponse response = listResponseOf("""
                {
                  "data": [
                    {
                      "byok_usage": 17.38,
                      "created_at": "2025-08-24T10:30:00Z",
                      "creator_user_id": "user_2dHFtVWx2n56w6HkM0000000000",
                      "disabled": false,
                      "expires_at": "2027-12-31T23:59:59Z",
                      "external_user": null,
                      "hash": "f01d52606dc8f0a8303a7b5cc3fa07109c2e346cec7c0a16b40de462992ce943",
                      "include_byok_in_limit": false,
                      "label": "Production API Key",
                      "limit": 100,
                      "limit_remaining": 74.5,
                      "limit_reset": "monthly",
                      "name": "My Production Key",
                      "updated_at": "2025-08-24T15:45:00Z",
                      "usage": 25.5,
                      "workspace_id": "0df9e665-d932-5740-b2c7-b52af166bc11"
                    }
                  ]
                }
                """);

        List<OpenRouterApiKey> items = response.items();
        assertThat(items).hasSize(1);

        OpenRouterApiKey key = items.get(0);
        assertThat(key.hash()).isEqualTo("f01d52606dc8f0a8303a7b5cc3fa07109c2e346cec7c0a16b40de462992ce943");
        assertThat(key.name()).isEqualTo("My Production Key");
        assertThat(key.label()).isEqualTo("Production API Key");
        assertThat(key.disabled()).isFalse();
        assertThat(key.limit()).isEqualTo(100.0);
        assertThat(key.limitRemaining()).isEqualTo(74.5);
        assertThat(key.limitReset()).isEqualTo("monthly");
        assertThat(key.includeByokInLimit()).isFalse();
        assertThat(key.usage()).isEqualTo(25.5);
        assertThat(key.byokUsage()).isEqualTo(17.38);
        assertThat(key.createdAt()).isEqualTo("2025-08-24T10:30:00Z");
        assertThat(key.updatedAt()).isEqualTo("2025-08-24T15:45:00Z");
        assertThat(key.expiresAt()).isEqualTo("2027-12-31T23:59:59Z");
        assertThat(key.externalUser()).isNull();
        assertThat(key.creatorUserId()).isEqualTo("user_2dHFtVWx2n56w6HkM0000000000");
        assertThat(key.workspaceId()).isEqualTo("0df9e665-d932-5740-b2c7-b52af166bc11");
    }

    @Test
    void listResponseReturnsEmptyWhenAbsent() {
        assertThat(listResponseOf("{}").items()).isEmpty();
    }

    @Test
    void createRequestEmitsTheSchemaBodyWithOnlyConfiguredFields() {
        OpenRouterKeyCreateRequest request = new OpenRouterKeyCreateRequest.Builder(client())
                .name("My New API Key")
                .limit(50.0)
                .limitReset("monthly")
                .expiresAt("2027-12-31T23:59:59Z")
                .includeByokInLimit(true)
                .creatorUserId("user_2dHFtVWx2n56w6HkM0000000000")
                .workspaceId("0df9e665-d932-5740-b2c7-b52af166bc11")
                .externalUser("partner-user-123")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/keys");
        assertThat(request.getHttpMethod()).isEqualTo("POST");

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getString("name")).isEqualTo("My New API Key");
        assertThat(body.getDouble("limit")).isEqualTo(50.0);
        assertThat(body.getString("limit_reset")).isEqualTo("monthly");
        assertThat(body.getString("expires_at")).isEqualTo("2027-12-31T23:59:59Z");
        assertThat(body.getBoolean("include_byok_in_limit")).isTrue();
        assertThat(body.getString("creator_user_id")).isEqualTo("user_2dHFtVWx2n56w6HkM0000000000");
        assertThat(body.getString("workspace_id")).isEqualTo("0df9e665-d932-5740-b2c7-b52af166bc11");
        assertThat(body.getJSONObject("external").getString("user")).isEqualTo("partner-user-123");
        assertThat(body.getJSONObject("external").has("api_key")).isFalse();
    }

    @Test
    void createRequestOmitsUnsetKeys() {
        OpenRouterKeyCreateRequest request = new OpenRouterKeyCreateRequest.Builder(client())
                .name("Minimal Key")
                .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsOnly("name");
    }

    @Test
    void createRequestCarriesTheExternalApiKeySecret() {
        OpenRouterKeyCreateRequest request = new OpenRouterKeyCreateRequest.Builder(client())
                .name("Partner Key")
                .externalUser("partner-user-123")
                .externalApiKey("partner-key-32-characters-minimum!")
                .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getJSONObject("external").getString("api_key"))
                .isEqualTo("partner-key-32-characters-minimum!");
    }

    @Test
    void createRequestRequiresTheName() {
        assertThatThrownBy(() -> new OpenRouterKeyCreateRequest.Builder(client()).build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterKeyCreateRequest.Builder(client()).name("").build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createResponseCarriesThePlaintextKeyOnceAndNeverLeaksItInToString() {
        OpenRouterKeyCreateResponse response = createResponseOf("""
                {
                  "data": {
                    "created_at": "2025-08-24T16:00:00Z",
                    "hash": "f01d52606dc8f0a8303a7b5cc3fa07109c2e346cec7c0a16b40de462992ce943",
                    "label": "sk-or-v1-0e6...1c96",
                    "limit": 100,
                    "name": "My New API Key",
                    "usage": 0
                  },
                  "key": "sk-or-v1-example-key-not-real"
                }
                """);

        assertThat(response.key()).isEqualTo("sk-or-v1-example-key-not-real");
        assertThat(response.data()).isNotNull();
        assertThat(response.data().hash()).isEqualTo("f01d52606dc8f0a8303a7b5cc3fa07109c2e346cec7c0a16b40de462992ce943");
        assertThat(response.toString()).doesNotContain("sk-or-v1-example-key-not-real");
    }

    @Test
    void getAndPatchAndDeleteRequestsUrlEncodeTheHashSegment() {
        OpenRouterKeyGetRequest getRequest =
                new OpenRouterKeyGetRequest.Builder(client(), "hash with/special chars").build();
        assertThat(getRequest.getRelativeUrl())
                .isEqualTo("/keys/hash+with%2Fspecial+chars");
        assertThat(getRequest.getHttpMethod()).isEqualTo("GET");
        assertThat(getRequest.getBody()).isNull();

        OpenRouterKeyUpdateRequest patchRequest =
                new OpenRouterKeyUpdateRequest.Builder(client(), "abc123").build();
        assertThat(patchRequest.getRelativeUrl()).isEqualTo("/keys/abc123");
        assertThat(patchRequest.getHttpMethod()).isEqualTo("PATCH");

        OpenRouterKeyDeleteRequest deleteRequest =
                new OpenRouterKeyDeleteRequest.Builder(client(), "abc123").build();
        assertThat(deleteRequest.getRelativeUrl()).isEqualTo("/keys/abc123");
        assertThat(deleteRequest.getHttpMethod()).isEqualTo("DELETE");
        assertThat(deleteRequest.getBody()).isNull();
    }

    @Test
    void hashAccessorsReturnTheUrlEncodedSegment() {
        OpenRouterKeyGetRequest getRequest =
                new OpenRouterKeyGetRequest.Builder(client(), "a b/c").build();
        assertThat(getRequest.hash()).isEqualTo("a+b%2Fc");
    }

    @Test
    void pathRequestsRejectMissingHash() {
        assertThatThrownBy(() -> new OpenRouterKeyGetRequest.Builder(client(), null).build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterKeyUpdateRequest.Builder(client(), "").build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterKeyDeleteRequest.Builder(client(), null).build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateRequestEmitsOnlyConfiguredFields() {
        OpenRouterKeyUpdateRequest request = new OpenRouterKeyUpdateRequest.Builder(client(), "abc123")
                .name("Updated API Key Name")
                .limit(75.0)
                .limitReset("daily")
                .disabled(false)
                .includeByokInLimit(true)
                .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getString("name")).isEqualTo("Updated API Key Name");
        assertThat(body.getDouble("limit")).isEqualTo(75.0);
        assertThat(body.getString("limit_reset")).isEqualTo("daily");
        assertThat(body.getBoolean("disabled")).isFalse();
        assertThat(body.getBoolean("include_byok_in_limit")).isTrue();

        OpenRouterKeyUpdateRequest minimal = new OpenRouterKeyUpdateRequest.Builder(client(), "abc123")
                .disabled(true)
                .build();
        JSONObject minimalBody = new JSONObject(minimal.getBody());
        assertThat(minimalBody.keySet()).containsOnly("disabled");
    }

    @Test
    void getAndUpdateResponsesSurfaceTheKeyData() {
        OpenRouterKeyGetResponse getResponse = getResponseOf("""
                {
                  "data": {
                    "hash": "f01d52606dc8f0a8303a7b5cc3fa07109c2e346cec7c0a16b40de462992ce943",
                    "name": "My Production Key",
                    "disabled": false,
                    "usage": 25.5
                  }
                }
                """);
        assertThat(getResponse.data()).isNotNull();
        assertThat(getResponse.data().name()).isEqualTo("My Production Key");

        OpenRouterKeyUpdateResponse updateResponse = updateResponseOf("""
                {
                  "data": {
                    "hash": "f01d52606dc8f0a8303a7b5cc3fa07109c2e346cec7c0a16b40de462992ce943",
                    "name": "Updated API Key Name",
                    "limit": 75
                  }
                }
                """);
        assertThat(updateResponse.data()).isNotNull();
        assertThat(updateResponse.data().limit()).isEqualTo(75.0);

        assertThat(getResponseOf("{}").data()).isNull();
        assertThat(updateResponseOf("{}").data()).isNull();
    }

    @Test
    void deleteResponseSurfacesTheConfirmation() {
        OpenRouterKeyDeleteResponse response = deleteResponseOf("{\"deleted\": true}");
        assertThat(response.deleted()).isTrue();
        assertThat(deleteResponseOf("{}").deleted()).isNull();
    }

    private OpenRouterCurrentKeyResponse currentKeyResponseOf(String json) {
        OpenRouterCurrentKeyRequest request = new OpenRouterCurrentKeyRequest.Builder(client()).build();
        return request.createResponse(json);
    }

    private OpenRouterKeysListResponse listResponseOf(String json) {
        OpenRouterKeysListRequest request = new OpenRouterKeysListRequest.Builder(client()).build();
        return request.createResponse(json);
    }

    private OpenRouterKeyCreateResponse createResponseOf(String json) {
        OpenRouterKeyCreateRequest request = new OpenRouterKeyCreateRequest.Builder(client())
                .name("My New API Key")
                .build();
        return request.createResponse(json);
    }

    private OpenRouterKeyGetResponse getResponseOf(String json) {
        OpenRouterKeyGetRequest request = new OpenRouterKeyGetRequest.Builder(client(), "abc123").build();
        return request.createResponse(json);
    }

    private OpenRouterKeyUpdateResponse updateResponseOf(String json) {
        OpenRouterKeyUpdateRequest request = new OpenRouterKeyUpdateRequest.Builder(client(), "abc123").build();
        return request.createResponse(json);
    }

    private OpenRouterKeyDeleteResponse deleteResponseOf(String json) {
        OpenRouterKeyDeleteRequest request = new OpenRouterKeyDeleteRequest.Builder(client(), "abc123").build();
        return request.createResponse(json);
    }
}
