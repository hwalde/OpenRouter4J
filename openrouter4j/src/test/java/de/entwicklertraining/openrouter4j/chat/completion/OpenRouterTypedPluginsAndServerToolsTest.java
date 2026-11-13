package de.entwicklertraining.openrouter4j.chat.completion;

import de.entwicklertraining.openrouter4j.OpenRouterAdvisorServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterAlignmentPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterApplyPatchServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterArmorPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterAutoBetaRouterPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterAutoRouterPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterBashServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterContextCompressionPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterFusionPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterFusionServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterFilesServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterImageGenerationServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterJevRouterPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterModerationPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterParetoRouterPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterResponseHealingPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterSafeguardResult;
import de.entwicklertraining.openrouter4j.OpenRouterSearchModelsServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterShellServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterSubagentServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterSwitchyardRouterPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterWebSearchPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterWebSearchServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterWebFetchPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterFileParserPlugin;
import de.entwicklertraining.openrouter4j.messages.OpenRouterMessagesRequest;
import de.entwicklertraining.openrouter4j.responses.OpenRouterResponsesRequest;
import de.entwicklertraining.openrouter4j.responses.OpenRouterResponsesResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the typed plugin and server-tool implementations added for the
 * remaining OpenRouter plugin ids / built-in server tools: only explicitly
 * configured fields are emitted, the id/type discriminator is set, and the
 * verbatim escape hatch works (while rejecting the discriminator key on
 * plugins).
 */
class OpenRouterTypedPluginsAndServerToolsTest {

    private OpenRouterChatCompletionRequest.Builder baseBuilder() {
        return new OpenRouterClient().chat().completion()
                .model("test/model")
                .addMessage("user", "Hello");
    }

    // ------------------------------------------------------------------
    // Plugins

    @Test
    void autoRouterPluginEmitsOnlyConfiguredFields() {
        JSONObject plugin = OpenRouterAutoRouterPlugin.builder()
                .allowedModels(List.of("anthropic/*", "openai/*"))
                .excludedModels(List.of("openai/gpt-4o"))
                .costTier("low")
                .pinModel(false)
                .enabled(true)
                .build()
                .toJson();

        assertThat(plugin.getString("id")).isEqualTo("auto-router");
        assertThat(plugin.getJSONArray("allowed_models").toList()).containsExactly("anthropic/*", "openai/*");
        assertThat(plugin.getJSONArray("excluded_models").toList()).containsExactly("openai/gpt-4o");
        assertThat(plugin.getString("cost_tier")).isEqualTo("low");
        assertThat(plugin.getBoolean("pin_model")).isFalse();
        assertThat(plugin.getBoolean("enabled")).isTrue();
        assertThat(plugin.has("cost_quality_tradeoff")).isFalse();
    }

    @Test
    void autoRouterPluginDeprecatedFieldIsSerializedAndOptionHatchWorks() {
        JSONObject plugin = OpenRouterAutoRouterPlugin.builder()
                .costQualityTradeoff(7)
                .option("future_key", "future-value")
                .build()
                .toJson();

        assertThat(plugin.getInt("cost_quality_tradeoff")).isEqualTo(7);
        assertThat(plugin.getString("future_key")).isEqualTo("future-value");
        assertThat(plugin.has("allowed_models")).isFalse();
    }

    @Test
    void autoRouterPluginRejectsIdViaOptionHatch() {
        assertThatThrownBy(() -> OpenRouterAutoRouterPlugin.builder().option("id", "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void autoBetaRouterPluginEmitsOnlyConfiguredFields() {
        JSONObject plugin = OpenRouterAutoBetaRouterPlugin.builder()
                .allowedModels(List.of("openai/*"))
                .excludedModels(List.of("openai/gpt-4o"))
                .costTier("xhigh")
                .costQualityTradeoff(6)
                .enabled(false)
                .build()
                .toJson();

        assertThat(plugin.getString("id")).isEqualTo("auto-beta-router");
        assertThat(plugin.getJSONArray("allowed_models").toList()).containsExactly("openai/*");
        assertThat(plugin.getJSONArray("excluded_models").toList()).containsExactly("openai/gpt-4o");
        assertThat(plugin.getString("cost_tier")).isEqualTo("xhigh");
        assertThat(plugin.getInt("cost_quality_tradeoff")).isEqualTo(6);
        assertThat(plugin.getBoolean("enabled")).isFalse();
        assertThat(plugin.has("pin_model")).isFalse();
    }

    @Test
    void moderationPluginEmitsIdOnly() {
        JSONObject minimal = new OpenRouterModerationPlugin().toJson();
        assertThat(minimal.keySet()).containsExactly("id");
        assertThat(minimal.getString("id")).isEqualTo("moderation");

        JSONObject withHatch = OpenRouterModerationPlugin.builder()
                .option("future_key", 42)
                .build()
                .toJson();
        assertThat(withHatch.getString("id")).isEqualTo("moderation");
        assertThat(withHatch.getInt("future_key")).isEqualTo(42);
    }

    @Test
    void webFetchPluginEmitsOnlyConfiguredFields() {
        JSONObject plugin = OpenRouterWebFetchPlugin.builder()
                .maxUses(10)
                .maxContentTokens(2048)
                .allowedDomains(List.of("example.com"))
                .blockedDomains(List.of("nope.org"))
                .build()
                .toJson();

        assertThat(plugin.getString("id")).isEqualTo("web-fetch");
        assertThat(plugin.getInt("max_uses")).isEqualTo(10);
        assertThat(plugin.getInt("max_content_tokens")).isEqualTo(2048);
        assertThat(plugin.getJSONArray("allowed_domains").toList()).containsExactly("example.com");
        assertThat(plugin.getJSONArray("blocked_domains").toList()).containsExactly("nope.org");
    }

    @Test
    void fileParserPluginEmitsPdfEngineNested() {
        JSONObject plugin = OpenRouterFileParserPlugin.builder()
                .pdfEngine("mistral-ocr")
                .enabled(true)
                .build()
                .toJson();

        assertThat(plugin.getString("id")).isEqualTo("file-parser");
        assertThat(plugin.getJSONObject("pdf").getString("engine")).isEqualTo("mistral-ocr");
        assertThat(plugin.getBoolean("enabled")).isTrue();

        // unset pdf engine -> no pdf key at all
        JSONObject minimal = OpenRouterFileParserPlugin.builder().build().toJson();
        assertThat(minimal.has("pdf")).isFalse();
    }

    @Test
    void responseHealingPluginEmitsIdAndOptionalEnabled() {
        JSONObject minimal = new OpenRouterResponseHealingPlugin().toJson();
        assertThat(minimal.getString("id")).isEqualTo("response-healing");
        assertThat(minimal.has("enabled")).isFalse();

        JSONObject disabled = OpenRouterResponseHealingPlugin.builder().enabled(false).build().toJson();
        assertThat(disabled.getBoolean("enabled")).isFalse();
    }

    @Test
    void contextCompressionPluginEmitsEngine() {
        JSONObject plugin = OpenRouterContextCompressionPlugin.builder()
                .engine("middle-out")
                .build()
                .toJson();

        assertThat(plugin.getString("id")).isEqualTo("context-compression");
        assertThat(plugin.getString("engine")).isEqualTo("middle-out");

        JSONObject minimal = OpenRouterContextCompressionPlugin.builder().build().toJson();
        assertThat(minimal.has("engine")).isFalse();
    }

    @Test
    void paretoRouterPluginEmitsOnlyConfiguredFields() {
        JSONObject plugin = OpenRouterParetoRouterPlugin.builder()
                .maxPrice(5.0)
                .priceSource("weighted_avg")
                .build()
                .toJson();

        assertThat(plugin.getString("id")).isEqualTo("pareto-router");
        assertThat(plugin.getDouble("max_price")).isEqualTo(5.0);
        assertThat(plugin.getString("price_source")).isEqualTo("weighted_avg");
        assertThat(plugin.has("min_coding_score")).isFalse();

        JSONObject scored = OpenRouterParetoRouterPlugin.builder().minCodingScore(0.8).build().toJson();
        assertThat(scored.getDouble("min_coding_score")).isEqualTo(0.8);
    }

    @Test
    void fusionPluginEmitsOnlyConfiguredFields() {
        JSONObject plugin = OpenRouterFusionPlugin.builder()
                .analysisModels(List.of("~anthropic/claude-opus-latest", "~openai/gpt-latest"))
                .model("~anthropic/claude-opus-latest")
                .preset("general-high")
                .maxToolCalls(8)
                .build()
                .toJson();

        assertThat(plugin.getString("id")).isEqualTo("fusion");
        assertThat(plugin.getJSONArray("analysis_models").toList())
                .containsExactly("~anthropic/claude-opus-latest", "~openai/gpt-latest");
        assertThat(plugin.getString("model")).isEqualTo("~anthropic/claude-opus-latest");
        assertThat(plugin.getString("preset")).isEqualTo("general-high");
        assertThat(plugin.getInt("max_tool_calls")).isEqualTo(8);

        JSONObject minimal = OpenRouterFusionPlugin.builder().build().toJson();
        assertThat(minimal.has("analysis_models")).isFalse();
        assertThat(minimal.has("preset")).isFalse();
    }

    // ------------------------------------------------------------------
    // Server tools

    @Test
    void advisorToolEmitsOnlyConfiguredFields() {
        JSONObject tool = OpenRouterAdvisorServerTool.builder()
                .model("~anthropic/claude-opus-latest")
                .name("reviewer")
                .instructions("Give a focused plan.")
                .maxCompletionTokens(2048)
                .forwardTranscript(true)
                .reasoningEffort("high")
                .reasoningMaxTokens(1024)
                .temperature(0.7)
                .build()
                .toJson();

        assertThat(tool.getString("type")).isEqualTo("openrouter:advisor");
        JSONObject parameters = tool.getJSONObject("parameters");
        assertThat(parameters.getString("model")).isEqualTo("~anthropic/claude-opus-latest");
        assertThat(parameters.getString("name")).isEqualTo("reviewer");
        assertThat(parameters.getString("instructions")).isEqualTo("Give a focused plan.");
        assertThat(parameters.getInt("max_completion_tokens")).isEqualTo(2048);
        assertThat(parameters.getBoolean("forward_transcript")).isTrue();
        assertThat(parameters.getJSONObject("reasoning").getString("effort")).isEqualTo("high");
        assertThat(parameters.getJSONObject("reasoning").getInt("max_tokens")).isEqualTo(1024);
        assertThat(parameters.getDouble("temperature")).isEqualTo(0.7);
        assertThat(parameters.has("stream")).isFalse();
    }

    @Test
    void advisorToolEmitsStreamWhenSet() {
        JSONObject tool = OpenRouterAdvisorServerTool.builder().stream(true).build().toJson();

        assertThat(tool.getJSONObject("parameters").getBoolean("stream")).isTrue();
    }

    @Test
    void advisorToolWithoutOptionsEmitsNoParameters() {
        JSONObject tool = OpenRouterAdvisorServerTool.builder().build().toJson();
        assertThat(tool.getString("type")).isEqualTo("openrouter:advisor");
        assertThat(tool.has("parameters")).isFalse();
    }

    @Test
    void bashToolEmitsEngineAndEnvironment() {
        JSONObject tool = OpenRouterBashServerTool.builder()
                .engine("openrouter")
                .environmentContainerId("sess_abc123")
                .environmentFileIds(List.of("or_file_12345678-report.csv"))
                .build()
                .toJson();

        assertThat(tool.getString("type")).isEqualTo("openrouter:bash");
        JSONObject parameters = tool.getJSONObject("parameters");
        assertThat(parameters.getString("engine")).isEqualTo("openrouter");
        JSONObject environment = parameters.getJSONObject("environment");
        assertThat(environment.getString("type")).isEqualTo("container_reference");
        assertThat(environment.getString("container_id")).isEqualTo("sess_abc123");
        assertThat(environment.getJSONArray("file_ids").toList()).containsExactly("or_file_12345678-report.csv");
    }

    @Test
    void bashToolContainerAutoEnvironment() {
        JSONObject tool = OpenRouterBashServerTool.builder()
                .environmentType("container_auto")
                .build()
                .toJson();

        JSONObject environment = tool.getJSONObject("parameters").getJSONObject("environment");
        assertThat(environment.getString("type")).isEqualTo("container_auto");
        assertThat(environment.has("container_id")).isFalse();
    }

    @Test
    void shellToolEmitsOnlyConfiguredFields() {
        JSONObject tool = OpenRouterShellServerTool.builder()
                .engine("openrouter")
                .environmentType("container_auto")
                .build()
                .toJson();

        assertThat(tool.getString("type")).isEqualTo("openrouter:shell");
        JSONObject parameters = tool.getJSONObject("parameters");
        assertThat(parameters.getString("engine")).isEqualTo("openrouter");
        assertThat(parameters.getJSONObject("environment").getString("type")).isEqualTo("container_auto");

        JSONObject minimal = OpenRouterShellServerTool.builder().build().toJson();
        assertThat(minimal.has("parameters")).isFalse();
    }

    @Test
    void shellToolContainerReferenceImpliedByContainerId() {
        JSONObject tool = OpenRouterShellServerTool.builder()
                .environmentContainerId("sess_x")
                .environmentFileIds(List.of("f1"))
                .build()
                .toJson();

        JSONObject environment = tool.getJSONObject("parameters").getJSONObject("environment");
        assertThat(environment.getString("type")).isEqualTo("container_reference");
        assertThat(environment.getString("container_id")).isEqualTo("sess_x");
        assertThat(environment.getJSONArray("file_ids").toList()).containsExactly("f1");
    }

    @Test
    void applyPatchToolEmitsEngineOnlyWhenSet() {
        JSONObject tool = OpenRouterApplyPatchServerTool.builder().engine("openrouter").build().toJson();
        assertThat(tool.getString("type")).isEqualTo("openrouter:apply_patch");
        assertThat(tool.getJSONObject("parameters").getString("engine")).isEqualTo("openrouter");

        JSONObject minimal = OpenRouterApplyPatchServerTool.builder().build().toJson();
        assertThat(minimal.has("parameters")).isFalse();
    }

    @Test
    void filesToolEmitsTypeOnlyByDefault() {
        JSONObject minimal = new OpenRouterFilesServerTool().toJson();
        assertThat(minimal.keySet()).containsExactly("type");
        assertThat(minimal.getString("type")).isEqualTo("openrouter:files");

        JSONObject withHatch = OpenRouterFilesServerTool.builder().option("future_key", "v").build().toJson();
        assertThat(withHatch.getJSONObject("parameters").getString("future_key")).isEqualTo("v");
    }

    @Test
    void fusionToolEmitsOnlyConfiguredFields() {
        JSONObject tool = OpenRouterFusionServerTool.builder()
                .analysisModels(List.of("~openai/gpt-latest"))
                .model("~anthropic/claude-opus-latest")
                .maxToolCalls(12)
                .maxCompletionTokens(16384)
                .reasoningEffort("medium")
                .reasoningMaxTokens(2048)
                .temperature(0.7)
                .build()
                .toJson();

        assertThat(tool.getString("type")).isEqualTo("openrouter:fusion");
        JSONObject parameters = tool.getJSONObject("parameters");
        assertThat(parameters.getJSONArray("analysis_models").toList()).containsExactly("~openai/gpt-latest");
        assertThat(parameters.getString("model")).isEqualTo("~anthropic/claude-opus-latest");
        assertThat(parameters.getInt("max_tool_calls")).isEqualTo(12);
        assertThat(parameters.getInt("max_completion_tokens")).isEqualTo(16384);
        assertThat(parameters.getJSONObject("reasoning").getString("effort")).isEqualTo("medium");
        assertThat(parameters.getJSONObject("reasoning").getInt("max_tokens")).isEqualTo(2048);
        assertThat(parameters.getDouble("temperature")).isEqualTo(0.7);
    }

    @Test
    void imageGenerationToolEmitsModelAndEscapeHatch() {
        JSONObject tool = OpenRouterImageGenerationServerTool.builder()
                .model("openai/gpt-5-image")
                .option("aspect_ratio", "16:9")
                .option("quality", "high")
                .build()
                .toJson();

        assertThat(tool.getString("type")).isEqualTo("openrouter:image_generation");
        JSONObject parameters = tool.getJSONObject("parameters");
        assertThat(parameters.getString("model")).isEqualTo("openai/gpt-5-image");
        assertThat(parameters.getString("aspect_ratio")).isEqualTo("16:9");
        assertThat(parameters.getString("quality")).isEqualTo("high");
    }

    @Test
    void searchModelsToolEmitsMaxResultsOnlyWhenSet() {
        JSONObject tool = OpenRouterSearchModelsServerTool.builder().maxResults(5).build().toJson();
        assertThat(tool.getString("type")).isEqualTo("openrouter:experimental__search_models");
        assertThat(tool.getJSONObject("parameters").getInt("max_results")).isEqualTo(5);

        JSONObject minimal = OpenRouterSearchModelsServerTool.builder().build().toJson();
        assertThat(minimal.has("parameters")).isFalse();
    }

    @Test
    void subagentToolEmitsOnlyConfiguredFields() {
        JSONObject tool = OpenRouterSubagentServerTool.builder()
                .model("~anthropic/claude-haiku-latest")
                .name("summarizer")
                .instructions("Summarize the findings.")
                .maxToolCalls(5)
                .maxCompletionTokens(2048)
                .inheritedFunctionNames(List.of("lookup_order"))
                .reasoningEffort("low")
                .reasoningMaxTokens(512)
                .temperature(0.5)
                .build()
                .toJson();

        assertThat(tool.getString("type")).isEqualTo("openrouter:subagent");
        JSONObject parameters = tool.getJSONObject("parameters");
        assertThat(parameters.getString("model")).isEqualTo("~anthropic/claude-haiku-latest");
        assertThat(parameters.getString("name")).isEqualTo("summarizer");
        assertThat(parameters.getString("instructions")).isEqualTo("Summarize the findings.");
        assertThat(parameters.getInt("max_tool_calls")).isEqualTo(5);
        assertThat(parameters.getInt("max_completion_tokens")).isEqualTo(2048);
        assertThat(parameters.getJSONArray("inherited_function_names").toList()).containsExactly("lookup_order");
        assertThat(parameters.getJSONObject("reasoning").getString("effort")).isEqualTo("low");
        assertThat(parameters.getJSONObject("reasoning").getInt("max_tokens")).isEqualTo(512);
        assertThat(parameters.getDouble("temperature")).isEqualTo(0.5);
        assertThat(parameters.has("inherit_functions")).isFalse();
    }

    @Test
    void subagentToolEmitsInheritFunctionsWhenSet() {
        JSONObject tool = OpenRouterSubagentServerTool.builder().inheritFunctions(true).build().toJson();

        assertThat(tool.getJSONObject("parameters").getBoolean("inherit_functions")).isTrue();
    }

    // ------------------------------------------------------------------
    // Emission through the request builder

    @Test
    void typedPluginsAndServerToolsAreEmittedThroughTheRequestBuilder() {
        OpenRouterChatCompletionRequest request = baseBuilder()
                .addPlugin(OpenRouterAutoRouterPlugin.builder().costTier("low").build())
                .addPlugin(OpenRouterFileParserPlugin.builder().pdfEngine("mistral-ocr").build())
                .addServerTool(OpenRouterAdvisorServerTool.builder().name("reviewer").build())
                .addServerTool(OpenRouterSearchModelsServerTool.builder().maxResults(10).build())
                .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getJSONArray("plugins").length()).isEqualTo(2);
        assertThat(body.getJSONArray("plugins").getJSONObject(0).getString("id")).isEqualTo("auto-router");
        assertThat(body.getJSONArray("plugins").getJSONObject(1).getJSONObject("pdf").getString("engine"))
                .isEqualTo("mistral-ocr");
        assertThat(body.getJSONArray("tools").length()).isEqualTo(2);
        assertThat(body.getJSONArray("tools").getJSONObject(0).getString("type")).isEqualTo("openrouter:advisor");
        assertThat(body.getJSONArray("tools").getJSONObject(0).getJSONObject("parameters").getString("name"))
                .isEqualTo("reviewer");
        assertThat(body.getJSONArray("tools").getJSONObject(1).getJSONObject("parameters").getInt("max_results"))
                .isEqualTo(10);
    }

    @Test
    void switchyardRouterPluginEmitsIdOnlyWhenNoAlgorithm() {
        JSONObject json = OpenRouterSwitchyardRouterPlugin.builder().build().toJson();
        assertThat(json.getString("id")).isEqualTo("switchyard-router");
        assertThat(json.keySet()).containsExactly("id");
    }

    @Test
    void switchyardRouterPluginEmitsAlgorithmWhenSet() {
        JSONObject json = OpenRouterSwitchyardRouterPlugin.builder()
                .algorithm("composite")
                .build().toJson();
        assertThat(json.getString("id")).isEqualTo("switchyard-router");
        assertThat(json.getString("algorithm")).isEqualTo("composite");
    }

    @Test
    void switchyardRouterPluginRejectsIdViaOptionHatch() {
        assertThatThrownBy(() -> OpenRouterSwitchyardRouterPlugin.builder().option("id", "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void switchyardRouterPluginOptionHatchPassesThrough() {
        JSONObject json = OpenRouterSwitchyardRouterPlugin.builder()
                .option("future_key", "v")
                .build().toJson();
        assertThat(json.getString("future_key")).isEqualTo("v");
    }

    @Test
    void webSearchPluginXSearchIsEmittedWhenSet() {
        OpenRouterWebSearchServerTool.XSearchOptions xSearch = OpenRouterWebSearchServerTool.XSearchOptions.builder()
                .allowedXHandles(List.of("openai", "xai"))
                .fromDate("2025-01-01")
                .enableImageUnderstanding(true)
                .build();
        JSONObject json = OpenRouterWebSearchPlugin.builder()
                .engine("native")
                .xSearch(xSearch)
                .build().toJson();
        JSONObject xSearchJson = json.getJSONObject("x_search");
        assertThat(xSearchJson.getJSONArray("allowed_x_handles").toList()).containsExactly("openai", "xai");
        assertThat(xSearchJson.getString("from_date")).isEqualTo("2025-01-01");
        assertThat(xSearchJson.getBoolean("enable_image_understanding")).isTrue();
        assertThat(xSearchJson.has("excluded_x_handles")).isFalse();
        assertThat(xSearchJson.has("to_date")).isFalse();
        assertThat(xSearchJson.has("enable_video_understanding")).isFalse();
    }

    @Test
    void webSearchPluginXSearchFalseFlagsAreEmitted() {
        OpenRouterWebSearchServerTool.XSearchOptions xSearch = OpenRouterWebSearchServerTool.XSearchOptions.builder()
                .enableImageUnderstanding(false)
                .enableVideoUnderstanding(false)
                .build();
        JSONObject json = OpenRouterWebSearchPlugin.builder().xSearch(xSearch).build().toJson();
        JSONObject xSearchJson = json.getJSONObject("x_search");
        assertThat(xSearchJson.getBoolean("enable_image_understanding")).isFalse();
        assertThat(xSearchJson.getBoolean("enable_video_understanding")).isFalse();
    }

    @Test
    void webSearchPluginXSearchIsAbsentWhenUnset() {
        JSONObject json = OpenRouterWebSearchPlugin.builder().engine("native").build().toJson();
        assertThat(json.has("x_search")).isFalse();
        assertThat(OpenRouterWebSearchPlugin.builder().build().xSearch()).isNull();
    }

    // ------------------------------------------------------------------
    // alignment plugin + response alignment() / alignmentCalls()

    @Test
    void alignmentPluginEmitsRulesAndOnlyConfiguredFields() {
        OpenRouterAlignmentPlugin minimal = OpenRouterAlignmentPlugin.builder()
                .rule("Never offer a discount.", "content")
                .rule("Never call delete_file.", List.of("tool_calls", "refusal"))
                .build();

        JSONObject minimalJson = minimal.toJson();
        assertThat(minimalJson.keySet()).containsExactlyInAnyOrder("id", "rules");
        assertThat(minimalJson.getString("id")).isEqualTo("alignment");
        JSONArray rulesArr = minimalJson.getJSONArray("rules");
        assertThat(rulesArr.length()).isEqualTo(2);
        assertThat(rulesArr.getJSONObject(0).keySet()).containsExactlyInAnyOrder("rule", "watch");
        assertThat(rulesArr.getJSONObject(0).getString("rule")).isEqualTo("Never offer a discount.");
        assertThat(rulesArr.getJSONObject(0).getJSONArray("watch").toList()).containsExactly("content");
        assertThat(rulesArr.getJSONObject(1).getString("rule")).isEqualTo("Never call delete_file.");
        assertThat(rulesArr.getJSONObject(1).getJSONArray("watch").toList())
                .containsExactly("tool_calls", "refusal");
        assertThat(minimal.id()).isEqualTo("alignment");
        assertThat(minimal.rules()).hasSize(2);
        assertThat(minimal.rules().get(0).rule()).isEqualTo("Never offer a discount.");
        assertThat(minimal.rules().get(0).watch()).containsExactly("content");
        assertThat(minimal.mode()).isNull();
        assertThat(minimal.threshold()).isNull();
        assertThat(minimal.instruct()).isNull();
        assertThat(minimal.maxRetries()).isNull();

        // Full configuration including false-is-a-set-option for instruct and 0 for maxRetries
        JSONObject hatchObj = new JSONObject().put("nested", "v");
        OpenRouterAlignmentPlugin full = OpenRouterAlignmentPlugin.builder()
                .rule("Rule 1", "content")
                .mode("enforce")
                .threshold(0.75)
                .instruct(false)
                .maxRetries(0)
                .option("future_key", hatchObj)
                .build();
        hatchObj.put("nested", "mutated");

        JSONObject fullJson = full.toJson();
        assertThat(fullJson.keySet()).containsExactlyInAnyOrder(
                "id", "rules", "mode", "threshold", "instruct", "max_retries", "future_key");
        assertThat(fullJson.getString("mode")).isEqualTo("enforce");
        assertThat(fullJson.getDouble("threshold")).isEqualTo(0.75);
        assertThat(fullJson.getBoolean("instruct")).isFalse();
        assertThat(fullJson.getInt("max_retries")).isEqualTo(0);
        assertThat(fullJson.getJSONObject("future_key").getString("nested")).isEqualTo("v");
        assertThat(full.mode()).isEqualTo("enforce");
        assertThat(full.threshold()).isEqualTo(0.75);
        assertThat(full.instruct()).isFalse();
        assertThat(full.maxRetries()).isEqualTo(0);

        // Set-then-null unsets optional fields, and rules(List) replaces accumulated rules
        OpenRouterAlignmentPlugin replacedAndUnset = OpenRouterAlignmentPlugin.builder()
                .rule("Old rule", "content")
                .rules(List.of(OpenRouterAlignmentPlugin.Rule.of("New rule", "images", "audio")))
                .mode("audit")
                .mode(null)
                .threshold(0.5)
                .threshold(null)
                .instruct(true)
                .instruct(null)
                .maxRetries(2)
                .maxRetries(null)
                .build();
        JSONObject replacedJson = replacedAndUnset.toJson();
        assertThat(replacedJson.keySet()).containsExactlyInAnyOrder("id", "rules");
        assertThat(replacedJson.getJSONArray("rules").length()).isEqualTo(1);
        assertThat(replacedJson.getJSONArray("rules").getJSONObject(0).getString("rule")).isEqualTo("New rule");
        assertThat(replacedJson.getJSONArray("rules").getJSONObject(0).getJSONArray("watch").toList())
                .containsExactly("images", "audio");
    }

    @Test
    void alignmentPluginValidatesRequiredRulesAndFieldBounds() {
        // Missing rules at build()
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.builder().build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least one rule");

        // Clearing rules with empty list then building fails
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.builder()
                .rule("R", "content")
                .rules(List.of())
                .build())
                .isInstanceOf(IllegalStateException.class);

        // Rule text bounds: 1-200 code points (200 emojis accepted, 201 rejected)
        String twoHundredEmojis = "\uD83D\uDE00".repeat(200);
        assertThat(OpenRouterAlignmentPlugin.Rule.of(twoHundredEmojis, "content").rule())
                .isEqualTo(twoHundredEmojis);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.Rule.of(null, "content"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.Rule.of("", "content"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.Rule.of("a".repeat(201), "content"))
                .isInstanceOf(IllegalArgumentException.class);

        // Watch bounds: 1-5 unique non-empty items; unknown field names allowed verbatim
        OpenRouterAlignmentPlugin.Rule fiveWatched = OpenRouterAlignmentPlugin.Rule.of(
                "Check all", "content", "tool_calls", "refusal", "images", "custom_future_field");
        assertThat(fiveWatched.watch()).containsExactly(
                "content", "tool_calls", "refusal", "images", "custom_future_field");
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.Rule.of("R"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.Rule.of("R", (String[]) null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.Rule.of("R", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.Rule.of("R", "content", "content"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate");
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.Rule.of("R", "content", ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.Rule.of(
                "R", "f1", "f2", "f3", "f4", "f5", "f6"))
                .isInstanceOf(IllegalArgumentException.class);

        // 128-rule cap via both rule(...) and rules(List), plus null guards on addRule/rules
        OpenRouterAlignmentPlugin.Builder maxBuilder = OpenRouterAlignmentPlugin.builder();
        for (int i = 0; i < 128; i++) {
            maxBuilder.rule("Rule " + i, "content");
        }
        assertThat(maxBuilder.build().rules()).hasSize(128);
        assertThatThrownBy(() -> maxBuilder.rule("Rule 129", "content"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.builder().addRule(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.builder()
                .rules(Collections.nCopies(129, OpenRouterAlignmentPlugin.Rule.of("R", "content"))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.builder()
                .rules(Arrays.asList(OpenRouterAlignmentPlugin.Rule.of("R", "content"), null)))
                .isInstanceOf(IllegalArgumentException.class);

        // Escape-hatch rules via option("rules", ...) satisfies build() when typed rules is empty,
        // and overrides typed rules when both are set (extraOptions is serialized after typed fields)
        JSONArray rawRules = new JSONArray()
                .put(new JSONObject().put("rule", "Raw rule").put("watch", new JSONArray().put("content")));
        OpenRouterAlignmentPlugin viaOption = OpenRouterAlignmentPlugin.builder()
                .option("rules", rawRules)
                .option("nullable_extra", null)
                .build();
        assertThat(viaOption.rules()).isEmpty();
        JSONObject viaOptionJson = viaOption.toJson();
        assertThat(viaOptionJson.getJSONArray("rules").getJSONObject(0).getString("rule"))
                .isEqualTo("Raw rule");
        assertThat(viaOptionJson.has("nullable_extra")).isTrue();
        assertThat(viaOptionJson.isNull("nullable_extra")).isTrue();

        OpenRouterAlignmentPlugin bothTypedAndOption = OpenRouterAlignmentPlugin.builder()
                .rule("Typed rule", "content")
                .option("rules", rawRules)
                .build();
        assertThat(bothTypedAndOption.toJson().getJSONArray("rules").getJSONObject(0).getString("rule"))
                .isEqualTo("Raw rule");

        // Mode, threshold, maxRetries, and option("id") guards
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.builder().mode("invalid"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(OpenRouterAlignmentPlugin.builder()
                .rule("R", "content").threshold(0.01).build().threshold()).isEqualTo(0.01);
        assertThat(OpenRouterAlignmentPlugin.builder()
                .rule("R", "content").threshold(1.0).build().threshold()).isEqualTo(1.0);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.builder().threshold(0.009))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.builder().threshold(1.01))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.builder().threshold(Double.NaN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.builder().maxRetries(-1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.builder().option("id", "other"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterAlignmentPlugin.builder().option(null, "v"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chatAndResponsesExposeAlignmentAndSwallowMalformedShapes() {
        OpenRouterChatCompletionRequest chatReq = baseBuilder().build();
        OpenRouterChatCompletionResponse chatResp = new OpenRouterChatCompletionResponse(new JSONObject("""
                {
                  "id": "gen-align-1",
                  "choices": [{"index": 0, "message": {"role": "assistant", "content": "Hi"}, "finish_reason": "stop"}],
                  "alignment": {
                    "calls": [
                      {
                        "call": 1,
                        "outcome": "blocked",
                        "rules": [{"applicable": true, "probability": 0.92, "broken": true}]
                      },
                      "not-an-object",
                      {
                        "call": 2,
                        "outcome": "allowed",
                        "rules": [{"applicable": true, "probability": 0.04, "broken": false}]
                      }
                    ]
                  }
                }
                """), chatReq);

        assertThat(chatResp.alignment()).isNotNull();
        assertThat(chatResp.alignmentCalls()).hasSize(2);
        assertThat(chatResp.alignmentCalls().get(0).getInt("call")).isEqualTo(1);
        assertThat(chatResp.alignmentCalls().get(0).getString("outcome")).isEqualTo("blocked");
        assertThat(chatResp.alignmentCalls().get(1).getInt("call")).isEqualTo(2);
        assertThat(chatResp.alignmentCalls().get(1).getString("outcome")).isEqualTo("allowed");

        // Absent, explicit JSON null, wrong-typed alignment, wrong-typed calls
        OpenRouterChatCompletionResponse absent = new OpenRouterChatCompletionResponse(new JSONObject("{}"), chatReq);
        assertThat(absent.alignment()).isNull();
        assertThat(absent.alignmentCalls()).isEmpty();

        OpenRouterChatCompletionResponse nullAlignment = new OpenRouterChatCompletionResponse(
                new JSONObject("{\"alignment\": null}"), chatReq);
        assertThat(nullAlignment.alignment()).isNull();
        assertThat(nullAlignment.alignmentCalls()).isEmpty();

        OpenRouterChatCompletionResponse badAlignment = new OpenRouterChatCompletionResponse(
                new JSONObject("{\"alignment\": \"wrong\"}"), chatReq);
        assertThat(badAlignment.alignment()).isNull();
        assertThat(badAlignment.alignmentCalls()).isEmpty();

        OpenRouterChatCompletionResponse badCalls = new OpenRouterChatCompletionResponse(
                new JSONObject("{\"alignment\": {\"calls\": 42}}"), chatReq);
        assertThat(badCalls.alignment()).isNotNull();
        assertThat(badCalls.alignmentCalls()).isEmpty();

        // Responses surface
        OpenRouterResponsesRequest respReq = new OpenRouterClient().responses()
                .model("openai/gpt-4.1-mini")
                .input("Hello")
                .build();
        OpenRouterResponsesResponse respWithAlign = respReq.createResponse("""
                {
                  "id": "resp-1",
                  "object": "response",
                  "status": "completed",
                  "output": [],
                  "alignment": {
                    "calls": [
                      {"call": 1, "outcome": "unavailable", "reason": "time_limit"}
                    ]
                  }
                }
                """);
        assertThat(respWithAlign.alignment()).isNotNull();
        assertThat(respWithAlign.alignmentCalls()).hasSize(1);
        assertThat(respWithAlign.alignmentCalls().get(0).getString("reason")).isEqualTo("time_limit");
        assertThat(respReq.createResponse("{}").alignment()).isNull();
        assertThat(respReq.createResponse("{}").alignmentCalls()).isEmpty();

        // Streaming accumulator + synthetic response carry-through
        StreamingToolCallAccumulator acc = new StreamingToolCallAccumulator(new de.entwicklertraining.api.base.streaming.StreamingResponseHandler<>() {
            @Override
            public void onData(String data) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable t) {
            }
        });
        acc.onData("{\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\",\"content\":\"Ok\"},\"finish_reason\":\"stop\"}]}");
        OpenRouterChatCompletionCallHandler handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
        // Before any alignment chunk arrives, synthetic response omits alignment and safeguard_results
        JSONObject contentOnlySynth = handler.buildSyntheticResponseJson(acc, "openai/gpt-4.1-mini");
        assertThat(contentOnlySynth.has("alignment")).isFalse();
        assertThat(contentOnlySynth.has("safeguard_results")).isFalse();

        acc.onData("{\"choices\":[],\"alignment\":{\"calls\":[{\"call\":1,\"outcome\":\"allowed\",\"rules\":[]}]}}");
        // A later chunk with wrong-typed alignment does not clobber the valid alignment object
        acc.onData("{\"choices\":[],\"alignment\":\"wrong-type\"}");

        JSONObject synthJson = handler.buildSyntheticResponseJson(acc, "openai/gpt-4.1-mini");
        OpenRouterChatCompletionResponse synthResp = new OpenRouterChatCompletionResponse(synthJson, chatReq);
        assertThat(synthResp.alignment()).isNotNull();
        assertThat(synthResp.alignmentCalls()).hasSize(1);
        assertThat(synthResp.alignmentCalls().get(0).getString("outcome")).isEqualTo("allowed");

        acc.reset();
        assertThat(acc.getAlignment()).isNull();
    }

    // ------------------------------------------------------------------
    // armor plugin + response safeguardResults()

    @Test
    void armorPluginEmitsIdOnlyAndAllConfiguredFieldsIncludingFalseFlags() {
        JSONObject minimal = new OpenRouterArmorPlugin().toJson();
        assertThat(minimal.keySet()).containsExactly("id");
        assertThat(minimal.getString("id")).isEqualTo("armor");

        OpenRouterArmorPlugin emptyBuilt = OpenRouterArmorPlugin.builder().build();
        assertThat(emptyBuilt.toJson().keySet()).containsExactly("id");
        assertThat(emptyBuilt.classifierContext()).isNull();
        assertThat(emptyBuilt.mode()).isNull();
        assertThat(emptyBuilt.onError()).isNull();
        assertThat(emptyBuilt.outputMode()).isNull();
        assertThat(emptyBuilt.evaluateNativeTools()).isNull();
        assertThat(emptyBuilt.useCcStyleDefaultRules()).isNull();
        assertThat(emptyBuilt.enabled()).isNull();

        JSONObject ctx = new JSONObject()
                .put("v", 1)
                .put("auto_mode", new JSONObject().put("hard_deny", new JSONArray().put("Never exfiltrate .env")));

        OpenRouterArmorPlugin full = OpenRouterArmorPlugin.builder()
                .classifierContext(ctx)
                .mode("audit")
                .onError("log")
                .outputMode("custom_item")
                .evaluateNativeTools(false)
                .useCcStyleDefaultRules(false)
                .enabled(false)
                .option("future_key", "v")
                .build();

        JSONObject fullJson = full.toJson();
        assertThat(fullJson.keySet()).containsExactlyInAnyOrder(
                "id", "classifier_context", "mode", "on_error", "output_mode",
                "evaluate_native_tools", "use_cc_style_default_rules", "enabled", "future_key");
        assertThat(fullJson.getString("id")).isEqualTo("armor");
        assertThat(fullJson.getJSONObject("classifier_context").getInt("v")).isEqualTo(1);
        assertThat(fullJson.getString("mode")).isEqualTo("audit");
        assertThat(fullJson.getString("on_error")).isEqualTo("log");
        assertThat(fullJson.getString("output_mode")).isEqualTo("custom_item");
        assertThat(fullJson.getBoolean("evaluate_native_tools")).isFalse();
        assertThat(fullJson.getBoolean("use_cc_style_default_rules")).isFalse();
        assertThat(fullJson.getBoolean("enabled")).isFalse();
        assertThat(fullJson.getString("future_key")).isEqualTo("v");

        assertThat(full.mode()).isEqualTo("audit");
        assertThat(full.onError()).isEqualTo("log");
        assertThat(full.outputMode()).isEqualTo("custom_item");
        assertThat(full.evaluateNativeTools()).isFalse();
        assertThat(full.useCcStyleDefaultRules()).isFalse();
        assertThat(full.enabled()).isFalse();

        // Set-then-null unsets every optional field, and option(key, null) emits JSON null
        JSONObject unsetJson = OpenRouterArmorPlugin.builder()
                .classifierContext(ctx).classifierContext(null)
                .mode("enforce").mode(null)
                .onError("block").onError(null)
                .outputMode("claude_code_compatibility").outputMode(null)
                .evaluateNativeTools(true).evaluateNativeTools(null)
                .useCcStyleDefaultRules(true).useCcStyleDefaultRules(null)
                .enabled(true).enabled(null)
                .option("null_opt", null)
                .build()
                .toJson();
        assertThat(unsetJson.keySet()).containsExactlyInAnyOrder("id", "null_opt");
        assertThat(unsetJson.isNull("null_opt")).isTrue();
    }

    @Test
    void armorPluginDetachedCopyAndClosedEnumValidation() {
        JSONObject callerCtx = new JSONObject().put("v", 1);
        OpenRouterArmorPlugin.Builder builder = OpenRouterArmorPlugin.builder()
                .classifierContext(callerCtx);
        // Mutate caller's object BEFORE build()
        callerCtx.put("v", 999);
        OpenRouterArmorPlugin plugin = builder.build();
        // Mutate caller's object AFTER build() and mutate read-back copy
        callerCtx.put("injected", true);
        plugin.classifierContext().put("v", 888);

        assertThat(plugin.toJson().getJSONObject("classifier_context").getInt("v")).isEqualTo(1);
        assertThat(plugin.toJson().getJSONObject("classifier_context").has("injected")).isFalse();

        // Closed enum validation on mode, onError, outputMode
        assertThatThrownBy(() -> OpenRouterArmorPlugin.builder().mode("unknown"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterArmorPlugin.builder().onError("ignore"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterArmorPlugin.builder().outputMode("xml"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterArmorPlugin.builder().option("id", "other"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterArmorPlugin.builder().option(null, "v"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chatAndResponsesExposeSafeguardResultsAndSwallowMalformedShapes() {
        OpenRouterChatCompletionRequest chatReq = baseBuilder().build();
        OpenRouterChatCompletionResponse chatResp = new OpenRouterChatCompletionResponse(new JSONObject("""
                {
                  "id": "gen-armor-1",
                  "choices": [{"index": 0, "message": {"role": "assistant", "content": "Blocked"}, "finish_reason": "stop"}],
                  "safeguard_results": [
                    {
                      "type": "dangerous_tool_use",
                      "status": {
                        "type": "available",
                        "tool_uses": {
                          "call_abc": {
                            "type": "evaluated",
                            "outcome": "flagged",
                            "explanation": "[Git Push]"
                          },
                          "call_def": {
                            "type": "evaluated",
                            "outcome": "not_flagged"
                          }
                        }
                      }
                    },
                    "malformed-entry",
                    {
                      "type": 123,
                      "status": "not-an-object"
                    }
                  ]
                }
                """), chatReq);

        List<OpenRouterSafeguardResult> results = chatResp.safeguardResults();
        assertThat(results).hasSize(2);
        OpenRouterSafeguardResult first = results.get(0);
        assertThat(first.type()).isEqualTo("dangerous_tool_use");
        assertThat(first.status()).isNotNull();
        assertThat(first.status().getString("type")).isEqualTo("available");
        assertThat(first.toolUses()).isNotNull();
        assertThat(first.toolUses().getJSONObject("call_abc").getString("outcome")).isEqualTo("flagged");
        assertThat(first.toolUses().getJSONObject("call_abc").getString("explanation")).isEqualTo("[Git Push]");
        assertThat(first.json().getString("type")).isEqualTo("dangerous_tool_use");

        // Wrong-typed scalar is swallowed to null, not coerced to "123"
        OpenRouterSafeguardResult second = results.get(1);
        assertThat(second.type()).isNull();
        assertThat(second.status()).isNull();
        assertThat(second.toolUses()).isNull();

        // Null constructor tolerance and absent/null/wrong-type response legs
        OpenRouterSafeguardResult fromNull = new OpenRouterSafeguardResult(null);
        assertThat(fromNull.type()).isNull();
        assertThat(fromNull.status()).isNull();
        assertThat(fromNull.toolUses()).isNull();
        assertThat(fromNull.json()).isNotNull();

        assertThat(new OpenRouterChatCompletionResponse(new JSONObject("{}"), chatReq).safeguardResults()).isEmpty();
        assertThat(new OpenRouterChatCompletionResponse(
                new JSONObject("{\"safeguard_results\": null}"), chatReq).safeguardResults()).isEmpty();
        assertThat(new OpenRouterChatCompletionResponse(
                new JSONObject("{\"safeguard_results\": \"wrong\"}"), chatReq).safeguardResults()).isEmpty();

        // Responses surface
        OpenRouterResponsesRequest respReq = new OpenRouterClient().responses()
                .model("openai/gpt-4.1")
                .input("Hello")
                .build();
        OpenRouterResponsesResponse respWithArmor = respReq.createResponse("""
                {
                  "id": "resp-armor-1",
                  "object": "response",
                  "status": "completed",
                  "output": [],
                  "safeguard_results": [
                    {
                      "type": "dangerous_tool_use",
                      "status": {"type": "available", "tool_uses": {"call_1": {"type": "skipped"}}}
                    }
                  ]
                }
                """);
        assertThat(respWithArmor.safeguardResults()).hasSize(1);
        assertThat(respWithArmor.safeguardResults().get(0).type()).isEqualTo("dangerous_tool_use");
        assertThat(respWithArmor.safeguardResults().get(0).toolUses().getJSONObject("call_1").getString("type"))
                .isEqualTo("skipped");
        assertThat(respReq.createResponse("{}").safeguardResults()).isEmpty();

        // Streaming accumulator + synthetic response carry-through
        StreamingToolCallAccumulator acc = new StreamingToolCallAccumulator(new de.entwicklertraining.api.base.streaming.StreamingResponseHandler<>() {
            @Override
            public void onData(String data) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable t) {
            }
        });
        acc.onData("{\"choices\":[],\"safeguard_results\":[{\"type\":\"dangerous_tool_use\",\"status\":{\"type\":\"available\",\"tool_uses\":{}}}]}");
        // A later chunk with wrong-typed safeguard_results does not clobber the valid array
        acc.onData("{\"choices\":[],\"safeguard_results\":{\"wrong\":\"object\"}}");
        OpenRouterChatCompletionCallHandler handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
        JSONObject synthJson = handler.buildSyntheticResponseJson(acc, "openai/gpt-4.1");
        OpenRouterChatCompletionResponse synthResp = new OpenRouterChatCompletionResponse(synthJson, chatReq);
        assertThat(synthResp.safeguardResults()).hasSize(1);
        assertThat(synthResp.safeguardResults().get(0).type()).isEqualTo("dangerous_tool_use");

        acc.reset();
        assertThat(acc.getSafeguardResults()).isNull();
    }

    // ------------------------------------------------------------------
    // jev-router plugin

    @Test
    void jevRouterPluginEmitsIdOnlyAndConfiguredFields() {
        JSONObject minimal = new OpenRouterJevRouterPlugin().toJson();
        assertThat(minimal.keySet()).containsExactly("id");
        assertThat(minimal.getString("id")).isEqualTo("jev-router");

        OpenRouterJevRouterPlugin plugin = OpenRouterJevRouterPlugin.builder()
                .models("anthropic/*", "~openai/gpt-luna-latest")
                .excludedModels("openai/gpt-6-astra")
                .costTier("medium")
                .build();

        JSONObject json = plugin.toJson();
        assertThat(json.keySet()).containsExactlyInAnyOrder("id", "models", "excluded_models", "cost_tier");
        assertThat(json.getString("id")).isEqualTo("jev-router");
        assertThat(json.getJSONArray("models").toList())
                .containsExactly("anthropic/*", "~openai/gpt-luna-latest");
        assertThat(json.getJSONArray("excluded_models").toList())
                .containsExactly("openai/gpt-6-astra");
        assertThat(json.getString("cost_tier")).isEqualTo("medium");

        assertThat(plugin.id()).isEqualTo("jev-router");
        assertThat(plugin.models()).containsExactly("anthropic/*", "~openai/gpt-luna-latest");
        assertThat(plugin.allowedModels()).isEmpty();
        assertThat(plugin.excludedModels()).containsExactly("openai/gpt-6-astra");
        assertThat(plugin.costTier()).isEqualTo("medium");
    }

    @Test
    void jevRouterPluginEmitsBothModelsAndAllowedModelsAndAcceptsOperatorCostTier() {
        OpenRouterJevRouterPlugin plugin = OpenRouterJevRouterPlugin.builder()
                .models(List.of("anthropic/*"))
                .allowedModels(List.of("google/*"))
                .costTier("operator-custom-tier")
                .option("future_key", 7)
                .build();

        JSONObject json = plugin.toJson();
        assertThat(json.keySet()).containsExactlyInAnyOrder(
                "id", "models", "allowed_models", "cost_tier", "future_key");
        assertThat(json.getJSONArray("models").toList()).containsExactly("anthropic/*");
        assertThat(json.getJSONArray("allowed_models").toList()).containsExactly("google/*");
        assertThat(json.getString("cost_tier")).isEqualTo("operator-custom-tier");
        assertThat(json.getInt("future_key")).isEqualTo(7);
    }

    @Test
    void jevRouterPluginReplaceClearAndValidation() {
        // Replacement and empty/null clearing
        OpenRouterJevRouterPlugin replaced = OpenRouterJevRouterPlugin.builder()
                .models("first/*")
                .models("second/*")
                .allowedModels("allow-1/*")
                .allowedModels(List.of())
                .excludedModels("excl-1/*")
                .excludedModels()
                .costTier("high")
                .costTier(null)
                .build();

        JSONObject json = replaced.toJson();
        assertThat(json.keySet()).containsExactlyInAnyOrder("id", "models");
        assertThat(json.getJSONArray("models").toList()).containsExactly("second/*");
        assertThat(replaced.allowedModels()).isEmpty();
        assertThat(replaced.excludedModels()).isEmpty();
        assertThat(replaced.costTier()).isNull();

        // Explicit null List cast, null String[] cast, and empty varargs all clear,
        // and option(key, null) emits JSON null
        OpenRouterJevRouterPlugin clearedViaNull = OpenRouterJevRouterPlugin.builder()
                .models("first/*")
                .models((List<String>) null)
                .models("again/*")
                .models((String[]) null)
                .models("third/*")
                .models()
                .allowedModels("a/*")
                .allowedModels((List<String>) null)
                .allowedModels("a2/*")
                .allowedModels((String[]) null)
                .allowedModels("a3/*")
                .allowedModels()
                .excludedModels("e/*")
                .excludedModels((List<String>) null)
                .excludedModels("e2/*")
                .excludedModels((String[]) null)
                .option("null_opt", null)
                .build();
        assertThat(clearedViaNull.toJson().keySet()).containsExactlyInAnyOrder("id", "null_opt");
        assertThat(clearedViaNull.toJson().isNull("null_opt")).isTrue();

        // Validation guards
        assertThatThrownBy(() -> OpenRouterJevRouterPlugin.builder().models(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterJevRouterPlugin.builder().allowedModels(Arrays.asList("ok", null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterJevRouterPlugin.builder().excludedModels(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterJevRouterPlugin.builder().costTier(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterJevRouterPlugin.builder().option("id", "other"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterJevRouterPlugin.builder().option(null, "v"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void alignmentArmorAndJevRouterPluginsEmitAcrossChatResponsesAndMessagesAndCopyThroughToolLoop() {
        OpenRouterAlignmentPlugin alignment = OpenRouterAlignmentPlugin.builder()
                .rule("Be concise.", "content")
                .build();
        OpenRouterArmorPlugin armor = OpenRouterArmorPlugin.builder()
                .mode("enforce")
                .build();
        OpenRouterJevRouterPlugin jev = OpenRouterJevRouterPlugin.builder()
                .costTier("low")
                .build();

        // Chat Completions + tool-call loop copy (sync and streaming)
        OpenRouterChatCompletionRequest chatReq = baseBuilder()
                .plugins(alignment, armor, jev)
                .build();
        JSONArray chatPlugins = new JSONObject(chatReq.getBody()).getJSONArray("plugins");
        assertThat(chatPlugins.length()).isEqualTo(3);
        assertThat(chatPlugins.getJSONObject(0).getString("id")).isEqualTo("alignment");
        assertThat(chatPlugins.getJSONObject(1).getString("id")).isEqualTo("armor");
        assertThat(chatPlugins.getJSONObject(2).getString("id")).isEqualTo("jev-router");

        OpenRouterChatCompletionCallHandler handler = new OpenRouterChatCompletionCallHandler(new OpenRouterClient());
        OpenRouterChatCompletionRequest nextSync = handler.buildNextRequest(chatReq, chatReq.messages());
        JSONArray nextSyncPlugins = new JSONObject(nextSync.getBody()).getJSONArray("plugins");
        assertThat(nextSyncPlugins.length()).isEqualTo(3);
        assertThat(nextSyncPlugins.getJSONObject(0).getJSONArray("rules").getJSONObject(0).getString("rule"))
                .isEqualTo("Be concise.");
        assertThat(nextSyncPlugins.getJSONObject(1).getString("mode")).isEqualTo("enforce");
        assertThat(nextSyncPlugins.getJSONObject(2).getString("cost_tier")).isEqualTo("low");

        StreamingToolCallAccumulator acc = new StreamingToolCallAccumulator(new de.entwicklertraining.api.base.streaming.StreamingResponseHandler<>() {
            @Override
            public void onData(String data) {
            }

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable t) {
            }
        });
        OpenRouterChatCompletionRequest nextStream = handler.buildStreamingRequest(chatReq, chatReq.messages(), acc);
        JSONArray nextStreamPlugins = new JSONObject(nextStream.getBody()).getJSONArray("plugins");
        assertThat(nextStreamPlugins.length()).isEqualTo(3);
        assertThat(nextStreamPlugins.getJSONObject(0).getJSONArray("rules").getJSONObject(0).getString("rule"))
                .isEqualTo("Be concise.");
        assertThat(nextStreamPlugins.getJSONObject(1).getString("mode")).isEqualTo("enforce");
        assertThat(nextStreamPlugins.getJSONObject(2).getString("cost_tier")).isEqualTo("low");

        // Responses API
        OpenRouterResponsesRequest respReq = new OpenRouterClient().responses()
                .model("typesafe/jev-router")
                .input("Hi")
                .addPlugin(alignment)
                .addPlugin(armor)
                .addPlugin(jev)
                .build();
        JSONArray respPlugins = new JSONObject(respReq.getBody()).getJSONArray("plugins");
        assertThat(respPlugins.length()).isEqualTo(3);
        assertThat(respPlugins.getJSONObject(0).getString("id")).isEqualTo("alignment");
        assertThat(respPlugins.getJSONObject(1).getString("id")).isEqualTo("armor");
        assertThat(respPlugins.getJSONObject(2).getString("id")).isEqualTo("jev-router");

        // Anthropic Messages API
        OpenRouterMessagesRequest msgReq = new OpenRouterClient().messages()
                .model("anthropic/claude-sonnet-4-5")
                .maxTokens(256)
                .addMessage("user", "Hi")
                .addPlugin(alignment)
                .addPlugin(armor)
                .addPlugin(jev)
                .build();
        JSONArray msgPlugins = new JSONObject(msgReq.getBody()).getJSONArray("plugins");
        assertThat(msgPlugins.length()).isEqualTo(3);
        assertThat(msgPlugins.getJSONObject(0).getString("id")).isEqualTo("alignment");
        assertThat(msgPlugins.getJSONObject(1).getString("id")).isEqualTo("armor");
        assertThat(msgPlugins.getJSONObject(2).getString("id")).isEqualTo("jev-router");
    }
}
