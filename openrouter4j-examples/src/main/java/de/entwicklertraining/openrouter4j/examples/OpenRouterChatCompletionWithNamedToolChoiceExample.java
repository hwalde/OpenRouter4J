package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterJsonSchema;
import de.entwicklertraining.openrouter4j.OpenRouterToolDefinition;
import de.entwicklertraining.openrouter4j.OpenRouterToolResult;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

import org.json.JSONObject;

/**
 * Demonstrates the named {@code tool_choice} form: forcing the model to call one
 * specific tool instead of letting it decide ({@code "auto"}) or merely requiring
 * some tool call ({@code "required"}).
 *
 * On the wire this sends:
 * {@code "tool_choice": {"type": "function", "function": {"name": "get_weather"}}}
 */
public class OpenRouterChatCompletionWithNamedToolChoiceExample {

    public static void main(String[] args) {
        // Define a tool function
        OpenRouterToolDefinition weatherTool = OpenRouterToolDefinition.builder("get_weather")
                .description("Get current weather in a given location")
                .parameter("location", OpenRouterJsonSchema.stringSchema("City name"), true)
                .callback(ctx -> {
                    // Fake weather data
                    String loc = ctx.arguments().getString("location");
                    JSONObject result = new JSONObject()
                            .put("city", loc)
                            .put("forecast", "Sunny, 20 C");
                    return OpenRouterToolResult.of(result);
                })
                .build();

        // Create the OpenRouter client
        OpenRouterClient client = new OpenRouterClient();

        // Build request - the model MUST call get_weather, it cannot answer directly
        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("google/gemini-2.5-flash")
                .provider("google-ai-studio")
                .addMessage("user", "What's the weather in Paris?")
                .addTool(weatherTool)
                .toolChoiceFunction("get_weather") // named tool_choice form
                .execute();

        // Print
        System.out.println("OpenRouter says: " + response.assistantMessage());
    }
}
