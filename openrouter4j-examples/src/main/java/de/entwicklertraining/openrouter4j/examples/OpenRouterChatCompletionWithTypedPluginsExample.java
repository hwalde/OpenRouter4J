package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterAlignmentPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterArmorPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterAutoRouterPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterFileParserPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterJevRouterPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterModerationPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterSafeguardResult;
import de.entwicklertraining.openrouter4j.OpenRouterSwitchyardRouterPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterWebFetchPlugin;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;
import de.entwicklertraining.openrouter4j.responses.OpenRouterResponsesResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Demonstrates the typed plugin implementations beyond the classic
 * {@code web} plugin. Every typed plugin emits only the fields explicitly
 * configured on its builder; {@code OpenRouterGenericPlugin} /
 * {@code OpenRouterPlugin.of(id)} remains the verbatim escape hatch for
 * unknown keys.
 *
 * Shown here (one plugin per request - plugins are usually combined with care):
 * - {@code auto-router}: automatic routing filtered by model patterns and cost tier
 * - {@code web-fetch}: let the model fetch from (or never from) domains
 * - {@code file-parser}: PDF parsing with a chosen engine
 * - {@code moderation}: OpenRouter's moderation guard, no configuration keys
 * - {@code switchyard-router}: route between model tiers with a chosen algorithm
 * - {@code jev-router}: configure Jev (TypeSafe) router model pool and cost tier
 * - {@code alignment} (Beta): evaluate assistant messages against per-request rules
 *   and inspect {@code response.alignment()} / {@code response.alignmentCalls()}
 * - {@code armor} (Beta): evaluate client tool calls against a rule stack and
 *   inspect {@code response.safeguardResults()}
 */
public class OpenRouterChatCompletionWithTypedPluginsExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // 1) auto-router: automatic model routing between wildcard patterns,
        //    restricted to the low cost tier (cost_tier takes precedence over
        //    the deprecated cost_quality_tradeoff number).
        run(client.chat().completion()
                .addPlugin(OpenRouterAutoRouterPlugin.builder()
                        .allowedModels(List.of("anthropic/*", "openai/*"))
                        .costTier("low")
                        .build())
                .addMessage("user", "Summarize what HTTP status code 402 means."));

        // 2) web-fetch: the model may fetch up to 5 pages, only from
        //    example.com (and never from nope.org), truncated at 2048 tokens.
        run(client.chat().completion()
                .addPlugin(OpenRouterWebFetchPlugin.builder()
                        .maxUses(5)
                        .maxContentTokens(2048)
                        .allowedDomains(List.of("example.com"))
                        .blockedDomains(List.of("nope.org"))
                        .build())
                .addMessage("user", "What does the example.com landing page say?"));

        // 3) file-parser: PDF parsing via Mistral OCR.
        run(client.chat().completion()
                .addPlugin(OpenRouterFileParserPlugin.builder()
                        .pdfEngine("mistral-ocr")
                        .build())
                .addMessage("user", "What does the attached document say?"));

        // 4) moderation: no configuration keys in the schema - just the id.
        run(client.chat().completion()
                .addPlugin(new OpenRouterModerationPlugin())
                .addMessage("user", "Tell me a clean joke about databases."));

        // 5) switchyard-router: route between model tiers with the composite
        //    algorithm (keeps the tier from the last human turn, re-evaluates
        //    tool turns with stage signals).
        run(client.chat().completion()
                .addPlugin(OpenRouterSwitchyardRouterPlugin.builder()
                        .algorithm("composite")
                        .build())
                .addMessage("user", "Write a haiku about compile errors."));

        // 6) jev-router: restrict the Jev (TypeSafe) router pool to preferred
        //    model patterns with hard exclusions and a medium cost tier.
        run(client.chat().completion()
                .model("typesafe/jev-router")
                .addPlugin(OpenRouterJevRouterPlugin.builder()
                        .models("anthropic/*", "openai/gpt-5.6-sol")
                        .excludedModels("openai/gpt-6-astra")
                        .costTier("medium")
                        .build())
                .addMessage("user", "Summarize the trade-offs of connection pooling."));

        // 7) alignment (Beta): evaluate every assistant message against rules
        //    in audit mode and read the per-message outcomes from alignment().
        OpenRouterChatCompletionResponse aligned = client.chat().completion()
                .addPlugin(OpenRouterAlignmentPlugin.builder()
                        .rule("Do not say a refund is issued or approved.", "content")
                        .rule("Do not ask for payment card details.", List.of("content", "tool_calls"))
                        .mode("audit")
                        .threshold(0.7)
                        .instruct(true)
                        .maxRetries(1)
                        .build())
                .addMessage("user", "My mug arrived broken. Will I get my money back?")
                .execute();
        System.out.println("Alignment raw object: " + aligned.alignment());
        System.out.println("Alignment evaluated calls: " + aligned.alignmentCalls().size());

        // 8) armor (Beta): evaluate every client tool call against a
        //    classifier_context rule stack and inspect safeguardResults().
        //    We call client.responses() with a function tool here so the model
        //    can emit a client tool call and the caller can inspect that turn's
        //    safeguardResults() before executing any tool (on Chat Completions,
        //    tools with handlers are executed inside the automatic tool loop and
        //    only the final turn's response is returned).
        JSONObject classifierContext = new JSONObject()
                .put("v", 1)
                .put("auto_mode", new JSONObject()
                        .put("environment", new JSONArray().put("Trusted repo: github.com/acme/app"))
                        .put("hard_deny", new JSONArray().put(new JSONObject()
                                .put("id", "git-push")
                                .put("name", "Git Push")
                                .put("instructions", "Running any git push command."))));
        JSONObject shellParams = new JSONObject()
                .put("type", "object")
                .put("properties", new JSONObject()
                        .put("command", new JSONObject().put("type", "string")))
                .put("required", new JSONArray().put("command"));
        OpenRouterResponsesResponse armored = client.responses()
                .model("openai/gpt-4.1")
                .input("Push the current branch to origin with git push origin main.")
                .addFunctionTool("run_shell", "Run a shell command in the project directory", shellParams)
                .addPlugin(OpenRouterArmorPlugin.builder()
                        .classifierContext(classifierContext)
                        .mode("enforce")
                        .onError("block")
                        .outputMode("custom_item")
                        .evaluateNativeTools(false)
                        .useCcStyleDefaultRules(false)
                        .enabled(true)
                        .build())
                .execute();
        for (OpenRouterSafeguardResult verdict : armored.safeguardResults()) {
            System.out.println("Armor safeguard (" + verdict.type() + "): " + verdict.toolUses());
        }
    }

    private static void run(de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest.Builder builder) {
        OpenRouterChatCompletionResponse response = builder.execute();
        if (response.hasError()) {
            throw new IllegalStateException("OpenRouter failed: code=" + response.errorCode()
                    + " message=" + response.errorMessage());
        }
        System.out.println("Model served: " + response.model());
        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("---");
    }
}
