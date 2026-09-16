package de.entwicklertraining.openrouter4j.guardrails;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the guardrail management requests (URL shape, body presence/absence,
 * path-segment URL encoding, loud validation) and the response accessors
 * against recorded JSON shapes of the OpenRouter /guardrails endpoints.
 */
class OpenRouterGuardrailsTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void listRequestEmitsTypedQueryParameters() {
        OpenRouterGuardrailsListRequest request = new OpenRouterGuardrailsListRequest.Builder(client())
                .offset(5)
                .limit(20)
                .workspaceId("0df9e665-d932-5740-b2c7-b52af166bc11")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/guardrails?offset=5&limit=20"
                + "&workspace_id=0df9e665-d932-5740-b2c7-b52af166bc11");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void createRequestEmitsOnlyConfiguredFields() {
        OpenRouterGuardrailCreateRequest request = new OpenRouterGuardrailCreateRequest.Builder(client())
                .name("My New Guardrail")
                .description("A guardrail for limiting API usage")
                .allowedProviders(List.of("openai", "anthropic"))
                .allowedDataRegions(List.of("europe"))
                .contentFilters(List.of(new JSONObject().put("action", "redact").put("pattern", "\\b(sk-[a-z]+)\\b")))
                .contentFilterBuiltins(List.of(new JSONObject().put("slug", "email").put("action", "redact")))
                .limitUsd(50.0)
                .resetInterval("monthly")
                .enforceZdrOpenai(true)
                .includeByokInBudgets(false)
                .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder(
                "name", "description", "allowed_providers", "allowed_data_regions",
                "content_filters", "content_filter_builtins", "limit_usd",
                "reset_interval", "enforce_zdr_openai", "include_byok_in_budgets");
        assertThat(body.get("name")).isEqualTo("My New Guardrail");
        assertThat(body.getJSONArray("allowed_providers").toList()).containsExactly("openai", "anthropic");
        assertThat(body.getJSONArray("allowed_data_regions").toList()).containsExactly("europe");
        assertThat(body.getJSONArray("content_filters").getJSONObject(0).getString("action")).isEqualTo("redact");
        assertThat(body.getJSONArray("content_filter_builtins").getJSONObject(0).getString("slug")).isEqualTo("email");
        assertThat(body.getDouble("limit_usd")).isEqualTo(50.0);
        assertThat(body.get("reset_interval")).isEqualTo("monthly");
        assertThat(body.getBoolean("enforce_zdr_openai")).isTrue();
        // unset fields never appear
        assertThat(body.has("allowed_models")).isFalse();
        assertThat(body.has("workspace_id")).isFalse();
        assertThat(body.has("model_catalog")).isFalse();
    }

    @Test
    void createRequestRejectsMissingName() {
        assertThatThrownBy(() -> new OpenRouterGuardrailCreateRequest.Builder(client()).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("name");
    }

    @Test
    void createRequestRejectsLimitWithoutResetInterval() {
        assertThatThrownBy(() -> new OpenRouterGuardrailCreateRequest.Builder(client())
                .name("G").limitUsd(50.0).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("together");
        assertThatThrownBy(() -> new OpenRouterGuardrailCreateRequest.Builder(client())
                .name("G").resetInterval("monthly").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("together");

        // both together are accepted
        OpenRouterGuardrailCreateRequest request = new OpenRouterGuardrailCreateRequest.Builder(client())
                .name("G").limitUsd(50.0).resetInterval("weekly").build();
        assertThat(new JSONObject(request.getBody()).keySet()).containsExactlyInAnyOrder("name", "limit_usd", "reset_interval");
    }

    @Test
    void getAndDeleteRequestsUrlEncodeTheId() {
        assertThat(client().guardrails().get("id/with slash").build().getRelativeUrl())
                .isEqualTo("/guardrails/id%2Fwith+slash");
        assertThat(client().guardrails().delete("g-1").build().getRelativeUrl()).isEqualTo("/guardrails/g-1");
        assertThat(client().guardrails().delete("g-1").build().getHttpMethod()).isEqualTo("DELETE");
        assertThat(client().guardrails().get("g-1").build().id()).isEqualTo("g-1");
    }

    @Test
    void assignmentRequestsCarryTheHashAndUserIdLists() {
        OpenRouterGuardrailKeysAssignRequest assignKeys = client().guardrails().assignKeys("g-1")
                .addKeyHash("c56454edb818d6b14bc0d61c46025f1450b0f4012d12304ab40aacb519fcbc93")
                .build();
        assertThat(assignKeys.getRelativeUrl()).isEqualTo("/guardrails/g-1/assignments/keys");
        assertThat(assignKeys.getHttpMethod()).isEqualTo("POST");
        assertThat(new JSONObject(assignKeys.getBody()).getJSONArray("key_hashes").toList())
                .containsExactly("c56454edb818d6b14bc0d61c46025f1450b0f4012d12304ab40aacb519fcbc93");

        assertThatThrownBy(() -> client().guardrails().assignKeys("g-1").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least one");

        OpenRouterGuardrailKeysUnassignRequest unassignKeys = client().guardrails().unassignKeys("g-1")
                .keyHashes(List.of("h1"))
                .build();
        assertThat(unassignKeys.getRelativeUrl()).isEqualTo("/guardrails/g-1/assignments/keys/remove");
        assertThat(new JSONObject(unassignKeys.getBody()).getJSONArray("key_hashes").toList()).containsExactly("h1");

        OpenRouterGuardrailMembersAssignRequest assignMembers = client().guardrails().assignMembers("g-1")
                .memberUserIds(List.of("user_abc123", "user_def456"))
                .build();
        assertThat(assignMembers.getRelativeUrl()).isEqualTo("/guardrails/g-1/assignments/members");
        assertThat(new JSONObject(assignMembers.getBody()).getJSONArray("member_user_ids").toList())
                .containsExactly("user_abc123", "user_def456");

        assertThatThrownBy(() -> client().guardrails().assignMembers("g-1").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least one");

        OpenRouterGuardrailMembersUnassignRequest unassignMembers = client().guardrails().unassignMembers("g-1")
                .addMemberUserId("user_abc123")
                .build();
        assertThat(unassignMembers.getRelativeUrl()).isEqualTo("/guardrails/g-1/assignments/members/remove");
        assertThat(new JSONObject(unassignMembers.getBody()).getJSONArray("member_user_ids").toList())
                .containsExactly("user_abc123");
    }

    @Test
    void assignmentListRequestsShareTheTypedResponses() {
        OpenRouterGuardrailKeyAssignmentsListRequest perGuardrail =
                client().guardrails().keyAssignments("g-1").limit(10).build();
        assertThat(perGuardrail.getRelativeUrl()).isEqualTo("/guardrails/g-1/assignments/keys?limit=10");
        assertThat(perGuardrail.createResponse("{\"data\": [], \"total_count\": 0}").items()).isEmpty();

        OpenRouterGuardrailAllKeyAssignmentsListRequest global =
                client().guardrails().allKeyAssignments().build();
        assertThat(global.getRelativeUrl()).isEqualTo("/guardrails/assignments/keys");
        OpenRouterGuardrailKeyAssignmentsListResponse<OpenRouterGuardrailAllKeyAssignmentsListRequest> globalResponse =
                global.createResponse("""
                        {
                          "data": [
                            {
                              "id": "550e8400-e29b-41d4-a716-446655440000",
                              "key_hash": "c56454edb818d6b14bc0d61c46025f1450b0f4012d12304ab40aacb519fcbc93",
                              "key_label": "prod-key",
                              "key_name": "Production Key",
                              "guardrail_id": "550e8400-e29b-41d4-a716-446655440001",
                              "assigned_by": "user_abc123",
                              "created_at": "2025-08-24T10:30:00Z"
                            }
                          ],
                          "total_count": 1
                        }
                        """);
        assertThat(globalResponse.items()).hasSize(1);
        assertThat(globalResponse.items().get(0).keyHash())
                .isEqualTo("c56454edb818d6b14bc0d61c46025f1450b0f4012d12304ab40aacb519fcbc93");
        assertThat(globalResponse.items().get(0).keyLabel()).isEqualTo("prod-key");
        assertThat(globalResponse.totalCount()).isEqualTo(1);

        OpenRouterGuardrailMemberAssignmentsListRequest memberAssignments =
                client().guardrails().memberAssignments("g-1").build();
        assertThat(memberAssignments.getRelativeUrl()).isEqualTo("/guardrails/g-1/assignments/members");
        assertThat(client().guardrails().allMemberAssignments().build().getRelativeUrl())
                .isEqualTo("/guardrails/assignments/members");
    }

    @Test
    void guardrailResponseSurfacesTypedFields() {
        OpenRouterGuardrailGetResponse response = new OpenRouterGuardrailGetResponse(new JSONObject("""
                {
                  "data": {
                    "id": "550e8400-e29b-41d4-a716-446655440000",
                    "name": "Production Guardrail",
                    "description": "Guardrail for production environment",
                    "allowed_models": ["openai/gpt-5.2"],
                    "allowed_providers": ["openai", "anthropic"],
                    "allowed_data_regions": ["europe"],
                    "content_filter_builtins": [{"slug": "email", "action": "redact", "label": "[EMAIL]"}],
                    "limit_usd": 100,
                    "reset_interval": "monthly",
                    "include_byok_in_budgets": false,
                    "enforce_zdr_anthropic": true,
                    "enforce_zdr_openai": true,
                    "enforce_zdr_google": false,
                    "model_catalog": {"sort": "explicit"},
                    "workspace_id": "0df9e665-d932-5740-b2c7-b52af166bc11",
                    "created_at": "2025-08-24T10:30:00Z",
                    "updated_at": "2025-08-24T15:45:00Z"
                  }
                }
                """), null);

        OpenRouterGuardrail guardrail = response.data();
        assertThat(guardrail.id()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(guardrail.name()).isEqualTo("Production Guardrail");
        assertThat(guardrail.allowedModels()).containsExactly("openai/gpt-5.2");
        assertThat(guardrail.allowedProviders()).containsExactly("openai", "anthropic");
        assertThat(guardrail.allowedDataRegions()).containsExactly("europe");
        assertThat(guardrail.contentFilterBuiltins()).hasSize(1);
        assertThat(guardrail.contentFilterBuiltins().get(0).getString("slug")).isEqualTo("email");
        assertThat(guardrail.limitUsd()).isEqualTo(100.0);
        assertThat(guardrail.resetInterval()).isEqualTo("monthly");
        assertThat(guardrail.enforceZdrAnthropic()).isTrue();
        assertThat(guardrail.enforceZdrGoogle()).isFalse();
        assertThat(guardrail.modelCatalog().getString("sort")).isEqualTo("explicit");
        assertThat(guardrail.workspaceId()).isEqualTo("0df9e665-d932-5740-b2c7-b52af166bc11");
    }

    @Test
    void responsesReturnNullAndEmptyWhenAbsent() {
        OpenRouterGuardrailGetResponse get = new OpenRouterGuardrailGetResponse(new JSONObject("{}"), null);
        assertThat(get.data()).isNull();

        OpenRouterGuardrailsListResponse list = new OpenRouterGuardrailsListResponse(new JSONObject("{}"), null);
        assertThat(list.items()).isEmpty();
        assertThat(list.totalCount()).isNull();

        OpenRouterGuardrailDeleteResponse deleted = new OpenRouterGuardrailDeleteResponse(new JSONObject("{}"), null);
        assertThat(deleted.deleted()).isNull();

        OpenRouterGuardrailKeysAssignResponse assigned =
                new OpenRouterGuardrailKeysAssignResponse(new JSONObject("{\"assigned_count\": 2}"), null);
        assertThat(assigned.assignedCount()).isEqualTo(2);
    }
}
