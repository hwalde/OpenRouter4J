package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import de.entwicklertraining.openrouter4j.audio.OpenRouterSpeechRequest;
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
                // addTimestampGranularity("word")) on OpenAI-compatible
                // providers. Speaker labelling and recognition-bias terms
                // round out the optional STT controls - both are supported
                // only by some providers (HTTP 400 otherwise) and may cost
                // extra; diarize requires verbose_json and then labels each
                // word in words[] with its speaker (speaker_label):
                // .responseFormat("verbose_json")
                // .diarize(Boolean.TRUE)
                // .keyterms("OpenRouter", "Scribe")
                .execute();

        System.out.println("Transcript: " + transcription.text());
        System.out.println("Usage: input=" + transcription.inputTokens()
                + " output=" + transcription.outputTokens()
                + " seconds=" + transcription.seconds()
                + " cost=" + transcription.cost());

        // 2. Text-to-speech: the response body is the raw audio bytes
        //    (audio/mpeg for mp3, audio/pcm for pcm - 16-bit little-endian).
        //    instructions(...) are delivery hints (tone, pacing, emotion) -
        //    honored by the gpt-4o-mini-tts, Gemini TTS and Fish Audio
        //    models, silently ignored elsewhere.
        OpenRouterSpeechResponse speech = client.audio().speech()
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello from OpenRouter4J.")
                .voice("en_paul_neutral")
                .responseFormat("mp3")
                .speed(1.0)
                .instructions("Speak in a warm and friendly tone.")
                .user("end-user-42")
                .trace(OpenRouterTraceConfig.builder()
                        .traceId("support-call-4711")
                        .generationName("greeting-speech")
                        .build())
                .providerOption("openai", new JSONObject())
                .execute();

        // 3. Voice cloning/design reference parts - built here without
        //    executing to show the wire shape (no live call, no real asset):
        //    1-3 audio clips (addVoiceReferenceAudio), each transcript
        //    directly after its clip (addVoiceReferenceText), plus at most
        //    one image_url scene image (addVoiceReferenceImageUrl) - or
        //    exactly one image part on its own for voice design - and
        //    addInputReferenceRaw(JSONObject), the verbatim hatch for
        //    SpeechInputReference shapes the typed factories do not cover
        //    (e.g. a url-based clip). The parts are sent in append order;
        //    the API rejects a second image part with HTTP 400.
        OpenRouterSpeechRequest referenceBody = client.audio().speech()
                .model("mistralai/voxtral-mini-tts-2603")
                .input("Hello from OpenRouter4J.")
                .addVoiceReferenceAudio("data:audio/wav;base64,UklGRuQXDABXQVZF", "wav")
                .addVoiceReferenceText("I used to rule the world.")
                .addVoiceReferenceImageUrl("data:image/png;base64,iVBORw0KGgo")
                .addInputReferenceRaw(new JSONObject()
                        .put("type", "input_audio")
                        .put("input_audio", new JSONObject()
                                .put("url", "https://example.com/reference.wav")))
                .build();
        System.out.println("reference body: " + referenceBody.getBody());

        Path out = Path.of("generated-speech.mp3");
        Files.write(out, speech.bytes());
        System.out.println("Wrote " + out.toAbsolutePath() + " (" + speech.length() + " bytes)");
    }
}
