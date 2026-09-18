package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import de.entwicklertraining.openrouter4j.OpenRouterWebSearchServerTool;
import de.entwicklertraining.openrouter4j.responses.OpenRouterResponsesRequest;
import de.entwicklertraining.openrouter4j.responses.OpenRouterResponsesResponse;

/**
 * Demonstrates the OpenAI Responses API on OpenRouter: POST /responses,
 * non-streaming and streaming.
 *
 * <p>The Responses API is the successor surface for several features that
 * only exist there (e.g. full {@code openrouter:tool_search} support,
 * {@code openrouter:apply_patch}, subagent function inheritance). The API
 * is stateless: {@code previous_response_id} is not supported - send the
 * full conversation history in {@code input} instead.
 *
 * <p>Streaming: every Responses-API event arrives as its raw JSON string on
 * {@code onData}; the {@code type} field distinguishes
 * {@code response.created}, {@code response.output_text.delta},
 * {@code response.completed}, {@code response.failed}, ... terminated by
 * the {@code [DONE]} sentinel.
 */
public class OpenRouterResponsesExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // Non-streaming: the input is a plain string here; multi-turn input
        // uses addMessage(role, text) / addInputItem(JSONObject) instead.
        OpenRouterResponsesResponse response = client.responses()
                .model("openai/gpt-4o")
                .input("Tell me a joke about programming.")
                .maxOutputTokens(256)
                .reasoningEffort("low")
                .addTool(OpenRouterWebSearchServerTool.builder().build().toJson())
                .toolChoice("auto")
                .execute();

        System.out.println("Response id:   " + response.id());
        System.out.println("Status:        " + response.status());
        System.out.println("Model:         " + response.model());
        System.out.println("Output text:   " + response.outputText());
        System.out.println("Input tokens:  " + response.inputTokens());
        System.out.println("Output tokens: " + response.outputTokens());
        System.out.println("Total tokens:  " + response.totalTokens());
        if (response.cost() != null) {
            System.out.println("Cost (USD):    " + response.cost());
        }
        if (response.error() != null) {
            System.out.println("Error:         " + response.error());
        }

        // Streaming: the body automatically carries stream: true and every
        // event arrives as raw JSON.
        System.out.println("\nStreaming:");
        client.responses()
                .model("openai/gpt-4o")
                .addMessage("user", "Count from one to five, one number per line.")
                .stream(new StreamingResponseHandler<String>() {
                    @Override
                    public void onData(String eventJson) {
                        JSONObject event = new JSONObject(eventJson);
                        if ("response.output_text.delta".equals(event.optString("type"))) {
                            System.out.print(event.optString("delta", ""));
                        } else if ("response.completed".equals(event.optString("type"))) {
                            System.out.println("\n[completed]");
                        }
                    }

                    @Override
                    public void onComplete() {
                        // stream ended
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.err.println("stream error: " + throwable.getMessage());
                    }
                })
                .execute();

        // The typed request is also reusable for preset management:
        // client.presets().upsertFromResponses("my-preset").body(theRequest).execute()
        // stores this body as a new version of the preset (management key).
        OpenRouterResponsesRequest reusable = client.responses()
                .model("openai/gpt-4o")
                .input("Be terse.")
                .build();
        System.out.println("\nReusable preset body: " + reusable.getBody());
    }
}
