package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.video.OpenRouterVideoGenerationRequest;
import de.entwicklertraining.openrouter4j.video.OpenRouterVideoGenerationResponse;
import de.entwicklertraining.openrouter4j.video.OpenRouterVideoModelsResponse;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Demonstrates the async video generation API: submit a job (POST /videos,
 * answers 202 with a polling URL), wait for it to reach a terminal state
 * (GET /videos/{jobId} - here via the {@code awaitCompletion(client)} helper) and
 * download the finished video bytes (GET /videos/{jobId}/content).
 */
public class OpenRouterVideoGenerationExample {

    public static void main(String[] args) throws Exception {
        OpenRouterClient client = new OpenRouterClient();

        // 1. Discover the video generation models (supported durations,
        //    resolutions, frame images, audio support, ...).
        OpenRouterVideoModelsResponse models = client.videos().models().execute();
        System.out.println("Video generation models:");
        for (OpenRouterVideoModelsResponse.OpenRouterVideoModel model : models.models()) {
            System.out.println("- " + model.id() + " durations=" + model.supportedDurations()
                    + " audio=" + model.generateAudio());
        }

        // 2. Submit the job. The submission response is 202 (pending).
        OpenRouterVideoGenerationResponse<OpenRouterVideoGenerationRequest> submitted = client.videos().generate()
                .model("google/veo-3.1")
                .prompt("A serene mountain landscape at sunset, time-lapse clouds")
                .aspectRatio("16:9")
                .resolution("720p")
                .duration(8)
                .execute();

        System.out.println("job " + submitted.id() + " status: " + submitted.status());

        // 3. Poll until the job is terminal (blocks; default interval 5 s,
        //    timeout 15 min). A failed job does not throw - check status().
        //    The polled responses keep the submission request's type.
        OpenRouterVideoGenerationResponse<OpenRouterVideoGenerationRequest> finished =
                submitted.awaitCompletion(client);

        System.out.println("final status: " + finished.status()
                + ", cost: " + finished.cost()
                + ", error: " + finished.error());
        System.out.println("urls: " + finished.unsignedUrls());

        // 3b. Continuation: chain a new job to the finished one via
        //     previous_job_id (extend or remix an earlier generation).
        OpenRouterVideoGenerationResponse<OpenRouterVideoGenerationRequest> remixed = client.videos().generate()
                .model("google/veo-3.1")
                .prompt("The same mountain landscape as dawn breaks, storm clouds rolling in")
                .previousJobId(finished.id())
                .execute();
        System.out.println("continuation job " + remixed.id() + " status: " + remixed.status());

        // 4. Download the raw video bytes (typically mp4). A multi-output job
        // can address each generated video with index(0), index(1), ...
        if (finished.isCompleted() && !finished.unsignedUrls().isEmpty()) {
            byte[] video = client.videos().jobContent(finished.id()).index(0).execute().bytes();
            Path out = Path.of("generated-video.mp4");
            Files.write(out, video);
            System.out.println("Wrote " + out.toAbsolutePath() + " (" + video.length + " bytes)");
        }
    }
}
