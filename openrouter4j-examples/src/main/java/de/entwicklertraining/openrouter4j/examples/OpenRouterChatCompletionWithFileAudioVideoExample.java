package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;
import org.json.JSONObject;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Demonstrates the document, audio and video message content parts:
 *
 * - {@code addFileByUrl(fileData, filename)}: a {@code file} content part
 *   ({@code {"type":"file","file":{"file_data":...,"filename":...}}}) for
 *   document understanding (PDFs and other files); {@code file_data} is a URL
 *   or a base64 data URL.
 * - {@code addAudioByBase64(base64, format)}: an {@code input_audio} content
 *   part (wav, mp3, flac, m4a, ogg, aiff, aac, pcm16, pcm24).
 * - {@code addVideoByUrl(url)}: a {@code video_url} content part.
 * - {@code addFileById(fileId, filename)}: the Files-API variant - needs a real
 *   upload id, so this example only prints its wire format.
 * - {@code addContentPart(JSONObject)}: the verbatim escape hatch for other or
 *   future part types (e.g. the legacy {@code input_video} variant).
 *
 * Trap: document/audio/video understanding is model-dependent - route to a
 * model that supports the respective input.
 */
public class OpenRouterChatCompletionWithFileAudioVideoExample {

    public static void main(String[] args) throws Exception {
        OpenRouterClient client = new OpenRouterClient();

        // 1. Document (PDF) by URL - file_data is a URL or a base64 data URL.
        OpenRouterChatCompletionResponse pdfResponse = client.chat().completion()
                .model("google/gemini-3.5-flash-lite")
                .addFileByUrl("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf", "dummy.pdf")
                .addMessage("user", "What is written in this document?")
                .execute();
        System.out.println("PDF answer: " + pdfResponse.assistantMessage());

        // 2. Audio as base64 - the example synthesizes a 0.2s tone so it is self-contained.
        byte[] wav = createShortToneWav();
        OpenRouterChatCompletionResponse audioResponse = client.chat().completion()
                .model("google/gemini-3.5-flash-lite")
                .addAudioByBase64(Base64.getEncoder().encodeToString(wav), "wav")
                .addMessage("user", "What do you hear?")
                .execute();
        System.out.println("Audio answer: " + audioResponse.assistantMessage());

        // 3. Video by URL (or base64 data URL).
        OpenRouterChatCompletionResponse videoResponse = client.chat().completion()
                .model("google/gemini-3.5-flash-lite")
                .addVideoByUrl("https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_1MB.mp4")
                .addMessage("user", "Describe what happens in this clip in one sentence.")
                .execute();
        System.out.println("Video answer: " + videoResponse.assistantMessage());

        // 4. The Files-API variant needs a real upload id (see the Files API docs),
        //    so this request is only built, not executed:
        OpenRouterChatCompletionRequest byIdRequest = client.chat().completion()
                .model("google/gemini-3.5-flash-lite")
                .addFileById("file_abc123", "report.pdf")
                .addMessage("user", "Summarise the report.")
                .build();
        System.out.println("file_id wire format: " + new JSONObject(byIdRequest.getBody()));

        // 5. Verbatim escape hatch for arbitrary content parts (e.g. the legacy
        //    input_video variant) - also only built, not executed:
        OpenRouterChatCompletionRequest legacyRequest = client.chat().completion()
                .model("google/gemini-3.5-flash-lite")
                .addContentPart(new JSONObject()
                        .put("type", "input_video")
                        .put("input_video", new JSONObject().put("data", "<base64>")))
                .addMessage("user", "Describe the video.")
                .build();
        System.out.println("Escape-hatch wire format: " + new JSONObject(legacyRequest.getBody()));

        // A local PDF can be sent as a data URL as well:
        // Path pdf = Path.of("local.pdf");
        // String dataUrl = "data:application/pdf;base64,"
        //         + Base64.getEncoder().encodeToString(Files.readAllBytes(pdf));
        // ... .addFileByUrl(dataUrl, "local.pdf") ...
        if (args.length > 0 && Files.exists(Path.of(args[0]))) {
            System.out.println("Local file " + args[0] + " could be sent as a base64 data URL.");
        }
    }

    /** Synthesizes a 0.2s 440 Hz mono 8 kHz tone as a minimal WAV file. */
    private static byte[] createShortToneWav() throws Exception {
        float sampleRate = 8000f;
        int samples = (int) (0.2 * sampleRate);
        byte[] pcm = new byte[samples * 2];
        for (int i = 0; i < samples; i++) {
            short v = (short) (Math.sin(2 * Math.PI * 440 * i / sampleRate) * 8000);
            pcm[2 * i] = (byte) v;
            pcm[2 * i + 1] = (byte) (v >> 8);
        }
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        try (AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(pcm), format, samples)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, out);
            return out.toByteArray();
        }
    }
}
