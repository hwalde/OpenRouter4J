package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrail;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailCreateResponse;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailKeyAssignmentsListResponse;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailsListResponse;

import java.util.List;

/**
 * Demonstrates the guardrail endpoints. All of them require a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * - a normal inference key is rejected with an authorization error.
 *
 * <p>Guardrails are OpenRouter's server-side request policy layer: allowed
 * models/providers, data regions, spend limits, content filters and
 * zero-data-retention enforcement. A created guardrail enforces nothing until
 * it is assigned to API keys or members. This example creates a guardrail,
 * assigns an API key to it and deletes it again.
 */
public class OpenRouterGuardrailsExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // 1. List the guardrails of the account.
        OpenRouterGuardrailsListResponse list = client.guardrails().list().execute();
        List<OpenRouterGuardrail> guardrails = list.items();
        System.out.println("Guardrails: " + guardrails.size());
        for (OpenRouterGuardrail guardrail : guardrails) {
            System.out.printf("  %s, providers: %s, limit: %s USD per %s%n",
                    guardrail.name(),
                    guardrail.allowedProviders(),
                    guardrail.limitUsd(),
                    guardrail.resetInterval());
        }

        // 2. Which API keys are governed by guardrails today (global list)?
        OpenRouterGuardrailKeyAssignmentsListResponse<?> assignments =
                client.guardrails().allKeyAssignments().limit(10).execute();
        System.out.println("Key assignments: " + assignments.items().size());

        // 3. Create a guardrail: EU traffic only, two providers, a monthly
        //    spend limit of 50 USD (limit_usd and reset_interval must always
        //    be set together) and a builtin content filter.
        OpenRouterGuardrailCreateResponse created = client.guardrails().create()
                .name("Example EU Guardrail")
                .description("Created by OpenRouterGuardrailsExample")
                .allowedProviders(List.of("openai", "anthropic"))
                .allowedDataRegions(List.of("europe"))
                .limitUsd(50.0)
                .resetInterval("monthly")
                .addBuiltinContentFilter("email", "redact")
                .execute();
        String id = created.data() != null ? created.data().id() : null;
        System.out.println("Created guardrail " + id);

        // 4. Assign an existing API key (by hash, see client.keys().list()).
        //    client.guardrails().assignKeys(id).addKeyHash("<key hash>").execute();
        //    ... and members by user id (see client.organization().members()):
        //    client.guardrails().assignMembers(id).addMemberUserId("<user id>").execute();

        // 5. Read the guardrail back.
        OpenRouterGuardrail fetched = client.guardrails().get(id).execute().data();
        System.out.println("Fetched guardrail " + fetched.name()
                + ", regions: " + fetched.allowedDataRegions());

        // 6. Delete it (permanent).
        Boolean deleted = client.guardrails().delete(id).execute().deleted();
        System.out.println("Deleted: " + deleted);
    }
}
