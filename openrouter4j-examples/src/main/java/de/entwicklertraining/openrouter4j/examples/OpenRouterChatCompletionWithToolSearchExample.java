package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterJsonSchema;
import de.entwicklertraining.openrouter4j.OpenRouterToolDefinition;
import de.entwicklertraining.openrouter4j.OpenRouterToolResult;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;
import org.json.JSONObject;

/**
 * Demonstrates deferred tool loading ({@code function.defer_loading}) - the
 * pattern for large tool catalogs (dozens or hundreds of tools): tools marked
 * with {@code deferLoading(true)} are withheld from the model until tool search
 * reveals them, so the prompt stays small and cache-stable.
 *
 * <p><b>How the deferral is expanded on Chat Completions (the endpoint this
 * library implements):</b> <b>without</b> the {@code openrouter:tool_search}
 * server tool, the request routes to a provider whose gateway expands deferred
 * tools itself - on Chat Completions that works on Anthropic models and
 * Anthropic-compatible endpoints only (this example uses
 * {@code anthropic/claude-haiku-4.5} for that reason; on other models the
 * request fails with a 400). <b>With</b> {@code openrouter:tool_search},
 * OpenRouter manages deferral itself and it works on any model - but that
 * server tool is only served by the Responses and Anthropic Messages APIs, so
 * requesting it on Chat Completions returns a 400 (the typed class
 * {@code OpenRouterToolSearchServerTool} exists for the moment this library
 * speaks those APIs - do not send it through {@code serverTools(...)} here).
 *
 * <p>Constraint in either case: at least one tool must remain non-deferred
 * (the {@code get_weather} tool below). On the tool-search path, {@code
 * tool_choice} must also be omitted or left at the default {@code "auto"}.
 */
public class OpenRouterChatCompletionWithToolSearchExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // Two deferred tools: withheld from the model until tool search reveals them
        OpenRouterToolDefinition searchArchive = OpenRouterToolDefinition.builder("search_archive")
                .description("Search the internal document archive")
                .parameter("query", OpenRouterJsonSchema.stringSchema("The search query"), true)
                .callback(ctx -> OpenRouterToolResult.of(new JSONObject().put("hits", 3)))
                .deferLoading(true)
                .build();
        OpenRouterToolDefinition lookupCustomer = OpenRouterToolDefinition.builder("lookup_customer")
                .description("Look up a customer record by id")
                .parameter("customerId", OpenRouterJsonSchema.stringSchema("The customer id"), true)
                .callback(ctx -> OpenRouterToolResult.of(new JSONObject().put("name", "Ada")))
                .deferLoading(true)
                .build();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                // Provider-managed deferral on Chat Completions: Anthropic models only
                .model("anthropic/claude-haiku-4.5")
                // At least one tool must remain non-deferred
                .addTool(OpenRouterToolDefinition.builder("get_weather")
                        .description("Get the current weather of a city")
                        .parameter("city", OpenRouterJsonSchema.stringSchema("The city name"), true)
                        .callback(ctx -> OpenRouterToolResult.of(new JSONObject().put("weather", "sunny")))
                        .build())
                .addTool(searchArchive)
                .addTool(lookupCustomer)
                .addMessage("user", "Search the archive for the Q3 report and tell me the weather in Berlin.")
                .execute();

        System.out.println("Answer: " + response.assistantMessage());
    }
}
