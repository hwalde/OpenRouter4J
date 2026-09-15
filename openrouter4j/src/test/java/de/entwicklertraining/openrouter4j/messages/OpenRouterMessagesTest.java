package de.entwicklertraining.openrouter4j.messages;

import de.entwicklertraining.api.base.streaming.StreamingResponseHandler;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterJsonSchema;
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
        assertThat(body.has("trace")).isFalse();
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
    }

    @Test
    void clientEntryPointBuildsTheRequest() {
        assertThat(client().messages().model("m").maxTokens(1).addMessage("user", "hi").build()
                .getRelativeUrl()).isEqualTo("/messages");
    }
}
