package de.entwicklertraining.openrouter4j.scim;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The shared response of the SCIM sync-job create and get endpoints: one
 * sync job under {@code data}.
 *
 * <p>All accessors follow the swallow-and-return-null convention.
 */
public final class OpenRouterScimSyncJobResponse<T extends OpenRouterRequest<?>> extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterScimSyncJobResponse(JSONObject json, T request) {
        super(json, request);
    }

    /** @return the sync job, or {@code null} when the {@code data} object is absent */
    public OpenRouterScimSyncJob job() {
        JSONObject data = json.optJSONObject("data");
        return data == null ? null : new OpenRouterScimSyncJob(data);
    }
}
