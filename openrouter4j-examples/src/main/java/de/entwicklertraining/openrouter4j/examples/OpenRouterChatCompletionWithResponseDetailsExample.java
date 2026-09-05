package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates the response-side accessors for OpenRouter-specific fields:
 *
 * - reasoning() / reasoningDetails(): chain-of-thought output of reasoning models
 * - provider(): which provider served the request
 * - nativeFinishReason(): the provider-native finish reason next to the normalised one
 * - openrouterMetadata(): routing metadata (requires metadataInResponse(true))
 * - cost() / cachedPromptTokens() / reasoningTokens(): usage extras
 * - throwOnError(): fails loudly when OpenRouter reports a mid-request error
 *   inside an otherwise valid HTTP 200 response - without this check, such a
 *   response would look like an empty one because accessors swallow exceptions.
 */
public class OpenRouterChatCompletionWithResponseDetailsExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("openai/gpt-4o-mini")
                .metadataInResponse(true) // opt in to openrouter_metadata
                .addMessage("user", "What is 17 * 23? Think step by step.")
                .execute();

        // Fail loudly instead of mistaking a failed response for an empty one.
        response.throwOnError();

        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("Provider: " + response.provider());
        System.out.println("Finish reason: " + response.finishReason()
                + " (native: " + response.nativeFinishReason() + ")");
        System.out.println("Reasoning: " + response.reasoning());
        System.out.println("Cost (USD): " + response.cost());
        System.out.println("Cached prompt tokens: " + response.cachedPromptTokens());
        System.out.println("Reasoning tokens: " + response.reasoningTokens());
        if (response.openrouterMetadata() != null) {
            System.out.println("Routing metadata: " + response.openrouterMetadata());
        }
    }
}
