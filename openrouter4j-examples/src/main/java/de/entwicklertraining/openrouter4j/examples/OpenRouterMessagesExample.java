package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.OpenRouterClearToolUsesEdit;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterCompactEdit;
import de.entwicklertraining.openrouter4j.OpenRouterSafeguard;
import de.entwicklertraining.openrouter4j.OpenRouterStopCondition;
import de.entwicklertraining.openrouter4j.messages.OpenRouterAnthropicTool;
import de.entwicklertraining.openrouter4j.messages.OpenRouterMessagesRequest;
import de.entwicklertraining.openrouter4j.messages.OpenRouterMessagesResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

/**
 * Demonstrates the Anthropic Messages API on OpenRouter (POST /messages):
 * the system prompt travels as a top-level field, {@code max_tokens} is
 * required by Anthropic semantics, and the response carries typed views over
 * the content blocks (text, tool_use, thinking, ...). The second half shows
 * SSE streaming - every Anthropic event (message_start, content_block_delta,
 * message_stop, ...) arrives as its raw JSON string on {@code onData}.
 */
public class OpenRouterMessagesExample {

    public static void main(String[] args) throws Exception {
        OpenRouterClient client = new OpenRouterClient();

        // 1. Non-streaming request with a tool definition.
        OpenRouterMessagesResponse response = client.messages()
                .model("anthropic/claude-sonnet-4")
                .maxTokens(1024)
                .system("You are a helpful assistant.")
                .addMessage("user", "What is the weather in Paris?")
                .addTool(new OpenRouterAnthropicTool.Builder()
                        .name("get_weather")
                        .description("Get the current weather of a city")
                        .inputSchema(new JSONObject()
                                .put("type", "object")
                                .put("properties", new JSONObject().put(
                                        "city", new JSONObject().put("type", "string")))
                                .put("required", new JSONArray().put("city")))
                        .build())
                // Stop the server-side tool loop when a condition fires
                // (OR logic; overrides max_tool_calls; ends with one final
                // turn with tool calls disabled).
                .stopServerToolsWhen(
                        OpenRouterStopCondition.stepCountIs(3),
                        OpenRouterStopCondition.maxCost(0.25))
                // Server-side context editing: shed old tool results once the
                // conversation grows, and compact before the context window
                // is exhausted.
                .addContextManagement(OpenRouterClearToolUsesEdit.builder()
                        .triggerInputTokens(100000)
                        .keepLastToolUses(5)
                        .clearAtLeastInputTokens(20000)
                        .build())
                .addContextManagement(OpenRouterCompactEdit.builder()
                        .instructions("Preserve the task state and the last user request")
                        .triggerInputTokens(150000)
                        .build())
                // Anthropic server-side safeguards (Anthropic-provider semantics).
                .safeguards(
                        OpenRouterSafeguard.of("dangerous_tool_use"),
                        OpenRouterSafeguard.of("harmful_content",
                                new JSONObject().put("permission_mode", "auto").put("v", 1)))
                .effort("medium")
                .execute();

        System.out.println("id: " + response.id()
                + ", model: " + response.model()
                + ", stop_reason: " + response.stopReason());
        System.out.println("text: " + response.text());
        System.out.println("tool calls: " + response.toolUseBlocks().size()
                + ", usage: in=" + response.inputTokens() + " out=" + response.outputTokens()
                + " cost=" + response.cost());

        // What the server removed from the prompt / transformed in the input.
        // applied_edits only appears when the request used contextManagement(...);
        // input_transformations only when the provider transformed the input
        // (commonly Anthropic dropping thinking blocks on a prefix mismatch).
        for (OpenRouterMessagesResponse.OpenRouterAppliedContextEdit edit
                : response.appliedContextEdits()) {
            System.out.println("context edit applied: " + edit.type()
                    + " " + edit.json());
        }
        for (OpenRouterMessagesResponse.OpenRouterInputTransformation transformation
                : response.inputTransformations()) {
            System.out.println("input transformed: " + transformation.type()
                    + " at " + transformation.path() + " (" + transformation.reason() + ")");
        }

        // Safeguard outcomes (Anthropic-provider semantics).
        for (OpenRouterMessagesResponse.OpenRouterSafeguardResult result
                : response.safeguardResults()) {
            System.out.println("safeguard " + result.type() + ": " + result.status());
        }

        // 2. Streaming: enable SSE and parse the Anthropic event model. The
        //    body carries stream:true automatically; events are executed
        //    through the client (executeAsync) and completed when the stream
        //    ends.
        OpenRouterMessagesRequest streamingRequest = client.messages()
                .model("anthropic/claude-sonnet-4")
                .maxTokens(1024)
                .addMessage("user", "Tell me a two-sentence story.")
                .stream(new StreamingResponseHandler<String>() {
                    @Override
                    public void onStreamStart() {
                        System.out.print("Assistant: ");
                    }

                    @Override
                    public void onData(String eventJson) {
                        JSONObject event = new JSONObject(eventJson);
                        if ("content_block_delta".equals(event.optString("type"))) {
                            JSONObject delta = event.optJSONObject("delta");
                            if (delta != null && "text_delta".equals(delta.optString("type"))) {
                                System.out.print(delta.optString("text"));
                            }
                        }
                    }

                    @Override
                    public void onMetadata(Map<String, Object> metadata) {
                        // SSE event metadata (event type, id, ...)
                    }

                    @Override
                    public void onComplete() {
                        System.out.println();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.err.println("stream error: " + throwable.getMessage());
                    }
                })
                .build();

        client.executeAsync(streamingRequest).get();
    }
}
