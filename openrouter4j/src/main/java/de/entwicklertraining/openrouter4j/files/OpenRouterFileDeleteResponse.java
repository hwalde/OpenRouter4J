package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of DELETE /files/{file_id}: the deletion confirmation.
 *
 * <p>Tolerance trap: the response shape depends on the negotiated storage -
 * the OpenRouter shape answers {@code type: "file_deleted"} while the
 * OpenAI shape answers {@code deleted: true}.
 * {@link #isDeleted()} treats either as deleted.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterFileDeleteResponse
        extends OpenRouterResponse<OpenRouterFileDeleteRequest> {

    OpenRouterFileDeleteResponse(JSONObject json, OpenRouterFileDeleteRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code id} - the id of the deleted file.
     *
     * @return the value, or {@code null} when absent
     */
    public String id() {
        return json.optString("id", null);
    }

    /**
     * JSON path: {@code type} - the confirmation discriminator
     * ({@code "file_deleted"} on the OpenRouter shape).
     *
     * @return the value, or {@code null} when absent
     */
    public String type() {
        return json.optString("type", null);
    }

    /**
     * JSON path: {@code type} / {@code deleted} - whether the file was
     * deleted. The OpenRouter shape answers {@code type: "file_deleted"},
     * the OpenAI shape answers {@code deleted: true}; either is treated as
     * deleted.
     *
     * @return {@code true} when either shape confirms the deletion,
     *         {@code false} when the OpenAI {@code deleted} field is present
     *         and false, {@code null} when neither field confirms anything
     */
    public Boolean isDeleted() {
        if ("file_deleted".equals(json.optString("type", null))) {
            return Boolean.TRUE;
        }
        if (!json.has("deleted") || json.isNull("deleted")) {
            return null;
        }
        return json.optBoolean("deleted");
    }
}
