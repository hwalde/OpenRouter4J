package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;
import de.entwicklertraining.api.base.ApiResponse;
import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Demonstrates the reasoning/refusal/audio side of a streamed chat completion.
 *
 * Reasoning deltas ({@code delta.reasoning}), reasoning details and refusal
 * deltas are NOT forwarded as content chunks - the user handler only sees
 * content. They are accumulated instead, and after the stream ends the
 * synthetic response exposes them through the same accessors as the
 * synchronous path ({@code reasoning()}, {@code reasoningDetails()},
 * {@code refusal()}/{@code hasRefusal()}, {@code audio()}), together with the
 * chunk-level {@code service_tier}, {@code openrouter_metadata} and
 * {@code system_fingerprint} fields.
 */
public class OpenRouterChatCompletionStreamingReasoningExample {

    public static void main(String[] args) throws Exception {
        OpenRouterClient client = new OpenRouterClient();

        StreamingResponseHandler<String> handler = new StreamingResponseHandler<>() {
            @Override
            public void onStreamStart() {
                System.out.print("Answer: ");
            }

            @Override
            public void onData(String chunk) {
                // Only content deltas arrive here - reasoning/refusal deltas are accumulated.
                if (chunk != null && !chunk.isEmpty()) {
                    System.out.print(chunk);
                    System.out.flush();
                }
            }

            @Override
            public void onMetadata(Map<String, Object> metadata) { }

            @Override
            public void onComplete() {
                System.out.println();
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Streaming error: " + throwable.getMessage());
            }
        };

        OpenRouterChatCompletionRequest request = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .reasoningEffort("low")
                .addMessage("user", "What is 17 * 23?")
                .stream(handler)
                .build();

        CompletableFuture<? extends ApiResponse<?>> future = client.executeAsync(request);
        ApiResponse<?> result = future.get();

        // The synthetic response of the streaming loop mirrors the synchronous one:
        if (result instanceof OpenRouterChatCompletionResponse response) {
            System.out.println("Reasoning: " + response.reasoning());
            System.out.println("Reasoning details: " + response.reasoningDetails().size());
            System.out.println("Refused: " + response.hasRefusal() + " " + response.refusal());
            System.out.println("Service tier: " + response.serviceTier());
            System.out.println("System fingerprint: " + response.systemFingerprint());
        }
    }
}
