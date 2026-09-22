package de.entwicklertraining.openrouter4j.interns;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Response of GET /interns: one page of the interns visible to the API key,
 * newest first.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention.
 */
public final class OpenRouterInternsListResponse
        extends OpenRouterResponse<OpenRouterInternsListRequest> {

    OpenRouterInternsListResponse(JSONObject json, OpenRouterInternsListRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the interns of this page.
     *
     * @return the interns, empty when absent
     */
    public java.util.List<OpenRouterIntern> interns() {
        java.util.List<OpenRouterIntern> result = new java.util.ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = data.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterIntern(row));
                    }
                }
            }
        } catch (Exception e) {
            // swallow-and-return-empty convention
        }
        return result;
    }

    /**
     * JSON path: {@code has_more} - {@code true} when more interns match the
     * filters than this page returned.
     *
     * @return the flag, or {@code null} when absent
     */
    public Boolean hasMore() {
        if (!json.has("has_more") || json.isNull("has_more")) {
            return null;
        }
        return json.optBoolean("has_more");
    }

    /**
     * JSON path: {@code next_cursor} - the opaque cursor to send as
     * {@code starting_after} for the next page, or {@code null} when there
     * is no next page.
     *
     * @return the cursor, or {@code null} when absent
     */
    public String nextCursor() {
        return json.optString("next_cursor", null);
    }
}
