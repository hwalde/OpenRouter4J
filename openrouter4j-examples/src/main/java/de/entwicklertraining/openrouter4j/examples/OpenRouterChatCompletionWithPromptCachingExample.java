package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates the prompt-caching controls at request root:
 *
 * - {@code cacheControl()} / {@code cacheControl(String ttl)}: request-root
 *   {@code cache_control} with {@code {"type":"ephemeral"}} enables *automatic*
 *   caching - OpenRouter applies the cache breakpoint to the last cacheable block
 *   and advances it as the conversation grows (supported by Anthropic, Google
 *   Vertex, Azure and Amazon Bedrock). Optional TTL: {@code "5m"} (default) or {@code "1h"}.
 * - {@code promptCacheKey(String)}: sticky-routing key so related requests land on
 *   the same provider and hit the prompt cache. Only used when {@code session_id}
 *   is not set (see the observability example).
 * - {@code promptCacheOptions(String mode[, String ttl])}: e.g. {@code "explicit"}
 *   disables OpenAI-managed breakpoints so only blocks marked with
 *   {@code prompt_cache_breakpoint} participate in caching.
 *
 * <p>Note on the model used here: {@code deepseek/deepseek-v4-flash-0731} caches
 * prompts implicitly - DeepSeek's context caching reuses long shared prefixes
 * automatically, and the official DeepSeek endpoint on OpenRouter advertises
 * implicit caching. {@code promptCacheKey(...)} is provider-agnostic (OpenRouter
 * sticky routing) and keeps the conversation on one endpoint so the cache is hit.
 * The request-root {@code cacheControl(...)} shown is honoured by Anthropic,
 * Google Vertex, Azure and Amazon Bedrock; on DeepSeek the request still
 * succeeds, caching just stays implicit.
 */
public class OpenRouterChatCompletionWithPromptCachingExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                // Automatic caching with a one-hour time-to-live
                .cacheControl("1h")
                // Keep the whole conversation on one provider to hit the cache
                .promptCacheKey("support-agent-conversation-42")
                // Alternative mode: only blocks marked with prompt_cache_breakpoint are
                // cached (comment this line out for OpenAI-managed breakpoints).
                // This option only affects OpenAI models; on DeepSeek it is ignored.
                .promptCacheOptions("explicit")
                .addMessage("system", "You are a support agent. Here is the full knowledge base: ...(long text)...")
                .addMessage("user", "How do I reset my password?")
                .execute();

        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("Cache hits are visible in the usage details of the response JSON.");
    }
}
