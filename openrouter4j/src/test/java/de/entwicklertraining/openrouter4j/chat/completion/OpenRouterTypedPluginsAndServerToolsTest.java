package de.entwicklertraining.openrouter4j.chat.completion;

import de.entwicklertraining.openrouter4j.OpenRouterAdvisorServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterApplyPatchServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterAutoBetaRouterPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterAutoRouterPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterBashServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterContextCompressionPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterFusionPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterFusionServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterFilesServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterImageGenerationServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterModerationPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterParetoRouterPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterResponseHealingPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterSearchModelsServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterShellServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterSubagentServerTool;
import de.entwicklertraining.openrouter4j.OpenRouterWebFetchPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterFileParserPlugin;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

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
}
