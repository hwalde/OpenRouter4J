package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.keys.OpenRouterApiKey;
import de.entwicklertraining.openrouter4j.keys.OpenRouterCurrentKeyResponse;
import de.entwicklertraining.openrouter4j.keys.OpenRouterKeyCreateResponse;
import de.entwicklertraining.openrouter4j.keys.OpenRouterKeysListResponse;

import java.util.List;

/**
 * Demonstrates the API key management endpoints. The management operations
 * (list, create, get, update, delete) require a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>;
 * GET /key (currentKey) works with a normal inference key.
 *
 * <p>Secrets rule: the plaintext key of a create response is shown exactly
 * once and cannot be retrieved later - never log it. This example prints only
 * the masked label.
 */
public class OpenRouterKeysManagementExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // 1. The key making the call (works with a normal inference key):
        //    limits, usage and whether it is a management key.
        OpenRouterCurrentKeyResponse current = client.currentKey().execute();
        System.out.println("Current key " + current.label()
                + " (management key: " + current.isManagementKey() + ")");
        System.out.println("  limit: " + current.limit()
                + ", remaining: " + current.limitRemaining()
                + ", usage: " + current.usage());
        // Region policy and scoping - region policy is one of several
        // restrictions; other key rules (e.g. management keys blocked from
        // inference) still apply independently.
        System.out.println("  allowed data regions: " + current.allowedDataRegions());
        System.out.println("  organization: " + current.organizationId()
                + ", workspace: " + current.workspaceId());
        // The free-model (:free variant) daily request quota - remaining can
        // report the tier policy rather than a real budget (exempt accounts
        // and BYOK requests are not gated by it).
        if (current.freeModelDailyRequests() != null) {
            System.out.println("  free-model daily requests: "
                    + current.freeModelDailyRequests().used() + "/"
                    + current.freeModelDailyRequests().limit() + " used, "
                    + current.freeModelDailyRequests().remaining() + " remaining");
        }

        // 2. List the keys of the account (management key required).
        OpenRouterKeysListResponse list = client.keys().list().execute();
        List<OpenRouterApiKey> keys = list.items();
        System.out.println("Keys: " + keys.size());
        for (OpenRouterApiKey key : keys) {
            System.out.printf("  %s (%s), disabled: %s, usage: %s USD%n",
                    key.name(), key.label(), key.disabled(), key.usage());
        }

        // 3. Create a key - the plaintext key() is returned exactly once.
        OpenRouterKeyCreateResponse created = client.keys().create()
                .name("Example Rotation Key")
                .limit(5.0)
                .limitReset("monthly")
                .execute();
        String hash = created.data() != null ? created.data().hash() : null;
        System.out.println("Created key " + created.data().label()
                + " (hash " + hash + ") - plaintext key intentionally not printed");

        // 4. Update it: tighten the limit and disable it.
        client.keys().update(hash)
                .limit(2.0)
                .disabled(true)
                .execute();
        System.out.println("Updated key " + hash);

        // 5. Read one key by hash.
        OpenRouterApiKey fetched = client.keys().get(hash).execute().data();
        System.out.println("Fetched key " + fetched.name() + ", limit now " + fetched.limit());

        // 6. Delete it (permanent).
        Boolean deleted = client.keys().delete(hash).execute().deleted();
        System.out.println("Deleted: " + deleted);
    }
}
