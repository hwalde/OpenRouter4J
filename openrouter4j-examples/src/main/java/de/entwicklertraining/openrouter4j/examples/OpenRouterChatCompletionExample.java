package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * A basic example of calling the OpenRouter chat completion to just generate text from text-only input.
 */
public class OpenRouterChatCompletionExample {

    public static void main(String[] args) {
        // Create the OpenRouter client
        OpenRouterClient client = new OpenRouterClient();

        // Minimal usage:
        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addMessage("user", "Hello, how are you?")
                .execute();

        System.out.println("OpenRouter says: " + response.assistantMessage());

        // Message provenance: choices[0].message.model names the model that
        // actually produced this message (it can differ from the top-level
        // model in multi-attempt routing); message.name echoes the optional
        // participant name.
        System.out.println("Producing model: " + response.messageModel());
        System.out.println("Message name:    " + response.messageName());

        // Server-tool statistics: when server tools (openrouter:web_search,
        // openrouter:bash, ...) ran inside the request, the response carries
        // usage.server_tool_use_details alongside the metered cost
        // (serverToolCost()).
        if (response.serverToolUseDetails() != null) {
            System.out.println("Server tool calls requested: " + response.serverToolCallsRequested());
            System.out.println("Server tool calls executed:  " + response.serverToolCallsExecuted());
            System.out.println("Web search requests:         " + response.serverToolWebSearchRequests());
            // Do not sum serverToolWebSearchRequests() and
            // serverToolCallsRequested(): a server-orchestrated search counts
            // in both, provider-native search reports only web_search_requests.
        }
    }
}
