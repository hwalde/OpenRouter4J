package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /files/{file_id}: the file document.
 *
 * <p>Tolerance: some response shapes wrap the file in a {@code data} object,
 * others place it at the top level. {@link #file()} reads {@code data} when
 * it is an object and falls back to the root itself. The accessors of the
 * returned view follow the swallow-and-return-null convention.
 */
public final class OpenRouterFileGetResponse
        extends OpenRouterResponse<OpenRouterFileGetRequest> {

    OpenRouterFileGetResponse(JSONObject json, OpenRouterFileGetRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the file document; when {@code data} is
     * absent or not an object, the root object itself is used as the file
     * document.
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
