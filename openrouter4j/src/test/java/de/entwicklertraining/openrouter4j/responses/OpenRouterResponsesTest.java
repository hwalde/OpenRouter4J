package de.entwicklertraining.openrouter4j.responses;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the Responses-API request at the request-JSON level (presence and
 * absence of every option, input forms, build validation) and the response
 * accessors against the recorded JSON shape of the OpenRouter /responses
 * endpoint.
 */
class OpenRouterResponsesTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void minimalRequestEmitsModelAndStringInput() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("Tell me a joke")
                .build();
        assertThat(request.getRelativeUrl()).isEqualTo("/responses");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("model", "input");
        assertThat(body.getString("model")).isEqualTo("openai/gpt-4o");
        assertThat(body.getString("input")).isEqualTo("Tell me a joke");
    }

    @Test
    void itemInputFormEmitsMessageItems() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .addMessage("user", "Hello, how are you?")
                .addMessage("assistant", "Fine, thanks.")
                .build();
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getJSONArray("input").toList()).hasSize(2);
        assertThat(body.getJSONArray("input").getJSONObject(0).toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                        "type", "message", "role", "user", "content", "Hello, how are you?"));
    }

    @Test
    void stringInputThenItemsSwitchesTheForm() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("plain")
                .addMessage("user", "as item")
                .build();
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.has("input")).isTrue();
        assertThat(body.get("input") instanceof org.json.JSONArray).isTrue();
    }

    @Test
    void unsetOptionsNeverAppearInTheBody() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .temperature(0.7)
                .build();
        JSONObject body = new JSONObject(request.getBody());
        for (String absent : List.of("instructions", "max_output_tokens", "max_tool_calls", "top_p", "top_k",
                "frequency_penalty", "presence_penalty", "tools", "tool_choice", "parallel_tool_calls",
                "reasoning", "modalities", "include", "background", "store", "metadata", "service_tier",
                "session_id", "safety_identifier", "user", "prompt_cache_key", "truncation",
                "cache_control", "plugins", "trace", "stop_server_tools_when", "provider", "stream",
                "models")) {
            assertThat(body.has(absent)).as(absent).isFalse();
        }
    }

    @Test
    void sampledAndStructuredOptionsEmitTheirSchemaFields() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .instructions("Be terse.")
                .maxOutputTokens(256)
                .maxToolCalls(5)
                .topP(0.9)
                .topK(40)
                .frequencyPenalty(0.1)
                .presencePenalty(0.2)
                .parallelToolCalls(false)
                .modalities(List.of("text", "image"))
                .addInclude("reasoning.encrypted_content")
                .background(true)
                .store(false)
                .serviceTier("flex")
                .sessionId("session-1")
                .safetyIdentifier("user-123")
                .user("user-456")
                .promptCacheKey("cache-key")
                .truncation("auto")
                .cacheControl("5m")
                .build();
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getString("instructions")).isEqualTo("Be terse.");
        assertThat(body.getInt("max_output_tokens")).isEqualTo(256);
        assertThat(body.getInt("max_tool_calls")).isEqualTo(5);
        assertThat(body.getDouble("top_p")).isEqualTo(0.9);
        assertThat(body.getInt("top_k")).isEqualTo(40);
        assertThat(body.getDouble("frequency_penalty")).isEqualTo(0.1);
        assertThat(body.getDouble("presence_penalty")).isEqualTo(0.2);
        assertThat(body.getBoolean("parallel_tool_calls")).isFalse();
        assertThat(body.getJSONArray("modalities").toList()).containsExactly("text", "image");
        assertThat(body.getJSONArray("include").toList()).containsExactly("reasoning.encrypted_content");
        assertThat(body.getBoolean("background")).isTrue();
        assertThat(body.getBoolean("store")).isFalse();
        assertThat(body.getString("service_tier")).isEqualTo("flex");
        assertThat(body.getString("session_id")).isEqualTo("session-1");
        assertThat(body.getString("safety_identifier")).isEqualTo("user-123");
        assertThat(body.getString("user")).isEqualTo("user-456");
        assertThat(body.getString("prompt_cache_key")).isEqualTo("cache-key");
        assertThat(body.getString("truncation")).isEqualTo("auto");
        assertThat(body.getJSONObject("cache_control").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("type", "ephemeral", "ttl", "5m"));
    }

    @Test
    void functionToolsAndRawToolsShareTheToolsArray() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .addFunctionTool("get_weather", "Get the weather", new JSONObject("{\"type\":\"object\"}"))
                .addTool(new JSONObject("{\"type\":\"function\",\"name\":\"raw\"}"))
                .addTool(new JSONObject("{\"type\":\"web_search\"}"))
                .toolChoice("auto")
                .build();
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getJSONArray("tools").length()).isEqualTo(3);
        assertThat(body.getJSONArray("tools").getJSONObject(0).getString("name")).isEqualTo("get_weather");
        assertThat(body.getString("tool_choice")).isEqualTo("auto");
    }

    @Test
    void reasoningObjectIsEmittedOnlyWhenAnyReasoningOptionIsSet() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .reasoningEffort("high")
                .reasoningMaxTokens(1024)
                .reasoningSummary("auto")
                .reasoningEnabled(true)
                .build();
        JSONObject reasoning = new JSONObject(request.getBody()).getJSONObject("reasoning");
        assertThat(reasoning.toMap()).containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                "effort", "high", "max_tokens", 1024, "summary", "auto", "enabled", true));
    }

    @Test
    void providerObjectIsEmittedOnlyWhenAnyProviderOptionIsSet() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .providerOrder("openai", "anthropic")
                .requireParameters(true)
                .allowFallbacks(false)
                .build();
        JSONObject provider = new JSONObject(request.getBody()).getJSONObject("provider");
        assertThat(provider.getJSONArray("order").toList()).containsExactly("openai", "anthropic");
        assertThat(provider.getBoolean("require_parameters")).isTrue();
        assertThat(provider.getBoolean("allow_fallbacks")).isFalse();
        assertThat(provider.has("only")).isFalse();
        assertThat(provider.has("ignore")).isFalse();
    }

    @Test
    void providerOnlyEmitsDisableFallbacks() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .providerOnly("openai")
                .build();
        JSONObject provider = new JSONObject(request.getBody()).getJSONObject("provider");
        assertThat(provider.getBoolean("allow_fallbacks")).isFalse();
        assertThat(provider.getJSONArray("only").toList()).containsExactly("openai");
    }

    @Test
    void streamFlagIsEmittedWhenSet() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .stream(true)
                .build();
        assertThat(new JSONObject(request.getBody()).getBoolean("stream")).isTrue();
        assertThat(request.streamRequested()).isTrue();
    }

    @Test
    void buildRejectsMissingModelAndMissingInput() {
        assertThatThrownBy(() -> client().responses().input("hi").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("model");
        assertThatThrownBy(() -> client().responses().model("openai/gpt-4o").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("input");
    }

    @Test
    void buildRejectsNullInputItemAndNullRole() {
        assertThatThrownBy(() -> client().responses().model("openai/gpt-4o").addMessage(null, "x"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().responses().model("openai/gpt-4o").addInputItem(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().responses().model("openai/gpt-4o").addFunctionTool(null, "d", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void responseSurfacesTheRecordedFixture() {
        OpenRouterResponsesResponse response = responseOf("""
                {
                  "completed_at": 1704067210,
                  "created_at": 1704067200,
                  "error": null,
                  "id": "resp-abc123",
                  "incomplete_details": null,
                  "instructions": null,
                  "max_output_tokens": null,
                  "model": "openai/gpt-4o",
                  "object": "response",
                  "output": [
                    {
                      "content": [
                        {"annotations": [], "text": "Hello! How can I help you today?", "type": "output_text"}
                      ],
                      "id": "msg-abc123",
                      "role": "assistant",
                      "status": "completed",
                      "type": "message"
                    },
                    {
                      "id": "fc-001",
                      "type": "function_call",
                      "call_id": "call_9x1",
                      "name": "get_weather",
                      "arguments": "{\\"location\\":\\"Berlin\\"}",
                      "status": "completed"
                    },
                    {
                      "id": "reasoning-123",
                      "type": "reasoning",
                      "summary": [{"type": "summary_text", "text": "Analyzed the problem."}]
                    }
                  ],
                  "parallel_tool_calls": true,
                  "status": "completed",
                  "usage": {
                    "input_tokens": 10,
                    "input_tokens_details": {"cached_tokens": 0},
                    "output_tokens": 25,
                    "output_tokens_details": {"reasoning_tokens": 5},
                    "total_tokens": 35,
                    "cost": 0.00042
                  }
                }
                """);

        assertThat(response.id()).isEqualTo("resp-abc123");
        assertThat(response.object()).isEqualTo("response");
        assertThat(response.model()).isEqualTo("openai/gpt-4o");
        assertThat(response.status()).isEqualTo("completed");
        assertThat(response.createdAt()).isEqualTo(1704067200L);
        assertThat(response.completedAt()).isEqualTo(1704067210L);
        assertThat(response.error()).isNull();
        assertThat(response.incompleteDetails()).isNull();
        assertThat(response.output()).hasSize(3);

        assertThat(response.outputText()).isEqualTo("Hello! How can I help you today?");
        assertThat(response.messageItems()).hasSize(1);
        assertThat(response.messageItems().get(0).role()).isEqualTo("assistant");
        assertThat(response.messageItems().get(0).text()).isEqualTo("Hello! How can I help you today?");

        assertThat(response.functionCallItems()).hasSize(1);
        assertThat(response.functionCallItems().get(0).name()).isEqualTo("get_weather");
        assertThat(response.functionCallItems().get(0).callId()).isEqualTo("call_9x1");
        assertThat(response.functionCallItems().get(0).arguments()).isEqualTo("{\"location\":\"Berlin\"}");

        assertThat(response.reasoningItems()).hasSize(1);
        assertThat(response.reasoningItems().get(0).summaryText()).isEqualTo("Analyzed the problem.");

        assertThat(response.inputTokens()).isEqualTo(10L);
        assertThat(response.outputTokens()).isEqualTo(25L);
        assertThat(response.totalTokens()).isEqualTo(35L);
        assertThat(response.cachedTokens()).isEqualTo(0L);
        assertThat(response.reasoningTokens()).isEqualTo(5L);
        assertThat(response.cost()).isEqualTo(0.00042);
    }

    @Test
    void responseOnEmptyBodyReturnsNullsAndEmptyLists() {
        OpenRouterResponsesResponse response = responseOf("{}");
        assertThat(response.id()).isNull();
        assertThat(response.status()).isNull();
        assertThat(response.output()).isEmpty();
        assertThat(response.messageItems()).isEmpty();
        assertThat(response.functionCallItems()).isEmpty();
        assertThat(response.reasoningItems()).isEmpty();
        assertThat(response.outputText()).isNull();
        assertThat(response.usage()).isNull();
        assertThat(response.openrouterMetadata()).isNull();
    }

    @Test
    void responseExposesMetadataAndServiceTierWhenPresent() {
        OpenRouterResponsesResponse response = responseOf("""
                {
                  "id": "resp-1",
                  "service_tier": "priority",
                  "openrouter_metadata": {"requested": "openai/gpt-4o", "strategy": "direct"}
                }
                """);
        assertThat(response.serviceTier()).isEqualTo("priority");
        assertThat(response.openrouterMetadata().getString("strategy")).isEqualTo("direct");
    }

    private OpenRouterResponsesResponse responseOf(String json) {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .build();
        return request.createResponse(json);
    }
}
