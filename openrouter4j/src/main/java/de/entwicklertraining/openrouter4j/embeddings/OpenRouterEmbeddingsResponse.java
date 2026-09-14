package de.entwicklertraining.openrouter4j.embeddings;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of POST /embeddings: the embedding vectors for the input(s), the
 * model that served the request and the token usage.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention: a field that is absent (or a malformed body) yields {@code null}
 * (or an empty list) instead of an exception. Use {@link #getJson()} to
 * inspect the raw response.
 */
public final class OpenRouterEmbeddingsResponse extends OpenRouterResponse<OpenRouterEmbeddingsRequest> {

    OpenRouterEmbeddingsResponse(JSONObject json, OpenRouterEmbeddingsRequest request) {
        super(json, request);
    }

    /**
     * @return every embedding of the response as {@link OpenRouterEmbedding}
     *         views, empty when {@code data} is absent or not an array
     */
    public List<OpenRouterEmbedding> embeddings() {
        List<OpenRouterEmbedding> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterEmbedding(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * Convenience lookup by input index (batch requests return one embedding
     * per input, matched by {@code index}).
     *
     * @param index the input index to find
     * @return the matching embedding, or {@code null} when no entry has that index
     */
    public OpenRouterEmbedding embedding(int index) {
        for (OpenRouterEmbedding embedding : embeddings()) {
            if (embedding.index() != null && embedding.index() == index) {
                return embedding;
            }
        }
        return null;
    }

    /**
     * JSON path: {@code id} - unique identifier for the embeddings response
     * (e.g. {@code embd-1234567890}).
     *
     * @return the value, or {@code null} when absent
     */
    public String id() {
        try {
            return json.optString("id", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code model} - the model that served the request (may
     * differ from the requested model when routing substituted an endpoint).
     *
     * @return the value, or {@code null} when absent
     */
    public String model() {
        try {
            return json.optString("model", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code object} - always {@code "list"} on a well-formed
     * response.
     *
     * @return the value, or {@code null} when absent
     */
    public String object() {
        try {
            return json.optString("object", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return the raw {@code usage} object, or {@code null} when absent
     */
    public JSONObject usage() {
        try {
            return json.optJSONObject("usage");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code usage.prompt_tokens} - tokens counted for the input.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long promptTokens() {
        try {
            JSONObject usage = usage();
            if (usage == null || !usage.has("prompt_tokens") || usage.isNull("prompt_tokens")) {
                return null;
            }
            return usage.optLong("prompt_tokens");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code usage.total_tokens} - total tokens used.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long totalTokens() {
        try {
            JSONObject usage = usage();
            if (usage == null || !usage.has("total_tokens") || usage.isNull("total_tokens")) {
                return null;
            }
            return usage.optLong("total_tokens");
        } catch (Exception e) {
            return null;
        }
    }
}
