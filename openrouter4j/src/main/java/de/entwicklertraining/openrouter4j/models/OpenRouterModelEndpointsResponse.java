package de.entwicklertraining.openrouter4j.models;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /models/{author}/{slug}/endpoints: the model entry plus its
 * serving endpoints.
 */
public final class OpenRouterModelEndpointsResponse extends OpenRouterResponse<OpenRouterModelEndpointsRequest> {

    OpenRouterModelEndpointsResponse(JSONObject json, OpenRouterModelEndpointsRequest request) {
        super(json, request);
    }

    /**
     * @return the model entry as a typed {@link OpenRouterModel} view, or
     *         {@code null} when {@code data} is absent or not an object
     */
    public OpenRouterModel model() {
        try {
            JSONObject data = json.optJSONObject("data");
            return data != null ? new OpenRouterModel(data) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code data.endpoints[]} - the serving endpoints of the model.
     *
     * @return the endpoints as typed views, empty when absent
     */
    public List<OpenRouterModelEndpoint> endpoints() {
        List<OpenRouterModelEndpoint> result = new ArrayList<>();
        try {
            JSONObject data = json.optJSONObject("data");
            JSONArray endpoints = data != null ? data.optJSONArray("endpoints") : null;
            if (endpoints != null) {
                for (int i = 0; i < endpoints.length(); i++) {
                    JSONObject endpoint = endpoints.optJSONObject(i);
                    if (endpoint != null) {
                        result.add(new OpenRouterModelEndpoint(endpoint));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
}
