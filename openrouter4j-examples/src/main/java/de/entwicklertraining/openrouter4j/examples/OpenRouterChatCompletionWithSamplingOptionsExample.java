package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

import java.util.Map;

/**
 * Demonstrates the sampling parameters supported by OpenRouter.
 *
 * Shows frequency_penalty, presence_penalty, repetition_penalty, seed, min_p,
 * top_a, logit_bias, logprobs and top_logprobs. Every parameter is emitted only
 * when it is set - unset parameters never appear in the request JSON.
 *
 * Note: min_p, top_a and repetition_penalty are OpenRouter extensions that not
 * every provider supports; combine them with requireParameters(true) when the
 * endpoint must honour them.
 */
public class OpenRouterChatCompletionWithSamplingOptionsExample {

    public static void main(String[] args) {
        // Create the OpenRouter client
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addMessage("user", "Write one creative sentence about the sea.")
                // Standard penalties
                .frequencyPenalty(0.5)
                .presencePenalty(0.3)
                // OpenRouter extension - not supported by every provider
                .repetitionPenalty(1.1)
                // Best-effort deterministic sampling
                .seed(42)
                // OpenRouter sampling extensions - not supported by every provider
                .minP(0.05)
                .topA(0.9)
                // Ban one token entirely (bias -100) and slightly favour another
                .logitBias(Map.of(50256, -100.0))
                // Token log probabilities
                .logprobs(true)
                .topLogprobs(5)
                .execute();

        System.out.println("OpenRouter says: " + response.assistantMessage());
    }
}
