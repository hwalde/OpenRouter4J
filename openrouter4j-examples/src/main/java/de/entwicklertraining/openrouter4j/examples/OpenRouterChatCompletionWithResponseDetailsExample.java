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
 * - promptAudioTokens() / promptVideoTokens() / promptCacheWriteTokens() /
 *   completionAudioTokens() / serverToolCost(): the remaining usage token
 *   details and the metered server-tool execution cost
 * - throwOnError(): fails loudly when OpenRouter reports a mid-request error
 *   inside an otherwise valid HTTP 200 response - without this check, such a
 *   response would look like an empty one because accessors swallow exceptions.
 */
public class OpenRouterChatCompletionWithResponseDetailsExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .metadataInResponse(true) // opt in to openrouter_metadata
                .addMessage("user", "What is 17 * 23? Think step by step.")
                .execute();

        // Fail loudly instead of mistaking a failed response for an empty one.
        // hasError()/error()/errorCode()/errorMessage() allow inspecting the
        // failure without throwing.
        if (response.hasError()) {
            throw new IllegalStateException("OpenRouter failed: code=" + response.errorCode()
                    + " message=" + response.errorMessage());
        }

        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("Provider: " + response.provider());
        System.out.println("Finish reason: " + response.finishReason()
                + " (native: " + response.nativeFinishReason() + ")");
        System.out.println("Reasoning: " + response.reasoning());
        if (!response.reasoningDetails().isEmpty()) {
            System.out.println("Reasoning details: " + response.reasoningDetails().size() + " item(s)");
        }
        System.out.println("Cost (USD): " + response.cost());
        if (response.costDetails() != null) {
            System.out.println("Cost details: " + response.costDetails());
        }
        System.out.println("Cached prompt tokens: " + response.cachedPromptTokens());
        System.out.println("Reasoning tokens: " + response.reasoningTokens());
        // Token details beyond the cached/reasoning pair - present only when the
        // served modality or caching setup produced them:
        System.out.println("Prompt audio tokens: " + response.promptAudioTokens());
        System.out.println("Prompt video tokens: " + response.promptVideoTokens());
        System.out.println("Prompt cache-write tokens: " + response.promptCacheWriteTokens());
        System.out.println("Completion audio tokens: " + response.completionAudioTokens());
        // Metered server-tool execution cost (e.g. shell sandbox time), 0.0 when a
        // metered server tool ran but settled at zero dollars, absent otherwise:
        System.out.println("Server-tool cost (USD): " + response.serverToolCost());
        if (response.openrouterMetadata() != null) {
            System.out.println("Routing metadata: " + response.openrouterMetadata());
        }
    }
}
