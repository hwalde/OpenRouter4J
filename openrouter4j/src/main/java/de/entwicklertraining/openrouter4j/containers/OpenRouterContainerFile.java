package de.entwicklertraining.openrouter4j.containers;

import org.json.JSONObject;

/**
 * A typed view of one file entry of a code-execution container (the
 * {@code ContainerFile} schema): the {@code data[]} items of
 * GET /containers/{container_id}/files, the body of
 * GET /containers/{container_id}/files/{file_id} and the root of the
 * list endpoint's response shape.
 *
 * <p>A container file id has the shape {@code cfile_} + base64url of the
 * file path; the container id is the canonical id exactly as returned in a
 * bash/shell server-tool result (e.g. {@code sess_abc123}; a restarted
 * session carries its own {@code -r<nonce>}-suffixed id).
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}
 * convention. Use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterContainerFile {

    private final JSONObject json;

    OpenRouterContainerFile(JSONObject json) {
        this.json = json;
    }

    /**
     * @return a defensive copy of the raw {@code ContainerFile} JSON behind this view
     */
    public JSONObject json() {
        return new JSONObject(json.toString());
    }

    /**
     * JSON path: {@code id} - the container file id
     * ({@code cfile_} + base64url of the file path).
     *
     * @return the value, or {@code null} when absent
     */
    public String id() {
        return json.optString("id", null);
    }

    /**
     * JSON path: {@code object} - always {@code "container.file"} for a
     * container file.
     *
     * @return the value, or {@code null} when absent
     */
    public String objectType() {
        return json.optString("object", null);
    }

    /**
     * JSON path: {@code container_id} - echoes the container id of the
     * request path.
     *
     * @return the value, or {@code null} when absent
     */
    public String containerId() {
        return json.optString("container_id", null);
    }

    /**
     * JSON path: {@code bytes} - the file size in bytes.
     *
     * @return the value, or {@code null} when absent
     */
    public Long bytes() {
        if (!json.has("bytes") || json.isNull("bytes")) {
            return null;
        }
        return json.optLong("bytes");
    }

    /**
     * JSON path: {@code created_at} - Unix timestamp (seconds) when the file
     * was last synced into the container.
     *
     * @return the value, or {@code null} when absent
     */
    public Long createdAt() {
        if (!json.has("created_at") || json.isNull("created_at")) {
            return null;
        }
        return json.optLong("created_at");
    }

    /**
     * JSON path: {@code path} - the container-relative file path
     * (e.g. {@code /home/oai/share/out.csv}).
     *
     * @return the value, or {@code null} when absent
     */
    public String path() {
        return json.optString("path", null);
    }

    /**
     * JSON path: {@code source} - always {@code "assistant"} for files the
     * code execution wrote.
     *
     * @return the value, or {@code null} when absent
     */
    public String source() {
        return json.optString("source", null);
    }
}
