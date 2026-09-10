package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterCacheMarker;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterJsonSchema;
import de.entwicklertraining.openrouter4j.OpenRouterToolDefinition;
import de.entwicklertraining.openrouter4j.OpenRouterToolResult;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;
import org.json.JSONObject;

/**
 * Demonstrates explicit per-block prompt-cache breakpoints:
 *
 * - {@code addMessage(role, text, marker)}: emits the message as a content-parts
 *   array whose text part carries {@code cache_control} (Anthropic style,
 *   {@code {"type":"ephemeral"[,"ttl":...]}}) or {@code prompt_cache_breakpoint}
 *   (OpenAI style, {@code {"mode":"explicit"}}). The two are interchangeable -
 *   OpenRouter converts between them based on the provider serving the request
 *   (TTLs are not translated). Everything through the marked part becomes the
 *   candidate cached prefix. Docs: explicit breakpoints are limited to four per
 *   request and should be reserved for large stable blocks.
 * - {@code OpenRouterToolDefinition.Builder.cacheControl()}: the same marker on
 *   the tool object itself - a big tool catalog is the canonical large stable block.
 * - {@code promptCacheOptions("explicit", "30m")}: request-root mode that disables
 *   OpenAI-managed breakpoints so <em>only</em> parts marked with
 *   {@code prompt_cache_breakpoint} participate in caching (OpenAI GPT-5.6+ only;
 *   on other models the option is ignored and markers are auto-converted).
 *
 * <p>Markers live on the messages and tools, which the tool-call loop carries
 * verbatim into follow-up requests - the cache boundary stays stable across turns.
 */
public class OpenRouterChatCompletionWithExplicitCacheBreakpointsExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterToolDefinition weatherTool = OpenRouterToolDefinition.builder("get_weather")
                .description("Get the current weather of a city")
                .parameter("city", OpenRouterJsonSchema.stringSchema("The city name"), true)
                .callback(ctx -> OpenRouterToolResult.of(new JSONObject().put("weather", "sunny")))
                // Mark the tool object itself as an explicit cache breakpoint
                .cacheControl("1h")
                .build();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addTool(weatherTool)
                // Anthropic-style marker with 1-hour TTL on the large stable prefix
                .addMessage("system", "You are a helpful assistant. " + "Knowledge base: ".repeat(50),
                        OpenRouterCacheMarker.cacheControl("1h"))
                // OpenAI-style marker on the user turn (auto-converted per provider)
                .addMessage("user", "What is the weather in Berlin?",
                        OpenRouterCacheMarker.promptCacheBreakpoint())
                .execute();

        System.out.println("Answer: " + response.assistantMessage());
    }
}
