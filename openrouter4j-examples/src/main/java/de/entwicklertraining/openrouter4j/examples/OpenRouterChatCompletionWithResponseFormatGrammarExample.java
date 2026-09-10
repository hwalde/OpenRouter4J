package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates the grammar and python {@code response_format} types:
 *
 * - {@code responseGrammar(String)}: emits
 *   {@code response_format: {"type":"grammar","grammar":...}} - a
 *   llama.cpp/Grammar-style grammar string that constrains generation token by
 *   token (e.g. {@code root ::= "yes" | "no"}). Requires an endpoint with grammar
 *   support - combine with {@code requireParameters(true)} to route only to
 *   supporting endpoints and avoid the silent drop.
 * - {@code responsePython()}: emits {@code response_format: {"type":"python"}} -
 *   requests Python-code output; carries no payload.
 *
 * <p>Precedence when several forms are set: {@code responseSchema} (structured
 * output) &gt; {@code responseGrammar} &gt; {@code responsePython} &gt;
 * {@code responseMimeType} - exactly one {@code response_format} object is
 * emitted, the highest-priority form configured wins.
 */
public class OpenRouterChatCompletionWithResponseFormatGrammarExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addMessage("user", "Is the Eiffel Tower taller than the Brandenburg Gate? Answer yes or no.")
                // Constrain the answer to a strict yes/no grammar
                .responseGrammar("root ::= \"yes\" | \"no\"")
                .execute();

        System.out.println("Answer: " + response.assistantMessage());

        // Python-code output - a separate request, because the precedence chain
        // (schema > grammar > python > mime) would let the grammar win on one builder
        OpenRouterChatCompletionResponse pythonResponse = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addMessage("user", "Write a short Python function that adds two numbers.")
                .responsePython()
                .execute();

        System.out.println("Python: " + pythonResponse.assistantMessage());
    }
}
