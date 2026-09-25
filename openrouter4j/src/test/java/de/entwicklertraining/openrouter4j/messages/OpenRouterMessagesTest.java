package de.entwicklertraining.openrouter4j.messages;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.OpenRouterClearThinkingEdit;
import de.entwicklertraining.openrouter4j.OpenRouterClearToolUsesEdit;
import de.entwicklertraining.openrouter4j.OpenRouterCacheMarker;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterToolLifecycleBlock;
import de.entwicklertraining.openrouter4j.OpenRouterToolReference;
import de.entwicklertraining.openrouter4j.OpenRouterCompactEdit;
import de.entwicklertraining.openrouter4j.OpenRouterContextManagementEdit;
import de.entwicklertraining.openrouter4j.OpenRouterJsonSchema;
import de.entwicklertraining.openrouter4j.OpenRouterModerationPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterPercentileCutoffs;
import de.entwicklertraining.openrouter4j.OpenRouterSafeguard;
import de.entwicklertraining.openrouter4j.OpenRouterStopCondition;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the Anthropic Messages request shape, the streaming flag and the
 * response accessors against recorded JSON shapes of POST /messages.
 */
class OpenRouterMessagesTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    private OpenRouterMessagesRequest.Builder minimalBuilder() {
        return new OpenRouterMessagesRequest.Builder(client())
                .model("anthropic/claude-sonnet-4")
                .maxTokens(1024)
                .addMessage("user", "Hello, how are you?");
    }

    @Test
    void requestUsesPostMethodOnMessagesUrl() {
        OpenRouterMessagesRequest request = minimalBuilder().build();

        assertThat(request.getRelativeUrl()).isEqualTo("/messages");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void requiredFieldsAreEmittedAndOptionalFieldsAreAbsentWhenUnset() {
        JSONObject body = new JSONObject(minimalBuilder().build().getBody());

        assertThat(body.getString("model")).isEqualTo("anthropic/claude-sonnet-4");
        assertThat(body.getInt("max_tokens")).isEqualTo(1024);
        assertThat(body.getJSONArray("messages").length()).isEqualTo(1);
        assertThat(body.getJSONArray("messages").getJSONObject(0).toMap())
                .isEqualTo(new JSONObject().put("role", "user").put("content", "Hello, how are you?").toMap());
        assertThat(body.has("system")).isFalse();
        assertThat(body.has("temperature")).isFalse();
        assertThat(body.has("top_p")).isFalse();
        assertThat(body.has("top_k")).isFalse();
        assertThat(body.has("stop_sequences")).isFalse();
        assertThat(body.has("stream")).isFalse();
        assertThat(body.has("thinking")).isFalse();
        assertThat(body.has("output_config")).isFalse();
        assertThat(body.has("metadata")).isFalse();
        assertThat(body.has("models")).isFalse();
        assertThat(body.has("fallbacks")).isFalse();
        assertThat(body.has("service_tier")).isFalse();
        assertThat(body.has("speed")).isFalse();
        assertThat(body.has("session_id")).isFalse();
        assertThat(body.has("user")).isFalse();
        assertThat(body.has("cache_control")).isFalse();
        assertThat(body.has("tools")).isFalse();
        assertThat(body.has("tool_choice")).isFalse();
        assertThat(body.has("plugins")).isFalse();
        assertThat(body.has("stop_server_tools_when")).isFalse();
        assertThat(body.has("trace")).isFalse();
        assertThat(body.has("safeguards")).isFalse();
        assertThat(body.has("provider")).isFalse();
    }

    @Test
    void missingModelMaxTokensOrMessagesIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterMessagesRequest.Builder(client())
                .maxTokens(10)
                .addMessage("user", "hi")
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("model");
        assertThatThrownBy(() -> new OpenRouterMessagesRequest.Builder(client())
                .model("anthropic/claude-sonnet-4")
                .addMessage("user", "hi")
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("maxTokens");
        assertThatThrownBy(() -> new OpenRouterMessagesRequest.Builder(client())
                .model("anthropic/claude-sonnet-4")
                .maxTokens(10)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("message");
    }

    @Test
    void systemTravelsAsTopLevelField() {
        JSONObject body = new JSONObject(minimalBuilder().system("You are a helpful assistant.").build().getBody());

        assertThat(body.getString("system")).isEqualTo("You are a helpful assistant.");
    }

    @Test
    void systemBlocksAreEmittedAsTextBlockArray() {
        JSONObject body = new JSONObject(minimalBuilder()
                .addSystemTextBlock("First block.")
                .addSystemTextBlock("Second block.")
                .build()
                .getBody());

        assertThat(body.getJSONArray("system").length()).isEqualTo(2);
        assertThat(body.getJSONArray("system").getJSONObject(0).toMap())
                .isEqualTo(new JSONObject().put("type", "text").put("text", "First block.").toMap());
    }

    @Test
    void cachedMessageEmitsContentBlocksWithCacheControl() {
        JSONObject body = new JSONObject(minimalBuilder()
                .addCachedMessage("user", "big stable context", "1h")
                .build()
                .getBody());

        JSONObject message = body.getJSONArray("messages")
                .getJSONObject(body.getJSONArray("messages").length() - 1);
        assertThat(message.getJSONArray("content").length()).isEqualTo(1);
        JSONObject block = message.getJSONArray("content").getJSONObject(0);
        assertThat(block.getString("type")).isEqualTo("text");
        assertThat(block.getString("text")).isEqualTo("big stable context");
        assertThat(block.getJSONObject("cache_control").toMap()).isEqualTo(
                new JSONObject().put("type", "ephemeral").put("ttl", "1h").toMap());
    }

    @Test
    void cachedMessageWithoutTtlOmitsTheTtlKey() {
        JSONObject body = new JSONObject(minimalBuilder()
                .addCachedMessage("user", "big stable context", null)
                .build()
                .getBody());

        JSONObject cacheControl = body.getJSONArray("messages")
                .getJSONObject(body.getJSONArray("messages").length() - 1)
                .getJSONArray("content").getJSONObject(0).getJSONObject("cache_control");
        assertThat(cacheControl.getString("type")).isEqualTo("ephemeral");
        assertThat(cacheControl.has("ttl")).isFalse();
    }

    @Test
    void messageBlocksEscapeHatchIsCarriedVerbatim() {
        JSONObject toolResult = new JSONObject()
                .put("type", "tool_result")
                .put("tool_use_id", "toolu_01")
                .put("content", "42");
        JSONObject body = new JSONObject(minimalBuilder()
                .addMessageWithBlocks("user", List.of(toolResult))
                .build()
                .getBody());

        assertThat(body.getJSONArray("messages")
                .getJSONObject(body.getJSONArray("messages").length() - 1)
                .getJSONArray("content").getJSONObject(0).toMap())
                .isEqualTo(toolResult.toMap());
    }

    @Test
    void thinkingFormsAreEmittedExactly() {
        JSONObject budget = new JSONObject(minimalBuilder().thinking(2048).build().getBody());
        assertThat(budget.getJSONObject("thinking").toMap())
                .isEqualTo(new JSONObject().put("type", "enabled").put("budget_tokens", 2048).toMap());

        JSONObject adaptive = new JSONObject(minimalBuilder().thinkingMode("adaptive").build().getBody());
        assertThat(adaptive.getJSONObject("thinking").toMap())
                .isEqualTo(new JSONObject().put("type", "adaptive").toMap());

        JSONObject disabled = new JSONObject(minimalBuilder().thinkingMode("disabled").build().getBody());
        assertThat(disabled.getJSONObject("thinking").toMap())
                .isEqualTo(new JSONObject().put("type", "disabled").toMap());

        assertThatThrownBy(() -> minimalBuilder().thinkingMode("turbo"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> minimalBuilder().thinking(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void thinkingDisplayIsEmittedWithBudgetAndAdaptive() {
        JSONObject budget = new JSONObject(minimalBuilder()
                .thinking(2048).thinkingDisplay("summarized").build().getBody());
        JSONObject thinkingBudget = budget.getJSONObject("thinking");
        assertThat(thinkingBudget.getString("type")).isEqualTo("enabled");
        assertThat(thinkingBudget.getInt("budget_tokens")).isEqualTo(2048);
        assertThat(thinkingBudget.getString("display")).isEqualTo("summarized");

        JSONObject adaptive = new JSONObject(minimalBuilder()
                .thinkingMode("adaptive").thinkingDisplay("updates").build().getBody());
        JSONObject thinkingAdaptive = adaptive.getJSONObject("thinking");
        assertThat(thinkingAdaptive.getString("type")).isEqualTo("adaptive");
        assertThat(thinkingAdaptive.getString("display")).isEqualTo("updates");
    }

    @Test
    void thinkingBlockBindingIsEmittedWithPrefixMismatchBehavior() {
        JSONObject body = new JSONObject(minimalBuilder()
                .thinking(2048).thinkingBlockBinding("drop_block").build().getBody());
        JSONObject binding = body.getJSONObject("thinking").getJSONObject("block_binding");
        assertThat(binding.keySet()).containsExactly("prefix_mismatch_behavior");
        assertThat(binding.getString("prefix_mismatch_behavior")).isEqualTo("drop_block");
        assertThat(binding.has("mismatch_behavior")).isFalse();
    }

    @Test
    void thinkingBlockBindingNullUnsets() {
        JSONObject body = new JSONObject(minimalBuilder()
                .thinking(2048).thinkingBlockBinding("error").thinkingBlockBinding(null)
                .build().getBody());
        JSONObject thinking = body.getJSONObject("thinking");
        assertThat(thinking.has("block_binding")).isFalse();
    }

    @Test
    void thinkingBlockBindingRawEscapeHatchEmitsVerbatim() {
        JSONObject body = new JSONObject(minimalBuilder()
                .thinkingMode("adaptive")
                .thinkingBlockBindingRaw(new JSONObject().put("mismatch_behavior", "error"))
                .build().getBody());
        JSONObject binding = body.getJSONObject("thinking").getJSONObject("block_binding");
        assertThat(binding.getString("mismatch_behavior")).isEqualTo("error");
        assertThat(binding.has("prefix_mismatch_behavior")).isFalse();
    }

    @Test
    void thinkingBlockBindingRawNullUnsets() {
        JSONObject body = new JSONObject(minimalBuilder()
                .thinking(2048)
                .thinkingBlockBindingRaw(new JSONObject().put("mismatch_behavior", "error"))
                .thinkingBlockBindingRaw(null)
                .build().getBody());
        assertThat(body.getJSONObject("thinking").has("block_binding")).isFalse();
    }

    @Test
    void thinkingDisplayNullUnsets() {
        JSONObject body = new JSONObject(minimalBuilder()
                .thinking(2048)
                .thinkingDisplay("summarized")
                .thinkingDisplay(null)
                .build().getBody());
        assertThat(body.getJSONObject("thinking").has("display")).isFalse();
    }

    @Test
    void thinkingDisplayAndBlockBindingRequireEnabledThinking() {
        assertThatThrownBy(() -> minimalBuilder().thinkingDisplay("summarized").build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> minimalBuilder().thinkingBlockBinding("error").build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> minimalBuilder()
                .thinkingMode("disabled").thinkingDisplay("summarized").build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void thinkingDisplayValidatesAllowedValues() {
        assertThatThrownBy(() -> minimalBuilder().thinking(2048).thinkingDisplay("verbose"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void thinkingBlockBindingValidatesPrefixMismatchBehavior() {
        assertThatThrownBy(() -> minimalBuilder().thinking(2048).thinkingBlockBinding("warn"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void thinkingDisplayAndBlockBindingAreAbsentWhenUnset() {
        JSONObject body = new JSONObject(minimalBuilder().thinking(2048).build().getBody());
        JSONObject thinking = body.getJSONObject("thinking");
        assertThat(thinking.has("display")).isFalse();
        assertThat(thinking.has("block_binding")).isFalse();
    }

    @Test
    void outputConfigCarriesEffortFormatAndTaskBudget() {
        OpenRouterJsonSchema schema = OpenRouterJsonSchema.objectSchema()
                .property("answer", OpenRouterJsonSchema.stringSchema("the answer"), true);
        JSONObject body = new JSONObject(minimalBuilder()
                .effort("medium")
                .outputFormat(schema)
                .taskBudget(400_000, 350_000)
                .build()
                .getBody());

        JSONObject outputConfig = body.getJSONObject("output_config");
        assertThat(outputConfig.getString("effort")).isEqualTo("medium");
        assertThat(outputConfig.getJSONObject("format").getString("type")).isEqualTo("json_schema");
        assertThat(outputConfig.getJSONObject("format").getJSONObject("schema")).isNotNull();
        assertThat(outputConfig.getJSONObject("task_budget").getInt("total")).isEqualTo(400_000);
        assertThat(outputConfig.getJSONObject("task_budget").getInt("remaining")).isEqualTo(350_000);
    }

    @Test
    void taskBudgetBelowMinimumIsRejectedLoudly() {
        assertThatThrownBy(() -> minimalBuilder().taskBudget(19_999))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void modelsAndFallbacksCannotBeCombined() {
        assertThatThrownBy(() -> minimalBuilder()
                .models("anthropic/claude-sonnet-4", "google/gemini-3.5-flash-lite")
                .fallbacks("z-ai/glm-5.3-flash")
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fallbacksAreEmittedAsModelObjectsAndCappedAtThree() {
        JSONObject body = new JSONObject(minimalBuilder()
                .fallbacks("a/b", "c/d")
                .build()
                .getBody());

        assertThat(body.getJSONArray("fallbacks").length()).isEqualTo(2);
        assertThat(body.getJSONArray("fallbacks").getJSONObject(0).getString("model")).isEqualTo("a/b");

        assertThatThrownBy(() -> minimalBuilder().fallbacks("a/b", "c/d", "e/f", "g/h"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void modelsFallbackListIsEmittedAsPlainStringArray() {
        JSONObject body = new JSONObject(minimalBuilder()
                .models("anthropic/claude-sonnet-4", "google/gemini-3.5-flash-lite")
                .build()
                .getBody());

        assertThat(body.getJSONArray("models").toList())
                .containsExactly("anthropic/claude-sonnet-4", "google/gemini-3.5-flash-lite");
    }

    @Test
    void pluginsTraceAndProviderOnlyIgnoreAreEmitted() {
        OpenRouterTraceConfig trace = OpenRouterTraceConfig.builder().traceId("trace-1").build();
        JSONObject body = new JSONObject(minimalBuilder()
                .addPlugin(new OpenRouterModerationPlugin())
                .trace(trace)
                .providerOnly("anthropic")
                .providerIgnore("openai")
                .build()
                .getBody());

        assertThat(body.getJSONArray("plugins").getJSONObject(0).getString("id")).isEqualTo("moderation");
        assertThat(body.getJSONObject("trace").getString("trace_id")).isEqualTo("trace-1");
        assertThat(body.getJSONObject("provider").getJSONArray("only").toList()).containsExactly("anthropic");
        assertThat(body.getJSONObject("provider").getJSONArray("ignore").toList()).containsExactly("openai");
    }

    @Test
    void simpleFieldsAreEmittedOnlyWhenSet() {
        JSONObject body = new JSONObject(minimalBuilder()
                .temperature(0.7)
                .topP(0.9)
                .topK(40)
                .stopSequences("STOP", "END")
                .metadataUserId("user-123")
                .serviceTier("standard")
                .speed("fast")
                .sessionId("session-1")
                .user("user-123")
                .cacheControl("1h")
                .build()
                .getBody());

        assertThat(body.getDouble("temperature")).isEqualTo(0.7);
        assertThat(body.getDouble("top_p")).isEqualTo(0.9);
        assertThat(body.getInt("top_k")).isEqualTo(40);
        assertThat(body.getJSONArray("stop_sequences").toList()).containsExactly("STOP", "END");
        assertThat(body.getJSONObject("metadata").getString("user_id")).isEqualTo("user-123");
        assertThat(body.getString("service_tier")).isEqualTo("standard");
        assertThat(body.getString("speed")).isEqualTo("fast");
        assertThat(body.getString("session_id")).isEqualTo("session-1");
        assertThat(body.getString("user")).isEqualTo("user-123");
        assertThat(body.getJSONObject("cache_control").toMap()).isEqualTo(
                new JSONObject().put("type", "ephemeral").put("ttl", "1h").toMap());
        assertThat(body.has("stream")).isFalse();
        assertThat(body.has("models")).isFalse();
        assertThat(body.has("fallbacks")).isFalse();
        assertThat(body.has("tools")).isFalse();
    }

    @Test
    void customToolsAreEmittedWithSchemaAndOptionalFields() {
        OpenRouterAnthropicTool tool = new OpenRouterAnthropicTool.Builder()
                .name("get_weather")
                .description("Get the weather of a city")
                .inputSchema(new JSONObject()
                        .put("type", "object")
                        .put("properties", new JSONObject().put(
                                "city", new JSONObject().put("type", "string")))
                        .put("required", List.of("city")))
                .deferLoading(false)
                .cacheControl("1h")
                .build();

        JSONObject body = new JSONObject(minimalBuilder().addTool(tool).build().getBody());
        JSONObject toolJson = body.getJSONArray("tools").getJSONObject(0);

        assertThat(toolJson.getString("name")).isEqualTo("get_weather");
        assertThat(toolJson.getString("description")).isEqualTo("Get the weather of a city");
        assertThat(toolJson.getJSONObject("input_schema").getJSONObject("properties")
                .getJSONObject("city").getString("type")).isEqualTo("string");
        assertThat(toolJson.getBoolean("defer_loading")).isFalse();
        assertThat(toolJson.getJSONObject("cache_control").getString("ttl")).isEqualTo("1h");
        assertThat(toolJson.has("type")).isFalse();
    }

    @Test
    void toolCacheControlWithNullTtlEmitsEphemeralWithoutTtl() {
        OpenRouterAnthropicTool tool = new OpenRouterAnthropicTool.Builder()
                .name("get_weather")
                .inputSchema(new JSONObject().put("type", "object"))
                .cacheControl(null)
                .build();

        JSONObject toolJson = tool.toJson();
        assertThat(toolJson.getJSONObject("cache_control").toMap())
                .isEqualTo(new JSONObject().put("type", "ephemeral").toMap());

        OpenRouterAnthropicTool uncached = new OpenRouterAnthropicTool.Builder()
                .name("get_weather")
                .inputSchema(new JSONObject().put("type", "object"))
                .build();
        assertThat(uncached.toJson().has("cache_control")).isFalse();
    }

    @Test
    void toolWithoutNameOrSchemaIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterAnthropicTool.Builder()
                .inputSchema(new JSONObject().put("type", "object"))
                .build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterAnthropicTool.Builder()
                .name("x")
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rawToolEscapeHatchIsCarriedVerbatim() {
        JSONObject serverTool = new JSONObject()
                .put("type", "web_search_20250305")
                .put("name", "web_search")
                .put("max_uses", 5);
        JSONObject body = new JSONObject(minimalBuilder().addTool(serverTool).build().getBody());

        assertThat(body.getJSONArray("tools").getJSONObject(0).toMap()).isEqualTo(serverTool.toMap());
    }

    @Test
    void toolChoiceFormsAreEmittedExactly() {
        JSONObject auto = new JSONObject(minimalBuilder().toolChoiceAuto(null).build().getBody());
        assertThat(auto.getJSONObject("tool_choice").toMap())
                .isEqualTo(new JSONObject().put("type", "auto").toMap());

        JSONObject any = new JSONObject(minimalBuilder().toolChoiceAny(true).build().getBody());
        assertThat(any.getJSONObject("tool_choice").toMap()).isEqualTo(
                new JSONObject().put("type", "any").put("disable_parallel_tool_use", true).toMap());

        JSONObject tool = new JSONObject(minimalBuilder().toolChoiceTool("get_weather", null).build().getBody());
        assertThat(tool.getJSONObject("tool_choice").toMap()).isEqualTo(
                new JSONObject().put("type", "tool").put("name", "get_weather").toMap());

        assertThatThrownBy(() -> minimalBuilder().toolChoiceTool(null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void providerObjectIsEmittedOnlyWhenAnyProviderOptionIsSet() {
        JSONObject body = new JSONObject(minimalBuilder()
                .providerOrder("anthropic", "google-vertex")
                .requireParameters(true)
                .build()
                .getBody());

        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getJSONArray("order").toList()).containsExactly("anthropic", "google-vertex");
        assertThat(provider.getBoolean("require_parameters")).isTrue();
    }

    @Test
    void streamTrueEmitsTheStreamFlag() {
        JSONObject body = new JSONObject(minimalBuilder().stream(true).build().getBody());
        assertThat(body.getBoolean("stream")).isTrue();
        assertThat(minimalBuilder().stream(true).build().streamRequested()).isTrue();
    }

    @Test
    void handlerBasedStreamingEnablesTheStreamFlagAndInstallsSseProcessor() {
        StreamingResponseHandler<String> handler = new StreamingResponseHandler<>() {
            @Override
            public void onStreamStart() {
            }

            @Override
            public void onData(String data) {
            }

            @Override
            public void onMetadata(Map<String, Object> metadata) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable throwable) {
            }
        };

        OpenRouterMessagesRequest request = minimalBuilder().stream(handler).build();

        assertThat(request.streamRequested()).isTrue();
        assertThat(new JSONObject(request.getBody()).getBoolean("stream")).isTrue();
        assertThat(request.isStreamingEnabled()).isTrue();
    }

    @Test
    void responseExposesContentBlocksAndUsage() {
        String fixture = """
                {
                  "id": "msg_01XFDUDYJgAACzvnptvVoYEL",
                  "type": "message",
                  "role": "assistant",
                  "model": "claude-sonnet-4-5-20250929",
                  "content": [
                    {"type": "text", "text": "Hello!", "citations": []},
                    {"type": "tool_use", "id": "toolu_01", "name": "get_weather",
                     "input": {"city": "Paris"}},
                    {"type": "thinking", "thinking": "Let me think.", "signature": "sig-1"},
                    {"type": "server_tool_use", "id": "srv_01", "name": "web_search",
                     "input": {"query": "weather"}}
                  ],
                  "stop_reason": "tool_use",
                  "stop_sequence": null,
                  "stop_details": null,
                  "container": null,
                  "provider": "anthropic",
                  "usage": {
                    "input_tokens": 12,
                    "output_tokens": 18,
                    "cache_creation_input_tokens": null,
                    "cache_read_input_tokens": 100,
                    "cost": 0.01,
                    "service_tier": "standard"
                  }
                }
                """;
        OpenRouterMessagesResponse response = minimalBuilder().build().createResponse(fixture);

        assertThat(response.id()).isEqualTo("msg_01XFDUDYJgAACzvnptvVoYEL");
        assertThat(response.model()).isEqualTo("claude-sonnet-4-5-20250929");
        assertThat(response.role()).isEqualTo("assistant");
        assertThat(response.stopReason()).isEqualTo("tool_use");
        assertThat(response.stopSequence()).isNull();
        assertThat(response.provider()).isEqualTo("anthropic");
        assertThat(response.text()).isEqualTo("Hello!");
        assertThat(response.textBlocks()).hasSize(1);
        assertThat(response.textBlocks().get(0).citations()).isNotNull();
        assertThat(response.toolUseBlocks()).hasSize(1);
        assertThat(response.toolUseBlocks().get(0).id()).isEqualTo("toolu_01");
        assertThat(response.toolUseBlocks().get(0).name()).isEqualTo("get_weather");
        assertThat(response.toolUseBlocks().get(0).input().getString("city")).isEqualTo("Paris");
        assertThat(response.thinkingBlocks()).hasSize(1);
        assertThat(response.thinkingBlocks().get(0).signature()).isEqualTo("sig-1");
        assertThat(response.serverToolUseBlocks()).hasSize(1);
        assertThat(response.serverToolUseBlocks().get(0).name()).isEqualTo("web_search");
        assertThat(response.contentBlocks()).hasSize(4);
        assertThat(response.inputTokens()).isEqualTo(12L);
        assertThat(response.outputTokens()).isEqualTo(18L);
        assertThat(response.cacheReadInputTokens()).isEqualTo(100L);
        assertThat(response.cacheCreationInputTokens()).isNull();
        assertThat(response.cost()).isEqualTo(0.01);
        assertThat(response.serviceTier()).isEqualTo("standard");
        assertThat(response.openrouterMetadata()).isNull();
    }

    @Test
    void responseAccessorsSwallowMalformedBody() {
        OpenRouterMessagesResponse response = minimalBuilder().build().createResponse("{}");

        assertThat(response.id()).isNull();
        assertThat(response.text()).isNull();
        assertThat(response.contentBlocks()).isEmpty();
        assertThat(response.toolUseBlocks()).isEmpty();
        assertThat(response.usage()).isNull();
        assertThat(response.cost()).isNull();
        assertThat(response.appliedContextEdits()).isEmpty();
        assertThat(response.inputTransformations()).isEmpty();
        assertThat(response.safeguardResults()).isEmpty();
        assertThat(response.compactionBlocks()).isEmpty();
        assertThat(response.containerUploadBlocks()).isEmpty();
        assertThat(response.webSearchToolResultBlocks()).isEmpty();
        assertThat(response.webFetchToolResultBlocks()).isEmpty();
        assertThat(response.codeExecutionToolResultBlocks()).isEmpty();
        assertThat(response.bashCodeExecutionToolResultBlocks()).isEmpty();
        assertThat(response.textEditorCodeExecutionToolResultBlocks()).isEmpty();
        assertThat(response.toolSearchToolResultBlocks()).isEmpty();
        assertThat(response.advisorToolResultBlocks()).isEmpty();
        assertThat(response.shellToolResultBlocks()).isEmpty();
        assertThat(response.bashToolResultBlocks()).isEmpty();
    }

    @Test
    void responseExposesAppliedContextEditsAndInputTransformations() {
        String fixture = """
                {
                  "id": "msg_01ABC",
                  "type": "message",
                  "role": "assistant",
                  "model": "claude-sonnet-4-5-20250929",
                  "content": [{"type": "text", "text": "ok"}],
                  "stop_reason": "end_turn",
                  "stop_details": null,
                  "stop_sequence": null,
                  "context_management": {
                    "applied_edits": [
                      {"type": "clear_tool_uses_20250919", "cleared_tool_uses": 2,
                       "cleared_input_tokens": 1500},
                      {"type": "clear_thinking_20251015"}
                    ]
                  },
                  "input_transformations": [
                    {"type": "thinking_dropped", "path": "messages.1.content.0",
                     "reason": "prefix_binding_mismatch"},
                    {"type": "thinking_dropped", "path": null, "reason": null}
                  ],
                  "usage": {"input_tokens": 12, "output_tokens": 8}
                }
                """;
        OpenRouterMessagesResponse response = minimalBuilder().build().createResponse(fixture);

        assertThat(response.appliedContextEdits()).hasSize(2);
        assertThat(response.appliedContextEdits().get(0).type())
                .isEqualTo("clear_tool_uses_20250919");
        assertThat(response.appliedContextEdits().get(0).json().getInt("cleared_tool_uses"))
                .isEqualTo(2);
        assertThat(response.appliedContextEdits().get(1).type())
                .isEqualTo("clear_thinking_20251015");

        assertThat(response.inputTransformations()).hasSize(2);
        assertThat(response.inputTransformations().get(0).type()).isEqualTo("thinking_dropped");
        assertThat(response.inputTransformations().get(0).path())
                .isEqualTo("messages.1.content.0");
        assertThat(response.inputTransformations().get(0).reason())
                .isEqualTo("prefix_binding_mismatch");
        assertThat(response.inputTransformations().get(1).type()).isEqualTo("thinking_dropped");
        assertThat(response.inputTransformations().get(1).path()).isNull();
        assertThat(response.inputTransformations().get(1).reason()).isNull();
    }

    @Test
    void responseEditAndTransformationAccessorsAreEmptyWithoutTheFields() {
        String fixture = """
                {
                  "id": "msg_01DEF",
                  "type": "message",
                  "role": "assistant",
                  "model": "claude-sonnet-4-5-20250929",
                  "content": [{"type": "text", "text": "ok"}],
                  "stop_reason": "end_turn",
                  "stop_details": null,
                  "stop_sequence": null,
                  "usage": {"input_tokens": 12, "output_tokens": 8}
                }
                """;
        OpenRouterMessagesResponse response = minimalBuilder().build().createResponse(fixture);

        assertThat(response.appliedContextEdits()).isEmpty();
        assertThat(response.inputTransformations()).isEmpty();
    }

    @Test
    void stopServerToolsWhenIsEmittedOnlyWhenSet() {
        OpenRouterStopCondition condition = OpenRouterStopCondition.stepCountIs(3);

        JSONObject bodyWith = new JSONObject(minimalBuilder()
                .stopServerToolsWhen(
                        OpenRouterStopCondition.stepCountIs(5),
                        OpenRouterStopCondition.hasToolCall("finalize"),
                        OpenRouterStopCondition.maxTokensUsed(10_000),
                        OpenRouterStopCondition.maxCost(0.5),
                        OpenRouterStopCondition.finishReasonIs("length"))
                .build()
                .getBody());

        assertThat(bodyWith.has("stop_server_tools_when")).isTrue();
        org.json.JSONArray conditions = bodyWith.getJSONArray("stop_server_tools_when");
        assertThat(conditions.length()).isEqualTo(5);
        assertThat(conditions.getJSONObject(0).getString("type")).isEqualTo("step_count_is");
        assertThat(conditions.getJSONObject(0).getInt("step_count")).isEqualTo(5);
        assertThat(conditions.getJSONObject(1).getString("type")).isEqualTo("has_tool_call");
        assertThat(conditions.getJSONObject(1).getString("tool_name")).isEqualTo("finalize");
        assertThat(conditions.getJSONObject(2).getString("type")).isEqualTo("max_tokens_used");
        assertThat(conditions.getJSONObject(2).getLong("max_tokens")).isEqualTo(10_000L);
        assertThat(conditions.getJSONObject(3).getString("type")).isEqualTo("max_cost");
        assertThat(conditions.getJSONObject(3).getDouble("max_cost_in_dollars")).isEqualTo(0.5);
        assertThat(conditions.getJSONObject(4).getString("type")).isEqualTo("finish_reason_is");
        assertThat(conditions.getJSONObject(4).getString("reason")).isEqualTo("length");

        assertThat(minimalBuilder().addStopServerToolsWhen(condition).build()
                .stopServerToolsWhen()).hasSize(1);
        assertThat(minimalBuilder().stopServerToolsWhen(List.of(condition)).build()
                .stopServerToolsWhen()).hasSize(1);
        assertThat(minimalBuilder().build().stopServerToolsWhen()).isEmpty();
        assertThat(new JSONObject(minimalBuilder().build().getBody())
                .has("stop_server_tools_when")).isFalse();
    }

    @Test
    void clientEntryPointBuildsTheRequest() {
        assertThat(client().messages().model("m").maxTokens(1).addMessage("user", "hi").build()
                .getRelativeUrl()).isEqualTo("/messages");
    }

    @Test
    void contextManagementClearToolUsesEmitsEveryDocumentedField() {
        JSONObject body = new JSONObject(minimalBuilder()
                .addContextManagement(OpenRouterClearToolUsesEdit.builder()
                        .triggerInputTokens(100000)
                        .keepLastToolUses(5)
                        .clearAtLeastInputTokens(50000)
                        .clearToolInputs("search", "calculator")
                        .excludeTools("vault")
                        .build())
                .build()
                .getBody());

        assertThat(body.has("context_management")).isTrue();
        JSONObject contextManagement = body.getJSONObject("context_management");
        JSONArray edits = contextManagement.getJSONArray("edits");
        assertThat(edits).hasSize(1);
        JSONObject edit = edits.getJSONObject(0);
        assertThat(edit.getString("type")).isEqualTo("clear_tool_uses_20250919");
        assertThat(edit.getJSONObject("trigger").getString("type")).isEqualTo("input_tokens");
        assertThat(edit.getJSONObject("trigger").getLong("value")).isEqualTo(100000L);
        assertThat(edit.getJSONObject("keep").getString("type")).isEqualTo("tool_uses");
        assertThat(edit.getJSONObject("keep").getLong("value")).isEqualTo(5L);
        assertThat(edit.getJSONObject("clear_at_least").getString("type")).isEqualTo("input_tokens");
        assertThat(edit.getJSONObject("clear_at_least").getLong("value")).isEqualTo(50000L);
        assertThat(edit.getJSONArray("clear_tool_inputs").toList())
                .containsExactly("search", "calculator");
        assertThat(edit.getJSONArray("exclude_tools").toList()).containsExactly("vault");
    }

    @Test
    void contextManagementSupportsEveryStrategyVariantAndListForm() {
        JSONObject body = new JSONObject(minimalBuilder()
                .contextManagement(List.of(
                        OpenRouterClearToolUsesEdit.builder()
                                .triggerToolUses(10)
                                .clearToolInputs(true)
                                .build(),
                        OpenRouterClearThinkingEdit.builder().keepLastTurns(3).build(),
                        OpenRouterClearThinkingEdit.builder().keepAll().build(),
                        OpenRouterCompactEdit.builder()
                                .instructions("Keep the task state")
                                .pauseAfterCompaction(true)
                                .triggerInputTokens(100000)
                                .build(),
                        OpenRouterContextManagementEdit.raw(new JSONObject()
                                .put("type", "future_strategy"))
                ))
                .build()
                .getBody());

        JSONArray edits = body.getJSONObject("context_management").getJSONArray("edits");
        assertThat(edits).hasSize(5);
        assertThat(edits.getJSONObject(0).getJSONObject("trigger").getString("type"))
                .isEqualTo("tool_uses");
        assertThat(edits.getJSONObject(0).getBoolean("clear_tool_inputs")).isTrue();
        assertThat(edits.getJSONObject(1).getString("type")).isEqualTo("clear_thinking_20251015");
        assertThat(edits.getJSONObject(1).getJSONObject("keep").getString("type"))
                .isEqualTo("thinking_turns");
        assertThat(edits.getJSONObject(1).getJSONObject("keep").getInt("value")).isEqualTo(3);
        assertThat(edits.getJSONObject(2).getJSONObject("keep").getString("type")).isEqualTo("all");
        assertThat(edits.getJSONObject(3).getString("type")).isEqualTo("compact_20260112");
        assertThat(edits.getJSONObject(3).getString("instructions"))
                .isEqualTo("Keep the task state");
        assertThat(edits.getJSONObject(3).getBoolean("pause_after_compaction")).isTrue();
        assertThat(edits.getJSONObject(3).getJSONObject("trigger").getString("type"))
                .isEqualTo("input_tokens");
        assertThat(edits.getJSONObject(4).getString("type")).isEqualTo("future_strategy");
    }

    @Test
    void contextManagementIsOmittedWhenUnset() {
        assertThat(new JSONObject(minimalBuilder().build().getBody())
                .has("context_management")).isFalse();
        assertThat(minimalBuilder().build().contextManagement()).isEqualTo(java.util.List.of());
    }

    @Test
    void contextManagementRawEditRequiresTypeField() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> OpenRouterContextManagementEdit.raw(new JSONObject().put("x", 1)));
    }

    @Test
    void contextManagementAddAccumulatesInsteadOfReplacing() {
        OpenRouterMessagesRequest request = minimalBuilder()
                .addContextManagement(OpenRouterClearThinkingEdit.builder().keepAll().build())
                .addContextManagement(OpenRouterCompactEdit.builder()
                        .triggerInputTokens(100000)
                        .build())
                .build();

        assertThat(request.contextManagement()).hasSize(2);
        JSONArray edits = new JSONObject(request.getBody())
                .getJSONObject("context_management").getJSONArray("edits");
        assertThat(edits).hasSize(2);
        assertThat(edits.getJSONObject(0).getString("type")).isEqualTo("clear_thinking_20251015");
        assertThat(edits.getJSONObject(1).getString("type")).isEqualTo("compact_20260112");
    }

    @Test
    void contextManagementSetterCanBeMixedWithAdd() {
        OpenRouterMessagesRequest request = minimalBuilder()
                .contextManagement(List.of(
                        OpenRouterClearToolUsesEdit.builder().triggerToolUses(10).build()))
                .addContextManagement(OpenRouterCompactEdit.builder()
                        .triggerInputTokens(100000)
                        .build())
                .build();

        assertThat(request.contextManagement()).hasSize(2);
        JSONArray edits = new JSONObject(request.getBody())
                .getJSONObject("context_management").getJSONArray("edits");
        assertThat(edits).hasSize(2);
        assertThat(edits.getJSONObject(0).getString("type")).isEqualTo("clear_tool_uses_20250919");
        assertThat(edits.getJSONObject(1).getString("type")).isEqualTo("compact_20260112");
    }

    @Test
    void contextManagementSetterReplacesPreviouslyAddedEdits() {
        OpenRouterMessagesRequest request = minimalBuilder()
                .addContextManagement(OpenRouterClearThinkingEdit.builder().keepAll().build())
                .contextManagement(List.of(
                        OpenRouterCompactEdit.builder().triggerInputTokens(100000).build()))
                .build();

        // The setter form replaces: the earlier add is gone.
        assertThat(request.contextManagement()).hasSize(1);
        JSONArray edits = new JSONObject(request.getBody())
                .getJSONObject("context_management").getJSONArray("edits");
        assertThat(edits).hasSize(1);
        assertThat(edits.getJSONObject(0).getString("type")).isEqualTo("compact_20260112");
    }

    @Test
    void safeguardsAreEmittedOnlyWhenSet() {
        JSONObject bodyWith = new JSONObject(minimalBuilder()
                .safeguards(
                        OpenRouterSafeguard.of("dangerous_tool_use"),
                        OpenRouterSafeguard.of("harmful_content",
                                new JSONObject().put("permission_mode", "auto").put("v", 1)))
                .build()
                .getBody());

        assertThat(bodyWith.has("safeguards")).isTrue();
        JSONArray safeguards = bodyWith.getJSONArray("safeguards");
        assertThat(safeguards.length()).isEqualTo(2);
        assertThat(safeguards.getJSONObject(0).getString("type"))
                .isEqualTo("dangerous_tool_use");
        assertThat(safeguards.getJSONObject(0).has("classifier_context")).isFalse();
        assertThat(safeguards.getJSONObject(0).keySet()).containsExactly("type");
        assertThat(safeguards.getJSONObject(1).getString("type"))
                .isEqualTo("harmful_content");
        JSONObject ctx = safeguards.getJSONObject(1).getJSONObject("classifier_context");
        assertThat(ctx.getString("permission_mode")).isEqualTo("auto");
        assertThat(ctx.getInt("v")).isEqualTo(1);
        assertThat(safeguards.getJSONObject(1).keySet())
                .containsExactlyInAnyOrder("type", "classifier_context");

        assertThat(new JSONObject(minimalBuilder().build().getBody())
                .has("safeguards")).isFalse();
        assertThat(minimalBuilder().build().safeguards()).isEmpty();
    }

    @Test
    void safeguardsRawRequiresTypeField() {
        assertThatThrownBy(() -> OpenRouterSafeguard.raw(new JSONObject().put("x", 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void safeguardsRawEmitsVerbatim() {
        JSONObject body = new JSONObject(minimalBuilder()
                .addSafeguard(OpenRouterSafeguard.raw(new JSONObject()
                        .put("type", "future_kind").put("extra", 1)))
                .build()
                .getBody());

        JSONObject entry = body.getJSONArray("safeguards").getJSONObject(0);
        assertThat(entry.keySet()).containsExactlyInAnyOrder("type", "extra");
        assertThat(entry.getString("type")).isEqualTo("future_kind");
        assertThat(entry.getInt("extra")).isEqualTo(1);
    }

    @Test
    void safeguardsRejectNullArguments() {
        assertThatThrownBy(() -> minimalBuilder().addSafeguard(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> minimalBuilder().safeguards((OpenRouterSafeguard[]) null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> minimalBuilder().safeguards((OpenRouterSafeguard) null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> minimalBuilder().safeguards((List<OpenRouterSafeguard>) null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void safeguardsOfWithNullClassifierContextEmitsTypeOnly() {
        JSONObject body = new JSONObject(minimalBuilder()
                .addSafeguard(OpenRouterSafeguard.of("dangerous_tool_use", null))
                .build()
                .getBody());

        JSONObject entry = body.getJSONArray("safeguards").getJSONObject(0);
        assertThat(entry.keySet()).containsExactly("type");
    }

    @Test
    void safeguardsEmptySetterClearsAndOmits() {
        OpenRouterMessagesRequest request = minimalBuilder()
                .addSafeguard(OpenRouterSafeguard.of("dangerous_tool_use"))
                .safeguards(List.of())
                .build();

        assertThat(request.safeguards()).isEmpty();
        assertThat(new JSONObject(request.getBody()).has("safeguards")).isFalse();
    }

    @Test
    void safeguardsAddAccumulatesInsteadOfReplacing() {
        OpenRouterMessagesRequest request = minimalBuilder()
                .addSafeguard(OpenRouterSafeguard.of("dangerous_tool_use"))
                .addSafeguard(OpenRouterSafeguard.of("harmful_content"))
                .build();

        assertThat(request.safeguards()).hasSize(2);
        JSONArray safeguards = new JSONObject(request.getBody()).getJSONArray("safeguards");
        assertThat(safeguards.length()).isEqualTo(2);
        assertThat(safeguards.getJSONObject(0).getString("type"))
                .isEqualTo("dangerous_tool_use");
        assertThat(safeguards.getJSONObject(1).getString("type"))
                .isEqualTo("harmful_content");
    }

    @Test
    void safeguardsSetterReplacesPreviouslyAddedEntries() {
        OpenRouterMessagesRequest request = minimalBuilder()
                .addSafeguard(OpenRouterSafeguard.of("dangerous_tool_use"))
                .safeguards(List.of(OpenRouterSafeguard.of("harmful_content")))
                .build();

        assertThat(request.safeguards()).hasSize(1);
        JSONArray safeguards = new JSONObject(request.getBody()).getJSONArray("safeguards");
        assertThat(safeguards.length()).isEqualTo(1);
        assertThat(safeguards.getJSONObject(0).getString("type"))
                .isEqualTo("harmful_content");
    }

    @Test
    void responseExposesSafeguardResults() {
        String fixture = """
                {
                  "id": "msg_01XYZ",
                  "type": "message",
                  "role": "assistant",
                  "model": "claude-sonnet-4-5-20250929",
                  "content": [{"type": "text", "text": "ok"}],
                  "stop_reason": "end_turn",
                  "safeguard_results": [
                    {"type": "dangerous_tool_use",
                     "status": {"type": "allowed", "toolu_01": "passed"}},
                    {"type": "harmful_content",
                     "status": {"type": "blocked"}}
                  ],
                  "usage": {"input_tokens": 12, "output_tokens": 8}
                }
                """;
        OpenRouterMessagesResponse response = minimalBuilder().build().createResponse(fixture);

        assertThat(response.safeguardResults()).hasSize(2);
        assertThat(response.safeguardResults().get(0).type())
                .isEqualTo("dangerous_tool_use");
        assertThat(response.safeguardResults().get(0).status().getString("type"))
                .isEqualTo("allowed");
        assertThat(response.safeguardResults().get(0).status().getString("toolu_01"))
                .isEqualTo("passed");
        assertThat(response.safeguardResults().get(0).json().getString("type"))
                .isEqualTo("dangerous_tool_use");
        assertThat(response.safeguardResults().get(1).type())
                .isEqualTo("harmful_content");
        assertThat(response.safeguardResults().get(1).status().getString("type"))
                .isEqualTo("blocked");
    }

    @Test
    void responseSafeguardResultsAreEmptyWithoutTheField() {
        String fixture = """
                {
                  "id": "msg_01ABC",
                  "type": "message",
                  "role": "assistant",
                  "model": "claude-sonnet-4-5-20250929",
                  "content": [{"type": "text", "text": "ok"}],
                  "stop_reason": "end_turn",
                  "usage": {"input_tokens": 12, "output_tokens": 8}
                }
                """;
        OpenRouterMessagesResponse response = minimalBuilder().build().createResponse(fixture);

        assertThat(response.safeguardResults()).isEmpty();
    }

    @Test
    void responseExposesCompactionAndContainerUploadBlocks() {
        String fixture = """
                {
                  "id": "msg_01COMP",
                  "type": "message",
                  "role": "assistant",
                  "model": "claude-sonnet-4-5-20250929",
                  "content": [
                    {"type": "compaction", "content": "Compacted summary.",
                     "encrypted_content": "enc-1"},
                    {"type": "container_upload", "file_id": "cfile_01abc"}
                  ],
                  "stop_reason": "compaction",
                  "usage": {"input_tokens": 12, "output_tokens": 8}
                }
                """;
        OpenRouterMessagesResponse response = minimalBuilder().build().createResponse(fixture);

        assertThat(response.compactionBlocks()).hasSize(1);
        assertThat(response.compactionBlocks().get(0).content()).isEqualTo("Compacted summary.");
        assertThat(response.compactionBlocks().get(0).encryptedContent()).isEqualTo("enc-1");
        assertThat(response.compactionBlocks().get(0).json().getString("type")).isEqualTo("compaction");
        assertThat(response.containerUploadBlocks()).hasSize(1);
        assertThat(response.containerUploadBlocks().get(0).fileId()).isEqualTo("cfile_01abc");
        assertThat(response.containerUploadBlocks().get(0).json().getString("file_id"))
                .isEqualTo("cfile_01abc");
        assertThat(response.textBlocks()).isEmpty();
    }

    @Test
    void responseCompactionBlockToleratesAbsentOptionalFields() {
        String fixture = """
                {
                  "content": [{"type": "compaction", "content": "summary only"}],
                  "stop_reason": "compaction"
                }
                """;
        OpenRouterMessagesResponse response = minimalBuilder().build().createResponse(fixture);

        assertThat(response.compactionBlocks()).hasSize(1);
        assertThat(response.compactionBlocks().get(0).content()).isEqualTo("summary only");
        assertThat(response.compactionBlocks().get(0).encryptedContent()).isNull();
    }

    @Test
    void responseExposesWebSearchAndWebFetchToolResultBlocks() {
        String fixture = """
                {
                  "content": [
                    {"type": "web_search_tool_result", "tool_use_id": "srvtoolu_01",
                     "caller": {"type": "direct"},
                     "content": [{"type": "web_search_result", "url": "https://example.com",
                                  "title": "Example Page", "encrypted_content": "enc"}]},
                    {"type": "web_search_tool_result", "tool_use_id": "srvtoolu_02",
                     "caller": {"type": "direct"},
                     "content": {"type": "web_search_tool_result_error",
                                 "error_code": "max_uses_exceeded"}},
                    {"type": "web_fetch_tool_result", "tool_use_id": "srvtoolu_03",
                     "caller": {"type": "direct"},
                     "content": {"type": "web_fetch_result", "url": "https://example.com",
                                 "retrieved_at": "2026-09-25T00:00:00Z",
                                 "content": {"type": "document", "title": "Example Page"}}},
                    {"type": "web_fetch_tool_result", "tool_use_id": "srvtoolu_04",
                     "caller": {"type": "direct"},
                     "content": {"type": "web_fetch_tool_result_error",
                                 "error_code": "url_not_accessible"}}
                  ]
                }
                """;
        OpenRouterMessagesResponse response = minimalBuilder().build().createResponse(fixture);

        assertThat(response.webSearchToolResultBlocks()).hasSize(2);
        var search = response.webSearchToolResultBlocks().get(0);
        assertThat(search.toolUseId()).isEqualTo("srvtoolu_01");
        assertThat(search.caller().getString("type")).isEqualTo("direct");
        assertThat(search.results()).hasSize(1);
        assertThat(search.results().get(0).getString("url")).isEqualTo("https://example.com");
        assertThat(search.content()).isNull();
        assertThat(search.errorCode()).isNull();
        assertThat(search.json().getString("tool_use_id")).isEqualTo("srvtoolu_01");
        var searchError = response.webSearchToolResultBlocks().get(1);
        assertThat(searchError.errorCode()).isEqualTo("max_uses_exceeded");
        assertThat(searchError.results()).isEmpty();
        assertThat(searchError.content().getString("type")).isEqualTo("web_search_tool_result_error");

        assertThat(response.webFetchToolResultBlocks()).hasSize(2);
        var fetch = response.webFetchToolResultBlocks().get(0);
        assertThat(fetch.toolUseId()).isEqualTo("srvtoolu_03");
        assertThat(fetch.caller().getString("type")).isEqualTo("direct");
        assertThat(fetch.url()).isEqualTo("https://example.com");
        assertThat(fetch.retrievedAt()).isEqualTo("2026-09-25T00:00:00Z");
        assertThat(fetch.document().getString("title")).isEqualTo("Example Page");
        assertThat(fetch.errorCode()).isNull();
        assertThat(fetch.json().getString("type")).isEqualTo("web_fetch_tool_result");
        assertThat(response.webFetchToolResultBlocks().get(1).errorCode())
                .isEqualTo("url_not_accessible");
    }

    @Test
    void responseExposesCodeExecutionToolResultBlocks() {
        String fixture = """
                {
                  "content": [
                    {"type": "code_execution_tool_result", "tool_use_id": "srvtoolu_05",
                     "content": {"type": "code_execution_result",
                                 "content": [{"type": "code_execution_output",
                                              "file_id": "file_01abc"}],
                                 "return_code": 0, "stderr": "", "stdout": "Hello"}},
                    {"type": "code_execution_tool_result", "tool_use_id": "srvtoolu_06",
                     "content": {"type": "code_execution_tool_result_error",
                                 "error_code": "execution_time_exceeded"}},
                    {"type": "code_execution_tool_result", "tool_use_id": "srvtoolu_06b",
                     "content": {"type": "encrypted_code_execution_result",
                                 "content": [],
                                 "encrypted_stdout": "enc_stdout_1",
                                 "return_code": 0, "stderr": ""}},
                    {"type": "bash_code_execution_tool_result", "tool_use_id": "srvtoolu_07",
                     "content": {"type": "bash_code_execution_result",
                                 "content": [{"type": "bash_code_execution_output",
                                              "file_id": "file_02"}],
                                 "return_code": 1, "stderr": "boom", "stdout": "partial out"}},
                    {"type": "bash_code_execution_tool_result", "tool_use_id": "srvtoolu_07b",
                     "content": {"type": "bash_code_execution_tool_result_error",
                                 "error_code": "output_file_too_large"}}
                  ]
                }
                """;
        OpenRouterMessagesResponse response = minimalBuilder().build().createResponse(fixture);

        assertThat(response.codeExecutionToolResultBlocks()).hasSize(3);
        var execution = response.codeExecutionToolResultBlocks().get(0);
        assertThat(execution.toolUseId()).isEqualTo("srvtoolu_05");
        assertThat(execution.returnCode()).isEqualTo(0);
        assertThat(execution.stdout()).isEqualTo("Hello");
        assertThat(execution.stderr()).isEmpty();
        assertThat(execution.outputFileIds()).containsExactly("file_01abc");
        assertThat(execution.encryptedStdout()).isNull();
        assertThat(execution.errorCode()).isNull();
        assertThat(execution.json().getString("tool_use_id")).isEqualTo("srvtoolu_05");
        var executionError = response.codeExecutionToolResultBlocks().get(1);
        assertThat(executionError.errorCode()).isEqualTo("execution_time_exceeded");
        assertThat(executionError.returnCode()).isNull();
        assertThat(executionError.outputFileIds()).isEmpty();
        var encrypted = response.codeExecutionToolResultBlocks().get(2);
        assertThat(encrypted.encryptedStdout()).isEqualTo("enc_stdout_1");
        assertThat(encrypted.stdout()).isNull();
        assertThat(encrypted.errorCode()).isNull();

        assertThat(response.bashCodeExecutionToolResultBlocks()).hasSize(2);
        var bash = response.bashCodeExecutionToolResultBlocks().get(0);
        assertThat(bash.toolUseId()).isEqualTo("srvtoolu_07");
        assertThat(bash.returnCode()).isEqualTo(1);
        assertThat(bash.stdout()).isEqualTo("partial out");
        assertThat(bash.stderr()).isEqualTo("boom");
        assertThat(bash.outputFileIds()).containsExactly("file_02");
        assertThat(bash.errorCode()).isNull();
        assertThat(bash.json().getString("type")).isEqualTo("bash_code_execution_tool_result");
        var bashError = response.bashCodeExecutionToolResultBlocks().get(1);
        assertThat(bashError.errorCode()).isEqualTo("output_file_too_large");
        assertThat(bashError.returnCode()).isNull();
        assertThat(bashError.stdout()).isNull();
        assertThat(bashError.outputFileIds()).isEmpty();
    }

    @Test
    void responseExposesTextEditorAndToolSearchToolResultBlocks() {
        String fixture = """
                {
                  "content": [
                    {"type": "text_editor_code_execution_tool_result", "tool_use_id": "srvtoolu_08",
                     "content": {"type": "text_editor_code_execution_view_result",
                                 "content": "file content", "file_type": "text",
                                 "num_lines": 10, "start_line": 1, "total_lines": 42}},
                    {"type": "text_editor_code_execution_tool_result", "tool_use_id": "srvtoolu_09",
                     "content": {"type": "text_editor_code_execution_tool_result_error",
                                 "error_code": "file_not_found", "error_message": "missing"}},
                    {"type": "text_editor_code_execution_tool_result", "tool_use_id": "srvtoolu_09b",
                     "content": {"type": "text_editor_code_execution_create_result",
                                 "is_file_update": true}},
                    {"type": "text_editor_code_execution_tool_result", "tool_use_id": "srvtoolu_09c",
                     "content": {"type": "text_editor_code_execution_str_replace_result",
                                 "lines": ["old", "new"], "old_start": 3, "old_lines": 1,
                                 "new_start": 7, "new_lines": 2}},
                    {"type": "tool_search_tool_result", "tool_use_id": "srvtoolu_10",
                     "content": {"type": "tool_search_tool_search_result",
                                 "tool_references": [{"type": "tool_reference",
                                                      "tool_name": "my_tool"}]}},
                    {"type": "tool_search_tool_result", "tool_use_id": "srvtoolu_10b",
                     "content": {"type": "tool_search_tool_result_error",
                                 "error_code": "too_many_requests",
                                 "error_message": "slow down"}}
                  ]
                }
                """;
        OpenRouterMessagesResponse response = minimalBuilder().build().createResponse(fixture);

        assertThat(response.textEditorCodeExecutionToolResultBlocks()).hasSize(4);
        var view = response.textEditorCodeExecutionToolResultBlocks().get(0);
        assertThat(view.toolUseId()).isEqualTo("srvtoolu_08");
        assertThat(view.resultType()).isEqualTo("text_editor_code_execution_view_result");
        assertThat(view.text()).isEqualTo("file content");
        assertThat(view.fileType()).isEqualTo("text");
        assertThat(view.numLines()).isEqualTo(10);
        assertThat(view.startLine()).isEqualTo(1);
        assertThat(view.totalLines()).isEqualTo(42);
        assertThat(view.errorCode()).isNull();
        assertThat(view.isFileUpdate()).isNull();
        assertThat(view.lines()).isEmpty();
        assertThat(view.json().getString("type")).isEqualTo("text_editor_code_execution_tool_result");
        var viewError = response.textEditorCodeExecutionToolResultBlocks().get(1);
        assertThat(viewError.errorCode()).isEqualTo("file_not_found");
        assertThat(viewError.errorMessage()).isEqualTo("missing");
        assertThat(viewError.text()).isNull();
        var created = response.textEditorCodeExecutionToolResultBlocks().get(2);
        assertThat(created.resultType()).isEqualTo("text_editor_code_execution_create_result");
        assertThat(created.isFileUpdate()).isTrue();
        assertThat(created.errorCode()).isNull();
        var replaced = response.textEditorCodeExecutionToolResultBlocks().get(3);
        assertThat(replaced.resultType()).isEqualTo("text_editor_code_execution_str_replace_result");
        assertThat(replaced.lines()).containsExactly("old", "new");
        assertThat(replaced.oldStart()).isEqualTo(3);
        assertThat(replaced.oldLines()).isEqualTo(1);
        assertThat(replaced.newStart()).isEqualTo(7);
        assertThat(replaced.newLines()).isEqualTo(2);
        assertThat(replaced.isFileUpdate()).isNull();

        assertThat(response.toolSearchToolResultBlocks()).hasSize(2);
        var search = response.toolSearchToolResultBlocks().get(0);
        assertThat(search.toolUseId()).isEqualTo("srvtoolu_10");
        assertThat(search.toolReferenceNames()).containsExactly("my_tool");
        assertThat(search.errorCode()).isNull();
        assertThat(search.errorMessage()).isNull();
        assertThat(search.json().getString("tool_use_id")).isEqualTo("srvtoolu_10");
        var searchError = response.toolSearchToolResultBlocks().get(1);
        assertThat(searchError.errorCode()).isEqualTo("too_many_requests");
        assertThat(searchError.errorMessage()).isEqualTo("slow down");
        assertThat(searchError.toolReferenceNames()).isEmpty();
    }

    @Test
    void responseExposesAdvisorAndSandboxToolResultBlocks() {
        String fixture = """
                {
                  "content": [
                    {"type": "advisor_tool_result", "tool_use_id": "srvtoolu_11",
                     "content": {"type": "advisor_result", "text": "Advisor response text"}},
                    {"type": "openrouter_shell_tool_result", "tool_use_id": "srvtoolu_12",
                     "container_id": "ctr_01abc",
                     "content": {"output": [{"stdout": "README.md\\n", "stderr": "",
                                             "outcome": {"type": "exit", "exit_code": 0}}]},
                     "files": [{"type": "container_file_citation", "container_id": "ctr_01abc",
                                "file_id": "cfile_1", "filename": "out.txt",
                                "start_index": 0, "end_index": 5}]},
                    {"type": "openrouter_bash_tool_result", "tool_use_id": "srvtoolu_13",
                     "container_id": "ctr_02",
                     "content": {"command": "ls", "exitCode": 0, "stderr": "",
                                 "stdout": "README.md\\n"},
                     "files": [{"type": "container_file_citation", "container_id": "ctr_02",
                                "file_id": "cfile_9", "filename": "notes.md",
                                "start_index": 1, "end_index": 9}]}
                  ]
                }
                """;
        OpenRouterMessagesResponse response = minimalBuilder().build().createResponse(fixture);

        assertThat(response.advisorToolResultBlocks()).hasSize(1);
        assertThat(response.advisorToolResultBlocks().get(0).toolUseId()).isEqualTo("srvtoolu_11");
        assertThat(response.advisorToolResultBlocks().get(0).text())
                .isEqualTo("Advisor response text");
        assertThat(response.advisorToolResultBlocks().get(0).json().getString("tool_use_id"))
                .isEqualTo("srvtoolu_11");

        assertThat(response.shellToolResultBlocks()).hasSize(1);
        var shell = response.shellToolResultBlocks().get(0);
        assertThat(shell.toolUseId()).isEqualTo("srvtoolu_12");
        assertThat(shell.containerId()).isEqualTo("ctr_01abc");
        assertThat(shell.files()).hasSize(1);
        assertThat(shell.files().get(0).getString("filename")).isEqualTo("out.txt");
        assertThat(shell.fileIds()).containsExactly("cfile_1");
        assertThat(shell.content().getJSONArray("output")).hasSize(1);
        assertThat(shell.json().getString("type")).isEqualTo("openrouter_shell_tool_result");

        assertThat(response.bashToolResultBlocks()).hasSize(1);
        var bash = response.bashToolResultBlocks().get(0);
        assertThat(bash.toolUseId()).isEqualTo("srvtoolu_13");
        assertThat(bash.containerId()).isEqualTo("ctr_02");
        assertThat(bash.content().getString("command")).isEqualTo("ls");
        assertThat(bash.files()).hasSize(1);
        assertThat(bash.files().get(0).getString("filename")).isEqualTo("notes.md");
        assertThat(bash.files().get(0).getInt("start_index")).isEqualTo(1);
        assertThat(bash.fileIds()).containsExactly("cfile_9");
        assertThat(bash.json().getString("type")).isEqualTo("openrouter_bash_tool_result");
    }

    @Test
    void toolAdditionMessageEmitsSystemMessageWithBlock() {
        JSONObject body = new JSONObject(minimalBuilder()
                .addToolAdditionMessage(OpenRouterToolReference.tool("my_tool"))
                .build()
                .getBody());
        JSONArray messages = body.getJSONArray("messages");
        assertThat(messages.length()).isEqualTo(2);
        JSONObject msg = messages.getJSONObject(1);
        assertThat(msg.getString("role")).isEqualTo("system");
        JSONObject block = msg.getJSONArray("content").getJSONObject(0);
        assertThat(block.getString("type")).isEqualTo("tool_addition");
        assertThat(block.getJSONObject("tool").getString("type")).isEqualTo("tool_reference");
        assertThat(block.getJSONObject("tool").getString("name")).isEqualTo("my_tool");
        assertThat(block.has("cache_control")).isFalse();
    }

    @Test
    void toolAdditionMessageWithCacheControl() {
        JSONObject body = new JSONObject(minimalBuilder()
                .addToolAdditionMessage(OpenRouterToolReference.mcpTool("t", "srv"), OpenRouterCacheMarker.cacheControl("1h"))
                .build()
                .getBody());
        JSONObject block = body.getJSONArray("messages").getJSONObject(1)
                .getJSONArray("content").getJSONObject(0);
        assertThat(block.getJSONObject("tool").getString("type")).isEqualTo("mcp_tool_reference");
        assertThat(block.getJSONObject("tool").getString("server_name")).isEqualTo("srv");
        assertThat(block.getJSONObject("cache_control").getString("type")).isEqualTo("ephemeral");
        assertThat(block.getJSONObject("cache_control").getString("ttl")).isEqualTo("1h");
    }

    @Test
    void toolRemovalMessageEmitsSystemMessageWithBlock() {
        JSONObject body = new JSONObject(minimalBuilder()
                .addToolRemovalMessage(OpenRouterToolReference.mcpToolset("srv"))
                .build()
                .getBody());
        JSONObject block = body.getJSONArray("messages").getJSONObject(1)
                .getJSONArray("content").getJSONObject(0);
        assertThat(block.getString("type")).isEqualTo("tool_removal");
        assertThat(block.getJSONObject("tool").getString("type")).isEqualTo("mcp_toolset_reference");
        assertThat(block.getJSONObject("tool").getString("server_name")).isEqualTo("srv");
    }

    @Test
    void toolReferenceThreeWireForms() {
        JSONObject tool = OpenRouterToolReference.tool("t").toJson();
        assertThat(tool.keySet()).containsOnly("type", "name");
        JSONObject mcp = OpenRouterToolReference.mcpTool("t", "s").toJson();
        assertThat(mcp.keySet()).containsOnly("type", "name", "server_name");
        JSONObject toolset = OpenRouterToolReference.mcpToolset("s").toJson();
        assertThat(toolset.keySet()).containsOnly("type", "server_name");
    }

    @Test
    void toolLifecycleBlockRejectsBreakpointMarker() {
        assertThatThrownBy(() -> OpenRouterToolLifecycleBlock.toolAddition(
                OpenRouterToolReference.tool("t"), OpenRouterCacheMarker.promptCacheBreakpoint()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void providerNewFieldsAreEmittedWhenSet() {
        JSONObject body = new JSONObject(minimalBuilder()
                .dataCollection("deny")
                .quantizations("int4", "fp8")
                .sortBy("price", "none")
                .maxPrice("0.5", "1.0", "2.0", "0.1")
                .preferredMaxLatency(2.5)
                .preferredMinThroughput(OpenRouterPercentileCutoffs.builder().p50(100.0).build())
                .enforceDistillableText(true)
                .zdr(true)
                .build()
                .getBody());
        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getString("data_collection")).isEqualTo("deny");
        assertThat(provider.getJSONArray("quantizations").toList()).containsExactly("int4", "fp8");
        JSONObject sort = provider.getJSONObject("sort");
        assertThat(sort.getString("by")).isEqualTo("price");
        assertThat(sort.getString("partition")).isEqualTo("none");
        JSONObject maxPrice = provider.getJSONObject("max_price");
        assertThat(maxPrice.getString("prompt")).isEqualTo("0.5");
        assertThat(maxPrice.getString("completion")).isEqualTo("1.0");
        assertThat(maxPrice.getString("image")).isEqualTo("2.0");
        assertThat(maxPrice.getString("audio")).isEqualTo("0.1");
        assertThat(provider.getDouble("preferred_max_latency")).isEqualTo(2.5);
        assertThat(provider.getJSONObject("preferred_min_throughput").getDouble("p50")).isEqualTo(100.0);
        assertThat(provider.getBoolean("enforce_distillable_text")).isTrue();
        assertThat(provider.getBoolean("zdr")).isTrue();
    }

    @Test
    void providerNewFieldsAreAbsentWhenUnset() {
        JSONObject bare = new JSONObject(minimalBuilder()
                .build()
                .getBody());
        assertThat(bare.has("provider")).isFalse();

        JSONObject body = new JSONObject(minimalBuilder()
                .requireParameters(true)
                .build()
                .getBody());
        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.has("data_collection")).isFalse();
        assertThat(provider.has("quantizations")).isFalse();
        assertThat(provider.has("sort")).isFalse();
        assertThat(provider.has("max_price")).isFalse();
        assertThat(provider.has("preferred_max_latency")).isFalse();
        assertThat(provider.has("preferred_min_throughput")).isFalse();
        assertThat(provider.has("enforce_distillable_text")).isFalse();
        assertThat(provider.has("zdr")).isFalse();
    }

    @Test
    void providerObjectEmittedWhenOnlyNewFieldSet() {
        JSONObject body = new JSONObject(minimalBuilder()
                .zdr(true)
                .build()
                .getBody());
        assertThat(body.has("provider")).isTrue();
        assertThat(body.getJSONObject("provider").getBoolean("zdr")).isTrue();
    }

    @Test
    void providerSortPlainFormAndMaxPriceTwoArgAreEmitted() {
        JSONObject body = new JSONObject(minimalBuilder()
                .sort("latency")
                .maxPrice("0.5", "1.0")
                .preferredMinThroughput(50.0)
                .build()
                .getBody());
        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getString("sort")).isEqualTo("latency");
        JSONObject maxPrice = provider.getJSONObject("max_price");
        assertThat(maxPrice.keySet()).containsOnly("prompt", "completion");
        assertThat(provider.getDouble("preferred_min_throughput")).isEqualTo(50.0);
    }

    @Test
    void toolReferenceRawPassthroughAndTypeRequired() {
        JSONObject raw = OpenRouterToolReference.raw(
                new JSONObject().put("type", "tool_reference").put("name", "t")).toJson();
        assertThat(raw.toMap()).containsExactlyInAnyOrderEntriesOf(
                java.util.Map.of("type", "tool_reference", "name", "t"));
        assertThatThrownBy(() -> OpenRouterToolReference.raw(new JSONObject().put("name", "t")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toolRemovalMessageWithCacheControl() {
        JSONObject body = new JSONObject(minimalBuilder()
                .addToolRemovalMessage(OpenRouterToolReference.tool("t"), OpenRouterCacheMarker.cacheControl("1h"))
                .build()
                .getBody());
        JSONObject msg = body.getJSONArray("messages").getJSONObject(1);
        assertThat(msg.getString("role")).isEqualTo("system");
        JSONObject block = msg.getJSONArray("content").getJSONObject(0);
        assertThat(block.getString("type")).isEqualTo("tool_removal");
        assertThat(block.getJSONObject("cache_control").getString("ttl")).isEqualTo("1h");
    }

    @Test
    void toolLifecycleBlockSingleArgFactories() {
        JSONObject addition = OpenRouterToolLifecycleBlock.toolAddition(
                OpenRouterToolReference.tool("t")).toJson();
        assertThat(addition.keySet()).containsOnly("type", "tool");
        assertThat(addition.getString("type")).isEqualTo("tool_addition");
        JSONObject removal = OpenRouterToolLifecycleBlock.toolRemoval(
                OpenRouterToolReference.tool("t")).toJson();
        assertThat(removal.keySet()).containsOnly("type", "tool");
        assertThat(removal.getString("type")).isEqualTo("tool_removal");
    }

    @Test
    void toolRemovalMessageAssertsSystemRole() {
        JSONObject body = new JSONObject(minimalBuilder()
                .addToolRemovalMessage(OpenRouterToolReference.tool("t"))
                .build()
                .getBody());
        JSONObject msg = body.getJSONArray("messages").getJSONObject(1);
        assertThat(msg.getString("role")).isEqualTo("system");
    }

    @Test
    void providerSortByObjectFormWinsOverPlainForm() {
        JSONObject body = new JSONObject(minimalBuilder()
                .sort("price")
                .sortBy("latency", "none")
                .build()
                .getBody());
        JSONObject sort = body.getJSONObject("provider").getJSONObject("sort");
        assertThat(sort.getString("by")).isEqualTo("latency");
    }

    @Test
    void providerPreferredLatencyNumberWinsOverCutoffs() {
        JSONObject body = new JSONObject(minimalBuilder()
                .preferredMaxLatency(OpenRouterPercentileCutoffs.builder().p50(1.0).build())
                .preferredMaxLatency(2.5)
                .build()
                .getBody());
        assertThat(body.getJSONObject("provider").getDouble("preferred_max_latency")).isEqualTo(2.5);
    }

    @Test
    void providerPreferredLatencyCutoffsFormIsEmitted() {
        JSONObject body = new JSONObject(minimalBuilder()
                .preferredMaxLatency(OpenRouterPercentileCutoffs.builder().p50(1.0).p90(3.5).build())
                .build()
                .getBody());
        JSONObject cutoffs = body.getJSONObject("provider").getJSONObject("preferred_max_latency");
        assertThat(cutoffs.getDouble("p50")).isEqualTo(1.0);
        assertThat(cutoffs.getDouble("p90")).isEqualTo(3.5);
        assertThat(cutoffs.has("p75")).isFalse();
        assertThat(cutoffs.has("p99")).isFalse();
    }

    @Test
    void providerSortByRejectsBlankPartition() {
        assertThatThrownBy(() -> minimalBuilder().sortBy("price", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("partition");
        assertThatThrownBy(() -> minimalBuilder().sortBy("price", "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void providerQuantizationsEmptyListClears() {
        JSONObject body = new JSONObject(minimalBuilder()
                .quantizations("int4", "fp8")
                .quantizations(List.of())
                .requireParameters(true)
                .build()
                .getBody());
        assertThat(body.getJSONObject("provider").has("quantizations")).isFalse();
    }

    @Test
    void providerPreferredMinThroughputNumberWinsOverCutoffs() {
        JSONObject body = new JSONObject(minimalBuilder()
                .preferredMinThroughput(OpenRouterPercentileCutoffs.builder().p50(10.0).build())
                .preferredMinThroughput(30.0)
                .build()
                .getBody());
        assertThat(body.getJSONObject("provider").getDouble("preferred_min_throughput")).isEqualTo(30.0);
    }

    @Test
    void providerAccessorsRoundTrip() {
        var request = minimalBuilder()
                .dataCollection("deny")
                .zdr(true)
                .sort("price")
                .enforceDistillableText(true)
                .build();
        assertThat(request.dataCollection()).isEqualTo("deny");
        assertThat(request.zdr()).isTrue();
        assertThat(request.sort()).isEqualTo("price");
        assertThat(request.enforceDistillableText()).isTrue();
        assertThat(request.quantizations()).isEmpty();
        assertThat(request.sortBy()).isNull();
        assertThat(request.maxPricePrompt()).isNull();
        assertThat(request.preferredMaxLatency()).isNull();
    }

    @Test
    void providerSortByObjectWinsRegardlessOfCallOrder() {
        JSONObject body = new JSONObject(minimalBuilder()
                .sortBy("latency", "none")
                .sort("price")
                .build()
                .getBody());
        JSONObject sort = body.getJSONObject("provider").getJSONObject("sort");
        assertThat(sort.getString("by")).isEqualTo("latency");
    }

    @Test
    void providerPreferredLatencyNumberWinsRegardlessOfCallOrder() {
        JSONObject body = new JSONObject(minimalBuilder()
                .preferredMaxLatency(2.5)
                .preferredMaxLatency(OpenRouterPercentileCutoffs.builder().p50(1.0).build())
                .build()
                .getBody());
        assertThat(body.getJSONObject("provider").getDouble("preferred_max_latency")).isEqualTo(2.5);
    }

    @Test
    void providerSortByNullCriterionEmitsNoSortKey() {
        JSONObject body = new JSONObject(minimalBuilder()
                .sortBy(null, "none")
                .requireParameters(true)
                .build()
                .getBody());
        assertThat(body.getJSONObject("provider").has("sort")).isFalse();
    }
}
