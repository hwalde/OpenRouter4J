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
        //    encrypted at rest, never returned, never logged. declaredZdr is
        //    the credential-side counterpart of request-side zdr(true) routing:
        //    true declares the upstream account zero-data-retention so ZDR-
        //    routed requests may use this credential; false keeps them away.
        //    declaredRegion declares the data region the upstream account
        //    processes requests in ("global"/"europe"/"us"): "europe" or "us"
        //    makes the credential eligible for requests to eu.openrouter.ai /
        //    us.openrouter.ai for that provider (Azure accepts both, Anthropic
        //    "us" only; for OpenAI/Fireworks the value must match the region in
        //    the key material).
        OpenRouterByokCreateResponse created = client.byok().create()
                .provider("deepseek")
                .key("sk-example-do-not-log")
                .name("Example DeepSeek Key")
                .allowedModels(List.of("deepseek/deepseek-v4-flash-0731"))
                .declaredZdr(true)
                .declaredRegion("europe")
                .execute();
        String id = created.data() != null ? created.data().id() : null;
        System.out.println("Created credential " + id
                + " with masked label " + (created.data() != null ? created.data().label() : null)
                + ", declared ZDR: " + (created.data() != null ? created.data().declaredZdr() : null)
                + ", declared region: " + (created.data() != null ? created.data().declaredRegion() : null));

        // 3. Read one credential by id (metadata only - the raw key never
        //    comes back).
        OpenRouterByokKey fetched = client.byok().get(id).execute().data();
        System.out.println("Fetched credential " + fetched.provider() + ", disabled: " + fetched.disabled());

        // 4. Rotate the credential in-place (the masked label regenerates)
        //    and revise the ZDR self-declaration. declaredRegion(null) clears
        //    the region declaration back to undeclared ("the credential
        //    follows the region OpenRouter records for the endpoint").
        client.byok().update(id)
                .key("sk-example-rotated-do-not-log")
                .declaredZdr(false)
                .declaredRegion(null)
                .execute();
        System.out.println("Rotated credential " + id);

        // 5. Delete it (permanent).
        Boolean deleted = client.byok().delete(id).execute().deleted();
        System.out.println("Deleted: " + deleted);
    }
}
