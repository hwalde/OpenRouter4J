package de.entwicklertraining.openrouter4j.vault;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * The shared response of the vault secret list endpoints
 * (GET /vault/secrets and GET /vault/interns/{internId}/secrets): one page
 * of secret metadata for the selected scope (schema
 * {@code VaultSecretListResponse}).
 *
 * <p>The scope is selected by the API key's active workspace - there is no
 * workspace parameter and no fallback. Every vault route, including the
 * list, answers 404 outside the Intern API programme.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterVaultSecretsListResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterVaultSecretsListResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the secret metadata rows of this page,
     * sorted by name (at most 100 entries).
     *
     * @return the secrets of this page, empty when absent
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

    /**
     * JSON path: {@code has_more} - {@code true} when more secrets exist
     * beyond this page. Request the next page with {@code offset} increased
     * by the number of returned entries.
     *
     * @return the flag, or {@code null} when absent
     */
    public Boolean hasMore() {
        if (!json.has("has_more") || json.isNull("has_more")) {
            return null;
        }
        return json.optBoolean("has_more");
    }
}
