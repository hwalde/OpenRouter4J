package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterDeferredToolsControl;
import de.entwicklertraining.openrouter4j.OpenRouterDeferredToolsDetails;
import de.entwicklertraining.openrouter4j.OpenRouterDeferredToolsSearchStrategy;
import de.entwicklertraining.openrouter4j.OpenRouterToolDefinition;
import de.entwicklertraining.openrouter4j.OpenRouterToolResult;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;
import org.json.JSONObject;

/**
 * Demonstrates the {@code deferred_tools} protocol - the request-level opt-in
 * to OpenRouter's versioned router-level deferred-tool handling: the router
 * withholds the deferred tools from the model, serves their discovery with its
 * own search and call wrappers on <b>any</b> model, keeps the provider-facing
 * tool list stable across the run and replays assistant reasoning unchanged on
 * continuation turns. Unlike the bare {@code defer_loading} pattern (see
 * {@link OpenRouterChatCompletionWithToolSearchExample}), this works on Chat
 * Completions on every model - the {@code deferred_tools} protocol is the
 * replacement for the {@code openrouter:tool_search} server tool there.
 *
 * <p>The run's true totals arrive in {@code usage.deferred_tools_details}:
 * the native usage fields report only the final upstream call, the details
 * object sums every call of the run.
 *
 * <p>Traps (full list on {@link OpenRouterDeferredToolsControl}): the Batch
 * API rejects {@code deferred_tools}; a conversation cannot be enabled
 * partway once earlier assistant messages carry tool calls; the portable
 * profile needs the catalog byte-stable across turns (tool order, descriptions
 * and {@code cache_control} included).
 */
public class OpenRouterDeferredToolsExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // 1. The control object: required protocol / profile / validation / search.
        //    The portable profile defers exactly the tools marked deferLoading(true);
        //    the bm25 search caps one search at 5 hits.
        OpenRouterDeferredToolsControl deferredTools = OpenRouterDeferredToolsControl.builder()
                .protocol("v1")
                .profile("portable")
                .validation("runtime")
                .search(OpenRouterDeferredToolsSearchStrategy.bm25(5))
                .build();

        // 2. One non-deferred tool must remain direct (the router's rule), and one
        //    tool is deferred: withheld until the protocol's search reveals it.
        OpenRouterToolDefinition directTool = OpenRouterToolDefinition.builder("get_weather")
                .description("Get the current weather of a city")
                .callback(ctx -> OpenRouterToolResult.of(new JSONObject().put("temperature", "20 degrees")))
                .build();
        OpenRouterToolDefinition deferredTool = OpenRouterToolDefinition.builder("create_issue")
                .description("Create an issue in the issue tracker")
                .deferLoading(true)
                .callback(ctx -> OpenRouterToolResult.of(new JSONObject().put("id", "ISSUE-42")))
                .build();

        // 3. The same value works on client.chat().completion(), client.responses()
        //    and client.messages() - the wire key is deferred_tools on all three.
        //    The chat tool-call loop carries it into every follow-up turn.
        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("openai/gpt-4o")
                .addMessage("user", "Create an issue about the flaky login test")
                .addTool(directTool)
                .addTool(deferredTool)
                .deferredTools(deferredTools)
                .execute();

        System.out.println(response.assistantMessage());

        // 4. The run totals across every upstream call (the native usage fields
        //    report only the final call).
        OpenRouterDeferredToolsDetails details = response.deferredToolsDetails();
        if (details != null) {
            System.out.printf("deferred-tools run: %d turns, %d prompt + %d completion = %d tokens%n",
                    details.turns(), details.promptTokens(), details.completionTokens(), details.totalTokens());
        }
    }
}
