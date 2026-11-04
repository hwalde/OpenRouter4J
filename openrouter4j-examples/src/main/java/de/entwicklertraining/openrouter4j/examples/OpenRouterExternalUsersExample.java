package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.externalusers.OpenRouterExternalUser;
import de.entwicklertraining.openrouter4j.externalusers.OpenRouterExternalUsersListResponse;

/**
 * Demonstrates the external-user registrations (management key required):
 *
 * - GET /external-users (list, with the exact-match user filter)
 * - POST /external-users (register explicitly)
 * - GET /external-users/{user} (read one registration)
 * - PATCH /external-users/{user} (reactivate / deactivate)
 * - DELETE /external-users/{user} (soft-deactivate)
 *
 * A registration is normally created implicitly by sending {@code user} on
 * inference requests or {@code external.user} on API keys (for organization
 * keys outside HIPAA workspaces; the stored value has surrounding
 * whitespace trimmed) - the explicit register call just brings it into
 * existence before first use (409 when it already exists, active or
 * deactivated). The tracking string is immutable and case-sensitive;
 * {@code is_active} is registration state only and never blocks inference.
 */
public class OpenRouterExternalUsersExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // Explicit registration (normally implicit via inference `user`).
        OpenRouterExternalUser registered = client.externalUsers()
                .register("employee_123")
                .execute()
                .data();
        System.out.println("Registered " + registered.user()
                + " (active: " + registered.isActive() + ")");

        // List with the exact-match user filter. The filter is also the
        // lookup path for ids of "." or "..", which no by-user route can
        // address (the path segment normalizes away).
        OpenRouterExternalUsersListResponse<?> page = client.externalUsers()
                .list()
                .user("employee_123")
                .includeInactive(true)
                .limit(50)
                .execute();
        System.out.println("Registrations: " + page.items().size()
                + " of " + page.totalCount());
        for (OpenRouterExternalUser registration : page.items()) {
            System.out.println("  " + registration.user()
                    + " active=" + registration.isActive()
                    + " created " + registration.createdAt());
        }

        // Read one registration by its URL-encoded tracking id.
        OpenRouterExternalUser one = client.externalUsers()
                .get("employee_123")
                .execute()
                .data();
        System.out.println("Single read: " + one.user() + " updated " + one.updatedAt());

        // Soft-deactivate: the tracking id is retained and the registration
        // can be reactivated later with update(user, true). Repeat
        // deactivation succeeds, and inference is never blocked by this.
        client.externalUsers().deactivate("employee_123").execute();
        OpenRouterExternalUser reactivated = client.externalUsers()
                .update("employee_123", true)
                .execute()
                .data();
        System.out.println("Reactivated: " + reactivated.user()
                + " (active: " + reactivated.isActive() + ")");
    }
}
