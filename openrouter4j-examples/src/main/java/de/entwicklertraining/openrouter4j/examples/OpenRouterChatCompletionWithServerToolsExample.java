package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterDatetimeServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterJsonSchema;
import de.entwicklertraining.openrouter4j.OpenRouterServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterStopCondition;
import de.entwicklertraining.openrouter4j.OpenRouterToolDefinition;
import de.entwicklertraining.openrouter4j.OpenRouterToolResult;
import de.entwicklertraining.openrouter4j.OpenRouterWebSearchServerTool;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;
import org.json.JSONObject;

/**
 * Demonstrates built-in OpenRouter <em>server tools</em> inside the {@code tools}
 * request array, mixed with a regular client-side function tool, and the
 * {@code stop_server_tools_when} stop conditions for the server-tool agent loop.
 *
 * <p>Server tools are executed by OpenRouter itself - no client-side callback is
 * needed; the results reach the model as server_tool_calls. The function tool
 * still runs in this process through the usual tool-call loop.
 *
 * <p>Any stop condition firing halts the server-tool loop (OR logic); when set,
 * it overrides max_tool_calls. Here the loop stops after 3 steps or once the
 * cumulative cost exceeds 0.10 USD, whichever comes first.
 */
public class OpenRouterChatCompletionWithServerToolsExample {

    public static void main(String[] args) {
        OpenRouterToolDefinition weatherTool = OpenRouterToolDefinition.builder("get_weather")
                .description("Get the current weather of a city (mock)")
                .parameter("location", OpenRouterJsonSchema.stringSchema("City name"), true)
                .callback(ctx -> OpenRouterToolResult.of(new JSONObject()
                        .put("city", ctx.arguments().getString("location"))
                        .put("temperature_celsius", 22)))
                .build();

        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("openai/gpt-4o-mini")
                .addMessage("user", "What is the weather in Cologne right now, and what time is it there? "
                        + "Search the web if you need current information.")
                .addTool(weatherTool)
                // Built-in server tool, typed - executed by OpenRouter, not by us:
                .addServerTool(OpenRouterWebSearchServerTool.builder()
                        .maxResults(5)                       // parameters.max_results
                        .searchContextSize("medium")         // parameters.search_context_size
                        .build())
                // Another built-in server tool (default configuration emits only the type):
                .addServerTool(OpenRouterDatetimeServerTool.unconfigured())
                // Escape hatch for server-tool types without a typed implementation:
                // OpenRouterServerTool.of("openrouter:bash")
                //         .withOption("parameters", new JSONObject().put("engine", "openrouter"))
                // Stop conditions for the server-tool agent loop (OR logic):
                .stopServerToolsWhen(
                        OpenRouterStopCondition.stepCountIs(3),
                        OpenRouterStopCondition.maxCost(0.10))
                .execute();

        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("Cost: " + response.cost());
    }
}
