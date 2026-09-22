package de.entwicklertraining.openrouter4j.vault;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The shared response of the vault secret store endpoints
 * (PUT /vault/secrets/{name} and PUT /vault/interns/{internId}/secrets/{name}):
 * the metadata of the stored secret (schema {@code VaultSecretResponse},
 * wrapped in {@code data}).
 *
 * <p><b>Metadata only:</b> the plaintext value is encrypted at rest and never
 * returned - it cannot be retrieved after this call. The fingerprint can be
 * used to confirm that a rewrite stored the same value (comparable only
 * within one vault).
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterVaultSecretMutationResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterVaultSecretMutationResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the stored secret's metadata.
     *
     * @return the secret view, or {@code null} when the {@code data} object
     *         is absent
     */
    public OpenRouterVaultSecret secret() {
        JSONObject data = json.optJSONObject("data");
        return data == null ? null : new OpenRouterVaultSecret(data);
    }
}
