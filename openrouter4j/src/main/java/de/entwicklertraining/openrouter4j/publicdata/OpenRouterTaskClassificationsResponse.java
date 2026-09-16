package de.entwicklertraining.openrouter4j.publicdata;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /classifications/task: the task-classification market
 * share over the requested window.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterTaskClassificationsResponse
        extends OpenRouterResponse<OpenRouterTaskClassificationsRequest> {

    OpenRouterTaskClassificationsResponse(JSONObject json, OpenRouterTaskClassificationsRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data.as_of} - the data snapshot date (YYYY-MM-DD).
     *
     * @return the value, or {@code null} when absent
     */
    public String asOf() {
        return optDataString("as_of");
    }

    /**
     * JSON path: {@code data.window_days} - the trailing window in days.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long windowDays() {
        try {
            JSONObject data = json.optJSONObject("data");
            if (data != null && data.has("window_days") && !data.isNull("window_days")) {
                return data.optLong("window_days");
            }
        } catch (Exception ignored) {
            // swallow
        }
        return null;
    }

    /**
     * JSON path: {@code data.classifications[]} - the classifications with
     * their shares and top models.
     *
     * @return the classifications as typed views, empty when absent
     */
    public List<OpenRouterTaskClassification> classifications() {
        return list("classifications", OpenRouterTaskClassification::new);
    }

    /**
     * JSON path: {@code data.macro_categories[]} - the macro categories
     * (Code, Data, Agent, General) with aggregate shares.
     *
     * @return the macro categories as typed views, empty when absent
     */
    public List<OpenRouterMacroCategory> macroCategories() {
        return list("macro_categories", OpenRouterMacroCategory::new);
    }

    private <T> List<T> list(String key, java.util.function.Function<JSONObject, T> factory) {
        List<T> result = new ArrayList<>();
        try {
            JSONObject data = json.optJSONObject("data");
            JSONArray array = data != null ? data.optJSONArray(key) : null;
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject entry = array.optJSONObject(i);
                    if (entry != null) {
                        result.add(factory.apply(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    private String optDataString(String key) {
        try {
            JSONObject data = json.optJSONObject("data");
            if (data != null) {
                return data.optString(key, null);
            }
        } catch (Exception ignored) {
            // swallow
        }
        return null;
    }
}
