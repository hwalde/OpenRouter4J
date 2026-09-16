package de.entwicklertraining.openrouter4j.publicdata;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /benchmarks: the unified benchmark rows and the request
 * metadata.
 *
 * <p>The rows are heterogeneous - their shape depends on the requested
 * {@code source} - so {@link #items()} returns raw JSON objects; use
 * {@link #asOf()} and the other {@code meta} accessors for the typed
 * request-level view. Follows the swallow-and-return-null convention.
 */
public final class OpenRouterBenchmarksResponse extends OpenRouterResponse<OpenRouterBenchmarksRequest> {

    OpenRouterBenchmarksResponse(JSONObject json, OpenRouterBenchmarksRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data[]} - the benchmark rows. The row shape depends
     * on the requested source: Artificial Analysis rows carry index scores
     * ({@code intelligence_index}, {@code coding_index}, {@code agentic_index},
     * {@code pricing}), OpenRouter rows carry accuracy data
     * ({@code benchmark_type}, {@code accuracy}, {@code avg_cost_per_task},
     * {@code total_tasks}).
     *
     * @return the rows as raw JSON objects, empty when absent
     */
    public List<JSONObject> items() {
        List<JSONObject> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(entry);
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
     * JSON path: {@code meta.source} - the resolved source filter.
     *
     * @return the value, or {@code null} when absent
     */
    public String source() {
        return optMetaString("source");
    }

    /**
     * JSON path: {@code meta.task_type} - the resolved task-type filter.
     *
     * @return the value, or {@code null} when absent
     */
    public String taskType() {
        return optMetaString("task_type");
    }

    /**
     * JSON path: {@code meta.citation}.
     *
     * @return the value, or {@code null} when absent
     */
    public String citation() {
        return optMetaString("citation");
    }

    /**
     * JSON path: {@code meta.source_url}.
     *
     * @return the value, or {@code null} when absent
     */
    public String sourceUrl() {
        return optMetaString("source_url");
    }

    /**
     * JSON path: {@code meta.model_count} - the number of models in the result.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long modelCount() {
        try {
            JSONObject meta = json.optJSONObject("meta");
            if (meta != null && meta.has("model_count") && !meta.isNull("model_count")) {
                return meta.optLong("model_count");
            }
        } catch (Exception ignored) {
            // swallow
        }
        return null;
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
