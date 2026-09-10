package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.*;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

import org.json.JSONObject;

public class OpenRouterChatCompletionWithFunctionCallingExample {

    public static void main(String[] args) {
        // Define a tool function
        OpenRouterToolDefinition weatherTool = OpenRouterToolDefinition.builder("get_weather")
                .description("Get current weather in a given location")
                .parameter("location", OpenRouterJsonSchema.stringSchema("City name"), true)
                // strict: true makes the model's tool arguments adhere exactly to the
                // declared parameters schema (protection against malformed JSON
                // arguments on providers that support it; API default is false).
                .strict(true)
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

        // Build request
        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addMessage("user", "What's the weather in Paris?")
                .addTool(weatherTool)
                //.parallelToolCalls(true) // somehow this bugs => model gets stuck in a loop
                .execute();

        // Print
        System.out.println("OpenRouter says: " + response.assistantMessage());
    }
}
