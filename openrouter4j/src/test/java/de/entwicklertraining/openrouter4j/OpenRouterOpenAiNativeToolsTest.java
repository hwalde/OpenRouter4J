package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest;
import de.entwicklertraining.openrouter4j.responses.OpenRouterResponsesRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the OpenAI-native tool types and the web search shorthand at the
 * request-JSON level: exact emission per tool, required-field rejection,
 * presence and absence in the request body, and the verbatim escape hatch.
 */
class OpenRouterOpenAiNativeToolsTest {

    private OpenRouterChatCompletionRequest.Builder chatBuilder() {
        return new OpenRouterClient().chat().completion()
                .model("test/model")
                .addMessage("user", "Hello");
    }

    private OpenRouterResponsesRequest.Builder responsesBuilder() {
        return new OpenRouterClient().responses()
                .model("openai/gpt-4o")
                .input("hi");
    }

    // ------------------------------------------------------------------
    // Web search shorthand

    @Test
    void webSearchShorthandEmitsTypeOnlyWhenUnset() {
        JSONObject tool = OpenRouterWebSearchShorthandTool.builder().build().toJson();
        assertThat(tool.keySet()).containsExactlyInAnyOrder("type");
        assertThat(tool.getString("type")).isEqualTo("web_search_preview");
    }

    @Test
    void webSearchShorthandEmitsFlatFieldsNotParametersWrapper() {
        JSONObject tool = OpenRouterWebSearchShorthandTool.builder()
                .type("web_search_preview_2025_03_11")
                .engine("exa")
                .maxResults(5)
                .maxTotalResults(50)
                .maxUses(3)
                .maxCharacters(2000)
                .mode("deep")
                .searchContextSize("medium")
                .allowedDomains(List.of("example.com"))
                .userLocation(OpenRouterWebSearchPlugin.UserLocation.builder()
                        .city("Cologne")
                        .country("DE")
                        .build())
                .xSearch(OpenRouterWebSearchServerTool.XSearchOptions.builder()
                        .allowedXHandles(List.of("openai"))
                        .enableImageUnderstanding(false)
                        .build())
                .build()
                .toJson();

        assertThat(tool.getString("type")).isEqualTo("web_search_preview_2025_03_11");
        assertThat(tool.getString("engine")).isEqualTo("exa");
        assertThat(tool.getInt("max_results")).isEqualTo(5);
        assertThat(tool.getInt("max_total_results")).isEqualTo(50);
        assertThat(tool.getInt("max_uses")).isEqualTo(3);
        assertThat(tool.getInt("max_characters")).isEqualTo(2000);
        assertThat(tool.getString("mode")).isEqualTo("deep");
        assertThat(tool.getString("search_context_size")).isEqualTo("medium");
        assertThat(tool.getJSONArray("allowed_domains").toList()).containsExactly("example.com");
        assertThat(tool.has("excluded_domains")).isFalse();
        assertThat(tool.has("parameters")).isFalse();
        JSONObject userLocation = tool.getJSONObject("user_location");
        assertThat(userLocation.getString("city")).isEqualTo("Cologne");
        assertThat(userLocation.getString("country")).isEqualTo("DE");
        JSONObject xSearch = tool.getJSONObject("x_search");
        assertThat(xSearch.getJSONArray("allowed_x_handles").toList()).containsExactly("openai");
        assertThat(xSearch.getBoolean("enable_image_understanding")).isFalse();
    }

    @Test
    void webSearchShorthandExcludedDomainsAreEmitted() {
        JSONObject tool = OpenRouterWebSearchShorthandTool.builder()
                .excludedDomains(List.of("spam.example"))
                .build()
                .toJson();
        assertThat(tool.getJSONArray("excluded_domains").toList()).containsExactly("spam.example");
        assertThat(tool.has("allowed_domains")).isFalse();
        assertThat(tool.has("parameters")).isFalse();
    }

    @Test
    void webSearchShorthandEmitsBothDomainListsVerbatimWhenBothSet() {
        JSONObject tool = OpenRouterWebSearchShorthandTool.builder()
                .allowedDomains(List.of("example.com"))
                .excludedDomains(List.of("spam.example"))
                .build()
                .toJson();
        assertThat(tool.getJSONArray("allowed_domains").toList()).containsExactly("example.com");
        assertThat(tool.getJSONArray("excluded_domains").toList()).containsExactly("spam.example");
    }

    @Test
    void webSearchShorthandEmptyDomainListsOmitTheFields() {
        JSONObject tool = OpenRouterWebSearchShorthandTool.builder()
                .allowedDomains(List.of())
                .excludedDomains(List.of())
                .build()
                .toJson();
        assertThat(tool.has("allowed_domains")).isFalse();
        assertThat(tool.has("excluded_domains")).isFalse();
    }

    @Test
    void webSearchShorthandIsEmittedVerbatimNotConvertedByTheLibrary() {
        OpenRouterChatCompletionRequest request = chatBuilder()
                .addServerTool(OpenRouterServerTool.webSearchShorthand()
                        .searchContextSize("low")
                        .build())
                .build();
        JSONObject body = new JSONObject(request.getBody());
        JSONObject tool = body.getJSONArray("tools").getJSONObject(0);
        assertThat(tool.getString("type")).isEqualTo("web_search_preview");
        assertThat(tool.getString("search_context_size")).isEqualTo("low");
        assertThat(tool.has("parameters")).isFalse();
    }

    @Test
    void webSearchShorthandOptionHatchPassesThroughAndRejectsType() {
        JSONObject tool = OpenRouterWebSearchShorthandTool.builder()
                .option("parameters", new JSONObject().put("engine", "exa"))
                .option("future_key", "v")
                .build()
                .toJson();
        assertThat(tool.getJSONObject("parameters").getString("engine")).isEqualTo("exa");
        assertThat(tool.getString("future_key")).isEqualTo("v");

        assertThatThrownBy(() -> OpenRouterWebSearchShorthandTool.builder().option("type", "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void webSearchShorthandCustomTypeIsAcceptedVerbatim() {
        JSONObject tool = OpenRouterWebSearchShorthandTool.builder("web_search_2025_08_26").build().toJson();
        assertThat(tool.getString("type")).isEqualTo("web_search_2025_08_26");
    }

    // ------------------------------------------------------------------
    // Custom tool (Responses-only)

    @Test
    void customToolTextFormEmitsNameAndTypeOnlyWhenUnset() {
        JSONObject tool = OpenRouterCustomTool.text("my_tool").toJson();
        assertThat(tool.keySet()).containsExactlyInAnyOrder("type", "name");
        assertThat(tool.getString("type")).isEqualTo("custom");
        assertThat(tool.getString("name")).isEqualTo("my_tool");
        assertThat(tool.has("format")).isFalse();
        assertThat(tool.has("async")).isFalse();
    }

    @Test
    void customToolTextFormatFormIsEmitted() {
        JSONObject tool = OpenRouterCustomTool.builder("my_tool")
                .description("Does a thing")
                .formatText()
                .async(true)
                .build()
                .toJson();
        assertThat(tool.getString("description")).isEqualTo("Does a thing");
        assertThat(tool.getJSONObject("format").toMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of("type", "text"));
        assertThat(tool.getBoolean("async")).isTrue();
    }

    @Test
    void customToolGrammarFormatFormIsEmitted() {
        JSONObject tool = OpenRouterCustomTool.grammar("my_tool", "expr: \"a\"+", "lark").toJson();
        JSONObject format = tool.getJSONObject("format");
        assertThat(format.getString("type")).isEqualTo("grammar");
        assertThat(format.getString("definition")).isEqualTo("expr: \"a\"+");
        assertThat(format.getString("syntax")).isEqualTo("lark");
        assertThat(tool.has("description")).isFalse();
        assertThat(tool.has("async")).isFalse();
    }

    @Test
    void customToolAsyncFalseIsEmitted() {
        JSONObject tool = OpenRouterCustomTool.builder("my_tool").async(false).build().toJson();
        assertThat(tool.getBoolean("async")).isFalse();
    }

    @Test
    void customToolReplacesFormatFormOnSecondCall() {
        JSONObject tool = OpenRouterCustomTool.builder("my_tool")
                .formatGrammar("g", "regex")
                .formatText()
                .build()
                .toJson();
        assertThat(tool.getJSONObject("format").toMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of("type", "text"));
    }

    @Test
    void customToolRejectsBlankNameAndOptionHatchOnTypedKeys() {
        assertThatThrownBy(() -> OpenRouterCustomTool.builder("  "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterCustomTool.builder("n").option("type", "x"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterCustomTool.builder("n").option("name", "x"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterCustomTool.grammar("n", "  ", "lark"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterCustomTool.grammar("n", "g", "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void customToolOptionHatchPassesThrough() {
        JSONObject tool = OpenRouterCustomTool.builder("my_tool")
                .option("future_key", 42)
                .build()
                .toJson();
        assertThat(tool.getInt("future_key")).isEqualTo(42);
    }

    @Test
    void customToolLandsInTheResponsesToolsArray() {
        OpenRouterResponsesRequest request = responsesBuilder()
                .addTool(OpenRouterCustomTool.text("my_tool").toJson())
                .build();
        JSONObject body = new JSONObject(request.getBody());
        JSONObject tool = body.getJSONArray("tools").getJSONObject(0);
        assertThat(tool.getString("type")).isEqualTo("custom");
        assertThat(tool.getString("name")).isEqualTo("my_tool");
    }

    @Test
    void customToolIsAbsentWhenNoToolIsAdded() {
        JSONObject body = new JSONObject(responsesBuilder().build().getBody());
        assertThat(body.has("tools")).isFalse();
    }

    // ------------------------------------------------------------------
    // MCP server tool

    @Test
    void mcpToolEmitsTypeAndServerLabelOnlyWhenUnset() {
        JSONObject tool = OpenRouterMcpServerTool.builder("my-server").build().toJson();
        assertThat(tool.keySet()).containsExactlyInAnyOrder("type", "server_label");
        assertThat(tool.getString("type")).isEqualTo("mcp");
        assertThat(tool.getString("server_label")).isEqualTo("my-server");
    }

    @Test
    void mcpToolEmitsOnlyConfiguredFields() {
        JSONObject tool = OpenRouterMcpServerTool.builder("my-server")
                .serverUrl("https://example.com/mcp")
                .connectorId("connector_dropbox")
                .authorization("Bearer tok")
                .headers(Map.of("X-Custom", "v"))
                .serverDescription("My MCP server")
                .build()
                .toJson();
        assertThat(tool.getString("server_url")).isEqualTo("https://example.com/mcp");
        assertThat(tool.getString("connector_id")).isEqualTo("connector_dropbox");
        assertThat(tool.getString("authorization")).isEqualTo("Bearer tok");
        assertThat(tool.getJSONObject("headers").toMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of("X-Custom", "v"));
        assertThat(tool.getString("server_description")).isEqualTo("My MCP server");
        assertThat(tool.has("allowed_tools")).isFalse();
        assertThat(tool.has("require_approval")).isFalse();
    }

    @Test
    void mcpToolAllowedToolsArrayFormIsEmitted() {
        JSONObject tool = OpenRouterMcpServerTool.builder("my-server")
                .allowedTools("tool_a", "tool_b")
                .build()
                .toJson();
        assertThat(tool.getJSONArray("allowed_tools").toList()).containsExactly("tool_a", "tool_b");
    }

    @Test
    void mcpToolEmptyAllowedToolsAndHeadersOmitTheFields() {
        JSONObject emptyList = OpenRouterMcpServerTool.builder("my-server")
                .allowedTools(List.of())
                .build()
                .toJson();
        assertThat(emptyList.has("allowed_tools")).isFalse();

        JSONObject emptyVarargs = OpenRouterMcpServerTool.builder("my-server")
                .allowedTools()
                .build()
                .toJson();
        assertThat(emptyVarargs.has("allowed_tools")).isFalse();

        JSONObject emptyHeaders = OpenRouterMcpServerTool.builder("my-server")
                .headers(Map.of())
                .build()
                .toJson();
        assertThat(emptyHeaders.has("headers")).isFalse();
    }

    @Test
    void mcpToolAllowedToolsObjectFormIsEmitted() {
        JSONObject tool = OpenRouterMcpServerTool.builder("my-server")
                .allowedToolsObject(true, List.of("tool_a"))
                .build()
                .toJson();
        JSONObject allowed = tool.getJSONObject("allowed_tools");
        assertThat(allowed.getBoolean("read_only")).isTrue();
        assertThat(allowed.getJSONArray("tool_names").toList()).containsExactly("tool_a");
    }

    @Test
    void mcpToolAllowedToolsObjectPartialNullFormsOmitTheMissingPart() {
        JSONObject readOnlyOnly = OpenRouterMcpServerTool.builder("my-server")
                .allowedToolsObject(true, null)
                .build()
                .toJson();
        assertThat(readOnlyOnly.getJSONObject("allowed_tools").keySet()).containsExactlyInAnyOrder("read_only");

        JSONObject namesOnly = OpenRouterMcpServerTool.builder("my-server")
                .allowedToolsObject(null, List.of("t"))
                .build()
                .toJson();
        assertThat(namesOnly.getJSONObject("allowed_tools").keySet()).containsExactlyInAnyOrder("tool_names");

        JSONObject bothEmpty = OpenRouterMcpServerTool.builder("my-server")
                .allowedToolsObject(null, List.of())
                .build()
                .toJson();
        assertThat(bothEmpty.has("allowed_tools")).isFalse();
    }

    @Test
    void mcpToolAllowedToolsFormsReplaceEachOther() {
        JSONObject tool = OpenRouterMcpServerTool.builder("my-server")
                .allowedTools("tool_a")
                .allowedToolsObject(false, List.of("tool_b"))
                .build()
                .toJson();
        assertThat(tool.getJSONObject("allowed_tools").getBoolean("read_only")).isFalse();

        JSONObject backToArray = OpenRouterMcpServerTool.builder("my-server")
                .allowedToolsObject(true, List.of("tool_b"))
                .allowedTools("tool_a")
                .build()
                .toJson();
        assertThat(backToArray.getJSONArray("allowed_tools").toList()).containsExactly("tool_a");
    }

    @Test
    void mcpToolRequireApprovalFormsAreEmitted() {
        JSONObject stringForm = OpenRouterMcpServerTool.builder("my-server")
                .requireApproval("always")
                .build()
                .toJson();
        assertThat(stringForm.getString("require_approval")).isEqualTo("always");

        JSONObject objectForm = OpenRouterMcpServerTool.builder("my-server")
                .requireApproval(true, List.of("tool_a"))
                .build()
                .toJson();
        JSONObject approval = objectForm.getJSONObject("require_approval");
        assertThat(approval.getJSONObject("always").getJSONArray("tool_names").toList())
                .containsExactly("tool_a");

        JSONObject verbatimInput = new JSONObject().put("never", new JSONObject());
        JSONObject verbatim = OpenRouterMcpServerTool.builder("my-server")
                .requireApproval(verbatimInput)
                .build()
                .toJson();
        assertThat(verbatim.getJSONObject("require_approval").toMap()).isEqualTo(verbatimInput.toMap());
    }

    @Test
    void mcpToolRequireApprovalNeverVariantAndEmptyToolNamesAreEmitted() {
        JSONObject neverForm = OpenRouterMcpServerTool.builder("my-server")
                .requireApproval(false, List.of("tool_a"))
                .build()
                .toJson();
        assertThat(neverForm.getJSONObject("require_approval").getJSONObject("never")
                .getJSONArray("tool_names").toList()).containsExactly("tool_a");

        JSONObject allTools = OpenRouterMcpServerTool.builder("my-server")
                .requireApproval(true, List.of())
                .build()
                .toJson();
        JSONObject always = allTools.getJSONObject("require_approval").getJSONObject("always");
        assertThat(always.has("tool_names")).isFalse();
    }

    @Test
    void mcpToolRejectsBlankServerLabelAndOptionHatchOnTypedKeys() {
        assertThatThrownBy(() -> OpenRouterMcpServerTool.builder("  "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterMcpServerTool.builder("s").option("type", "x"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterMcpServerTool.builder("s").option("server_label", "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void mcpToolOptionHatchPassesThrough() {
        JSONObject tool = OpenRouterMcpServerTool.builder("my-server")
                .option("future_key", "v")
                .build()
                .toJson();
        assertThat(tool.getString("future_key")).isEqualTo("v");
    }

    @Test
    void mcpToolLandsInTheResponsesToolsArray() {
        OpenRouterResponsesRequest request = responsesBuilder()
                .addTool(OpenRouterMcpServerTool.builder("my-server")
                        .serverUrl("https://example.com/mcp")
                        .build()
                        .toJson())
                .build();
        JSONObject tool = new JSONObject(request.getBody()).getJSONArray("tools").getJSONObject(0);
        assertThat(tool.getString("type")).isEqualTo("mcp");
        assertThat(tool.getString("server_label")).isEqualTo("my-server");
        assertThat(tool.getString("server_url")).isEqualTo("https://example.com/mcp");
    }

    // ------------------------------------------------------------------
    // Code interpreter

    @Test
    void codeInterpreterStringContainerFormIsEmitted() {
        JSONObject tool = OpenRouterCodeInterpreterServerTool.builder()
                .container("auto")
                .build()
                .toJson();
        assertThat(tool.keySet()).containsExactlyInAnyOrder("type", "container");
        assertThat(tool.getString("type")).isEqualTo("code_interpreter");
        assertThat(tool.getString("container")).isEqualTo("auto");
    }

    @Test
    void codeInterpreterAutoContainerObjectFormIsEmitted() {
        JSONObject tool = OpenRouterCodeInterpreterServerTool.builder()
                .containerAuto(List.of("or_file_1"), "4g")
                .build()
                .toJson();
        JSONObject container = tool.getJSONObject("container");
        assertThat(container.getString("type")).isEqualTo("auto");
        assertThat(container.getJSONArray("file_ids").toList()).containsExactly("or_file_1");
        assertThat(container.getString("memory_limit")).isEqualTo("4g");
    }

    @Test
    void codeInterpreterAutoContainerWithoutOptionsEmitsTypeOnly() {
        JSONObject tool = OpenRouterCodeInterpreterServerTool.builder().containerAuto().build().toJson();
        JSONObject container = tool.getJSONObject("container");
        assertThat(container.keySet()).containsExactlyInAnyOrder("type");
        assertThat(container.getString("type")).isEqualTo("auto");
        assertThat(container.has("memory_limit")).isFalse();
    }

    @Test
    void codeInterpreterMemoryLimitEnumIsPinnedAndNullEmitsJsonNull() {
        for (String limit : List.of("1g", "4g", "16g", "64g")) {
            JSONObject tool = OpenRouterCodeInterpreterServerTool.builder()
                    .containerAuto(null, limit)
                    .build()
                    .toJson();
            assertThat(tool.getJSONObject("container").getString("memory_limit")).isEqualTo(limit);
        }

        JSONObject explicitNull = OpenRouterCodeInterpreterServerTool.builder()
                .containerAuto(null, null)
                .build()
                .toJson();
        JSONObject container = explicitNull.getJSONObject("container");
        assertThat(container.has("memory_limit")).isTrue();
        assertThat(container.isNull("memory_limit")).isTrue();

        JSONObject omitted = OpenRouterCodeInterpreterServerTool.builder()
                .containerAuto(List.of("or_file_1"))
                .build()
                .toJson();
        assertThat(omitted.getJSONObject("container").has("memory_limit")).isFalse();
        assertThat(omitted.getJSONObject("container").getJSONArray("file_ids").toList())
                .containsExactly("or_file_1");
    }

    @Test
    void codeInterpreterVerbatimContainerObjectIsEmitted() {
        JSONObject container = new JSONObject().put("type", "auto").put("memory_limit", JSONObject.NULL);
        JSONObject tool = OpenRouterCodeInterpreterServerTool.builder()
                .container(container)
                .build()
                .toJson();
        assertThat(tool.getJSONObject("container").getString("type")).isEqualTo("auto");
        assertThat(tool.getJSONObject("container").isNull("memory_limit")).isTrue();
    }

    @Test
    void codeInterpreterRejectsMissingOrInvalidContainerAndOptionHatchOnTypedKeys() {
        assertThatThrownBy(() -> OpenRouterCodeInterpreterServerTool.builder().build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> OpenRouterCodeInterpreterServerTool.builder().container("  "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterCodeInterpreterServerTool.builder()
                .containerAuto(null, "8g"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterCodeInterpreterServerTool.builder()
                .containerAuto(null, "63g"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterCodeInterpreterServerTool.builder()
                .container("auto")
                .option("type", "x"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterCodeInterpreterServerTool.builder()
                .container("auto")
                .option("container", "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void codeInterpreterOptionHatchPassesThrough() {
        JSONObject tool = OpenRouterCodeInterpreterServerTool.builder()
                .container("auto")
                .option("future_key", 1)
                .build()
                .toJson();
        assertThat(tool.getInt("future_key")).isEqualTo(1);
    }

    @Test
    void codeInterpreterLandsInTheResponsesToolsArray() {
        OpenRouterResponsesRequest request = responsesBuilder()
                .addTool(OpenRouterCodeInterpreterServerTool.builder().containerAuto().build().toJson())
                .build();
        JSONObject tool = new JSONObject(request.getBody()).getJSONArray("tools").getJSONObject(0);
        assertThat(tool.getString("type")).isEqualTo("code_interpreter");
        assertThat(tool.getJSONObject("container").getString("type")).isEqualTo("auto");
    }

    // ------------------------------------------------------------------
    // Computer use

    @Test
    void computerUseEmitsAllThreeRequiredFields() {
        JSONObject tool = OpenRouterComputerUseServerTool.builder(1024, 768, "linux").build().toJson();
        assertThat(tool.keySet()).containsExactlyInAnyOrder("type", "display_width", "display_height", "environment");
        assertThat(tool.getString("type")).isEqualTo("computer_use_preview");
        assertThat(tool.getInt("display_width")).isEqualTo(1024);
        assertThat(tool.getInt("display_height")).isEqualTo(768);
        assertThat(tool.getString("environment")).isEqualTo("linux");
    }

    @Test
    void computerUseRejectsMissingRequiredFieldsAndNonPositiveSizes() {
        assertThatThrownBy(() -> OpenRouterComputerUseServerTool.builder().build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> OpenRouterComputerUseServerTool.builder()
                .displayWidth(1024)
                .displayHeight(768)
                .build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> OpenRouterComputerUseServerTool.builder().displayWidth(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterComputerUseServerTool.builder().displayHeight(-1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterComputerUseServerTool.builder().environment("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void computerUseRejectsOptionHatchOnTypedKeysAndPassesExtras() {
        assertThatThrownBy(() -> OpenRouterComputerUseServerTool.builder()
                .option("display_width", 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterComputerUseServerTool.builder()
                .option("environment", "linux"))
                .isInstanceOf(IllegalArgumentException.class);
        JSONObject tool = OpenRouterComputerUseServerTool.builder(1, 1, "browser")
                .option("future_key", true)
                .build()
                .toJson();
        assertThat(tool.getBoolean("future_key")).isTrue();
    }

    @Test
    void computerUseLandsInTheResponsesToolsArray() {
        OpenRouterResponsesRequest request = responsesBuilder()
                .addTool(OpenRouterComputerUseServerTool.builder(800, 600, "browser").build().toJson())
                .build();
        JSONObject tool = new JSONObject(request.getBody()).getJSONArray("tools").getJSONObject(0);
        assertThat(tool.getString("type")).isEqualTo("computer_use_preview");
        assertThat(tool.getString("environment")).isEqualTo("browser");
    }

    // ------------------------------------------------------------------
    // File search

    @Test
    void fileSearchEmitsVectorStoreIdsOnlyWhenUnset() {
        JSONObject tool = OpenRouterFileSearchServerTool.builder("vs_abc").build().toJson();
        assertThat(tool.keySet()).containsExactlyInAnyOrder("type", "vector_store_ids");
        assertThat(tool.getString("type")).isEqualTo("file_search");
        assertThat(tool.getJSONArray("vector_store_ids").toList()).containsExactly("vs_abc");
    }

    @Test
    void fileSearchEmitsComparisonFilterAndRankingOptions() {
        JSONObject tool = OpenRouterFileSearchServerTool.builder(List.of("vs_a", "vs_b"))
                .maxNumResults(5)
                .filter("author", "eq", "Alice")
                .ranker("default-2024-11-15")
                .scoreThreshold(0.5)
                .build()
                .toJson();
        assertThat(tool.getJSONArray("vector_store_ids").toList()).containsExactly("vs_a", "vs_b");
        assertThat(tool.getInt("max_num_results")).isEqualTo(5);
        JSONObject filter = tool.getJSONObject("filters");
        assertThat(filter.toMap()).containsExactlyInAnyOrderEntriesOf(
                Map.of("key", "author", "type", "eq", "value", "Alice"));
        JSONObject ranking = tool.getJSONObject("ranking_options");
        assertThat(ranking.getString("ranker")).isEqualTo("default-2024-11-15");
        assertThat(ranking.getDouble("score_threshold")).isEqualTo(0.5);
    }

    @Test
    void fileSearchFilterAcceptsArrayValuesAndCompoundFilterEscapeHatch() {
        JSONObject arrayFilter = OpenRouterFileSearchServerTool.builder("vs")
                .filter("tag", "in", List.of("a", "b"))
                .build()
                .toJson();
        assertThat(arrayFilter.getJSONObject("filters").getJSONArray("value").toList())
                .containsExactly("a", "b");

        JSONObject compound = new JSONObject()
                .put("type", "and")
                .put("filters", new JSONArray()
                        .put(new JSONObject().put("key", "k").put("type", "eq").put("value", "v")));
        JSONObject compoundTool = OpenRouterFileSearchServerTool.builder("vs")
                .filters(compound)
                .build()
                .toJson();
        assertThat(compoundTool.getJSONObject("filters").toMap()).isEqualTo(compound.toMap());
        assertThat(compoundTool.getJSONObject("filters").getJSONArray("filters").getJSONObject(0)
                .getString("key")).isEqualTo("k");
    }

    @Test
    void fileSearchRejectsEmptyOrBlankVectorStoreIdsAndOptionHatchOnTypedKeys() {
        assertThatThrownBy(() -> OpenRouterFileSearchServerTool.builder(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterFileSearchServerTool.builder("  "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterFileSearchServerTool.builder("vs").filter("  ", "eq", "v"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterFileSearchServerTool.builder("vs").filter("k", "  ", "v"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterFileSearchServerTool.builder("vs").ranker("  "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterFileSearchServerTool.builder("vs").option("type", "x"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterFileSearchServerTool.builder("vs").option("vector_store_ids", "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fileSearchOptionHatchPassesThroughAndLandsInTheResponsesToolsArray() {
        OpenRouterResponsesRequest request = responsesBuilder()
                .addTool(OpenRouterFileSearchServerTool.builder("vs_abc")
                        .option("future_key", "v")
                        .build()
                        .toJson())
                .build();
        JSONObject tool = new JSONObject(request.getBody()).getJSONArray("tools").getJSONObject(0);
        assertThat(tool.getString("type")).isEqualTo("file_search");
        assertThat(tool.getString("future_key")).isEqualTo("v");
    }

    @Test
    void allNewToolsShareTheToolsArrayWithFunctionTools() {
        OpenRouterResponsesRequest request = responsesBuilder()
                .addFunctionTool("get_weather", "desc", new JSONObject("{\"type\":\"object\"}"))
                .addTool(OpenRouterCustomTool.text("custom_one").toJson())
                .addTool(OpenRouterMcpServerTool.builder("srv").build().toJson())
                .addTool(OpenRouterCodeInterpreterServerTool.builder().containerAuto().build().toJson())
                .addTool(OpenRouterComputerUseServerTool.builder(1, 1, "linux").build().toJson())
                .addTool(OpenRouterFileSearchServerTool.builder("vs").build().toJson())
                .build();
        JSONArray tools = new JSONObject(request.getBody()).getJSONArray("tools");
        assertThat(tools.length()).isEqualTo(6);
        assertThat(tools.getJSONObject(0).getString("name")).isEqualTo("get_weather");
        assertThat(tools.getJSONObject(1).getString("type")).isEqualTo("custom");
        assertThat(tools.getJSONObject(2).getString("type")).isEqualTo("mcp");
        assertThat(tools.getJSONObject(3).getString("type")).isEqualTo("code_interpreter");
        assertThat(tools.getJSONObject(4).getString("type")).isEqualTo("computer_use_preview");
        assertThat(tools.getJSONObject(5).getString("type")).isEqualTo("file_search");
    }
}
