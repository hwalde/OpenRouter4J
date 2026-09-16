package de.entwicklertraining.openrouter4j.activity;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /analytics/meta: the metrics, dimensions, filter operators
 * and granularities the analytics query engine accepts
 * (see {@link OpenRouterAnalyticsQueryRequest}).
 *
 * <p>Follows the swallow-and-return-empty convention.
 */
public final class OpenRouterAnalyticsMetaResponse extends OpenRouterResponse<OpenRouterAnalyticsMetaRequest> {

    OpenRouterAnalyticsMetaResponse(JSONObject json, OpenRouterAnalyticsMetaRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data.metrics[]} - the metrics the query engine
     * accepts for {@code metrics(...)}.
     *
     * @return the metrics as typed views, empty when absent
     */
    public List<OpenRouterAnalyticsMetric> metrics() {
        return list("metrics", OpenRouterAnalyticsMetric::new);
    }

    /**
     * JSON path: {@code data.dimensions[]} - the dimensions the query engine
     * accepts for {@code dimensions(...)} and {@code filter(field, ...)}.
     * Trap: filters on enriched dimensions use the underlying id, not the
     * display label.
     *
     * @return the dimensions as typed views, empty when absent
     */
    public List<OpenRouterAnalyticsDimension> dimensions() {
        return list("dimensions", OpenRouterAnalyticsDimension::new);
    }

    /**
     * JSON path: {@code data.operators[]} - the filter operators accepted by
     * {@code filter(field, operator, value)}.
     *
     * @return the operators as typed views, empty when absent
     */
    public List<OpenRouterAnalyticsOperator> operators() {
        return list("operators", OpenRouterAnalyticsOperator::new);
    }

    /**
     * JSON path: {@code data.granularities[]} - the granularities accepted by
     * {@code granularity(...)}.
     *
     * @return the granularities as typed views, empty when absent
     */
    public List<OpenRouterAnalyticsGranularity> granularities() {
        return list("granularities", OpenRouterAnalyticsGranularity::new);
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
}
