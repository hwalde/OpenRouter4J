package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates the server-tool {@code tool_choice} form: forcing a built-in
 * OpenRouter server tool directly via {@code tool_choice.type} instead of
 * wrapping it in the named function form.
 *
 * <p>The type string is emitted verbatim ({@code {"type":"openrouter:web_search"}}),
 * so any server-tool type works - including ones OpenRouter adds later.
 *
 * <p>Precedence when several tool_choice forms are set: the named function form
 * (toolChoiceFunction) wins, then this server-tool form, then the plain string
 * keywords ("auto", "required", "none").
 */
public class OpenRouterChatCompletionWithServerToolChoiceExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("openai/gpt-4o-mini")
                .addMessage("user", "What is the latest release on the OpenRouter changelog?")
                // Force the web_search server tool for this request:
                .toolChoiceServerTool("openrouter:web_search")
                .execute();

        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("Cost: " + response.cost());
    }
}
