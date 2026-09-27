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
        System.out.println("Native tokens (prompt/completion/reasoning/cached/completion-images): "
                + metadata.nativeTokensPrompt() + " / " + metadata.nativeTokensCompletion()
                + " / " + metadata.nativeTokensReasoning() + " / " + metadata.nativeTokensCached()
                + " / " + metadata.nativeTokensCompletionImages());
        System.out.println("Total cost (USD):    " + metadata.totalCost());
        System.out.println("Upstream cost (USD): " + metadata.upstreamInferenceCost());
        System.out.println("Latency / generation time (ms): " + metadata.latency()
                + " / " + metadata.generationTime());
        // Caller attribution and scoping. The num_media_* / num_input_audio_prompt
        // fields are item counts (not tokens) and are null for generations
        // without such input/output.
        System.out.println("Origin / user agent / referrer: " + metadata.origin()
                + " / " + metadata.userAgent() + " / " + metadata.httpReferer());
        System.out.println("Workspace: " + metadata.workspaceId()
                + ", fetches: " + metadata.numFetches()
                + ", media prompt/completion/audio-input: " + metadata.numMediaPrompt()
                + " / " + metadata.numMediaCompletion()
                + " / " + metadata.numInputAudioPrompt());
        // Only set on a response-cache HIT (the X-OpenRouter-Cache family).
        System.out.println("Cache source generation: " + metadata.responseCacheSourceId());
        // One entry per provider attempt, including fallback attempts.
        System.out.println("Provider attempts:    " + metadata.providerResponses().size());

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
