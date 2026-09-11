package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.ArrayList;
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
 *       {@code role:"tool"} message exactly as before (default, unchanged
 *       wire format);</li>
 *   <li><b>Multi-part form (since 1.14.0)</b> - a list of content parts (at
 *       least text and image parts are typed via {@link #textPart(String)} /
 *       {@link #imageUrlPart(String)}), emitted as a {@code content} array so a
 *       tool can feed images or documents back to a multimodal model.</li>
 * </ul>
 * Create with {@link #of(JSONObject)} for the legacy form or
 * {@link #ofParts(List)} for the multi-part form.
 */
public final class OpenRouterToolResult {

    private final JSONObject content;
    private final List<JSONObject> contentParts;

    /**
     * Creates a tool result in the legacy single-object form (unchanged
     * behaviour and wire format).
     *
     * @param content the tool output as a JSON object
     */
    public OpenRouterToolResult(JSONObject content) {
        this(content, null);
    }

    private OpenRouterToolResult(JSONObject content, List<JSONObject> contentParts) {
        this.content = content;
        this.contentParts = contentParts;
    }

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
     *
     * @param parts the content parts of the tool result
     * @return the tool result
     */
    public static OpenRouterToolResult ofParts(List<JSONObject> parts) {
        Objects.requireNonNull(parts, "parts must not be null");
        if (parts.isEmpty()) {
            throw new IllegalArgumentException("parts must not be empty");
        }
        return new OpenRouterToolResult(null, List.copyOf(parts));
    }

    /**
     * Varargs variant of {@link #ofParts(List)}.
     *
     * @param parts the content parts of the tool result
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
     * Returns the legacy single-object payload, or {@code null} when this
     * result was created in the multi-part form ({@link #ofParts(List)}).
     *
     * @return the tool output as a JSON object, or {@code null} for multi-part results
     */
    public JSONObject content() {
        return content;
    }

    /**
     * Returns the content parts of the multi-part form, or {@code null} when
     * this result was created in the legacy single-object form.
     *
     * @return the content parts, or {@code null} for legacy results
     */
    public List<JSONObject> contentParts() {
        return contentParts;
    }

    /**
     * Returns {@code true} when this result carries multi-part content and the
     * tool-call loops must emit {@code content} as an array.
     *
     * @return whether the multi-part form is set
     */
    public boolean hasContentParts() {
        return contentParts != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OpenRouterToolResult that)) return false;
        return Objects.equals(content, that.content)
                && Objects.equals(contentParts, that.contentParts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, contentParts);
    }

    @Override
    public String toString() {
        return "OpenRouterToolResult[content=" + content + ", contentParts=" + contentParts + "]";
    }
}
