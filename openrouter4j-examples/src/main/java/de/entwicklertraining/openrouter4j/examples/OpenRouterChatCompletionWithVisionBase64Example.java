package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Demonstrates how to use Vision with a local image (base64 encoded) in OpenRouter.
 * The image is located at "src/main/resources/image.jpg".
 * We pass it to the model and ask what is in the image.
 */
public class OpenRouterChatCompletionWithVisionBase64Example {

    public static void main(String[] args) throws IOException {
        // Create the OpenRouter client
        OpenRouterClient client = new OpenRouterClient();

        // Build an OpenRouter Completion request with an image in base64 format
        // We load the image from the classpath resources and write it to a temp file
        // This is necessary because addImageByBase64 expects a Path
        InputStream imageStream = OpenRouterChatCompletionWithVisionBase64Example.class
                .getClassLoader()
                .getResourceAsStream("image.jpg");

        if (imageStream == null) {
            throw new RuntimeException("Could not find image.jpg in classpath resources");
        }

        // Create a temporary file from the stream
        Path tempFile = Files.createTempFile("image", ".jpg");
        Files.copy(imageStream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("google/gemini-2.5-flash") // or any OpenRouter model that supports vision
                .provider("google-ai-studio")
                .addMessage("user", "Please describe the following photo:")
                // Using the newly created method addImageByBase64
                .addImageByBase64(tempFile)
                .execute();

        // Print model's response
        System.out.println("Model's answer:");
        System.out.println(response.assistantMessage());

        // Clean up temp file
        Files.deleteIfExists(tempFile);
    }
}
