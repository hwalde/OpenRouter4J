package de.entwicklertraining.openrouter4j.audio;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /audio/speech: the synthesized speech as raw audio bytes
 * (Content-Type: {@code audio/mpeg} for mp3, {@code audio/pcm} for pcm -
 * 16-bit little-endian).
 *
 * <p>This is a binary endpoint - the library stores the bytes and never
 * parses them. Use {@link #bytes()} to obtain the audio and write it to a
 * file or play it onward.
 */
public final class OpenRouterSpeechResponse extends OpenRouterResponse<OpenRouterSpeechRequest> {

    private final byte[] content;

    OpenRouterSpeechResponse(byte[] content, OpenRouterSpeechRequest request) {
        super(new JSONObject(), request);
        this.content = content;
    }

    /**
     * @return the raw audio bytes, or {@code null} when none were delivered
     */
    public byte[] bytes() {
        return content;
    }

    /**
     * @return the number of audio bytes delivered, {@code 0} when none
     */
    public int length() {
        return content != null ? content.length : 0;
    }
}
