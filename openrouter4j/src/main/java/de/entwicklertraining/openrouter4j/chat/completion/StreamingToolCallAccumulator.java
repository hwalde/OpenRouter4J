package de.entwicklertraining.openrouter4j.chat.completion;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

/**
 * Internal wrapper handler that receives raw SSE JSON chunks (via RAW_JSON
 * extractor) and splits them into content (forwarded to user handler) and
 * tool_calls (accumulated internally for the CallHandler to process).
 */
final class StreamingToolCallAccumulator implements StreamingResponseHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(StreamingToolCallAccumulator.class);

    private final StreamingResponseHandler<String> userHandler;
    private String finishReason;
    private String role;
    private final StringBuilder contentBuilder = new StringBuilder();
    private final TreeMap<Integer, ToolCallData> toolCallsByIndex = new TreeMap<>();

    StreamingToolCallAccumulator(StreamingResponseHandler<String> userHandler) {
        this.userHandler = userHandler;
    }

    @Override
    public void onStreamStart() {
        userHandler.onStreamStart();
    }

    @Override
    public void onData(String rawJson) {
        try {
            JSONObject json = new JSONObject(rawJson);
            JSONArray choices = json.optJSONArray("choices");
            if (choices == null || choices.isEmpty()) return;

            JSONObject choice = choices.getJSONObject(0);

            if (choice.has("finish_reason") && !choice.isNull("finish_reason")) {
                this.finishReason = choice.getString("finish_reason");
            }

            JSONObject delta = choice.optJSONObject("delta");
            if (delta == null) return;

            if (delta.has("role")) {
                this.role = delta.getString("role");
            }

            if (delta.has("content") && !delta.isNull("content")) {
                String content = delta.getString("content");
                contentBuilder.append(content);
                userHandler.onData(content);
            }

            if (delta.has("tool_calls")) {
                JSONArray toolCalls = delta.getJSONArray("tool_calls");
                for (int i = 0; i < toolCalls.length(); i++) {
                    JSONObject tc = toolCalls.getJSONObject(i);
                    int index = tc.getInt("index");

                    ToolCallData data = toolCallsByIndex.computeIfAbsent(index, k -> new ToolCallData());

                    if (tc.has("id")) {
                        data.id = tc.getString("id");
                    }
                    if (tc.has("type")) {
                        data.type = tc.getString("type");
                    }
                    if (tc.has("function")) {
                        JSONObject fn = tc.getJSONObject("function");
                        if (fn.has("name")) {
                            data.name = fn.getString("name");
                        }
                        if (fn.has("arguments")) {
                            data.argumentsBuilder.append(fn.getString("arguments"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to parse streaming chunk: {}", e.getMessage());
        }
    }

    @Override
    public void onComplete() {
        // Intentionally NOT forwarded - CallHandler controls when to signal completion
    }

    @Override
    public void onError(Throwable throwable) {
        userHandler.onError(throwable);
    }

    @Override
    public void onMetadata(Map<String, Object> metadata) {
        userHandler.onMetadata(metadata);
    }

    @Override
    public boolean shouldCancel() {
        return userHandler.shouldCancel();
    }

    String getFinishReason() {
        return finishReason;
    }

    boolean hasToolCalls() {
        return "tool_calls".equals(finishReason) && !toolCallsByIndex.isEmpty();
    }

    JSONArray getAccumulatedToolCalls() {
        JSONArray result = new JSONArray();
        for (var entry : toolCallsByIndex.entrySet()) {
            ToolCallData data = entry.getValue();
            JSONObject tc = new JSONObject();
            tc.put("id", data.id);
            tc.put("type", data.type != null ? data.type : "function");
            JSONObject fn = new JSONObject();
            fn.put("name", data.name);
            fn.put("arguments", data.argumentsBuilder.toString());
            tc.put("function", fn);
            result.put(tc);
        }
        return result;
    }

    JSONObject buildAssistantMessage() {
        JSONObject msg = new JSONObject();
        msg.put("role", role != null ? role : "assistant");
        if (hasToolCalls()) {
            msg.put("content", JSONObject.NULL);
            msg.put("tool_calls", getAccumulatedToolCalls());
        } else {
            msg.put("content", contentBuilder.toString());
        }
        return msg;
    }

    void reset() {
        finishReason = null;
        toolCallsByIndex.clear();
        role = null;
        contentBuilder.setLength(0);
    }

    static final class ToolCallData {
        String id;
        String type;
        String name;
        final StringBuilder argumentsBuilder = new StringBuilder();
    }
}
