package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates the typed accessors for the {@code openrouter_metadata} routing
 * object. The metadata is a per-request opt-in via
 * {@code metadataInResponse(true)} (the {@code X-OpenRouter-Metadata: enabled}
 * header) and captures what the router did: which model was requested, which
 * routing strategy ran, which provider actually served the request, how long
 * the upstream generation took, whether the router retried against fallbacks
 * and which pipeline stages (context compression, guardrails, ...) materially
 * altered the request.
 *
 * Traps documented in the javadoc:
 * - on streaming responses the metadata arrives on the final chunk before
 *   [DONE] (the streaming accumulator captures it);
 * - cache hits never include openrouter_metadata.
 */
public class OpenRouterChatCompletionWithRouterMetadataExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .metadataInResponse(true) // opt in to openrouter_metadata
                .addMessage("user", "Say hello in exactly three words.")
                .execute();

        System.out.println("Answer: " + response.assistantMessage());

        if (response.openrouterMetadata() != null) {
            System.out.println("Requested model: " + response.metadataRequestedModel());
            System.out.println("Routing strategy: " + response.metadataRoutingStrategy());
            System.out.println("Region: " + response.metadataRegion());
            System.out.println("Router summary: " + response.metadataSummary());
            System.out.println("Attempt: " + response.metadataAttempt());
            System.out.println("BYOK: " + response.metadataIsByok());
            System.out.println("Generation time (ms): " + response.metadataGenerationTimeMs());
            // The provider that actually served the request, from
            // endpoints.available[].selected - null when no endpoint was selected
            // (e.g. guardrail-blocked error responses):
            System.out.println("Selected provider: " + response.metadataSelectedProvider());
            System.out.println("Endpoints snapshot: " + response.metadataEndpoints());
            System.out.println("Router params: " + response.metadataParams());
            // Present when the router retried against fallbacks:
            System.out.println("Attempts: " + response.metadataAttempts().size());
            // Pipeline stages that materially altered the request/response
            // (context compression, guardrails, healing, server tools, ...):
            System.out.println("Pipeline stages: " + response.metadataPipeline().size());
        } else {
            // Absent when the opt-in header was not sent - or on cache hits,
            // which never include the metadata.
            System.out.println("No routing metadata on this response (cache hit?).");
        }
    }
}
