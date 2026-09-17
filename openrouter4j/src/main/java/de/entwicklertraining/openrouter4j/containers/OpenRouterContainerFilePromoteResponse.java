package de.entwicklertraining.openrouter4j.containers;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import de.entwicklertraining.openrouter4j.files.OpenRouterFile;
import org.json.JSONObject;

/**
 * Response of POST /containers/{container_id}/files/{file_id}/promote: the
 * promoted document in the FILES API shape.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}
 * convention.
 */
public final class OpenRouterContainerFilePromoteResponse
        extends OpenRouterResponse<OpenRouterContainerFilePromoteRequest> {

    /**
     * Creates a typed response.
     *
     * @param json the parsed response body (an empty object when the body was
     *        not a JSON object)
     * @param request the request that produced this response
     */
    public OpenRouterContainerFilePromoteResponse(JSONObject json, OpenRouterContainerFilePromoteRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} when present, otherwise the root - the promoted
     * document arrives in the FILES API shape, and depending on the API
     * generation the object sits either under a {@code data} envelope or
     * directly at the root; both placements are accepted here.
     *
     * @return the promoted file view, or {@code null} when the body carries no fields
     */
    public OpenRouterFile file() {
        try {
            if (json == null || json.length() == 0) {
                return null;
            }
            JSONObject data = json.optJSONObject("data");
            JSONObject target = data != null ? data : json;
            if (target.length() == 0) {
                return null;
            }
            return new OpenRouterFile(target);
        } catch (Exception ignored) {
            return null;
        }
    }
}
