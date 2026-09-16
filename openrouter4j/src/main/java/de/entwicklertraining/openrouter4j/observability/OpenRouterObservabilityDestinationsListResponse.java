package de.entwicklertraining.openrouter4j.observability;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Response of GET /observability/destinations: the observability destinations
 * of the account.
 *
 * <p>Follows the swallow-and-return-empty convention.
 */
public final class OpenRouterObservabilityDestinationsListResponse extends OpenRouterResponse<OpenRouterObservabilityDestinationsListRequest> {


    OpenRouterObservabilityDestinationsListResponse(JSONObject json, OpenRouterObservabilityDestinationsListRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data[]} - the destinations as typed views, empty when
 * absent.
 */
    public List<OpenRouterObservabilityDestination> items() {
        List<OpenRouterObservabilityDestination> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterObservabilityDestination(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
    /**
 * JSON path: {@code total_count} - total number of destinations matching the
 * filters.
 */
    public Integer totalCount() {
        if (!json.has("total_count") || json.isNull("total_count")) {
            return null;
        }
        return (int) json.optLong("total_count");
    }
}
