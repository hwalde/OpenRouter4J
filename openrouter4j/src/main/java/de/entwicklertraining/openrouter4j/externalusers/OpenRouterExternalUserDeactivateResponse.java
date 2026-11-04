package de.entwicklertraining.openrouter4j.externalusers;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of DELETE /external-users/{user}: the route answers 204 with no
 * body, so this view is normally empty - the deactivation is confirmed by
 * the status code alone (repeat deactivation succeeds).
 *
 * <p>Follows the swallow-and-return-null convention; {@link #json()} stays
 * the raw escape hatch should a proxy ever add a body.
 */
public final class OpenRouterExternalUserDeactivateResponse
        extends OpenRouterResponse<OpenRouterExternalUserDeactivateRequest> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body (empty for the documented 204)
     * @param request the request that produced this response
     */
    public OpenRouterExternalUserDeactivateResponse(JSONObject json,
            OpenRouterExternalUserDeactivateRequest request) {
        super(json, request);
    }

    /**
     * @return the raw JSON body behind this view (empty for the documented 204)
     */
    public JSONObject json() {
        return json;
    }
}
