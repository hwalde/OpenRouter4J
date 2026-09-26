package de.entwicklertraining.openrouter4j.batches;

import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsRequest;
import de.entwicklertraining.openrouter4j.messages.OpenRouterMessagesRequest;
import de.entwicklertraining.openrouter4j.responses.OpenRouterResponsesRequest;
import org.json.JSONObject;

import java.util.Objects;

/**
 * One item of the {@code requests} array of
 * {@code POST https://openrouter.ai/api/v1/batches}: {@code custom_id} plus
 * the request {@code body} in the shape of the batch's
 * {@link OpenRouterBatchEndpoint}.
 *
 * <p>{@code custom_id} must be unique within the batch and is how the
 * completed {@link OpenRouterBatchResult} items map back to their inputs. The
 * {@code body} is sent verbatim: it may omit {@code model} to inherit the
 * batch-level model, and it must not set a different one (the submission is
 * rejected otherwise - {@link OpenRouterBatchSubmitRequest.Builder#build()}
 * checks this loudly).
 *
 * <p>Per-request restrictions the Batch API enforces after the {@code 202}
 * (they are documented on
 * <a href="https://openrouter.ai/docs/batch-quickstart">the Batch API page</a>
 * and are NOT checked by this class): multimodal input must be URL-only
 * (base64 and {@code data:} URIs are rejected everywhere), audio and video
 * input parts are rejected, {@code stream: true} and {@code speed} are
 * rejected, and OpenRouter-orchestrated web search is unavailable ({@code
 * :online} variants are rejected with 422, the {@code web} plugin with
 * 422/400).
 */
public final class OpenRouterBatchItem {

    private final String customId;
    private final JSONObject body;

    private OpenRouterBatchItem(String customId, JSONObject body) {
        this.customId = customId;
        this.body = body;
    }

    /**
     * Creates an item whose body is sent verbatim.
     *
     * @param customId the caller-assigned id, unique within the batch
     * @param body the request body in the shape of the batch endpoint
     * @return the item
     */
    public static OpenRouterBatchItem of(String customId, JSONObject body) {
        if (customId == null || customId.isBlank()) {
            throw new IllegalArgumentException("custom_id must not be blank");
        }
        if (body == null) {
            throw new IllegalArgumentException("body must not be null");
        }
        return new OpenRouterBatchItem(customId, body);
    }

    /**
     * Creates an item from an existing chat completions request; the request
     * body is reused verbatim via {@code getBody()}.
     *
     * @param customId the caller-assigned id, unique within the batch
     * @param request the chat completions request to take the body from
     * @return the item
     */
    public static OpenRouterBatchItem of(String customId, OpenRouterChatCompletionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("chat completions request must not be null");
        }
        return of(customId, bodyFrom(request.getBody(), "chat completions request"));
    }

    /**
     * Creates an item from an existing Anthropic Messages request; the
     * request body is reused verbatim via {@code getBody()}.
     *
     * @param customId the caller-assigned id, unique within the batch
     * @param request the messages request to take the body from
     * @return the item
     */
    public static OpenRouterBatchItem of(String customId, OpenRouterMessagesRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("messages request must not be null");
        }
        return of(customId, bodyFrom(request.getBody(), "messages request"));
    }

    /**
     * Creates an item from an existing Responses request; the request body is
     * reused verbatim via {@code getBody()}.
     *
     * @param customId the caller-assigned id, unique within the batch
     * @param request the responses request to take the body from
     * @return the item
     */
    public static OpenRouterBatchItem of(String customId, OpenRouterResponsesRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("responses request must not be null");
        }
        return of(customId, bodyFrom(request.getBody(), "responses request"));
    }

    /**
     * Creates an item from an existing embeddings request; the request body
     * is reused verbatim via {@code getBody()}.
     *
     * @param customId the caller-assigned id, unique within the batch
     * @param request the embeddings request to take the body from
     * @return the item
     */
    public static OpenRouterBatchItem of(String customId, OpenRouterEmbeddingsRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("embeddings request must not be null");
        }
        return of(customId, bodyFrom(request.getBody(), "embeddings request"));
    }

    private static JSONObject bodyFrom(String json, String what) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException(what + " carries no body");
        }
        return new JSONObject(json);
    }

    /**
     * @return the caller-assigned id, unique within the batch
     */
    public String customId() {
        return customId;
    }

    /**
     * @return the request body, sent verbatim
     */
    public JSONObject body() {
        return body;
    }

    /**
     * @return the wire form {@code {"custom_id": ..., "body": ...}}
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("custom_id", customId);
        json.put("body", body);
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpenRouterBatchItem other)) {
            return false;
        }
        return customId.equals(other.customId) && body.similar(other.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customId);
    }
}
