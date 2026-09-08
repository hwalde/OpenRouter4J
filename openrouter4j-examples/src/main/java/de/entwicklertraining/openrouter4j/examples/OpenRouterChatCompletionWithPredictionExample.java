package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates predicted outputs ({@code prediction}): static content that supported
 * models can largely reuse, reducing latency when much of the response is known in
 * advance - e.g. rewriting an existing file or template with small edits.
 *
 * How much of the prediction the model actually used is reported in
 * {@code usage.completion_tokens_details} as {@code accepted_prediction_tokens} and
 * {@code rejected_prediction_tokens}, surfaced here via
 * {@code acceptedPredictionTokens()} / {@code rejectedPredictionTokens()}.
 */
public class OpenRouterChatCompletionWithPredictionExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        String originalCode = """
                public class Greeter {
                    public String greet(String name) {
                        return "Hello, " + name + "!";
                    }
                }
                """;

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("mistralai/codestral-2508") // only non-OpenAI model supporting prediction (documented exception)
                .addMessage("user", """
                        Rewrite this Java class so it greets in German instead of English.
                        Keep the structure identical:
                        %s
                        """.formatted(originalCode))
                // The model will mostly re-emit the original code - giving it as a
                // prediction lets supported models reuse it and answer faster.
                .prediction(originalCode)
                .execute();

        System.out.println("Rewritten class:");
        System.out.println(response.assistantMessage());

        Integer accepted = response.acceptedPredictionTokens();
        Integer rejected = response.rejectedPredictionTokens();
        System.out.println("Prediction tokens accepted: " + accepted);
        System.out.println("Prediction tokens rejected: " + rejected);
    }
}
