package de.entwicklertraining.openrouter4j.oauth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Helper for the PKCE (Proof Key for Code Exchange, RFC 7636) values used by
 * the OpenRouter OAuth authorization-code flow:
 *
 * <pre>{@code
 * String verifier = OpenRouterPkce.generateCodeVerifier();
 * String challenge = OpenRouterPkce.codeChallengeS256(verifier); // S256
 * }</pre>
 *
 * <p>The verifier must be kept secret until the exchange; the challenge
 * travels in {@code POST /auth/keys/code} as {@code code_challenge} with
 * {@code code_challenge_method: "S256"}, and the verifier in
 * {@code POST /auth/keys} as {@code code_verifier}. All methods use the
 * base64url alphabet without padding, as RFC 7636 requires.
 */
public final class OpenRouterPkce {

    private OpenRouterPkce() {
        // static helper only
    }

    /**
     * Generates a fresh code verifier: 32 bytes from {@link SecureRandom},
     * base64url-encoded without padding, which yields the RFC 7636 minimum
     * length of 43 characters from the unreserved alphabet
     * {@code [A-Za-z0-9-._~]}.
     *
     * @return a new random code verifier (43 characters)
     */
    public static String generateCodeVerifier() {
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    /**
     * Derives the {@code S256} code challenge from a verifier:
     * {@code BASE64URL(SHA256(ASCII(verifier)))}.
     *
     * @param verifier the code verifier (43-128 characters, RFC 7636 alphabet)
     * @return the S256 code challenge
     * @throws IllegalArgumentException when the verifier is too short, too
     *         long or contains characters outside the RFC 7636 alphabet
     */
    public static String codeChallengeS256(String verifier) {
        validateVerifier(verifier);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated for every Java platform; this cannot happen
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static void validateVerifier(String verifier) {
        if (verifier == null || verifier.length() < 43 || verifier.length() > 128) {
            throw new IllegalArgumentException(
                    "a PKCE code verifier must be 43-128 characters long, got "
                            + (verifier == null ? "null" : verifier.length()));
        }
        for (char c : verifier.toCharArray()) {
            boolean unreserved = (c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '-' || c == '.' || c == '_' || c == '~';
            if (!unreserved) {
                throw new IllegalArgumentException(
                        "a PKCE code verifier may only contain [A-Za-z0-9-._~], found: " + c);
            }
        }
    }
}
