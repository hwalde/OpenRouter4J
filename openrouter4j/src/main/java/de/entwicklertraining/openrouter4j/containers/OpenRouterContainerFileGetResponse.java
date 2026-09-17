package de.entwicklertraining.openrouter4j.containers;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /containers/{container_id}/files/{file_id}: the metadata of
 * one container file in the {@code ContainerFile} schema.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}
 * convention.
 */
public final class OpenRouterContainerFileGetResponse
        extends OpenRouterResponse<OpenRouterContainerFileGetRequest> {

    /**
     * Creates a typed response.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterContainerFileGetResponse(JSONObject json, OpenRouterContainerFileGetRequest request) {
        super(json, request);
    }

    /**
     * JSON path: the root - the response body is the {@code ContainerFile}
     * object itself.
     *
     * @return the file view, or {@code null} when the body carries no fields
     */
    public OpenRouterContainerFile file() {
        try {
            if (json == null || json.length() == 0) {
                return null;
            }
            return new OpenRouterContainerFile(json);
        } catch (Exception ignored) {
            return null;
        }
    }
}
