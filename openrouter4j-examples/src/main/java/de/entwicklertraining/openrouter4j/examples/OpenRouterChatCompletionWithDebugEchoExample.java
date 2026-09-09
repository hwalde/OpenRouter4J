package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.OpenRouterClient;

import java.util.Map;

/**
 * Demonstrates the streaming-only {@code debug.echo_upstream_body} option.
 *
 * <p>When {@code debugEchoUpstreamBody(true)} is set together with streaming,
 * OpenRouter emits a debug chunk at the start of the stream containing the
 * transformed upstream request body - exactly what OpenRouter sent to the
 * provider after applying its own request transformations. Invaluable when
 * debugging provider-specific silent parameter drops (the
 * {@code responseSchema}-dropped-without-error class of problems).
 *
 * <p>Trap 1: the option is streaming-only - the key is still sent on the wire
 * when set, but without {@code stream(true)} OpenRouter ignores it.
 *
 * <p>Trap 2: the debug chunk is a raw SSE {@code data:} event with no
 * {@code choices} array, so the standard content extractor does not forward it
 * to {@link StreamingResponseHandler#onData(Object)} - only model text reaches
 * that callback. To inspect the echoed upstream body, read the raw stream
 * (e.g. {@code curl -N}) or install a custom {@code StreamProcessor}; the typed
 * surface shown here is what puts the option into the request.
 */
public class OpenRouterChatCompletionWithDebugEchoExample {

    public static void main(String[] args) throws Exception {
        OpenRouterClient client = new OpenRouterClient();

        StreamingResponseHandler<String> handler = new StreamingResponseHandler<>() {
            @Override
            public void onStreamStart() {
                System.out.println("[stream] started - the first raw SSE data event is the debug echo");
            }

            @Override
            public void onData(String chunk) {
                if (chunk != null && !chunk.isEmpty()) {
                    System.out.print(chunk);
                    System.out.flush();
                }
            }

            @Override
            public void onMetadata(Map<String, Object> metadata) {
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

        // Build the request with streaming enabled and the debug option set.
        // debugEchoUpstreamBody is streaming-only - the key is sent on the
        // wire, but without .stream(handler) OpenRouter ignores it.
        var request = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addMessage("user", "Say hello in one short sentence.")
                .debugEchoUpstreamBody(true)
                .stream(handler)
                .build();

        // Execute asynchronously using the client
        client.executeAsync(request).get();

        System.out.println("The echoed upstream body travels as the first raw SSE data event");
        System.out.println("of the HTTP response - inspect it with a raw stream client (e.g. curl -N)");
        System.out.println("or a custom StreamProcessor to see exactly what OpenRouter sent to the provider.");
    }
}
