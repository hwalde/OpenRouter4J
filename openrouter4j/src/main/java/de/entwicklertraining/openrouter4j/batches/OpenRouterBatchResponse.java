package de.entwicklertraining.openrouter4j.batches;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The shared response of the batch submit and poll endpoints
 * (POST {@code /api/v1/batches}, GET {@code /api/v1/batches/:id}): the batch
 * object itself. Submit answers {@code 202 Accepted} with
 * {@code status: "validating"} - that means the batch was persisted and
 * queued for validation, not that its requests succeeded.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterBatchResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterBatchResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * @return the batch view, or {@code null} when the body carries no
     *         {@code id} (a malformed body)
     */
    public OpenRouterBatch batch() {
        if (json == null || !json.has("id")) {
            return null;
        }
        return new OpenRouterBatch(json);
    }
}
