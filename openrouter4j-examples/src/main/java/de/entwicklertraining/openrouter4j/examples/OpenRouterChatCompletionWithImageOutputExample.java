package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterImageConfig;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates multimodal OUTPUT: asking an image-generating model for images.
 *
 * Non-text output is unreachable without modalities(...): the request must
 * explicitly ask for the "image" modality, and image_config carries the
 * provider-specific image options (image count, aspect ratio, resolution).
 *
 * The generated images arrive in choices[0].message.images - accessible
 * via imageUrls() (the URL/base64 payloads) or the raw images() array.
 */
public class OpenRouterChatCompletionWithImageOutputExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("google/gemini-2.5-flash-image-preview")
                .modalities("text", "image")                       // required for image output
                .imageConfig(OpenRouterImageConfig.builder()
                        .numImages(1)                              // image_config.num_images
                        .aspectRatio("16:9")                       // image_config.aspect_ratio
                        .build())
                .addMessage("user", "Draw a small lighthouse at sunset.")
                .execute();

        System.out.println("Text part: " + response.assistantMessage());

        var urls = response.imageUrls();
        if (!urls.isEmpty()) {
            System.out.println("The response carries " + urls.size() + " generated image(s):");
            urls.forEach(url -> System.out.println("- " + url));
        }
    }
}
