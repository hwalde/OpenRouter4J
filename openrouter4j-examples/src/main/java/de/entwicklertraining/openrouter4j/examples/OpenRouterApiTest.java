package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

public class OpenRouterApiTest {
    public static void main(String[] args) {
        System.out.println("Starting OpenRouter API Test...");
        System.out.println("API Key present: " + (System.getenv("OPENROUTER_API_KEY") != null));

        try {
            OpenRouterClient client = new OpenRouterClient();

            System.out.println("\n=== Test 1: Simple Chat with google/gemini-2.5-flash ===");
            OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("google/gemini-2.5-flash")
                .provider("google-ai-studio")
                .addMessage("user", "What is 2+2? Answer with just the number.")
                .execute();

            System.out.println("Response: " + response.assistantMessage());
            System.out.println("Finish Reason: " + response.finishReason());
            System.out.println("Model: " + response.model());

            System.out.println("\n=== Test 2: Chat with openai/gpt-4o-mini ===");
            OpenRouterChatCompletionResponse response2 = client.chat().completion()
                .model("openai/gpt-4o-mini")
                .provider("openai")
                .addMessage("user", "Say 'Hello OpenRouter4J!' in exactly those words.")
                .execute();

            System.out.println("Response: " + response2.assistantMessage());
            System.out.println("Finish Reason: " + response2.finishReason());

            System.out.println("\n=== ALL TESTS PASSED ===");

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
