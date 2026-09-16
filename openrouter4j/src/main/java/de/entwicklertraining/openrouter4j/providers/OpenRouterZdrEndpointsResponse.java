package de.entwicklertraining.openrouter4j.providers;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /endpoints/zdr: the endpoints that remain available when
 * zero-data-retention routing is requested.
 *
 * <p>Follows the swallow-and-return-empty convention.
 */
public final class OpenRouterZdrEndpointsResponse extends OpenRouterResponse<OpenRouterZdrEndpointsRequest> {

    OpenRouterZdrEndpointsResponse(JSONObject json, OpenRouterZdrEndpointsRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data[]} - the ZDR endpoints.
     *
     * @return the endpoints as typed views, empty when absent
     */
    public List<OpenRouterZdrEndpoint> items() {
        List<OpenRouterZdrEndpoint> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterZdrEndpoint(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
}
