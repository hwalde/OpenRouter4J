package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates the {@code trace} request object: metadata for observability
 * and tracing that OpenRouter forwards to configured broadcast destinations
 * (Langfuse, Datadog, Weave, ...).
 *
 * <p>The five keys the schema names with special handling:
 *
 * - traceId: groups all requests of one multi-step workflow under one trace
 * - traceName: display name of the root trace
 * - spanName: name of the span this generation opens
 * - generationName: display name of this single generation
 * - parentSpanId: links the generation as a child of an existing span
 *   (e.g. an OpenTelemetry span id from your own tracing system)
 *
 * <p>Any additional key is passed through as custom metadata (schema allows
 * additional properties); configure it via {@code option(key, value)} - the
 * five known keys are reserved for their typed methods.
 *
 * <p>This is a separate mechanism from the {@code metadata} request field
 * (see the observability example): metadata attaches to the generation record
 * on OpenRouter's side, trace metadata is broadcast to your configured
 * observability destinations.
 */
public class OpenRouterChatCompletionWithTraceExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .trace(OpenRouterTraceConfig.builder()
                        .traceId("order-4711")
                        .traceName("Order processing")
                        .spanName("classify-intent")
                        .generationName("step-1-classify")
                        .parentSpanId("otel-abc123")
                        .option("customer_segment", "enterprise") // custom metadata, passed through
                        .option("attempt", 2)
                        .build())
                .addMessage("user", "Say hello in one short sentence.")
                .execute();

        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("The trace keys above are forwarded to configured broadcast destinations");
        System.out.println("(Langfuse, Datadog, Weave, ...) and group this generation inside your own tracing view.");
    }
}
