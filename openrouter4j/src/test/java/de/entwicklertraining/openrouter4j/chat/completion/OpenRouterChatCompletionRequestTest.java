package de.entwicklertraining.openrouter4j.chat.completion;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterToolDefinition;
import de.entwicklertraining.openrouter4j.OpenRouterToolResult;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OpenRouterChatCompletionRequestTest {

    private OpenRouterChatCompletionRequest.Builder baseBuilder() {
        return OpenRouterChatCompletionRequest.builder(new OpenRouterClient())
                .model("test/model")
                .addMessage("user", "Hello");
    }

    private JSONObject bodyOf(OpenRouterChatCompletionRequest.Builder builder) {
        return new JSONObject(builder.build().getBody());
    }

    @Test
    void noProviderObjectWhenNothingIsSet() {
        JSONObject body = bodyOf(baseBuilder());
        assertThat(body.has("provider")).isFalse();
    }

    @Test
    void providerObjectWithOrderOnly() {
        JSONObject body = bodyOf(baseBuilder().provider("alibaba", "deepseek"));

        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getJSONArray("order").toList()).containsExactly("alibaba", "deepseek");
        assertThat(provider.has("require_parameters")).isFalse();
        assertThat(provider.has("allow_fallbacks")).isFalse();
    }

    @Test
    void providerObjectWithRequireParametersOnly() {
        JSONObject body = bodyOf(baseBuilder().requireParameters(true));

        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getBoolean("require_parameters")).isTrue();
        assertThat(provider.has("order")).isFalse();
        assertThat(provider.has("allow_fallbacks")).isFalse();
    }

    @Test
    void providerObjectWithAllowFallbacksOnly() {
        JSONObject body = bodyOf(baseBuilder().allowFallbacks(false));

        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getBoolean("allow_fallbacks")).isFalse();
        assertThat(provider.has("order")).isFalse();
        assertThat(provider.has("require_parameters")).isFalse();
    }

    @Test
    void providerObjectWithAllOptionsCombined() {
        JSONObject body = bodyOf(baseBuilder()
                .provider("alibaba")
                .requireParameters(true)
                .allowFallbacks(false));

        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getJSONArray("order").toList()).containsExactly("alibaba");
        assertThat(provider.getBoolean("require_parameters")).isTrue();
        assertThat(provider.getBoolean("allow_fallbacks")).isFalse();
    }

    @Test
    void explicitFalseRequireParametersIsSerialized() {
        JSONObject body = bodyOf(baseBuilder().requireParameters(false));

        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getBoolean("require_parameters")).isFalse();
    }

    @Test
    void explicitTrueAllowFallbacksIsSerialized() {
        JSONObject body = bodyOf(baseBuilder().allowFallbacks(true));

        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getBoolean("allow_fallbacks")).isTrue();
    }

    @Test
    void requestAccessorsExposeProviderRoutingOptions() {
        OpenRouterChatCompletionRequest request = baseBuilder()
                .provider("alibaba")
                .requireParameters(true)
                .allowFallbacks(false)
                .build();

        assertThat(request.providers()).containsExactly("alibaba");
        assertThat(request.requireParameters()).isTrue();
        assertThat(request.allowFallbacks()).isFalse();
    }

    @Test
    void unsetProviderRoutingOptionsAreNull() {
        OpenRouterChatCompletionRequest request = baseBuilder().build();

        assertThat(request.requireParameters()).isNull();
        assertThat(request.allowFallbacks()).isNull();
    }

    @Test
    void noReasoningKeyWhenNothingIsSet() {
        JSONObject body = bodyOf(baseBuilder());
        assertThat(body.has("reasoning")).isFalse();
    }

    @Test
    void reasoningMaxTokensUsesCurrentApiFormat() {
        JSONObject body = bodyOf(baseBuilder().reasoningMaxTokens(1024));

        JSONObject reasoning = body.getJSONObject("reasoning");
        assertThat(reasoning.getInt("max_tokens")).isEqualTo(1024);
        assertThat(reasoning.has("type")).isFalse();
        assertThat(reasoning.has("budget")).isFalse();
    }

    @Test
    void legacyThinkingMethodAlsoEmitsCurrentFormat() {
        JSONObject body = bodyOf(baseBuilder().thinking(512));

        JSONObject reasoning = body.getJSONObject("reasoning");
        assertThat(reasoning.getInt("max_tokens")).isEqualTo(512);
        assertThat(reasoning.has("type")).isFalse();
        assertThat(reasoning.has("budget")).isFalse();
    }

    @Test
    void reasoningEffortIsEmitted() {
        JSONObject body = bodyOf(baseBuilder().reasoningEffort("high"));

        JSONObject reasoning = body.getJSONObject("reasoning");
        assertThat(reasoning.getString("effort")).isEqualTo("high");
        assertThat(reasoning.has("max_tokens")).isFalse();
    }

    @Test
    void reasoningExcludeAndEnabledAreEmitted() {
        JSONObject body = bodyOf(baseBuilder()
                .reasoningEffort("low")
                .reasoningExclude(true)
                .reasoningEnabled(false));

        JSONObject reasoning = body.getJSONObject("reasoning");
        assertThat(reasoning.getString("effort")).isEqualTo("low");
        assertThat(reasoning.getBoolean("exclude")).isTrue();
        assertThat(reasoning.getBoolean("enabled")).isFalse();
    }

    @Test
    void reasoningAccessorsExposeOptions() {
        OpenRouterChatCompletionRequest request = baseBuilder()
                .reasoningEffort("high")
                .reasoningMaxTokens(2048)
                .reasoningExclude(true)
                .reasoningEnabled(true)
                .build();

        assertThat(request.reasoningEffort()).isEqualTo("high");
        assertThat(request.reasoningMaxTokens()).isEqualTo(2048);
        assertThat(request.reasoningExclude()).isTrue();
        assertThat(request.reasoningEnabled()).isTrue();
        // The deprecated alias must keep returning the same value
        assertThat(request.thinkingBudget()).isEqualTo(2048);
    }

    @Test
    void unsetReasoningOptionsAreNull() {
        OpenRouterChatCompletionRequest request = baseBuilder().build();

        assertThat(request.reasoningEffort()).isNull();
        assertThat(request.reasoningMaxTokens()).isNull();
        assertThat(request.reasoningExclude()).isNull();
        assertThat(request.reasoningEnabled()).isNull();
    }

    @Test
    void reasoningIsPropagatedToToolLoopFollowUpRequests() throws Exception {
        OpenRouterChatCompletionRequest initial = baseBuilder()
                .reasoningEffort("high")
                .reasoningMaxTokens(1024)
                .reasoningExclude(true)
                .requireParameters(true)
                .allowFallbacks(true)
                .build();

        // The real copy path used by the tool-call loop between turns.
        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
        OpenRouterChatCompletionRequest next = handler.buildNextRequest(
                initial,
                List.of(new JSONObject().put("role", "user").put("content", "continue")));

        JSONObject body = new JSONObject(next.getBody());
        JSONObject reasoning = body.getJSONObject("reasoning");
        assertThat(reasoning.getString("effort")).isEqualTo("high");
        assertThat(reasoning.getInt("max_tokens")).isEqualTo(1024);
        assertThat(reasoning.getBoolean("exclude")).isTrue();
        // Regression guard for the copy lists (defect D1): provider routing flags
        // must survive into follow-up requests of the tool-call loop.
        assertThat(body.getJSONObject("provider").getBoolean("require_parameters")).isTrue();
        assertThat(body.getJSONObject("provider").getBoolean("allow_fallbacks")).isTrue();
    }

    @Test
    void allNewOptionsArePropagatedToStreamingFollowUpRequests() throws Exception {
        OpenRouterChatCompletionRequest initial = builderWithTool()
                .reasoningEnabled(false)
                .reasoningEffort("minimal")
                .reasoningMaxTokens(512)
                .reasoningExclude(true)
                .maxCompletionTokens(2048)
                .toolChoiceFunction("get_weather")
                .frequencyPenalty(0.5)
                .seed(42)
                .requireParameters(true)
                .allowFallbacks(true)
                .build();

        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
        OpenRouterChatCompletionRequest next = handler.buildStreamingRequest(
                initial,
                List.of(new JSONObject().put("role", "user").put("content", "continue")),
                new StreamingToolCallAccumulator(new StreamingResponseHandler<String>() {
                    @Override public void onData(String chunk) { }
                    @Override public void onComplete() { }
                    @Override public void onError(Throwable error) { }
                }));

        JSONObject body = new JSONObject(next.getBody());
        JSONObject reasoning = body.getJSONObject("reasoning");
        assertThat(reasoning.getBoolean("enabled")).isFalse();
        assertThat(reasoning.getString("effort")).isEqualTo("minimal");
        assertThat(reasoning.getInt("max_tokens")).isEqualTo(512);
        assertThat(reasoning.getBoolean("exclude")).isTrue();
        assertThat(body.getInt("max_completion_tokens")).isEqualTo(2048);
        assertThat(body.getJSONObject("tool_choice").getJSONObject("function").getString("name"))
                .isEqualTo("get_weather");
        assertThat(body.getDouble("frequency_penalty")).isEqualTo(0.5);
        assertThat(body.getInt("seed")).isEqualTo(42);
        assertThat(body.getJSONObject("provider").getBoolean("require_parameters")).isTrue();
        assertThat(body.getJSONObject("provider").getBoolean("allow_fallbacks")).isTrue();
    }

    @Test
    void maxCompletionTokensIsEmitted() {
        JSONObject body = bodyOf(baseBuilder().maxCompletionTokens(2048));

        assertThat(body.getInt("max_completion_tokens")).isEqualTo(2048);
        assertThat(body.has("max_tokens")).isFalse();
    }

    @Test
    void deprecatedMaxTokensStillEmitted() {
        JSONObject body = bodyOf(baseBuilder().maxOutputTokens(1024));

        assertThat(body.getInt("max_tokens")).isEqualTo(1024);
        assertThat(body.has("max_completion_tokens")).isFalse();
    }

    @Test
    void bothMaxTokenVariantsAreEmittedVerbatimWhenBothSet() {
        JSONObject body = bodyOf(baseBuilder()
                .maxCompletionTokens(2048)
                .maxOutputTokens(1024));

        assertThat(body.getInt("max_completion_tokens")).isEqualTo(2048);
        assertThat(body.getInt("max_tokens")).isEqualTo(1024);
    }

    @Test
    void noMaxTokenKeysWhenUnset() {
        JSONObject body = bodyOf(baseBuilder());

        assertThat(body.has("max_tokens")).isFalse();
        assertThat(body.has("max_completion_tokens")).isFalse();
    }

    @Test
    void maxCompletionTokensAccessorAndPropagation() throws Exception {
        OpenRouterChatCompletionRequest initial = baseBuilder()
                .maxCompletionTokens(2048)
                .maxOutputTokens(1024)
                .build();

        assertThat(initial.maxCompletionTokens()).isEqualTo(2048);

        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
        OpenRouterChatCompletionRequest next = handler.buildNextRequest(
                initial,
                List.of(new JSONObject().put("role", "user").put("content", "continue")));

        JSONObject body = new JSONObject(next.getBody());
        assertThat(body.getInt("max_completion_tokens")).isEqualTo(2048);
        assertThat(body.getInt("max_tokens")).isEqualTo(1024);
    }

    private OpenRouterChatCompletionRequest.Builder builderWithTool() {
        return baseBuilder().addTool(OpenRouterToolDefinition.builder("get_weather")
                .description("Get the current weather of a city")
                .callback(ctx -> OpenRouterToolResult.of(new JSONObject().put("temperature", "20 degrees")))
                .build());
    }

    @Test
    void namedToolChoiceEmitsObjectForm() {
        JSONObject body = bodyOf(builderWithTool().toolChoiceFunction("get_weather"));

        JSONObject toolChoice = body.getJSONObject("tool_choice");
        assertThat(toolChoice.getString("type")).isEqualTo("function");
        assertThat(toolChoice.getJSONObject("function").getString("name")).isEqualTo("get_weather");
    }

    @Test
    void stringToolChoiceKeywordsStillEmitPlainString() {
        JSONObject body = bodyOf(builderWithTool().toolChoice("required"));

        assertThat(body.getString("tool_choice")).isEqualTo("required");
    }

    @Test
    void namedToolChoiceWinsOverStringForm() {
        JSONObject body = bodyOf(builderWithTool()
                .toolChoice("auto")
                .toolChoiceFunction("get_weather"));

        assertThat(body.getJSONObject("tool_choice").getJSONObject("function").getString("name"))
                .isEqualTo("get_weather");
    }

    @Test
    void noToolChoiceEmittedWithoutTools() {
        JSONObject stringForm = bodyOf(baseBuilder().toolChoice("auto"));
        JSONObject namedForm = bodyOf(baseBuilder().toolChoiceFunction("get_weather"));

        assertThat(stringForm.has("tool_choice")).isFalse();
        assertThat(namedForm.has("tool_choice")).isFalse();
    }

    @Test
    void namedToolChoiceAccessorAndPropagation() throws Exception {
        OpenRouterChatCompletionRequest initial = builderWithTool()
                .toolChoiceFunction("get_weather")
                .build();

        assertThat(initial.toolChoiceFunction()).isEqualTo("get_weather");

        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
        OpenRouterChatCompletionRequest next = handler.buildNextRequest(
                initial,
                List.of(new JSONObject().put("role", "user").put("content", "continue")));

        assertThat(new JSONObject(next.getBody()).getJSONObject("tool_choice")
                .getJSONObject("function").getString("name")).isEqualTo("get_weather");
    }

    @Test
    void noSamplingKeysWhenUnset() {
        JSONObject body = bodyOf(baseBuilder());

        assertThat(body.has("frequency_penalty")).isFalse();
        assertThat(body.has("presence_penalty")).isFalse();
        assertThat(body.has("repetition_penalty")).isFalse();
        assertThat(body.has("seed")).isFalse();
        assertThat(body.has("min_p")).isFalse();
        assertThat(body.has("top_a")).isFalse();
        assertThat(body.has("logit_bias")).isFalse();
        assertThat(body.has("logprobs")).isFalse();
        assertThat(body.has("top_logprobs")).isFalse();
    }

    @Test
    void allSamplingParametersAreEmitted() {
        JSONObject body = bodyOf(baseBuilder()
                .frequencyPenalty(0.5)
                .presencePenalty(-1.0)
                .repetitionPenalty(1.2)
                .seed(42)
                .minP(0.05)
                .topA(0.75)
                .logprobs(true)
                .topLogprobs(5));

        assertThat(body.getDouble("frequency_penalty")).isEqualTo(0.5);
        assertThat(body.getDouble("presence_penalty")).isEqualTo(-1.0);
        assertThat(body.getDouble("repetition_penalty")).isEqualTo(1.2);
        assertThat(body.getInt("seed")).isEqualTo(42);
        assertThat(body.getDouble("min_p")).isEqualTo(0.05);
        assertThat(body.getDouble("top_a")).isEqualTo(0.75);
        assertThat(body.getBoolean("logprobs")).isTrue();
        assertThat(body.getInt("top_logprobs")).isEqualTo(5);
    }

    @Test
    void logitBiasIsEmittedAsTokenIdToObject() {
        JSONObject body = bodyOf(baseBuilder()
                .addLogitBias(50256, -100.0)
                .addLogitBias(1234, 0.5));

        JSONObject logitBias = body.getJSONObject("logit_bias");
        assertThat(logitBias.getDouble("50256")).isEqualTo(-100.0);
        assertThat(logitBias.getDouble("1234")).isEqualTo(0.5);
    }

    @Test
    void logitBiasMapReplacesPreviousEntries() {
        JSONObject body = bodyOf(baseBuilder()
                .addLogitBias(1, 1.0)
                .logitBias(Map.of(2, 2.0)));

        JSONObject logitBias = body.getJSONObject("logit_bias");
        assertThat(logitBias.has("1")).isFalse();
        assertThat(logitBias.getDouble("2")).isEqualTo(2.0);
    }

    @Test
    void samplingAccessorsExposeOptions() {
        OpenRouterChatCompletionRequest request = baseBuilder()
                .frequencyPenalty(0.5)
                .presencePenalty(-1.0)
                .repetitionPenalty(1.2)
                .seed(42)
                .minP(0.05)
                .topA(0.75)
                .addLogitBias(50256, -100.0)
                .logprobs(true)
                .topLogprobs(5)
                .build();

        assertThat(request.frequencyPenalty()).isEqualTo(0.5);
        assertThat(request.presencePenalty()).isEqualTo(-1.0);
        assertThat(request.repetitionPenalty()).isEqualTo(1.2);
        assertThat(request.seed()).isEqualTo(42);
        assertThat(request.minP()).isEqualTo(0.05);
        assertThat(request.topA()).isEqualTo(0.75);
        assertThat(request.logitBias()).containsEntry(50256, -100.0);
        assertThat(request.logprobs()).isTrue();
        assertThat(request.topLogprobs()).isEqualTo(5);
    }

    @Test
    void samplingParametersArePropagatedToFollowUpRequests() throws Exception {
        OpenRouterChatCompletionRequest initial = baseBuilder()
                .frequencyPenalty(0.5)
                .presencePenalty(-1.0)
                .repetitionPenalty(1.2)
                .seed(42)
                .minP(0.05)
                .topA(0.75)
                .addLogitBias(50256, -100.0)
                .logprobs(true)
                .topLogprobs(5)
                .build();

        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
        OpenRouterChatCompletionRequest next = handler.buildNextRequest(
                initial,
                List.of(new JSONObject().put("role", "user").put("content", "continue")));

        JSONObject body = new JSONObject(next.getBody());
        assertThat(body.getDouble("frequency_penalty")).isEqualTo(0.5);
        assertThat(body.getDouble("presence_penalty")).isEqualTo(-1.0);
        assertThat(body.getDouble("repetition_penalty")).isEqualTo(1.2);
        assertThat(body.getInt("seed")).isEqualTo(42);
        assertThat(body.getDouble("min_p")).isEqualTo(0.05);
        assertThat(body.getDouble("top_a")).isEqualTo(0.75);
        assertThat(body.getJSONObject("logit_bias").getDouble("50256")).isEqualTo(-100.0);
        assertThat(body.getBoolean("logprobs")).isTrue();
        assertThat(body.getInt("top_logprobs")).isEqualTo(5);
    }
}
