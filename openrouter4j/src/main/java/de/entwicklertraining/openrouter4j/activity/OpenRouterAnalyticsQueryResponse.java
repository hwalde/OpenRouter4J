package de.entwicklertraining.openrouter4j.activity;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of POST /analytics/query: the result rows of the query plus query
 * metadata.
 *
 * <p>The rows are free-form objects whose keys follow the requested metrics
 * and dimensions (e.g. {@code {"date__day": "...", "request_count": 1500}}),
 * so they are exposed as raw {@link JSONObject}s. All accessors follow the
 * swallow-and-return-{@code null}/empty convention.
 */
public final class OpenRouterAnalyticsQueryResponse extends OpenRouterResponse<OpenRouterAnalyticsQueryRequest> {

    OpenRouterAnalyticsQueryResponse(JSONObject json, OpenRouterAnalyticsQueryRequest request) {
        super(json, request);
    }

    /**
     * @return the raw {@code data} object of the response, or {@code null} when absent
     */
    public JSONObject data() {
        try {
            return json.optJSONObject("data");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code data.data[]} - the result rows of the query.
     *
     * @return the rows as raw objects (keys follow the requested metrics and
     *         dimensions), empty when absent
     */
    public List<JSONObject> rows() {
        List<JSONObject> result = new ArrayList<>();
        try {
            JSONObject data = data();
            JSONArray rows = data != null ? data.optJSONArray("data") : null;
            if (rows != null) {
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.optJSONObject(i);
                    if (row != null) {
                        result.add(row);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    private JSONObject metadata() {
        JSONObject data = data();
        return data != null ? data.optJSONObject("metadata") : null;
    }

    /**
     * JSON path: {@code data.metadata.query_time_ms} - server-side query time.
     *
     * @return the value, or {@code null} when absent
     */
    public Double queryTimeMs() {
        JSONObject metadata = metadata();
        if (metadata == null || !metadata.has("query_time_ms") || metadata.isNull("query_time_ms")) {
            return null;
        }
        return metadata.optDouble("query_time_ms");
    }

    /**
     * JSON path: {@code data.metadata.row_count} - number of rows returned.
     *
     * @return the value, or {@code null} when absent
     */
    public Long rowCount() {
        JSONObject metadata = metadata();
        if (metadata == null || !metadata.has("row_count") || metadata.isNull("row_count")) {
            return null;
        }
        return metadata.optLong("row_count");
    }

    /**
     * JSON path: {@code data.metadata.truncated} - whether the result set was
     * truncated by a row limit.
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean truncated() {
        JSONObject metadata = metadata();
        if (metadata == null || !metadata.has("truncated") || metadata.isNull("truncated")) {
            return null;
        }
        return metadata.optBoolean("truncated");
    }

    /**
     * JSON path: {@code data.cachedAt} - unix timestamp of when the result
     * was cached, when served from cache.
     *
     * @return the value, or {@code null} when absent
     */
    public Double cachedAt() {
        JSONObject data = data();
        if (data == null || !data.has("cachedAt") || data.isNull("cachedAt")) {
            return null;
        }
        return data.optDouble("cachedAt");
    }

    /**
     * JSON path: {@code data.warnings[]} - warnings about filter resolution
     * issues; the query still ran normally.
     *
     * @return the warnings, empty when absent
     */
    public List<String> warnings() {
        List<String> result = new ArrayList<>();
        try {
            JSONObject data = data();
            JSONArray warnings = data != null ? data.optJSONArray("warnings") : null;
            if (warnings != null) {
                for (int i = 0; i < warnings.length(); i++) {
                    String warning = warnings.optString(i, null);
                    if (warning != null) {
                        result.add(warning);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
}
