package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates {@code service_tier}: the explicit way to pin a request to a
 * capacity tier, in contrast to the {@code :nitro}/{@code :floor} model variants.
 *
 * Accepted values: "auto", "default", "fast" (alias for "priority"), "flex",
 * "priority" and "scale". "flex" never falls back to default-tier endpoints,
 * while "priority" is tried first.
 *
 * The tier that actually served the request is echoed back in the top-level
 * {@code service_tier} response field and surfaced via
 * {@code OpenRouterChatCompletionResponse#serviceTier()}.
 */
public class OpenRouterChatCompletionWithServiceTierExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("google/gemini-3.5-flash-lite")
                // "flex" is the cheap tier; it never falls back to default-tier endpoints
                .serviceTier("flex")
                .addMessage("user", "Say hello in one short sentence.")
                .execute();

        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("Tier actually used: " + response.serviceTier());
    }
}
