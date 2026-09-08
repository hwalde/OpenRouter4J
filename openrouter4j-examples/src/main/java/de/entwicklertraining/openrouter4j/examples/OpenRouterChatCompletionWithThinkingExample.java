package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * An example demonstrating the reasoning feature in the OpenRouter API.
 *
 * OpenRouter expects the reasoning configuration in the current format:
 * {@code "reasoning": {"effort": "...", "max_tokens": N, "exclude": ..., "enabled": ...}}.
 * The old {@code "reasoning": {"type": "enabled", "budget": N}} shape is obsolete and
 * is no longer sent by this library.
 *
 * This example shows different ways to configure reasoning:
 * 1. Without reasoning (default)
 * 2. With a reasoning token budget ({@code reasoning.max_tokens})
 * 3. With an effort hint ({@code reasoning.effort})
 * 4. With reasoning enabled but the output excluded from the response
 * 5. With a summary verbosity hint ({@code reasoning.summary})
 */
public class OpenRouterChatCompletionWithThinkingExample {

    public static void main(String[] args) {
        // Create the OpenRouter client
        OpenRouterClient client = new OpenRouterClient();

        String question = "Solve this math problem step by step: If a train travels at 120 km/h and another train travels at 80 km/h in the opposite direction, how long will it take for them to be 500 km apart if they start at the same location?";

        // Example 1: Without reasoning (disabled by default)
        System.out.println("EXAMPLE 1: WITHOUT REASONING (DISABLED)");
        OpenRouterChatCompletionResponse response1 = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addMessage("user", question)
                // No reasoning method call means no reasoning key is sent at all
                .execute();

        System.out.println("Response without reasoning:");
        System.out.println(response1.assistantMessage());
        System.out.println("\n-----------------------------------\n");

        // Example 2: With an explicit reasoning token budget
        System.out.println("EXAMPLE 2: WITH REASONING TOKEN BUDGET");
        OpenRouterChatCompletionResponse response2 = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addMessage("user", question)
                .reasoningMaxTokens(1000) // reasoning.max_tokens
                .execute();

        System.out.println("Response with reasoning budget of 1000 tokens:");
        System.out.println(response2.assistantMessage());
        System.out.println("\n-----------------------------------\n");

        // Example 3: With an effort hint instead of a token budget
        System.out.println("EXAMPLE 3: WITH REASONING EFFORT HINT");
        OpenRouterChatCompletionResponse response3 = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addMessage("user", question)
                .reasoningEffort("high") // reasoning.effort
                .execute();

        System.out.println("Response with reasoning effort 'high':");
        System.out.println(response3.assistantMessage());
        System.out.println("\n-----------------------------------\n");

        // Example 4: Reason, but keep the reasoning output out of the response
        System.out.println("EXAMPLE 4: REASONING EXCLUDED FROM RESPONSE");
        OpenRouterChatCompletionResponse response4 = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addMessage("user", question)
                .reasoningEffort("low")
                .reasoningEnabled(true) // reasoning.enabled - explicit on-switch (provider default otherwise)
                .reasoningExclude(true) // reasoning.exclude - reason, but keep the output out of the response
                .execute();

        System.out.println("Response with reasoning excluded:");
        System.out.println(response4.assistantMessage());
        System.out.println("\n-----------------------------------\n");

        // Example 5: Control the verbosity of the reasoning summaries
        System.out.println("EXAMPLE 5: REASONING SUMMARY VERBOSITY");
        OpenRouterChatCompletionResponse response5 = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addMessage("user", question)
                .reasoningEffort("medium")
                .reasoningSummary("detailed") // reasoning.summary: "auto", "concise" or "detailed"
                .execute();

        System.out.println("Response with detailed reasoning summaries:");
        System.out.println(response5.assistantMessage());
        if (!response5.reasoningDetails().isEmpty()) {
            System.out.println("Reasoning details returned: " + response5.reasoningDetails().size());
        }
    }
}
