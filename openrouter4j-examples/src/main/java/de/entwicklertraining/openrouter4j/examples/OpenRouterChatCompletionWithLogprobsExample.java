package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates reading token log probabilities back out of the response.
 *
 * The library can request logprobs via logprobs(true) / topLogprobs(n);
 * the paid data arrives under choices[0].logprobs and the provider-side model
 * snapshot under the top-level system_fingerprint field.
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

        if (response.logprobs() != null) {
            var content = response.logprobs().optJSONArray("content");
            if (content != null) {
                for (int i = 0; i < content.length(); i++) {
                    var entry = content.getJSONObject(i);
                    System.out.printf("token=%s logprob=%.4f%n",
                            entry.getString("token"), entry.getDouble("logprob"));
                }
            }
        }
    }
}
