package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates message-author names, the {@code developer} role and the
 * mid-conversation reasoning-effort change:
 *
 * - {@code addNamedMessage(role, name, text)}: the optional {@code name} key on
 *   system/user/developer/assistant messages - helps the model distinguish
 *   participants of the same role. (Not an {@code addMessage} overload: that
 *   signature is taken by the cache-marker variant, see the javadoc.)
 * - {@code addDeveloperMessage(text)}: the {@code role:"developer"} message -
 *   per the OpenAI message spec, developer instructions take precedence over
 *   conflicting system messages.
 * - {@code addConfigurationUpdate(effort)}: a content-less system message
 *   {@code {"role":"system","content":"","configuration_update":{"reasoning":
 *   {"effort":...}}}} that changes the reasoning effort from this point onward
 *   <em>without invalidating the prompt cache for the preceding turns</em>.
 *
 * Docs traps for the configuration update: it must sit directly before the user
 * turn the new effort applies to, be kept at the same position in later
 * requests, and two updates must not be adjacent. The tool-call loop carries
 * messages verbatim, so the update keeps its position across turns.
 */
public class OpenRouterChatCompletionWithConfigurationUpdateExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // Turn 1: cheap effort for a simple question. The system message carries
        // an author name; the developer message carries behaviour guidance.
        OpenRouterChatCompletionResponse first = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addNamedMessage("system", "support-bot", "You are a concise support assistant.")
                .addDeveloperMessage("Always answer in the language of the question.")
                .addMessage("user", "What is 2 + 2?")
                .execute();
        System.out.println("Turn 1 (low effort): " + first.assistantMessage());

        // Turn 2: same conversation prefix (cache stays valid), then the
        // content-less configuration update directly before the new user turn -
        // the harder question gets high reasoning effort.
        OpenRouterChatCompletionResponse second = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .addNamedMessage("system", "support-bot", "You are a concise support assistant.")
                .addDeveloperMessage("Always answer in the language of the question.")
                .addMessage("user", "What is 2 + 2?")
                .addMessage("assistant", first.assistantMessage())
                .addConfigurationUpdate("high")
                .addMessage("user", "And what is 17 * 24 + sqrt(961)?")
                .execute();
        System.out.println("Turn 2 (high effort): " + second.assistantMessage());

        // The update message keeps its position in later requests of the
        // conversation (no second, adjacent update - a later update replaces
        // the previous one only when positioned correctly):
        List<String> transcript = new ArrayList<>();
        transcript.add(second.assistantMessage());
        System.out.println("Conversation turns so far: " + transcript.size());
    }
}
