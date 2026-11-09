package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.privateendpoints.OpenRouterManagedPrivateEndpoint;
import de.entwicklertraining.openrouter4j.privateendpoints.OpenRouterPrivateEndpoint;
import de.entwicklertraining.openrouter4j.privateendpoints.OpenRouterPrivateEndpointPricing;
import de.entwicklertraining.openrouter4j.privateendpoints.OpenRouterPrivateEndpointSettingsUpdate;
import de.entwicklertraining.openrouter4j.privateendpoints.OpenRouterPrivateEndpointValidation;
import de.entwicklertraining.openrouter4j.privateendpoints.OpenRouterPrivateEndpointsListResponse;
import java.util.List;

/**
 * Demonstrates the private endpoints management surface (management key and
 * the private endpoints entitlement required):
 *
 * - POST /private-endpoints (create a draft, with pricing, settings and an
 *   idempotency key)
 * - POST /private-endpoints/{id}/validate (live validation with a workspace's
 *   BYOK credential)
 * - POST /private-endpoints/{id}/activate (make the validated draft routable)
 * - GET /private-endpoints/{id} (read the full endpoint incl. the parameters
 *   the create response does not echo)
 * - GET /private-endpoints (list, drafts included)
 * - PATCH /private-endpoints/{id} (partial update; settings null clears)
 * - POST /private-endpoints/{id}/disable and /enable (pause and resume routing)
 * - DELETE /private-endpoints/{id} (with draft_only)
 *
 * The draft lifecycle in one call is create(...).activate(workspaceId).
 */
public class OpenRouterPrivateEndpointsExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // Create a draft. The response is a ManagedPrivateEndpoint and does
        // NOT echo declared_parameters or tested_parameters - read those with
        // get(). The Idempotency-Key header makes retries safe (a repeated
        // key with the same request replays the first endpoint).
        OpenRouterManagedPrivateEndpoint draft = client.privateEndpoints().create()
                .modelPermaslug("openai/gpt-4o-2024-08-06")
                .providerSlug("azure")
                .upstreamModelId("gpt-4o-prod")
                .baseUrl("https://contoso.openai.azure.com")
                .declaredParameters(List.of("tools", "tool_choice", "temperature"))
                .declaredRegion("europe")
                .declaredZdr(true)
                .pricing(OpenRouterPrivateEndpointPricing.builder()
                        .prompt("0.0000025")
                        .completion("0.00001")
                        .build())
                .settings(OpenRouterPrivateEndpointSettingsUpdate.builder()
                        .contextLength(128000)
                        .reasoningSupported(true)
                        .build())
                .idempotencyKey("wayfair-gpt-4o-eastus-2026-09")
                .execute()
                .data();
        System.out.println("Created draft " + draft.id() + " (" + draft.status() + ")");

        // Validate the draft against the upstream with a workspace's BYOK
        // credential. Failed checks are not an error: the call answers 200
        // with passed=false and the failed checks.
        OpenRouterPrivateEndpointValidation validation = client.privateEndpoints()
                .validate(draft.id(), "550e8400-e29b-41d4-a716-446655440000")
                .execute()
                .data();
        System.out.println("Validation passed: " + validation.passed()
                + " (" + validation.checks().size() + " checks)");

        // Activate the validated draft - 409 when never validated, when the
        // validation is stale, or when it is already active.
        OpenRouterManagedPrivateEndpoint active = client.privateEndpoints()
                .activate(draft.id())
                .execute()
                .data();
        System.out.println("Active: " + active.status());

        // The full endpoint view carries the upstream configuration, pricing
        // and the declared/tested parameters the managed view omits.
        OpenRouterPrivateEndpoint endpoint = client.privateEndpoints()
                .get(draft.id())
                .execute()
                .data();
        System.out.println("Endpoint " + endpoint.id()
                + " upstream=" + endpoint.upstreamModelId()
                + " declared=" + endpoint.declaredParameters()
                + " tested=" + endpoint.testedParameters());

        // List all endpoints of the organization, drafts included.
        OpenRouterPrivateEndpointsListResponse<?> all = client.privateEndpoints()
                .list()
                .execute();
        System.out.println("Endpoints: " + all.items().size());
        all.items().forEach(row -> System.out.println("  " + row.id()
                + " " + row.status() + " " + row.modelPermaslug()));

        // PATCH: only the fields present change. The settings object is a
        // partial update itself - here an explicit null clears the prompt cap
        // and restores the provider default for a flag.
        OpenRouterPrivateEndpoint updated = client.privateEndpoints()
                .update(draft.id())
                .settings(OpenRouterPrivateEndpointSettingsUpdate.builder()
                        .maxPromptTokens(null)
                        .completionsChat(null)
                        .build())
                .execute()
                .data();
        System.out.println("Updated: max_prompt_tokens="
                + updated.settings().maxPromptTokens());

        // Pause and resume routing without deleting the endpoint.
        System.out.println("Disabled: " + client.privateEndpoints()
                .disable(draft.id()).execute().data().status());
        System.out.println("Enabled: " + client.privateEndpoints()
                .enable(draft.id()).execute().data().status());

        // Delete it. draftOnly(true) refuses (409) when it was ever
        // activated - drop the flag to delete unconditionally.
        String deleted = client.privateEndpoints()
                .delete(draft.id())
                .draftOnly(true)
                .execute()
                .id();
        System.out.println("Deleted: " + deleted);
    }
}
