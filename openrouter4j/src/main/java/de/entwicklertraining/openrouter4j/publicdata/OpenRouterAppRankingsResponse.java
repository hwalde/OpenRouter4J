package de.entwicklertraining.openrouter4j.publicdata;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /datasets/app-rankings: the ranked apps and the request
 * metadata.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterAppRankingsResponse extends OpenRouterResponse<OpenRouterAppRankingsRequest> {

    OpenRouterAppRankingsResponse(JSONObject json, OpenRouterAppRankingsRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data[]} - the ranked apps.
     *
     * @return the rows as typed views, empty when absent
     */
    public List<OpenRouterAppRanking> items() {
        List<OpenRouterAppRanking> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterAppRanking(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * JSON path: {@code meta.as_of} - the data snapshot timestamp.
     *
     * @return the value, or {@code null} when absent
     */
    public String asOf() {
        return optMetaString("as_of");
    }

    /**
     * JSON path: {@code meta.version} - the dataset version (e.g. {@code v1}).
     *
     * @return the value, or {@code null} when absent
     */
    public String version() {
        return optMetaString("version");
    }

    /**
     * JSON path: {@code meta.start_date} - the resolved window start
     * (possibly clamped to the 2025-01-01 dataset floor).
     *
     * @return the value, or {@code null} when absent
     */
    public String startDate() {
        return optMetaString("start_date");
    }

    /**
     * JSON path: {@code meta.end_date} - the resolved window end.
     *
     * @return the value, or {@code null} when absent
     */
    public String endDate() {
        return optMetaString("end_date");
    }

    private String optMetaString(String key) {
        try {
            JSONObject meta = json.optJSONObject("meta");
            if (meta != null) {
                return meta.optString(key, null);
            }
        } catch (Exception ignored) {
            // swallow
        }
        return null;
    }
}
