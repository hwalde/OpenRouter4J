package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import de.entwicklertraining.openrouter4j.audio.OpenRouterSpeechResponse;
import de.entwicklertraining.openrouter4j.audio.OpenRouterSttResponse;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Demonstrates the dedicated Audio API: transcribing audio to text
 * (POST /audio/transcriptions) and synthesizing speech from text
 * (POST /audio/speech). The TTS response is a raw audio bytestream (mp3 or
 * pcm) held by the typed response.
 */
public class OpenRouterAudioExample {

    public static void main(String[] args) throws Exception {
        OpenRouterClient client = new OpenRouterClient();

        // 1. Speech-to-text: the audio travels as raw base64 in the JSON
        //    body (input_audio.data). Larger files (up to 25 MB) can travel
        //    as a multipart file part via audioByFile(Path).
        OpenRouterSttResponse transcription = client.audio().transcriptions()
                .model("openai/whisper-large-v3")
                .audioByPath(Path.of("sample.wav"), "wav")
                .language("en")
                // Observability: end-user id and broadcast trace metadata
                // (never sent to the provider); in multipart form trace
                // travels as a JSON-encoded string, which the library performs.
                .user("end-user-42")
                .trace(OpenRouterTraceConfig.builder()
                        .traceId("support-call-4711")
                        .generationName("transcription")
                        .build())
                // Provider passthrough (JSON body mode only - the multipart
                // form has no provider field): options reach only the
                // provider that serves the request.
                .providerOption("openai", new JSONObject().put("prompt", "Meeting transcript"))
                // verbose_json additionally returns segments (and words with
                // addTimestampGranularity("word")) on OpenAI-compatible providers:
                // .responseFormat("verbose_json")
                .execute();

        System.out.println("Transcript: " + transcription.text());
        System.out.println("Usage: input=" + transcription.inputTokens()
                + " output=" + transcription.outputTokens()
                + " seconds=" + transcription.seconds()
                + " cost=" + transcription.cost());

        // 2. Text-to-speech: the response body is the raw audio bytes
        //    (audio/mpeg for mp3, audio/pcm for pcm - 16-bit little-endian).
        OpenRouterSpeechResponse speech = client.audio().speech()
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello from OpenRouter4J.")
                .voice("en_paul_neutral")
                .responseFormat("mp3")
                .speed(1.0)
                .user("end-user-42")
                .trace(OpenRouterTraceConfig.builder()
                        .traceId("support-call-4711")
                        .generationName("greeting-speech")
                        .build())
                .providerOption("openai", new JSONObject())
                .execute();

        Path out = Path.of("generated-speech.mp3");
        Files.write(out, speech.bytes());
        System.out.println("Wrote " + out.toAbsolutePath() + " (" + speech.length() + " bytes)");
    }
}
