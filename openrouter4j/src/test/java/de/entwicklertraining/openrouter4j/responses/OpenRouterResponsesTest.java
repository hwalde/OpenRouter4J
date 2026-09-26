package de.entwicklertraining.openrouter4j.responses;

import de.entwicklertraining.openrouter4j.OpenRouterPercentileCutoffs;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONArray;
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
    void responseCachingHeadersAndCustomHeaderAreEmitted() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("Tell me a joke")
                .responseCache(true)
                .build();
        assertThat(request.getAdditionalHeaders()).containsEntry("X-OpenRouter-Cache", "true");

        request = client().responses()
                .model("openai/gpt-4o")
                .input("Tell me a joke")
                .responseCache(false)
                .build();
        assertThat(request.getAdditionalHeaders()).containsEntry("X-OpenRouter-Cache", "false");

        request = client().responses().model("openai/gpt-4o").input("x")
                .responseCacheClear(true).build();
        assertThat(request.getAdditionalHeaders()).containsEntry("X-OpenRouter-Cache-Clear", "true");

        request = client().responses().model("openai/gpt-4o").input("x")
                .responseCacheTtl(7).build();
        assertThat(request.getAdditionalHeaders()).containsEntry("X-OpenRouter-Cache-TTL", "7");

        request = client().responses().model("openai/gpt-4o").input("x")
                .responseCacheTtl(1).build();
        assertThat(request.getAdditionalHeaders()).containsEntry("X-OpenRouter-Cache-TTL", "1");

        request = client().responses().model("openai/gpt-4o").input("x")
                .responseCacheTtl(86400).build();
        assertThat(request.getAdditionalHeaders()).containsEntry("X-OpenRouter-Cache-TTL", "86400");

        request = client().responses().model("openai/gpt-4o").input("x")
                .header("X-Custom-Header", "v").build();
        assertThat(request.getAdditionalHeaders()).containsEntry("X-Custom-Header", "v");

        OpenRouterResponsesRequest bare = client().responses()
                .model("openai/gpt-4o")
                .input("Tell me a joke")
                .build();
        assertThat(bare.getAdditionalHeaders())
                .doesNotContainKey("X-OpenRouter-Cache")
                .doesNotContainKey("X-OpenRouter-Cache-Clear")
                .doesNotContainKey("X-OpenRouter-Cache-TTL")
                .doesNotContainKey("X-Custom-Header");

        assertThat(client().responses().model("m").input("x")
                .responseCache(true).responseCache(null).build().getAdditionalHeaders())
                .doesNotContainKey("X-OpenRouter-Cache");
        assertThat(client().responses().model("m").input("x")
                .responseCacheClear(true).responseCacheClear(false).build().getAdditionalHeaders())
                .doesNotContainKey("X-OpenRouter-Cache-Clear");
        assertThat(client().responses().model("m").input("x")
                .responseCacheTtl(600).responseCacheTtl(null).build().getAdditionalHeaders())
                .doesNotContainKey("X-OpenRouter-Cache-TTL");
        assertThat(client().responses().model("m").input("x")
                .header("X-C", "v").header("X-C", null).build().getAdditionalHeaders())
                .doesNotContainKey("X-C");

        assertThat(client().responses().model("m").input("x").responseCache(true)
                .header("X-OpenRouter-Cache", "custom").build().getAdditionalHeaders())
                .containsEntry("X-OpenRouter-Cache", "custom");
        assertThat(client().responses().model("m").input("x").header("X-OpenRouter-Cache", "x")
                .responseCache(null).build().getAdditionalHeaders())
                .doesNotContainKey("X-OpenRouter-Cache");
        assertThat(client().responses().model("m").input("x").responseCacheTtl(600)
                .header("X-OpenRouter-Cache-TTL", "120").build().getAdditionalHeaders())
                .containsEntry("X-OpenRouter-Cache-TTL", "120");
        assertThat(client().responses().model("m").input("x").header("X-OpenRouter-Cache", "x")
                .responseCache(true).build().getAdditionalHeaders())
                .containsEntry("X-OpenRouter-Cache", "true");
        assertThat(client().responses().model("m").input("x").header("X-OpenRouter-Cache-TTL", "120")
                .responseCacheTtl(600).build().getAdditionalHeaders())
                .containsEntry("X-OpenRouter-Cache-TTL", "600");
        assertThat(client().responses().model("m").input("x").responseCacheClear(true)
                .header("X-OpenRouter-Cache-Clear", "x").build().getAdditionalHeaders())
                .containsEntry("X-OpenRouter-Cache-Clear", "x");
        assertThat(client().responses().model("m").input("x").header("X-OpenRouter-Cache-Clear", "x")
                .responseCacheClear(true).build().getAdditionalHeaders())
                .containsEntry("X-OpenRouter-Cache-Clear", "true");

        assertThatThrownBy(() -> client().responses().model("m").input("x").responseCacheTtl(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().responses().model("m").input("x").responseCacheTtl(86401))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void headerEscapeHatchValidatesNamesAndValues() {
        assertThatThrownBy(() -> client().responses().model("m").input("x").header(null, "v"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().responses().model("m").input("x").header("  ", "v"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().responses().model("m").input("x").header("X-Bad\rName", "v"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().responses().model("m").input("x").header("X-Bad\nName", "v"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().responses().model("m").input("x").header("X-Name", "bad\rvalue"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().responses().model("m").input("x").header("X-Name", "bad\nvalue"))
                .isInstanceOf(IllegalArgumentException.class);
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
                "session_id", "safety_identifier", "user", "prompt_cache_key", "prompt_cache_options",
                "truncation", "top_logprobs",
                "cache_control", "plugins", "trace", "stop_server_tools_when", "provider", "stream",
                "models", "prompt", "image_config", "debug", "text")) {
            assertThat(body.has(absent)).as(absent).isFalse();
        }
    }

    @Test
    void promptTemplateIsEmittedWithIdAndVariables() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .prompt("preset-abc-123")
                .promptVariable("tone", "friendly")
                .promptVariable("length", 2)
                .build();
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getJSONObject("prompt").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                        "id", "preset-abc-123",
                        "variables", java.util.Map.of("tone", "friendly", "length", 2)));

        // An id without variables emits only the id.
        JSONObject idOnly = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .prompt("preset-abc-123")
                .build()
                .getBody());
        assertThat(idOnly.getJSONObject("prompt").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("id", "preset-abc-123"));

        assertThatThrownBy(() -> client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .promptVariable("tone", "friendly")
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("prompt id");
    }

    @Test
    void imageConfigDebugAndTextAreEmittedWhenSetAndAbsentWhenUnset() {
        JSONObject body = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("draw a cat")
                .imageConfig(de.entwicklertraining.openrouter4j.OpenRouterImageConfig.builder()
                        .numImages(1)
                        .aspectRatio("16:9")
                        .build())
                .debug(true)
                .textFormat(new JSONObject().put("type", "text"))
                .textVerbosity("high")
                .build()
                .getBody());
        assertThat(body.getJSONObject("image_config").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("num_images", 1, "aspect_ratio", "16:9"));
        assertThat(body.getJSONObject("debug").getBoolean("echo_upstream_body")).isTrue();
        assertThat(body.getJSONObject("text").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                        "format", java.util.Map.of("type", "text"),
                        "verbosity", "high"));

        // debug(false) is a set option and must be emitted (not treated as unset).
        JSONObject debugFalse = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .debug(false)
                .build()
                .getBody());
        assertThat(debugFalse.getJSONObject("debug").getBoolean("echo_upstream_body")).isFalse();

        // The verbatim text object replaces the composed form.
        JSONObject verbatim = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .textFormat(new JSONObject().put("type", "text"))
                .text(new JSONObject().put("format", new JSONObject().put("type", "custom")))
                .build()
                .getBody());
        assertThat(verbatim.getJSONObject("text").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                        "format", java.util.Map.of("type", "custom")));

        assertThatThrownBy(() -> client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .textVerbosity("ultra"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("verbosity");
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
    void topLogprobsAndPromptCacheOptionsAreEmittedOnlyWhenSet() {
        OpenRouterResponsesRequest unset = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .build();
        JSONObject unsetBody = new JSONObject(unset.getBody());
        assertThat(unsetBody.has("top_logprobs")).isFalse();
        assertThat(unsetBody.has("prompt_cache_options")).isFalse();

        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .topLogprobs(5)
                .promptCacheOptions("explicit")
                .build();
        JSONObject modeOnly = new JSONObject(request.getBody());
        assertThat(modeOnly.getInt("top_logprobs")).isEqualTo(5);
        assertThat(modeOnly.getJSONObject("prompt_cache_options").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("mode", "explicit"));

        OpenRouterResponsesRequest withTtl = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .promptCacheOptions("explicit", "30m")
                .build();
        JSONObject modeAndTtl = new JSONObject(withTtl.getBody());
        assertThat(modeAndTtl.getJSONObject("prompt_cache_options").toMap())
                .containsExactlyInAnyOrderEntriesOf(
                        java.util.Map.of("mode", "explicit", "ttl", "30m"));
        assertThat(withTtl.topLogprobs()).isNull();
        assertThat(withTtl.promptCacheOptionsMode()).isEqualTo("explicit");
        assertThat(withTtl.promptCacheOptionsTtl()).isEqualTo("30m");
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
    void toolChoiceObjectFormsAreEmittedExactly() {
        OpenRouterResponsesRequest named = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .toolChoiceFunction("get_weather")
                .build();
        JSONObject namedBody = new JSONObject(named.getBody()).getJSONObject("tool_choice");
        assertThat(namedBody.toMap()).containsExactlyInAnyOrderEntriesOf(
                java.util.Map.of("type", "function", "name", "get_weather"));
        assertThat(named.toolChoiceFunctionName()).isEqualTo("get_weather");
        assertThat(named.toolChoice()).isNull();

        OpenRouterResponsesRequest allowed = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .toolChoiceAllowedTools("required", "get_weather",
                        new JSONObject("{\"type\":\"web_search\"}"))
                .build();
        JSONObject allowedBody = new JSONObject(allowed.getBody()).getJSONObject("tool_choice");
        assertThat(allowedBody.toMap()).containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                "type", "allowed_tools",
                "mode", "required",
                "tools", List.of(
                        java.util.Map.of("type", "function", "name", "get_weather"),
                        java.util.Map.of("type", "web_search"))));
        assertThat(allowed.toolChoiceAllowedToolsMode()).isEqualTo("required");
        assertThat(allowed.toolChoiceAllowedTools()).hasSize(2);

        OpenRouterResponsesRequest typed = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .toolChoiceType("apply_patch")
                .build();
        JSONObject typedBody = new JSONObject(typed.getBody()).getJSONObject("tool_choice");
        assertThat(typedBody.toMap()).containsExactlyInAnyOrderEntriesOf(java.util.Map.of("type", "apply_patch"));
        assertThat(typed.toolChoiceType()).isEqualTo("apply_patch");
    }

    @Test
    void toolChoiceObjectFormsWinOverTheStringForm() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .toolChoice("auto")
                .toolChoiceType("shell")
                .toolChoiceAllowedTools("auto", "get_weather")
                .toolChoiceFunction("get_weather")
                .build();
        JSONObject toolChoice = new JSONObject(request.getBody()).getJSONObject("tool_choice");
        assertThat(toolChoice.getString("type")).isEqualTo("function");
        assertThat(toolChoice.getString("name")).isEqualTo("get_weather");
        // toolChoice() reports the raw setting; the object form wins only in the body.
        assertThat(request.toolChoice()).isEqualTo("auto");

        OpenRouterResponsesRequest allowedOverType = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .toolChoiceType("shell")
                .toolChoiceAllowedTools("auto", "get_weather")
                .build();
        JSONObject allowedOverTypeBody = new JSONObject(allowedOverType.getBody()).getJSONObject("tool_choice");
        assertThat(allowedOverTypeBody.toMap()).containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                "type", "allowed_tools",
                "mode", "auto",
                "tools", List.of(java.util.Map.of("type", "function", "name", "get_weather"))));
        assertThat(allowedOverType.toolChoiceAllowedToolsMode()).isEqualTo("auto");

        OpenRouterResponsesRequest typeOverString = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .toolChoice("none")
                .toolChoiceType("shell")
                .build();
        assertThat(new JSONObject(typeOverString.getBody()).getJSONObject("tool_choice").getString("type"))
                .isEqualTo("shell");
    }

    @Test
    void toolChoiceReadBackAccessorsAreNullWhenUnset() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .toolChoice("auto")
                .build();
        assertThat(request.toolChoice()).isEqualTo("auto");
        assertThat(request.toolChoiceFunctionName()).isNull();
        assertThat(request.toolChoiceAllowedToolsMode()).isNull();
        assertThat(request.toolChoiceAllowedTools()).isEmpty();
        assertThat(request.toolChoiceType()).isNull();
    }

    @Test
    void toolChoiceAllowedToolsReplacesRatherThanAppends() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .toolChoiceAllowedTools("auto", "a")
                .toolChoiceAllowedTools("required", "b")
                .build();
        JSONObject toolChoice = new JSONObject(request.getBody()).getJSONObject("tool_choice");
        assertThat(toolChoice.toMap()).containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                "type", "allowed_tools",
                "mode", "required",
                "tools", List.of(java.util.Map.of("type", "function", "name", "b"))));
    }

    @Test
    void toolChoiceObjectFormsRejectInvalidArguments() {
        assertThatThrownBy(() -> client().responses().model("m").input("hi").toolChoiceFunction(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().responses().model("m").input("hi").toolChoiceFunction(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().responses().model("m").input("hi").toolChoiceType(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().responses().model("m").input("hi").toolChoiceType(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().responses().model("m").input("hi")
                .toolChoiceAllowedTools("sometimes", "get_weather"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mode");
        assertThatThrownBy(() -> client().responses().model("m").input("hi")
                .toolChoiceAllowedTools("auto"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().responses().model("m").input("hi")
                .toolChoiceAllowedTools("auto", (java.util.Collection<?>) null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().responses().model("m").input("hi")
                .toolChoiceAllowedTools("auto", (Object[]) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one tool ref");
        assertThatThrownBy(() -> client().responses().model("m").input("hi")
                .toolChoiceAllowedTools("auto", (Object) null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().responses().model("m").input("hi")
                .toolChoiceAllowedTools("auto", ""))
                .isInstanceOf(IllegalArgumentException.class);
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
    void modelsListReplacesTheSingleModelRequirement() {
        OpenRouterResponsesRequest request = client().responses()
                .models("openai/gpt-4o", "anthropic/claude-4.5-sonnet-20250929")
                .input("hi")
                .build();
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("models", "input");
        assertThat(body.getJSONArray("models").toList()).containsExactly(
                "openai/gpt-4o", "anthropic/claude-4.5-sonnet-20250929");
        assertThat(body.has("model")).isFalse();

        OpenRouterResponsesRequest listForm = client().responses()
                .models(java.util.List.of("openai/gpt-4o"))
                .input("hi")
                .build();
        assertThat(new JSONObject(listForm.getBody()).getJSONArray("models").toList())
                .containsExactly("openai/gpt-4o");
    }

    @Test
    void rawInputItemsLandVerbatimInTheInputArray() {
        JSONObject raw = new JSONObject("{\"type\":\"message\",\"role\":\"user\",\"content\":[{\"type\":\"input_image\",\"image_url\":\"https://example.com/x.png\"}]}");
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("switched away from this")
                .addInputItem(raw)
                .build();
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getJSONArray("input").length()).isEqualTo(1);
        assertThat(body.getJSONArray("input").getJSONObject(0).toMap())
                .isEqualTo(raw.toMap());
    }

    // --- Typed input items (OpenRouterInputItem) ---

    @Test
    void functionCallItemsEmitDocumentedKeys() {
        JSONObject item = OpenRouterInputItem
                .functionCall("call-abc123", "get_weather", "{\"location\":\"SF\"}")
                .toJson();
        assertThat(item.keySet()).containsExactlyInAnyOrder("type", "call_id", "name", "arguments");
        assertThat(item.getString("type")).isEqualTo("function_call");
        assertThat(item.getString("call_id")).isEqualTo("call-abc123");
        assertThat(item.getString("name")).isEqualTo("get_weather");
        assertThat(item.getString("arguments")).isEqualTo("{\"location\":\"SF\"}");

        JSONObject withStatus = OpenRouterInputItem
                .functionCall("call-abc123", "get_weather", "{}", "completed")
                .toJson();
        assertThat(withStatus.keySet())
                .containsExactlyInAnyOrder("type", "call_id", "name", "arguments", "status");
        assertThat(withStatus.getString("status")).isEqualTo("completed");
    }

    @Test
    void functionCallOutputItemsEmitDocumentedKeys() {
        JSONObject item = OpenRouterInputItem
                .functionCallOutput("call-abc123", "{\"temperature\":72}")
                .toJson();
        assertThat(item.keySet()).containsExactlyInAnyOrder("type", "call_id", "output");
        assertThat(item.getString("type")).isEqualTo("function_call_output");
        assertThat(item.getString("call_id")).isEqualTo("call-abc123");
        assertThat(item.getString("output")).isEqualTo("{\"temperature\":72}");

        JSONObject withStatus = OpenRouterInputItem
                .functionCallOutput("call-abc123", "ok", "completed")
                .toJson();
        assertThat(withStatus.keySet()).containsExactlyInAnyOrder("type", "call_id", "output", "status");
        assertThat(withStatus.getString("status")).isEqualTo("completed");
    }

    @Test
    void itemReferenceEmitsDocumentedKeys() {
        JSONObject item = OpenRouterInputItem.itemReference("msg-abc123").toJson();
        assertThat(item.keySet()).containsExactlyInAnyOrder("type", "id");
        assertThat(item.getString("type")).isEqualTo("item_reference");
        assertThat(item.getString("id")).isEqualTo("msg-abc123");
    }

    @Test
    void outputMessageItemsEmitDocumentedKeys() {
        JSONObject item = OpenRouterInputItem.outputMessage("msg-123", "Hello!").toJson();
        assertThat(item.keySet()).containsExactlyInAnyOrder("type", "role", "id", "content", "status");
        assertThat(item.getString("type")).isEqualTo("message");
        assertThat(item.getString("role")).isEqualTo("assistant");
        assertThat(item.getString("id")).isEqualTo("msg-123");
        assertThat(item.getString("status")).isEqualTo("completed");
        JSONArray content = item.getJSONArray("content");
        assertThat(content.length()).isEqualTo(1);
        assertThat(content.getJSONObject(0).keySet()).containsExactlyInAnyOrder("type", "text");
        assertThat(content.getJSONObject(0).getString("type")).isEqualTo("output_text");
        assertThat(content.getJSONObject(0).getString("text")).isEqualTo("Hello!");

        JSONObject withStatus = OpenRouterInputItem.outputMessage("msg-123", "Hi", "in_progress").toJson();
        assertThat(withStatus.getString("status")).isEqualTo("in_progress");

        JSONObject nullStatus = OpenRouterInputItem.outputMessage("msg-123", "Hi", null).toJson();
        assertThat(nullStatus.getString("status")).isEqualTo("completed");
    }

    @Test
    void toJsonReturnsADetachedCopy() {
        OpenRouterInputItem item = OpenRouterInputItem.itemReference("msg-1");
        item.toJson().put("injected", true);
        assertThat(item.toJson().has("injected")).isFalse();
        assertThat(item.toJson().keySet()).containsExactlyInAnyOrder("type", "id");

        OpenRouterInputItem msg = OpenRouterInputItem.outputMessage("m", "t");
        msg.toJson().getJSONArray("content").getJSONObject(0).put("text", "tampered");
        assertThat(msg.toJson().getJSONArray("content").getJSONObject(0).getString("text"))
                .isEqualTo("t");

        JSONArray summaryArray = new JSONArray("[{\"type\":\"summary_text\",\"text\":\"a\"}]");
        OpenRouterInputItem reasoningItem = OpenRouterInputItem.reasoningWithSummary("r", summaryArray, null);
        summaryArray.getJSONObject(0).put("text", "tampered");
        assertThat(reasoningItem.toJson().getJSONArray("summary").getJSONObject(0).getString("text"))
                .isEqualTo("a");
        summaryArray.put(new JSONObject().put("type", "summary_text").put("text", "x"));
        assertThat(reasoningItem.toJson().getJSONArray("summary").toList()).hasSize(1);

        JSONObject rawIn = new JSONObject("{\"type\":\"shell_call\",\"action\":{\"commands\":[\"ls\"]}}");
        OpenRouterInputItem rawItem = OpenRouterInputItem.raw(rawIn);
        rawIn.getJSONObject("action").put("injected", true);
        assertThat(rawItem.toJson().getJSONObject("action").has("injected")).isFalse();
        rawIn.put("injected", true);
        assertThat(rawItem.toJson().has("injected")).isFalse();
    }

    @Test
    void reasoningItemsCarrySummaryAndSignatureUnmodified() {
        JSONObject item = OpenRouterInputItem
                .reasoning("reasoning-abc123", "Analyzed the problem", "sig-verbatim==")
                .toJson();
        assertThat(item.keySet()).containsExactlyInAnyOrder("type", "id", "summary", "signature");
        assertThat(item.getString("type")).isEqualTo("reasoning");
        assertThat(item.getString("id")).isEqualTo("reasoning-abc123");
        assertThat(item.getString("signature")).isEqualTo("sig-verbatim==");
        JSONArray summary = item.getJSONArray("summary");
        assertThat(summary.length()).isEqualTo(1);
        assertThat(summary.getJSONObject(0).keySet()).containsExactlyInAnyOrder("type", "text");
        assertThat(summary.getJSONObject(0).getString("type")).isEqualTo("summary_text");
        assertThat(summary.getJSONObject(0).getString("text")).isEqualTo("Analyzed the problem");

        JSONObject withoutSignature = OpenRouterInputItem.reasoning("reasoning-1", "only summary").toJson();
        assertThat(withoutSignature.has("signature")).isFalse();

        JSONArray multiSummary = new JSONArray(
                "[{\"type\":\"summary_text\",\"text\":\"first\"},{\"type\":\"summary_text\",\"text\":\"second\"}]");
        JSONObject passthrough = OpenRouterInputItem
                .reasoningWithSummary("reasoning-2", multiSummary, "sig-2")
                .toJson();
        assertThat(passthrough.getJSONArray("summary").toList()).isEqualTo(multiSummary.toList());
        assertThat(passthrough.getString("signature")).isEqualTo("sig-2");
    }

    @Test
    void mixedInputArrayEmitsTypedItemsInOrder() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .addMessage("user", "What is the weather in SF?")
                .addInput(OpenRouterInputItem.functionCall("call-1", "get_weather", "{\"city\":\"SF\"}"))
                .addInput(OpenRouterInputItem.functionCallOutput("call-1", "{\"temp\":72}"))
                .addInput(OpenRouterInputItem.outputMessage("msg-1", "It is 72F."))
                .addInput(OpenRouterInputItem.reasoning("reasoning-1", "looked it up", "sig-1"))
                .addInput(OpenRouterInputItem.itemReference("msg-old"))
                .build();
        JSONObject body = new JSONObject(request.getBody());
        JSONArray input = body.getJSONArray("input");
        assertThat(input.length()).isEqualTo(6);
        assertThat(input.getJSONObject(0).getString("type")).isEqualTo("message");
        assertThat(input.getJSONObject(0).getString("role")).isEqualTo("user");
        assertThat(input.getJSONObject(1).getString("type")).isEqualTo("function_call");
        assertThat(input.getJSONObject(1).getString("call_id")).isEqualTo("call-1");
        assertThat(input.getJSONObject(2).getString("type")).isEqualTo("function_call_output");
        assertThat(input.getJSONObject(2).getString("call_id")).isEqualTo("call-1");
        assertThat(input.getJSONObject(3).getString("type")).isEqualTo("message");
        assertThat(input.getJSONObject(3).getString("id")).isEqualTo("msg-1");
        assertThat(input.getJSONObject(4).getString("type")).isEqualTo("reasoning");
        assertThat(input.getJSONObject(4).getString("signature")).isEqualTo("sig-1");
        assertThat(input.getJSONObject(5).getString("type")).isEqualTo("item_reference");
        assertThat(input.getJSONObject(5).getString("id")).isEqualTo("msg-old");
    }

    @Test
    void addInputAloneSwitchesToTheItemArrayForm() {
        JSONObject body = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .addInput(OpenRouterInputItem.itemReference("id-1"))
                .build()
                .getBody());
        assertThat(body.get("input")).isInstanceOf(JSONArray.class);
        JSONArray input = body.getJSONArray("input");
        assertThat(input.length()).isEqualTo(1);
        assertThat(input.getJSONObject(0).toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("type", "item_reference", "id", "id-1"));
    }

    @Test
    void addInputAfterStringInputSwitchesToTheItemArrayForm() {
        JSONObject body = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("plain string first")
                .addInput(OpenRouterInputItem.functionCall("call-1", "t", "{}"))
                .build()
                .getBody());
        assertThat(body.get("input")).isInstanceOf(JSONArray.class);
        JSONArray input = body.getJSONArray("input");
        assertThat(input.length()).isEqualTo(1);
        assertThat(input.getJSONObject(0).getString("type")).isEqualTo("function_call");
        assertThat(input.getJSONObject(0).getString("call_id")).isEqualTo("call-1");
    }

    @Test
    void rawTypedItemPassesThroughAndRequiresType() {
        JSONObject raw = new JSONObject(
                "{\"type\":\"shell_call\",\"call_id\":\"c-1\",\"action\":{\"commands\":[\"ls\"]}}");
        JSONObject item = OpenRouterInputItem.raw(raw).toJson();
        assertThat(item.toMap()).isEqualTo(raw.toMap());

        assertThatThrownBy(() -> OpenRouterInputItem.raw(new JSONObject().put("call_id", "c-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type");
        assertThatThrownBy(() -> OpenRouterInputItem.raw(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("item must not be null");
    }

    @Test
    void inputItemFactoriesValidateLoudly() {
        assertThatThrownBy(() -> OpenRouterInputItem.functionCall(null, "t", "{}"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterInputItem.functionCall("c", "", "{}"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterInputItem.functionCall("c", "t", null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> OpenRouterInputItem.functionCallOutput(null, "x"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterInputItem.functionCallOutput("c", null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> OpenRouterInputItem.itemReference(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterInputItem.outputMessage("m", null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> OpenRouterInputItem.reasoning("", "summary"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterInputItem.reasoningWithSummary("r", null, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> client().responses().model("m")
                .addInput(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void metadataPluginsTraceAndStopConditionsAreEmittedWhenSet() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .metadata(new JSONObject("{\"user_id\":\"u-1\"}"))
                .addPlugin(de.entwicklertraining.openrouter4j.OpenRouterModerationPlugin.builder().build())
                .trace(de.entwicklertraining.openrouter4j.OpenRouterTraceConfig.builder().traceId("trace-1").build())
                .stopServerToolsWhen(java.util.List.of(
                        de.entwicklertraining.openrouter4j.OpenRouterStopCondition.stepCountIs(3)))
                .build();
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getJSONObject("metadata").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("user_id", "u-1"));
        assertThat(body.getJSONArray("plugins").length()).isEqualTo(1);
        assertThat(body.getJSONArray("plugins").getJSONObject(0).getString("id")).isEqualTo("moderation");
        assertThat(body.getJSONObject("trace").optString("trace_id")).isEqualTo("trace-1");
        assertThat(body.getJSONArray("stop_server_tools_when").length()).isEqualTo(1);
    }

    @Test
    void providerIgnoreAloneTriggersTheProviderObject() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .providerIgnore("deepseek", "qwen")
                .build();
        JSONObject provider = new JSONObject(request.getBody()).getJSONObject("provider");
        assertThat(provider.getJSONArray("ignore").toList()).containsExactly("deepseek", "qwen");
        assertThat(provider.has("order")).isFalse();
        assertThat(provider.has("only")).isFalse();
        assertThat(provider.has("require_parameters")).isFalse();
    }

    @Test
    void handlerStreamingAloneMakesTheBodyCarryStreamTrue() {
        OpenRouterResponsesRequest request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .stream(new de.entwicklertraining.api.base.streaming.StreamingResponseHandler<String>() {
                    @Override
                    public void onData(String data) {
                        // not executed in this test
                    }

                    @Override
                    public void onComplete() {
                        // not executed in this test
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        // not executed in this test
                    }
                })
                .build();
        assertThat(new JSONObject(request.getBody()).getBoolean("stream")).isTrue();
        assertThat(request.streamRequested()).isTrue();
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
        assertThat(response.errorType()).isNull();
    }

    @Test
    void responseSurfacesErrorTypeFromBothDocumentedLocations() {
        assertThat(responseOf("{\"error_type\":\"context_length_exceeded\"}").errorType())
                .isEqualTo("context_length_exceeded");
        assertThat(responseOf("{\"error\":{\"error_type\":\"provider_overloaded\"}}").errorType())
                .isEqualTo("provider_overloaded");
        assertThat(responseOf("{\"error\":{\"code\":429}}").errorType()).isNull();
        assertThat(responseOf("{\"error_type\":null}").errorType()).isNull();
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

    @Test
    void providerNewFieldsAreEmittedWhenSet() {
        JSONObject body = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
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
        JSONObject bare = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .build()
                .getBody());
        assertThat(bare.has("provider")).isFalse();

        JSONObject body = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
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
        JSONObject body = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .zdr(true)
                .build()
                .getBody());
        assertThat(body.has("provider")).isTrue();
        assertThat(body.getJSONObject("provider").getBoolean("zdr")).isTrue();
    }

    @Test
    void providerSortPlainFormAndMaxPriceTwoArgAreEmitted() {
        JSONObject body = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
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
    void providerSortByObjectFormWinsOverPlainForm() {
        JSONObject body = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .sort("price")
                .sortBy("latency", "none")
                .build()
                .getBody());
        JSONObject sort = body.getJSONObject("provider").getJSONObject("sort");
        assertThat(sort.getString("by")).isEqualTo("latency");
    }

    @Test
    void providerPreferredLatencyCutoffsFormIsEmitted() {
        JSONObject body = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
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
    void providerPreferredLatencyNumberWinsOverCutoffs() {
        JSONObject body = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .preferredMaxLatency(OpenRouterPercentileCutoffs.builder().p50(1.0).build())
                .preferredMaxLatency(2.5)
                .build()
                .getBody());
        assertThat(body.getJSONObject("provider").getDouble("preferred_max_latency")).isEqualTo(2.5);
    }

    @Test
    void providerPreferredMinThroughputNumberWinsOverCutoffs() {
        JSONObject body = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .preferredMinThroughput(OpenRouterPercentileCutoffs.builder().p50(10.0).build())
                .preferredMinThroughput(30.0)
                .build()
                .getBody());
        assertThat(body.getJSONObject("provider").getDouble("preferred_min_throughput")).isEqualTo(30.0);
    }

    @Test
    void providerSortByRejectsBlankPartition() {
        assertThatThrownBy(() -> client().responses()
                .model("openai/gpt-4o")
                .input("hi").sortBy("price", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("partition");
        assertThatThrownBy(() -> client().responses()
                .model("openai/gpt-4o")
                .input("hi").sortBy("price", "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void providerQuantizationsEmptyListClears() {
        JSONObject body = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .quantizations("int4", "fp8")
                .quantizations(List.of())
                .requireParameters(true)
                .build()
                .getBody());
        assertThat(body.getJSONObject("provider").has("quantizations")).isFalse();
    }

    @Test
    void providerAccessorsRoundTrip() {
        var request = client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .dataCollection("deny")
                .zdr(true)
                .sort("price")
                .enforceDistillableText(true)
                .build();
        assertThat(request.dataCollection()).isEqualTo("deny");
        assertThat(request.zdr()).isTrue();
        assertThat(request.sort()).isEqualTo("price");
        assertThat(request.enforceDistillableText()).isTrue();
    }

    @Test
    void providerSortByObjectWinsRegardlessOfCallOrder() {
        JSONObject body = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .sortBy("latency", "none")
                .sort("price")
                .build()
                .getBody());
        JSONObject sort = body.getJSONObject("provider").getJSONObject("sort");
        assertThat(sort.getString("by")).isEqualTo("latency");
    }

    @Test
    void providerPreferredLatencyNumberWinsRegardlessOfCallOrder() {
        JSONObject body = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .preferredMaxLatency(2.5)
                .preferredMaxLatency(OpenRouterPercentileCutoffs.builder().p50(1.0).build())
                .build()
                .getBody());
        assertThat(body.getJSONObject("provider").getDouble("preferred_max_latency")).isEqualTo(2.5);
    }

    @Test
    void providerSortByNullCriterionEmitsNoSortKey() {
        JSONObject body = new JSONObject(client().responses()
                .model("openai/gpt-4o")
                .input("hi")
                .sortBy(null, "none")
                .requireParameters(true)
                .build()
                .getBody());
        assertThat(body.getJSONObject("provider").has("sort")).isFalse();
    }

    @Test
    void textPartAnnotationsAreTypedByDiscriminator() {
        OpenRouterResponsesResponse response = responseOf("""
                {
                  "output": [{
                    "type": "message", "role": "assistant", "status": "completed",
                    "content": [{
                      "type": "output_text",
                      "text": "Paris is the capital of France.",
                      "annotations": [
                        {"type": "url_citation", "url": "https://example.com/fr",
                         "title": "France", "start_index": 0, "end_index": 5,
                         "content": "Paris is the capital."},
                        {"type": "file_citation", "file_id": "file-1",
                         "filename": "notes.md", "index": 2},
                        {"type": "file_path", "file_id": "file-2", "index": 0}
                      ]
                    }]
                  }]
                }
                """);
        OpenRouterResponsesResponse.OutputMessageItem item = response.messageItems().get(0);
        List<OpenRouterResponsesResponse.OutputTextPart> parts = item.outputTextParts();
        assertThat(parts).hasSize(1);
        assertThat(parts.get(0).text()).isEqualTo("Paris is the capital of France.");

        List<OpenRouterTextAnnotation> annotations = parts.get(0).annotations();
        assertThat(annotations).hasSize(3);

        OpenRouterTextAnnotation url = annotations.get(0);
        assertThat(url.type()).isEqualTo("url_citation");
        assertThat(url.isUrlCitation()).isTrue();
        assertThat(url.isFileCitation()).isFalse();
        assertThat(url.isFilePath()).isFalse();
        assertThat(url.urlCitation().url()).isEqualTo("https://example.com/fr");
        assertThat(url.urlCitation().title()).isEqualTo("France");
        assertThat(url.urlCitation().startIndex()).isEqualTo(0L);
        assertThat(url.urlCitation().endIndex()).isEqualTo(5L);
        assertThat(url.urlCitation().content()).isEqualTo("Paris is the capital.");
        assertThat(url.fileCitation()).isNull();
        assertThat(url.filePath()).isNull();

        OpenRouterTextAnnotation file = annotations.get(1);
        assertThat(file.isFileCitation()).isTrue();
        assertThat(file.fileCitation().fileId()).isEqualTo("file-1");
        assertThat(file.fileCitation().filename()).isEqualTo("notes.md");
        assertThat(file.fileCitation().index()).isEqualTo(2L);
        assertThat(file.urlCitation()).isNull();
        assertThat(file.filePath()).isNull();

        OpenRouterTextAnnotation path = annotations.get(2);
        assertThat(path.isFilePath()).isTrue();
        assertThat(path.filePath().fileId()).isEqualTo("file-2");
        assertThat(path.filePath().index()).isEqualTo(0L);
        assertThat(path.urlCitation()).isNull();
        assertThat(path.fileCitation()).isNull();
    }

    @Test
    void containerFileAnnotationFallsThroughToTheRawHatch() {
        OpenRouterResponsesResponse response = responseOf("""
                {
                  "output": [{
                    "type": "message", "role": "assistant",
                    "content": [{
                      "type": "output_text", "text": "See the report.",
                      "annotations": [
                        {"type": "file", "file": {"name": "report.md",
                         "hash": "abc", "content": ["# Report"]}}
                      ]
                    }]
                  }]
                }
                """);
        OpenRouterTextAnnotation annotation = response.messageItems().get(0)
                .annotations().get(0);
        assertThat(annotation.type()).isEqualTo("file");
        assertThat(annotation.urlCitation()).isNull();
        assertThat(annotation.fileCitation()).isNull();
        assertThat(annotation.filePath()).isNull();
        assertThat(annotation.json().getJSONObject("file").getString("name"))
                .isEqualTo("report.md");
    }

    @Test
    void absentAnnotationsYieldEmptyAndFlattenAcrossParts() {
        OpenRouterResponsesResponse response = responseOf("""
                {
                  "output": [{
                    "type": "message", "role": "assistant",
                    "content": [
                      {"type": "output_text", "text": "First.",
                       "annotations": [{"type": "file_path", "file_id": "f", "index": 1}]},
                      {"type": "output_text", "text": "Second.", "annotations": []},
                      {"type": "output_text", "text": "Third."}
                    ]
                  }]
                }
                """);
        OpenRouterResponsesResponse.OutputMessageItem item = response.messageItems().get(0);
        assertThat(item.annotations()).hasSize(1);
        assertThat(item.annotations().get(0).filePath().fileId()).isEqualTo("f");
        List<OpenRouterResponsesResponse.OutputTextPart> parts = item.outputTextParts();
        assertThat(parts).hasSize(3);
        assertThat(parts.get(0).annotations()).hasSize(1);
        assertThat(parts.get(1).annotations()).isEmpty();
        assertThat(parts.get(2).annotations()).isEmpty();

        OpenRouterResponsesResponse none = responseOf("""
                {"output": [{"type": "message", "content": "plain string"}]}
                """);
        assertThat(none.messageItems().get(0).outputTextParts()).isEmpty();
        assertThat(none.messageItems().get(0).annotations()).isEmpty();
    }
}
