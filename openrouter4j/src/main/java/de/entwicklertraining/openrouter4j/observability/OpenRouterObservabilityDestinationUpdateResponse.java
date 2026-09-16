package de.entwicklertraining.openrouter4j.observability;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of PATCH /observability/destinations/{id}: the updated destination.
 */
public final class OpenRouterObservabilityDestinationUpdateResponse extends OpenRouterResponse<OpenRouterObservabilityDestinationUpdateRequest> {


    OpenRouterObservabilityDestinationUpdateResponse(JSONObject json, OpenRouterObservabilityDestinationUpdateRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data} - the updated destination.
 */
    public OpenRouterObservabilityDestination data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterObservabilityDestination(data) : null;
    }
}
