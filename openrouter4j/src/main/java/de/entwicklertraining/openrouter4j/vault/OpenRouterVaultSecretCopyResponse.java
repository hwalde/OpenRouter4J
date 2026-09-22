package de.entwicklertraining.openrouter4j.vault;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of POST /vault/interns/{internId}/secrets/copy: the metadata for
 * the copies now stored in the intern scope, one entry per requested name
 * (schema {@code VaultSecretCopyResponse}).
 *
 * <p>The plaintext is never returned - only metadata. The copies carry their
 * own fingerprints (keyed with the intern vault's data key), so they differ
 * from the workspace originals' fingerprints even for identical values.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention.
 */
public final class OpenRouterVaultSecretCopyResponse
        extends OpenRouterResponse<OpenRouterVaultSecretCopyRequest> {

    OpenRouterVaultSecretCopyResponse(JSONObject json, OpenRouterVaultSecretCopyRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the copied secrets' metadata, one entry per
     * requested name.
     *
     * @return the copied secrets, empty when absent
     */
    public java.util.List<OpenRouterVaultSecret> secrets() {
        java.util.List<OpenRouterVaultSecret> result = new java.util.ArrayList<>();
        try {
            org.json.JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = data.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterVaultSecret(row));
                    }
                }
            }
        } catch (Exception e) {
            // swallow-and-return-empty convention
        }
        return result;
    }
}
