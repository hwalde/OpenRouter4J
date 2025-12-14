package de.entwicklertraining.openrouter4j.chat.completion;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.openrouter4j.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Handles OpenRouter chat completion requests with automatic tool calling loops.
 *
 * OpenRouter follows the OpenAI format for tool calls:
 * - When the model wants to call tools, finish_reason is "tool_calls"
 * - Tool results are returned as messages with role "tool" and tool_call_id
 * - The conversation continues until finish_reason is "stop" or max iterations reached
 */
public final class OpenRouterChatCompletionCallHandler {

    private static final int MAX_TURNS = 10;
    private final OpenRouterClient client;

    public OpenRouterChatCompletionCallHandler(OpenRouterClient client) {
        this.client = client;
    }

    public OpenRouterChatCompletionResponse handleRequest(
            OpenRouterChatCompletionRequest initialRequest,
            boolean useExponentialBackoff
    ) {
        // Copy the initial messages and build a tool map
        List<JSONObject> messages = new ArrayList<>(initialRequest.messages());
        var toolMap = new HashMap<String, OpenRouterToolDefinition>();
        for (var tool : initialRequest.tools()) {
            toolMap.put(tool.name(), tool);
        }

        OpenRouterChatCompletionRequest currentRequest = initialRequest;
        int turnCount = 0;

        while (true) {
            turnCount++;
            if (turnCount > MAX_TURNS) {
                throw new ApiClient.ApiClientException(
                        "Exceeded maximum of " + MAX_TURNS + " OpenRouter call iterations without final stop."
                );
            }

            // Send the request
            OpenRouterChatCompletionResponse response;
            if (useExponentialBackoff) {
                response = client.sendRequestWithExponentialBackoff(currentRequest);
            } else {
                response = client.sendRequest(currentRequest);
            }

            // Check for API errors
            if (response.getJson().has("error")) {
                throw new ApiClient.HTTP_400_RequestRejectedException(
                        "OpenRouter API returned an error: " + response.getJson().toString()
                );
            }

            String finishReason = response.finishReason();

            // Check for refusal
            if (response.hasRefusal()) {
                return response;
            }

            // Check if we have tool calls
            if (!"tool_calls".equals(finishReason) || !response.hasToolCalls()) {
                // No tool calls - this is the final response
                return response;
            }

            // Process tool calls
            JSONArray toolCalls = response.toolCalls();
            if (toolCalls == null || toolCalls.isEmpty()) {
                return response;
            }

            // Add the assistant message with tool_calls to the conversation
            messages.add(response.message());

            // Process each tool call
            for (int i = 0; i < toolCalls.length(); i++) {
                JSONObject toolCall = toolCalls.getJSONObject(i);
                String toolCallId = toolCall.getString("id");
                JSONObject functionObj = toolCall.getJSONObject("function");
                String functionName = functionObj.getString("name");
                String argumentsStr = functionObj.getString("arguments");

                // Check if we have this tool
                if (!toolMap.containsKey(functionName)) {
                    throw new ApiClient.ApiResponseUnusableException(
                            "Unknown tool requested: " + functionName
                    );
                }

                // Parse arguments
                JSONObject args;
                try {
                    args = new JSONObject(argumentsStr);
                } catch (Exception e) {
                    throw new ApiClient.ApiResponseUnusableException(
                            "Failed to parse tool call arguments for " + functionName + ": " + e.getMessage()
                    );
                }

                // Execute the tool callback
                OpenRouterToolDefinition toolDef = toolMap.get(functionName);
                OpenRouterToolResult result = toolDef.callback().handle(
                        new OpenRouterToolCallContext(args)
                );

                // Add tool result as a message with role "tool"
                // OpenRouter expects: { "role": "tool", "tool_call_id": "...", "content": "..." }
                JSONObject toolResultMsg = new JSONObject();
                toolResultMsg.put("role", "tool");
                toolResultMsg.put("tool_call_id", toolCallId);
                toolResultMsg.put("content", result.content().toString());
                messages.add(toolResultMsg);
            }

            // Build the next request with updated messages
            currentRequest = buildNextRequest(initialRequest, messages);
        }
    }

    private OpenRouterChatCompletionRequest buildNextRequest(
            OpenRouterChatCompletionRequest original,
            List<JSONObject> updatedMessages
    ) {
        var builder = OpenRouterChatCompletionRequest.builder(client)
                .model(original.model())
                .maxExecutionTimeInSeconds(original.getMaxExecutionTimeInSeconds())
                .setCancelSupplier(original.getIsCanceledSupplier())
                .temperature(original.temperature())
                .topK(original.topK())
                .topP(original.topP())
                .maxOutputTokens(original.maxTokens())
                .stopSequences(original.stopSequences())
                .tools(original.tools())
                .toolChoice(original.toolChoice())
                .parallelToolCalls(original.parallelToolCalls())
                .responseSchema(original.responseSchema())
                .responseMimeType(original.responseMimeType())
                .thinking(original.thinkingBudget())
                .addAllMessages(updatedMessages);

        // Add provider selection if present
        if (original.providers() != null && !original.providers().isEmpty()) {
            builder.provider(original.providers().toArray(new String[0]));
        }

        // Copy capture settings if available
        if (original.hasCaptureOnSuccess()) {
            builder.captureOnSuccess(original.getCaptureOnSuccess());
        }
        if (original.hasCaptureOnError()) {
            builder.captureOnError(original.getCaptureOnError());
        }

        return builder.build();
    }
}
