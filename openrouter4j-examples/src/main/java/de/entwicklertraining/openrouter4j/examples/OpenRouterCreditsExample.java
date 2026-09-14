package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.credits.OpenRouterCreditsResponse;

/**
 * Demonstrates GET /credits - the total credits purchased and used on the
 * account, in USD.
 *
 * <p>OpenRouter requires a management key for this endpoint; a normal
 * inference key is rejected with HTTP 403 (surfaced as api-base's
 * {@code HTTP_403_PermissionDeniedException}).
 */
public class OpenRouterCreditsExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterCreditsResponse response = client.credits().execute();

        System.out.println("Total credits purchased (USD): " + response.totalCredits());
        System.out.println("Total usage (USD):             " + response.totalUsage());
        if (response.totalCredits() != null && response.totalUsage() != null) {
            System.out.println("Remaining (USD):               "
                    + (response.totalCredits() - response.totalUsage()));
        }
    }
}
