package de.entwicklertraining.openrouter4j.chat.completion;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.OpenRouterAppAttribution;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterDatetimeServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterGenericPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterGenericServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterImageConfig;
import de.entwicklertraining.openrouter4j.OpenRouterPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterStopCondition;
import de.entwicklertraining.openrouter4j.OpenRouterToolDefinition;
import de.entwicklertraining.openrouter4j.OpenRouterToolResult;
import de.entwicklertraining.openrouter4j.OpenRouterWebFetchServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterWebSearchPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterWebSearchServerTool;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                .reasoningSummary("concise")
                .maxCompletionTokens(2048)
                .toolChoiceFunction("get_weather")
                .frequencyPenalty(0.5)
                .seed(42)
                .requireParameters(true)
                .allowFallbacks(true)
                .serviceTier("flex")
                .prediction("known prefix of the answer")
                .cacheControl("1h")
                .promptCacheKey("conversation-42")
                .promptCacheOptions("explicit")
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
        assertThat(body.getJSONObject("reasoning").getString("summary")).isEqualTo("concise");
        assertThat(body.getString("service_tier")).isEqualTo("flex");
        assertThat(body.getJSONObject("prediction").getString("content"))
                .isEqualTo("known prefix of the answer");
        assertThat(body.getJSONObject("cache_control").getString("ttl")).isEqualTo("1h");
        assertThat(body.getString("prompt_cache_key")).isEqualTo("conversation-42");
        assertThat(body.getJSONObject("prompt_cache_options").getString("mode")).isEqualTo("explicit");
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

    // ------------------------------------------------------------------
    // App attribution headers

    @Test
    void attributionHeadersAreEmittedPerRequest() {
    OpenRouterChatCompletionRequest request = baseBuilder()
            .httpReferer("https://myapp.example")
            .appTitle("My App")
            .appCategories("code", "chat")
            .build();

    Map<String, String> headers = request.getAdditionalHeaders();
    assertThat(headers.get("HTTP-Referer")).isEqualTo("https://myapp.example");
    assertThat(headers.get("X-OpenRouter-Title")).isEqualTo("My App");
    assertThat(headers.get("X-OpenRouter-Categories")).isEqualTo("code,chat");
    }

    @Test
    void noAttributionHeadersWhenUnset() {
    OpenRouterChatCompletionRequest request = baseBuilder().build();
    Map<String, String> headers = request.getAdditionalHeaders();
    assertThat(headers.containsKey("HTTP-Referer")).isFalse();
    assertThat(headers.containsKey("X-OpenRouter-Title")).isFalse();
    assertThat(headers.containsKey("X-OpenRouter-Categories")).isFalse();
    }

    @Test
    void clientLevelAttributionIsUsedAsFallback() {
    OpenRouterClient client = new OpenRouterClient();
    client.appAttribution(OpenRouterAppAttribution.builder()
            .httpReferer("https://client.example")
            .appTitle("Client App")
            .categories("client-cat")
            .build());

    OpenRouterChatCompletionRequest request = OpenRouterChatCompletionRequest.builder(client)
            .model("test/model")
            .addMessage("user", "Hello")
            .build();

    Map<String, String> headers = request.getAdditionalHeaders();
    assertThat(headers.get("HTTP-Referer")).isEqualTo("https://client.example");
    assertThat(headers.get("X-OpenRouter-Title")).isEqualTo("Client App");
    assertThat(headers.get("X-OpenRouter-Categories")).isEqualTo("client-cat");
    }

    @Test
    void perRequestAttributionWinsOverClientLevel() {
    OpenRouterClient client = new OpenRouterClient();
    client.appAttribution(OpenRouterAppAttribution.builder()
            .httpReferer("https://client.example")
            .appTitle("Client App")
            .build());

    OpenRouterChatCompletionRequest request = OpenRouterChatCompletionRequest.builder(client)
            .model("test/model")
            .addMessage("user", "Hello")
            .httpReferer("https://request.example")
            .appTitle("Request App")
            .build();

    Map<String, String> headers = request.getAdditionalHeaders();
    assertThat(headers.get("HTTP-Referer")).isEqualTo("https://request.example");
    assertThat(headers.get("X-OpenRouter-Title")).isEqualTo("Request App");
    }

    @Test
    void moreThanTwoCategoriesAreRejected() {
    assertThatThrownBy(() -> baseBuilder().appCategories("a", "b", "c"))
            .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> OpenRouterAppAttribution.builder()
            .addCategory("a").addCategory("b").addCategory("c").build())
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void attributionAccessorsExposeOptions() {
    OpenRouterChatCompletionRequest request = baseBuilder()
            .httpReferer("https://myapp.example")
            .appTitle("My App")
            .appCategories("code")
            .build();

    assertThat(request.httpReferer()).isEqualTo("https://myapp.example");
    assertThat(request.appTitle()).isEqualTo("My App");
    assertThat(request.appCategories()).containsExactly("code");
    assertThat(request.appCategories()).isNotNull();
    }

    // ------------------------------------------------------------------
    // Model fallback list

    @Test
    void modelsListIsEmitted() {
    JSONObject body = bodyOf(baseBuilder().models("model/a", "model/b"));
    assertThat(body.getJSONArray("models").toList()).containsExactly("model/a", "model/b");
    assertThat(body.has("model")).isTrue();
    }

    @Test
    void noModelsKeyWhenUnset() {
    assertThat(bodyOf(baseBuilder()).has("models")).isFalse();
    assertThat(bodyOf(baseBuilder().models(new String[0])).has("models")).isFalse();
    }

    @Test
    void modelsAccessorAndPropagation() throws Exception {
    OpenRouterChatCompletionRequest initial = baseBuilder()
            .model("model/primary")
            .models("model/a", "model/b")
            .build();

    assertThat(initial.models()).containsExactly("model/a", "model/b");
    assertThat(initial.model()).isEqualTo("model/primary");

    var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
    OpenRouterChatCompletionRequest next = handler.buildNextRequest(
            initial,
            List.of(new JSONObject().put("role", "user").put("content", "continue")));

    JSONObject body = new JSONObject(next.getBody());
    assertThat(body.getJSONArray("models").toList()).containsExactly("model/a", "model/b");
    assertThat(body.getString("model")).isEqualTo("model/primary");
    }

    // ------------------------------------------------------------------
    // Observability: metadata, user, session_id, metadata opt-in

    @Test
    void observabilityFieldsAreEmitted() {
    Map<String, String> meta = new HashMap<>();
    meta.put("request_id", "abc-123");
    meta.put("tenant", "acme");

    JSONObject body = bodyOf(baseBuilder()
            .metadata(meta)
            .user("end-user-42")
            .sessionId("session-abc"));

    assertThat(body.getJSONObject("metadata").getString("request_id")).isEqualTo("abc-123");
    assertThat(body.getJSONObject("metadata").getString("tenant")).isEqualTo("acme");
    assertThat(body.getString("user")).isEqualTo("end-user-42");
    assertThat(body.getString("session_id")).isEqualTo("session-abc");
    }

    @Test
    void noObservabilityKeysWhenUnset() {
    JSONObject body = bodyOf(baseBuilder());
    assertThat(body.has("metadata")).isFalse();
    assertThat(body.has("user")).isFalse();
    assertThat(body.has("session_id")).isFalse();
    }

    @Test
    void sessionIdAlsoSetsHeader() {
    OpenRouterChatCompletionRequest request = baseBuilder().sessionId("session-abc").build();
    assertThat(request.getAdditionalHeaders().get("x-session-id")).isEqualTo("session-abc");
    assertThat(new JSONObject(request.getBody()).getString("session_id")).isEqualTo("session-abc");
    }

    @Test
    void noSessionHeaderWhenUnset() {
    assertThat(baseBuilder().build().getAdditionalHeaders().containsKey("x-session-id")).isFalse();
    }

    @Test
    void metadataInResponseOptInSetsHeader() {
    OpenRouterChatCompletionRequest optedIn = baseBuilder().metadataInResponse(true).build();
    assertThat(optedIn.getAdditionalHeaders().get("X-OpenRouter-Metadata")).isEqualTo("enabled");
    assertThat(optedIn.metadataInResponse()).isTrue();

    OpenRouterChatCompletionRequest optedOut = baseBuilder().metadataInResponse(false).build();
    assertThat(optedOut.getAdditionalHeaders().containsKey("X-OpenRouter-Metadata")).isFalse();
    assertThat(optedIn.getBody()).doesNotContain("X-OpenRouter-Metadata");
    }

    @Test
    void metadataValidationRejectsLimitViolations() {
    Map<String, String> tooMany = new HashMap<>();
    for (int i = 0; i < 17; i++) {
        tooMany.put("key" + i, "v");
    }
    assertThatThrownBy(() -> baseBuilder().metadata(tooMany))
            .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> baseBuilder().metadata(Map.of("k".repeat(65), "v")))
            .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> baseBuilder().metadata(Map.of("k", "v".repeat(513))))
            .isInstanceOf(IllegalArgumentException.class);
    var builder = baseBuilder();
    for (int i = 0; i < 16; i++) {
    builder.addMetadata("key" + i, "v");
    }
    assertThatThrownBy(() -> builder.addMetadata("one-too-many", "v"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("16");
    }

    @Test
    void observabilityAccessorsExposeOptions() {
    OpenRouterChatCompletionRequest request = baseBuilder()
            .addMetadata("k", "v")
            .user("end-user-42")
            .sessionId("session-abc")
            .metadataInResponse(true)
            .build();

    assertThat(request.metadata()).containsEntry("k", "v");
    assertThat(request.metadata()).isNotNull();
    assertThat(request.user()).isEqualTo("end-user-42");
    assertThat(request.sessionId()).isEqualTo("session-abc");
    assertThat(request.metadataInResponse()).isTrue();
    }

    // ------------------------------------------------------------------
    // Provider preferences (extended provider object)

    @Test
    void newProviderPreferencesAreEmitted() {
    JSONObject body = bodyOf(baseBuilder()
            .dataCollection("deny")
            .ignoreProviders("provider1", "provider2")
            .onlyProviders("provider3")
            .maxPrice("0.5", "1.5", "2.5", "3.5")
            .quantizations("int4", "fp8")
            .sort("latency")
            .enforceDistillableText(true));

    JSONObject provider = body.getJSONObject("provider");
    assertThat(provider.getString("data_collection")).isEqualTo("deny");
    assertThat(provider.getJSONArray("ignore").toList()).containsExactly("provider1", "provider2");
    assertThat(provider.getJSONArray("only").toList()).containsExactly("provider3");
    JSONObject maxPrice = provider.getJSONObject("max_price");
    assertThat(maxPrice.getString("prompt")).isEqualTo("0.5");
    assertThat(maxPrice.getString("completion")).isEqualTo("1.5");
    assertThat(maxPrice.getString("image")).isEqualTo("2.5");
    assertThat(maxPrice.getString("audio")).isEqualTo("3.5");
    assertThat(provider.getJSONArray("quantizations").toList()).containsExactly("int4", "fp8");
    assertThat(provider.getString("sort")).isEqualTo("latency");
    assertThat(provider.getBoolean("enforce_distillable_text")).isTrue();
    }

    @Test
    void providerObjectEmittedWhenOnlyNewPreferenceSet() {
    assertThat(bodyOf(baseBuilder().dataCollection("deny")).getJSONObject("provider")
            .getString("data_collection")).isEqualTo("deny");
    assertThat(bodyOf(baseBuilder().sort("price")).getJSONObject("provider")
            .getString("sort")).isEqualTo("price");
    assertThat(bodyOf(baseBuilder().maxPrice("0.5", "1.5")).getJSONObject("provider")
            .getJSONObject("max_price").getString("prompt")).isEqualTo("0.5");
    }

    @Test
    void maxPriceTwoArgFormLeavesImageAudioUnset() {
    JSONObject maxPrice = bodyOf(baseBuilder().maxPrice("0.5", "1.5"))
            .getJSONObject("provider").getJSONObject("max_price");
    assertThat(maxPrice.has("image")).isFalse();
    assertThat(maxPrice.has("audio")).isFalse();
    }

    @Test
    void noProviderObjectWhenNothingIsSetIncludingNewPreferences() {
    JSONObject body = bodyOf(baseBuilder());
    assertThat(body.has("provider")).isFalse();
    }

    @Test
    void explicitFalseEnforceDistillableTextIsSerialized() {
    JSONObject provider = bodyOf(baseBuilder().enforceDistillableText(false))
            .getJSONObject("provider");
    assertThat(provider.getBoolean("enforce_distillable_text")).isFalse();
    }

    @Test
    void providerPreferenceAccessorsExposeOptions() {
    OpenRouterChatCompletionRequest request = baseBuilder()
            .dataCollection("allow")
            .addIgnoreProvider("p1")
            .addOnlyProvider("p2")
            .maxPrice("0.5", "1.5")
            .quantizations("int4")
            .sort("throughput")
            .enforceDistillableText(false)
            .build();

    assertThat(request.dataCollection()).isEqualTo("allow");
    assertThat(request.ignoreProviders()).containsExactly("p1");
    assertThat(request.onlyProviders()).containsExactly("p2");
    assertThat(request.maxPricePrompt()).isEqualTo("0.5");
    assertThat(request.maxPriceCompletion()).isEqualTo("1.5");
    assertThat(request.maxPriceImage()).isNull();
    assertThat(request.maxPriceAudio()).isNull();
    assertThat(request.quantizations()).containsExactly("int4");
    assertThat(request.sort()).isEqualTo("throughput");
    assertThat(request.enforceDistillableText()).isFalse();
    }

    @Test
    void unsetProviderPreferencesAreEmptyOrNull() {
    OpenRouterChatCompletionRequest request = baseBuilder().build();
    assertThat(request.dataCollection()).isNull();
    assertThat(request.ignoreProviders()).isEmpty();
    assertThat(request.onlyProviders()).isEmpty();
    assertThat(request.maxPricePrompt()).isNull();
    assertThat(request.maxPriceCompletion()).isNull();
    assertThat(request.maxPriceImage()).isNull();
    assertThat(request.maxPriceAudio()).isNull();
    assertThat(request.quantizations()).isEmpty();
    assertThat(request.sort()).isNull();
    assertThat(request.enforceDistillableText()).isNull();
    }

    // ------------------------------------------------------------------
    // Plugins, modalities, image_config

    @Test
    void webPluginIsSerializedWithExplicitFieldsOnly() {
        JSONObject body = bodyOf(baseBuilder()
                .addPlugin(OpenRouterWebSearchPlugin.builder()
                        .maxResults(5)
                        .engine("exa")
                        .searchPrompt("Search the web")
                        .build()));

        assertThat(body.has("plugins")).isTrue();
        assertThat(body.getJSONArray("plugins").length()).isEqualTo(1);
        JSONObject plugin = body.getJSONArray("plugins").getJSONObject(0);
        assertThat(plugin.getString("id")).isEqualTo("web");
        assertThat(plugin.getInt("max_results")).isEqualTo(5);
        assertThat(plugin.getString("engine")).isEqualTo("exa");
        assertThat(plugin.getString("search_prompt")).isEqualTo("Search the web");
        assertThat(plugin.has("enabled")).isFalse();
        assertThat(plugin.has("mode")).isFalse();
        assertThat(plugin.has("include_domains")).isFalse();
        assertThat(plugin.has("exclude_domains")).isFalse();
        assertThat(plugin.has("user_location")).isFalse();
    }

    @Test
    void webPluginWithAllFieldsIsSerialized() {
        JSONObject body = bodyOf(baseBuilder().plugins(
                OpenRouterWebSearchPlugin.builder()
                        .enabled(false)
                        .engine("native")
                        .maxResults(3)
                        .maxUses(2)
                        .mode("deep")
                        .searchPrompt("prompt")
                        .includeDomains(List.of("example.com"))
                        .excludeDomains(List.of("*.substack.com"))
                        .userLocation(OpenRouterWebSearchPlugin.UserLocation.builder()
                                .city("Cologne")
                                .country("DE")
                                .build())
                        .build()));

        JSONObject plugin = body.getJSONArray("plugins").getJSONObject(0);
        assertThat(plugin.getString("id")).isEqualTo("web");
        assertThat(plugin.getBoolean("enabled")).isFalse();
        assertThat(plugin.getString("engine")).isEqualTo("native");
        assertThat(plugin.getInt("max_results")).isEqualTo(3);
        assertThat(plugin.getInt("max_uses")).isEqualTo(2);
        assertThat(plugin.getString("mode")).isEqualTo("deep");
        assertThat(plugin.getString("search_prompt")).isEqualTo("prompt");
        assertThat(plugin.getJSONArray("include_domains").toList()).containsExactly("example.com");
        assertThat(plugin.getJSONArray("exclude_domains").toList()).containsExactly("*.substack.com");
        JSONObject location = plugin.getJSONObject("user_location");
        assertThat(location.getString("type")).isEqualTo("approximate");
        assertThat(location.getString("city")).isEqualTo("Cologne");
        assertThat(location.getString("country")).isEqualTo("DE");
        assertThat(location.has("region")).isFalse();
        assertThat(location.has("timezone")).isFalse();
    }

    @Test
    void genericPluginIsSerializedVerbatim() {
        JSONObject body = bodyOf(baseBuilder().plugins(
                OpenRouterPlugin.of("file-parser").withOption("pdf", "some-parsing-engine")));

        assertThat(body.getJSONArray("plugins").length()).isEqualTo(1);
        JSONObject plugin = body.getJSONArray("plugins").getJSONObject(0);
        assertThat(plugin.getString("id")).isEqualTo("file-parser");
        assertThat(plugin.getString("pdf")).isEqualTo("some-parsing-engine");
    }

    @Test
    void pluginsAreOmittedWhenUnset() {
        JSONObject body = bodyOf(baseBuilder());
        assertThat(body.has("plugins")).isFalse();
    }

    @Test
    void pluginsCanBeClearedWithEmptyList() {
        OpenRouterChatCompletionRequest request = baseBuilder()
                .addPlugin(OpenRouterPlugin.of("web"))
                .plugins(List.of())
                .build();
        assertThat(new JSONObject(request.getBody()).has("plugins")).isFalse();
        assertThat(request.plugins()).isEmpty();
    }

    @Test
    void modalitiesCanBeClearedWithEmptyList() {
        OpenRouterChatCompletionRequest request = baseBuilder()
                .modalities("text", "image")
                .modalities(List.of())
                .build();
        assertThat(new JSONObject(request.getBody()).has("modalities")).isFalse();
        assertThat(request.modalities()).isEmpty();
    }

    @Test
    void genericPluginRejectsIdOverride() {
        assertThatThrownBy(() -> OpenRouterPlugin.of("web").withOption("id", "x"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterGenericPlugin("web", java.util.Map.of("id", "x")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void modalitiesAreSerializedAndOmittedWhenUnset() {
        assertThat(bodyOf(baseBuilder()).has("modalities")).isFalse();

        JSONObject body = bodyOf(baseBuilder().modalities("text", "image"));
        assertThat(body.getJSONArray("modalities").toList()).containsExactly("text", "image");
        assertThat(baseBuilder().modalities("text", "image").build().modalities())
                .containsExactly("text", "image");
    }

    @Test
    void imageConfigIsSerializedAndOmittedWhenUnset() {
        assertThat(bodyOf(baseBuilder()).has("image_config")).isFalse();

        JSONObject body = bodyOf(baseBuilder().imageConfig(OpenRouterImageConfig.builder()
                .numImages(2)
                .aspectRatio("16:9")
                .resolution("2K")
                .build()));

        JSONObject imageConfig = body.getJSONObject("image_config");
        assertThat(imageConfig.getInt("num_images")).isEqualTo(2);
        assertThat(imageConfig.getString("aspect_ratio")).isEqualTo("16:9");
        assertThat(imageConfig.getString("resolution")).isEqualTo("2K");
    }

    @Test
    void imageConfigSupportsArbitraryProviderOptions() {
        JSONObject body = bodyOf(baseBuilder().imageConfig(OpenRouterImageConfig.builder()
                .option("quality", "high")
                .build()));

        assertThat(body.getJSONObject("image_config").getString("quality")).isEqualTo("high");
    }

    // ------------------------------------------------------------------
    // Every new option must survive the tool-loop copy path (sync + streaming)

    @Test
    void allNewOptionsArePropagatedToSyncFollowUpRequests() throws Exception {
    OpenRouterChatCompletionRequest initial = builderWithTool()
            .models("model/a")
            .httpReferer("https://myapp.example")
            .appTitle("My App")
            .appCategories("code")
            .addMetadata("k", "v")
            .user("end-user-42")
            .sessionId("session-abc")
            .metadataInResponse(true)
            .dataCollection("deny")
            .ignoreProviders("p1")
            .onlyProviders("p2")
            .maxPrice("0.5", "1.5")
            .quantizations("int4")
            .sort("latency")
            .enforceDistillableText(true)
            .addPlugin(OpenRouterWebSearchPlugin.builder().maxResults(5).build())
            .modalities("text", "image")
            .imageConfig(OpenRouterImageConfig.builder().aspectRatio("1:1").build())
            .build();

    var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
    OpenRouterChatCompletionRequest next = handler.buildNextRequest(
            initial,
            List.of(new JSONObject().put("role", "user").put("content", "continue")));

    JSONObject body = new JSONObject(next.getBody());
    assertThat(body.getJSONArray("models").toList()).containsExactly("model/a");
    assertThat(body.getJSONObject("metadata").getString("k")).isEqualTo("v");
    assertThat(body.getString("user")).isEqualTo("end-user-42");
    assertThat(body.getString("session_id")).isEqualTo("session-abc");
    JSONObject provider = body.getJSONObject("provider");
    assertThat(provider.getString("data_collection")).isEqualTo("deny");
    assertThat(provider.getJSONArray("ignore").toList()).containsExactly("p1");
    assertThat(provider.getJSONArray("only").toList()).containsExactly("p2");
    assertThat(provider.getJSONObject("max_price").getString("prompt")).isEqualTo("0.5");
    assertThat(provider.getJSONArray("quantizations").toList()).containsExactly("int4");
    assertThat(provider.getString("sort")).isEqualTo("latency");
    assertThat(provider.getBoolean("enforce_distillable_text")).isTrue();
    assertThat(body.getJSONArray("plugins").getJSONObject(0).getString("id")).isEqualTo("web");
    assertThat(body.getJSONArray("plugins").getJSONObject(0).getInt("max_results")).isEqualTo(5);
    assertThat(body.getJSONArray("modalities").toList()).containsExactly("text", "image");
    assertThat(body.getJSONObject("image_config").getString("aspect_ratio")).isEqualTo("1:1");

    Map<String, String> headers = next.getAdditionalHeaders();
    assertThat(headers.get("HTTP-Referer")).isEqualTo("https://myapp.example");
    assertThat(headers.get("X-OpenRouter-Title")).isEqualTo("My App");
    assertThat(headers.get("X-OpenRouter-Categories")).isEqualTo("code");
    assertThat(headers.get("x-session-id")).isEqualTo("session-abc");
    assertThat(headers.get("X-OpenRouter-Metadata")).isEqualTo("enabled");
    }

    @Test
    void allAttributionAndRoutingOptionsArePropagatedToStreamingFollowUpRequests() throws Exception {
    OpenRouterChatCompletionRequest initial = builderWithTool()
            .models("model/a")
            .httpReferer("https://myapp.example")
            .appTitle("My App")
            .sessionId("session-abc")
            .metadataInResponse(true)
            .dataCollection("deny")
            .sort("latency")
            .addPlugin(OpenRouterPlugin.of("web"))
            .modalities("text")
            .imageConfig(OpenRouterImageConfig.builder().numImages(1).build())
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

    assertThat(next.stream()).isTrue();
    assertThat(next.isStreamingEnabled()).isTrue();

    JSONObject body = new JSONObject(next.getBody());
    assertThat(body.getJSONArray("models").toList()).containsExactly("model/a");
    assertThat(body.getString("session_id")).isEqualTo("session-abc");
    assertThat(body.getJSONObject("provider").getString("data_collection")).isEqualTo("deny");
    assertThat(body.getJSONObject("provider").getString("sort")).isEqualTo("latency");
    assertThat(body.getJSONArray("plugins").getJSONObject(0).getString("id")).isEqualTo("web");
    assertThat(body.getJSONArray("modalities").toList()).containsExactly("text");
    assertThat(body.getJSONObject("image_config").getInt("num_images")).isEqualTo(1);

    Map<String, String> headers = next.getAdditionalHeaders();
    assertThat(headers.get("HTTP-Referer")).isEqualTo("https://myapp.example");
    assertThat(headers.get("X-OpenRouter-Title")).isEqualTo("My App");
    assertThat(headers.get("x-session-id")).isEqualTo("session-abc");
    assertThat(headers.get("X-OpenRouter-Metadata")).isEqualTo("enabled");
    }

    // ------------------------------------------------------------------
    // Server tools in the tools array, server-tool tool_choice, stop_server_tools_when

    @Test
    void serverToolIsEmittedIntoToolsArray() {
        JSONObject body = bodyOf(baseBuilder()
                .addServerTool(OpenRouterWebSearchServerTool.builder()
                        .maxResults(5)
                        .engine("exa")
                        .build()));

        assertThat(body.has("tools")).isTrue();
        assertThat(body.getJSONArray("tools").length()).isEqualTo(1);
        JSONObject tool = body.getJSONArray("tools").getJSONObject(0);
        assertThat(tool.getString("type")).isEqualTo("openrouter:web_search");
        assertThat(tool.getJSONObject("parameters").getInt("max_results")).isEqualTo(5);
        assertThat(tool.getJSONObject("parameters").getString("engine")).isEqualTo("exa");
        assertThat(tool.getJSONObject("parameters").has("max_uses")).isFalse();
    }

    @Test
    void serverToolWithoutConfigurationEmitsTypeOnly() {
        JSONObject body = bodyOf(baseBuilder()
                .addServerTool(OpenRouterDatetimeServerTool.unconfigured()));

        JSONObject tool = body.getJSONArray("tools").getJSONObject(0);
        assertThat(tool.getString("type")).isEqualTo("openrouter:datetime");
        assertThat(tool.has("parameters")).isFalse();
    }

    @Test
    void serverToolAndFunctionToolCanBeMixed() {
        JSONObject body = bodyOf(builderWithTool()
                .addServerTool(OpenRouterWebFetchServerTool.builder()
                        .maxUses(10)
                        .maxContentTokens(100_000)
                        .allowedDomains(List.of("example.com"))
                        .build())
                .addServerTool(OpenRouterServerTool.of("openrouter:bash")
                        .withOption("parameters", new JSONObject().put("engine", "openrouter"))));

        assertThat(body.getJSONArray("tools").length()).isEqualTo(3);
        JSONObject functionTool = body.getJSONArray("tools").getJSONObject(0);
        assertThat(functionTool.getString("type")).isEqualTo("function");
        assertThat(functionTool.getJSONObject("function").getString("name")).isEqualTo("get_weather");
        JSONObject webFetch = body.getJSONArray("tools").getJSONObject(1);
        assertThat(webFetch.getString("type")).isEqualTo("openrouter:web_fetch");
        assertThat(webFetch.getJSONObject("parameters").getInt("max_uses")).isEqualTo(10);
        assertThat(webFetch.getJSONObject("parameters").getInt("max_content_tokens")).isEqualTo(100_000);
        assertThat(webFetch.getJSONObject("parameters").getJSONArray("allowed_domains").toList())
                .containsExactly("example.com");
        JSONObject bash = body.getJSONArray("tools").getJSONObject(2);
        assertThat(bash.getString("type")).isEqualTo("openrouter:bash");
        assertThat(bash.getJSONObject("parameters").getString("engine")).isEqualTo("openrouter");
    }

    @Test
    void genericServerToolEmitsVerbatim() {
        JSONObject body = bodyOf(baseBuilder()
                .addServerTool(OpenRouterServerTool.of("openrouter:subagent")
                        .withOption("model", "openai/gpt-4o-mini")
                        .withOption("description", "delegate")));

        JSONObject tool = body.getJSONArray("tools").getJSONObject(0);
        assertThat(tool.getString("type")).isEqualTo("openrouter:subagent");
        assertThat(tool.getString("model")).isEqualTo("openai/gpt-4o-mini");
        assertThat(tool.getString("description")).isEqualTo("delegate");
    }

    @Test
    void genericServerToolRejectsTypeOverride() {
        assertThatThrownBy(() -> new OpenRouterGenericServerTool("openrouter:bash",
                java.util.Map.of("type", "function")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toolChoiceAndParallelToolCallsEmittedWithServerToolsPresent() {
        JSONObject body = bodyOf(baseBuilder()
                .addServerTool(OpenRouterServerTool.of("openrouter:web_search"))
                .toolChoice("auto")
                .parallelToolCalls(true));

        assertThat(body.getString("tool_choice")).isEqualTo("auto");
        assertThat(body.getBoolean("parallel_tool_calls")).isTrue();
    }

    @Test
    void toolChoiceNotEmittedWithoutAnyTools() {
        JSONObject body = bodyOf(baseBuilder().toolChoice("auto").parallelToolCalls(true));
        assertThat(body.has("tools")).isFalse();
        assertThat(body.has("tool_choice")).isFalse();
        assertThat(body.has("parallel_tool_calls")).isFalse();
    }

    @Test
    void webSearchServerToolWithAllSettersIsSerialized() {
        JSONObject body = bodyOf(baseBuilder().addServerTool(
                OpenRouterWebSearchServerTool.builder()
                        .engine("parallel")
                        .maxResults(5)
                        .maxTotalResults(50)
                        .maxUses(3)
                        .maxCharacters(2000)
                        .mode("advanced")
                        .searchContextSize("high")
                        .allowedDomains(List.of("example.com"))
                        .excludedDomains(List.of("*.ads.example.com"))
                        .option("future_key", "future_value")
                        .build()));

        JSONObject parameters = body.getJSONArray("tools").getJSONObject(0).getJSONObject("parameters");
        assertThat(parameters.getString("engine")).isEqualTo("parallel");
        assertThat(parameters.getInt("max_results")).isEqualTo(5);
        assertThat(parameters.getInt("max_total_results")).isEqualTo(50);
        assertThat(parameters.getInt("max_uses")).isEqualTo(3);
        assertThat(parameters.getInt("max_characters")).isEqualTo(2000);
        assertThat(parameters.getString("mode")).isEqualTo("advanced");
        assertThat(parameters.getString("search_context_size")).isEqualTo("high");
        assertThat(parameters.getJSONArray("allowed_domains").toList()).containsExactly("example.com");
        assertThat(parameters.getJSONArray("excluded_domains").toList()).containsExactly("*.ads.example.com");
        assertThat(parameters.getString("future_key")).isEqualTo("future_value");
    }

    @Test
    void webSearchServerToolMaxUsesEmittedWhenSet() {
        JSONObject body = bodyOf(baseBuilder().addServerTool(
                OpenRouterWebSearchServerTool.builder().maxUses(3).build()));

        JSONObject parameters = body.getJSONArray("tools").getJSONObject(0).getJSONObject("parameters");
        assertThat(parameters.getInt("max_uses")).isEqualTo(3);
    }

    @Test
    void webFetchServerToolWithAllSettersIsSerialized() {
        JSONObject body = bodyOf(baseBuilder().addServerTool(
                OpenRouterWebFetchServerTool.builder()
                        .maxUses(10)
                        .maxContentTokens(100_000)
                        .allowedDomains(List.of("example.com"))
                        .blockedDomains(List.of("internal.example.com"))
                        .engine("exa")
                        .option("future_key", 42)
                        .build()));

        JSONObject parameters = body.getJSONArray("tools").getJSONObject(0).getJSONObject("parameters");
        assertThat(parameters.getInt("max_uses")).isEqualTo(10);
        assertThat(parameters.getInt("max_content_tokens")).isEqualTo(100_000);
        assertThat(parameters.getJSONArray("allowed_domains").toList()).containsExactly("example.com");
        assertThat(parameters.getJSONArray("blocked_domains").toList()).containsExactly("internal.example.com");
        assertThat(parameters.getString("engine")).isEqualTo("exa");
        assertThat(parameters.getInt("future_key")).isEqualTo(42);
    }

    @Test
    void datetimeServerToolWithTimezoneAndOptionIsSerialized() {
        JSONObject body = bodyOf(baseBuilder().addServerTool(
                OpenRouterDatetimeServerTool.builder()
                        .timezone("America/New_York")
                        .option("future_key", "future_value")
                        .build()));

        JSONObject parameters = body.getJSONArray("tools").getJSONObject(0).getJSONObject("parameters");
        assertThat(parameters.getString("timezone")).isEqualTo("America/New_York");
        assertThat(parameters.getString("future_key")).isEqualTo("future_value");
    }

    @Test
    void serverToolListVariantAndAddStopConditionVariantReachTheJson() {
        OpenRouterChatCompletionRequest request = baseBuilder()
                .serverTools(List.of(
                        OpenRouterWebSearchServerTool.builder().maxResults(2).build(),
                        OpenRouterServerTool.of("openrouter:datetime")))
                .addStopServerToolsWhen(OpenRouterStopCondition.stepCountIs(2))
                .build();

        assertThat(request.serverTools()).hasSize(2);

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getJSONArray("tools").length()).isEqualTo(2);
        assertThat(body.getJSONArray("tools").getJSONObject(0)
                .getJSONObject("parameters").getInt("max_results")).isEqualTo(2);
        assertThat(body.getJSONArray("tools").getJSONObject(1).getString("type"))
                .isEqualTo("openrouter:datetime");
        assertThat(body.getJSONArray("stop_server_tools_when").getJSONObject(0).getInt("step_count"))
                .isEqualTo(2);
    }

    @Test
    void serverToolChoiceEmitsObjectForm() {
        JSONObject body = bodyOf(builderWithTool().toolChoiceServerTool("openrouter:web_search"));

        JSONObject toolChoice = body.getJSONObject("tool_choice");
        assertThat(toolChoice.getString("type")).isEqualTo("openrouter:web_search");
        assertThat(toolChoice.has("function")).isFalse();
    }

    @Test
    void serverToolChoiceFormWinsOverStringForm() {
        JSONObject body = bodyOf(builderWithTool()
                .toolChoice("auto")
                .toolChoiceServerTool("web_search"));

        assertThat(body.getJSONObject("tool_choice").getString("type")).isEqualTo("web_search");
    }

    @Test
    void namedFunctionChoiceWinsOverServerToolChoiceForm() {
        JSONObject body = bodyOf(builderWithTool()
                .toolChoiceServerTool("openrouter:web_search")
                .toolChoiceFunction("get_weather"));

        assertThat(body.getJSONObject("tool_choice").getJSONObject("function").getString("name"))
                .isEqualTo("get_weather");
    }

    @Test
    void serverToolChoiceNotEmittedWithoutTools() {
        JSONObject body = bodyOf(baseBuilder().toolChoiceServerTool("openrouter:web_search"));
        assertThat(body.has("tool_choice")).isFalse();
    }

    @Test
    void serverToolChoiceAccessorAndPropagation() {
        OpenRouterChatCompletionRequest initial = builderWithTool()
                .toolChoiceServerTool("openrouter:web_search")
                .build();

        assertThat(initial.toolChoiceServerTool()).isEqualTo("openrouter:web_search");

        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
        OpenRouterChatCompletionRequest next = handler.buildNextRequest(
                initial,
                List.of(new JSONObject().put("role", "user").put("content", "continue")));

        assertThat(new JSONObject(next.getBody()).getJSONObject("tool_choice").getString("type"))
                .isEqualTo("openrouter:web_search");
    }

    @Test
    void stopServerToolsWhenIsEmittedWithTypedConditions() {
        JSONObject body = bodyOf(baseBuilder()
                .stopServerToolsWhen(
                        OpenRouterStopCondition.stepCountIs(5),
                        OpenRouterStopCondition.hasToolCall("finalize"),
                        OpenRouterStopCondition.maxTokensUsed(10_000),
                        OpenRouterStopCondition.maxCost(0.5),
                        OpenRouterStopCondition.finishReasonIs("length")));

        assertThat(body.has("stop_server_tools_when")).isTrue();
        JSONArray conditions = body.getJSONArray("stop_server_tools_when");
        assertThat(conditions.length()).isEqualTo(5);

        JSONObject stepCount = conditions.getJSONObject(0);
        assertThat(stepCount.getString("type")).isEqualTo("step_count_is");
        assertThat(stepCount.getInt("step_count")).isEqualTo(5);

        JSONObject hasToolCall = conditions.getJSONObject(1);
        assertThat(hasToolCall.getString("type")).isEqualTo("has_tool_call");
        assertThat(hasToolCall.getString("tool_name")).isEqualTo("finalize");

        JSONObject maxTokens = conditions.getJSONObject(2);
        assertThat(maxTokens.getString("type")).isEqualTo("max_tokens_used");
        assertThat(maxTokens.getLong("max_tokens")).isEqualTo(10_000L);

        JSONObject maxCost = conditions.getJSONObject(3);
        assertThat(maxCost.getString("type")).isEqualTo("max_cost");
        assertThat(maxCost.getDouble("max_cost_in_dollars")).isEqualTo(0.5);

        JSONObject finishReason = conditions.getJSONObject(4);
        assertThat(finishReason.getString("type")).isEqualTo("finish_reason_is");
        assertThat(finishReason.getString("reason")).isEqualTo("length");
    }

    @Test
    void rawStopConditionIsEmittedVerbatim() {
        JSONObject raw = new JSONObject()
                .put("type", "some_future_condition")
                .put("custom", "value");
        JSONObject body = bodyOf(baseBuilder().stopServerToolsWhen(List.of(OpenRouterStopCondition.raw(raw))));

        JSONObject condition = body.getJSONArray("stop_server_tools_when").getJSONObject(0);
        assertThat(condition.getString("type")).isEqualTo("some_future_condition");
        assertThat(condition.getString("custom")).isEqualTo("value");
    }

    @Test
    void rawStopConditionRequiresTypeField() {
        assertThatThrownBy(() -> OpenRouterStopCondition.raw(new JSONObject().put("custom", "value")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void stopServerToolsWhenIsOmittedWhenUnset() {
        JSONObject body = bodyOf(baseBuilder());
        assertThat(body.has("stop_server_tools_when")).isFalse();
    }

    @Test
    void stopServerToolsWhenAccessorAndPropagation() {
        OpenRouterChatCompletionRequest initial = builderWithTool()
                .addServerTool(OpenRouterServerTool.of("openrouter:web_search"))
                .stopServerToolsWhen(OpenRouterStopCondition.stepCountIs(3))
                .build();

        assertThat(initial.serverTools()).hasSize(1);
        assertThat(initial.serverTools().get(0).type()).isEqualTo("openrouter:web_search");
        assertThat(initial.stopServerToolsWhen()).hasSize(1);

        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
        OpenRouterChatCompletionRequest next = handler.buildNextRequest(
                initial,
                List.of(new JSONObject().put("role", "user").put("content", "continue")));

        JSONObject body = new JSONObject(next.getBody());
        assertThat(body.getJSONArray("tools").getJSONObject(0).getString("type")).isEqualTo("function");
        assertThat(body.getJSONArray("tools").getJSONObject(1).getString("type"))
                .isEqualTo("openrouter:web_search");
        assertThat(body.getJSONArray("stop_server_tools_when").getJSONObject(0).getInt("step_count"))
                .isEqualTo(3);
    }

    @Test
    void serverToolsAndStopConditionsArePropagatedToStreamingFollowUpRequests() {
        OpenRouterChatCompletionRequest initial = builderWithTool()
                .addServerTool(OpenRouterWebSearchServerTool.builder().maxResults(3).build())
                .stopServerToolsWhen(OpenRouterStopCondition.maxCost(1.0))
                .toolChoiceServerTool("openrouter:web_search")
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

        assertThat(next.stream()).isTrue();

        JSONObject body = new JSONObject(next.getBody());
        assertThat(body.getJSONArray("tools").getJSONObject(1).getString("type"))
                .isEqualTo("openrouter:web_search");
        assertThat(body.getJSONArray("stop_server_tools_when").getJSONObject(0).getDouble("max_cost_in_dollars"))
                .isEqualTo(1.0);
        assertThat(body.getJSONObject("tool_choice").getString("type")).isEqualTo("openrouter:web_search");
    }

    @Test
    void serviceTierIsEmitted() {
        JSONObject body = bodyOf(baseBuilder().serviceTier("flex"));

        assertThat(body.getString("service_tier")).isEqualTo("flex");
    }

    @Test
    void serviceTierIsOmittedWhenUnset() {
        JSONObject body = bodyOf(baseBuilder());

        assertThat(body.has("service_tier")).isFalse();
    }

    @Test
    void serviceTierAccessorAndPropagation() throws Exception {
        OpenRouterChatCompletionRequest initial = baseBuilder().serviceTier("priority").build();

        assertThat(initial.serviceTier()).isEqualTo("priority");

        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
        OpenRouterChatCompletionRequest next = handler.buildNextRequest(
                initial,
                List.of(new JSONObject().put("role", "user").put("content", "continue")));

        assertThat(new JSONObject(next.getBody()).getString("service_tier")).isEqualTo("priority");
    }

    @Test
    void predictionStringFormIsEmitted() {
        JSONObject body = bodyOf(baseBuilder().prediction("The capital of France is"));

        JSONObject prediction = body.getJSONObject("prediction");
        assertThat(prediction.getString("type")).isEqualTo("content");
        assertThat(prediction.getString("content")).isEqualTo("The capital of France is");
    }

    @Test
    void predictionPartsFormIsEmitted() {
        JSONObject body = bodyOf(baseBuilder().predictionParts("part one", "part two"));

        JSONObject prediction = body.getJSONObject("prediction");
        assertThat(prediction.getString("type")).isEqualTo("content");
        JSONArray content = prediction.getJSONArray("content");
        assertThat(content.length()).isEqualTo(2);
        assertThat(content.getJSONObject(0).getString("type")).isEqualTo("text");
        assertThat(content.getJSONObject(0).getString("text")).isEqualTo("part one");
        assertThat(content.getJSONObject(1).getString("text")).isEqualTo("part two");
    }

    @Test
    void predictionStringFormWinsOverPartsForm() {
        JSONObject body = bodyOf(baseBuilder()
                .predictionParts("part one")
                .prediction("single string"));

        assertThat(body.getJSONObject("prediction").getString("content")).isEqualTo("single string");
    }

    @Test
    void predictionIsOmittedWhenUnset() {
        JSONObject body = bodyOf(baseBuilder());

        assertThat(body.has("prediction")).isFalse();
    }

    @Test
    void predictionAccessorAndPropagation() throws Exception {
        OpenRouterChatCompletionRequest initial = baseBuilder()
                .prediction("The capital of France is")
                .build();

        assertThat(initial.predictionContent()).isEqualTo("The capital of France is");
        assertThat(initial.predictionParts()).isEmpty();

        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
        OpenRouterChatCompletionRequest next = handler.buildNextRequest(
                initial,
                List.of(new JSONObject().put("role", "user").put("content", "continue")));

        assertThat(new JSONObject(next.getBody()).getJSONObject("prediction").getString("content"))
                .isEqualTo("The capital of France is");
    }

    @Test
    void promptCachingControlsAreEmitted() {
        JSONObject body = bodyOf(baseBuilder()
                .cacheControl("1h")
                .promptCacheKey("conversation-42")
                .promptCacheOptions("explicit", "30m"));

        assertThat(body.getJSONObject("cache_control").getString("type")).isEqualTo("ephemeral");
        assertThat(body.getJSONObject("cache_control").getString("ttl")).isEqualTo("1h");
        assertThat(body.getString("prompt_cache_key")).isEqualTo("conversation-42");
        assertThat(body.getJSONObject("prompt_cache_options").getString("mode")).isEqualTo("explicit");
        assertThat(body.getJSONObject("prompt_cache_options").getString("ttl")).isEqualTo("30m");
    }

    @Test
    void cacheControlWithoutTtlOmitsTtlKey() {
        JSONObject body = bodyOf(baseBuilder().cacheControl());

        JSONObject cacheControl = body.getJSONObject("cache_control");
        assertThat(cacheControl.getString("type")).isEqualTo("ephemeral");
        assertThat(cacheControl.has("ttl")).isFalse();
    }

    @Test
    void promptCacheOptionsWithoutTtlOmitsTtlKey() {
        JSONObject body = bodyOf(baseBuilder().promptCacheOptions("explicit"));

        JSONObject options = body.getJSONObject("prompt_cache_options");
        assertThat(options.getString("mode")).isEqualTo("explicit");
        assertThat(options.has("ttl")).isFalse();
    }

    @Test
    void promptCachingControlsAreOmittedWhenUnset() {
        JSONObject body = bodyOf(baseBuilder());

        assertThat(body.has("cache_control")).isFalse();
        assertThat(body.has("prompt_cache_key")).isFalse();
        assertThat(body.has("prompt_cache_options")).isFalse();
    }

    @Test
    void promptCachingAccessorsAndPropagation() throws Exception {
        OpenRouterChatCompletionRequest initial = baseBuilder()
                .cacheControl("1h")
                .promptCacheKey("conversation-42")
                .promptCacheOptions("explicit")
                .build();

        assertThat(initial.cacheControlType()).isEqualTo("ephemeral");
        assertThat(initial.cacheControlTtl()).isEqualTo("1h");
        assertThat(initial.promptCacheKey()).isEqualTo("conversation-42");
        assertThat(initial.promptCacheOptionsMode()).isEqualTo("explicit");
        assertThat(initial.promptCacheOptionsTtl()).isNull();

        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
        OpenRouterChatCompletionRequest next = handler.buildNextRequest(
                initial,
                List.of(new JSONObject().put("role", "user").put("content", "continue")));

        JSONObject body = new JSONObject(next.getBody());
        assertThat(body.getJSONObject("cache_control").getString("ttl")).isEqualTo("1h");
        assertThat(body.getString("prompt_cache_key")).isEqualTo("conversation-42");
        assertThat(body.getJSONObject("prompt_cache_options").getString("mode")).isEqualTo("explicit");
    }

    @Test
    void reasoningSummaryIsEmitted() {
        JSONObject body = bodyOf(baseBuilder().reasoningSummary("detailed"));

        JSONObject reasoning = body.getJSONObject("reasoning");
        assertThat(reasoning.getString("summary")).isEqualTo("detailed");
        // a lone summary must still produce exactly one reasoning object with no other keys
        assertThat(reasoning.length()).isEqualTo(1);
    }

    @Test
    void reasoningSummaryCoexistsWithOtherReasoningOptions() {
        JSONObject body = bodyOf(baseBuilder()
                .reasoningEffort("high")
                .reasoningSummary("concise"));

        JSONObject reasoning = body.getJSONObject("reasoning");
        assertThat(reasoning.getString("effort")).isEqualTo("high");
        assertThat(reasoning.getString("summary")).isEqualTo("concise");
        assertThat(reasoning.length()).isEqualTo(2);
    }

    @Test
    void reasoningSummaryIsOmittedWhenUnset() {
        JSONObject body = bodyOf(baseBuilder());

        assertThat(body.has("reasoning")).isFalse();
    }

    @Test
    void reasoningSummaryAccessorAndPropagation() throws Exception {
        OpenRouterChatCompletionRequest initial = baseBuilder()
                .reasoningEffort("high")
                .reasoningSummary("detailed")
                .build();

        assertThat(initial.reasoningSummary()).isEqualTo("detailed");

        var handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
        OpenRouterChatCompletionRequest next = handler.buildNextRequest(
                initial,
                List.of(new JSONObject().put("role", "user").put("content", "continue")));

        JSONObject reasoning = new JSONObject(next.getBody()).getJSONObject("reasoning");
        assertThat(reasoning.getString("summary")).isEqualTo("detailed");
        assertThat(reasoning.getString("effort")).isEqualTo("high");
    }
    }
