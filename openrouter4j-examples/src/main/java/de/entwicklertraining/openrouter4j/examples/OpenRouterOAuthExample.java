package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.oauth.OpenRouterAuthorizationCodeExchangeResponse;
import de.entwicklertraining.openrouter4j.oauth.OpenRouterCreateAuthorizationCodeResponse;
import de.entwicklertraining.openrouter4j.oauth.OpenRouterPkce;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Demonstrates the OAuth 2.0 authorization-code flow with PKCE that turns a
 * user consent into an OpenRouter API key: POST /auth/keys/code (create the
 * code) followed by POST /auth/keys (exchange code + PKCE verifier for the
 * key).
 *
 * <p>The flow is interactive, so the example runs in two phases and keeps
 * the PKCE verifier consistent across both: the first run (no arguments)
 * generates the verifier, stores it in a file next to this program's working
 * directory, creates the code and prints what to do next; the second run
 * (first argument: the received authorization code) reads the stored
 * verifier back and performs the exchange. The verifier is a secret - it is
 * stored with owner-only permissions expectations on a local disk and must
 * never be logged or committed.
 */
public class OpenRouterOAuthExample {

    private static final Path VERIFIER_FILE = Path.of(System.getProperty("java.io.tmpdir"), "openrouter4j-oauth-example.verifier");

    public static void main(String[] args) throws Exception {
        OpenRouterClient client = new OpenRouterClient();

        if (args.length == 0 || args[0].isBlank()) {
            // Phase 1: create the authorization code and persist the verifier
            // for the exchange in phase 2.
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

            Files.writeString(VERIFIER_FILE, verifier, StandardCharsets.UTF_8);
            System.out.println("Authorization code id: " + codeResponse.id());
            System.out.println("Created at:            " + codeResponse.createdAt());
            System.out.println("PKCE verifier stored in " + VERIFIER_FILE + " (keep it secret).");
            System.out.println("Open the consent page for this code, then re-run this program");
            System.out.println("with the received authorization code as the first argument.");
            return;
        }

        // Phase 2: exchange the received code for the API key, using the
        // stored verifier that pairs with the challenge of phase 1.
        String verifier = Files.readString(VERIFIER_FILE, StandardCharsets.UTF_8).trim();

        OpenRouterAuthorizationCodeExchangeResponse exchange = client.exchangeAuthorizationCode()
                .code(args[0])
                .codeVerifier(verifier)
                .codeChallengeMethod("S256")
                .execute();

        System.out.println("API key created for user: " + exchange.userId());
        System.out.println("(the key itself is intentionally not printed - treat it as a secret)");
    }
}
