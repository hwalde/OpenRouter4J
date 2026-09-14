package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.oauth.OpenRouterAuthorizationCodeExchangeResponse;
import de.entwicklertraining.openrouter4j.oauth.OpenRouterCreateAuthorizationCodeResponse;
import de.entwicklertraining.openrouter4j.oauth.OpenRouterPkce;

/**
 * Demonstrates the OAuth 2.0 authorization-code flow with PKCE that turns a
 * user consent into an OpenRouter API key: POST /auth/keys/code (create the
 * code) followed by POST /auth/keys (exchange code + PKCE verifier for the
 * key).
 *
 * <p>The example keeps the flow honest about its interactive step: after
 * creating the code, the user must open the consent URL, approve, and the
 * authorization code arrives (pasted or via the callback). Run it with the
 * received code as the first argument; without an argument it only creates
 * the code and prints what to do next. The verifier must be generated once
 * and kept for the exchange.
 */
public class OpenRouterOAuthExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // One PKCE pair per flow: challenge travels with the code creation,
        // the verifier is the secret for the exchange and must never be logged.
        String verifier = OpenRouterPkce.generateCodeVerifier();
        String challenge = OpenRouterPkce.codeChallengeS256(verifier);

        OpenRouterCreateAuthorizationCodeResponse codeResponse = client.createAuthorizationCode()
                .callbackUrl("http://localhost:9876/callback")
                .codeChallenge(challenge)
                .codeChallengeMethod("S256")
                .keyLabel("OpenRouter4J OAuth example")
                .limit(5.0)
                .usageLimitType("monthly")
                .execute();

        System.out.println("Authorization code id: " + codeResponse.id());
        System.out.println("Created at:            " + codeResponse.createdAt());
        System.out.println("Open the consent page for this code, then re-run this program");
        System.out.println("with the received authorization code as the first argument.");

        if (args.length == 0 || args[0].isBlank()) {
            System.out.println("(no authorization code given - stopping before the exchange)");
            return;
        }

        OpenRouterAuthorizationCodeExchangeResponse exchange = client.exchangeAuthorizationCode()
                .code(args[0])
                .codeVerifier(verifier)
                .codeChallengeMethod("S256")
                .execute();

        System.out.println("API key created for user: " + exchange.userId());
        System.out.println("(the key itself is intentionally not printed - treat it as a secret)");
    }
}
