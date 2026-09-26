package de.entwicklertraining.openrouter4j.batches;

import org.json.JSONObject;

/**
 * A typed view of the {@code request_counts} object of a batch: how many
 * requests the batch holds and how many have finished.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterBatchRequestCounts {

    private final JSONObject json;

    OpenRouterBatchRequestCounts(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw {@code request_counts} object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code total} - the number of requests in the batch.
     *
     * @return the value, or {@code null} when absent
     */
    public Integer total() {
        return optInteger("total");
    }

    /**
     * JSON path: {@code completed} - the number of requests that finished
     * successfully.
     *
     * @return the value, or {@code null} when absent
     */
    public Integer completed() {
        return optInteger("completed");
    }

    /**
     * JSON path: {@code failed} - the number of requests that failed.
     *
     * @return the value, or {@code null} when absent
     */
    public Integer failed() {
        return optInteger("failed");
    }

    private Integer optInteger(String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        return json.optInt(key);
    }
}
