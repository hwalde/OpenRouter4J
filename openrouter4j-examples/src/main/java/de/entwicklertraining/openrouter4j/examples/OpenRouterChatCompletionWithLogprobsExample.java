package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterTokenLogprob;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates reading token log probabilities back out of the response.
 *
 * The library can request logprobs via logprobs(true) / topLogprobs(n);
 * the paid data arrives under choices[0].logprobs and is exposed through the
 * typed accessors contentLogprobs() / refusalLogprobs() (per-token: token,
 * logprob, UTF-8 bytes, top alternatives). The raw logprobs() JSONObject
 * stays available for anything the typed accessors do not cover.
 */
public class OpenRouterChatCompletionWithLogprobsExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .logprobs(true)
                .topLogprobs(3)
                .addMessage("user", "Name the capital of France in one word.")
                .execute();

        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("System fingerprint: " + response.systemFingerprint());

        for (OpenRouterTokenLogprob token : response.contentLogprobs()) {
            System.out.printf("token=%s logprob=%.4f bytes=%s topAlternatives=%d%n",
                    token.token(), token.logprob(), token.bytes(), token.topLogprobs().size());
        }
        // The refusal side exists only when the model refused; it is read the
        // same way: response.refusalLogprobs().
        System.out.println("Refusal tokens with logprobs: " + response.refusalLogprobs().size());
    }
}
