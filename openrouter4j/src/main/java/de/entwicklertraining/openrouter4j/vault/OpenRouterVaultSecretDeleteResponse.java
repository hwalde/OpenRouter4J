package de.entwicklertraining.openrouter4j.vault;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of the vault secret delete endpoints
 * (DELETE /vault/secrets/{name} and DELETE /vault/interns/{internId}/secrets/{name}):
 * the API answers 204 with no body on success and 404 when the name is
 * unknown.
 *
 * <p>Follows the swallow-and-return-null convention: an empty body yields an
 * empty JSON, so {@link #deleted()} is {@code null} on the documented 204
 * answer. Reaching this response at all means the deletion succeeded - an
 * unknown name raises the 404 exception before this is constructed.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterVaultSecretDeleteResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    OpenRouterVaultSecretDeleteResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code deleted} - a confirmation flag, should the API ever
     * send one; {@code null} on the documented empty 204 answer.
     *
     * @return the flag, or {@code null} when absent
     */
    public Boolean deleted() {
        if (!json.has("deleted") || json.isNull("deleted")) {
            return null;
        }
        return json.optBoolean("deleted");
    }
}
