package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterAutoRouterPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterFileParserPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterModerationPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterWebFetchPlugin;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates the typed plugin implementations beyond the classic
 * {@code web} plugin. Every typed plugin emits only the fields explicitly
 * configured on its builder; {@code OpenRouterGenericPlugin} /
 * {@code OpenRouterPlugin.of(id)} remains the verbatim escape hatch for
 * unknown keys.
 *
 * Shown here (one plugin per request - plugins are usually combined with care):
 * - {@code auto-router}: automatic routing filtered by model patterns and cost tier
 * - {@code web-fetch}: let the model fetch from (or never from) domains
 * - {@code file-parser}: PDF parsing with a chosen engine
 * - {@code moderation}: OpenRouter's moderation guard, no configuration keys
 */
public class OpenRouterChatCompletionWithTypedPluginsExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // 1) auto-router: automatic model routing between wildcard patterns,
        //    restricted to the low cost tier (cost_tier takes precedence over
        //    the deprecated cost_quality_tradeoff number).
        run(client.chat().completion()
                .addPlugin(OpenRouterAutoRouterPlugin.builder()
                        .allowedModels(java.util.List.of("anthropic/*", "openai/*"))
                        .costTier("low")
                        .build())
                .addMessage("user", "Summarize what HTTP status code 402 means."));

        // 2) web-fetch: the model may fetch up to 5 pages, only from
        //    example.com (and never from nope.org), truncated at 2048 tokens.
        run(client.chat().completion()
                .addPlugin(OpenRouterWebFetchPlugin.builder()
                        .maxUses(5)
                        .maxContentTokens(2048)
                        .allowedDomains(java.util.List.of("example.com"))
                        .blockedDomains(java.util.List.of("nope.org"))
                        .build())
                .addMessage("user", "What does the example.com landing page say?"));

        // 3) file-parser: PDF parsing via Mistral OCR.
        run(client.chat().completion()
                .addPlugin(OpenRouterFileParserPlugin.builder()
                        .pdfEngine("mistral-ocr")
                        .build())
                .addMessage("user", "What does the attached document say?"));

        // 4) moderation: no configuration keys in the schema - just the id.
        run(client.chat().completion()
                .addPlugin(new OpenRouterModerationPlugin())
                .addMessage("user", "Tell me a clean joke about databases."));
    }

    private static void run(de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest.Builder builder) {
        OpenRouterChatCompletionResponse response = builder.execute();
        if (response.hasError()) {
            throw new IllegalStateException("OpenRouter failed: code=" + response.errorCode()
                    + " message=" + response.errorMessage());
        }
        System.out.println("Model served: " + response.model());
        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("---");
    }
}
