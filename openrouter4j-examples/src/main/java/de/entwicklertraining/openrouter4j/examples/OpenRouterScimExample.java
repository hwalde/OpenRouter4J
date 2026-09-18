package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.scim.OpenRouterScimGroup;
import de.entwicklertraining.openrouter4j.scim.OpenRouterScimGroupMapping;
import de.entwicklertraining.openrouter4j.scim.OpenRouterScimGroupMappingsListResponse;
import de.entwicklertraining.openrouter4j.scim.OpenRouterScimSyncJob;

/**
 * Demonstrates the SCIM provisioning surface: group-to-workspace mappings,
 * the SCIM groups and the directory-sync jobs (management key required for
 * all of them - a normal inference key is rejected with an authorization
 * error).
 *
 * <p>Traps: re-creating the same mapping with the same role succeeds and
 * re-applies it, but a different role for an existing mapping is rejected
 * with HTTP 409 - use {@code updateGroupMapping} to change a role. Deleting
 * a mapping with {@code keepMembers(false)} removes the workspace access of
 * members that arrived through the SCIM group.
 */
public class OpenRouterScimExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // List the SCIM groups of the organization - the group ids are the
        // scim_group_id values the mapping endpoints accept.
        for (OpenRouterScimGroup group : client.scim().groups().execute().groups()) {
            System.out.println("Group " + group.displayName() + " (" + group.id() + ")");
        }

        // List the existing group-to-workspace mappings.
        OpenRouterScimGroupMappingsListResponse mappings =
                client.scim().groupMappings().limit(50).execute();
        System.out.println("Total mappings: " + mappings.totalCount());
        for (OpenRouterScimGroupMapping mapping : mappings.mappings()) {
            System.out.println("  group " + mapping.scimGroupId() + " -> workspace "
                    + mapping.workspaceId() + " as " + mapping.role());
        }

        // Create (or re-apply) a mapping: same role as an existing mapping
        // re-applies it; a different role for an existing mapping is a 409.
        OpenRouterScimGroupMapping created = client.scim().createGroupMapping()
                .role("member")
                .scimGroupId("550e8400-e29b-41d4-a716-446655440000")
                .workspaceId("660e8400-e29b-41d4-a716-446655440000")
                .execute()
                .mapping();
        System.out.println("Mapping id: " + (created == null ? null : created.id()));

        // Change the role of an existing mapping (PATCH, role only).
        if (created != null) {
            System.out.println("Updated role: "
                    + client.scim().updateGroupMapping(created.id()).role("admin").execute().mapping().role());
        }

        // Start a directory sync and poll it (terminal states: succeeded, failed).
        OpenRouterScimSyncJob job = client.scim().startSyncJob().execute().job();
        if (job != null) {
            System.out.println("Sync job " + job.id() + " queued");
            OpenRouterScimSyncJob polled = client.scim().syncJob(job.id()).execute().job();
            System.out.println("Sync status: " + polled.status()
                    + (polled.errorMessage() != null ? " (" + polled.errorMessage() + ")" : ""));
        }
    }
}
