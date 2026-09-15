package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.image.OpenRouterImageGenerationResponse;
import de.entwicklertraining.openrouter4j.image.OpenRouterImageModelsResponse;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Demonstrates the dedicated Image API: discovering the image models
 * (GET /images/models) and generating an image (POST /images). The generated
 * bytes arrive base64-encoded in {@code b64_json} and are decoded by the
 * typed response.
 */
public class OpenRouterImageGenerationExample {

    public static void main(String[] args) throws Exception {
        OpenRouterClient client = new OpenRouterClient();

        // 1. Discover the image generation models.
        OpenRouterImageModelsResponse models = client.images().models().execute();
        System.out.println("Image generation models:");
        for (OpenRouterImageModelsResponse.OpenRouterImageModel model : models.models()) {
            System.out.println("- " + model.id() + " (" + model.name() + ")"
                    + " streaming=" + model.supportsStreaming());
        }

        // 2. Generate an image. Per-endpoint capabilities and pricing of a
        //    model are available via client.images().modelEndpoints("author/slug").
        OpenRouterImageGenerationResponse response = client.images().generate()
                .model("bytedance-seed/seedream-4.5")
                .prompt("a red panda astronaut floating in space, studio lighting")
                .aspectRatio("16:9")
                .outputFormat("png")
                .execute();

        System.out.println("created: " + response.created()
                + ", total tokens: " + response.totalTokens()
                + ", cost: " + response.cost());

        OpenRouterImageGenerationResponse.OpenRouterImage image = response.firstImage();
        if (image != null) {
            Path out = Path.of("generated-image.png");
            Files.write(out, image.bytes());
            System.out.println("Wrote " + image.mediaType() + " image to " + out.toAbsolutePath()
                    + " (" + image.bytes().length + " bytes)");
        }
    }
}
