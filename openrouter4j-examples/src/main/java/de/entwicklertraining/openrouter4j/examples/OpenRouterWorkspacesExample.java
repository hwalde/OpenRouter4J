package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterOrganizationMember;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspace;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspaceBudget;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspaceCreateResponse;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspaceMembersListResponse;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspacesListResponse;

import java.util.List;

/**
 * Demonstrates the workspace, budget and organization endpoints. All of them
 * require a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * - a normal inference key is rejected with an authorization error.
 *
 * <p>Workspaces group API keys, guardrails and budgets. Budgets guard the
 * same spend per interval (daily, weekly, monthly, lifetime). This example
 * creates a throwaway workspace, reads it, upserts a monthly budget and
 * deletes the workspace again.
 */
public class OpenRouterWorkspacesExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // 1. List the workspaces of the account.
        OpenRouterWorkspacesListResponse list = client.workspaces().list().execute();
        List<OpenRouterWorkspace> workspaces = list.items();
        System.out.println("Workspaces: " + workspaces.size());
        for (OpenRouterWorkspace workspace : workspaces) {
            System.out.printf("  %s (slug %s), default text model: %s%n",
                    workspace.name(), workspace.slug(), workspace.defaultTextModel());
        }

        // 2. The members of the organization - their user ids feed the
        //    members add/remove requests and the guardrail assignments.
        OpenRouterWorkspaceMembersListResponse defaultMembers =
                client.workspaces().members(workspaces.get(0).id()).limit(10).execute();
        System.out.println("Members of the first workspace: " + defaultMembers.items().size());

        // 3. Create a throwaway workspace (slug is URL-friendly and renameable
        //    later via PATCH /workspaces/{id}).
        OpenRouterWorkspaceCreateResponse created = client.workspaces().create()
                .name("Example Workspace")
                .slug("example-workspace")
                .description("Created by OpenRouterWorkspacesExample")
                .defaultProviderSort("price")
                // Governance: disable server tools for this workspace - a
                // request naming a disabled tool is rejected with HTTP 403.
                .disabledServerTools("openrouter:bash", "openrouter:shell")
                .execute();
        String id = created.data() != null ? created.data().id() : null;
        System.out.println("Created workspace " + id);

        // 4. Upsert a monthly budget of 25 USD (limit must be greater than 0).
        client.workspaces().upsertBudget("example-workspace", "monthly")
                .limitUsd(25.0)
                .execute();
        OpenRouterWorkspaceBudget budget =
                client.workspaces().budget("example-workspace", "monthly").execute().data();
        System.out.println("Monthly budget: " + (budget != null ? budget.limitUsd() : null) + " USD");

        // 5. Delete the workspace (permanent).
        Boolean deleted = client.workspaces().delete(id).execute().deleted();
        System.out.println("Deleted: " + deleted);

        // 6. The organization members (management key required).
        List<OpenRouterOrganizationMember> orgMembers = client.organization().members().limit(10).execute().items();
        System.out.println("Organization members: " + orgMembers.size());
    }
}
