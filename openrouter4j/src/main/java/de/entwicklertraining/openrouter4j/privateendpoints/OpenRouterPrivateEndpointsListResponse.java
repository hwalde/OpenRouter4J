package de.entwicklertraining.openrouter4j.privateendpoints;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Response of GET /private-endpoints (schema
 * {@code ListPrivateEndpointsResponse}): the organization's private
 * endpoints, drafts included, as summary rows.
 *
 * <p>The schema carries no total count and no pagination - the whole list
 * arrives in one {@code data} array.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterPrivateEndpointsListResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterPrivateEndpointsListResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the endpoint summary rows. Non-object
     * entries are dropped.
     *
     * @return the rows, empty when absent
     */
    public List<OpenRouterPrivateEndpointSummary> items() {
        List<OpenRouterPrivateEndpointSummary> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = data.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterPrivateEndpointSummary(row));
                    }
                }
            }
        } catch (Exception e) {
            // swallow-and-return-empty convention
        }
        return result;
    }
}
