package de.entwicklertraining.openrouter4j.observability;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of DELETE /observability/destinations/{id}: the deletion
 * confirmation.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterObservabilityDestinationDeleteResponse extends OpenRouterResponse<OpenRouterObservabilityDestinationDeleteRequest> {


    OpenRouterObservabilityDestinationDeleteResponse(JSONObject json, OpenRouterObservabilityDestinationDeleteRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code deleted} - confirmation that the destination was deleted.
 */
    public Boolean deleted() {
        if (!json.has("deleted") || json.isNull("deleted")) {
            return null;
        }
        return json.optBoolean("deleted");
    }
}
