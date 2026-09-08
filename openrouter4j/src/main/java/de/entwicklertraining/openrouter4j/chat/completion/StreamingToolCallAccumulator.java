package de.entwicklertraining.openrouter4j.chat.completion;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Internal wrapper handler that receives raw SSE JSON chunks (via RAW_JSON
 * extractor) and splits them into content (forwarded to user handler) and
 * tool_calls (accumulated internally for the CallHandler to process).
 * <p>
 * Besides content and tool calls it also accumulates the remaining delta
 * fields OpenRouter can stream ({@code reasoning}, {@code reasoning_details},
 * {@code refusal}, {@code audio}) and the chunk-level fields
 * {@code service_tier}, {@code openrouter_metadata} and
 * {@code system_fingerprint}, so the synthetic response of the streaming loop
 * reports the same state as the synchronous response.
 */
final class StreamingToolCallAccumulator implements StreamingResponseHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(StreamingToolCallAccumulator.class);

    private final StreamingResponseHandler<String> userHandler;
    private String finishReason;
    private String nativeFinishReason;
    private String role;
    private JSONObject usage;
    private JSONObject error;
    private String serviceTier;
    private JSONObject openrouterMetadata;
    private String systemFingerprint;
    private String audioId;
    private Long audioExpiresAt;
    private final StringBuilder contentBuilder = new StringBuilder();
    private final StringBuilder reasoningBuilder = new StringBuilder();
    private final StringBuilder refusalBuilder = new StringBuilder();
    private final List<JSONObject> reasoningDetails = new ArrayList<>();
    private final StringBuilder audioDataBuilder = new StringBuilder();
    private final StringBuilder audioTranscriptBuilder = new StringBuilder();
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

            // The terminal usage chunk carries the usage object with an empty
            // choices array ("choices": []) - read it before the choices check
            // so it is not silently dropped.
            if (json.has("usage") && !json.isNull("usage")) {
                this.usage = json.getJSONObject("usage");
            }

            // A mid-generation failure arrives as a regular data: event carrying a
            // top-level "error" object (and no usable choices) - read it before the
            // choices check so the failure is not silently swallowed. The chunk is
            // NOT forwarded as content; the synthetic response of the streaming loop
            // exposes the error through the hasError()/error()/... accessors.
            if (json.has("error") && !json.isNull("error")) {
                this.error = json.getJSONObject("error");
            }

            // Chunk-level fields carried by regular chunks (and echoed by the
            // synthetic response so streaming behaves like the synchronous path).
            if (json.has("service_tier") && !json.isNull("service_tier")) {
                this.serviceTier = json.getString("service_tier");
            }
            if (json.has("openrouter_metadata") && !json.isNull("openrouter_metadata")) {
                this.openrouterMetadata = json.getJSONObject("openrouter_metadata");
            }
            if (json.has("system_fingerprint") && !json.isNull("system_fingerprint")) {
                this.systemFingerprint = json.getString("system_fingerprint");
            }

            JSONArray choices = json.optJSONArray("choices");
            if (choices == null || choices.isEmpty()) return;

            JSONObject choice = choices.getJSONObject(0);

            if (choice.has("finish_reason") && !choice.isNull("finish_reason")) {
                this.finishReason = choice.getString("finish_reason");
            }

            if (choice.has("native_finish_reason") && !choice.isNull("native_finish_reason")) {
                this.nativeFinishReason = choice.getString("native_finish_reason");
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

            if (delta.has("reasoning") && !delta.isNull("reasoning")) {
                reasoningBuilder.append(delta.getString("reasoning"));
            }

            if (delta.has("reasoning_details") && !delta.isNull("reasoning_details")) {
                JSONArray details = delta.getJSONArray("reasoning_details");
                for (int i = 0; i < details.length(); i++) {
                    JSONObject detail = details.optJSONObject(i);
                    if (detail != null) {
                        reasoningDetails.add(detail);
                    }
                }
            }

            if (delta.has("refusal") && !delta.isNull("refusal")) {
                refusalBuilder.append(delta.getString("refusal"));
            }

            if (delta.has("audio") && !delta.isNull("audio")) {
                mergeAudioDelta(delta.getJSONObject("audio"));
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

    /**
     * The provider-native finish reason of the last chunk that carried one
     * ({@code choices[0].native_finish_reason}), or {@code null} when absent.
     */
    String getNativeFinishReason() {
        return nativeFinishReason;
    }

    /**
     * The usage object of the terminal usage chunk, or {@code null} when the
     * stream carried no usage. OpenRouter emits one final chunk whose
     * {@code choices} array is empty and whose {@code usage} object holds the
     * token and cost totals; that chunk is not content and is not forwarded to
     * the user handler - it is exposed here instead.
     */
    JSONObject getUsage() {
        return usage;
    }

    /**
     * The top-level {@code error} object of a mid-stream failure chunk, or
     * {@code null} when the stream carried no error. OpenRouter reports
     * mid-generation failures as regular {@code data:} events with an
     * {@code error} field inside an otherwise valid HTTP 200 stream; without
     * capturing it, a failed stream would look like a normal empty one.
     */
    JSONObject getError() {
        return error;
    }

    /**
     * The accumulated {@code reasoning} text of the streamed deltas, or
     * {@code null} when the stream carried none.
     */
    String getReasoning() {
        return reasoningBuilder.length() > 0 ? reasoningBuilder.toString() : null;
    }

    /**
     * The accumulated {@code reasoning_details} objects of the streamed deltas,
     * appended in arrival order, empty when the stream carried none.
     */
    List<JSONObject> getReasoningDetails() {
        return List.copyOf(reasoningDetails);
    }

    /**
     * The accumulated {@code refusal} text of the streamed deltas, or
     * {@code null} when the stream carried none.
     */
    String getRefusal() {
        return refusalBuilder.length() > 0 ? refusalBuilder.toString() : null;
    }

    /**
     * The merged {@code audio} output object of the streamed deltas
     * ({@code id}, {@code data}, {@code expires_at}, {@code transcript}),
     * or {@code null} when the stream carried none. The base64 {@code data}
     * and the {@code transcript} fragments are concatenated in arrival order;
     * {@code id} and {@code expires_at} keep the first value seen.
     */
    JSONObject getAudio() {
        if (audioId == null && audioExpiresAt == null
                && audioDataBuilder.length() == 0 && audioTranscriptBuilder.length() == 0) {
            return null;
        }
        JSONObject audio = new JSONObject();
        if (audioId != null) {
            audio.put("id", audioId);
        }
        if (audioDataBuilder.length() > 0) {
            audio.put("data", audioDataBuilder.toString());
        }
        if (audioExpiresAt != null) {
            audio.put("expires_at", audioExpiresAt);
        }
        if (audioTranscriptBuilder.length() > 0) {
            audio.put("transcript", audioTranscriptBuilder.toString());
        }
        return audio;
    }

    /**
     * The chunk-level {@code service_tier} value, or {@code null} when no chunk
     * carried one.
     */
    String getServiceTier() {
        return serviceTier;
    }

    /**
     * The chunk-level {@code openrouter_metadata} object, or {@code null} when
     * no chunk carried one (it is opt-in via the {@code X-OpenRouter-Metadata}
     * header).
     */
    JSONObject getOpenrouterMetadata() {
        return openrouterMetadata;
    }

    /**
     * The chunk-level {@code system_fingerprint} value, or {@code null} when no
     * chunk carried one.
     */
    String getSystemFingerprint() {
        return systemFingerprint;
    }

    private void mergeAudioDelta(JSONObject audio) {
        if (audioId == null && audio.has("id") && !audio.isNull("id")) {
            audioId = audio.getString("id");
        }
        if (audio.has("data") && !audio.isNull("data")) {
            audioDataBuilder.append(audio.getString("data"));
        }
        if (audioExpiresAt == null && audio.has("expires_at") && !audio.isNull("expires_at")) {
            audioExpiresAt = audio.getLong("expires_at");
        }
        if (audio.has("transcript") && !audio.isNull("transcript")) {
            audioTranscriptBuilder.append(audio.getString("transcript"));
        }
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
        if (reasoningBuilder.length() > 0) {
            msg.put("reasoning", reasoningBuilder.toString());
        }
        if (!reasoningDetails.isEmpty()) {
            JSONArray details = new JSONArray();
            for (JSONObject detail : reasoningDetails) {
                details.put(detail);
            }
            msg.put("reasoning_details", details);
        }
        if (refusalBuilder.length() > 0) {
            msg.put("refusal", refusalBuilder.toString());
        }
        JSONObject audio = getAudio();
        if (audio != null) {
            msg.put("audio", audio);
        }
        return msg;
    }

    void reset() {
        finishReason = null;
        nativeFinishReason = null;
        usage = null;
        error = null;
        serviceTier = null;
        openrouterMetadata = null;
        systemFingerprint = null;
        audioId = null;
        audioExpiresAt = null;
        toolCallsByIndex.clear();
        role = null;
        contentBuilder.setLength(0);
        reasoningBuilder.setLength(0);
        refusalBuilder.setLength(0);
        reasoningDetails.clear();
        audioDataBuilder.setLength(0);
        audioTranscriptBuilder.setLength(0);
    }

    static final class ToolCallData {
        String id;
        String type;
        String name;
        final StringBuilder argumentsBuilder = new StringBuilder();
    }
}
