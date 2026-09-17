package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /files: the uploaded file document.
 *
 * <p>Tolerance trap: the API schema's 200 example returns the file object at
 * the top level, while some responses wrap it in a {@code data} object.
 * {@link #file()} reads {@code data} when it is an object and falls back to
 * the root itself, so both wire forms resolve to an {@link OpenRouterFile}.
 * The accessors of the returned view follow the swallow-and-return-null
 * convention.
 */
public final class OpenRouterFileUploadResponse
        extends OpenRouterResponse<OpenRouterFileUploadRequest> {

    OpenRouterFileUploadResponse(JSONObject json, OpenRouterFileUploadRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the uploaded file document; when {@code data}
     * is absent or not an object, the root object itself is used as the file
     * document (the schema's 200 example places the file at the top level).
     *
     * @return the file view, or a view over the (possibly empty) root when
     *         the body carries no usable fields
     */
    public OpenRouterFile file() {
        try {
            JSONObject data = json.optJSONObject("data");
            return new OpenRouterFile(data != null ? data : json);
        } catch (Exception ignored) {
            return null;
        }
    }
}
