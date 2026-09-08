package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates the extended provider preferences object. Alongside the already
 * supported order list (provider(...)), require_parameters and allow_fallbacks,
 * OpenRouter offers further routing controls, all emitted inside the same
 * "provider" object:
 *
 * - dataCollection("deny"): never route to providers that train on prompts/completions
 * - ignoreProviders(...): provider slugs that must not serve the request
 * - onlyProviders(...): restrict routing to exactly these providers
 * - maxPrice(prompt, completion[, image, audio]): per-million-token price caps
 * - quantizations(...): accepted quantization levels (e.g. int4, fp8)
 * - sort("latency"): sort eligible providers by price, throughput or latency
 * - enforceDistillableText(true): only endpoints with distillable text output
 *
 * This example picks a cheap, privacy-friendly, low-latency route.
 */
public class OpenRouterChatCompletionWithProviderPreferencesExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .provider("deepseek")                    // preferred provider (order list)
                .dataCollection("deny")                // no providers that train on data
                .ignoreProviders("provider1")          // skip a specific provider
                .maxPrice("0.5", "1.5")                // USD per million tokens
                .quantizations("fp8", "fp4")         // accepted quantization levels
                .sort("latency")                       // prefer the fastest eligible endpoint
                .addMessage("user", "Say hello in one short sentence.")
                .execute();

        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("The provider that served the request is reported in the raw response JSON: "
                + response.getJson().optString("provider", "(not reported)"));
    }
}
