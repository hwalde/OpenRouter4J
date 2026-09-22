package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultSecret;
import java.util.List;

/**
 * Demonstrates the vault surface: host-bound secrets for the active
 * workspace and for interns.
 *
 * <p>Traps: responses are metadata only - the plaintext value is never
 * returned after the store call, so treat it as a write-only secret (the
 * request {@code toString()} never contains it either). The fingerprint is
 * comparable only within one vault. Every requested name in a copy must
 * exist in the workspace scope - otherwise the API answers 404 and nothing
 * is copied. Every vault route, including the list, answers 404 outside the
 * Intern API programme.
 */
public class OpenRouterVaultExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // Store (create or replace) a workspace secret bound to exact
        // hostnames. Matching at release time is exact: a secret bound to
        // api.example.com is never released to example.com.
        OpenRouterVaultSecret stored = client.vault().store("github_token")
                .value("ghp_exampleTokenValue")
                .hosts("api.github.com")
                .execute()
                .secret();
        System.out.println("Stored secret: " + (stored == null ? null : stored.name())
                + ", fingerprint " + (stored == null ? null : stored.fingerprint()));

        // List the workspace secret metadata (sorted by name, paginated).
        for (OpenRouterVaultSecret secret : client.vault().list().limit(100).execute().secrets()) {
            System.out.println("Secret " + secret.name()
                    + " -> hosts " + secret.hosts()
                    + ", created " + secret.createdAt());
        }

        // Copy workspace secrets into an intern's scope: every name must
        // exist, otherwise 404 and nothing is copied. The copies carry their
        // own fingerprints (keyed with the intern vault's data key), so a
        // later rotation of the workspace secret does not propagate.
        if (stored != null) {
            List<OpenRouterVaultSecret> copies = client.vault()
                    .copySecretsToIntern("550e8400-e29b-41d4-a716-446655440000")
                    .names("github_token")
                    .execute()
                    .secrets();
            System.out.println("Copied into intern scope: " + copies.size() + " secret(s)");

            // Intern secrets list, store and delete work the same way,
            // scoped to the intern.
            for (OpenRouterVaultSecret secret
                    : client.vault().internSecrets("550e8400-e29b-41d4-a716-446655440000")
                            .execute().secrets()) {
                System.out.println("Intern secret " + secret.name()
                        + ", fingerprint " + secret.fingerprint());
            }

            client.vault().deleteInternSecret("550e8400-e29b-41d4-a716-446655440000",
                    "github_token").execute();
        }

        // Delete the workspace secret (permanent; unknown names answer 404).
        client.vault().delete("github_token").execute();
    }
}
