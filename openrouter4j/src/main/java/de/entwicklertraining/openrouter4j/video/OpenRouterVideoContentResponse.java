package de.entwicklertraining.openrouter4j.video;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /videos/{jobId}/content: the raw video bytes proxied from
 * the upstream provider (per the API a {@code video/mp4} stream).
 *
 * <p>This is a binary endpoint - the library stores the bytes and never
 * parses them. Use {@link #bytes()} to obtain the video and write it to a
 * file or stream it onward.
 */
public final class OpenRouterVideoContentResponse
        extends OpenRouterResponse<OpenRouterVideoContentRequest> {

    private final byte[] content;

    OpenRouterVideoContentResponse(byte[] content, OpenRouterVideoContentRequest request) {
        super(new JSONObject(), request);
        this.content = content;
    }

    /**
     * @return the raw video bytes, or {@code null} when none were delivered
     */
    public byte[] bytes() {
        return content;
    }

    /**
     * @return the number of video bytes delivered, {@code 0} when none
     */
    public int length() {
        return content != null ? content.length : 0;
    }
}
