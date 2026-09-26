package de.entwicklertraining.openrouter4j.batches;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of DELETE {@code /api/v1/batches/:id}: the batch id plus the
 * per-target deletion outcome. Deletion is only allowed once the batch is
 * terminal - an in-flight batch answers {@code 409}, and deletion is not
 * cancellation (there is no cancel operation). A {@code 200} is synchronous:
 * every applicable cleanup has completed, and a later GET or DELETE of the
 * same id answers {@code 404}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 */
public final class OpenRouterBatchDeleteResponse
        extends OpenRouterResponse<OpenRouterBatchDeleteRequest> {

    OpenRouterBatchDeleteResponse(JSONObject json, OpenRouterBatchDeleteRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code id} - the deleted batch id.
     *
     * @return the value, or {@code null} when absent
     */
    public String id() {
        return json.optString("id", null);
    }

    /**
     * JSON path: {@code deletion} - the OpenRouter and upstream cleanup
     * outcome.
     *
     * @return the deletion view, or {@code null} when absent
     */
    public OpenRouterBatchDeletion deletion() {
        JSONObject deletion = json.optJSONObject("deletion");
        return deletion == null ? null : new OpenRouterBatchDeletion(deletion);
    }
}
