package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterWebSearchPlugin;
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
                .model("openai/gpt-4o-mini")
                .addPlugin(OpenRouterWebSearchPlugin.builder()
                        .maxResults(5)                                   // plugins[].max_results
                        .engine("exa")                                   // plugins[].engine
                        .includeDomains(java.util.List.of("openrouter.ai")) // plugins[].include_domains
                        .build())
                .addMessage("user", "What is the latest news about OpenRouter?")
                .execute();

        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("Cost: " + response.cost());

        // Escape hatch for plugin ids without a typed implementation:
        // OpenRouterPlugin.of("file-parser").withOption("pdf", "engine");
    }
}
