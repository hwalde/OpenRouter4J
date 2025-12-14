package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest;
import de.entwicklertraining.api.base.ApiResponse;
import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.api.base.streaming.StreamingContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Demonstrates streaming responses from the OpenRouter Chat Completion API.
 *
 * <p>This example shows how to receive tokens in real-time as they are generated,
 * rather than waiting for the complete response. This is useful for:
 * <ul>
 *   <li>Better user experience with immediate feedback</li>
 *   <li>Processing long responses progressively</li>
 *   <li>Implementing chat interfaces with typing indicators</li>
 * </ul>
 *
 * <p>OpenRouter uses Server-Sent Events (SSE) format, compatible with OpenAI's streaming API.
 * Each chunk contains a delta with partial content that can be displayed immediately.
 */
public class OpenRouterChatCompletionStreamingExample {

    public static void main(String[] args) throws Exception {
        // Create the OpenRouter client
        OpenRouterClient client = new OpenRouterClient();

        // Create a handler to process streaming chunks
        StreamingResponseHandler<String> handler = new StreamingResponseHandler<>() {
            @Override
            public void onStreamStart() {
                System.out.println("=== Streaming started ===");
                System.out.print("Response: ");
            }

            @Override
            public void onData(String chunk) {
                // Print each chunk as it arrives (no newline - continuous text)
                if (chunk != null && !chunk.isEmpty()) {
                    System.out.print(chunk);
                    System.out.flush();
                }
            }

            @Override
            public void onMetadata(Map<String, Object> metadata) {
                // Metadata is received with SSE events (event type, id, etc.)
                // We ignore it in this simple example, but it could be logged
            }

            @Override
            public void onComplete() {
                System.out.println();
                System.out.println("=== Streaming completed ===");
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println();
                System.err.println("=== Streaming error: " + throwable.getMessage() + " ===");
                throwable.printStackTrace();
            }
        };

        // Build and execute the streaming request
        System.out.println("Sending streaming request to OpenRouter...\n");

        // Build the request with streaming enabled
        // The stream() method enables SSE format with OpenAI-style parsing (choices[0].delta.content)
        OpenRouterChatCompletionRequest request = client.chat().completion()
                .model("google/gemini-2.5-flash")
                .provider("google-ai-studio")
                .systemInstruction("You are a helpful assistant. Respond concisely.")
                .addMessage("user", "Explain what streaming APIs are in 3-4 sentences.")
                // Enable streaming with our handler - uses SSE format with OpenAI-style parsing
                .stream(handler)
                .build();

        // Execute asynchronously using the client
        CompletableFuture<? extends ApiResponse<?>> future = client.executeAsync(request);

        // Wait for the streaming to complete
        ApiResponse<?> response = future.get();

        // After streaming completes, we can access the StreamingContext for metadata
        if (response.getStreamingContext().isPresent()) {
            StreamingContext<?> ctx = response.getStreamingContext().get();
            System.out.println("\nStreaming Statistics:");
            System.out.println("- Lines processed: " + ctx.getLinesProcessed());
            System.out.println("- Duration: " + ctx.getDurationMillis() + " ms");
            System.out.println("- Success: " + ctx.isSuccess());
            System.out.println("- Chunks received: " + ctx.getChunks().size());
        }
    }
}
