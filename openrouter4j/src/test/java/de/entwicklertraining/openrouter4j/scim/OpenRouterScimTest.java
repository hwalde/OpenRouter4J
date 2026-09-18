package de.entwicklertraining.openrouter4j.scim;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the SCIM provisioning requests (all eight operations: URL shape,
 * pagination, id URL-encoding, body fields, build validation) and the
 * response accessors against the recorded JSON shapes of the OpenRouter
 * /scim endpoints.
 */
class OpenRouterScimTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void groupMappingsListCarriesPaginationOnly() {
        OpenRouterScimGroupMappingsListRequest request =
                client().scim().groupMappings().offset(5).limit(10).build();
        assertThat(request.getRelativeUrl()).isEqualTo("/scim/group-mappings?offset=5&limit=10");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void groupMappingCreateEmitsExactlyTheSchemaBody() {
        OpenRouterScimGroupMappingCreateRequest request = client().scim().createGroupMapping()
                .role("member")
                .scimGroupId("550e8400-e29b-41d4-a716-446655440000")
                .workspaceId("660e8400-e29b-41d4-a716-446655440000")
                .build();
        assertThat(request.getRelativeUrl()).isEqualTo("/scim/group-mappings");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("role", "scim_group_id", "workspace_id");
        assertThat(body.getString("role")).isEqualTo("member");
        assertThat(body.getString("scim_group_id")).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(body.getString("workspace_id")).isEqualTo("660e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void groupMappingCreateValidatesLoudly() {
        assertThatThrownBy(() -> client().scim().createGroupMapping().role("owner").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("admin");
        assertThatThrownBy(() -> client().scim().createGroupMapping()
                .scimGroupId("550e8400-e29b-41d4-a716-446655440000")
                .workspaceId("660e8400-e29b-41d4-a716-446655440000")
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("role");
        assertThatThrownBy(() -> client().scim().createGroupMapping()
                .role("admin")
                .workspaceId("660e8400-e29b-41d4-a716-446655440000")
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("scimGroupId");
    }

    @Test
    void groupMappingGetEncodesTheId() {
        OpenRouterScimGroupMappingGetRequest request =
                client().scim().groupMapping("770e8400-e29b-41d4-a716-446655440000").build();
        assertThat(request.getRelativeUrl())
                .isEqualTo("/scim/group-mappings/770e8400-e29b-41d4-a716-446655440000");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void groupMappingGetEncodesSpecialCharacters() {
        OpenRouterScimGroupMappingGetRequest request =
                client().scim().groupMapping("id/with spaces").build();
        assertThat(request.getRelativeUrl()).isEqualTo("/scim/group-mappings/id%2Fwith+spaces");
    }

    @Test
    void groupMappingUpdateEmitsRoleOnly() {
        OpenRouterScimGroupMappingUpdateRequest request = client().scim().updateGroupMapping("some-id")
                .role("admin")
                .build();
        assertThat(request.getRelativeUrl()).isEqualTo("/scim/group-mappings/some-id");
        assertThat(request.getHttpMethod()).isEqualTo("PATCH");
        assertThat(new JSONObject(request.getBody()).keySet()).containsExactly("role");
        assertThat(new JSONObject(request.getBody()).getString("role")).isEqualTo("admin");
    }

    @Test
    void groupMappingUpdateRequiresTheRole() {
        assertThatThrownBy(() -> client().scim().updateGroupMapping("some-id").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("role");
    }

    @Test
    void groupMappingDeleteCarriesKeepMembersQuery() {
        OpenRouterScimGroupMappingDeleteRequest request = client().scim().deleteGroupMapping("some-id")
                .keepMembers(false)
                .build();
        assertThat(request.getRelativeUrl()).isEqualTo("/scim/group-mappings/some-id?keep_members=false");
        assertThat(request.getHttpMethod()).isEqualTo("DELETE");
        assertThat(request.getBody()).isNull();

        OpenRouterScimGroupMappingDeleteRequest keeping = client().scim().deleteGroupMapping("some-id")
                .keepMembers(true)
                .build();
        assertThat(keeping.getRelativeUrl()).isEqualTo("/scim/group-mappings/some-id?keep_members=true");
    }

    @Test
    void groupMappingDeleteWithoutKeepMembersOmitsTheQuery() {
        OpenRouterScimGroupMappingDeleteRequest request =
                client().scim().deleteGroupMapping("some-id").build();
        assertThat(request.getRelativeUrl()).isEqualTo("/scim/group-mappings/some-id");
    }

    @Test
    void groupsListCarriesPaginationOnly() {
        OpenRouterScimGroupsListRequest request = client().scim().groups().limit(50).build();
        assertThat(request.getRelativeUrl()).isEqualTo("/scim/groups?limit=50");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();

        OpenRouterScimGroupsListRequest paged = client().scim().groups().offset(5).limit(10).build();
        assertThat(paged.getRelativeUrl()).isEqualTo("/scim/groups?offset=5&limit=10");
    }

    @Test
    void syncJobCreateSendsAnEmptyBody() {
        OpenRouterScimSyncJobCreateRequest request = client().scim().startSyncJob().build();
        assertThat(request.getRelativeUrl()).isEqualTo("/scim/sync-jobs");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
        assertThat(request.getBody()).isEqualTo("{}");
    }

    @Test
    void syncJobGetEncodesTheId() {
        OpenRouterScimSyncJobGetRequest request =
                client().scim().syncJob("880e8400-e29b-41d4-a716-446655440000").build();
        assertThat(request.getRelativeUrl())
                .isEqualTo("/scim/sync-jobs/880e8400-e29b-41d4-a716-446655440000");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void requestsRejectMissingIds() {
        assertThatThrownBy(() -> new OpenRouterScimGroupMappingGetRequest.Builder(client(), null).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("id");
        assertThatThrownBy(() -> new OpenRouterScimGroupMappingDeleteRequest.Builder(client(), "").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("id");
        assertThatThrownBy(() -> new OpenRouterScimSyncJobGetRequest.Builder(client(), null).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("id");
    }

    @Test
    void mappingsListResponseSurfacesTheRecordedFixture() {
        OpenRouterScimGroupMappingsListResponse response = mappingsListResponseOf("""
                {
                  "data": [
                    {
                      "created_at": "2025-08-24T10:30:00.000Z",
                      "id": "770e8400-e29b-41d4-a716-446655440000",
                      "organization_id": "org_123456",
                      "role": "member",
                      "scim_group_id": "550e8400-e29b-41d4-a716-446655440000",
                      "updated_at": "2025-08-24T10:30:00.000Z",
                      "workspace_id": "660e8400-e29b-41d4-a716-446655440000"
                    }
                  ],
                  "total_count": 1
                }
                """);
        assertThat(response.totalCount()).isEqualTo(1L);
        assertThat(response.mappings()).hasSize(1);
        OpenRouterScimGroupMapping mapping = response.mappings().get(0);
        assertThat(mapping.id()).isEqualTo("770e8400-e29b-41d4-a716-446655440000");
        assertThat(mapping.role()).isEqualTo("member");
        assertThat(mapping.scimGroupId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(mapping.workspaceId()).isEqualTo("660e8400-e29b-41d4-a716-446655440000");
        assertThat(mapping.organizationId()).isEqualTo("org_123456");
    }

    @Test
    void mutationResponseSurfacesTheMapping() {
        OpenRouterScimGroupMappingCreateRequest request = client().scim().createGroupMapping()
                .role("admin")
                .scimGroupId("550e8400-e29b-41d4-a716-446655440000")
                .workspaceId("660e8400-e29b-41d4-a716-446655440000")
                .build();
        OpenRouterScimGroupMappingMutationResponse<OpenRouterScimGroupMappingCreateRequest> response =
                request.createResponse("""
                {
                  "data": {
                    "created_at": "2025-08-24T10:30:00.000Z",
                    "id": "770e8400-e29b-41d4-a716-446655440000",
                    "organization_id": "org_123456",
                    "role": "admin",
                    "scim_group_id": "550e8400-e29b-41d4-a716-446655440000",
                    "updated_at": "2025-08-24T10:30:00.000Z",
                    "workspace_id": "660e8400-e29b-41d4-a716-446655440000"
                  }
                }
                """);
        assertThat(response.mapping().role()).isEqualTo("admin");
    }

    @Test
    void deleteResponseSurfacesDeletedFlag() {
        OpenRouterScimGroupMappingDeleteRequest request =
                client().scim().deleteGroupMapping("some-id").build();
        OpenRouterScimGroupMappingDeleteResponse response =
                request.createResponse("{\"deleted\": true}");
        assertThat(response.deleted()).isTrue();
        assertThat(request.createResponse("{}").deleted()).isNull();
    }

    @Test
    void groupsListResponseSurfacesTheRecordedFixture() {
        OpenRouterScimGroupsListResponse response = groupsListResponseOf("""
                {
                  "data": [
                    {
                      "created_at": "2025-08-24T10:30:00.000Z",
                      "display_name": "Engineering",
                      "external_id": "group-external-id",
                      "id": "550e8400-e29b-41d4-a716-446655440000",
                      "organization_id": "org_123456",
                      "updated_at": "2025-08-24T10:30:00.000Z"
                    }
                  ],
                  "total_count": 1
                }
                """);
        assertThat(response.totalCount()).isEqualTo(1L);
        assertThat(response.groups()).hasSize(1);
        OpenRouterScimGroup group = response.groups().get(0);
        assertThat(group.displayName()).isEqualTo("Engineering");
        assertThat(group.externalId()).isEqualTo("group-external-id");
        assertThat(group.id()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void syncJobResponseSurfacesTheRecordedFixture() {
        OpenRouterScimSyncJobCreateRequest create = client().scim().startSyncJob().build();
        OpenRouterScimSyncJobResponse<OpenRouterScimSyncJobCreateRequest> created =
                create.createResponse("""
                {
                  "data": {
                    "created_at": "2025-08-24T10:30:00.000Z",
                    "deleted_groups": null,
                    "error_message": null,
                    "finished_at": null,
                    "id": "880e8400-e29b-41d4-a716-446655440000",
                    "started_at": null,
                    "status": "queued",
                    "synced_groups": null
                  }
                }
                """);
        assertThat(created.job().status()).isEqualTo("queued");
        assertThat(created.job().isTerminal()).isFalse();

        OpenRouterScimSyncJobGetRequest get = client().scim()
                .syncJob("880e8400-e29b-41d4-a716-446655440000").build();
        OpenRouterScimSyncJobResponse<OpenRouterScimSyncJobGetRequest> polled =
                get.createResponse("""
                {
                  "data": {
                    "created_at": "2025-08-24T10:30:00.000Z",
                    "deleted_groups": 2,
                    "error_message": null,
                    "finished_at": "2025-08-24T10:35:00.000Z",
                    "id": "880e8400-e29b-41d4-a716-446655440000",
                    "started_at": "2025-08-24T10:31:00.000Z",
                    "status": "succeeded",
                    "synced_groups": 14
                  }
                }
                """);
        OpenRouterScimSyncJob job = polled.job();
        assertThat(job.isSucceeded()).isTrue();
        assertThat(job.isTerminal()).isTrue();
        assertThat(job.isFailed()).isFalse();
        assertThat(job.syncedGroups()).isEqualTo(14L);
        assertThat(job.deletedGroups()).isEqualTo(2L);
        assertThat(job.errorMessage()).isNull();
        assertThat(job.startedAt()).isEqualTo("2025-08-24T10:31:00.000Z");
        assertThat(job.finishedAt()).isEqualTo("2025-08-24T10:35:00.000Z");
    }

    @Test
    void syncJobResponseSurfacesFailureFields() {
        OpenRouterScimSyncJobGetRequest get = client().scim()
                .syncJob("880e8400-e29b-41d4-a716-446655440000").build();
        OpenRouterScimSyncJobResponse<OpenRouterScimSyncJobGetRequest> polled =
                get.createResponse("""
                {
                  "data": {
                    "created_at": "2025-08-24T10:30:00.000Z",
                    "deleted_groups": null,
                    "error_message": "directory unreachable",
                    "finished_at": "2025-08-24T10:32:00.000Z",
                    "id": "880e8400-e29b-41d4-a716-446655440000",
                    "started_at": "2025-08-24T10:31:00.000Z",
                    "status": "failed",
                    "synced_groups": null
                  }
                }
                """);
        assertThat(polled.job().isFailed()).isTrue();
        assertThat(polled.job().errorMessage()).isEqualTo("directory unreachable");
    }

    @Test
    void responsesOnEmptyBodyReturnNullsAndEmptyLists() {
        assertThat(mappingsListResponseOf("{}").mappings()).isEmpty();
        assertThat(mappingsListResponseOf("{}").totalCount()).isNull();
        assertThat(groupsListResponseOf("{}").groups()).isEmpty();

        OpenRouterScimGroupMappingCreateRequest create = client().scim().createGroupMapping()
                .role("member")
                .scimGroupId("g")
                .workspaceId("w")
                .build();
        assertThat(create.createResponse("{}").mapping()).isNull();
    }

    private OpenRouterScimGroupMappingsListResponse mappingsListResponseOf(String json) {
        OpenRouterScimGroupMappingsListRequest request = client().scim().groupMappings().build();
        return request.createResponse(json);
    }

    private OpenRouterScimGroupsListResponse groupsListResponseOf(String json) {
        OpenRouterScimGroupsListRequest request = client().scim().groups().build();
        return request.createResponse(json);
    }
}
