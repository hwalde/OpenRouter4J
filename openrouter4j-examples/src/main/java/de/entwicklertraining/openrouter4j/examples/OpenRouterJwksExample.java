package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.oauth.OpenRouterJwk;

/**
 * Demonstrates the JWKS discovery endpoint (GET /oauth/jwks): the RFC 7517
 * JWK Set with the public keys OpenRouter signs access tokens with. These
 * are the keys that verify the short-lived access token the
 * workload-identity federation ({@code client.exchangeWorkloadIdentityToken()})
 * hands out - pick the entry whose {@code kid} matches the token header and
 * feed modulus/exponent to a standard JWS verification library.
 */
public class OpenRouterJwksExample {

    public static void main(String[] args) throws Exception {
        OpenRouterClient client = new OpenRouterClient();

        var response = client.oauthJwks().execute();

        System.out.println("signing keys: " + response.keys().size());
        for (OpenRouterJwk jwk : response.keys()) {
            System.out.println("  kid=" + jwk.keyId()
                    + " kty=" + jwk.keyType()
                    + " alg=" + jwk.algorithm()
                    + " use=" + jwk.use());
        }
    }
}
