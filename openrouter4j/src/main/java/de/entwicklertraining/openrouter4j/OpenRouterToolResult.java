package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

/**
 * Encapsulates the output from a tool invocation.
 * <p>
 * Two shapes are supported, mirroring the API's {@code ChatToolMessage.content}
 * {@code anyOf}:
 * <ul>
 *   <li><b>Legacy single-object form</b> - a {@link JSONObject} payload, emitted
 *       by the tool-call loops as the plain string content of the
 *       {@code role:"tool"} message (default, unchanged wire format);</li>
 *   <li><b>Multi-part form (since 1.14.0)</b> - a list of content parts (at
 *       least text and image parts are typed via {@link #textPart(String)} /
 *       {@link #imageUrlPart(String)}), emitted as a {@code content} array so a
 *       tool can feed images or documents back to a multimodal model.</li>
 * </ul>
 * Create with {@link #of(JSONObject)} for the legacy form or
 * {@link #ofParts(List)} for the multi-part form. The type stays a record with
 * the single component {@code content}, so record patterns such as
 * {@code r instanceof OpenRouterToolResult(JSONObject c)} keep working for both
 * forms.
 *
 * @param content the legacy tool output; for a multi-part result an
 *                informational view {@code {"content_parts":[...]}} of the parts
 */
public record OpenRouterToolResult(JSONObject content) {

    /**
     * Creates a tool result in the legacy single-object form (unchanged
     * behaviour and wire format).
     *
     * @param content the tool output as a JSON object
     * @return the tool result
     */
    public static OpenRouterToolResult of(JSONObject content) {
        return new OpenRouterToolResult(content);
    }

    /**
     * Creates a multi-part tool result whose {@code role:"tool"} message emits
     * {@code content} as an array of content parts - for example to hand a
     * screenshot or a document page back to a vision-capable model. At least
     * text and image parts are typed ({@link #textPart(String)},
     * {@link #imageUrlPart(String)}); arbitrary other parts can be passed as
     * verbatim JSON objects (e.g. {@code file} / {@code input_audio} /
     * {@code video_url}, the same shapes as on a user message).
     * <p>
     * Only results created here are emitted as an array: a hand-built
     * {@code new OpenRouterToolResult(new JSONObject().put("content_parts", ...))}
     * is an ordinary legacy result and is sent as a plain string.
     *
     * @param parts the content parts of the tool result (must not be empty)
     * @return the tool result
     */
    public static OpenRouterToolResult ofParts(List<JSONObject> parts) {
        Objects.requireNonNull(parts, "parts must not be null");
        if (parts.isEmpty()) {
            throw new IllegalArgumentException("parts must not be empty");
        }
        return new OpenRouterToolResult(new ContentParts(List.copyOf(parts)));
    }

    /**
     * Varargs variant of {@link #ofParts(List)}.
     *
     * @param parts the content parts of the tool result (must not be empty)
     * @return the tool result
     */
    public static OpenRouterToolResult ofParts(JSONObject... parts) {
        Objects.requireNonNull(parts, "parts must not be null");
        return ofParts(List.of(parts));
    }

    /**
     * Builds a typed <b>text</b> content part ({@code {"type":"text","text":...}})
     * for use with {@link #ofParts(List)}.
     *
     * @param text the text of the part
     * @return the content part object
     */
    public static JSONObject textPart(String text) {
        return new JSONObject().put("type", "text").put("text", text);
    }

    /**
     * Builds a typed <b>image</b> content part ({@code {"type":"image_url",
     * "image_url":{"url":...}}}) for use with {@link #ofParts(List)}; the URL
     * may be an external URL or a base64 data URL.
     *
     * @param url the image URL or base64 data URL
     * @return the content part object
     */
    public static JSONObject imageUrlPart(String url) {
        return imageUrlPart(url, null);
    }

    /**
     * Builds a typed <b>image</b> content part with an explicit resolution tier
     * ({@code detail}) - the tool-result counterpart of
     * {@code OpenRouterChatCompletionRequest.Builder#addImageByUrl(String, OpenRouterImageDetail)}.
     *
     * @param url the image URL or base64 data URL
     * @param detail the resolution tier, or {@code null} to leave it unset
     * @return the content part object
     */
    public static JSONObject imageUrlPart(String url, OpenRouterImageDetail detail) {
        JSONObject imageUrl = new JSONObject().put("url", url);
        if (detail != null) {
            imageUrl.put("detail", detail.wireName());
        }
        return new JSONObject().put("type", "image_url").put("image_url", imageUrl);
    }

    /**
     * Returns the content parts of the multi-part form (an unmodifiable list),
     * or {@code null} when this result is in the legacy single-object form.
     *
     * @return the content parts, or {@code null} for legacy results
     */
    public List<JSONObject> contentParts() {
        return content instanceof ContentParts parts ? parts.parts : null;
    }

    /**
     * Returns {@code true} when this result was created via
     * {@link #ofParts(List)} and the tool-call loops emit {@code content} as an
     * array.
     *
     * @return whether the multi-part form is set
     */
    public boolean hasContentParts() {
        return content instanceof ContentParts;
    }

    /**
     * Marker payload of the multi-part form. Keeping the parts inside the one
     * record component (instead of adding a second component) is what keeps
     * this type source- and binary-compatible with the 1.x record: the
     * canonical constructor, the component list and record patterns are
     * unchanged. The JSON content is an informational view only; the parts
     * list is the source of truth.
     */
    private static final class ContentParts extends JSONObject {

        private final List<JSONObject> parts;

        private ContentParts(List<JSONObject> parts) {
            super();
            this.parts = parts;
            put("content_parts", new JSONArray(parts));
        }

        @Override
        public boolean equals(Object o) {
            return this == o || (o instanceof ContentParts that && parts.equals(that.parts));
        }

        @Override
        public int hashCode() {
            return parts.hashCode();
        }
    }
}
