package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterJsonSchema;
import de.entwicklertraining.openrouter4j.OpenRouterToolDefinition;
import de.entwicklertraining.openrouter4j.OpenRouterToolResult;
import de.entwicklertraining.openrouter4j.OpenRouterToolSearchServerTool;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;
import org.json.JSONObject;

import java.util.List;

/**
 * Demonstrates deferred tool loading with the {@code openrouter:tool_search}
 * server tool - the pattern for large tool catalogs (dozens or hundreds of tools):
 *
 * - {@code OpenRouterToolDefinition.Builder.deferLoading(true)}: withholds the
 *   tool from the model ({@code function.defer_loading}) until the tool-search
 *   server tool finds it and makes it callable.
 * - {@code OpenRouterToolSearchServerTool}: emitted as
 *   {@code {"type":"openrouter:tool_search","parameters":{"max_results":...}}}
 *   into the same {@code tools} array; {@code max_results} defaults to 5.
 * - API constraints: a request using {@code defer_loading} requires this server
 *   tool, and at least one tool must remain non-deferred (the
 *   {@code get_weather} tool below is deliberately not deferred).
 *
 * <p>Because the deferred tools never sit in the prompt until needed, prompts
 * stay small and cache-stable.
 */
public class OpenRouterChatCompletionWithToolSearchExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // Two deferred tools: withheld from the model until tool search finds them
        List<OpenRouterToolDefinition> deferredTools = List.of(
                OpenRouterToolDefinition.builder("search_archive")
                        .description("Search the internal document archive")
                        .parameter("query", OpenRouterJsonSchema.stringSchema("The search query"), true)
                        .callback(ctx -> OpenRouterToolResult.of(new JSONObject().put("hits", 3)))
                        .deferLoading(true)
                        .build(),
                OpenRouterToolDefinition.builder("lookup_customer")
                        .description("Look up a customer record by id")
                        .parameter("customerId", OpenRouterJsonSchema.stringSchema("The customer id"), true)
                        .callback(ctx -> OpenRouterToolResult.of(new JSONObject().put("name", "Ada")))
                        .deferLoading(true)
                        .build());

        OpenRouterChatCompletionRequest.Builder builder = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                // The tool-search server tool makes deferred tools callable
                .addServerTool(OpenRouterToolSearchServerTool.builder().maxResults(5).build())
                // At least one tool must remain non-deferred
                .addTool(OpenRouterToolDefinition.builder("get_weather")
                        .description("Get the current weather of a city")
                        .parameter("city", OpenRouterJsonSchema.stringSchema("The city name"), true)
                        .callback(ctx -> OpenRouterToolResult.of(new JSONObject().put("weather", "sunny")))
                        .build());
        deferredTools.forEach(builder::addTool);

        OpenRouterChatCompletionResponse response = builder
                .addMessage("user", "Search the archive for the Q3 report and tell me the weather in Berlin.")
                .execute();

        System.out.println("Answer: " + response.assistantMessage());
    }
}
