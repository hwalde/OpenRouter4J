package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the workspace and organization management requests (URL shape, body
 * presence/absence, path-segment URL encoding, loud validation) and the
 * response accessors against recorded JSON shapes of the OpenRouter
 * /workspaces, /organization endpoints.
 */
class OpenRouterWorkspacesTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void listRequestEmitsTypedQueryParameters() {
        OpenRouterWorkspacesListRequest request = new OpenRouterWorkspacesListRequest.Builder(client())
                .offset(5)
                .limit(10)
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/workspaces?offset=5&limit=10");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();

        assertThat(new OpenRouterWorkspacesListRequest.Builder(client()).build().getRelativeUrl())
                .isEqualTo("/workspaces");
    }

    @Test
    void createRequestEmitsOnlyConfiguredFields() {
        OpenRouterWorkspaceCreateRequest request = new OpenRouterWorkspaceCreateRequest.Builder(client())
                .name("Production")
                .slug("production")
                .description("Production environment workspace")
                .defaultTextModel("openai/gpt-4o")
                .defaultProviderSort("price")
                .ioLoggingSamplingRate(1.0)
                .isObservabilityBroadcastEnabled(false)
                .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder(
                "name", "slug", "description", "default_text_model",
                "default_provider_sort", "io_logging_sampling_rate",
                "is_observability_broadcast_enabled");
        assertThat(body.get("name")).isEqualTo("Production");
        assertThat(body.get("slug")).isEqualTo("production");
        assertThat(body.getDouble("io_logging_sampling_rate")).isEqualTo(1.0);
        assertThat(body.getBoolean("is_observability_broadcast_enabled")).isFalse();
        assertThat(request.getRelativeUrl()).isEqualTo("/workspaces");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void createRequestRejectsMissingNameOrSlug() {
        assertThatThrownBy(() -> new OpenRouterWorkspaceCreateRequest.Builder(client())
                .slug("production").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("name");
        assertThatThrownBy(() -> new OpenRouterWorkspaceCreateRequest.Builder(client())
                .name("Production").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("slug");
    }

    @Test
    void createRequestCarriesIntegerListVerbatim() {
        OpenRouterWorkspaceCreateRequest request = new OpenRouterWorkspaceCreateRequest.Builder(client())
                .name("Production")
                .slug("production")
                .ioLoggingApiKeyIds(List.of(1, 2, 3))
                .build();

        assertThat(new JSONObject(request.getBody()).getJSONArray("io_logging_api_key_ids").toList())
                .containsExactly(1, 2, 3);
    }

    @Test
    void getAndDeleteRequestsUrlEncodeTheId() {
        assertThat(client().workspaces().get("id with space").build().getRelativeUrl())
                .isEqualTo("/workspaces/id+with+space");
        assertThat(client().workspaces().get("ws/id").build().getRelativeUrl())
                .isEqualTo("/workspaces/ws%2Fid");

        OpenRouterWorkspaceDeleteRequest delete = client().workspaces().delete("ws-1")
                .confirmDefaultWorkspaceDeletion(true)
                .build();
        assertThat(delete.getRelativeUrl())
                .isEqualTo("/workspaces/ws-1?confirm_default_workspace_deletion=true");
        assertThat(delete.getHttpMethod()).isEqualTo("DELETE");
        assertThat(delete.getBody()).isNull();
        assertThat(delete.id()).isEqualTo("ws-1");
    }

    @Test
    void updateRequestEmitsOnlyConfiguredFields() {
        OpenRouterWorkspaceUpdateRequest request = new OpenRouterWorkspaceUpdateRequest.Builder(client(), "ws-1")
                .name("Updated Workspace")
                .slug("updated-workspace")
                .isObservabilityBroadcastEnabled(true)
                .build();

        assertThat(request.getHttpMethod()).isEqualTo("PATCH");
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder(
                "name", "slug", "is_observability_broadcast_enabled");
        assertThat(body.get("slug")).isEqualTo("updated-workspace");
        assertThat(body.has("description")).isFalse();
        assertThat(body.has("default_text_model")).isFalse();
    }

    @Test
    void updateRequestRejectsEmptyId() {
        assertThatThrownBy(() -> client().workspaces().update("").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("id");
    }

    @Test
    void memberRequestsValidateNonEmptyUserIds() {
        assertThatThrownBy(() -> client().workspaces().addMembers("ws-1").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least one");

        OpenRouterWorkspaceMembersAddRequest add = client().workspaces().addMembers("ws-1")
                .addUserId("user_abc123")
                .addUserId("user_def456")
                .build();
        assertThat(new JSONObject(add.getBody()).getJSONArray("user_ids").toList())
                .containsExactly("user_abc123", "user_def456");
        assertThat(add.getRelativeUrl()).isEqualTo("/workspaces/ws-1/members/add");

        OpenRouterWorkspaceMembersRemoveRequest remove = client().workspaces().removeMembers("ws-1")
                .addUserId("user_abc123")
                .build();
        assertThat(remove.getRelativeUrl()).isEqualTo("/workspaces/ws-1/members/remove");
        assertThat(new JSONObject(remove.getBody()).getJSONArray("user_ids").toList())
                .containsExactly("user_abc123");
        assertThat(new JSONObject(remove.getBody()).has("member_user_ids")).isFalse();

        assertThatThrownBy(() -> {
            OpenRouterWorkspaceMembersAddRequest.Builder builder = client().workspaces().addMembers("ws-1");
            for (int i = 0; i <= 100; i++) {
                builder = builder.addUserId("u" + i);
            }
            builder.build();
        }).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("100");
    }

    @Test
    void budgetUpsertValidatesLimitAndInterval() {
        assertThatThrownBy(() -> client().workspaces().upsertBudget("ws-1", "monthly").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("limitUsd");
        assertThatThrownBy(() -> client().workspaces().upsertBudget("ws-1", "monthly").limitUsd(0.0).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater than 0");

        OpenRouterWorkspaceBudgetUpsertRequest request = client().workspaces()
                .upsertBudget("prod", "monthly")
                .limitUsd(100.0)
                .includeByokInBudgets(true)
                .build();
        assertThat(request.getRelativeUrl()).isEqualTo("/workspaces/prod/budgets/monthly");
        assertThat(request.getHttpMethod()).isEqualTo("PUT");
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getDouble("limit_usd")).isEqualTo(100.0);
        assertThat(body.getBoolean("include_byok_in_budgets")).isTrue();
    }

    @Test
    void listResponseSurfacesTypedWorkspaces() {
        OpenRouterWorkspacesListResponse response = responseOf("""
                {
                  "data": [
                    {
                      "id": "550e8400-e29b-41d4-a716-446655440000",
                      "default_guardrail_id": "595d5849-7e86-51fd-a7c0-705c34e4afff",
                      "name": "Production",
                      "slug": "production",
                      "description": "Production environment workspace",
                      "default_text_model": "openai/gpt-4o",
                      "default_image_model": "openai/dall-e-3",
                      "default_provider_sort": "price",
                      "is_observability_io_logging_enabled": false,
                      "is_observability_broadcast_enabled": false,
                      "is_data_discount_logging_enabled": true,
                      "io_logging_sampling_rate": 1,
                      "io_logging_api_key_ids": [1, 2],
                      "include_byok_in_budgets": false,
                      "created_at": "2025-08-24T10:30:00Z",
                      "updated_at": "2025-08-24T15:45:00Z",
                      "created_by": "user_abc123"
                    }
                  ],
                  "total_count": 1
                }
                """, json -> new OpenRouterWorkspacesListResponse(json, null));

        assertThat(response.totalCount()).isEqualTo(1);
        OpenRouterWorkspace workspace = response.items().get(0);
        assertThat(workspace.id()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(workspace.name()).isEqualTo("Production");
        assertThat(workspace.slug()).isEqualTo("production");
        assertThat(workspace.defaultTextModel()).isEqualTo("openai/gpt-4o");
        assertThat(workspace.defaultProviderSort()).isEqualTo("price");
        assertThat(workspace.isDataDiscountLoggingEnabled()).isTrue();
        assertThat(workspace.ioLoggingSamplingRate()).isEqualTo(1.0);
        assertThat(workspace.ioLoggingApiKeyIds()).containsExactly(1, 2);
        assertThat(workspace.defaultGuardrailId()).isEqualTo("595d5849-7e86-51fd-a7c0-705c34e4afff");
        assertThat(workspace.createdBy()).isEqualTo("user_abc123");
    }

    @Test
    void responsesReturnNullAndEmptyWhenAbsent() {
        OpenRouterWorkspaceGetResponse get = responseOf("{}", json -> new OpenRouterWorkspaceGetResponse(json, null));
        assertThat(get.data()).isNull();

        OpenRouterWorkspaceMembersListResponse members = responseOf("{\"total_count\": 0}",
                json -> new OpenRouterWorkspaceMembersListResponse(json, null));
        assertThat(members.items()).isEmpty();
        assertThat(members.totalCount()).isEqualTo(0);

        OpenRouterWorkspaceDeleteResponse deleted = responseOf("{}",
                json -> new OpenRouterWorkspaceDeleteResponse(json, null));
        assertThat(deleted.deleted()).isNull();
    }

    @Test
    void organizationMembersResponseSurfacesTypedMembers() {
        OpenRouterOrganizationMembersListResponse response = responseOf("""
                {
                  "data": [
                    {
                      "id": "user_2dHFtVWx2n56w6HkM0000000000",
                      "first_name": "Jane",
                      "last_name": "Doe",
                      "email": "jane.doe@example.com",
                      "role": "org:member"
                    }
                  ],
                  "total_count": 25
                }
                """, json -> new OpenRouterOrganizationMembersListResponse(json, null));

        assertThat(response.totalCount()).isEqualTo(25);
        OpenRouterOrganizationMember member = response.items().get(0);
        assertThat(member.id()).isEqualTo("user_2dHFtVWx2n56w6HkM0000000000");
        assertThat(member.firstName()).isEqualTo("Jane");
        assertThat(member.lastName()).isEqualTo("Doe");
        assertThat(member.email()).isEqualTo("jane.doe@example.com");
        assertThat(member.role()).isEqualTo("org:member");
    }

    private <T> T responseOf(String json, java.util.function.Function<JSONObject, T> factory) {
        return factory.apply(new JSONObject(json));
    }
}
