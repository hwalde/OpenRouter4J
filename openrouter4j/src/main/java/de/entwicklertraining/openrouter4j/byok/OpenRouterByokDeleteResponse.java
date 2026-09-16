package de.entwicklertraining.openrouter4j.byok;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of DELETE /byok/{id}: the deletion confirmation.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterByokDeleteResponse extends OpenRouterResponse<OpenRouterByokDeleteRequest> {


    OpenRouterByokDeleteResponse(JSONObject json, OpenRouterByokDeleteRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code deleted} - confirmation that the credential was deleted.
 */
    public Boolean deleted() {
        if (!json.has("deleted") || json.isNull("deleted")) {
            return null;
        }
        return json.optBoolean("deleted");
    }
}
