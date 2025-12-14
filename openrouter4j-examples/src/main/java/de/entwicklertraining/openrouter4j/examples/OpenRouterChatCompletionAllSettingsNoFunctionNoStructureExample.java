package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates an OpenRouter chat completion request that does NOT use:
 *  - structured outputs (no responseSchema)
 *  - function calling (no tools)
 *
 * But uses:
 *  - temperature, topK, topP, maxOutputTokens
 *  - stop sequences
 *  - parallelToolCalls
 *  - systemInstruction
 *  - provider selection
 */
public class OpenRouterChatCompletionAllSettingsNoFunctionNoStructureExample {

    public static void main(String[] args) {
        // Create the OpenRouter client
        OpenRouterClient client = new OpenRouterClient();

        // Build a request that uses (almost) all features except structured outputs & function calling
        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("google/gemini-2.5-flash") // or other OpenRouter models
                .provider("google-ai-studio")
                .temperature(1.0)
                .topK(64)
                .topP(0.8)
                .maxOutputTokens(512)
                .addStopSequence("END_OF_TEXT")
                .parallelToolCalls(false)
                .responseMimeType("text/plain")
                .systemInstruction("You are a creative writing assistant.")
                // Add some user messages
                .addMessage("user", "Hello, I'd like a short adventurous story featuring a detective.")
                .execute();

        if (response.hasRefusal()) {
            System.out.println("The model refused: " + response.refusal());
        } else {
            System.out.println("OpenRouter Response:\n" + response.assistantMessage());
        }
    }
}
