package de.entwicklertraining.openrouter4j.observability;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /observability/destinations: the created destination.
 */
public final class OpenRouterObservabilityDestinationCreateResponse extends OpenRouterResponse<OpenRouterObservabilityDestinationCreateRequest> {


    OpenRouterObservabilityDestinationCreateResponse(JSONObject json, OpenRouterObservabilityDestinationCreateRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data} - the created destination.
 */
    public OpenRouterObservabilityDestination data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterObservabilityDestination(data) : null;
    }
}
