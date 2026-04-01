package de.entwicklertraining.openrouter4j.chat.completion;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StreamingToolCallAccumulatorTest {

    private final List<String> receivedContent = new ArrayList<>();
    private boolean streamStartCalled = false;
    private boolean errorCalled = false;

    private final StreamingResponseHandler<String> mockHandler = new StreamingResponseHandler<>() {
        @Override public void onStreamStart() { streamStartCalled = true; }
        @Override public void onData(String data) { receivedContent.add(data); }
        @Override public void onComplete() {}
        @Override public void onError(Throwable t) { errorCalled = true; }
    };

    private StreamingToolCallAccumulator accumulator;

    @BeforeEach
    void setUp() {
        accumulator = new StreamingToolCallAccumulator(mockHandler);
        receivedContent.clear();
        streamStartCalled = false;
        errorCalled = false;
    }

    @Test
    void contentChunksAreForwardedToUserHandler() {
        accumulator.onData(contentChunk("Hello"));
        accumulator.onData(contentChunk(" world"));
        accumulator.onData(finishChunk("stop"));

        assertThat(receivedContent).containsExactly("Hello", " world");
        assertThat(accumulator.getFinishReason()).isEqualTo("stop");
        assertThat(accumulator.hasToolCalls()).isFalse();
    }

    @Test
    void toolCallChunksAreAccumulated() {
        accumulator.onData(toolCallStartChunk(0, "call_abc", "get_weather", "{\"lo"));
        accumulator.onData(toolCallArgChunk(0, "cation\":\"K"));
        accumulator.onData(toolCallArgChunk(0, "oeln\"}"));
        accumulator.onData(finishChunk("tool_calls"));

        assertThat(accumulator.hasToolCalls()).isTrue();
        assertThat(receivedContent).isEmpty();

        JSONArray toolCalls = accumulator.getAccumulatedToolCalls();
        assertThat(toolCalls.length()).isEqualTo(1);

        JSONObject tc = toolCalls.getJSONObject(0);
        assertThat(tc.getString("id")).isEqualTo("call_abc");
        assertThat(tc.getJSONObject("function").getString("name")).isEqualTo("get_weather");
        assertThat(tc.getJSONObject("function").getString("arguments")).isEqualTo("{\"location\":\"Koeln\"}");
    }

    @Test
    void multipleToolCallsAreAccumulated() {
        accumulator.onData(toolCallStartChunk(0, "call_1", "get_weather", "{\"location\":\"Berlin\"}"));
        accumulator.onData(toolCallStartChunk(1, "call_2", "get_time", "{\"timezone\":\"CET\"}"));
        accumulator.onData(finishChunk("tool_calls"));

        JSONArray toolCalls = accumulator.getAccumulatedToolCalls();
        assertThat(toolCalls.length()).isEqualTo(2);
        assertThat(toolCalls.getJSONObject(0).getJSONObject("function").getString("name")).isEqualTo("get_weather");
        assertThat(toolCalls.getJSONObject(1).getJSONObject("function").getString("name")).isEqualTo("get_time");
    }

    @Test
    void buildAssistantMessageForToolCalls() {
        accumulator.onData(roleChunk("assistant"));
        accumulator.onData(toolCallStartChunk(0, "call_abc", "fn", "{}"));
        accumulator.onData(finishChunk("tool_calls"));

        JSONObject msg = accumulator.buildAssistantMessage();
        assertThat(msg.getString("role")).isEqualTo("assistant");
        assertThat(msg.isNull("content")).isTrue();
        assertThat(msg.getJSONArray("tool_calls").length()).isEqualTo(1);
    }

    @Test
    void buildAssistantMessageForContent() {
        accumulator.onData(roleChunk("assistant"));
        accumulator.onData(contentChunk("Sunny"));
        accumulator.onData(finishChunk("stop"));

        JSONObject msg = accumulator.buildAssistantMessage();
        assertThat(msg.getString("role")).isEqualTo("assistant");
        assertThat(msg.getString("content")).isEqualTo("Sunny");
    }

    @Test
    void resetClearsState() {
        accumulator.onData(toolCallStartChunk(0, "call_abc", "fn", "{}"));
        accumulator.onData(finishChunk("tool_calls"));
        assertThat(accumulator.hasToolCalls()).isTrue();

        accumulator.reset();
        assertThat(accumulator.hasToolCalls()).isFalse();
        assertThat(accumulator.getFinishReason()).isNull();
    }

    @Test
    void onStreamStartIsForwarded() {
        accumulator.onStreamStart();
        assertThat(streamStartCalled).isTrue();
    }

    @Test
    void onErrorIsForwarded() {
        accumulator.onError(new RuntimeException("test"));
        assertThat(errorCalled).isTrue();
    }

    @Test
    void malformedChunkIsSkipped() {
        accumulator.onData("not json");
        assertThat(receivedContent).isEmpty();
        assertThat(accumulator.getFinishReason()).isNull();
    }

    // --- Helpers ---

    private String contentChunk(String text) {
        return new JSONObject()
            .put("choices", new JSONArray().put(new JSONObject()
                .put("index", 0)
                .put("delta", new JSONObject().put("content", text))
                .put("finish_reason", JSONObject.NULL)))
            .toString();
    }

    private String roleChunk(String role) {
        return new JSONObject()
            .put("choices", new JSONArray().put(new JSONObject()
                .put("index", 0)
                .put("delta", new JSONObject().put("role", role))
                .put("finish_reason", JSONObject.NULL)))
            .toString();
    }

    private String finishChunk(String reason) {
        return new JSONObject()
            .put("choices", new JSONArray().put(new JSONObject()
                .put("index", 0)
                .put("delta", new JSONObject())
                .put("finish_reason", reason)))
            .toString();
    }

    private String toolCallStartChunk(int index, String id, String name, String args) {
        JSONObject fn = new JSONObject().put("name", name).put("arguments", args);
        JSONObject tc = new JSONObject().put("index", index).put("id", id).put("type", "function").put("function", fn);
        return new JSONObject()
            .put("choices", new JSONArray().put(new JSONObject()
                .put("index", 0)
                .put("delta", new JSONObject().put("tool_calls", new JSONArray().put(tc)))
                .put("finish_reason", JSONObject.NULL)))
            .toString();
    }

    private String toolCallArgChunk(int index, String args) {
        JSONObject fn = new JSONObject().put("arguments", args);
        JSONObject tc = new JSONObject().put("index", index).put("function", fn);
        return new JSONObject()
            .put("choices", new JSONArray().put(new JSONObject()
                .put("index", 0)
                .put("delta", new JSONObject().put("tool_calls", new JSONArray().put(tc)))
                .put("finish_reason", JSONObject.NULL)))
            .toString();
    }
}
