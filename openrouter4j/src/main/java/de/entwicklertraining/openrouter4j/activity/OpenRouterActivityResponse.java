package de.entwicklertraining.openrouter4j.activity;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /activity: the usage rows of the account.
 *
 * <p>Follows the swallow-and-return-empty convention.
 */
public final class OpenRouterActivityResponse extends OpenRouterResponse<OpenRouterActivityRequest> {

    OpenRouterActivityResponse(JSONObject json, OpenRouterActivityRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data[]} - the usage rows.
     *
     * @return the rows as typed views, empty when absent
     */
    public List<OpenRouterActivityItem> items() {
        List<OpenRouterActivityItem> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterActivityItem(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
}
