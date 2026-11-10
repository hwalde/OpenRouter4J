package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.image.OpenRouterImageGenerationRequest;
import de.entwicklertraining.openrouter4j.image.OpenRouterImageGenerationResponse;
import de.entwicklertraining.openrouter4j.image.OpenRouterImageModelsResponse;
import org.json.JSONObject;

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
        // 3. Animation and markup options (model-dependent): frame_count /
        //    frame_duration_ms apply to output_format "gif" on animation
        //    models (each enforces its own sub-range server-side); the markup
        //    overlay (transparent PNG, same dimensions as the first reference
        //    image) guides edits on markup-guided-editing models and is
        //    ignored otherwise. A gpt-image-2.6-family model honors both.
        //    Use your own reachable image URLs for the reference and the
        //    overlay.
        OpenRouterImageGenerationResponse gif = client.images().generate()
                .model("openai/gpt-image-2.6-sunburst")
                .prompt("animate the panda waving, draw an arrow to the hand")
                .outputFormat("gif")
                .frameCount(8)
                .frameDurationMs(100)
                .addInputReferenceByUrl("https://example.com/panda.png")
                .markup("https://example.com/markup.png")
                .execute();
        System.out.println("animation cost: " + gif.cost());

        // The verbatim markupRaw(JSONObject) hatch covers content-part
        // shapes the URL convenience cannot build - deliberately not a
        // markup(JSONObject) overload, so a bare markup(null) stays the
        // unambiguous unset call. Built here without executing to show the
        // wire shape.
        OpenRouterImageGenerationRequest hatch = client.images().generate()
                .model("openai/gpt-image-2.6-sunburst")
                .prompt("draw an arrow here")
                .addInputReferenceByUrl("https://example.com/diagram.png")
                .markupRaw(new JSONObject()
                        .put("type", "image_url")
                        .put("image_url", new JSONObject()
                                .put("url", "data:image/png;base64,iVBOR...")))
                .build();
        System.out.println("markup hatch body: " + hatch.getBody());
    }
}
