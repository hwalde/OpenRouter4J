package de.entwicklertraining.openrouter4j.embeddings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

/**
 * A typed item of the item-object form of the {@code input} union on an
 * embeddings request (POST /embeddings): {@code {"content":[...]}}, where the
 * content parts are the multimodal union of
 * {@link OpenRouterEmbeddingsContentPart}. This is the form for multimodal
 * embedding input - one item per input to embed, mixing text and media parts.
 *
 * <p>Create instances through the typed factories ({@link #text(String)},
 * {@link #imageUrl(String)}, {@link #of(OpenRouterEmbeddingsContentPart...)})
 * or the verbatim escape hatch {@link #raw(JSONObject)} for item shapes this
 * library does not type. Append them via
 * {@code client.embeddings().addInput(...)}; for a completely untyped path use
 * {@code addInputItem(JSONObject)} on the builder.
 *
 * <p>Trap: multimodal parts are accepted only by embedding models that
 * declare the matching input modality (image/audio) - a text-only embedding
 * model answers HTTP 400. Discover support via {@code client.embeddingsModels()}
 * / {@code OpenRouterModel.inputModalities()}.
 *
 * @see <a href="https://openrouter.ai/docs/api/api-reference/embeddings/submit-an-embedding-request">Embeddings API reference</a>
 */
public final class OpenRouterEmbeddingsInputItem {

    private final JSONObject json;

    private OpenRouterEmbeddingsInputItem(JSONObject json) {
        this.json = json;
    }

    /**
     * An item with a single {@code text} part:
     * {@code {"content":[{"type":"text","text":...}]}}.
     *
     * @param text the text to embed (must not be null or empty)
     * @return the input item
     */
    public static OpenRouterEmbeddingsInputItem text(String text) {
        return of(OpenRouterEmbeddingsContentPart.text(text));
    }

    /**
     * An item with a single {@code image_url} part:
     * {@code {"content":[{"type":"image_url","image_url":{"url":...}}]}}.
     * See {@link OpenRouterEmbeddingsContentPart#imageUrl(String)} for the
     * modality trap.
     *
     * @param url the image URL
     * @return the input item
     */
    public static OpenRouterEmbeddingsInputItem imageUrl(String url) {
        return of(OpenRouterEmbeddingsContentPart.imageUrl(url));
    }

    /**
     * An item composed of the given content parts (at least one, in order):
     * e.g. an image plus the text describing it.
     *
     * @param parts the content parts of this item
     * @return the input item
     */
    public static OpenRouterEmbeddingsInputItem of(OpenRouterEmbeddingsContentPart... parts) {
        Objects.requireNonNull(parts, "parts must not be null");
        if (parts.length == 0) {
            throw new IllegalArgumentException("an input item requires at least one content part");
        }
        JSONArray content = new JSONArray();
        for (OpenRouterEmbeddingsContentPart part : parts) {
            Objects.requireNonNull(part, "content part must not be null");
            content.put(part.toJson());
        }
        return item(content);
    }

    /**
     * List-based variant of {@link #of(OpenRouterEmbeddingsContentPart...)}.
     *
     * @param parts the content parts of this item (at least one, in order)
     * @return the input item
     */
    public static OpenRouterEmbeddingsInputItem of(List<OpenRouterEmbeddingsContentPart> parts) {
        Objects.requireNonNull(parts, "parts must not be null");
        if (parts.isEmpty()) {
            throw new IllegalArgumentException("an input item requires at least one content part");
        }
        JSONArray content = new JSONArray();
        for (OpenRouterEmbeddingsContentPart part : parts) {
            Objects.requireNonNull(part, "content part must not be null");
            content.put(part.toJson());
        }
        return item(content);
    }

    /**
     * The verbatim escape hatch for item shapes this library does not type
     * (items carrying {@code input_audio} / {@code input_video} /
     * {@code input_file} parts, provider-specific extensions). The item must
     * carry a {@code content} field; everything else is passed through
     * unchanged. For a completely untyped path use
     * {@code addInputItem(JSONObject)} on the builder directly.
     *
     * @param item the raw input item (must contain {@code content})
     * @return the input item
     */
    public static OpenRouterEmbeddingsInputItem raw(JSONObject item) {
        Objects.requireNonNull(item, "item must not be null");
        if (!item.has("content")) {
            throw new IllegalArgumentException("a raw embeddings input item requires a content field");
        }
        return new OpenRouterEmbeddingsInputItem(new JSONObject(item.toString()));
    }

    /**
     * @return a copy of the item as a JSON object for the {@code input}
     *         array (the copy pattern of {@code OpenRouterStopCondition} -
     *         the value class stays immutable)
     */
    public JSONObject toJson() {
        return new JSONObject(json.toString());
    }

    private static OpenRouterEmbeddingsInputItem item(JSONArray content) {
        JSONObject json = new JSONObject();
        json.put("content", content);
        return new OpenRouterEmbeddingsInputItem(json);
    }
}
