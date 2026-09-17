package de.entwicklertraining.openrouter4j.containers;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /containers/{container_id}/files/{file_id}/content: the raw
 * content bytes of one container file.
 *
 * <p>This is a binary endpoint - the library stores the bytes and never
 * parses them. Use {@link #bytes()} to obtain the content and write it to a
 * file or stream it onward.
 */
public final class OpenRouterContainerFileContentResponse
        extends OpenRouterResponse<OpenRouterContainerFileContentRequest> {

    private final byte[] content;

    OpenRouterContainerFileContentResponse(byte[] content, OpenRouterContainerFileContentRequest request) {
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
