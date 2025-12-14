package de.entwicklertraining.openrouter4j.chat.completion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Wraps the JSON response from OpenRouter chat completions endpoint.
 *
 * OpenRouter Response Format:
 * {
 *   "id": "gen-xxx",
 *   "model": "google/gemini-2.5-flash",
 *   "choices": [{
 *     "index": 0,
 *     "message": {
 *       "role": "assistant",
 *       "content": "Response text",
 *       "refusal": null,
 *       "tool_calls": [{
 *         "id": "call_xxx",
 *         "type": "function",
 *         "function": {
 *           "name": "function_name",
 *           "arguments": "{\"arg\": \"value\"}"
 *         }
 *       }]
 *     },
 *     "finish_reason": "stop|tool_calls|length"
 *   }],
 *   "usage": {
 *     "prompt_tokens": 10,
 *     "completion_tokens": 20,
 *     "total_tokens": 30
 *   }
 * }
 */
public final class OpenRouterChatCompletionResponse extends OpenRouterResponse<OpenRouterChatCompletionRequest> {

    public OpenRouterChatCompletionResponse(JSONObject json, OpenRouterChatCompletionRequest request) {
        super(json, request);
    }

    /**
     * Returns the assistant's message content from choices[0].message.content
     */
    public String assistantMessage() {
        try {
            JSONArray choices = getJson().getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            return message.optString("content", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the finish_reason from choices[0].finish_reason
     * Possible values: "stop", "length", "tool_calls", "content_filter"
     */
    public String finishReason() {
        try {
            JSONArray choices = getJson().getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            return firstChoice.optString("finish_reason", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if the message contains a refusal.
     * OpenRouter supports refusals in choices[0].message.refusal
     */
    public boolean hasRefusal() {
        try {
            JSONArray choices = getJson().getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            return message.has("refusal") && !message.isNull("refusal");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the refusal text if present.
     */
    public String refusal() {
        try {
            JSONArray choices = getJson().getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            return message.optString("refusal", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Throws an exception if the model refused to comply.
     */
    public void throwOnRefusal() {
        if (hasRefusal()) {
            throw new ApiClient.ApiResponseUnusableException("Model refused to comply: " + refusal());
        }
    }

    /**
     * Checks if the response contains tool calls.
     */
    public boolean hasToolCalls() {
        try {
            JSONArray choices = getJson().getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            return message.has("tool_calls") && !message.isNull("tool_calls");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the tool calls array from choices[0].message.tool_calls
     */
    public JSONArray toolCalls() {
        try {
            JSONArray choices = getJson().getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            return message.optJSONArray("tool_calls");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns usage information if present.
     */
    public JSONObject usage() {
        return getJson().optJSONObject("usage");
    }

    /**
     * Returns the total tokens used (prompt + completion).
     */
    public Integer totalTokens() {
        JSONObject usage = usage();
        return usage != null ? usage.optInt("total_tokens") : null;
    }

    /**
     * Returns the prompt tokens used.
     */
    public Integer promptTokens() {
        JSONObject usage = usage();
        return usage != null ? usage.optInt("prompt_tokens") : null;
    }

    /**
     * Returns the completion tokens used.
     */
    public Integer completionTokens() {
        JSONObject usage = usage();
        return usage != null ? usage.optInt("completion_tokens") : null;
    }

    /**
     * Parses the assistant message as JSON.
     * Useful when response_format was set to json_object or json_schema.
     */
    public JSONObject parsed() {
        String content = assistantMessage();
        if (content == null || content.isBlank()) {
            throw new ApiClient.ApiResponseUnusableException("No content to parse");
        }
        return new JSONObject(content);
    }

    /**
     * Converts the assistant message to a Java object using Jackson.
     * Useful when response_format was set to json_schema.
     */
    public <T> T convertTo(Class<T> targetType) {
        String content = assistantMessage();
        if (content == null || content.isBlank()) {
            throw new ApiClient.ApiResponseUnusableException("No content to convert");
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(content, targetType);
        } catch (JsonProcessingException e) {
            throw new ApiClient.ApiResponseUnusableException(
                    "Failed to parse the model's JSON into the expected structure/POJO: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Returns the full message object from choices[0].message
     */
    public JSONObject message() {
        try {
            JSONArray choices = getJson().getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            return firstChoice.getJSONObject("message");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the model used for this completion.
     */
    public String model() {
        return getJson().optString("model", null);
    }

    /**
     * Returns the completion ID.
     */
    public String id() {
        return getJson().optString("id", null);
    }
}
