package de.entwicklertraining.openrouter4j.batches;

import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsResponse;
import de.entwicklertraining.openrouter4j.messages.OpenRouterMessagesResponse;
import de.entwicklertraining.openrouter4j.responses.OpenRouterResponsesResponse;
import org.json.JSONObject;

/**
 * A typed view of one entry of the {@code results} array of a completed
 * batch: the answer to exactly one submitted
 * {@link OpenRouterBatchItem}, matched by {@code custom_id}.
 *
 * <p>Exactly one of {@code response} and {@code error} is populated per
 * item - {@link #hasResponse()} / {@link #hasError()} tell which. A
 * successful {@code response} carries the standard response body of the
 * batch's {@link OpenRouterBatchEndpoint}, reachable verbatim via
 * {@link #body()} or through the typed view of that endpoint shape
 * ({@link #chatCompletionResponse()}, {@link #messagesResponse()},
 * {@link #responsesResponse()}, {@link #embeddingsResponse()} - each
 * {@code null} when this result carries no body). The typed views are
 * constructed without a backing request ({@code getRequest()} yields
 * {@code null}), because a batch result is not the answer to a single
 * executed request object.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterBatchResult {

    private final JSONObject json;

    OpenRouterBatchResult(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw result row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code id} - the batch result id (e.g.
     * {@code batch_req_123}).
     *
     * @return the value, or {@code null} when absent
     */
    public String id() {
        return json.optString("id", null);
    }

    /**
     * JSON path: {@code custom_id} - the id the matching
     * {@link OpenRouterBatchItem} was submitted with.
     *
     * @return the value, or {@code null} when absent
     */
    public String customId() {
        return json.optString("custom_id", null);
    }

    /**
     * @return {@code true} when this item carries a successful
     *         {@code response} (and no {@code error})
     */
    public boolean hasResponse() {
        return response() != null;
    }

    /**
     * @return {@code true} when this item carries an {@code error} (and no
     *         {@code response})
     */
    public boolean hasError() {
        return error() != null;
    }

    /**
     * JSON path: {@code response.status_code} - the HTTP status the request
     * was answered with (200 on success).
     *
     * @return the value, or {@code null} when absent
     */
    public Integer statusCode() {
        JSONObject response = response();
        if (response == null || !response.has("status_code") || response.isNull("status_code")) {
            return null;
        }
        return response.optInt("status_code");
    }

    /**
     * JSON path: {@code response.request_id} - the id of the individual
     * upstream request.
     *
     * @return the value, or {@code null} when absent
     */
    public String requestId() {
        JSONObject response = response();
        return response == null ? null : response.optString("request_id", null);
    }

    /**
     * JSON path: {@code response.body} - the verbatim response body of the
     * batch endpoint shape (the generation id of a completed item is its
     * {@code body.id}, e.g. {@code gen-batch-...} - that is the id to use
     * for Report Feedback on a bad generation).
     *
     * @return the body, or {@code null} when absent
     */
    public JSONObject body() {
        JSONObject response = response();
        return response == null ? null : response.optJSONObject("body");
    }

    /**
     * JSON path: {@code error} - why the request failed. The successful
     * {@code response} is absent when this is set.
     *
     * @return the error object, or {@code null} when absent
     */
    public JSONObject error() {
        return json.optJSONObject("error");
    }

    /**
     * Reads {@link #body()} as a chat completions response - the typed view
     * of the batch endpoint {@code /v1/chat/completions}.
     *
     * @return the response, or {@code null} when this result carries no body
     */
    public OpenRouterChatCompletionResponse chatCompletionResponse() {
        JSONObject body = body();
        return body == null ? null : new OpenRouterChatCompletionResponse(body, null);
    }

    /**
     * Reads {@link #body()} as an Anthropic Messages response - the typed
     * view of the batch endpoint {@code /v1/messages}.
     *
     * @return the response, or {@code null} when this result carries no body
     */
    public OpenRouterMessagesResponse messagesResponse() {
        JSONObject body = body();
        return body == null ? null : new OpenRouterMessagesResponse(body, null);
    }

    /**
     * Reads {@link #body()} as a Responses API response - the typed view of
     * the batch endpoint {@code /v1/responses}.
     *
     * @return the response, or {@code null} when this result carries no body
     */
    public OpenRouterResponsesResponse responsesResponse() {
        JSONObject body = body();
        return body == null ? null : new OpenRouterResponsesResponse(body, null);
    }

    /**
     * Reads {@link #body()} as an embeddings response - the typed view of the
     * batch endpoint {@code /v1/embeddings}.
     *
     * @return the response, or {@code null} when this result carries no body
     */
    public OpenRouterEmbeddingsResponse embeddingsResponse() {
        JSONObject body = body();
        return body == null ? null : new OpenRouterEmbeddingsResponse(body, null);
    }

    private JSONObject response() {
        return json.optJSONObject("response");
    }
}
