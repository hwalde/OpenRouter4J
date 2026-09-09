package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterPercentileCutoffs;
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
 * - sortBy("throughput", "none"): sort-object form with partition - "none"
 *   sorts ALL endpoints together regardless of model, the documented way to
 *   get "whichever model is fastest right now" behaviour with a models(...)
 *   fallback list ("model", the default, keeps fallbacks fallbacks)
 * - enforceDistillableText(true): only endpoints with distillable text output
 * - preferredMaxLatency(seconds): deprioritize endpoints slower than this
 *   median (p50) latency - still usable, never excluded (unlike max_price);
 *   a percentile form (p50/p75/p90/p99) exists via
 *   preferredMaxLatency(OpenRouterPercentileCutoffs)
 * - preferredMinThroughput(tokensPerSecond): deprioritize endpoints below this
 *   median (p50) throughput; percentile form likewise. With fallback models a
 *   better-performing fallback may be chosen over the primary model.
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
                .preferredMaxLatency(2.0)              // seconds (p50) - deprioritize, not exclude
                .preferredMinThroughput(20.0)          // tokens/s (p50)
                .addMessage("user", "Say hello in one short sentence.")
                .execute();

        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("The provider that served the request is reported in the raw response JSON: "
                + response.getJson().optString("provider", "(not reported)"));

        // The sort-object form with partition: with a models(...) fallback list,
        // partition "none" sorts all endpoints together so the fastest model
        // wins - not just the fastest endpoint of each model.
        OpenRouterChatCompletionResponse fastest = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .models("z-ai/glm-5.3-flash", "google/gemini-3.5-flash-lite") // fallback candidates
                .sortBy("throughput", "none")          // object form wins over sort(...)
                .preferredMinThroughput(OpenRouterPercentileCutoffs.builder()
                        .p50(30.0)                     // median at least 30 tokens/s
                        .p99(10.0)                     // tolerate tail down to 10 tokens/s
                        .build())
                .addMessage("user", "Say hello in one short sentence.")
                .execute();

        System.out.println("Fastest answer: " + fastest.assistantMessage());
    }
}

