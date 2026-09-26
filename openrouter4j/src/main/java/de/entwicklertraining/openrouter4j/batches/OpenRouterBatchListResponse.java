package de.entwicklertraining.openrouter4j.batches;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET {@code /api/v1/batches}: one page of the workspace's
 * batches, newest first. List items are metadata-only and always set
 * {@code results} to {@code null} - poll a batch by id when you need its
 * results. Batches are scoped to the workspace, not the key.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention.
 */
public final class OpenRouterBatchListResponse
        extends OpenRouterResponse<OpenRouterBatchListRequest> {

    OpenRouterBatchListResponse(JSONObject json, OpenRouterBatchListRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the batches of this page.
     *
     * @return the batches, empty when absent
     */
    public List<OpenRouterBatch> batches() {
        List<OpenRouterBatch> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = data.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterBatch(row));
                    }
                }
            }
        } catch (Exception e) {
            // swallow-and-return-empty convention
        }
        return result;
    }

    /**
     * JSON path: {@code first_id} - the id of the first batch of the page.
     *
     * @return the value, or {@code null} when absent
     */
    public String firstId() {
        return json.optString("first_id", null);
    }

    /**
     * JSON path: {@code last_id} - the id of the last batch of the page and
     * the cursor for the {@code after} parameter of the next page.
     *
     * @return the value, or {@code null} when absent
     */
    public String lastId() {
        return json.optString("last_id", null);
    }

    /**
     * JSON path: {@code has_more} - {@code true} when more batches match
     * than this page returned; request the next page with
     * {@code after =} {@link #lastId()}. Pagination does not use offsets.
     *
     * @return the flag, or {@code null} when absent
     */
    public Boolean hasMore() {
        if (!json.has("has_more") || json.isNull("has_more")) {
            return null;
        }
        return json.optBoolean("has_more");
    }
}
