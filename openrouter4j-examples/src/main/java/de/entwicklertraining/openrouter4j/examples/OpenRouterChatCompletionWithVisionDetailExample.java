package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterImageDetail;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates the image resolution tier ({@code detail}) on image content
 * parts: the vision model is told which fidelity to use for each image.
 *
 * The {@code detail} key is only emitted when explicitly configured; the
 * {@code original} tier is an OpenRouter extension that providers without an
 * original-resolution tier downgrade to {@code high}.
 */
public class OpenRouterChatCompletionWithVisionDetailExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        String url = "https://software-quality-services.de/wp-content/uploads/2024/09/Walde_0141.jpg";

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("google/gemini-2.5-flash")
                .addMessage("user", "Read the small text in this image as precisely as possible.")
                // the URL overload with an explicit tier:
                .addImageByUrl(url, OpenRouterImageDetail.HIGH)
                .execute();

        System.out.println("Model's answer:");
        System.out.println(response.assistantMessage());
    }
}
