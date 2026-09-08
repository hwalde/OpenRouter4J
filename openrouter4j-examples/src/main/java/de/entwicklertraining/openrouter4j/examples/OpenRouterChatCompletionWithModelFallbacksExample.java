package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates the model fallback list: beside the single primary {@code model},
 * the {@code models} array lists candidates that OpenRouter tries in order when
 * the primary model cannot serve the request.
 *
 * The primary model is always emitted; per the API semantics the models list
 * acts as fallback candidates for it (it does not replace it).
 */
public class OpenRouterChatCompletionWithModelFallbacksExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")        // primary model
                .models(                             // tried in order when the primary is unavailable
                        "z-ai/glm-5.3-flash",
                        "google/gemini-3.5-flash-lite")
                .addMessage("user", "Name three programming languages, comma-separated.")
                .execute();

        System.out.println("Model that served the request: " + response.model());
        System.out.println("Answer: " + response.assistantMessage());
    }
}
