package de.entwicklertraining.openrouter4j.audio;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the STT and TTS request shapes (JSON and multipart wire forms) and
 * the response accessors against recorded JSON shapes of
 * POST /audio/transcriptions and POST /audio/speech.
 */
class OpenRouterAudioTest {

    @TempDir
    Path tempDir;

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    // ---------- STT: JSON form ----------

    @Test
    void sttJsonFormEmitsModelAndInputAudio() {
        OpenRouterSttRequest request = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/audio/transcriptions");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
        assertThat(request.isMultipart()).isFalse();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getString("model")).isEqualTo("openai/whisper-large-v3");
        assertThat(body.getJSONObject("input_audio").getString("data")).isEqualTo("UklGRiQA");
        assertThat(body.getJSONObject("input_audio").getString("format")).isEqualTo("wav");
        assertThat(body.has("language")).isFalse();
        assertThat(body.has("response_format")).isFalse();
        assertThat(body.has("temperature")).isFalse();
        assertThat(body.has("timestamp_granularities")).isFalse();
    }

    @Test
    void sttOptionalFieldsAreEmittedOnlyWhenSet() {
        JSONObject body = new JSONObject(new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .language("en")
                .responseFormat("verbose_json")
                .temperature(0.0)
                .addTimestampGranularity("segment")
                .addTimestampGranularity("word")
                .build()
                .getBody());

        assertThat(body.getString("language")).isEqualTo("en");
        assertThat(body.getString("response_format")).isEqualTo("verbose_json");
        assertThat(body.getDouble("temperature")).isZero();
        assertThat(body.getJSONArray("timestamp_granularities").toList())
                .containsExactly("segment", "word");
    }

    @Test
    void sttRejectsUnknownTimestampGranularityLoudly() {
        assertThatThrownBy(() -> new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .addTimestampGranularity("char"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sttAudioByPathReadsAndEncodesTheFile() throws Exception {
        Path wav = tempDir.resolve("test.wav");
        Files.write(wav, new byte[]{0x52, 0x49, 0x46, 0x46});

        JSONObject body = new JSONObject(new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByPath(wav, "wav")
                .build()
                .getBody());

        String expected = Base64.getEncoder().encodeToString(new byte[]{0x52, 0x49, 0x46, 0x46});
        assertThat(body.getJSONObject("input_audio").getString("data")).isEqualTo(expected);
    }

    @Test
    void sttMissingAudioIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    // ---------- STT: multipart form ----------

    @Test
    void sttMultipartFormSendsFilePartAndFormFields() throws Exception {
        byte[] audio = "fake-wav-bytes".getBytes(StandardCharsets.UTF_8);
        Path wav = tempDir.resolve("test.wav");
        Files.write(wav, audio);

        OpenRouterSttRequest request = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByFile(wav)
                .language("en")
                .addTimestampGranularity("word")
                .build();

        assertThat(request.isMultipart()).isTrue();
        assertThat(request.getContentType()).startsWith("multipart/form-data; boundary=");
        assertThat(request.getHttpMethod()).isEqualTo("POST");

        byte[] bodyBytes = request.getBodyBytes();
        String bodyText = new String(bodyBytes, StandardCharsets.UTF_8);

        assertThat(bodyText).contains("name=\"model\"").contains("openai/whisper-large-v3");
        assertThat(bodyText).contains("name=\"language\"").contains("en");
        assertThat(bodyText).contains("name=\"timestamp_granularities[]\"").contains("word");
        assertThat(bodyText).contains("name=\"file\"; filename=\"test.wav\"");
        assertThat(bodyBytes).contains(audio);
        assertThat(bodyText).endsWith("--\r\n").contains("\r\n--" + request.getContentType()
                .split("boundary=")[1]);
    }

    // ---------- STT: response ----------

    @Test
    void sttResponseExposesTextAndUsage() {
        String fixture = """
                {
                  "text": "Hello, this is a test of OpenAI speech-to-text transcription.",
                  "usage": {
                    "cost": 0.000508,
                    "input_tokens": 83,
                    "output_tokens": 30,
                    "seconds": 9.2,
                    "total_tokens": 113
                  }
                }
                """;
        OpenRouterSttResponse response = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .build()
                .createResponse(fixture);

        assertThat(response.text()).contains("speech-to-text transcription");
        assertThat(response.inputTokens()).isEqualTo(83L);
        assertThat(response.outputTokens()).isEqualTo(30L);
        assertThat(response.totalTokens()).isEqualTo(113L);
        assertThat(response.seconds()).isEqualTo(9.2);
        assertThat(response.cost()).isEqualTo(0.000508);
    }

    @Test
    void sttVerboseResponseExposesSegmentsAndWords() {
        String fixture = """
                {
                  "text": "Hello there.",
                  "task": "transcribe",
                  "language": "english",
                  "duration": 9.2,
                  "segments": [
                    {"id": 0, "start": 0, "end": 3.2, "text": "Hello there.", "speaker": 0}
                  ],
                  "words": [
                    {"word": "Hello", "start": 0, "end": 0.4, "speaker": 0}
                  ]
                }
                """;
        OpenRouterSttResponse response = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .responseFormat("verbose_json")
                .build()
                .createResponse(fixture);

        assertThat(response.task()).isEqualTo("transcribe");
        assertThat(response.language()).isEqualTo("english");
        assertThat(response.duration()).isEqualTo(9.2);
        assertThat(response.segments()).hasSize(1);
        assertThat(response.segments().get(0).text()).isEqualTo("Hello there.");
        assertThat(response.segments().get(0).start()).isZero();
        assertThat(response.segments().get(0).end()).isEqualTo(3.2);
        assertThat(response.segments().get(0).speaker()).isZero();
        assertThat(response.words()).hasSize(1);
        assertThat(response.words().get(0).word()).isEqualTo("Hello");
        assertThat(response.words().get(0).end()).isEqualTo(0.4);
    }

    @Test
    void sttResponseAccessorsSwallowMalformedBody() {
        OpenRouterSttResponse response = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .build()
                .createResponse("{}");

        assertThat(response.text()).isNull();
        assertThat(response.segments()).isEmpty();
        assertThat(response.words()).isEmpty();
        assertThat(response.usage()).isNull();
    }

    // ---------- TTS ----------

    @Test
    void speechEmitsModelInputAndOptionalFields() {
        OpenRouterSpeechRequest request = new OpenRouterSpeechRequest.Builder(client())
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello world")
                .voice("en_paul_neutral")
                .responseFormat("pcm")
                .speed(1.0)
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/audio/speech");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
        assertThat(request.isBinaryResponse()).isTrue();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getString("model")).isEqualTo("mistralai/voxtral-mini-tts-2603");
        assertThat(body.getString("input")).isEqualTo("Hello world");
        assertThat(body.getString("voice")).isEqualTo("en_paul_neutral");
        assertThat(body.getString("response_format")).isEqualTo("pcm");
        assertThat(body.getDouble("speed")).isEqualTo(1.0);
        assertThat(body.has("input_references")).isFalse();
    }

    @Test
    void speechVoiceReferencesAreEmittedAsInputAudioAndTextParts() {
        JSONObject body = new JSONObject(new OpenRouterSpeechRequest.Builder(client())
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello world")
                .addVoiceReferenceAudio("data:audio/wav;base64,UklGRuQXDABXQVZF", "wav")
                .addVoiceReferenceText("I used to rule the world.")
                .build()
                .getBody());

        assertThat(body.getJSONArray("input_references").length()).isEqualTo(2);
        JSONObject audioPart = body.getJSONArray("input_references").getJSONObject(0);
        assertThat(audioPart.getString("type")).isEqualTo("input_audio");
        assertThat(audioPart.getJSONObject("input_audio").getString("data"))
                .isEqualTo("data:audio/wav;base64,UklGRuQXDABXQVZF");
        assertThat(audioPart.getJSONObject("input_audio").getString("format")).isEqualTo("wav");
        JSONObject textPart = body.getJSONArray("input_references").getJSONObject(1);
        assertThat(textPart.getString("type")).isEqualTo("text");
        assertThat(textPart.getString("text")).isEqualTo("I used to rule the world.");
    }

    @Test
    void speechRejectsEmptyVoiceAndOversizedTranscriptLoudly() {
        assertThatThrownBy(() -> new OpenRouterSpeechRequest.Builder(client())
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello world")
                .voice(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterSpeechRequest.Builder(client())
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello world")
                .addVoiceReferenceText("x".repeat(10_001)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void speechMissingModelOrInputIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterSpeechRequest.Builder(client())
                .input("Hello world")
                .build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterSpeechRequest.Builder(client())
                .model("mistralai/voxtral-mini-tts-2603")
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void speechResponseHoldsTheAudioBytes() {
        OpenRouterSpeechRequest request = new OpenRouterSpeechRequest.Builder(client())
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello world")
                .voice("en_paul_neutral")
                .build();
        byte[] audio = "fake-pcm-bytes".getBytes(StandardCharsets.UTF_8);

        OpenRouterSpeechResponse response = request.createResponse(audio);

        assertThat(response.bytes()).isSameAs(audio);
        assertThat(response.length()).isEqualTo(audio.length);
    }

    @Test
    void clientEntryPointBuildsTheAudioRequests() {
        assertThat(client().audio().transcriptions().model("m").audioByBase64("QQ", "wav").build()
                .getRelativeUrl()).isEqualTo("/audio/transcriptions");
        assertThat(client().audio().speech().model("m").input("hi").build()
                .getRelativeUrl()).isEqualTo("/audio/speech");
    }
}
