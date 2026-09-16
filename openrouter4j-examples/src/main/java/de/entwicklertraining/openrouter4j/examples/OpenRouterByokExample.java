package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.byok.OpenRouterByokCreateResponse;
import de.entwicklertraining.openrouter4j.byok.OpenRouterByokKey;
import de.entwicklertraining.openrouter4j.byok.OpenRouterByokListResponse;

import java.util.List;

/**
 * Demonstrates the BYOK (bring your own key) provider-credential endpoints.
 * All of them require a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * - a normal inference key is rejected with an authorization error.
 *
 * <p>Secrets rule: the provider credential set via {@code key(...)} is a
 * secret - it is encrypted at rest and never returned by any endpoint, so
 * this example never prints it.
 */
public class OpenRouterByokExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // 1. List the credentials of the account.
        OpenRouterByokListResponse list = client.byok().list().execute();
        List<OpenRouterByokKey> keys = list.items();
        System.out.println("BYOK credentials: " + keys.size());
        for (OpenRouterByokKey key : keys) {
            System.out.printf("  %s on %s (%s), sort order %s%n",
                    key.name(), key.provider(), key.label(), key.sortOrder());
        }

        // 2. Create a credential for one provider. The raw key is a secret:
        //    encrypted at rest, never returned, never logged.
        OpenRouterByokCreateResponse created = client.byok().create()
                .provider("deepseek")
                .key("sk-example-do-not-log")
                .name("Example DeepSeek Key")
                .allowedModels(List.of("deepseek/deepseek-v4-flash-0731"))
                .execute();
        String id = created.data() != null ? created.data().id() : null;
        System.out.println("Created credential " + id
                + " with masked label " + (created.data() != null ? created.data().label() : null));

        // 3. Read one credential by id (metadata only - the raw key never
        //    comes back).
        OpenRouterByokKey fetched = client.byok().get(id).execute().data();
        System.out.println("Fetched credential " + fetched.provider() + ", disabled: " + fetched.disabled());

        // 4. Rotate the credential in-place (the masked label regenerates).
        client.byok().update(id)
                .key("sk-example-rotated-do-not-log")
                .execute();
        System.out.println("Rotated credential " + id);

        // 5. Delete it (permanent).
        Boolean deleted = client.byok().delete(id).execute().deleted();
        System.out.println("Deleted: " + deleted);
    }
}
