package de.entwicklertraining.openrouter4j.files;

import org.json.JSONObject;

/**
 * A typed view of one file document of the OpenRouter Files API (the
 * {@code data} object of POST /files, GET /files/{file_id}, the
 * {@code data[]} items of GET /files, and the confirmation object of the
 * container promote flow).
 *
 * <p>The Files API negotiates the document shape per request through the
 * {@code _shape} discriminator field ({@code "openrouter"},
 * {@code "openai"} or {@code "anthropic"}). The OpenRouter shape is the
 * superset:
 * <ul>
 *   <li><b>OpenRouter shape</b> ({@code _shape: "openrouter"}): {@code id}
 *   (e.g. {@code or_file_...}), {@code type} ({@code "file"}),
 *   {@code filename}, {@code mime_type}, {@code size_bytes} (integer),
 *   {@code created_at} (ISO-8601 string), {@code downloadable} (boolean).
 *   Typed accessors: {@link #id()}, {@link #type()}, {@link #filename()},
 *   {@link #mimeType()}, {@link #sizeBytes()}, {@link #createdAt()},
 *   {@link #downloadable()}.</li>
 *   <li><b>OpenAI shape</b> ({@code _shape: "openai"}): {@code id},
 *   {@code object} ({@code "file"}), {@code bytes} (integer),
 *   {@code created_at} (unix seconds integer), {@code filename},
 *   {@code purpose}, {@code status} (e.g. {@code "processed"}). Typed
 *   accessors: {@link #id()}, {@link #objectType()},
 *   {@link #sizeBytesOpenAi()}, {@link #createdAtUnixSeconds()},
 *   {@link #filename()}, {@link #purpose()}, {@link #status()}.</li>
 *   <li><b>Anthropic shape</b> ({@code _shape: "anthropic"}): like the
 *   OpenRouter shape minus {@code downloadable} - {@code id},
 *   {@code filename}, {@code mime_type}, {@code size_bytes} (integer),
 *   {@code created_at} (ISO-8601 string). Typed accessors: {@link #id()},
 *   {@link #type()}, {@link #filename()}, {@link #mimeType()},
 *   {@link #sizeBytes()}, {@link #createdAt()}.</li>
 * </ul>
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}
 * convention; they return {@code null} when the field is absent or belongs
 * to a different shape. Use {@link #shape()} to find out which shape a
 * document carries and {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterFile {

    private final JSONObject json;

    /**
     * Creates a view over the given Files-API document JSON. The accessors
     * read the passed object (which shape it carries is decided by the
     * {@code _shape} field); the normal route to an instance is via the
     * response types ({@code file()} on the upload/get/list/promote
     * responses) - this constructor exists for callers that hold the raw
     * JSON from elsewhere.
     *
     * @param json the raw document JSON (not copied; accessors read it live)
     */
    public OpenRouterFile(JSONObject json) {
        this.json = json;
    }

    /**
     * @return a defensive copy of the raw document behind this view
     */
    public JSONObject json() {
        return new JSONObject(json.toString());
    }

    /**
     * JSON path: {@code _shape} - the shape discriminator of the document
     * ({@code "openrouter"}, {@code "openai"} or {@code "anthropic"}).
     *
     * @return the value, or {@code null} when absent
     */
    public String shape() {
        return json.optString("_shape", null);
    }

    /**
     * JSON path: {@code id} - the file id (e.g. {@code or_file_...} on the
     * OpenRouter shape, {@code file-...} on the OpenAI shape, {@code file_...}
     * on the Anthropic shape). Present in all three shapes.
     *
     * @return the value, or {@code null} when absent
     */
    public String id() {
        return json.optString("id", null);
    }

    /**
     * JSON path: {@code type} - the document type ({@code "file"} on the
     * OpenRouter and Anthropic shapes; not present on the OpenAI shape,
     * which carries {@code object} instead, see {@link #objectType()}).
     *
     * @return the value, or {@code null} when absent
     */
    public String type() {
        return json.optString("type", null);
    }

    /**
     * JSON path: {@code filename} - the file name. Present in all three shapes.
     *
     * @return the value, or {@code null} when absent
     */
    public String filename() {
        return json.optString("filename", null);
    }

    /**
     * JSON path: {@code mime_type} - the MIME type of the file (OpenRouter
     * and Anthropic shapes).
     *
     * @return the value, or {@code null} when absent
     */
    public String mimeType() {
        return json.optString("mime_type", null);
    }

    /**
     * JSON path: {@code size_bytes} - the file size in bytes (OpenRouter and
     * Anthropic shapes). For the OpenAI shape use
     * {@link #sizeBytesOpenAi()}.
     *
     * @return the value, or {@code null} when absent
     */
    public Long sizeBytes() {
        if (!json.has("size_bytes") || json.isNull("size_bytes")) {
            return null;
        }
        return json.optLong("size_bytes");
    }

    /**
     * JSON path: {@code bytes} - the file size in bytes (OpenAI shape only).
     * For the OpenRouter and Anthropic shapes use {@link #sizeBytes()}.
     *
     * @return the value, or {@code null} when absent
     */
    public Long sizeBytesOpenAi() {
        if (!json.has("bytes") || json.isNull("bytes")) {
            return null;
        }
        return json.optLong("bytes");
    }

    /**
     * JSON path: {@code created_at} in string form - the ISO-8601 creation
     * timestamp (OpenRouter and Anthropic shapes). For the OpenAI integer
     * form use {@link #createdAtUnixSeconds()}; returns {@code null} when
     * the value is not a string (e.g. the OpenAI unix-seconds number).
     *
     * @return the value, or {@code null} when absent or not a string
     */
    public String createdAt() {
        Object value = json.opt("created_at");
        return value instanceof String ? (String) value : null;
    }

    /**
     * JSON path: {@code created_at} in numeric form - unix seconds (OpenAI
     * shape). Returns {@code null} when the value is not a number (e.g. the
     * ISO-8601 string of the OpenRouter and Anthropic shapes, see
     * {@link #createdAt()}).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long createdAtUnixSeconds() {
        Object value = json.opt("created_at");
        return value instanceof Number ? ((Number) value).longValue() : null;
    }

    /**
     * JSON path: {@code downloadable} - whether the file content can be
     * downloaded via GET /files/{file_id}/content (OpenRouter shape only).
     *
     * @return the value, or {@code null} when absent
     */
    public Boolean downloadable() {
        if (!json.has("downloadable") || json.isNull("downloadable")) {
            return null;
        }
        return json.optBoolean("downloadable");
    }

    /**
     * JSON path: {@code object} - the OpenAI object type ({@code "file"}),
     * OpenAI shape only. The OpenRouter and Anthropic shapes carry
     * {@code type} instead, see {@link #type()}.
     *
     * @return the value, or {@code null} when absent
     */
    public String objectType() {
        return json.optString("object", null);
    }

    /**
     * JSON path: {@code purpose} - the upload purpose (e.g.
     * {@code "user_data"}), OpenAI shape only.
     *
     * @return the value, or {@code null} when absent
     */
    public String purpose() {
        return json.optString("purpose", null);
    }

    /**
     * JSON path: {@code status} - the processing status (e.g.
     * {@code "processed"}), OpenAI shape only.
     *
     * @return the value, or {@code null} when absent
     */
    public String status() {
        return json.optString("status", null);
    }
}
