package de.entwicklertraining.openrouter4j.chat.completion;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    }

    @Test
    void reasoningIsPropagatedToStreamingFollowUpRequests() throws Exception {
        OpenRouterChatCompletionRequest initial = baseBuilder()
                .reasoningEnabled(false)
                .reasoningEffort("minimal")
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
    }
}
