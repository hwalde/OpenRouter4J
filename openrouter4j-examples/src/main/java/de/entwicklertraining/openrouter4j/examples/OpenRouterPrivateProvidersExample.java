package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.privateproviders.OpenRouterPrivateProvider;
import de.entwicklertraining.openrouter4j.privateproviders.OpenRouterPrivateProvidersListResponse;

/**
 * Demonstrates the private providers (management key required, plus the
 * private endpoints entitlement):
 *
 * - GET /private-providers (list)
 * - POST /private-providers (create)
 * - GET /private-providers/{slug} (read one provider)
 * - PATCH /private-providers/{slug} (display name / base URL)
 * - DELETE /private-providers/{slug} (delete; 409 while private endpoints run under it)
 *
 * A private provider is the bring-your-own-key container visible only to
 * your organization; private endpoints are created under it ({@code
 * POST /private-endpoints} with the provider's {@code slug} as
 * {@code provider_slug}). The {@code name} and {@code slug} are fixed at
 * creation and the data policy is fixed by OpenRouter - a private provider's
 * endpoints are recorded as retaining no prompts, training on none and
 * publishing none.
 */
public class OpenRouterPrivateProvidersExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // Create a provider. The slug is derived from the name when omitted
        // ("Acme Inference" becomes "acme-inference") and cannot be changed
        // later; the base URL is what private endpoints under this provider
        // use when they set no URL of their own.
        OpenRouterPrivateProvider created = client.privateProviders()
                .create()
                .name("Acme Inference")
                .slug("acme-inference")
                .displayName("Acme")
                .baseUrl("https://inference.example.com/v1")
                .execute()
                .data();
        System.out.println("Created " + created.slug()
                + " (" + created.displayName() + ", created " + createdAt(created) + ")");

        // List the organization's providers - one page, no pagination.
        OpenRouterPrivateProvidersListResponse<?> page = client.privateProviders()
                .list()
                .execute();
        System.out.println("Providers: " + page.items().size());
        for (OpenRouterPrivateProvider provider : page.items()) {
            System.out.println("  " + provider.slug()
                    + " base=" + provider.baseUrl());
        }

        // Read one provider by slug (the slug travels URL-encoded in the path).
        OpenRouterPrivateProvider one = client.privateProviders()
                .get("acme-inference")
                .execute()
                .data();
        System.out.println("Single read: " + one.name());

        // Only display name and base URL can change; name and slug are
        // immutable and a PATCH with no editable field is rejected (400).
        OpenRouterPrivateProvider updated = client.privateProviders()
                .update("acme-inference")
                .displayName("Acme Inference GmbH")
                .execute()
                .data();
        System.out.println("Updated display name: " + updated.displayName());

        // Delete the provider. The API refuses with 409 while any private
        // endpoint still runs under it; the confirmation carries the slug.
        String deletedSlug = client.privateProviders()
                .delete("acme-inference")
                .execute()
                .slug();
        System.out.println("Deleted: " + deletedSlug);
    }

    private static String createdAt(OpenRouterPrivateProvider provider) {
        return provider.createdAt() != null ? provider.createdAt() : "unknown";
    }
}
