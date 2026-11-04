package de.entwicklertraining.openrouter4j.vault;

import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * The response of the effective-secrets list
 * (GET /vault/interns/{internId}/effective-secrets, schema
 * {@code VaultEffectiveSecretListResponse}): one page of the secrets an
 * intern receives, one entry per name.
 *
 * <p>Ordered by name; page through the list by raising {@code offset} by
 * the number of returned entries while {@link #hasMore()} is {@code true}.
 * Every vault route, including this read, answers 404 outside the Intern
 * API programme.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention.
 *
 * @param <T> the request type this response belongs to
 */
public final class OpenRouterVaultEffectiveSecretsListResponse<T extends OpenRouterRequest<?>>
        extends OpenRouterResponse<T> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterVaultEffectiveSecretsListResponse(JSONObject json, T request) {
        super(json, request);
    }

    /**
     * JSON path: {@code data} - the effective secret metadata rows of this
     * page, one per name, ordered by name (at most 100 entries).
     *
     * @return the effective secrets of this page, empty when absent
     */
    public List<OpenRouterVaultEffectiveSecret> secrets() {
        List<OpenRouterVaultEffectiveSecret> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = data.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterVaultEffectiveSecret(row));
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
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean hasMore() {
        Object value = json.opt("has_more");
        return value instanceof Boolean flag ? flag : null;
    }
}
