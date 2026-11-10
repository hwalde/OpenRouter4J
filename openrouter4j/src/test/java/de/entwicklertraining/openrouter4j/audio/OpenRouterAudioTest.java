package de.entwicklertraining.openrouter4j.audio;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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

    @Test
    void sttRejectsLineBreaksInMultipartInterpolatedFieldsLoudly() throws Exception {
        Path wav = tempDir.resolve("bad\r\nname.wav");
        Files.write(wav, new byte[]{0x00});

        assertThatThrownBy(() -> new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByFile(wav)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CR/LF");
        assertThatThrownBy(() -> new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("QQ==", "wav")
                .language("en\ntext")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CR/LF");
    }

    @Test
    void sttRejectsHeaderInjectionInFileNameLoudly() throws Exception {
        // a quote in the file name would end the quoted filename="..."
        // parameter of the part header and re-label the file part
        Path quoted = tempDir.resolve("a\"; name=\"model.wav");
        Files.write(quoted, new byte[]{0x00});
        assertThatThrownBy(() -> new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByFile(quoted)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fileName must not contain quote or backslash characters");

        Path backslash = tempDir.resolve("bad\\name.wav");
        Files.write(backslash, new byte[]{0x00});
        assertThatThrownBy(() -> new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByFile(backslash)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("fileName must not contain quote or backslash characters");
    }

    @Test
    void sttDiarizeAndKeytermsAreEmittedOnlyWhenSet() {
        JSONObject body = new JSONObject(new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .responseFormat("verbose_json")
                .diarize(Boolean.TRUE)
                .keyterms("OpenRouter", "Scribe")
                .build()
                .getBody());

        assertThat(body.getBoolean("diarize")).isTrue();
        assertThat(body.getJSONArray("keyterms").toList()).containsExactly("OpenRouter", "Scribe");

        JSONObject bare = new JSONObject(new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .build()
                .getBody());
        assertThat(bare.has("diarize")).isFalse();
        assertThat(bare.has("keyterms")).isFalse();
    }

    @Test
    void sttDiarizeFalseIsASetValueAndEmitted() {
        JSONObject body = new JSONObject(new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .diarize(Boolean.FALSE)
                .build()
                .getBody());

        assertThat(body.has("diarize")).isTrue();
        assertThat(body.getBoolean("diarize")).isFalse();
    }

    @Test
    void sttDiarizeNullUnsetsTheField() throws Exception {
        OpenRouterSttRequest request = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .responseFormat("verbose_json")
                .diarize(Boolean.TRUE)
                .diarize(null)
                .build();

        assertThat(request.diarize()).isNull();
        assertThat(new JSONObject(request.getBody()).has("diarize")).isFalse();

        Path wav = tempDir.resolve("test.wav");
        Files.write(wav, "fake-wav-bytes".getBytes(StandardCharsets.UTF_8));
        OpenRouterSttRequest multipart = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByFile(wav)
                .diarize(Boolean.FALSE)
                .diarize(null)
                .build();
        assertThat(multipart.diarize()).isNull();
        assertThat(new String(multipart.getBodyBytes(), StandardCharsets.UTF_8))
                .doesNotContain("name=\"diarize\"");
    }

    @Test
    void sttKeytermsSettersReplaceAndAddKeyTermAccumulates() {
        OpenRouterSttRequest.Builder builder = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav");
        builder.keyterms("OpenRouter", "Scribe");
        builder.addKeyTerm("Whisper");
        JSONObject body = new JSONObject(builder.build().getBody());
        assertThat(body.getJSONArray("keyterms").toList())
                .containsExactly("OpenRouter", "Scribe", "Whisper");

        builder.keyterms(List.of("Mistral"));
        body = new JSONObject(builder.build().getBody());
        assertThat(body.getJSONArray("keyterms").toList()).containsExactly("Mistral");

        builder.keyterms(List.of());
        body = new JSONObject(builder.build().getBody());
        assertThat(body.has("keyterms")).isFalse();

        builder.keyterms("OpenRouter");
        builder.keyterms((List<String>) null);
        body = new JSONObject(builder.build().getBody());
        assertThat(body.has("keyterms")).isFalse();

        builder.keyterms("OpenRouter");
        builder.keyterms((String[]) null);
        body = new JSONObject(builder.build().getBody());
        assertThat(body.has("keyterms")).isFalse();
    }

    @Test
    void sttKeytermBoundsAreValidatedLoudly() {
        OpenRouterSttRequest.Builder base = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav");

        assertThatThrownBy(() -> base.keyterms((String) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("keyterm");
        assertThatThrownBy(() -> base.keyterms(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> base.addKeyTerm("x".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100");
        assertThatThrownBy(() -> base.keyterms("x".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class);
        // the 100-character bound counts Unicode code points, not UTF-16 units:
        // 100 emoji are 100 code points but 200 UTF-16 chars (accepted above),
        // 101 emoji are over the bound
        assertThatThrownBy(() -> base.addKeyTerm("\uD83D\uDE00".repeat(101)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100");
        assertThatThrownBy(() -> base.keyterms("bad\r\nterm"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CR/LF");
        assertThatThrownBy(() -> base.addKeyTerm("bad\r\nterm"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CR/LF");
        List<String> tooMany = new ArrayList<>();
        for (int i = 0; i < 1001; i++) {
            tooMany.add("t" + i);
        }
        assertThatThrownBy(() -> base.keyterms(tooMany))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1000");

        // exactly at the bounds: 100 code points and 1000 terms are accepted
        builderWithBoundsAccepted();
    }

    private void builderWithBoundsAccepted() {
        OpenRouterSttRequest.Builder builder = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav");
        builder.addKeyTerm("x".repeat(100));
        // 100 emoji: 100 code points (accepted) though 200 UTF-16 chars
        builder.addKeyTerm("\uD83D\uDE00".repeat(100));
        List<String> maxTerms = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            maxTerms.add("t" + i);
        }
        builder.keyterms(maxTerms);
        JSONObject body = new JSONObject(builder.build().getBody());
        assertThat(body.getJSONArray("keyterms").length()).isEqualTo(1000);

        // the 1000-term cap holds for the accumulating path too
        assertThatThrownBy(() -> builder.addKeyTerm("one-too-many"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1000");
    }

    @Test
    void sttDiarizeRequiresVerboseJsonLoudly() {
        assertThatThrownBy(() -> new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .diarize(Boolean.TRUE)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("verbose_json");
        assertThatThrownBy(() -> new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .responseFormat("json")
                .diarize(Boolean.TRUE)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("verbose_json");

        // diarize(false) and unset pass without verbose_json
        new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .diarize(Boolean.FALSE)
                .build();
        new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .diarize(Boolean.TRUE)
                .responseFormat("verbose_json")
                .build();
    }

    @Test
    void sttRequestAccessorsExposeDiarizeAndKeyterms() {
        OpenRouterSttRequest request = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .responseFormat("verbose_json")
                .diarize(Boolean.TRUE)
                .keyterms("OpenRouter")
                .build();

        assertThat(request.diarize()).isTrue();
        assertThat(request.keyterms()).containsExactly("OpenRouter");
        assertThatThrownBy(() -> request.keyterms().add("x"))
                .isInstanceOf(UnsupportedOperationException.class);

        OpenRouterSttRequest bare = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .build();
        assertThat(bare.diarize()).isNull();
        assertThat(bare.keyterms()).isNull();
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

    @Test
    void sttMultipartFormCarriesDiarizeAndKeyterms() throws Exception {
        Path wav = tempDir.resolve("test.wav");
        Files.write(wav, "fake-wav-bytes".getBytes(StandardCharsets.UTF_8));

        OpenRouterSttRequest request = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByFile(wav)
                .responseFormat("verbose_json")
                .diarize(Boolean.TRUE)
                .keyterms("OpenRouter", "Scribe")
                .build();

        String bodyText = new String(request.getBodyBytes(), StandardCharsets.UTF_8);
        assertThat(bodyText).contains("name=\"diarize\"").contains("true");
        assertThat(bodyText).contains("name=\"keyterms[]\"")
                .contains("OpenRouter").contains("Scribe");
        assertThat(request.getBody()).contains("diarize=true").contains("keyterms=[OpenRouter, Scribe]");

        OpenRouterSttRequest bare = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByFile(wav)
                .build();
        String bareText = new String(bare.getBodyBytes(), StandardCharsets.UTF_8);
        assertThat(bareText).doesNotContain("name=\"diarize\"").doesNotContain("name=\"keyterms[]\"");
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
    void sttResponseExposesSpeakerLabels() {
        String fixture = """
                {
                  "text": "Hello there.",
                  "segments": [
                    {"id": 0, "start": 0, "end": 3.2, "text": "Hello there.", "speaker": 0,
                     "speaker_label": "speaker_0"},
                    {"id": 1, "start": 3.2, "end": 5.0, "text": "Hi.", "speaker_label": 42}
                  ],
                  "words": [
                    {"word": "Hello", "start": 0, "end": 0.4, "speaker": 0,
                     "speaker_label": "speaker_0"},
                    {"word": "there.", "start": 0.4, "end": 0.8, "speaker_label": null}
                  ]
                }
                """;
        OpenRouterSttResponse response = new OpenRouterSttRequest.Builder(client())
                .model("openai/whisper-large-v3")
                .audioByBase64("UklGRiQA", "wav")
                .responseFormat("verbose_json")
                .diarize(Boolean.TRUE)
                .build()
                .createResponse(fixture);

        assertThat(response.segments().get(0).speakerLabel()).isEqualTo("speaker_0");
        assertThat(response.segments().get(1).speakerLabel()).isNull();
        assertThat(response.words().get(0).speakerLabel()).isEqualTo("speaker_0");
        assertThat(response.words().get(1).speakerLabel()).isNull();
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
    void speechInstructionsAreEmittedOnlyWhenSet() {
        OpenRouterSpeechRequest request = new OpenRouterSpeechRequest.Builder(client())
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello world")
                .instructions("Speak in a warm and friendly tone.")
                .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getString("instructions")).isEqualTo("Speak in a warm and friendly tone.");
        assertThat(request.instructions()).isEqualTo("Speak in a warm and friendly tone.");

        OpenRouterSpeechRequest bare = new OpenRouterSpeechRequest.Builder(client())
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello world")
                .build();
        assertThat(new JSONObject(bare.getBody()).has("instructions")).isFalse();
        assertThat(bare.instructions()).isNull();

        OpenRouterSpeechRequest unset = new OpenRouterSpeechRequest.Builder(client())
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello world")
                .instructions("Speak slowly.")
                .instructions(null)
                .build();
        assertThat(new JSONObject(unset.getBody()).has("instructions")).isFalse();
        assertThat(unset.instructions()).isNull();
    }

    @Test
    void speechSceneImageAndRawReferenceShapes() {
        JSONObject body = new JSONObject(new OpenRouterSpeechRequest.Builder(client())
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello world")
                .addVoiceReferenceAudio("data:audio/wav;base64,UklGRuQXDABXQVZF", "wav")
                .addVoiceReferenceText("I used to rule the world.")
                .addVoiceReferenceImageUrl("data:image/png;base64,iVBORw0KGgo")
                .build()
                .getBody());

        JSONArray refs = body.getJSONArray("input_references");
        assertThat(refs.length()).isEqualTo(3);
        JSONObject audioPart = refs.getJSONObject(0);
        assertThat(audioPart.keySet()).containsExactlyInAnyOrder("type", "input_audio");
        assertThat(audioPart.getJSONObject("input_audio").keySet())
                .containsExactlyInAnyOrder("data", "format");
        assertThat(audioPart.getJSONObject("input_audio").getString("data"))
                .isEqualTo("data:audio/wav;base64,UklGRuQXDABXQVZF");
        JSONObject textPart = refs.getJSONObject(1);
        assertThat(textPart.keySet()).containsExactlyInAnyOrder("type", "text");
        assertThat(textPart.getString("text")).isEqualTo("I used to rule the world.");
        JSONObject imagePart = refs.getJSONObject(2);
        assertThat(imagePart.keySet()).containsExactlyInAnyOrder("type", "image_url");
        assertThat(imagePart.getString("type")).isEqualTo("image_url");
        assertThat(imagePart.getJSONObject("image_url").keySet()).containsExactlyInAnyOrder("url");
        assertThat(imagePart.getJSONObject("image_url").getString("url"))
                .isEqualTo("data:image/png;base64,iVBORw0KGgo");

        // the verbatim hatch passes unknown shapes through untouched
        JSONObject rawPart = new JSONObject()
                .put("type", "input_audio")
                .put("input_audio", new JSONObject().put("url", "https://example.com/reference.wav"))
                .put("provider_extension", new JSONObject().put("x", 1));
        JSONObject rawBody = new JSONObject(new OpenRouterSpeechRequest.Builder(client())
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello world")
                .addInputReferenceRaw(rawPart)
                .build()
                .getBody());
        JSONObject emitted = rawBody.getJSONArray("input_references").getJSONObject(0);
        assertThat(emitted.keySet()).containsExactlyInAnyOrder("type", "input_audio", "provider_extension");
        assertThat(emitted.getJSONObject("input_audio").keySet()).containsExactlyInAnyOrder("url");
        assertThat(emitted.getJSONObject("input_audio").getString("url"))
                .isEqualTo("https://example.com/reference.wav");
        assertThat(emitted.getJSONObject("provider_extension").getInt("x")).isEqualTo(1);

        assertThatThrownBy(() -> new OpenRouterSpeechRequest.Builder(client())
                .addVoiceReferenceImageUrl(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("voice reference image url");
        assertThatThrownBy(() -> new OpenRouterSpeechRequest.Builder(client())
                .addVoiceReferenceImageUrl(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("voice reference image url");
        assertThatThrownBy(() -> new OpenRouterSpeechRequest.Builder(client())
                .addInputReferenceRaw(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("input reference part");
    }

    @Test
    void speechReferencePartsKeepAppendOrderAcrossFactories() {
        JSONObject body = new JSONObject(new OpenRouterSpeechRequest.Builder(client())
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello world")
                .addVoiceReferenceAudio("data:audio/wav;base64,UklGRuQ", "wav")
                .addInputReferenceRaw(new JSONObject()
                        .put("type", "input_audio")
                        .put("input_audio", new JSONObject().put("url", "https://example.com/r.wav")))
                .addVoiceReferenceText("transcript")
                .addVoiceReferenceImageUrl("data:image/png;base64,iVBORw0KGgo")
                .build()
                .getBody());

        JSONArray refs = body.getJSONArray("input_references");
        assertThat(refs.length()).isEqualTo(4);
        assertThat(refs.getJSONObject(0).getString("type")).isEqualTo("input_audio");
        assertThat(refs.getJSONObject(0).getJSONObject("input_audio").has("data")).isTrue();
        assertThat(refs.getJSONObject(1).getString("type")).isEqualTo("input_audio");
        assertThat(refs.getJSONObject(1).getJSONObject("input_audio").has("url")).isTrue();
        assertThat(refs.getJSONObject(2).getString("type")).isEqualTo("text");
        assertThat(refs.getJSONObject(3).getString("type")).isEqualTo("image_url");
    }

    @Test
    void speechInputReferencesAreSnapshotAndHandedOutAsCopies() {
        JSONObject template = new JSONObject()
                .put("type", "image_url")
                .put("image_url", new JSONObject().put("url", "https://example.com/original.png"));
        OpenRouterSpeechRequest.Builder builder = new OpenRouterSpeechRequest.Builder(client())
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello world")
                .addInputReferenceRaw(template);

        // mutating the caller's object after the add cannot change the request
        template.getJSONObject("image_url").put("url", "https://example.com/mutated.png");
        OpenRouterSpeechRequest request = builder.build();
        assertThat(new JSONObject(request.getBody()).getJSONArray("input_references")
                .getJSONObject(0).getJSONObject("image_url").getString("url"))
                .isEqualTo("https://example.com/original.png");

        // the accessor hands out detached copies
        List<JSONObject> copies = request.inputReferences();
        copies.get(0).getJSONObject("image_url").put("url", "https://example.com/via-accessor.png");
        assertThat(request.inputReferences().get(0).getJSONObject("image_url").getString("url"))
                .isEqualTo("https://example.com/original.png");
        assertThat(request.inputReferences()).hasSize(1);

        // builder reuse after build() cannot rewrite the built request
        builder.addVoiceReferenceText("extra");
        assertThat(request.inputReferences()).hasSize(1);

        assertThat(new OpenRouterSpeechRequest.Builder(client())
                .model("m").input("i").build().inputReferences()).isNull();
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
