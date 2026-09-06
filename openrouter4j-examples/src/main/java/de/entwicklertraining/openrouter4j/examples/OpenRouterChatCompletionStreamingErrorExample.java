package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterToolDefinition;
import de.entwicklertraining.openrouter4j.OpenRouterToolResult;
import de.entwicklertraining.openrouter4j.OpenRouterJsonSchema;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;
import org.json.JSONObject;

/**
 * Demonstrates error handling on the streaming tool-call loop.
 *
 * <p>OpenRouter reports mid-generation failures as a regular {@code data:} event
 * carrying a top-level {@code error} object inside an otherwise valid HTTP 200
 * stream. The streaming accumulator captures that chunk, and the synthetic
 * response of the loop exposes it through the usual accessors
 * ({@code hasError()}, {@code error()}, {@code errorCode()}, {@code errorMessage()})
 * - so a failed stream no longer looks like an empty one.
 *
 * <p>The loop finishes normally after the error chunk (content that was already
 * streamed stays streamed); the loud check {@code throwOnError()} is the opt-in
 * for callers who must not mistake a failed stream for an empty answer.
 */
public class OpenRouterChatCompletionStreamingErrorExample {

    public static void main(String[] args) {
        OpenRouterToolDefinition echoTool = OpenRouterToolDefinition.builder("echo")
                .description("Echo the given text back")
                .parameter("text", OpenRouterJsonSchema.stringSchema("Text to echo"), true)
                .callback(ctx -> OpenRouterToolResult.of(new JSONObject()
                        .put("echo", ctx.arguments().getString("text"))))
                .build();

        OpenRouterClient client = new OpenRouterClient();

        StreamingResponseHandler<String> handler = new StreamingResponseHandler<>() {
            @Override
            public void onStreamStart() {
                System.out.println("[stream] started");
            }

            @Override
            public void onData(String chunk) {
                System.out.print(chunk);
            }

            @Override
            public void onComplete() {
                System.out.println();
            }

            @Override
            public void onError(Throwable error) {
                System.err.println("[stream] transport error: " + error.getMessage());
            }
        };

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("openai/gpt-4o-mini")
                .addMessage("user", "Echo the word 'hello' back to me.")
                .addTool(echoTool)
                .stream(handler)
                .execute();

        // Mid-stream failures surface here; without the captured error object a
        // failed stream would be indistinguishable from an empty answer.
        if (response.hasError()) {
            System.err.println("Stream failed: code=" + response.errorCode()
                    + " message=" + response.errorMessage());
            response.throwOnError(); // loud path, if the caller must not continue
        } else {
            System.out.println("Stream finished cleanly. Message: " + response.assistantMessage());
        }
    }
}
