package de.entwicklertraining.openrouter4j.chat.completion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Wraps the JSON response from OpenRouter chat completions endpoint.
 *
 * OpenRouter Response Format:
 * {
 *   "id": "gen-xxx",
 *   "model": "google/gemini-2.5-flash",
 *   "provider": "Google AI Studio",
 *   "choices": [{
 *     "index": 0,
 *     "message": {
 *       "role": "assistant",
 *       "content": "Response text",
 *       "refusal": null,
 *       "reasoning": "chain-of-thought output of reasoning models",
 *       "reasoning_details": [ ... ],
 *       "tool_calls": [{
 *         "id": "call_xxx",
 *         "type": "function",
 *         "function": {
 *           "name": "function_name",
 *           "arguments": "{\"arg\": \"value\"}"
 *         }
 *       }]
 *     },
 *     "finish_reason": "stop|tool_calls|length",
 *     "native_finish_reason": "provider-native finish reason"
 *   }],
 *   "openrouter_metadata": { ... routing metadata, opt-in via X-OpenRouter-Metadata ... },
 *   "usage": {
 *     "prompt_tokens": 10,
 *     "completion_tokens": 20,
 *     "total_tokens": 30,
 *     "cost": 0.0012,
 *     "cost_details": { ... },
 *     "prompt_tokens_details": {"cached_tokens": 2},
 *     "completion_tokens_details": {"reasoning_tokens": 5}
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

    /**
     * Returns the chain-of-thought output of reasoning models from
     * choices[0].message.reasoning, or {@code null} when the model did not
     * reason or reasoning was excluded from the response.
     */
    public String reasoning() {
        try {
            JSONArray choices = getJson().getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            return message.optString("reasoning", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the reasoning detail objects from choices[0].message.reasoning_details,
     * empty when absent (never {@code null}). The objects carry provider-specific
     * chain-of-thought details (e.g. encrypted or plain text summaries).
     */
    public List<JSONObject> reasoningDetails() {
        try {
            JSONArray choices = getJson().getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            JSONArray details = message.optJSONArray("reasoning_details");
            List<JSONObject> result = new ArrayList<>();
            if (details != null) {
                for (int i = 0; i < details.length(); i++) {
                    JSONObject detail = details.optJSONObject(i);
                    if (detail != null) {
                        result.add(detail);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Returns the provider-native finish reason from choices[0].native_finish_reason,
     * or {@code null} when absent. OpenRouter normalises the finish reason into
     * {@link #finishReason()}; this accessor surfaces the raw value the upstream
     * provider reported next to it.
     */
    public String nativeFinishReason() {
        try {
            JSONArray choices = getJson().getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            return firstChoice.optString("native_finish_reason", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the top-level {@code provider} field - the name of the provider that
     * served this request - or {@code null} when absent.
     */
    public String provider() {
        return getJson().optString("provider", null);
    }

    /**
     * Returns the routing metadata object ({@code openrouter_metadata}), or
     * {@code null} when absent. It is only present when the request opted in via
     * the {@code X-OpenRouter-Metadata: enabled} header
     * ({@code OpenRouterChatCompletionRequest.Builder#metadataInResponse(boolean)}).
     */
    public JSONObject openrouterMetadata() {
        return getJson().optJSONObject("openrouter_metadata");
    }

    /**
     * Checks whether the response carries a top-level {@code error} object.
     * OpenRouter reports mid-request failures this way - inside an otherwise
     * valid HTTP 200 response. Because the accessors of this class swallow
     * exceptions and return {@code null}, such a response would otherwise look
     * like an empty one instead of a failed one.
     * <p>
     * Since 1.7.0 this is also populated by the synthetic response of the
     * streaming tool-call loop when the stream carried a mid-stream error chunk.
     */
    public boolean hasError() {
        return getJson().has("error") && !getJson().isNull("error");
    }

    /**
     * Returns the raw top-level {@code error} object, or {@code null} when the
     * response does not carry one (see {@link #hasError()}). Since 1.7.0 the
     * synthetic response of the streaming tool-call loop carries a captured
     * mid-stream error chunk here as well.
     */
    public JSONObject error() {
        return getJson().optJSONObject("error");
    }

    /**
     * Returns the numeric error code from the top-level {@code error} object,
     * or {@code null} when absent.
     */
    public Integer errorCode() {
        JSONObject error = error();
        if (error == null || error.isNull("code")) {
            return null;
        }
        Object code = error.opt("code");
        return code instanceof Number n ? n.intValue() : null;
    }

    /**
     * Returns the message from the top-level {@code error} object,
     * or {@code null} when absent.
     */
    public String errorMessage() {
        JSONObject error = error();
        return error != null ? error.optString("message", null) : null;
    }

    /**
     * Throws an exception when the response carries a top-level {@code error}
     * object (see {@link #hasError()}). Call this before trusting the other
     * accessors when a failed response must not look like an empty one.
     */
    public void throwOnError() {
        if (hasError()) {
            Object errorObj = getJson().opt("error");
            String detail = errorMessage() != null
                    ? errorMessage()
                    : String.valueOf(errorObj);
            throw new ApiClient.ApiResponseUnusableException(
                    "OpenRouter reported an error inside the response: " + detail);
        }
    }

    /**
     * Returns the cost of this completion in USD from {@code usage.cost},
     * or {@code null} when absent.
     */
    public Double cost() {
        JSONObject usage = usage();
        if (usage == null || usage.isNull("cost")) {
            return null;
        }
        return usage.optDouble("cost");
    }

    /**
     * Returns the cost breakdown from {@code usage.cost_details}, or {@code null}
     * when absent.
     */
    public JSONObject costDetails() {
        JSONObject usage = usage();
        return usage != null ? usage.optJSONObject("cost_details") : null;
    }

    /**
     * Returns the number of cached prompt tokens from
     * {@code usage.prompt_tokens_details.cached_tokens}, or {@code null} when absent.
     */
    public Integer cachedPromptTokens() {
        JSONObject usage = usage();
        if (usage == null) {
            return null;
        }
        JSONObject details = usage.optJSONObject("prompt_tokens_details");
        if (details == null || details.isNull("cached_tokens")) {
            return null;
        }
        return details.optInt("cached_tokens");
    }

    /**
     * Returns the number of reasoning tokens from
     * {@code usage.completion_tokens_details.reasoning_tokens}, or {@code null}
     * when absent.
     */
    public Integer reasoningTokens() {
        JSONObject usage = usage();
        if (usage == null) {
            return null;
        }
        JSONObject details = usage.optJSONObject("completion_tokens_details");
        if (details == null || details.isNull("reasoning_tokens")) {
            return null;
        }
        return details.optInt("reasoning_tokens");
    }

    /**
     * Returns the capacity tier that actually served this request from the top-level
     * {@code service_tier} field ({@code "default"}, {@code "flex"}, {@code "priority"}
     * or {@code null}), or {@code null} when absent. The request pins the tier via
     * {@code OpenRouterChatCompletionRequest.Builder#serviceTier(String)}.
     */
    public String serviceTier() {
        return getJson().optString("service_tier", null);
    }

    /**
     * Returns the number of prediction tokens the model accepted from
     * {@code usage.completion_tokens_details.accepted_prediction_tokens}, or
     * {@code null} when absent. Only meaningful when the request carried a
     * {@code prediction} (predicted outputs).
     */
    public Integer acceptedPredictionTokens() {
        return predictionTokens("accepted_prediction_tokens");
    }

    /**
     * Returns the number of prediction tokens the model rejected from
     * {@code usage.completion_tokens_details.rejected_prediction_tokens}, or
     * {@code null} when absent. Only meaningful when the request carried a
     * {@code prediction} (predicted outputs).
     */
    public Integer rejectedPredictionTokens() {
        return predictionTokens("rejected_prediction_tokens");
    }

    private Integer predictionTokens(String key) {
        JSONObject usage = usage();
        if (usage == null) {
            return null;
        }
        JSONObject details = usage.optJSONObject("completion_tokens_details");
        if (details == null || details.isNull(key)) {
            return null;
        }
        return details.optInt(key);
    }
}
