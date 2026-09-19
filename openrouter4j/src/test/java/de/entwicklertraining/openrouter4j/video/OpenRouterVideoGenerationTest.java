package de.entwicklertraining.openrouter4j.video;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the video generation request shapes, the polling helper and the
 * binary content request against recorded JSON shapes of POST /videos,
 * GET /videos/{jobId} and GET /videos/{jobId}/content.
 */
class OpenRouterVideoGenerationTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void submissionUsesPostMethodOnVideosUrl() {
        OpenRouterVideoGenerationRequest request = new OpenRouterVideoGenerationRequest.Builder(client())
                .model("google/veo-3.1")
                .prompt("A serene mountain landscape at sunset")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/videos");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void requiredModelIsEmittedAndOptionalFieldsAreAbsentWhenUnset() {
        JSONObject body = new JSONObject(new OpenRouterVideoGenerationRequest.Builder(client())
                .model("google/veo-3.1")
                .build()
                .getBody());

        assertThat(body.getString("model")).isEqualTo("google/veo-3.1");
        assertThat(body.has("prompt")).isFalse();
        assertThat(body.has("aspect_ratio")).isFalse();
        assertThat(body.has("resolution")).isFalse();
        assertThat(body.has("size")).isFalse();
        assertThat(body.has("duration")).isFalse();
        assertThat(body.has("generate_audio")).isFalse();
        assertThat(body.has("seed")).isFalse();
        assertThat(body.has("callback_url")).isFalse();
        assertThat(body.has("previous_job_id")).isFalse();
        assertThat(body.has("creativity")).isFalse();
        assertThat(body.has("upscale_factor")).isFalse();
        assertThat(body.has("frame_images")).isFalse();
        assertThat(body.has("input_references")).isFalse();
        assertThat(body.has("provider")).isFalse();
    }

    @Test
    void optionalFieldsAreEmittedOnlyWhenSet() {
        JSONObject body = new JSONObject(new OpenRouterVideoGenerationRequest.Builder(client())
                .model("google/veo-3.1")
                .prompt("A serene mountain landscape at sunset")
                .aspectRatio("16:9")
                .resolution("720p")
                .size("1280x720")
                .duration(8)
                .generateAudio(true)
                .seed(42L)
                .callbackUrl("https://example.com/webhook")
                .previousJobId("job-previous-123")
                .creativity(1)
                .upscaleFactor(2.0)
                .build()
                .getBody());

        assertThat(body.getString("prompt")).isEqualTo("A serene mountain landscape at sunset");
        assertThat(body.getString("aspect_ratio")).isEqualTo("16:9");
        assertThat(body.getString("resolution")).isEqualTo("720p");
        assertThat(body.getString("size")).isEqualTo("1280x720");
        assertThat(body.getInt("duration")).isEqualTo(8);
        assertThat(body.getBoolean("generate_audio")).isTrue();
        assertThat(body.getLong("seed")).isEqualTo(42L);
        assertThat(body.getString("callback_url")).isEqualTo("https://example.com/webhook");
        assertThat(body.getString("previous_job_id")).isEqualTo("job-previous-123");
        assertThat(body.getInt("creativity")).isEqualTo(1);
        assertThat(body.getDouble("upscale_factor")).isEqualTo(2.0);
    }

    @Test
    void frameImagesCarryTheFrameType() {
        JSONObject body = new JSONObject(new OpenRouterVideoGenerationRequest.Builder(client())
                .model("google/veo-3.1")
                .addFirstFrameByUrl("https://example.com/first.png")
                .addLastFrameByBase64("data:image/png;base64,AAAA")
                .build()
                .getBody());

        assertThat(body.getJSONArray("frame_images").length()).isEqualTo(2);
        JSONObject first = body.getJSONArray("frame_images").getJSONObject(0);
        assertThat(first.getString("type")).isEqualTo("image_url");
        assertThat(first.getString("frame_type")).isEqualTo("first_frame");
        assertThat(first.getJSONObject("image_url").getString("url")).isEqualTo("https://example.com/first.png");
        JSONObject last = body.getJSONArray("frame_images").getJSONObject(1);
        assertThat(last.getString("frame_type")).isEqualTo("last_frame");
    }

    @Test
    void inputReferencesCarryTheTypeDiscriminator() {
        JSONObject body = new JSONObject(new OpenRouterVideoGenerationRequest.Builder(client())
                .model("google/veo-3.1")
                .addInputReferenceImage("https://example.com/ref.png")
                .addInputReferenceAudio("https://example.com/ref.mp3")
                .addInputReferenceVideo("https://example.com/ref.mp4")
                .build()
                .getBody());

        assertThat(body.getJSONArray("input_references").length()).isEqualTo(3);
        assertThat(body.getJSONArray("input_references").getJSONObject(0).getString("type"))
                .isEqualTo("image_url");
        assertThat(body.getJSONArray("input_references").getJSONObject(1).getString("type"))
                .isEqualTo("audio_url");
        assertThat(body.getJSONArray("input_references").getJSONObject(2).getString("type"))
                .isEqualTo("video_url");
    }

    @Test
    void providerOptionsAreWrappedInProviderObject() {
        JSONObject body = new JSONObject(new OpenRouterVideoGenerationRequest.Builder(client())
                .model("google/veo-3.1")
                .providerOption("google-vertex", new JSONObject().put("output_config", new JSONObject().put("effort", "low")))
                .build()
                .getBody());

        assertThat(body.getJSONObject("provider").getJSONObject("options")
                .getJSONObject("google-vertex").getJSONObject("output_config").getString("effort"))
                .isEqualTo("low");
    }

    @Test
    void nonHttpsCallbackUrlAndShortDurationAreRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterVideoGenerationRequest.Builder(client())
                .model("google/veo-3.1")
                .callbackUrl("http://example.com/webhook"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterVideoGenerationRequest.Builder(client())
                .model("google/veo-3.1")
                .duration(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void missingModelIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterVideoGenerationRequest.Builder(client()).build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void jobRequestUsesGetMethodOnJobUrl() {
        OpenRouterVideoJobRequest request = new OpenRouterVideoJobRequest.Builder(client(), "job-abc123").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/videos/job-abc123");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.jobId()).isEqualTo("job-abc123");
        assertThatThrownBy(() -> new OpenRouterVideoJobRequest.Builder(client(), null).build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void contentRequestIsBinaryAndUsesGetMethodOnContentUrl() {
        OpenRouterVideoContentRequest request =
                new OpenRouterVideoContentRequest.Builder(client(), "job-abc123").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/videos/job-abc123/content");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.isBinaryResponse()).isTrue();
    }

    @Test
    void contentResponseHoldsTheVideoBytes() {
        OpenRouterVideoContentRequest request =
                new OpenRouterVideoContentRequest.Builder(client(), "job-abc123").build();
        byte[] video = "fake-video-bytes".getBytes(StandardCharsets.UTF_8);

        OpenRouterVideoContentResponse response = request.createResponse(video);

        assertThat(response.bytes()).isSameAs(video);
        assertThat(response.length()).isEqualTo(video.length);
    }

    @Test
    void submissionResponseExposesJobFields() {
        String fixture = """
                {
                  "generation_id": "gen-xyz789",
                  "id": "job-abc123",
                  "polling_url": "/api/v1/videos/job-abc123",
                  "status": "pending"
                }
                """;
        OpenRouterVideoGenerationResponse<OpenRouterVideoGenerationRequest> response =
                new OpenRouterVideoGenerationRequest.Builder(client())
                        .model("google/veo-3.1")
                        .build()
                        .createResponse(fixture);

        assertThat(response.id()).isEqualTo("job-abc123");
        assertThat(response.pollingUrl()).isEqualTo("/api/v1/videos/job-abc123");
        assertThat(response.status()).isEqualTo("pending");
        assertThat(response.generationId()).isEqualTo("gen-xyz789");
        assertThat(response.isCompleted()).isFalse();
        assertThat(response.isTerminal()).isFalse();
    }

    @Test
    void completedResponseExposesUrlsAndUsage() {
        String fixture = """
                {
                  "id": "job-abc123",
                  "polling_url": "/api/v1/videos/job-abc123",
                  "status": "completed",
                  "unsigned_urls": ["https://storage.example.com/video.mp4"],
                  "usage": {"cost": 0.5, "is_byok": false}
                }
                """;
        OpenRouterVideoGenerationResponse<OpenRouterVideoJobRequest> response =
                new OpenRouterVideoJobRequest.Builder(client(), "job-abc123")
                        .build()
                        .createResponse(fixture);

        assertThat(response.isCompleted()).isTrue();
        assertThat(response.isTerminal()).isTrue();
        assertThat(response.unsignedUrls()).containsExactly("https://storage.example.com/video.mp4");
        assertThat(response.cost()).isEqualTo(0.5);
        assertThat(response.isByok()).isFalse();
    }

    @Test
    void failedResponseExposesTheError() {
        String fixture = """
                {
                  "id": "job-abc123",
                  "polling_url": "/api/v1/videos/job-abc123",
                  "status": "failed",
                  "error": "provider failed"
                }
                """;
        OpenRouterVideoGenerationResponse<OpenRouterVideoJobRequest> response =
                new OpenRouterVideoJobRequest.Builder(client(), "job-abc123")
                        .build()
                        .createResponse(fixture);

        assertThat(response.isTerminal()).isTrue();
        assertThat(response.error()).isEqualTo("provider failed");
    }

    @Test
    void awaitCompletionReturnsImmediatelyWhenAlreadyTerminal() {
        String fixture = """
                {
                  "id": "job-abc123",
                  "polling_url": "/api/v1/videos/job-abc123",
                  "status": "completed"
                }
                """;
        OpenRouterVideoGenerationResponse<OpenRouterVideoJobRequest> response =
                new OpenRouterVideoJobRequest.Builder(client(), "job-abc123")
                        .build()
                        .createResponse(fixture);

        OpenRouterVideoGenerationResponse<OpenRouterVideoJobRequest> result =
                response.awaitCompletion(null, 250, 1_000);

        assertThat(result).isSameAs(response);
    }

    @Test
    void awaitCompletionReturnsThisWhenJobIdIsMissing() {
        String fixture = """
                {
                  "status": "pending"
                }
                """;
        OpenRouterVideoGenerationResponse<OpenRouterVideoGenerationRequest> response =
                new OpenRouterVideoGenerationRequest.Builder(client())
                        .model("google/veo-3.1")
                        .build()
                        .createResponse(fixture);

        OpenRouterVideoGenerationResponse<OpenRouterVideoGenerationRequest> result =
                response.awaitCompletion(null, 250, 1_000);

        assertThat(result).isSameAs(response);
    }

    @Test
    void modelsListingUsesGetMethodOnVideosModelsUrl() {
        OpenRouterVideoModelsRequest request = new OpenRouterVideoModelsRequest.Builder(client()).build();
        assertThat(request.getRelativeUrl()).isEqualTo("/videos/models");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
    }

    @Test
    void modelsListingResponseExposesModelViews() {
        String fixture = """
                {
                  "data": [
                    {
                      "id": "google/veo-3.1",
                      "name": "Veo 3.1",
                      "canonical_slug": "google/veo-3.1",
                      "description": "Google video generation model",
                      "created": 1700000000,
                      "generate_audio": true,
                      "seed": null,
                      "supported_aspect_ratios": ["16:9"],
                      "supported_durations": [5, 8],
                      "supported_frame_images": ["first_frame", "last_frame"],
                      "supported_resolutions": ["720p"],
                      "supported_sizes": null,
                      "allowed_passthrough_parameters": [],
                      "pricing_skus": {"generate": "0.50"}
                    }
                  ]
                }
                """;
        OpenRouterVideoModelsResponse response =
                new OpenRouterVideoModelsRequest.Builder(client()).build().createResponse(fixture);

        assertThat(response.models()).hasSize(1);
        OpenRouterVideoModelsResponse.OpenRouterVideoModel model = response.models().get(0);
        assertThat(model.id()).isEqualTo("google/veo-3.1");
        assertThat(model.canonicalSlug()).isEqualTo("google/veo-3.1");
        assertThat(model.generateAudio()).isTrue();
        assertThat(model.seed()).isNull();
        assertThat(model.supportedResolutions()).containsExactly("720p");
        assertThat(model.supportedDurations()).containsExactly(5, 8);
        assertThat(model.supportedFrameImages()).containsExactly("first_frame", "last_frame");
        assertThat(model.pricingSkus().getString("generate")).isEqualTo("0.50");
    }

    @Test
    void responseAccessorsSwallowMalformedBody() {
        OpenRouterVideoGenerationResponse<OpenRouterVideoJobRequest> response =
                new OpenRouterVideoJobRequest.Builder(client(), "job-abc123")
                        .build()
                        .createResponse("{}");

        assertThat(response.id()).isNull();
        assertThat(response.status()).isNull();
        assertThat(response.unsignedUrls()).isEmpty();
        assertThat(response.cost()).isNull();
        assertThat(response.usage()).isNull();
    }

    @Test
    void clientEntryPointBuildsTheVideoRequests() {
        assertThat(client().videos().generate().model("google/veo-3.1").build().getRelativeUrl())
                .isEqualTo("/videos");
        assertThat(client().videos().job("job-1").build().getRelativeUrl()).isEqualTo("/videos/job-1");
        assertThat(client().videos().jobContent("job-1").build().getRelativeUrl())
                .isEqualTo("/videos/job-1/content");
        assertThat(client().videos().models().build().getRelativeUrl()).isEqualTo("/videos/models");
    }
}
