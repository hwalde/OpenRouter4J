package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demonstrates observability parameters:
 *
 * - metadata: up to 16 string key/value pairs attached to the generation
 *   (keys max 64 chars, values max 512 chars); the library validates the
 *   limits up front.
 * - user: a stable per-end-user identifier for abuse isolation.
 * - sessionId: groups related requests; OpenRouter uses it as sticky-routing
 *   key to maximise prompt-cache hits. It is sent both in the body
 *   (session_id) and as the x-session-id header - the body field wins.
 * - metadataInResponse(true): opt-in header (X-OpenRouter-Metadata: enabled)
 *   to receive routing metadata under openrouter_metadata on the response.
 */
public class OpenRouterChatCompletionWithObservabilityExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("request_id", "req-2026-09-04-001");
        metadata.put("tenant", "acme-corp");

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("openai/gpt-4o-mini")
                .metadata(metadata)            // attached to the generation
                .user("end-user-42")           // per-end-user identifier
                .sessionId("onboarding-flow")  // sticky routing for cache hits
                .metadataInResponse(true)      // X-OpenRouter-Metadata: enabled
                .addMessage("user", "Say hello in one short sentence.")
                .execute();

        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("Metadata, user and session_id reach OpenRouter's generation records;");
        System.out.println("openrouter_metadata on the response is available via the raw JSON:");
        System.out.println(response.getJson().optJSONObject("openrouter_metadata"));
    }
}
