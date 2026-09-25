package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates OpenRouter-level <b>response caching</b> (POST /chat/completions
 * with the {@code X-OpenRouter-Cache} header family): identical requests are
 * answered from cache with zeroed usage counters and no billing. The same three
 * builder methods exist on {@code client.responses()}, {@code client.messages()}
 * and {@code client.embeddings()}, plus the generic {@code header(name, value)}
 * escape hatch for headers the library does not type.
 */
public class OpenRouterResponseCachingExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // Opt this request into response caching (header X-OpenRouter-Cache: true)
        // with a 10-minute TTL (header X-OpenRouter-Cache-TTL: 600, default 300).
        // The FIRST identical request is a MISS: stored and billed normally.
        OpenRouterChatCompletionResponse miss = client.chat().completion()
                .model("google/gemini-2.5-flash")
                .addMessage("user", "What is 2+2? Answer with the number only.")
                .responseCache(true)
                .responseCacheTtl(600)
                .execute();
        System.out.println("first call (MISS, billed): " + miss.assistantMessage());

        // The SAME request again is a HIT: the cached response is replayed with
        // zeroed usage counters and no billing. Traps: the cache key includes the
        // JSON property order (not whitespace) and omitting an optional field is
        // not the same as sending its default - build the repeat the same way.
        // The response id/created reflect the NEW cache-hit generation record.
        OpenRouterChatCompletionResponse hit = client.chat().completion()
                .model("google/gemini-2.5-flash")
                .addMessage("user", "What is 2+2? Answer with the number only.")
                .responseCache(true)
                .responseCacheTtl(600)
                .execute();
        System.out.println("second call (HIT, free): " + hit.assistantMessage()
                + ", prompt tokens: " + hit.promptTokens());

        // Cached responses are returned verbatim regardless of temperature.
        // When you need a fresh answer, force a cache refresh
        // (X-OpenRouter-Cache-Clear: true - only this one cache entry is cleared):
        OpenRouterChatCompletionResponse fresh = client.chat().completion()
                .model("google/gemini-2.5-flash")
                .addMessage("user", "What is 2+2? Answer with the number only.")
                .responseCache(true)
                .responseCacheClear(true)
                .execute();
        System.out.println("refreshed call: " + fresh.assistantMessage());

        // responseCache(false) forces caching OFF for this request even when a
        // preset enables it; null (default) sends no header at all.
        //
        // The generic escape hatch sends any header the library does not type.
        // It shares the header map with responseCache*(...), so for the
        // X-OpenRouter-Cache* names the LAST builder call wins - here the hatch
        // deliberately overrides the typed responseCacheTtl(600) value:
        OpenRouterChatCompletionResponse custom = client.chat().completion()
                .model("google/gemini-2.5-flash")
                .addMessage("user", "What is 2+2? Answer with the number only.")
                .responseCache(true)
                .responseCacheTtl(600)
                .header("X-OpenRouter-Cache-TTL", "120")
                .execute();
        System.out.println("via header() hatch (TTL 120): " + custom.assistantMessage());
    }
}
