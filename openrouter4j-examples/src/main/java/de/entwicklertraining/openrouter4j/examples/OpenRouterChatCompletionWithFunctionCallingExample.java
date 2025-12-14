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
                .model("google/gemini-2.5-flash")
                .provider("google-ai-studio")
                .addMessage("user", "What's the weather in Paris?")
                .addTool(weatherTool)
                //.parallelToolCalls(true) // somehow this bugs => model gets stuck in a loop
                .execute();

        // Print
        System.out.println("OpenRouter says: " + response.assistantMessage());
    }
}
