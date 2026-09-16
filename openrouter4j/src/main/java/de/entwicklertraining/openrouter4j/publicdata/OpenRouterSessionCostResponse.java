package de.entwicklertraining.openrouter4j.publicdata;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /datasets/session-cost: the cost-per-session cells and the
 * request metadata.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterSessionCostResponse extends OpenRouterResponse<OpenRouterSessionCostRequest> {

    OpenRouterSessionCostResponse(JSONObject json, OpenRouterSessionCostRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data[]} - the cost-per-session cells.
     *
     * @return the rows as typed views, empty when absent
     */
    public List<OpenRouterSessionCostRow> items() {
        List<OpenRouterSessionCostRow> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterSessionCostRow(entry));
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
     * JSON path: {@code meta.window_days} - the source snapshot window in days.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long windowDays() {
        try {
            JSONObject meta = json.optJSONObject("meta");
            if (meta != null && meta.has("window_days") && !meta.isNull("window_days")) {
                return meta.optLong("window_days");
            }
        } catch (Exception ignored) {
            // swallow
        }
        return null;
    }

    /**
     * JSON path: {@code meta.window_end_date} - the end of the source
     * snapshot window.
     *
     * @return the value, or {@code null} when absent
     */
    public String windowEndDate() {
        return optMetaString("window_end_date");
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
