package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

public class OpenRouterApiTest {
    public static void main(String[] args) {
        System.out.println("Starting OpenRouter API Test...");
        System.out.println("API Key present: " + (System.getenv("OPENROUTER_API_KEY") != null));

        try {
            OpenRouterClient client = new OpenRouterClient();

            System.out.println("\n=== Test 1: Simple Chat with deepseek/deepseek-v4-flash-0731 ===");
            OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .provider("deepseek")
                .addMessage("user", "What is 2+2? Answer with just the number.")
                .execute();

            System.out.println("Response: " + response.assistantMessage());
            System.out.println("Finish Reason: " + response.finishReason());
            System.out.println("Model: " + response.model());

            System.out.println("\n=== Test 2: Chat with z-ai/glm-5.3-flash ===");
            OpenRouterChatCompletionResponse response2 = client.chat().completion()
                .model("z-ai/glm-5.3-flash")
                .provider("z-ai")
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
