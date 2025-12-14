package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * A basic example of calling the OpenRouter chat completion to just generate text from text-only input.
 */
public class OpenRouterChatCompletionExample {

    public static void main(String[] args) {
        // Create the OpenRouter client
        OpenRouterClient client = new OpenRouterClient();

        // Minimal usage:
        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("google/gemini-2.5-flash")
                .provider("google-ai-studio")
                .addMessage("user", "Hello, how are you?")
                .execute();

        System.out.println("OpenRouter says: " + response.assistantMessage());
    }
}
