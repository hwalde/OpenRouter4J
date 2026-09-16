package de.entwicklertraining.openrouter4j.keys;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /keys: the API keys of the account.
 *
 * <p>Follows the swallow-and-return-empty convention.
 */
public final class OpenRouterKeysListResponse extends OpenRouterResponse<OpenRouterKeysListRequest> {

    OpenRouterKeysListResponse(JSONObject json, OpenRouterKeysListRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data[]} - the API keys.
     *
     * @return the keys as typed views, empty when absent
     */
    public List<OpenRouterApiKey> items() {
        List<OpenRouterApiKey> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterApiKey(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
}
