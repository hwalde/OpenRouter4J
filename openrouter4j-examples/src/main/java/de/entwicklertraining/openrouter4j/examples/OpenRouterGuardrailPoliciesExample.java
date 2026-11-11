package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.guardrailpolicies.OpenRouterGuardrailBudgetPolicyConfig;
import de.entwicklertraining.openrouter4j.guardrailpolicies.OpenRouterGuardrailPolicyAssignmentState;
import de.entwicklertraining.openrouter4j.guardrailpolicies.OpenRouterGuardrailPolicyDetail;
import de.entwicklertraining.openrouter4j.guardrailpolicies.OpenRouterGuardrailPolicyRequest;

import java.util.List;

/**
 * Demonstrates the guardrail policies surface - the typed successors of the
 * monolithic guardrail CRUD ({@link OpenRouterGuardrailsExample}): three
 * separately assignable policy types (budget, model availability, moderation)
 * with their own CRUD per workspace, workspace-wide (account-level) policies
 * and the per-key/per-member assignment endpoints.
 *
 * <p><b>A management key is required</b> for every call in this example - a
 * normal inference key is rejected with an authorization error.
 *
 * <p>Semantics worth knowing: a workspace-wide policy applies to every member
 * and key of an assigned workspace and cannot be loosened by a member/key
 * policy; a member/key holds at most one policy of each type per workspace;
 * when levels combine the stricter rule wins (filters union, block beats
 * redact, every budget checked separately - budgets are per-subject, never a
 * shared pool).
 */
public class OpenRouterGuardrailPoliciesExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // 1. Create a budget policy in a workspace: a 200 USD monthly limit
        //    (BYOK spend excluded), assigned to one API key and one member.
        var created = client.guardrailPolicies()
                .createBudgetPolicy()
                .name("Engineering monthly budget")
                .config(OpenRouterGuardrailBudgetPolicyConfig.builder()
                        .limitUsd(200.0)
                        .resetInterval("monthly")
                        .includeByokInBudgets(false)
                        .build())
                .workspaceId("<workspace id>")
                .addApiKeyHash("<key hash>")
                .addMemberId("<member user id>")
                .execute();
        String policyId = created.data() != null ? created.data().id() : null;
        System.out.println("created policy " + policyId);

        // 2. Read it back - the detail view carries the assignment lists.
        if (policyId != null) {
            OpenRouterGuardrailPolicyDetail detail = client.guardrailPolicies()
                    .getBudgetPolicy(policyId)
                    .workspaceId("<workspace id>")
                    .execute().data();
            if (detail != null) {
                System.out.printf("policy %s: %d members, %d keys, workspace default: %s%n",
                        detail.name(), detail.memberIds().size(), detail.apiKeyHashes().size(),
                        detail.isWorkspaceDefault());
            }
        }

        // 3. List the budget policies of the workspace (offset/limit omitted
        //    return the full list; total_count counts before pagination).
        var list = client.guardrailPolicies()
                .listBudgetPolicies()
                .workspaceId("<workspace id>")
                .execute();
        System.out.println("budget policies: " + list.items().size() + " of " + list.totalCount());

        // 4. A workspace-wide (account-level) budget policy that covers every
        //    workspace except the opted-out one.
        var accountPolicy = OpenRouterGuardrailPolicyRequest
                .budget("Account monthly budget", OpenRouterGuardrailBudgetPolicyConfig.builder()
                        .limitUsd(2000.0)
                        .resetInterval("monthly")
                        .includeByokInBudgets(true)
                        .build())
                .isDefault(true)
                .optedOutWorkspaceIds(List.of("<workspace id>"))
                .build();
        client.guardrailPolicies().createAccountPolicy().policy(accountPolicy).execute();

        // 5. Change the policy assignment of one API key - with the optimistic
        //    concurrency guard: read the current state first and pass it as
        //    expected_assignment (the PUT fails with 409 when the stored state
        //    changed since the read - re-read and retry then).
        var state = client.guardrailPolicies()
                .apiKeyPolicyAssignment("<key hash>")
                .workspaceId("<workspace id>")
                .policyType("budget")
                .execute().data();
        OpenRouterGuardrailPolicyAssignmentState expected =
                state != null ? state : OpenRouterGuardrailPolicyAssignmentState.of(List.of(), List.of(), false);
        if (policyId != null) {
            client.guardrailPolicies()
                    .changeApiKeyPolicyAssignment("<key hash>")
                    .workspaceId("<workspace id>")
                    .policyType("budget")
                    .policyId(policyId)
                    .assigned(true)
                    .expectedAssignment(expected)
                    .execute();
        }

        // 6. Update (the PUT is a full replace: name, config and workspace_id
        //    are all required; api_key_hashes and member_ids are complete new
        //    lists and must be sent together) and delete the policy again
        //    (permanent).
        if (policyId != null) {
            client.guardrailPolicies().updateBudgetPolicy(policyId)
                    .name("Engineering monthly budget (renamed)")
                    .config(OpenRouterGuardrailBudgetPolicyConfig.builder()
                            .limitUsd(250.0)
                            .resetInterval("monthly")
                            .includeByokInBudgets(false)
                            .build())
                    .workspaceId("<workspace id>")
                    .execute();
            // client.guardrailPolicies().deleteBudgetPolicy(policyId)
            //         .workspaceId("<workspace id>").execute();  // permanent
        }
    }
}
