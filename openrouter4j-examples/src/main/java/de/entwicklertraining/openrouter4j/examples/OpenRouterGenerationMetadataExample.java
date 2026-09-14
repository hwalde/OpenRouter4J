package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;
import de.entwicklertraining.openrouter4j.generation.OpenRouterGenerationContentResponse;
import de.entwicklertraining.openrouter4j.generation.OpenRouterGenerationFeedbackResponse;
import de.entwicklertraining.openrouter4j.generation.OpenRouterGenerationResponse;

/**
 * Demonstrates the generation-metadata endpoints around one chat completion:
 *
 * - GET /generation?id=... - the post-mortem routing view of a generation
 *   (which provider served it, the native finish reason, the token/cost
 *   breakdown)
 * - GET /generation/content?id=... - the stored prompt and completion
 * - POST /generation/feedback - structured thumbs-up/down feedback
 *
 * <p>OpenRouter requires a management key for these endpoints; the
 * {@code id} is the response {@code id} of the chat completion (the
 * {@code gen-...} value). There is a delay of a few seconds between the
 * completion and its metadata becoming queryable - production code should
 * retry on 404.
 */
public class OpenRouterGenerationMetadataExample {

    public static void main(String[] args) throws Exception {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse completion = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addMessage("user", "Say hello in exactly three words.")
                .execute();
        String generationId = completion.id();
        System.out.println("Completion: " + completion.assistantMessage());
        System.out.println("Generation id: " + generationId);

        Thread.sleep(5000); // let OpenRouter settle the metadata

        // 1. Request/usage metadata - the post-mortem surface.
        OpenRouterGenerationResponse metadata = client.generation(generationId).execute();
        System.out.println("Provider:            " + metadata.providerName());
        System.out.println("Model:               " + metadata.model());
        System.out.println("Router:              " + metadata.router());
        System.out.println("Finish reason:       " + metadata.finishReason()
                + " (native: " + metadata.nativeFinishReason() + ")");
        System.out.println("Tokens (prompt/completion): " + metadata.tokensPrompt()
                + " / " + metadata.tokensCompletion());
        System.out.println("Native tokens (prompt/completion/reasoning/cached): "
                + metadata.nativeTokensPrompt() + " / " + metadata.nativeTokensCompletion()
                + " / " + metadata.nativeTokensReasoning() + " / " + metadata.nativeTokensCached());
        System.out.println("Total cost (USD):    " + metadata.totalCost());
        System.out.println("Upstream cost (USD): " + metadata.upstreamInferenceCost());
        System.out.println("Latency / generation time (ms): " + metadata.latency()
                + " / " + metadata.generationTime());

        // 2. The stored prompt and completion.
        OpenRouterGenerationContentResponse content = client.generationContent(generationId).execute();
        System.out.println("Stored input messages: " + content.inputMessages().size());
        System.out.println("Stored completion:     " + content.outputCompletion());

        // 3. Structured feedback on the generation.
        OpenRouterGenerationFeedbackResponse feedback = client.generationFeedback()
                        .generationId(generationId)
                        .category("other")
                        .comment("Example feedback: the answer followed the instruction.")
                        .execute();
        System.out.println("Feedback recorded: " + feedback.success());
    }
}
