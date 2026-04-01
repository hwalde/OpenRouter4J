package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.*;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;
import org.json.JSONObject;

import java.util.Map;

/**
 * Demonstrates streaming responses combined with function calling (tool use).
 *
 * <p>The model is asked a question that requires a tool call. The library
 * automatically executes the tool, sends the result back, and streams the
 * final text answer token by token. Lifecycle events show each step.
 *
 * <p>Timestamps per chunk prove that the response is truly streamed
 * (tokens arrive over time, not all at once).
 */
public class OpenRouterChatCompletionStreamingWithFunctionCallingExample {

    public static void main(String[] args) {
        OpenRouterToolDefinition weatherTool = OpenRouterToolDefinition.builder("get_weather")
                .description("Get current weather in a given location")
                .parameter("location", OpenRouterJsonSchema.stringSchema("City name"), true)
                .callback(ctx -> {
                    String loc = ctx.arguments().getString("location");
                    JSONObject result = new JSONObject()
                            .put("city", loc)
                            .put("temperature_celsius", 22)
                            .put("condition", "Partly cloudy");
                    return OpenRouterToolResult.of(result);
                })
                .build();

        OpenRouterClient client = new OpenRouterClient();

        long[] firstChunkTime = {0};
        long[] lastChunkTime = {0};
        int[] chunkCount = {0};
        long startTime = System.currentTimeMillis();

        StreamingToolCallHandler handler = new StreamingToolCallHandler() {
            @Override
            public void onStreamStart() {
                System.out.println("[stream] Started");
            }

            @Override
            public void onData(String chunk) {
                long now = System.currentTimeMillis();
                if (firstChunkTime[0] == 0) firstChunkTime[0] = now;
                lastChunkTime[0] = now;
                chunkCount[0]++;
                System.out.printf("[%5dms] %s%n", now - startTime, chunk);
            }

            @Override
            public void onToolCallDetected(String toolName, String toolCallId, JSONObject arguments) {
                System.out.printf("[tool]    Detected: %s(%s) [id=%s]%n", toolName, arguments, toolCallId);
            }

            @Override
            public void onToolExecuted(String toolName, String toolCallId, OpenRouterToolResult result) {
                System.out.printf("[tool]    Executed: %s -> %s%n", toolName, result.content());
            }

            @Override
            public void onTurnComplete(int turnNumber) {
                System.out.printf("[turn]    Turn %d complete, starting next turn...%n", turnNumber);
            }

            @Override
            public void onFinalComplete() {
                System.out.println("[stream] Final response complete");
            }

            @Override
            public void onComplete() {
                System.out.println("[stream] Stream closed");
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("[error]   " + throwable.getMessage());
                throwable.printStackTrace();
            }

            @Override
            public void onMetadata(Map<String, Object> metadata) {}
        };

        System.out.println("=== Streaming + Function Calling Example ===\n");

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("google/gemini-2.5-flash")
                .provider("google-ai-studio")
                .systemInstruction("You are a helpful weather assistant. Be concise.")
                .addMessage("user", "What's the weather like in Berlin right now?")
                .addTool(weatherTool)
                .stream(handler)
                .execute();

        System.out.println("\n=== Summary ===");
        System.out.println("Assistant: " + response.assistantMessage());
        System.out.println("Chunks received: " + chunkCount[0]);
        if (firstChunkTime[0] > 0 && lastChunkTime[0] > firstChunkTime[0]) {
            System.out.println("Streaming duration: " + (lastChunkTime[0] - firstChunkTime[0]) + " ms");
            System.out.println("-> Response was truly streamed (tokens arrived over time)");
        } else {
            System.out.println("-> Could not verify streaming (too few chunks)");
        }
    }
}
