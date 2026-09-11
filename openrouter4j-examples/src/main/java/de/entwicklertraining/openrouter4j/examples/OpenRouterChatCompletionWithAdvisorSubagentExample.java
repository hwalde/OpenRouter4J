package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterAdvisorServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterImageGenerationServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterSearchModelsServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterSubagentServerTool;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates the typed server tools added beyond web_search / web_fetch /
 * datetime: the {@code openrouter:advisor} (a higher-intelligence model
 * consulted mid-generation), the {@code openrouter:subagent} (a smaller worker
 * model executing a delegated task) and
 * {@code openrouter:experimental__search_models} (the model can search the
 * OpenRouter catalog itself), plus the {@code openrouter:image_generation}
 * tool with an escape-hatch option.
 *
 * Traps documented in the javadoc of the tools:
 * - advisor {@code stream(true)} has no effect on Chat Completions;
 * - subagent {@code inherit_functions}/{@code inherited_function_names} are
 *   Responses-API-only (a 400 elsewhere);
 * - server tools run inside OpenRouter's agent loop - budget it with
 *   {@code stopServerToolsWhen(...)} when cost matters.
 */
public class OpenRouterChatCompletionWithAdvisorSubagentExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addMessage("user", "I want to write a short fairy tale about a fox. "
                        + "Ask your reviewer for feedback on an outline first, then write the final tale.")
                // Advisor: consult a stronger model for a focused plan. The
                // advisor sees only the prompt in the tool call (no transcript
                // forwarding), answers with at most 2048 tokens:
                .addServerTool(OpenRouterAdvisorServerTool.builder()
                        .name("reviewer")
                        .model("~anthropic/claude-opus-latest")
                        .instructions("You are a senior story editor. Give a focused, decisive plan.")
                        .maxCompletionTokens(2048)
                        .build())
                // Subagent: a cheap worker for the actual draft. Named
                // instances are keyed by parameters.name:
                .addServerTool(OpenRouterSubagentServerTool.builder()
                        .name("drafter")
                        .instructions("You draft fairy tales quickly and charmingly.")
                        .temperature(0.9)
                        .build())
                // Let the model look up models itself (experimental tool):
                .addServerTool(OpenRouterSearchModelsServerTool.builder()
                        .maxResults(5)
                        .build())
                // Escape hatch carries the image_config-style keys verbatim:
                .addServerTool(OpenRouterImageGenerationServerTool.builder()
                        .option("aspect_ratio", "16:9")
                        .build())
                // Budget the server-tool agent loop (OR logic):
                .stopServerToolsWhen(de.entwicklertraining.openrouter4j.OpenRouterStopCondition.maxCost(0.25))
                .execute();

        if (response.hasError()) {
            throw new IllegalStateException("OpenRouter failed: code=" + response.errorCode()
                    + " message=" + response.errorMessage());
        }

        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("Cost: " + response.cost());
        System.out.println("Server-tool cost: " + response.serverToolCost());
    }
}
