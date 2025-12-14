package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates how to use Vision with an external URL in OpenRouter.
 * We pass an image URL and ask the model to describe what it sees.
 */
public class OpenRouterChatCompletionWithVisionUrlExample {

    public static void main(String[] args) {
        // Create the OpenRouter client
        OpenRouterClient client = new OpenRouterClient();

        // Example: an external image URL
        String url = "https://software-quality-services.de/wp-content/uploads/2024/09/Walde_0141.jpg";

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("google/gemini-2.5-flash") // or any OpenRouter model that supports vision
                .provider("google-ai-studio")
                .addMessage("user", "What do you see in this image?")
                // Using the newly created method addImageByUrl
                .addImageByUrl(url)
                .execute();

        System.out.println("Model's answer:");
        System.out.println(response.assistantMessage());
    }
}
