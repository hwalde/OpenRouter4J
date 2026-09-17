package de.entwicklertraining.openrouter4j.oauth;

import org.json.JSONObject;

import java.util.Objects;

/**
 * One JWK (RFC 7517 JSON Web Key) entry of the JWK Set returned by
 * GET /oauth/jwks: the public material of one key OpenRouter signs access
 * tokens with. For RSA keys the modulus/exponent pair ({@code n} / {@code e},
 * both base64url) is what a standard JWS verification library consumes.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}
 * convention. Use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterJwk {

    private final JSONObject json;

    OpenRouterJwk(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw JWK object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code kty} - the key type (e.g. {@code RSA}).
     *
     * @return the value, or {@code null} when absent
     */
    public String keyType() {
        return json.optString("kty", null);
    }

    /**
     * JSON path: {@code kid} - the key id a JWT {@code kid} header refers to.
     *
     * @return the value, or {@code null} when absent
     */
    public String keyId() {
        return json.optString("kid", null);
    }

    /**
     * JSON path: {@code alg} - the intended signing algorithm
     * (e.g. {@code RS256}).
     *
     * @return the value, or {@code null} when absent
     */
    public String algorithm() {
        return json.optString("alg", null);
    }

    /**
     * JSON path: {@code use} - the intended use of the key
     * (e.g. {@code sig}).
     *
     * @return the value, or {@code null} when absent
     */
    public String use() {
        return json.optString("use", null);
    }

    /**
     * JSON path: {@code n} - the RSA modulus, base64url-encoded.
     *
     * @return the value, or {@code null} when absent
     */
    public String modulusBase64Url() {
        return json.optString("n", null);
    }

    /**
     * JSON path: {@code e} - the RSA public exponent, base64url-encoded
     * (typically {@code AQAB}, i.e. 65537).
     *
     * @return the value, or {@code null} when absent
     */
    public String exponentBase64Url() {
        return json.optString("e", null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpenRouterJwk)) {
            return false;
        }
        return json.toString().equals(((OpenRouterJwk) o).json.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(json.toString());
    }

    @Override
    public String toString() {
        return json.toString();
    }
}
