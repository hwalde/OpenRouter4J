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
 * generates the verifier, stores it in a file in the system temporary
 * directory ({@code java.io.tmpdir}) and tells you the path; the second run
 * (first argument: the received authorization code) reads the stored
 * verifier back and performs the exchange, then deletes the file. Be honest
 * with yourself about the verifier file: it is written with the default OS
 * permissions into a directory other local users can read, so this example
 * is only safe on a single-user machine; adapt it with restrictive file
 * permissions and a private directory before using it for real. The
 * verifier is a secret - never log it or commit it.
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
            System.out.println("Phase 1 requires OPENROUTER_API_KEY to be set (HTTP 401 otherwise).");
            System.out.println("How the user authorizes this code is not documented by OpenRouter;");
            System.out.println("the documented alternative consent flow is");
            System.out.println("https://openrouter.ai/auth?callback_url=<callback>&code_challenge=<challenge>");
            System.out.println("&code_challenge_method=S256 and does not require this call (there is");
            System.out.println("also a headless variant without callback_url that displays the code");
            System.out.println("on screen). Either way, the code arrives as ?code=<code> on the");
            System.out.println("callback redirect - the page itself will fail to load, copy the");
            System.out.println("code from the address bar.");
            System.out.println("The code expires 10 minutes after issuance - exchange it promptly.");
            System.out.println("Then re-run this program with that code as the first argument.");
            return;
        }

        // Phase 2: exchange the received code for the API key, using the
        // stored verifier that pairs with the challenge of phase 1.
        String verifier = Files.readString(VERIFIER_FILE, StandardCharsets.UTF_8).trim();

        OpenRouterAuthorizationCodeExchangeResponse exchange;
        try {
            exchange = client.exchangeAuthorizationCode()
                    .code(args[0])
                    .codeVerifier(verifier)
                    .codeChallengeMethod("S256")
                    .execute();
        } finally {
            Files.deleteIfExists(VERIFIER_FILE);
        }

        System.out.println("API key created for user: " + exchange.userId());
        System.out.println("(the key itself is intentionally not printed - treat it as a secret)");
        System.out.println("(the stored verifier file was deleted)");
    }
}
