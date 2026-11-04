package de.entwicklertraining.openrouter4j.embeddings;

import org.json.JSONObject;

import java.util.Objects;

/**
 * A typed content part of an embeddings input item's {@code content} array
 * (POST /embeddings, the item-object form of the {@code input} union). The
 * published schema declares five part kinds on that union: {@code text}
 * ({@link #text(String)}), {@code image_url} ({@link #imageUrl(String)}) and
 * the three multimodal media forms {@code input_audio} / {@code input_video} /
 * {@code input_file} (a {@code {"type":...,"input_audio":{"data":...,
 * "format":...}}} object each - reach those through {@link #raw(JSONObject)}
 * or build the whole item with
 * {@code addInputItem(JSONObject)} on the builder).
 *
 * <p>The wire shapes here are deliberately standalone: the chat surface's
 * content parts are built inline on its builder and its {@code image_url}
 * part carries an extra {@code detail} field, so there is no shared value
 * type with a matching shape to reuse.
 *
 * <p>Create instances through the typed factories or the verbatim escape
 * hatch {@link #raw(JSONObject)}; append them to an item with
 * {@link OpenRouterEmbeddingsInputItem#of(OpenRouterEmbeddingsContentPart...)}.
 *
 * @see <a href="https://openrouter.ai/docs/api/api-reference/embeddings/submit-an-embedding-request">Embeddings API reference</a>
 */
public final class OpenRouterEmbeddingsContentPart {

    private final JSONObject json;

    private OpenRouterEmbeddingsContentPart(JSONObject json) {
        this.json = json;
    }

    /**
     * A {@code text} part: {@code {"type":"text","text":...}}.
     *
     * @param text the text to embed (must not be null or empty per the schema)
     * @return the content part
     */
    public static OpenRouterEmbeddingsContentPart text(String text) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("text must not be null or empty");
        }
        JSONObject json = new JSONObject();
        json.put("type", "text");
        json.put("text", text);
        return new OpenRouterEmbeddingsContentPart(json);
    }

    /**
     * An {@code image_url} part: {@code {"type":"image_url",
     * "image_url":{"url":...}}}. Trap: only embedding models that declare
     * image input accept it - a text-only embedding model answers HTTP 400.
     * Discover support via {@code client.embeddingsModels()} /
     * {@code OpenRouterModel.inputModalities()}.
     *
     * @param url the image URL
     * @return the content part
     */
    public static OpenRouterEmbeddingsContentPart imageUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("imageUrl must not be null or empty");
        }
        JSONObject json = new JSONObject();
        json.put("type", "image_url");
        json.put("image_url", new JSONObject().put("url", url));
        return new OpenRouterEmbeddingsContentPart(json);
    }

    /**
     * The verbatim escape hatch for part forms this library does not type
     * ({@code input_audio} / {@code input_video} / {@code input_file} with
     * their {@code MultimodalMedia} objects, future part kinds). The part
     * must carry a {@code type} field; everything else is passed through
     * unchanged. For a completely untyped path use
     * {@code addInputItem(JSONObject)} on the builder directly.
     *
     * @param part the raw content part (must contain {@code type})
     * @return the content part
     */
    public static OpenRouterEmbeddingsContentPart raw(JSONObject part) {
        Objects.requireNonNull(part, "part must not be null");
        if (part.optString("type", null) == null) {
            throw new IllegalArgumentException("a raw content part requires a type field");
        }
        return new OpenRouterEmbeddingsContentPart(new JSONObject(part.toString()));
    }

    /**
     * @return a copy of the part as a JSON object for the {@code content}
     *         array (the copy pattern of {@code OpenRouterStopCondition} -
     *         the value class stays immutable)
     */
    public JSONObject toJson() {
        return new JSONObject(json.toString());
    }
}
