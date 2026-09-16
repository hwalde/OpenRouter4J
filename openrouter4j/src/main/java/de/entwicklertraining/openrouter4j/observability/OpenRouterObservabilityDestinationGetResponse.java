package de.entwicklertraining.openrouter4j.observability;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of GET /observability/destinations/{id}: a single destination.
 */
public final class OpenRouterObservabilityDestinationGetResponse extends OpenRouterResponse<OpenRouterObservabilityDestinationGetRequest> {


    OpenRouterObservabilityDestinationGetResponse(JSONObject json, OpenRouterObservabilityDestinationGetRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data} - the destination.
 */
    public OpenRouterObservabilityDestination data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterObservabilityDestination(data) : null;
    }
}
