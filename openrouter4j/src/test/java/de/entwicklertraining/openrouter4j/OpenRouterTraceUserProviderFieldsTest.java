package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.openrouter4j.audio.OpenRouterSpeechRequest;
import de.entwicklertraining.openrouter4j.audio.OpenRouterSttRequest;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsRequest;
import de.entwicklertraining.openrouter4j.image.OpenRouterImageGenerationRequest;
import de.entwicklertraining.openrouter4j.rerank.OpenRouterRerankRequest;
import de.entwicklertraining.openrouter4j.video.OpenRouterVideoGenerationRequest;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@code trace} / {@code user} observability fields (and the STT /
 * speech {@code provider.options} passthrough) on the non-chat inference
 * endpoints: every field is emitted only when explicitly set and absent
 * otherwise, on every one of the six requests.
 */
class OpenRouterTraceUserProviderFieldsTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    private OpenRouterTraceConfig trace() {
        return OpenRouterTraceConfig.builder().traceId("trace-1").build();
    }

    @Test
    void embeddingsTraceIsEmittedWhenSetAndAbsentWhenUnset() {
        JSONObject body = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .trace(trace())
                .build()
                .getBody());
        assertThat(body.getJSONObject("trace").getString("trace_id")).isEqualTo("trace-1");

        JSONObject unset = new JSONObject(new OpenRouterEmbeddingsRequest.Builder(client())
                .model("openai/text-embedding-3-small")
                .input("hello")
                .build()
                .getBody());
        assertThat(unset.has("trace")).isFalse();
    }

    @Test
    void rerankTraceAndUserAreEmittedWhenSetAndAbsentWhenUnset() {
        JSONObject body = new JSONObject(new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("q")
                .addDocument("doc")
                .trace(trace())
                .user("user-1")
                .build()
                .getBody());
        assertThat(body.getJSONObject("trace").getString("trace_id")).isEqualTo("trace-1");
        assertThat(body.getString("user")).isEqualTo("user-1");

        JSONObject unset = new JSONObject(new OpenRouterRerankRequest.Builder(client())
                .model("cohere/rerank-v3.5")
                .query("q")
                .addDocument("doc")
                .build()
                .getBody());
        assertThat(unset.has("trace")).isFalse();
        assertThat(unset.has("user")).isFalse();
    }

    @Test
    void sttJsonFormEmitsTraceUserAndProviderOptionsWhenSet() {
        JSONObject body = new JSONObject(new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .trace(trace())
                .user("user-1")
                .providerOption("openai", new JSONObject().put("prompt", "greeting"))
                .build()
                .getBody());
        assertThat(body.getJSONObject("trace").getString("trace_id")).isEqualTo("trace-1");
        assertThat(body.getString("user")).isEqualTo("user-1");
        assertThat(body.getJSONObject("provider").getJSONObject("options")
                .getJSONObject("openai").getString("prompt")).isEqualTo("greeting");

        JSONObject unset = new JSONObject(new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .build()
                .getBody());
        assertThat(unset.has("trace")).isFalse();
        assertThat(unset.has("user")).isFalse();
        assertThat(unset.has("provider")).isFalse();
    }

    @Test
    void sttMultipartFormCarriesUserAndJsonEncodedTrace() throws Exception {
        Path wav = Files.createTempFile("trace-test", ".wav");
        Files.write(wav, "fake-wav".getBytes(StandardCharsets.UTF_8));

        OpenRouterSttRequest request = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByFile(wav)
                .trace(trace())
                .user("user-1")
                .build();
        String bodyText = new String(request.getBodyBytes(), StandardCharsets.UTF_8);
        assertThat(bodyText).contains("name=\"user\"").contains("user-1");
        assertThat(bodyText).contains("name=\"trace\"")
                .contains("{\"trace_id\":\"trace-1\"}");

        OpenRouterSttRequest unset = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByFile(wav)
                .build();
        String unsetText = new String(unset.getBodyBytes(), StandardCharsets.UTF_8);
        assertThat(unsetText).doesNotContain("name=\"user\"");
        assertThat(unsetText).doesNotContain("name=\"trace\"");
    }

    @Test
    void sttMultipartFormNeverCarriesAProviderFormField() throws Exception {
        Path wav = Files.createTempFile("provider-test", ".wav");
        Files.write(wav, "fake-wav".getBytes(StandardCharsets.UTF_8));

        OpenRouterSttRequest request = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByFile(wav)
                .providerOption("openai", new JSONObject().put("prompt", "greeting"))
                .build();

        // The multipart form schema has no provider field - the passthrough
        // is JSON-mode only and must not leak into the byte stream.
        assertThat(request.isMultipart()).isTrue();
        assertThat(new String(request.getBodyBytes(), StandardCharsets.UTF_8))
                .doesNotContain("name=\"provider\"");
    }

    @Test
    void speechEmitsTraceUserAndProviderOptionsWhenSet() {
        JSONObject body = new JSONObject(new OpenRouterSpeechRequest.Builder(client())
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello")
                .trace(trace())
                .user("user-1")
                .providerOption("openai", new JSONObject().put("style", "warm"))
                .build()
                .getBody());
        assertThat(body.getJSONObject("trace").getString("trace_id")).isEqualTo("trace-1");
        assertThat(body.getString("user")).isEqualTo("user-1");
        assertThat(body.getJSONObject("provider").getJSONObject("options")
                .getJSONObject("openai").getString("style")).isEqualTo("warm");

        JSONObject unset = new JSONObject(new OpenRouterSpeechRequest.Builder(client())
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello")
                .build()
                .getBody());
        assertThat(unset.has("trace")).isFalse();
        assertThat(unset.has("user")).isFalse();
        assertThat(unset.has("provider")).isFalse();
    }

    @Test
    void imageGenerationTraceIsEmittedWhenSetAndAbsentWhenUnset() {
        JSONObject body = new JSONObject(new OpenRouterImageGenerationRequest.Builder(client())
                .model("google/imagen-4")
                .prompt("a cat")
                .trace(trace())
                .build()
                .getBody());
        assertThat(body.getJSONObject("trace").getString("trace_id")).isEqualTo("trace-1");

        JSONObject unset = new JSONObject(new OpenRouterImageGenerationRequest.Builder(client())
                .model("google/imagen-4")
                .prompt("a cat")
                .build()
                .getBody());
        assertThat(unset.has("trace")).isFalse();
    }

    @Test
    void videoGenerationTraceAndUserAreEmittedWhenSetAndAbsentWhenUnset() {
        JSONObject body = new JSONObject(new OpenRouterVideoGenerationRequest.Builder(client())
                .model("google/veo-3.1")
                .prompt("a wave")
                .trace(trace())
                .user("user-1")
                .build()
                .getBody());
        assertThat(body.getJSONObject("trace").getString("trace_id")).isEqualTo("trace-1");
        assertThat(body.getString("user")).isEqualTo("user-1");

        JSONObject unset = new JSONObject(new OpenRouterVideoGenerationRequest.Builder(client())
                .model("google/veo-3.1")
                .prompt("a wave")
                .build()
                .getBody());
        assertThat(unset.has("trace")).isFalse();
        assertThat(unset.has("user")).isFalse();
    }
}
