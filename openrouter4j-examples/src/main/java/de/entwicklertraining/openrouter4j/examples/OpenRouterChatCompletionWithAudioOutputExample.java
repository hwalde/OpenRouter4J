package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates multimodal OUTPUT: asking an audio-output model to speak.
 *
 * Non-text output is unreachable without modalities(...): the request must
 * explicitly ask for the "audio" modality. The generated audio arrives in
 * choices[0].message.audio with id, base64 data, expires_at and a transcript.
 */
public class OpenRouterChatCompletionWithAudioOutputExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("google/gemini-2.5-flash-preview-tts") // or any audio-output model
                .modalities("text", "audio")                  // required for audio output
                .addMessage("user", "Say hello in a friendly voice.")
                .execute();

        System.out.println("Text part: " + response.assistantMessage());
        System.out.println("Transcript: " + response.audioTranscript());
        System.out.println("Audio id: " + response.audioId());
        System.out.println("Expires at: " + response.audioExpiresAt());

        // The base64 payload can be written to a file and played back:
        String audioData = response.audioData();
        if (audioData != null) {
            System.out.println("Audio payload received: " + audioData.length() + " base64 chars.");
        }
    }
}
