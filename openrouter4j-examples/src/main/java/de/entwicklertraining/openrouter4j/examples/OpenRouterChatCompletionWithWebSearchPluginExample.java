package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterWebSearchPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterWebSearchServerTool;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates the server-side web search plugin ({@code plugins[].id = "web"}).
 *
 * The plugin runs server-side at OpenRouter; its search results are delivered
 * to the model as tool calls (server_tool_calls) - you do not register or
 * execute anything client-side. Only the explicitly configured fields are
 * emitted into the plugins array.
 */
public class OpenRouterChatCompletionWithWebSearchPluginExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addPlugin(OpenRouterWebSearchPlugin.builder()
                        .maxResults(5)                                   // plugins[].max_results
                        .engine("exa")                                   // plugins[].engine
                        .includeDomains(java.util.List.of("openrouter.ai")) // plugins[].include_domains
                        .build())
                .addMessage("user", "What is the latest news about OpenRouter?")
                .execute();

        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("Cost: " + response.cost());

        // x_search: X (Twitter) search alongside native web search (only used
        // with native provider search on SpaceXAI/Grok models, billed separately).
        OpenRouterChatCompletionResponse xSearchResponse = client.chat().completion()
                .model("x-ai/grok-4")
                .addPlugin(OpenRouterWebSearchPlugin.builder()
                        .engine("native")
                        .xSearch(OpenRouterWebSearchServerTool.XSearchOptions.builder()
                                .allowedXHandles(java.util.List.of("openai", "xai"))
                                .fromDate("2025-01-01")
                                .enableImageUnderstanding(true)
                                .build())
                        .build())
                .addMessage("user", "What did @openai tweet recently?")
                .execute();
        System.out.println("X search answer: " + xSearchResponse.assistantMessage());

        // Escape hatch for plugin ids without a typed implementation:
        // OpenRouterPlugin.of("file-parser").withOption("pdf", "engine");
    }
}
