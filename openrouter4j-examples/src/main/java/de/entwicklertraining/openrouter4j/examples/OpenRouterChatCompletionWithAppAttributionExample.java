package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterAppAttribution;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates app attribution: HTTP-Referer, X-OpenRouter-Title and
 * X-OpenRouter-Categories. OpenRouter uses these headers for its leaderboards
 * and analytics.
 *
 * Two levels are shown:
 *   1. Client-level: configure once via client.appAttribution(...) and every
 *      request carries the headers.
 *   2. Per request: httpReferer(...)/appTitle(...)/appCategories(...) on the
 *      builder win over the client-level default for that single request.
 *
 * Note: at most 2 categories are accepted per request; the library rejects
 * more with an IllegalArgumentException.
 */
public class OpenRouterChatCompletionWithAppAttributionExample {

    public static void main(String[] args) {
        // Level 1: set once on the client - sent with every request
        OpenRouterClient client = new OpenRouterClient();
        client.appAttribution(OpenRouterAppAttribution.builder()
                .httpReferer("https://github.com/my-org/my-app")
                .appTitle("My App")
                .categories("code", "chat")
                .build());

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addMessage("user", "Say hello in one short sentence.")
                .execute();
        System.out.println("Client-level attribution: " + response.assistantMessage());

        // Level 2: override per request - wins for this request only
        OpenRouterChatCompletionResponse override = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .httpReferer("https://github.com/my-org/my-other-app")
                .appTitle("My Other App")
                .appCategories("tools")
                .addMessage("user", "Say goodbye in one short sentence.")
                .execute();
        System.out.println("Per-request attribution: " + override.assistantMessage());
    }
}
