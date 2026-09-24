package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

/**
 * A built-in OpenRouter <em>server tool</em> sent inside the {@code tools}
 * request array. Server tools are executed by OpenRouter itself (their results
 * arrive as {@code server_tool_calls}); the model uses them like regular tools
 * without the client registering a callback.
 * <p>
 * A server tool is emitted as {@code {"type": "<server-tool-type>"}} plus an
 * optional {@code parameters} object, e.g. {@code {"type":"openrouter:web_search"}}.
 * Function tools ({@link OpenRouterToolDefinition}) and server tools can be
 * mixed freely in one {@code tools} array.
 * <p>
 * Implementations: {@link OpenRouterWebSearchServerTool} ({@code openrouter:web_search}),
 * {@link OpenRouterWebFetchServerTool} ({@code openrouter:web_fetch}),
 * {@link OpenRouterDatetimeServerTool} ({@code openrouter:datetime}),
 * {@link OpenRouterToolSearchServerTool} ({@code openrouter:tool_search}),
 * {@link OpenRouterAdvisorServerTool} ({@code openrouter:advisor}),
 * {@link OpenRouterBashServerTool} ({@code openrouter:bash}),
 * {@link OpenRouterShellServerTool} ({@code openrouter:shell}),
 * {@link OpenRouterApplyPatchServerTool} ({@code openrouter:apply_patch}),
 * {@link OpenRouterFilesServerTool} ({@code openrouter:files}),
 * {@link OpenRouterFusionServerTool} ({@code openrouter:fusion}),
 * {@link OpenRouterImageGenerationServerTool} ({@code openrouter:image_generation}),
 * {@link OpenRouterSearchModelsServerTool} ({@code openrouter:experimental__search_models}),
 * {@link OpenRouterSubagentServerTool} ({@code openrouter:subagent}),
 * {@link OpenRouterWebSearchShorthandTool} ({@code web_search_preview} and its
 * variants - the OpenAI Responses-syntax shorthand the API converts to
 * {@code openrouter:web_search}; the typed {@code openrouter:*} form is the
 * canonical one), {@link OpenRouterCodeInterpreterServerTool}
 * ({@code code_interpreter}), {@link OpenRouterComputerUseServerTool}
 * ({@code computer_use_preview}), {@link OpenRouterFileSearchServerTool}
 * ({@code file_search}) and {@link OpenRouterMcpServerTool} ({@code mcp}) -
 * the last four are OpenAI-native tool types with flat top-level fields (no
 * {@code parameters} wrapper) that the published schema declares on the
 * Responses {@code tools} array - and {@link OpenRouterGenericServerTool} as
 * the verbatim escape hatch for every other server-tool type (or for types
 * OpenRouter adds later). {@link OpenRouterCustomTool} (the Responses-only
 * {@code custom} tool) deliberately does <em>not</em> implement this interface,
 * because the chat-completions {@code tools} union does not accept it.
 * <p>
 * Trap: plugins ({@link OpenRouterPlugin}, the {@code plugins} array) and server
 * tools (the {@code tools} array) are two different mechanisms that both exist
 * in the current API schema; the web search capability is available through both.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools">OpenRouter server tools</a>
 */
public interface OpenRouterServerTool {

    /**
     * The server-tool discriminator sent as the {@code type} field,
     * e.g. {@code "openrouter:web_search"}.
     */
    String type();

    /**
     * The JSON object emitted into the {@code tools} request array.
     * The returned object must contain at least the {@code type} field.
     */
    JSONObject toJson();

    /**
     * Creates a generic server tool that emits {@code {"type": type}} and nothing
     * else. Use this for server-tool types without a typed implementation; chain
     * {@link OpenRouterGenericServerTool#withOption(String, Object)} to add extra
     * fields (including a {@code parameters} object) verbatim.
     *
     * @param type the server-tool discriminator (e.g. {@code "openrouter:datetime"})
     * @return a generic server tool emitting only the type field
     */
    static OpenRouterGenericServerTool of(String type) {
        return new OpenRouterGenericServerTool(type);
    }

    /**
     * Creates a builder for the OpenAI Responses-syntax <em>web search
     * shorthand</em> tool ({@code web_search_preview} and its variants), which
     * the API converts automatically to {@code openrouter:web_search}.
     * Prefer {@link OpenRouterWebSearchServerTool} for new code - it is the
     * canonical, strictly more expressive form.
     *
     * @return a new {@link OpenRouterWebSearchShorthandTool.Builder}
     */
    static OpenRouterWebSearchShorthandTool.Builder webSearchShorthand() {
        return OpenRouterWebSearchShorthandTool.builder();
    }
}
