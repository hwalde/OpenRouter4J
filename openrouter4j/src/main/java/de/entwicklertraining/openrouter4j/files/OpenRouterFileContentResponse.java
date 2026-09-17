package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /files/{file_id}/content: the raw file bytes proxied from
 * the upstream storage.
 *
 * <p>This is a binary endpoint - the library stores the bytes and never
 * parses them. Use {@link #bytes()} to obtain the file and write it to a
 * file or stream it onward.
 */
public final class OpenRouterFileContentResponse
        extends OpenRouterResponse<OpenRouterFileContentRequest> {

    private final byte[] content;

    OpenRouterFileContentResponse(byte[] content, OpenRouterFileContentRequest request) {
        super(new JSONObject(), request);
        this.content = content;
    }

    /**
     * @return the raw file bytes, or {@code null} when none were delivered
     */
    public byte[] bytes() {
        return content;
    }

    /**
     * @return the number of file bytes delivered, {@code 0} when none
     */
    public int length() {
        return content != null ? content.length : 0;
    }
}
