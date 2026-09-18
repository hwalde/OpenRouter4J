package de.entwicklertraining.openrouter4j.presets;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest;
import de.entwicklertraining.openrouter4j.messages.OpenRouterMessagesRequest;
import de.entwicklertraining.openrouter4j.responses.OpenRouterResponsesRequest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the preset read and management requests (URL shape, pagination,
 * slug/version URL-encoding, body reuse from the ordinary inference
 * builders, build validation) and the response accessors against the
 * recorded JSON shapes of the OpenRouter /presets endpoints.
 */
class OpenRouterPresetsTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void listRequestCarriesPaginationOnly() {
        OpenRouterPresetsListRequest request = client().presets().list().offset(10).limit(20).build();
        assertThat(request.getRelativeUrl()).isEqualTo("/presets?offset=10&limit=20");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void getRequestEncodesTheSlug() {
        OpenRouterPresetGetRequest request = client().presets().get("my preset").build();
        assertThat(request.getRelativeUrl()).isEqualTo("/presets/my+preset");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.slug()).isEqualTo("my+preset");
    }

    @Test
    void versionsListRequestEncodesTheSlug() {
        OpenRouterPresetVersionsListRequest request =
                client().presets().versions("my/preset").limit(5).build();
        assertThat(request.getRelativeUrl()).isEqualTo("/presets/my%2Fpreset/versions?limit=5");
    }

    @Test
    void versionGetRequestEncodesSlugAndVersion() {
        OpenRouterPresetVersionGetRequest request =
                client().presets().version("my preset", "latest").build();
        assertThat(request.getRelativeUrl()).isEqualTo("/presets/my+preset/versions/latest");
    }

    @Test
    void versionGetRequestRejectsMissingSlugAndVersion() {
        assertThatThrownBy(() -> new OpenRouterPresetVersionGetRequest.Builder(client(), "slug", null).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("version");
        assertThatThrownBy(() -> new OpenRouterPresetGetRequest.Builder(client(), null).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("slug");
    }

    @Test
    void upsertFromChatSendsTheChatBodyVerbatimOnTheManagementRoute() {
        OpenRouterChatCompletionRequest chatRequest = client().chat().completion()
                .model("openai/gpt-4o")
                .addMessage("system", "You are terse.")
                .addMessage("user", "Hi")
                .temperature(0.2)
                .build();
        OpenRouterPresetUpsertFromChatRequest request = client().presets()
                .upsertFromChat("my-preset")
                .body(chatRequest)
                .build();
        assertThat(request.getRelativeUrl()).isEqualTo("/presets/my-preset/chat/completions");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getString("model")).isEqualTo("openai/gpt-4o");
        assertThat(body.getDouble("temperature")).isEqualTo(0.2);
        assertThat(body.getJSONArray("messages").length()).isEqualTo(2);
    }

    @Test
    void upsertFromChatRejectsMissingBody() {
        assertThatThrownBy(() -> client().presets().upsertFromChat("my-preset").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("body");
    }

    @Test
    void upsertFromMessagesSendsTheMessagesBodyVerbatimOnTheManagementRoute() {
        OpenRouterMessagesRequest messagesRequest = client().messages()
                .model("anthropic/claude-4.5-sonnet-20250929")
                .maxTokens(1024)
                .addMessage("user", "Hi")
                .build();
        OpenRouterPresetUpsertFromMessagesRequest request = client().presets()
                .upsertFromMessages("my-preset")
                .body(messagesRequest)
                .build();
        assertThat(request.getRelativeUrl()).isEqualTo("/presets/my-preset/messages");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getString("model")).isEqualTo("anthropic/claude-4.5-sonnet-20250929");
        assertThat(body.getInt("max_tokens")).isEqualTo(1024);
    }

    @Test
    void upsertFromResponsesSendsTheResponsesBodyVerbatimOnTheManagementRoute() {
        OpenRouterResponsesRequest responsesRequest = client().responses()
                .model("openai/gpt-4o")
                .input("Tell me a joke")
                .build();
        OpenRouterPresetUpsertFromResponsesRequest request = client().presets()
                .upsertFromResponses("my-preset")
                .body(responsesRequest)
                .build();
        assertThat(request.getRelativeUrl()).isEqualTo("/presets/my-preset/responses");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getString("model")).isEqualTo("openai/gpt-4o");
        assertThat(body.getString("input")).isEqualTo("Tell me a joke");
    }

    @Test
    void listResponseSurfacesTheRecordedFixture() {
        OpenRouterPresetsListResponse response = listResponseOf("""
                {
                  "data": [
                    {
                      "created_at": "2026-04-20T10:00:00Z",
                      "creator_user_id": "user_2dHFtVWx2n56w6HkM0000000000",
                      "description": null,
                      "designated_version_id": "550e8400-e29b-41d4-a716-446655440000",
                      "id": "650e8400-e29b-41d4-a716-446655440001",
                      "name": "my-preset",
                      "slug": "my-preset",
                      "status": "active",
                      "status_updated_at": null,
                      "updated_at": "2026-04-20T10:00:00Z",
                      "workspace_id": "750e8400-e29b-41d4-a716-446655440002"
                    }
                  ],
                  "total_count": 1
                }
                """);
        assertThat(response.totalCount()).isEqualTo(1L);
        assertThat(response.presets()).hasSize(1);
        OpenRouterPreset preset = response.presets().get(0);
        assertThat(preset.id()).isEqualTo("650e8400-e29b-41d4-a716-446655440001");
        assertThat(preset.slug()).isEqualTo("my-preset");
        assertThat(preset.name()).isEqualTo("my-preset");
        assertThat(preset.status()).isEqualTo("active");
        assertThat(preset.description()).isNull();
        assertThat(preset.designatedVersionId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(preset.designatedVersion()).isNull();
    }

    @Test
    void getResponseSurfacesTheDesignatedVersion() {
        OpenRouterPresetGetResponse response = getResponseOf("""
                {
                  "data": {
                    "created_at": "2026-04-20T10:00:00Z",
                    "creator_user_id": "user_2dHFtVWx2n56w6HkM0000000000",
                    "description": null,
                    "designated_version": {
                      "config": {"model": "openai/gpt-4o", "temperature": 0.7},
                      "created_at": "2026-04-20T10:00:00Z",
                      "creator_id": "user_2dHFtVWx2n56w6HkM0000000000",
                      "id": "550e8400-e29b-41d4-a716-446655440000",
                      "preset_id": "650e8400-e29b-41d4-a716-446655440001",
                      "system_prompt": "You are a helpful assistant.",
                      "updated_at": "2026-04-20T10:00:00Z",
                      "version": 1
                    },
                    "designated_version_id": "550e8400-e29b-41d4-a716-446655440000",
                    "id": "650e8400-e29b-41d4-a716-446655440001",
                    "name": "my-preset",
                    "slug": "my-preset",
                    "status": "active",
                    "status_updated_at": null,
                    "updated_at": "2026-04-20T10:00:00Z",
                    "workspace_id": "750e8400-e29b-41d4-a716-446655440002"
                  }
                }
                """);
        OpenRouterPreset preset = response.preset();
        assertThat(preset).isNotNull();
        OpenRouterPresetVersion version = preset.designatedVersion();
        assertThat(version).isNotNull();
        assertThat(version.id()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(version.presetId()).isEqualTo("650e8400-e29b-41d4-a716-446655440001");
        assertThat(version.version()).isEqualTo(1);
        assertThat(version.systemPrompt()).isEqualTo("You are a helpful assistant.");
        assertThat(version.config().getDouble("temperature")).isEqualTo(0.7);
    }

    @Test
    void versionsListAndVersionGetSurfacesTheRecordedFixture() {
        OpenRouterPresetVersionsListResponse list = versionsListResponseOf("""
                {
                  "data": [
                    {
                      "config": {"model": "openai/gpt-4o"},
                      "created_at": "2026-04-20T10:00:00Z",
                      "creator_id": "user_2dHFtVWx2n56w6HkM0000000000",
                      "id": "550e8400-e29b-41d4-a716-446655440000",
                      "preset_id": "650e8400-e29b-41d4-a716-446655440001",
                      "system_prompt": "You are a helpful assistant.",
                      "updated_at": "2026-04-20T10:00:00Z",
                      "version": 1
                    }
                  ],
                  "total_count": 1
                }
                """);
        assertThat(list.totalCount()).isEqualTo(1L);
        assertThat(list.versions()).hasSize(1);
        assertThat(list.versions().get(0).version()).isEqualTo(1);

        OpenRouterPresetVersionGetResponse single = versionGetResponseOf("""
                {
                  "data": {
                    "config": {"model": "openai/gpt-4o"},
                    "created_at": "2026-04-20T10:00:00Z",
                    "creator_id": "user_2dHFtVWx2n56w6HkM0000000000",
                    "id": "550e8400-e29b-41d4-a716-446655440000",
                    "preset_id": "650e8400-e29b-41d4-a716-446655440001",
                    "system_prompt": null,
                    "updated_at": "2026-04-20T10:00:00Z",
                    "version": 2
                  }
                }
                """);
        assertThat(single.version().version()).isEqualTo(2);
        assertThat(single.version().systemPrompt()).isNull();
    }

    @Test
    void upsertResponseSurfacesTheCreatedPreset() {
        OpenRouterPresetUpsertFromChatRequest request = client().presets()
                .upsertFromChat("my-preset")
                .body(client().chat().completion().model("openai/gpt-4o").addMessage("user", "Hi").build())
                .build();
        OpenRouterPresetUpsertResponse<OpenRouterPresetUpsertFromChatRequest> response =
                request.createResponse("""
                {
                  "data": {
                    "created_at": "2026-04-20T10:00:00Z",
                    "creator_user_id": "user_2dHFtVWx2n56w6HkM0000000000",
                    "description": null,
                    "designated_version": {
                      "config": {"model": "openai/gpt-4o", "temperature": 0.7},
                      "created_at": "2026-04-20T10:00:00Z",
                      "creator_id": "user_2dHFtVWx2n56w6HkM0000000000",
                      "id": "550e8400-e29b-41d4-a716-446655440000",
                      "preset_id": "650e8400-e29b-41d4-a716-446655440001",
                      "system_prompt": "You are a helpful assistant.",
                      "updated_at": "2026-04-20T10:00:00Z",
                      "version": 3
                    },
                    "designated_version_id": "550e8400-e29b-41d4-a716-446655440000",
                    "id": "650e8400-e29b-41d4-a716-446655440001",
                    "name": "my-preset",
                    "slug": "my-preset",
                    "status": "active",
                    "status_updated_at": null,
                    "updated_at": "2026-04-20T10:00:00Z",
                    "workspace_id": "750e8400-e29b-41d4-a716-446655440002"
                  }
                }
                """);
        assertThat(response.preset()).isNotNull();
        assertThat(response.preset().slug()).isEqualTo("my-preset");
        assertThat(response.preset().designatedVersion().version()).isEqualTo(3);
    }

    @Test
    void responsesOnEmptyBodyReturnNullsAndEmptyLists() {
        assertThat(listResponseOf("{}").presets()).isEmpty();
        assertThat(listResponseOf("{}").totalCount()).isNull();
        assertThat(getResponseOf("{}").preset()).isNull();
        assertThat(versionsListResponseOf("{}").versions()).isEmpty();
        assertThat(versionGetResponseOf("{}").version()).isNull();
    }

    private OpenRouterPresetsListResponse listResponseOf(String json) {
        OpenRouterPresetsListRequest request = client().presets().list().build();
        return request.createResponse(json);
    }

    private OpenRouterPresetGetResponse getResponseOf(String json) {
        OpenRouterPresetGetRequest request = client().presets().get("my-preset").build();
        return request.createResponse(json);
    }

    private OpenRouterPresetVersionsListResponse versionsListResponseOf(String json) {
        OpenRouterPresetVersionsListRequest request = client().presets().versions("my-preset").build();
        return request.createResponse(json);
    }

    private OpenRouterPresetVersionGetResponse versionGetResponseOf(String json) {
        OpenRouterPresetVersionGetRequest request = client().presets().version("my-preset", "1").build();
        return request.createResponse(json);
    }
}
