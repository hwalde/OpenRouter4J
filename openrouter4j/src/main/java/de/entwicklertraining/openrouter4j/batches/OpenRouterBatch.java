package de.entwicklertraining.openrouter4j.batches;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A typed view of a batch object (submit answer, list item or poll answer):
 * the endpoint shape, model, lifecycle status, per-request counts, usage and
 * - once completed - the inline {@code results}.
 *
 * <p>The lifecycle normally progresses
 * {@code validating} → {@code in_progress} → {@code finalizing} →
 * {@code completed}; the other statuses are {@code failed}, {@code expired},
 * {@code cancelling} and {@code cancelled}. {@link #isTerminal()} marks the
 * four statuses worth stopping a polling loop on. Submission success is not
 * request success: a {@code 202}/{@code validating} answer only means the
 * batch was persisted and queued for validation - per-request failures
 * surface later via {@code failed} and {@link #error()}.
 *
 * <p>List items are metadata-only and always carry {@code results}
 * {@code null}; retrieve the batch by id for its results. OpenRouter stores
 * batch inputs and results for 30 days.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterBatch {

    private final JSONObject json;

    OpenRouterBatch(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw batch object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code id} - the batch id (e.g. {@code batch_123}), the
     * path segment of GET/DELETE {@code /api/v1/batches/:id}.
     *
     * @return the value, or {@code null} when absent
     */
    public String id() {
        return json.optString("id", null);
    }

    /**
     * JSON path: {@code object} - always {@code batch}.
     *
     * @return the value, or {@code null} when absent
     */
    public String object() {
        return json.optString("object", null);
    }

    /**
     * JSON path: {@code endpoint} - the API shape every request of the batch
     * follows (e.g. {@code /v1/chat/completions}).
     *
     * @return the value, or {@code null} when absent
     */
    public String endpoint() {
        return json.optString("endpoint", null);
    }

    /**
     * JSON path: {@code model} - the batch-level model applied to every
     * request (a {@code :batch} endpoint variant is required at submit).
     *
     * @return the value, or {@code null} when absent
     */
    public String model() {
        return json.optString("model", null);
    }

    /**
     * JSON path: {@code completion_window} - always {@code 24h} (the only
     * accepted value).
     *
     * @return the value, or {@code null} when absent
     */
    public String completionWindow() {
        return json.optString("completion_window", null);
    }

    /**
     * JSON path: {@code status} - the lifecycle status: {@code validating},
     * {@code in_progress}, {@code finalizing}, {@code completed},
     * {@code failed}, {@code expired}, {@code cancelling} or {@code
     * cancelled}.
     *
     * @return the value, or {@code null} when absent
     */
    public String status() {
        return json.optString("status", null);
    }

    /**
     * @return {@code true} when {@link #status()} is one of the terminal
     *         statuses {@code completed}, {@code failed}, {@code expired} or
     *         {@code cancelled} - the point at which polling can stop
     */
    public boolean isTerminal() {
        String status = status();
        return "completed".equals(status)
                || "failed".equals(status)
                || "expired".equals(status)
                || "cancelled".equals(status);
    }

    /**
     * JSON path: {@code created_at} - the creation time in whole Unix
     * seconds (creation-time filtering keeps subsecond precision, so a
     * batch created within the same displayed second can still match a
     * {@code created_after} filter at its own {@code created_at}).
     *
     * @return the value, or {@code null} when absent
     */
    public Long createdAt() {
        if (!json.has("created_at") || json.isNull("created_at")) {
            return null;
        }
        return json.optLong("created_at");
    }

    /**
     * JSON path: {@code finalized_at} - the time the batch reached its
     * terminal status, in whole Unix seconds.
     *
     * @return the value, or {@code null} when absent
     */
    public Long finalizedAt() {
        if (!json.has("finalized_at") || json.isNull("finalized_at")) {
            return null;
        }
        return json.optLong("finalized_at");
    }

    /**
     * JSON path: {@code request_counts} - the total, completed and failed
     * request counts of the batch.
     *
     * @return the counts view, or {@code null} when absent
     */
    public OpenRouterBatchRequestCounts requestCounts() {
        JSONObject counts = json.optJSONObject("request_counts");
        return counts == null ? null : new OpenRouterBatchRequestCounts(counts);
    }

    /**
     * JSON path: {@code usage} - the token usage and cost of the batch;
     * populated once the batch has completed (every documented example shows
     * it {@code null} before that).
     *
     * @return the usage view, or {@code null} when absent
     */
    public OpenRouterBatchUsage usage() {
        JSONObject usage = json.optJSONObject("usage");
        return usage == null ? null : new OpenRouterBatchUsage(usage);
    }

    /**
     * JSON path: {@code results} - the per-request answers, inline once the
     * batch has completed ({@code null} before that and on every non-success
     * terminal status). List items always carry {@code null} here - poll
     * {@code GET /api/v1/batches/:id} for the results.
     *
     * @return the results, empty when absent
     */
    public List<OpenRouterBatchResult> results() {
        List<OpenRouterBatchResult> result = new ArrayList<>();
        JSONArray results = json.optJSONArray("results");
        if (results == null) {
            return result;
        }
        for (int i = 0; i < results.length(); i++) {
            JSONObject row = results.optJSONObject(i);
            if (row != null) {
                result.add(new OpenRouterBatchResult(row));
            }
        }
        return result;
    }

    /**
     * Finds the result of one submitted request by its {@code custom_id}.
     *
     * @param customId the id the {@link OpenRouterBatchItem} was submitted
     *                 with
     * @return the result, or {@code null} when absent
     */
    public OpenRouterBatchResult result(String customId) {
        if (customId == null) {
            return null;
        }
        JSONArray results = json.optJSONArray("results");
        if (results == null) {
            return null;
        }
        for (int i = 0; i < results.length(); i++) {
            JSONObject row = results.optJSONObject(i);
            if (row != null && customId.equals(row.optString("custom_id", null))) {
                return new OpenRouterBatchResult(row);
            }
        }
        return null;
    }

    /**
     * JSON path: {@code error} - why the batch failed or was rejected after
     * the {@code 202} (per-request problems move the whole batch to
     * {@code failed} and explain the rejection here).
     *
     * @return the error object, or {@code null} when absent
     */
    public JSONObject error() {
        return json.optJSONObject("error");
    }
}
