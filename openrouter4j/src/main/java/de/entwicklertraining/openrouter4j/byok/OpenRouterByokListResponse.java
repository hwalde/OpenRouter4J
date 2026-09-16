package de.entwicklertraining.openrouter4j.byok;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Response of GET /byok: the BYOK provider credentials of the account.
 *
 * <p>Follows the swallow-and-return-empty convention.
 */
public final class OpenRouterByokListResponse extends OpenRouterResponse<OpenRouterByokListRequest> {


    OpenRouterByokListResponse(JSONObject json, OpenRouterByokListRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data[]} - the credentials as typed views, empty when absent.
 */
    public List<OpenRouterByokKey> items() {
        List<OpenRouterByokKey> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterByokKey(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
    /**
 * JSON path: {@code total_count} - total number of credentials matching the
 * filters.
 */
    public Integer totalCount() {
        if (!json.has("total_count") || json.isNull("total_count")) {
            return null;
        }
        return (int) json.optLong("total_count");
    }
}
