package de.entwicklertraining.openrouter4j.byok;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the BYOK provider-credential management requests (URL shape, body
 * presence/absence, secret handling, path-segment URL encoding, loud
 * validation) and the response accessors against recorded JSON shapes of the
 * OpenRouter /byok endpoints.
 */
class OpenRouterByokTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void listRequestEmitsTypedQueryParameters() {
        OpenRouterByokListRequest request = new OpenRouterByokListRequest.Builder(client())
                .offset(0)
                .limit(50)
                .workspaceId("550e8400-e29b-41d4-a716-446655440000")
                .provider("openai")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/byok?offset=0&limit=50"
                + "&workspace_id=550e8400-e29b-41d4-a716-446655440000&provider=openai");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void createRequestEmitsOnlyConfiguredFields() {
        OpenRouterByokCreateRequest request = new OpenRouterByokCreateRequest.Builder(client())
                .provider("openai")
                .key("sk-proj-abc123")
                .name("Production OpenAI Key")
                .isFallback(true)
                .allowedModels(List.of("openai/gpt-5.2"))
                .allowedApiKeyHashes(List.of("c56454edb818d6b14bc0d61c46025f1450b0f4012d12304ab40aacb519fcbc93"))
                .workspaceId("550e8400-e29b-41d4-a716-446655440000")
                .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder(
                "provider", "key", "name", "is_fallback", "allowed_models",
                "allowed_api_key_hashes", "workspace_id");
        assertThat(body.get("provider")).isEqualTo("openai");
        assertThat(body.get("name")).isEqualTo("Production OpenAI Key");
        assertThat(body.getBoolean("is_fallback")).isTrue();
        assertThat(body.getJSONArray("allowed_models").toList()).containsExactly("openai/gpt-5.2");
        assertThat(body.getJSONArray("allowed_api_key_hashes").toList())
                .containsExactly("c56454edb818d6b14bc0d61c46025f1450b0f4012d12304ab40aacb519fcbc93");
        assertThat(body.has("disabled")).isFalse();
        assertThat(body.has("is_byok_only")).isFalse();
        assertThat(body.has("declared_zdr")).isFalse();
    }

    @Test
    void declaredZdrIsEmittedOnlyWhenSet() {
        JSONObject withTrue = new JSONObject(new OpenRouterByokCreateRequest.Builder(client())
                .provider("openai")
                .key("sk-proj-abc123")
                .declaredZdr(true)
                .build().getBody());
        assertThat(withTrue.getBoolean("declared_zdr")).isTrue();

        JSONObject withFalse = new JSONObject(new OpenRouterByokCreateRequest.Builder(client())
                .provider("openai")
                .key("sk-proj-abc123")
                .declaredZdr(false)
                .build().getBody());
        assertThat(withFalse.getBoolean("declared_zdr")).isFalse();

        JSONObject withNull = new JSONObject(new OpenRouterByokCreateRequest.Builder(client())
                .provider("openai")
                .key("sk-proj-abc123")
                .declaredZdr(null)
                .build().getBody());
        assertThat(withNull.has("declared_zdr")).isTrue();
        assertThat(withNull.isNull("declared_zdr")).isTrue();

        JSONObject createUnset = new JSONObject(new OpenRouterByokCreateRequest.Builder(client())
                .provider("openai")
                .key("sk-proj-abc123")
                .build().getBody());
        assertThat(createUnset.has("declared_zdr")).isFalse();
    }

    @Test
    void declaredZdrOnUpdateDistinguishesUnsetFromExplicitNull() {
        JSONObject updateTrue = new JSONObject(new OpenRouterByokUpdateRequest.Builder(client(), "b-1")
                .declaredZdr(true)
                .build().getBody());
        assertThat(updateTrue.keySet()).containsExactly("declared_zdr");
        assertThat(updateTrue.getBoolean("declared_zdr")).isTrue();

        JSONObject updateFalse = new JSONObject(new OpenRouterByokUpdateRequest.Builder(client(), "b-1")
                .declaredZdr(false)
                .build().getBody());
        assertThat(updateFalse.keySet()).containsExactly("declared_zdr");
        assertThat(updateFalse.getBoolean("declared_zdr")).isFalse();

        // Explicit null emits a JSON null and clears the declaration to inherit.
        JSONObject updateClear = new JSONObject(new OpenRouterByokUpdateRequest.Builder(client(), "b-1")
                .declaredZdr(null)
                .build().getBody());
        assertThat(updateClear.keySet()).containsExactly("declared_zdr");
        assertThat(updateClear.isNull("declared_zdr")).isTrue();

        // Unset omits the key and leaves the stored value unchanged.
        JSONObject updateUnset = new JSONObject(new OpenRouterByokUpdateRequest.Builder(client(), "b-1")
                .name("n")
                .build().getBody());
        assertThat(updateUnset.has("declared_zdr")).isFalse();
    }

    @Test
    void declaredZdrSetTwiceReplacesThePreviousValue() {
        JSONObject clearLast = new JSONObject(new OpenRouterByokUpdateRequest.Builder(client(), "b-1")
                .declaredZdr(true)
                .declaredZdr(null)
                .build().getBody());
        assertThat(clearLast.keySet()).containsExactly("declared_zdr");
        assertThat(clearLast.isNull("declared_zdr")).isTrue();

        JSONObject trueLast = new JSONObject(new OpenRouterByokUpdateRequest.Builder(client(), "b-1")
                .declaredZdr(null)
                .declaredZdr(true)
                .build().getBody());
        assertThat(trueLast.getBoolean("declared_zdr")).isTrue();

        JSONObject createClearLast = new JSONObject(new OpenRouterByokCreateRequest.Builder(client())
                .provider("openai")
                .key("sk-proj-abc123")
                .declaredZdr(true)
                .declaredZdr(null)
                .build().getBody());
        assertThat(createClearLast.isNull("declared_zdr")).isTrue();

        JSONObject createTrueLast = new JSONObject(new OpenRouterByokCreateRequest.Builder(client())
                .provider("openai")
                .key("sk-proj-abc123")
                .declaredZdr(null)
                .declaredZdr(true)
                .build().getBody());
        assertThat(createTrueLast.getBoolean("declared_zdr")).isTrue();
    }

    @Test
    void declaredZdrViewAccessorIsThreeState() {
        assertThat(new OpenRouterByokKey(new JSONObject()).declaredZdr()).isNull();
        assertThat(new OpenRouterByokKey(new JSONObject().put("declared_zdr", JSONObject.NULL)).declaredZdr()).isNull();
        assertThat(new OpenRouterByokKey(new JSONObject().put("declared_zdr", false)).declaredZdr()).isFalse();
        assertThat(new OpenRouterByokKey(new JSONObject().put("declared_zdr", true)).declaredZdr()).isTrue();
    }

    @Test
    void createRequestRejectsMissingProviderOrKey() {
        assertThatThrownBy(() -> new OpenRouterByokCreateRequest.Builder(client())
                .key("sk-proj-abc123").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("provider");
        assertThatThrownBy(() -> new OpenRouterByokCreateRequest.Builder(client())
                .provider("openai").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("key");
        assertThatThrownBy(() -> new OpenRouterByokCreateRequest.Builder(client())
                .provider("openai").key("").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("key");
    }

    @Test
    void toStringOfRequestDoesNotContainTheKey() {
        OpenRouterByokCreateRequest request = new OpenRouterByokCreateRequest.Builder(client())
                .provider("openai")
                .key("sk-proj-super-secret-value")
                .build();

        assertThat(request.toString()).doesNotContain("sk-proj-super-secret-value");
    }

    @Test
    void updateRequestSupportsKeyRotation() {
        OpenRouterByokUpdateRequest request = new OpenRouterByokUpdateRequest.Builder(client(), "b-1")
                .key("sk-proj-newkey456")
                .disabled(false)
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/byok/b-1");
        assertThat(request.getHttpMethod()).isEqualTo("PATCH");
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("key", "disabled");
        assertThat(request.toString()).doesNotContain("sk-proj-newkey456");
    }

    @Test
    void updateRequestEmitsOnlyConfiguredFields() {
        OpenRouterByokUpdateRequest request = new OpenRouterByokUpdateRequest.Builder(client(), "b-1")
                .name("Updated OpenAI Key")
                .isFallback(true)
                .isRequired(false)
                .isByokOnly(false)
                .allowedModels(List.of("openai/gpt-5.2"))
                .allowedUserIds(List.of("user_abc123"))
                .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder(
                "name", "is_fallback", "is_required", "is_byok_only",
                "allowed_models", "allowed_user_ids");
        assertThat(body.get("name")).isEqualTo("Updated OpenAI Key");
        assertThat(body.getBoolean("is_fallback")).isTrue();
        assertThat(body.getBoolean("is_required")).isFalse();
        assertThat(body.getJSONArray("allowed_models").toList()).containsExactly("openai/gpt-5.2");
        // workspace_id does not exist on the update request
        assertThat(body.has("workspace_id")).isFalse();
    }

    @Test
    void getAndDeleteRequestsUrlEncodeTheId() {
        assertThat(client().byok().get("id with space").build().getRelativeUrl())
                .isEqualTo("/byok/id+with+space");
        OpenRouterByokDeleteRequest delete = client().byok().delete("b/1").build();
        assertThat(delete.getRelativeUrl()).isEqualTo("/byok/b%2F1");
        assertThat(delete.getHttpMethod()).isEqualTo("DELETE");
        assertThat(delete.getBody()).isNull();
    }

    @Test
    void listResponseSurfacesTypedCredentials() {
        OpenRouterByokListResponse response = new OpenRouterByokListResponse(new JSONObject("""
                {
                  "data": [
                    {
                      "id": "11111111-2222-3333-4444-555555555555",
                      "provider": "openai",
                      "label": "sk-...AbCd",
                      "name": "Production OpenAI Key",
                      "disabled": false,
                      "is_fallback": false,
                      "is_required": false,
                      "is_byok_only": false,
                      "declared_zdr": true,
                      "allowed_models": null,
                      "allowed_api_key_hashes": null,
                      "allowed_user_ids": null,
                      "sort_order": 0,
                      "workspace_id": "550e8400-e29b-41d4-a716-446655440000",
                      "created_at": "2025-08-24T10:30:00Z"
                    }
                  ],
                  "total_count": 1
                }
                """), null);

        assertThat(response.totalCount()).isEqualTo(1);
        OpenRouterByokKey key = response.items().get(0);
        assertThat(key.id()).isEqualTo("11111111-2222-3333-4444-555555555555");
        assertThat(key.provider()).isEqualTo("openai");
        assertThat(key.label()).isEqualTo("sk-...AbCd");
        assertThat(key.name()).isEqualTo("Production OpenAI Key");
        assertThat(key.disabled()).isFalse();
        assertThat(key.isByokOnly()).isFalse();
        assertThat(key.declaredZdr()).isTrue();
        assertThat(key.allowedModels()).isEmpty();
        assertThat(key.allowedApiKeyHashes()).isEmpty();
        assertThat(key.sortOrder()).isEqualTo(0);
        assertThat(key.workspaceId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(key.createdAt()).isEqualTo("2025-08-24T10:30:00Z");
    }

    @Test
    void responsesReturnNullAndEmptyWhenAbsent() {
        OpenRouterByokGetResponse get = new OpenRouterByokGetResponse(new JSONObject("{}"), null);
        assertThat(get.data()).isNull();

        OpenRouterByokDeleteResponse deleted = new OpenRouterByokDeleteResponse(new JSONObject("{}"), null);
        assertThat(deleted.deleted()).isNull();
    }
}
