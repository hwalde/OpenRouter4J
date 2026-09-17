package de.entwicklertraining.openrouter4j.oauth;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /oauth/jwks: an RFC 7517 JWK Set with the public keys
 * OpenRouter signs access tokens with. This is the counterpart needed to
 * verify the access tokens the OAuth exchange endpoints hand out - notably
 * the workload-identity federation ({@code client.exchangeWorkloadIdentityToken()}),
 * which returns a short-lived access token whose signature can only be
 * checked against these keys (pick the entry whose {@code kid} matches the
 * token header, then verify with the standard JWS libraries).
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null} /
 * empty convention: a malformed body yields an empty key list instead of an
 * exception. Use {@link #getJson()} to inspect the raw response.
 */
public final class OpenRouterJwksResponse extends OpenRouterResponse<OpenRouterJwksRequest> {

    OpenRouterJwksResponse(JSONObject json, OpenRouterJwksRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code keys} - the JWK entries of the set.
     *
     * @return the keys, empty when absent or malformed
     */
    public List<OpenRouterJwk> keys() {
        try {
            JSONArray array = json.optJSONArray("keys");
            List<OpenRouterJwk> result = new ArrayList<>();
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject entry = array.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterJwk(entry));
                    }
                }
            }
            return result;
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Convenience: the JWK entry whose {@code kid} matches the given key id -
     * the lookup a JWT verifier performs with the token's {@code kid} header.
     *
     * @param keyId the key id to find
     * @return the matching entry, or {@code null} when there is none
     */
    public OpenRouterJwk key(String keyId) {
        if (keyId == null) {
            return null;
        }
        for (OpenRouterJwk jwk : keys()) {
            if (keyId.equals(jwk.keyId())) {
                return jwk;
            }
        }
        return null;
    }
}
