package de.entwicklertraining.openrouter4j.image;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the image generation request shape and the response accessors
 * against recorded JSON shapes of POST /images and the image model listings.
 */
class OpenRouterImageGenerationTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void requestUsesPostMethodOnImagesUrl() {
        OpenRouterImageGenerationRequest request = new OpenRouterImageGenerationRequest.Builder(client())
                .model("bytedance-seed/seedream-4.5")
                .prompt("a red panda astronaut")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/images");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void requiredFieldsAreEmittedAndOptionalFieldsAreAbsentWhenUnset() {
        JSONObject body = new JSONObject(new OpenRouterImageGenerationRequest.Builder(client())
                .model("bytedance-seed/seedream-4.5")
                .prompt("a red panda astronaut")
                .build()
                .getBody());

        assertThat(body.getString("model")).isEqualTo("bytedance-seed/seedream-4.5");
        assertThat(body.getString("prompt")).isEqualTo("a red panda astronaut");
        assertThat(body.has("aspect_ratio")).isFalse();
        assertThat(body.has("background")).isFalse();
        assertThat(body.has("quality")).isFalse();
        assertThat(body.has("resolution")).isFalse();
        assertThat(body.has("size")).isFalse();
        assertThat(body.has("n")).isFalse();
        assertThat(body.has("output_format")).isFalse();
        assertThat(body.has("output_compression")).isFalse();
        assertThat(body.has("seed")).isFalse();
        assertThat(body.has("user")).isFalse();
        assertThat(body.has("stream")).isFalse();
        assertThat(body.has("input_references")).isFalse();
        assertThat(body.has("frame_count")).isFalse();
        assertThat(body.has("frame_duration_ms")).isFalse();
        assertThat(body.has("markup")).isFalse();
        assertThat(body.has("provider")).isFalse();
    }

    @Test
    void optionalFieldsAreEmittedOnlyWhenSet() {
        JSONObject body = new JSONObject(new OpenRouterImageGenerationRequest.Builder(client())
                .model("bytedance-seed/seedream-4.5")
                .prompt("a red panda astronaut")
                .aspectRatio("16:9")
                .background("transparent")
                .quality("high")
                .resolution("2K")
                .size("2048x2048")
                .n(2)
                .outputFormat("png")
                .outputCompression(80)
                .seed(42L)
                .user("user-123")
                .build()
                .getBody());

        assertThat(body.getString("aspect_ratio")).isEqualTo("16:9");
        assertThat(body.getString("background")).isEqualTo("transparent");
        assertThat(body.getString("quality")).isEqualTo("high");
        assertThat(body.getString("resolution")).isEqualTo("2K");
        assertThat(body.getString("size")).isEqualTo("2048x2048");
        assertThat(body.getInt("n")).isEqualTo(2);
        assertThat(body.getString("output_format")).isEqualTo("png");
        assertThat(body.getInt("output_compression")).isEqualTo(80);
        assertThat(body.getLong("seed")).isEqualTo(42L);
        assertThat(body.getString("user")).isEqualTo("user-123");
        assertThat(body.has("stream")).isFalse();
    }

    @Test
    void frameCountAndFrameDurationMsAreEmittedOnlyWhenSetWithLoudBounds() {
        OpenRouterImageGenerationRequest request = new OpenRouterImageGenerationRequest.Builder(client())
                .model("bytedance-seed/seedream-4.5")
                .prompt("a red panda astronaut")
                .outputFormat("gif")
                .frameCount(8)
                .frameDurationMs(100)
                .build();
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getInt("frame_count")).isEqualTo(8);
        assertThat(body.getInt("frame_duration_ms")).isEqualTo(100);
        assertThat(request.frameCount()).isEqualTo(8);
        assertThat(request.frameDurationMs()).isEqualTo(100);

        OpenRouterImageGenerationRequest absent = new OpenRouterImageGenerationRequest.Builder(client())
                .model("bytedance-seed/seedream-4.5")
                .prompt("a red panda astronaut")
                .build();
        assertThat(absent.frameCount()).isNull();
        assertThat(absent.frameDurationMs()).isNull();

        OpenRouterImageGenerationRequest unset = new OpenRouterImageGenerationRequest.Builder(client())
                .model("bytedance-seed/seedream-4.5")
                .prompt("a red panda astronaut")
                .frameCount(8)
                .frameCount(null)
                .frameDurationMs(100)
                .frameDurationMs(null)
                .build();
        JSONObject unsetBody = new JSONObject(unset.getBody());
        assertThat(unsetBody.has("frame_count")).isFalse();
        assertThat(unsetBody.has("frame_duration_ms")).isFalse();
        assertThat(unset.frameCount()).isNull();
        assertThat(unset.frameDurationMs()).isNull();

        // The spec's outer bounds are validated loudly; the exact boundaries pass.
        new OpenRouterImageGenerationRequest.Builder(client())
                .model("m").prompt("p").frameCount(2).frameDurationMs(20).build();
        new OpenRouterImageGenerationRequest.Builder(client())
                .model("m").prompt("p").frameCount(64).frameDurationMs(2000).build();
        assertThatThrownBy(() -> new OpenRouterImageGenerationRequest.Builder(client()).frameCount(1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("frame_count must be between 2 and 64");
        assertThatThrownBy(() -> new OpenRouterImageGenerationRequest.Builder(client()).frameCount(65))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("frame_count must be between 2 and 64");
        assertThatThrownBy(() -> new OpenRouterImageGenerationRequest.Builder(client()).frameDurationMs(19))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("frame_duration_ms must be between 20 and 2000");
        assertThatThrownBy(() -> new OpenRouterImageGenerationRequest.Builder(client()).frameDurationMs(2001))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("frame_duration_ms must be between 20 and 2000");
    }

    @Test
    void markupBuildsTheContentPartShapeAndAcceptsTheRawHatch() {
        OpenRouterImageGenerationRequest request = new OpenRouterImageGenerationRequest.Builder(client())
                .model("openai/gpt-image-2.6-sunburst")
                .prompt("draw an arrow here")
                .addInputReferenceByUrl("https://example.com/diagram.png")
                .markup("https://example.com/markup.png")
                .build();
        JSONObject body = new JSONObject(request.getBody());
        JSONObject markup = body.getJSONObject("markup");
        assertThat(markup.keySet()).containsExactlyInAnyOrder("type", "image_url");
        assertThat(markup.getString("type")).isEqualTo("image_url");
        JSONObject imageUrl = markup.getJSONObject("image_url");
        assertThat(imageUrl.keySet()).containsExactlyInAnyOrder("url");
        assertThat(imageUrl.getString("url")).isEqualTo("https://example.com/markup.png");
        // The read-back accessor hands out a detached copy.
        JSONObject readBack = request.markup();
        assertThat(readBack.toString()).isEqualTo(markup.toString());
        readBack.getJSONObject("image_url").put("url", "https://example.com/changed.png");
        assertThat(new JSONObject(request.getBody()).getJSONObject("markup")
                .getJSONObject("image_url").getString("url"))
                .isEqualTo("https://example.com/markup.png");
        assertThat(request.markup().getJSONObject("image_url").getString("url"))
                .isEqualTo("https://example.com/markup.png");

        // The hatch is verbatim: a key the URL convenience cannot produce.
        JSONObject rawPart = new JSONObject()
                .put("type", "image_url")
                .put("image_url", new JSONObject().put("url", "data:image/png;base64,AAAA"))
                .put("detail", "high");
        OpenRouterImageGenerationRequest.Builder rawBuilder = new OpenRouterImageGenerationRequest.Builder(client())
                .model("openai/gpt-image-2.6-sunburst")
                .prompt("draw an arrow here")
                .markupRaw(rawPart);
        // The hatch stores a detached copy at set time: a mutation before
        // build() cannot change the request (the pre-build snapshot window).
        rawPart.getJSONObject("image_url").put("url", "https://example.com/pre-build.png");
        OpenRouterImageGenerationRequest raw = rawBuilder.build();
        JSONObject emittedRaw = new JSONObject(raw.getBody()).getJSONObject("markup");
        assertThat(emittedRaw.getJSONObject("image_url").getString("url"))
                .isEqualTo("data:image/png;base64,AAAA");
        assertThat(emittedRaw.getString("detail")).isEqualTo("high");
        // ... and neither can a mutation after build().
        rawPart.getJSONObject("image_url").put("url", "https://example.com/mutated.png");
        assertThat(new JSONObject(raw.getBody()).getJSONObject("markup")
                .getJSONObject("image_url").getString("url"))
                .isEqualTo("data:image/png;base64,AAAA");
        assertThat(raw.markup().getJSONObject("image_url").getString("url"))
                .isEqualTo("data:image/png;base64,AAAA");

        OpenRouterImageGenerationRequest unset = new OpenRouterImageGenerationRequest.Builder(client())
                .model("openai/gpt-image-2.6-sunburst")
                .prompt("draw an arrow here")
                .markup("https://example.com/markup.png")
                .markup(null)
                .build();
        assertThat(new JSONObject(unset.getBody()).has("markup")).isFalse();
        assertThat(unset.markup()).isNull();

        OpenRouterImageGenerationRequest rawUnset = new OpenRouterImageGenerationRequest.Builder(client())
                .model("openai/gpt-image-2.6-sunburst")
                .prompt("draw an arrow here")
                .markupRaw(rawPart)
                .markupRaw(null)
                .build();
        assertThat(new JSONObject(rawUnset.getBody()).has("markup")).isFalse();
        assertThat(rawUnset.markup()).isNull();

        assertThatThrownBy(() -> new OpenRouterImageGenerationRequest.Builder(client()).markup(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("markup url must not be empty");
    }

    @Test
    void streamFlagIsEmittedWhenRequested() {
        JSONObject body = new JSONObject(new OpenRouterImageGenerationRequest.Builder(client())
                .model("bytedance-seed/seedream-4.5")
                .prompt("a red panda astronaut")
                .stream(true)
                .build()
                .getBody());

        assertThat(body.getBoolean("stream")).isTrue();
    }

    @Test
    void inputReferencesAreEmittedAsImageUrlParts() {
        JSONObject body = new JSONObject(new OpenRouterImageGenerationRequest.Builder(client())
                .model("bytedance-seed/seedream-4.5")
                .prompt("a red panda astronaut")
                .addInputReferenceByUrl("https://example.com/panda.png")
                .addInputReferenceByBase64("data:image/png;base64,AAAA")
                .build()
                .getBody());

        assertThat(body.getJSONArray("input_references").length()).isEqualTo(2);
        JSONObject first = body.getJSONArray("input_references").getJSONObject(0);
        assertThat(first.getString("type")).isEqualTo("image_url");
        assertThat(first.getJSONObject("image_url").getString("url"))
                .isEqualTo("https://example.com/panda.png");
        JSONObject second = body.getJSONArray("input_references").getJSONObject(1);
        assertThat(second.getJSONObject("image_url").getString("url"))
                .isEqualTo("data:image/png;base64,AAAA");
    }

    @Test
    void moreThanSixteenInputReferencesAreRejected() {
        OpenRouterImageGenerationRequest.Builder builder = new OpenRouterImageGenerationRequest.Builder(client())
                .model("bytedance-seed/seedream-4.5")
                .prompt("a red panda astronaut");
        for (int i = 0; i < 16; i++) {
            builder.addInputReferenceByUrl("https://example.com/" + i + ".png");
        }
        assertThatThrownBy(() -> builder.addInputReferenceByUrl("https://example.com/16.png"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void providerObjectIsEmittedOnlyWhenAnyProviderOptionIsSet() {
        JSONObject body = new JSONObject(new OpenRouterImageGenerationRequest.Builder(client())
                .model("bytedance-seed/seedream-4.5")
                .prompt("a red panda astronaut")
                .providerOrder("google-ai-studio", "openai")
                .allowFallbacks(false)
                .providerOption("black-forest-labs", new JSONObject().put("steps", 40))
                .build()
                .getBody());

        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getJSONArray("order").toList()).containsExactly("google-ai-studio", "openai");
        assertThat(provider.getBoolean("allow_fallbacks")).isFalse();
        assertThat(provider.getJSONObject("options").getJSONObject("black-forest-labs").getInt("steps"))
                .isEqualTo(40);
    }

    @Test
    void nOutsideRangeIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterImageGenerationRequest.Builder(client())
                .model("bytedance-seed/seedream-4.5")
                .prompt("a red panda astronaut")
                .n(11))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterImageGenerationRequest.Builder(client())
                .model("bytedance-seed/seedream-4.5")
                .prompt("a red panda astronaut")
                .n(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void missingModelOrPromptIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterImageGenerationRequest.Builder(client())
                .prompt("a red panda astronaut")
                .build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterImageGenerationRequest.Builder(client())
                .model("bytedance-seed/seedream-4.5")
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void responseExposesImagesAndUsage() {
        String fixture = """
                {
                  "created": 1748372400,
                  "data": [
                    {"b64_json": "AAAA", "media_type": "image/png"}
                  ],
                  "usage": {
                    "completion_tokens": 4175,
                    "cost": 0.04,
                    "prompt_tokens": 0,
                    "total_tokens": 4175
                  }
                }
                """;
        OpenRouterImageGenerationResponse response =
                new OpenRouterImageGenerationRequest.Builder(client())
                        .model("bytedance-seed/seedream-4.5")
                        .prompt("a red panda astronaut")
                        .build()
                        .createResponse(fixture);

        assertThat(response.created()).isEqualTo(1748372400L);
        assertThat(response.images()).hasSize(1);
        OpenRouterImageGenerationResponse.OpenRouterImage image = response.firstImage();
        assertThat(image.base64()).isEqualTo("AAAA");
        assertThat(image.bytes()).isNotNull();
        assertThat(image.mediaType()).isEqualTo("image/png");
        assertThat(response.promptTokens()).isZero();
        assertThat(response.completionTokens()).isEqualTo(4175L);
        assertThat(response.totalTokens()).isEqualTo(4175L);
        assertThat(response.cost()).isEqualTo(0.04);
    }

    @Test
    void responseAccessorsSwallowMalformedBody() {
        OpenRouterImageGenerationResponse response =
                new OpenRouterImageGenerationRequest.Builder(client())
                        .model("bytedance-seed/seedream-4.5")
                        .prompt("a red panda astronaut")
                        .build()
                        .createResponse("{}");

        assertThat(response.images()).isEmpty();
        assertThat(response.firstImage()).isNull();
        assertThat(response.created()).isNull();
        assertThat(response.cost()).isNull();
    }

    @Test
    void modelsListingUsesGetMethodOnImagesModelsUrl() {
        OpenRouterImageModelsRequest request = new OpenRouterImageModelsRequest.Builder(client()).build();
        assertThat(request.getRelativeUrl()).isEqualTo("/images/models");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
    }

    @Test
    void modelsListingResponseExposesModelViews() {
        String fixture = """
                {
                  "data": [
                    {
                      "id": "bytedance-seed/seedream-4.5",
                      "name": "Seedream 4.5",
                      "description": "A text-to-image model.",
                      "created": 1692901234,
                      "architecture": {
                        "input_modalities": ["text"],
                        "output_modalities": ["image"]
                      },
                      "supported_parameters": {"resolution": {"type": "enum", "values": ["1K", "2K", "4K"]}},
                      "supports_streaming": false,
                      "endpoints": "/api/v1/images/models/bytedance-seed/seedream-4.5/endpoints"
                    }
                  ]
                }
                """;
        OpenRouterImageModelsResponse response =
                new OpenRouterImageModelsRequest.Builder(client()).build().createResponse(fixture);

        assertThat(response.models()).hasSize(1);
        OpenRouterImageModelsResponse.OpenRouterImageModel model = response.models().get(0);
        assertThat(model.id()).isEqualTo("bytedance-seed/seedream-4.5");
        assertThat(model.name()).isEqualTo("Seedream 4.5");
        assertThat(model.created()).isEqualTo(1692901234L);
        assertThat(model.inputModalities()).containsExactly("text");
        assertThat(model.outputModalities()).containsExactly("image");
        assertThat(model.supportsStreaming()).isFalse();
        assertThat(model.endpointsUrl())
                .isEqualTo("/api/v1/images/models/bytedance-seed/seedream-4.5/endpoints");
        assertThat(model.supportedParameters()).isNotNull();
    }

    @Test
    void modelEndpointsRequestSplitsAuthorAndSlug() {
        OpenRouterImageModelEndpointsRequest request =
                new OpenRouterImageModelEndpointsRequest.Builder(client(), "bytedance-seed", "seedream-4.5").build();

        assertThat(request.getRelativeUrl())
                .isEqualTo("/images/models/bytedance-seed/seedream-4.5/endpoints");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.author()).isEqualTo("bytedance-seed");
        assertThat(request.slug()).isEqualTo("seedream-4.5");
    }

    @Test
    void modelEndpointsResponseExposesEndpointViews() {
        String fixture = """
                {
                  "id": "bytedance-seed/seedream-4.5",
                  "endpoints": [
                    {
                      "provider_name": "Bytedance",
                      "provider_slug": "bytedance",
                      "provider_tag": "bytedance",
                      "supported_parameters": {},
                      "allowed_passthrough_parameters": [],
                      "supports_streaming": false,
                      "pricing": [
                        {"billable": "output_image", "cost_usd": 0.05, "unit": "image"}
                      ]
                    }
                  ]
                }
                """;
        OpenRouterImageModelEndpointsResponse response =
                new OpenRouterImageModelEndpointsRequest.Builder(client(), "bytedance-seed", "seedream-4.5")
                        .build()
                        .createResponse(fixture);

        assertThat(response.id()).isEqualTo("bytedance-seed/seedream-4.5");
        assertThat(response.endpoints()).hasSize(1);
        OpenRouterImageModelEndpointsResponse.OpenRouterImageEndpoint endpoint = response.endpoints().get(0);
        assertThat(endpoint.providerName()).isEqualTo("Bytedance");
        assertThat(endpoint.providerSlug()).isEqualTo("bytedance");
        assertThat(endpoint.providerTag()).isEqualTo("bytedance");
        assertThat(endpoint.supportsStreaming()).isFalse();
        assertThat(endpoint.allowedPassthroughParameters()).isEmpty();
        assertThat(endpoint.pricing()).isNotNull();
        assertThat(endpoint.pricing().getJSONObject(0).getDouble("cost_usd")).isEqualTo(0.05);
    }

    @Test
    void clientEntryPointBuildsTheThreeRequests() {
        assertThat(client().images().generate().model("bytedance-seed/seedream-4.5")
                .prompt("a red panda astronaut").build().getRelativeUrl()).isEqualTo("/images");
        assertThat(client().images().models().build().getRelativeUrl()).isEqualTo("/images/models");
        assertThat(client().images().modelEndpoints("bytedance-seed/seedream-4.5").build().getRelativeUrl())
                .isEqualTo("/images/models/bytedance-seed/seedream-4.5/endpoints");
        assertThatThrownBy(() -> client().images().modelEndpoints("no-slash"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
