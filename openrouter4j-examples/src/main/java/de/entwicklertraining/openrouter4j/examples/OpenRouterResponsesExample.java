package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterCodeInterpreterServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterComputerUseServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterCustomTool;
import de.entwicklertraining.openrouter4j.OpenRouterFileSearchServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterImageConfig;
import de.entwicklertraining.openrouter4j.OpenRouterMcpServerTool;
import org.json.JSONObject;
import de.entwicklertraining.openrouter4j.OpenRouterApplyPatchServerTool;
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
        // topLogprobs(n) asks for the top-n log probabilities per output
        // token; promptCacheOptions("explicit") enables explicit prompt
        // caching (only marked blocks are cached, OpenAI GPT-5.6+).
        OpenRouterResponsesResponse response = client.responses()
                .model("openai/gpt-4o")
                .input("Tell me a joke about programming.")
                .maxOutputTokens(256)
                .reasoningEffort("low")
                .topLogprobs(3)
                .promptCacheOptions("explicit")
                .addTool(OpenRouterWebSearchServerTool.builder().build().toJson())
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
        if (response.errorType() != null) {
            System.out.println("Error type:    " + response.errorType()
                    + " (canonical, stable across API formats)");
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

        // A stored prompt template (prompt.id + variables) instead of a raw
        // input prompt - the Responses counterpart of the chat preset field.
        OpenRouterResponsesRequest templated = client.responses()
                .model("openai/gpt-4o")
                .prompt("preset-my-template")
                .promptVariable("tone", "friendly")
                .build();
        System.out.println("\nTemplated body: " + templated.getBody());

        // The image/debug/text output options: imageConfig reuses the chat
        // image type, debug echoes the upstream body, textFormat/textVerbosity
        // compose the text output configuration.
        OpenRouterResponsesRequest shaped = client.responses()
                .model("openai/gpt-4o")
                .input("Draw a cat sitting on a windowsill.")
                .imageConfig(OpenRouterImageConfig.builder()
                        .numImages(1)
                        .aspectRatio("16:9")
                        .build())
                .debug(false)
                .textFormat(new JSONObject().put("type", "text"))
                .textVerbosity("low")
                .build();
        System.out.println("\nImage/debug/text body: " + shaped.getBody());

        // The typed request is also reusable for preset management:
        // client.presets().upsertFromResponses("my-preset").body(theRequest).execute()
        // stores this body as a new version of the preset (management key).
        OpenRouterResponsesRequest reusable = client.responses()
                .model("openai/gpt-4o")
                .input("Be terse.")
                .build();
        System.out.println("\nReusable preset body: " + reusable.getBody());

        // tool_choice object forms - one request per form so precedence cannot
        // mask them. The named function form is flat {"type":"function","name":...}
        // (not the chat nested function.name shape) and the name must match a
        // function tool in `tools`.
        OpenRouterResponsesRequest namedChoice = client.responses()
                .model("openai/gpt-4o")
                .input("What is the weather in Berlin?")
                .addFunctionTool("get_weather", "Get the weather",
                        new JSONObject("{\"type\":\"object\",\"properties\":{}}"))
                .toolChoiceFunction("get_weather")
                .build();
        System.out.println("\nNamed function tool_choice: " + namedChoice.getBody());

        // allowed_tools constrains the model to a predefined set; a tool ref is
        // a String (shorthand for {"type":"function","name":...}) or a verbatim
        // JSONObject. This is the one object form the docs accept alongside
        // deferred tool loading.
        OpenRouterResponsesRequest allowedToolsChoice = client.responses()
                .model("openai/gpt-4o")
                .input("What is the weather in Berlin?")
                .addFunctionTool("get_weather", "Get the weather",
                        new JSONObject("{\"type\":\"object\",\"properties\":{}}"))
                .addFunctionTool("get_time", "Get the current time",
                        new JSONObject("{\"type\":\"object\",\"properties\":{}}"))
                .toolChoiceAllowedTools("auto", "get_weather",
                        new JSONObject("{\"type\":\"function\",\"name\":\"get_time\"}"))
                .build();
        System.out.println("Allowed-tools tool_choice:  " + allowedToolsChoice.getBody());

        // Tool-type shorthand {"type":"<type>"} - the bare documented variants
        // web_search_preview, web_search_preview_2025_03_11, apply_patch, shell.
        // apply_patch / shell force the tool entries whose tools[].type is
        // OpenRouterApplyPatchServerTool.TOOL_TYPE / OpenRouterShellServerTool.TOOL_TYPE.
        // web_search_preview forces a tools[] entry of that same bare variant -
        // it does NOT pair with OpenRouterWebSearchServerTool (openrouter:web_search).
        OpenRouterResponsesRequest typeChoice = client.responses()
                .model("openai/gpt-4o")
                .input("Rewrite this function to be iterative.")
                .addTool(OpenRouterApplyPatchServerTool.builder().build().toJson())
                .toolChoiceType("apply_patch")
                .build();
        System.out.println("Tool-type tool_choice:      " + typeChoice.getBody());

        // OpenAI-native tool types on the Responses tools array (the published
        // schema declares them here, not on chat): custom (client-executed,
        // freeform output - the result comes back as function_call_output with
        // the original call_id, and async(true) lets the model keep working),
        // MCP (third-party tool server, allowed_tools narrows the exposed
        // tools), code_interpreter (sandbox; files land in client.containers()),
        // computer_use_preview and file_search. All of them are added via
        // addTool(...toJson()); OpenRouterCustomTool deliberately does not
        // implement OpenRouterServerTool so it cannot reach chat by accident.
        OpenRouterResponsesRequest nativeTools = client.responses()
                .model("openai/gpt-4o")
                .input("Search my files for the Q3 numbers, then run a quick check.")
                .addTool(OpenRouterCustomTool.grammar("emit_record",
                        "record: \"{\" name \"}\"", "lark").toJson())
                .addTool(OpenRouterMcpServerTool.builder("my-server")
                        .serverUrl("https://example.com/mcp")
                        .allowedTools("search", "fetch")
                        .requireApproval("never")
                        .build()
                        .toJson())
                .addTool(OpenRouterCodeInterpreterServerTool.builder()
                        .containerAuto()
                        .build()
                        .toJson())
                .addTool(OpenRouterComputerUseServerTool.builder(1024, 768, "linux").build().toJson())
                .addTool(OpenRouterFileSearchServerTool.builder("vs_abc123")
                        .maxNumResults(5)
                        .filter("author", "eq", "Alice")
                        .build()
                        .toJson())
                .build();
        System.out.println("OpenAI-native tools body:  " + nativeTools.getBody());
    }
}
