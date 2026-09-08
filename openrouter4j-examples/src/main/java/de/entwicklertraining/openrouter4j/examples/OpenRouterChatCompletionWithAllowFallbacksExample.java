package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates provider.allow_fallbacks - controlling whether OpenRouter may route
 * the request to providers outside the preferred order list.
 *
 * Default behaviour (allow_fallbacks=true): the order list is only a preference.
 * If the preferred provider is unavailable (or does not serve the model at all),
 * OpenRouter silently falls back to any other provider.
 *
 * With allowFallbacks(false) the request is pinned strictly to the providers in
 * the order list - if none of them can serve it, OpenRouter returns an error
 * instead of routing elsewhere.
 */
public class OpenRouterChatCompletionWithAllowFallbacksExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // Case 1: hard pinning to a provider that serves the model -> works
        System.out.println("=== Case 1: provider(\"alibaba\") + allowFallbacks(false) ===");
        OpenRouterChatCompletionResponse pinned = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .provider("alibaba")        // pin to Alibaba
                .allowFallbacks(false)      // never route to any other provider
                .addMessage("user", "Reply with one short sentence: where are you running?")
                .execute();
        System.out.println("Answer: " + pinned.assistantMessage());

        // Case 2: same pinning, but a provider that does NOT serve the model -> error
        System.out.println();
        System.out.println("=== Case 2: provider(\"openai\") + allowFallbacks(false) (expected failure) ===");
        try {
            client.chat().completion()
                    .model("deepseek/deepseek-v4-flash-0731")
                    .provider("openai")     // OpenAI does not serve deepseek models
                    .allowFallbacks(false)
                    .addMessage("user", "This should never reach a model.")
                    .execute();
            System.out.println("Unexpected: the call succeeded although fallbacks are disabled.");
        } catch (RuntimeException e) {
            System.out.println("Expected failure (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            System.out.println("=> With the default (allow_fallbacks=true) OpenRouter would have");
            System.out.println("   silently routed the request to another provider instead.");
        }

        // Case 3: default behaviour - the order list is just a preference
        System.out.println();
        System.out.println("=== Case 3: provider(\"openai\") with default fallbacks ===");
        OpenRouterChatCompletionResponse fallback = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .provider("openai")         // not served by OpenAI -> falls back to another provider
                .addMessage("user", "Reply with one short sentence: where are you running?")
                .execute();
        System.out.println("Answer: " + fallback.assistantMessage());
        System.out.println("=> The call succeeded despite the unsuitable preference, because");
        System.out.println("   allow_fallbacks defaults to true.");
    }
}
