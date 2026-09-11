package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterJsonSchema;
import de.entwicklertraining.openrouter4j.OpenRouterToolDefinition;
import de.entwicklertraining.openrouter4j.OpenRouterToolResult;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;
import org.json.JSONObject;

/**
 * Demonstrates a tool result that carries multi-part content
 * ({@code OpenRouterToolResult.ofParts(...)}): the {@code role:"tool"} message
 * then emits {@code content} as an <b>array</b> of content parts (the API's
 * {@code ChatToolMessage.content} accepts a plain string or a content-parts
 * array), so a tool can hand images or documents back to a multimodal model -
 * here a screenshot fetched by the tool is actually looked at.
 *
 * Typed helpers: {@code textPart(String)} and {@code imageUrlPart(url)} /
 * {@code imageUrlPart(url, detail)}; arbitrary other parts (e.g. {@code file},
 * {@code input_audio}, {@code video_url}, the same shapes as on a user message)
 * can be passed as verbatim JSON objects. Legacy {@code of(JSONObject)} results
 * keep emitting the plain string content - no wire-format change.
 */
public class OpenRouterChatCompletionWithMultimodalToolResultExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterToolDefinition screenshotTool = OpenRouterToolDefinition.builder("get_monitor_screenshot")
                .description("Returns a screenshot of the current monitor")
                .parameter("monitor", OpenRouterJsonSchema.stringSchema("Which monitor: 'left' or 'right'"), true)
                .callback(ctx -> OpenRouterToolResult.ofParts(
                        OpenRouterToolResult.textPart("Screenshot of the " + ctx.arguments().getString("monitor")
                                + " monitor, taken just now:"),
                        // A public sample image stands in for the real screenshot.
                        OpenRouterToolResult.imageUrlPart(
                                "https://upload.wikimedia.org/wikipedia/commons/thumb/4/47/PNG_transparency_demonstration_1.png/320px-PNG_transparency_demonstration_1.png")))
                .build();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("z-ai/glm-5.3-flash")
                .addTool(screenshotTool)
                .addMessage("user", "Take a screenshot of my left monitor and tell me what you see.")
                .execute();

        System.out.println("Answer: " + response.assistantMessage());
    }
}
