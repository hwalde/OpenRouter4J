package de.entwicklertraining.openrouter4j.keys;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of DELETE /keys/{hash}: the deletion confirmation.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterKeyDeleteResponse extends OpenRouterResponse<OpenRouterKeyDeleteRequest> {

    OpenRouterKeyDeleteResponse(JSONObject json, OpenRouterKeyDeleteRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code deleted} - confirmation that the key was deleted.
     *
     * @return {@code true} when deleted, {@code null} when absent
     */
    public Boolean deleted() {
        if (!json.has("deleted") || json.isNull("deleted")) {
            return null;
        }
        return json.optBoolean("deleted");
    }
}
