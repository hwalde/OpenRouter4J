package de.entwicklertraining.openrouter4j.chat.completion;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
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

    @Test
    void terminalUsageChunkIsCapturedAndNotForwarded() {
        accumulator.onData(contentChunk("Hello"));
        accumulator.onData(usageChunk(0.0012, 10, 15, 25));

        assertThat(receivedContent).containsExactly("Hello");
        assertThat(accumulator.getUsage()).isNotNull();
        assertThat(accumulator.getUsage().getDouble("cost")).isEqualTo(0.0012);
        assertThat(accumulator.getUsage().getInt("prompt_tokens")).isEqualTo(10);
        assertThat(accumulator.getUsage().getInt("completion_tokens")).isEqualTo(15);
        assertThat(accumulator.getUsage().getInt("total_tokens")).isEqualTo(25);
        assertThat(accumulator.getFinishReason()).isNull();
    }

    @Test
    void usageObjectWithEmptyChoicesArrayIsCaptured() {
        // OpenRouter emits the terminal usage chunk with "choices": [] - it must
        // not trip the empty-choices early return.
        String chunk = new JSONObject()
                .put("id", "gen-xxx")
                .put("choices", new JSONArray())
                .put("usage", new JSONObject().put("total_tokens", 25))
                .toString();
        accumulator.onData(chunk);

        assertThat(accumulator.getUsage()).isNotNull();
        assertThat(accumulator.getUsage().getInt("total_tokens")).isEqualTo(25);
    }

    @Test
    void nativeFinishReasonIsCaptured() {
        String chunk = new JSONObject()
                .put("choices", new JSONArray().put(new JSONObject()
                        .put("index", 0)
                        .put("delta", new JSONObject())
                        .put("finish_reason", "stop")
                        .put("native_finish_reason", "end_turn")))
                .toString();
        accumulator.onData(chunk);

        assertThat(accumulator.getFinishReason()).isEqualTo("stop");
        assertThat(accumulator.getNativeFinishReason()).isEqualTo("end_turn");
    }

    @Test
    void resetClearsUsageAndNativeFinishReason() {
        accumulator.onData(usageChunk(0.0012, 10, 15, 25));
        accumulator.onData(finishChunkWithNativeReason("stop", "end_turn"));
        assertThat(accumulator.getUsage()).isNotNull();
        assertThat(accumulator.getNativeFinishReason()).isEqualTo("end_turn");

        accumulator.reset();
        assertThat(accumulator.getUsage()).isNull();
        assertThat(accumulator.getNativeFinishReason()).isNull();
    }

    @Test
    void syntheticResponseCarriesUsageAndNativeFinishReason() {
        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());

        accumulator.onData(contentChunk("Hello"));
        accumulator.onData(finishChunkWithNativeReason("stop", "end_turn"));
        accumulator.onData(usageChunk(0.0012, 10, 15, 25));

        var response = new OpenRouterChatCompletionResponse(
                handler.buildSyntheticResponseJson(accumulator, "test/model"), null);

        assertThat(response.cost()).isEqualTo(0.0012);
        assertThat(response.promptTokens()).isEqualTo(10);
        assertThat(response.completionTokens()).isEqualTo(15);
        assertThat(response.totalTokens()).isEqualTo(25);
        assertThat(response.nativeFinishReason()).isEqualTo("end_turn");
        assertThat(response.finishReason()).isEqualTo("stop");
        assertThat(response.assistantMessage()).isEqualTo("Hello");
    }

    @Test
    void syntheticResponseWithoutUsageChunkHasNoUsageOrNativeFinishReason() {
        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());

        accumulator.onData(contentChunk("Hello"));
        accumulator.onData(finishChunk("stop"));

        var response = new OpenRouterChatCompletionResponse(
                handler.buildSyntheticResponseJson(accumulator, "test/model"), null);

        assertThat(response.cost()).isNull();
        assertThat(response.promptTokens()).isNull();
        assertThat(response.nativeFinishReason()).isNull();
        assertThat(response.finishReason()).isEqualTo("stop");
    }

    @Test
    void midStreamErrorChunkIsCapturedAndNotForwarded() {
        accumulator.onData(contentChunk("Hello"));
        accumulator.onData(errorChunk("Rate limit exceeded", 429, "rate_limit_exceeded", "provider_error"));

        assertThat(receivedContent).containsExactly("Hello");
        assertThat(accumulator.getError()).isNotNull();
        assertThat(accumulator.getError().getString("message")).isEqualTo("Rate limit exceeded");
        assertThat(accumulator.getError().getInt("code")).isEqualTo(429);
        assertThat(accumulator.getFinishReason()).isNull();
        assertThat(accumulator.hasToolCalls()).isFalse();
    }

    @Test
    void midStreamErrorChunkIsExposedOnSyntheticResponse() {
        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());

        accumulator.onData(contentChunk("Hello"));
        accumulator.onData(errorChunk("Rate limit exceeded", 429, "rate_limit_exceeded", null));
        accumulator.onData(usageChunk(0.002, 10, 5, 15));

        var response = new OpenRouterChatCompletionResponse(
                handler.buildSyntheticResponseJson(accumulator, "test/model"), null);

        assertThat(response.hasError()).isTrue();
        assertThat(response.errorCode()).isEqualTo(429);
        assertThat(response.errorMessage()).isEqualTo("Rate limit exceeded");
        assertThat(response.error().getJSONObject("metadata").getString("error_type"))
                .isEqualTo("rate_limit_exceeded");
        assertThat(response.cost()).isEqualTo(0.002);
        assertThat(response.assistantMessage()).isEqualTo("Hello");
    }

    @Test
    void midStreamErrorChunkWithoutMetadataIsExposedOnSyntheticResponse() {
        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());

        String chunk = new JSONObject()
                .put("error", new JSONObject().put("message", "provider down").put("code", 502))
                .toString();
        accumulator.onData(chunk);

        var response = new OpenRouterChatCompletionResponse(
                handler.buildSyntheticResponseJson(accumulator, "test/model"), null);

        assertThat(response.hasError()).isTrue();
        assertThat(response.errorCode()).isEqualTo(502);
        assertThat(response.errorMessage()).isEqualTo("provider down");
    }

    @Test
    void streamWithoutErrorChunkHasNoErrorOnSyntheticResponse() {
        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());

        accumulator.onData(contentChunk("Hello"));
        accumulator.onData(finishChunk("stop"));

        var response = new OpenRouterChatCompletionResponse(
                handler.buildSyntheticResponseJson(accumulator, "test/model"), null);

        assertThat(response.hasError()).isFalse();
        assertThat(response.error()).isNull();
        assertThat(response.errorCode()).isNull();
        assertThat(response.errorMessage()).isNull();
    }

    @Test
    void nullErrorChunkIsIgnored() {
        // {"error": null} must not be treated as a real failure.
        String chunk = new JSONObject().put("error", JSONObject.NULL).toString();
        accumulator.onData(chunk);

        assertThat(accumulator.getError()).isNull();
        assertThat(receivedContent).isEmpty();
    }

    @Test
    void resetClearsError() {
        accumulator.onData(errorChunk("fail", 500, null, null));
        assertThat(accumulator.getError()).isNotNull();

        accumulator.reset();
        assertThat(accumulator.getError()).isNull();
    }

    @Test
    void reasoningDeltasAreAccumulated() {
        accumulator.onData(reasoningChunk("Let me think."));
        accumulator.onData(reasoningChunk(" 2+2 is 4."));
        accumulator.onData(finishChunk("stop"));

        assertThat(accumulator.getReasoning()).isEqualTo("Let me think. 2+2 is 4.");
        assertThat(accumulator.getRefusal()).isNull();
        assertThat(accumulator.getAudio()).isNull();
    }

    @Test
    void reasoningDetailsAreMergedInArrivalOrder() {
        accumulator.onData(reasoningDetailsChunk(
                new JSONObject().put("type", "text").put("text", "step 1")));
        accumulator.onData(reasoningDetailsChunk(
                new JSONObject().put("type", "reasoning.encrypted").put("data", "enc")));

        assertThat(accumulator.getReasoningDetails()).hasSize(2);
        assertThat(accumulator.getReasoningDetails().get(0).getString("type")).isEqualTo("text");
        assertThat(accumulator.getReasoningDetails().get(1).getString("type")).isEqualTo("reasoning.encrypted");
    }

    @Test
    void refusalDeltasAreAccumulated() {
        accumulator.onData(refusalChunk("I cannot"));
        accumulator.onData(refusalChunk(" help with that."));
        accumulator.onData(finishChunk("stop"));

        assertThat(accumulator.getRefusal()).isEqualTo("I cannot help with that.");
        assertThat(accumulator.getReasoning()).isNull();
    }

    @Test
    void audioDeltasAreMerged() {
        accumulator.onData(audioChunk("audio_1", "AAAA", 1700000000L, "Hello"));
        accumulator.onData(audioChunk(null, "BBBB", null, " world"));

        JSONObject audio = accumulator.getAudio();
        assertThat(audio.getString("id")).isEqualTo("audio_1");
        assertThat(audio.getString("data")).isEqualTo("AAAABBBB");
        assertThat(audio.getLong("expires_at")).isEqualTo(1700000000L);
        assertThat(audio.getString("transcript")).isEqualTo("Hello world");
    }

    @Test
    void assistantMessageCarriesReasoningRefusalAndAudio() {
        accumulator.onData(reasoningChunk("thinking..."));
        accumulator.onData(contentChunk("Answer"));
        accumulator.onData(refusalChunk("nope"));
        accumulator.onData(audioChunk("audio_1", "AAAA", 1700000000L, "Answer"));
        accumulator.onData(reasoningDetailsChunk(new JSONObject().put("type", "text").put("text", "s")));
        accumulator.onData(finishChunk("stop"));

        JSONObject msg = accumulator.buildAssistantMessage();
        assertThat(msg.getString("reasoning")).isEqualTo("thinking...");
        assertThat(msg.getJSONArray("reasoning_details").length()).isEqualTo(1);
        assertThat(msg.getString("refusal")).isEqualTo("nope");
        assertThat(msg.getJSONObject("audio").getString("data")).isEqualTo("AAAA");
        assertThat(msg.getString("content")).isEqualTo("Answer");
    }

    @Test
    void assistantMessageWithoutReasoningRefusalAudioHasNoKeys() {
        accumulator.onData(contentChunk("Answer"));
        accumulator.onData(finishChunk("stop"));

        JSONObject msg = accumulator.buildAssistantMessage();
        assertThat(msg.has("reasoning")).isFalse();
        assertThat(msg.has("reasoning_details")).isFalse();
        assertThat(msg.has("refusal")).isFalse();
        assertThat(msg.has("audio")).isFalse();
    }

    @Test
    void chunkLevelFieldsAreCaptured() {
        String chunk = new JSONObject()
                .put("service_tier", "priority")
                .put("system_fingerprint", "fp_abc123")
                .put("openrouter_metadata", new JSONObject().put("provider", "Anthropic"))
                .put("choices", new JSONArray().put(new JSONObject()
                        .put("index", 0)
                        .put("delta", new JSONObject().put("content", "Hi"))
                        .put("finish_reason", JSONObject.NULL)))
                .toString();
        accumulator.onData(chunk);

        assertThat(accumulator.getServiceTier()).isEqualTo("priority");
        assertThat(accumulator.getSystemFingerprint()).isEqualTo("fp_abc123");
        assertThat(accumulator.getOpenrouterMetadata().getString("provider")).isEqualTo("Anthropic");
    }

    @Test
    void nullChunkLevelFieldsAreIgnored() {
        String chunk = new JSONObject()
                .put("service_tier", JSONObject.NULL)
                .put("system_fingerprint", JSONObject.NULL)
                .put("openrouter_metadata", JSONObject.NULL)
                .put("choices", new JSONArray())
                .toString();
        accumulator.onData(chunk);

        assertThat(accumulator.getServiceTier()).isNull();
        assertThat(accumulator.getSystemFingerprint()).isNull();
        assertThat(accumulator.getOpenrouterMetadata()).isNull();
    }

    @Test
    void syntheticResponseExposesReasoningRefusalAudioTierMetadataAndFingerprint() {
        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());

        accumulator.onData(reasoningChunk("thinking..."));
        accumulator.onData(contentChunk("Answer"));
        accumulator.onData(refusalChunk("nope"));
        accumulator.onData(audioChunk("audio_1", "AAAA", 1700000000L, "Answer"));
        accumulator.onData(reasoningDetailsChunk(new JSONObject().put("type", "text").put("text", "s")));
        accumulator.onData(tierChunk("priority", "fp_abc123"));
        accumulator.onData(finishChunk("stop"));

        var response = new OpenRouterChatCompletionResponse(
                handler.buildSyntheticResponseJson(accumulator, "test/model"), null);

        // Sync/streaming symmetry: the same accessors must behave identically
        // on the synthetic response as they do on the synchronous one.
        assertThat(response.reasoning()).isEqualTo("thinking...");
        assertThat(response.reasoningDetails()).hasSize(1);
        assertThat(response.reasoningDetails().get(0).getString("text")).isEqualTo("s");
        assertThat(response.refusal()).isEqualTo("nope");
        assertThat(response.hasRefusal()).isTrue();
        assertThat(response.audio().getString("data")).isEqualTo("AAAA");
        assertThat(response.audioId()).isEqualTo("audio_1");
        assertThat(response.audioExpiresAt()).isEqualTo(1700000000L);
        assertThat(response.audioTranscript()).isEqualTo("Answer");
        assertThat(response.serviceTier()).isEqualTo("priority");
        assertThat(response.systemFingerprint()).isEqualTo("fp_abc123");
    }

    @Test
    void syntheticResponseWithoutTierCarriesNoTierKeys() {
        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());

        accumulator.onData(contentChunk("Hello"));
        accumulator.onData(finishChunk("stop"));

        var response = new OpenRouterChatCompletionResponse(
                handler.buildSyntheticResponseJson(accumulator, "test/model"), null);

        assertThat(response.serviceTier()).isNull();
        assertThat(response.systemFingerprint()).isNull();
        assertThat(response.openrouterMetadata()).isNull();
        assertThat(response.hasRefusal()).isFalse();
        assertThat(response.refusal()).isNull();
        assertThat(response.reasoning()).isNull();
        assertThat(response.audio()).isNull();
        assertThat(response.imageUrls()).isEmpty();
    }

    @Test
    void syntheticResponseCarriesTypedMetadataFromTerminalChunk() {
        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());

        accumulator.onData(contentChunk("Hello"));
        // OpenRouter delivers openrouter_metadata on the final chunk before [DONE].
        accumulator.onData(metadataChunk(new JSONObject()
                .put("requested", "openai/gpt-4o-mini")
                .put("strategy", "fallback")
                .put("region", "iad")
                .put("attempt", 2)
                .put("is_byok", true)
                .put("generation_time", 640)
                .put("endpoints", new JSONObject()
                        .put("total", 1)
                        .put("available", new JSONArray().put(new JSONObject()
                                .put("provider", "OpenAI")
                                .put("model", "openai/gpt-4o-mini")
                                .put("selected", true))))
                .put("attempts", new JSONArray().put(new JSONObject()
                        .put("provider", "OpenAI")
                        .put("model", "openai/gpt-4o-mini")
                        .put("status", 200)))));
        accumulator.onData(finishChunk("stop"));

        var response = new OpenRouterChatCompletionResponse(
                handler.buildSyntheticResponseJson(accumulator, "test/model"), null);

        // Sync/streaming symmetry: the typed accessors must behave identically
        // on the synthetic response as they do on the synchronous one.
        assertThat(response.openrouterMetadata()).isNotNull();
        assertThat(response.metadataRequestedModel()).isEqualTo("openai/gpt-4o-mini");
        assertThat(response.metadataRoutingStrategy()).isEqualTo("fallback");
        assertThat(response.metadataRegion()).isEqualTo("iad");
        assertThat(response.metadataAttempt()).isEqualTo(2);
        assertThat(response.metadataIsByok()).isTrue();
        assertThat(response.metadataGenerationTimeMs()).isEqualTo(640L);
        assertThat(response.metadataSelectedProvider()).isEqualTo("OpenAI");
        assertThat(response.metadataAttempts()).hasSize(1);
        assertThat(response.metadataPipeline()).isEmpty();
    }

    @Test
    void resetClearsReasoningRefusalAudioAndChunkLevelFields() {
        accumulator.onData(reasoningChunk("r"));
        accumulator.onData(refusalChunk("f"));
        accumulator.onData(audioChunk("a", "AA", 1L, "t"));
        accumulator.onData(reasoningDetailsChunk(new JSONObject().put("type", "text")));
        accumulator.onData(tierChunk("flex", "fp_x"));

        accumulator.reset();

        assertThat(accumulator.getReasoning()).isNull();
        assertThat(accumulator.getReasoningDetails()).isEmpty();
        assertThat(accumulator.getRefusal()).isNull();
        assertThat(accumulator.getAudio()).isNull();
        assertThat(accumulator.getServiceTier()).isNull();
        assertThat(accumulator.getSystemFingerprint()).isNull();
        assertThat(accumulator.getOpenrouterMetadata()).isNull();
    }

    // --- Per-chunk logprobs ---

    private String logprobsChunk(String token, double logprob, String side) {
        JSONObject tokenLogprob = new JSONObject()
                .put("token", token)
                .put("logprob", logprob)
                .put("bytes", new JSONArray())
                .put("top_logprobs", new JSONArray());
        JSONObject logprobs = new JSONObject()
                .put(side, new JSONArray().put(tokenLogprob));
        return new JSONObject()
                .put("choices", new JSONArray().put(new JSONObject()
                        .put("index", 0)
                        .put("delta", new JSONObject())
                        .put("logprobs", logprobs)
                        .put("finish_reason", JSONObject.NULL)))
                .toString();
    }

    @Test
    void perChunkLogprobsAreAccumulatedInArrivalOrder() {
        accumulator.onData(logprobsChunk("Hel", -0.01, "content"));
        accumulator.onData(logprobsChunk("lo", -0.02, "content"));
        accumulator.onData(logprobsChunk("no", -0.7, "refusal"));
        accumulator.onData(finishChunk("stop"));

        assertThat(accumulator.getContentLogprobTokens()).hasSize(2);
        assertThat(accumulator.getContentLogprobTokens().get(0).getString("token")).isEqualTo("Hel");
        assertThat(accumulator.getContentLogprobTokens().get(1).getString("token")).isEqualTo("lo");
        assertThat(accumulator.getRefusalLogprobTokens()).hasSize(1);
        assertThat(accumulator.getRefusalLogprobTokens().get(0).getString("token")).isEqualTo("no");

        JSONObject logprobs = accumulator.getLogprobs();
        assertThat(logprobs).isNotNull();
        assertThat(logprobs.getJSONArray("content").length()).isEqualTo(2);
        assertThat(logprobs.getJSONArray("refusal").length()).isEqualTo(1);
    }

    @Test
    void syntheticResponseCarriesAccumulatedLogprobs() {
        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());

        accumulator.onData(logprobsChunk("Hi", -0.02, "content"));
        accumulator.onData(logprobsChunk("!", -0.5, "content"));
        accumulator.onData(finishChunk("stop"));

        var response = new OpenRouterChatCompletionResponse(
                handler.buildSyntheticResponseJson(accumulator, "test/model"), null);

        // Sync/streaming symmetry: the typed accessors must behave identically
        // on the synthetic response as they do on the synchronous one.
        assertThat(response.logprobs()).isNotNull();
        assertThat(response.contentLogprobs()).hasSize(2);
        assertThat(response.contentLogprobs().get(0).token()).isEqualTo("Hi");
        assertThat(response.contentLogprobs().get(0).logprob()).isEqualTo(-0.02);
        assertThat(response.contentLogprobs().get(1).token()).isEqualTo("!");
        assertThat(response.refusalLogprobs()).isEmpty();
    }

    @Test
    void syntheticResponseWithoutLogprobsChunksHasNoLogprobsKey() {
        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());

        accumulator.onData(contentChunk("Hello"));
        accumulator.onData(finishChunk("stop"));

        JSONObject synthetic = handler.buildSyntheticResponseJson(accumulator, "test/model");
        assertThat(synthetic.getJSONArray("choices").getJSONObject(0).has("logprobs")).isFalse();

        var response = new OpenRouterChatCompletionResponse(synthetic, null);
        assertThat(response.logprobs()).isNull();
        assertThat(response.contentLogprobs()).isEmpty();
        assertThat(response.refusalLogprobs()).isEmpty();
    }

    @Test
    void resetClearsAccumulatedLogprobs() {
        accumulator.onData(logprobsChunk("Hel", -0.01, "content"));
        accumulator.onData(logprobsChunk("no", -0.7, "refusal"));
        accumulator.reset();

        assertThat(accumulator.getContentLogprobTokens()).isEmpty();
        assertThat(accumulator.getRefusalLogprobTokens()).isEmpty();
        assertThat(accumulator.getLogprobs()).isNull();
    }

    // --- Helpers ---

    private String errorChunk(String message, Integer code, String errorType, String providerCode) {
        JSONObject error = new JSONObject().put("message", message).put("code", code);
        if (errorType != null || providerCode != null) {
            JSONObject metadata = new JSONObject();
            if (errorType != null) {
                metadata.put("error_type", errorType);
            }
            if (providerCode != null) {
                metadata.put("provider_code", providerCode);
            }
            error.put("metadata", metadata);
        }
        return new JSONObject().put("error", error).toString();
    }

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

    private String reasoningChunk(String text) {
        return new JSONObject()
            .put("choices", new JSONArray().put(new JSONObject()
                .put("index", 0)
                .put("delta", new JSONObject().put("reasoning", text))
                .put("finish_reason", JSONObject.NULL)))
            .toString();
    }

    private String reasoningDetailsChunk(JSONObject detail) {
        return new JSONObject()
            .put("choices", new JSONArray().put(new JSONObject()
                .put("index", 0)
                .put("delta", new JSONObject().put("reasoning_details", new JSONArray().put(detail)))
                .put("finish_reason", JSONObject.NULL)))
            .toString();
    }

    private String refusalChunk(String text) {
        return new JSONObject()
            .put("choices", new JSONArray().put(new JSONObject()
                .put("index", 0)
                .put("delta", new JSONObject().put("refusal", text))
                .put("finish_reason", JSONObject.NULL)))
            .toString();
    }

    private String audioChunk(String id, String data, Long expiresAt, String transcript) {
        JSONObject audio = new JSONObject();
        if (id != null) {
            audio.put("id", id);
        }
        if (data != null) {
            audio.put("data", data);
        }
        if (expiresAt != null) {
            audio.put("expires_at", expiresAt);
        }
        if (transcript != null) {
            audio.put("transcript", transcript);
        }
        return new JSONObject()
            .put("choices", new JSONArray().put(new JSONObject()
                .put("index", 0)
                .put("delta", new JSONObject().put("audio", audio))
                .put("finish_reason", JSONObject.NULL)))
            .toString();
    }

    private String tierChunk(String serviceTier, String systemFingerprint) {
        return new JSONObject()
            .put("service_tier", serviceTier)
            .put("system_fingerprint", systemFingerprint)
            .put("choices", new JSONArray().put(new JSONObject()
                .put("index", 0)
                .put("delta", new JSONObject())
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

    private String metadataChunk(JSONObject metadata) {
        return new JSONObject()
            .put("openrouter_metadata", metadata)
            .put("choices", new JSONArray().put(new JSONObject()
                .put("index", 0)
                .put("delta", new JSONObject())
                .put("finish_reason", JSONObject.NULL)))
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

    private String usageChunk(double cost, int prompt, int completion, int total) {
        return new JSONObject()
            .put("id", "gen-xxx")
            .put("choices", new JSONArray())
            .put("usage", new JSONObject()
                .put("cost", cost)
                .put("prompt_tokens", prompt)
                .put("completion_tokens", completion)
                .put("total_tokens", total))
            .toString();
    }

    private String finishChunkWithNativeReason(String reason, String nativeReason) {
        return new JSONObject()
            .put("choices", new JSONArray().put(new JSONObject()
                .put("index", 0)
                .put("delta", new JSONObject())
                .put("finish_reason", reason)
                .put("native_finish_reason", nativeReason)))
            .toString();
    }
}
