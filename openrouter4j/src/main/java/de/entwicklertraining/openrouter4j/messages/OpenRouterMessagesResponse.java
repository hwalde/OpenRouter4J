package de.entwicklertraining.openrouter4j.messages;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of POST /messages (the Anthropic Messages API with OpenRouter
 * extensions): the assistant message with its content blocks, the stop
 * reason and the token usage.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention: a field that is absent (or a malformed body) yields {@code null}
 * (or an empty list) instead of an exception. Use {@link #getJson()} to
 * inspect the raw response.
 */
public final class OpenRouterMessagesResponse extends OpenRouterResponse<OpenRouterMessagesRequest> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response, or
     *                {@code null} when the body is read from a source that
     *                is not one executed request (e.g. a batch result item)
     */
    public OpenRouterMessagesResponse(JSONObject json, OpenRouterMessagesRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code id} - the message id (e.g. {@code msg_01XFDUDYJgA...}).
     *
     * @return the value, or {@code null} when absent
     */
    public String id() {
        return optString("id");
    }

    /**
     * JSON path: {@code model} - the model that served the request.
     *
     * @return the value, or {@code null} when absent
     */
    public String model() {
        return optString("model");
    }

    /**
     * JSON path: {@code role} - always {@code assistant} on a well-formed
     * response.
     *
     * @return the value, or {@code null} when absent
     */
    public String role() {
        return optString("role");
    }

    /**
     * JSON path: {@code stop_reason} - why the response ended:
     * {@code end_turn}, {@code max_tokens}, {@code stop_sequence},
     * {@code tool_use}, ... (unknown values are passed through verbatim).
     *
     * @return the value, or {@code null} when absent
     */
    public String stopReason() {
        return optString("stop_reason");
    }

    /**
     * JSON path: {@code stop_sequence} - the stop sequence that ended the
     * response, when {@code stop_reason} is {@code stop_sequence}.
     *
     * @return the value, or {@code null} when absent
     */
    public String stopSequence() {
        return optString("stop_sequence");
    }

    /**
     * @return the raw {@code stop_details} object (e.g. refusal details), or
     *         {@code null} when absent
     */
    public JSONObject stopDetails() {
        try {
            return json.optJSONObject("stop_details");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return the raw {@code container} object, or {@code null} when absent
     */
    public JSONObject container() {
        try {
            return json.optJSONObject("container");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return the raw {@code content} array with every content block (text,
     *         tool_use, thinking, server_tool_use, ...), or {@code null}
     *         when absent
     */
    public JSONArray content() {
        try {
            return json.optJSONArray("content");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return every content block as a raw {@link JSONObject} list, empty
     *         when {@code content} is absent or not an array
     */
    public List<JSONObject> contentBlocks() {
        List<JSONObject> result = new ArrayList<>();
        JSONArray content = content();
        if (content != null) {
            for (int i = 0; i < content.length(); i++) {
                JSONObject block = content.optJSONObject(i);
                if (block != null) {
                    result.add(block);
                }
            }
        }
        return result;
    }

    /**
     * Convenience: the text of all {@code text} blocks concatenated in
     * order.
     *
     * @return the concatenated text, {@code null} when there is no text
     *         block
     */
    public String text() {
        List<OpenRouterTextBlock> blocks = textBlocks();
        if (blocks.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (OpenRouterTextBlock block : blocks) {
            if (block.text() != null) {
                sb.append(block.text());
            }
        }
        return sb.toString();
    }

    /**
     * @return the {@code text} content blocks, empty when absent
     */
    public List<OpenRouterTextBlock> textBlocks() {
        return mapBlocks("text", OpenRouterTextBlock::new);
    }

    /**
     * @return the {@code tool_use} content blocks, empty when absent
     */
    public List<OpenRouterToolUseBlock> toolUseBlocks() {
        return mapBlocks("tool_use", OpenRouterToolUseBlock::new);
    }

    /**
     * @return the {@code thinking} content blocks (visible reasoning with a
     *         signature), empty when absent
     */
    public List<OpenRouterThinkingBlock> thinkingBlocks() {
        return mapBlocks("thinking", OpenRouterThinkingBlock::new);
    }

    /**
     * @return the {@code redacted_thinking} content blocks (reasoning the
     *         provider withheld), empty when absent
     */
    public List<OpenRouterRedactedThinkingBlock> redactedThinkingBlocks() {
        return mapBlocks("redacted_thinking", OpenRouterRedactedThinkingBlock::new);
    }

    /**
     * @return the {@code server_tool_use} content blocks (OpenRouter
     *         server-orchestrated tool calls), empty when absent
     */
    public List<OpenRouterServerToolUseBlock> serverToolUseBlocks() {
        return mapBlocks("server_tool_use", OpenRouterServerToolUseBlock::new);
    }

    /**
     * @return the {@code compaction} content blocks - the compaction summary
     *         the server inserted after a {@code compact_20260112}
     *         context-management edit ({@code stop_reason} may then be
     *         {@code compaction}), empty when absent
     */
    public List<OpenRouterCompactionBlock> compactionBlocks() {
        return mapBlocks("compaction", OpenRouterCompactionBlock::new);
    }

    /**
     * @return the {@code container_upload} content blocks - the container
     *         file a bash/shell server tool wrote (download or promote it via
     *         {@code client.containers()}), empty when absent
     */
    public List<OpenRouterContainerUploadBlock> containerUploadBlocks() {
        return mapBlocks("container_upload", OpenRouterContainerUploadBlock::new);
    }

    /**
     * @return the {@code web_search_tool_result} content blocks - the result
     *         (or error) of a server-orchestrated web search, empty when
     *         absent
     */
    public List<OpenRouterWebSearchToolResultBlock> webSearchToolResultBlocks() {
        return mapBlocks("web_search_tool_result", OpenRouterWebSearchToolResultBlock::new);
    }

    /**
     * @return the {@code web_fetch_tool_result} content blocks - the result
     *         (or error) of a server-orchestrated web fetch, empty when
     *         absent
     */
    public List<OpenRouterWebFetchToolResultBlock> webFetchToolResultBlocks() {
        return mapBlocks("web_fetch_tool_result", OpenRouterWebFetchToolResultBlock::new);
    }

    /**
     * @return the {@code code_execution_tool_result} content blocks - the
     *         result (or error) of a code-execution server tool, empty when
     *         absent
     */
    public List<OpenRouterCodeExecutionToolResultBlock> codeExecutionToolResultBlocks() {
        return mapBlocks("code_execution_tool_result", OpenRouterCodeExecutionToolResultBlock::new);
    }

    /**
     * @return the {@code bash_code_execution_tool_result} content blocks -
     *         the result (or error) of a bash code-execution server tool,
     *         empty when absent
     */
    public List<OpenRouterBashCodeExecutionToolResultBlock> bashCodeExecutionToolResultBlocks() {
        return mapBlocks("bash_code_execution_tool_result", OpenRouterBashCodeExecutionToolResultBlock::new);
    }

    /**
     * @return the {@code text_editor_code_execution_tool_result} content
     *         blocks - the result (or error) of the text-editor code
     *         execution tool, empty when absent
     */
    public List<OpenRouterTextEditorCodeExecutionToolResultBlock> textEditorCodeExecutionToolResultBlocks() {
        return mapBlocks("text_editor_code_execution_tool_result", OpenRouterTextEditorCodeExecutionToolResultBlock::new);
    }

    /**
     * @return the {@code tool_search_tool_result} content blocks - the tool
     *         references a {@code openrouter:tool_search} lookup returned,
     *         empty when absent
     */
    public List<OpenRouterToolSearchToolResultBlock> toolSearchToolResultBlocks() {
        return mapBlocks("tool_search_tool_result", OpenRouterToolSearchToolResultBlock::new);
    }

    /**
     * @return the {@code advisor_tool_result} content blocks - the advisor
     *         model's advice, empty when absent
     */
    public List<OpenRouterAdvisorToolResultBlock> advisorToolResultBlocks() {
        return mapBlocks("advisor_tool_result", OpenRouterAdvisorToolResultBlock::new);
    }

    /**
     * @return the {@code openrouter_shell_tool_result} content blocks - the
     *         output of an {@code openrouter:shell} call run in the OpenRouter
     *         sandbox, empty when absent
     */
    public List<OpenRouterShellToolResultBlock> shellToolResultBlocks() {
        return mapBlocks("openrouter_shell_tool_result", OpenRouterShellToolResultBlock::new);
    }

    /**
     * @return the {@code openrouter_bash_tool_result} content blocks - the
     *         output of an {@code openrouter:bash} call run in the OpenRouter
     *         sandbox, empty when absent
     */
    public List<OpenRouterBashToolResultBlock> bashToolResultBlocks() {
        return mapBlocks("openrouter_bash_tool_result", OpenRouterBashToolResultBlock::new);
    }

    /**
     * JSON path: {@code context_management.applied_edits} - the server-side
     * context edits Anthropic actually applied to the prompt this turn (each
     * entry is a free-form object with a required {@code type}; e.g. a
     * {@code clear_tool_uses_20250919} edit reports what it cleared).
     *
     * <p>Trap: {@code context_management} is only present when the request
     * opted into context editing via the builder's {@code contextManagement(...)}
     * edits AND the serving provider actually applied edits - otherwise this
     * list is empty.
     *
     * <p>Streaming: the published OpenAPI schema documents
     * {@code context_management} only on the non-streaming payload - on a
     * streamed turn no SSE event carries it, so read the applied edits from
     * the non-streaming response.
     *
     * @return the applied edits, empty when {@code context_management} (or
     *         the {@code applied_edits} array) is absent
     */
    public List<OpenRouterAppliedContextEdit> appliedContextEdits() {
        List<OpenRouterAppliedContextEdit> result = new ArrayList<>();
        try {
            JSONObject contextManagement = json.optJSONObject("context_management");
            JSONArray edits = contextManagement == null
                    ? null
                    : contextManagement.optJSONArray("applied_edits");
            if (edits != null) {
                for (int i = 0; i < edits.length(); i++) {
                    JSONObject edit = edits.optJSONObject(i);
                    if (edit != null) {
                        result.add(new OpenRouterAppliedContextEdit(edit));
                    }
                }
            }
        } catch (Exception e) {
            // swallow-and-return-empty convention
        }
        return result;
    }

    /**
     * JSON path: {@code input_transformations} - the server-side
     * transformations the serving provider applied to the request input
     * (e.g. {@code {"path":"messages.1.content.0",
     * "reason":"prefix_binding_mismatch","type":"thinking_dropped"}}).
     * This is the diagnostic surface for "my thinking blocks disappeared".
     *
     * <p>Trap: the field appears only when the serving provider actually
     * transformed the input - commonly Anthropic dropping thinking blocks on
     * a prefix binding mismatch. A clean turn returns an empty list.
     *
     * <p>Streaming: per the published OpenAPI schema the field travels in the
     * {@code message_start} event's {@code message} object, so on a streamed
     * turn read it from the raw event JSON ({@code stream(handler)} forwards
     * every event verbatim).
     *
     * @return the transformations, empty when the field is absent or not an
     *         array
     */
    public List<OpenRouterInputTransformation> inputTransformations() {
        List<OpenRouterInputTransformation> result = new ArrayList<>();
        try {
            JSONArray transformations = json.optJSONArray("input_transformations");
            if (transformations != null) {
                for (int i = 0; i < transformations.length(); i++) {
                    JSONObject transformation = transformations.optJSONObject(i);
                    if (transformation != null) {
                        result.add(new OpenRouterInputTransformation(transformation));
                    }
                }
            }
        } catch (Exception e) {
            // swallow-and-return-empty convention
        }
        return result;
    }

    /**
     * JSON path: {@code safeguard_results} - the outcome of each Anthropic
     * server-side safeguard the request opted into via
     * {@code safeguards(...)}. Swallow-and-return-empty convention.
     * Anthropic-provider semantics.
     *
     * @return the safeguard results, empty when absent or on a malformed body
     */
    public List<OpenRouterSafeguardResult> safeguardResults() {
        List<OpenRouterSafeguardResult> result = new ArrayList<>();
        try {
            JSONArray results = json.optJSONArray("safeguard_results");
            if (results != null) {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject entry = results.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterSafeguardResult(entry));
                    }
                }
            }
        } catch (Exception e) {
            // swallow-and-return-empty convention
        }
        return result;
    }

    /**
     * JSON path: {@code provider} - the provider that served the request
     * (OpenRouter extension).
     *
     * @return the value, or {@code null} when absent
     */
    public String provider() {
        return optString("provider");
    }

    /**
     * @return the raw {@code openrouter_metadata} routing object, or
     *         {@code null} when absent
     */
    public JSONObject openrouterMetadata() {
        try {
            return json.optJSONObject("openrouter_metadata");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return the raw {@code usage} object, or {@code null} when absent
     */
    public JSONObject usage() {
        try {
            return json.optJSONObject("usage");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code usage.input_tokens} - input tokens counted (cache
     * reads are reported separately).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long inputTokens() {
        return optUsageLong("input_tokens");
    }

    /**
     * JSON path: {@code usage.output_tokens} - output tokens generated.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long outputTokens() {
        return optUsageLong("output_tokens");
    }

    /**
     * JSON path: {@code usage.cache_creation_input_tokens} - tokens written
     * to the prompt cache.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long cacheCreationInputTokens() {
        return optUsageLong("cache_creation_input_tokens");
    }

    /**
     * JSON path: {@code usage.cache_read_input_tokens} - tokens read from
     * the prompt cache.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long cacheReadInputTokens() {
        return optUsageLong("cache_read_input_tokens");
    }

    /**
     * JSON path: {@code usage.cost} - the cost in USD (OpenRouter
     * extension).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double cost() {
        try {
            JSONObject usage = usage();
            if (usage == null || !usage.has("cost") || usage.isNull("cost")) {
                return null;
            }
            Object value = usage.get("cost");
            return value instanceof Number number ? number.doubleValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code usage.service_tier} - the service tier used by the
     * upstream provider (OpenRouter extension).
     *
     * @return the value, or {@code null} when absent
     */
    public String serviceTier() {
        try {
            JSONObject usage = usage();
            return usage != null ? usage.optString("service_tier", null) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private <T> List<T> mapBlocks(String type, java.util.function.Function<JSONObject, T> mapper) {
        List<T> result = new ArrayList<>();
        for (JSONObject block : contentBlocks()) {
            if (type.equals(block.optString("type", null))) {
                result.add(mapper.apply(block));
            }
        }
        return result;
    }

    private String optString(String key) {
        try {
            return json.optString(key, null);
        } catch (Exception e) {
            return null;
        }
    }

    private Long optUsageLong(String key) {
        try {
            JSONObject usage = usage();
            if (usage == null || !usage.has(key) || usage.isNull(key)) {
                return null;
            }
            Object value = usage.get(key);
            return value instanceof Number number ? number.longValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * A {@code text} content block.
     */
    public static final class OpenRouterTextBlock {

        private final JSONObject json;

        OpenRouterTextBlock(JSONObject json) {
            this.json = json;
        }

        /** @return the text, or {@code null} when absent */
        public String text() {
            try {
                return json.optString("text", null);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the raw {@code citations} array, or {@code null} when
         *         absent
         */
        public JSONArray citations() {
            try {
                return json.optJSONArray("citations");
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * A {@code tool_use} content block - the model asked to call one of the
     * declared tools.
     */
    public static final class OpenRouterToolUseBlock {

        private final JSONObject json;

        OpenRouterToolUseBlock(JSONObject json) {
            this.json = json;
        }

        /** @return the tool call id (echoed back as {@code tool_use_id}), or {@code null} */
        public String id() {
            try {
                return json.optString("id", null);
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the tool name, or {@code null} when absent */
        public String name() {
            try {
                return json.optString("name", null);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the tool input as parsed from the model's JSON arguments,
         *         or {@code null} when absent
         */
        public JSONObject input() {
            try {
                return json.optJSONObject("input");
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * A {@code thinking} content block - visible reasoning with a signature
     * that must be passed back verbatim in multi-turn conversations.
     */
    public static final class OpenRouterThinkingBlock {

        private final JSONObject json;

        OpenRouterThinkingBlock(JSONObject json) {
            this.json = json;
        }

        /** @return the reasoning text, or {@code null} when absent */
        public String thinking() {
            try {
                return json.optString("thinking", null);
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the signature, or {@code null} when absent */
        public String signature() {
            try {
                return json.optString("signature", null);
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * A {@code redacted_thinking} content block - reasoning the provider
     * withheld; carries the opaque {@code data} to pass back verbatim.
     */
    public static final class OpenRouterRedactedThinkingBlock {

        private final JSONObject json;

        OpenRouterRedactedThinkingBlock(JSONObject json) {
            this.json = json;
        }

        /** @return the opaque redacted data, or {@code null} when absent */
        public String data() {
            try {
                return json.optString("data", null);
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * A {@code server_tool_use} content block - an OpenRouter
     * server-orchestrated tool call (e.g. web search).
     */
    public static final class OpenRouterServerToolUseBlock {

        private final JSONObject json;

        OpenRouterServerToolUseBlock(JSONObject json) {
            this.json = json;
        }

        /** @return the tool call id, or {@code null} when absent */
        public String id() {
            try {
                return json.optString("id", null);
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the server tool name, or {@code null} when absent */
        public String name() {
            try {
                return json.optString("name", null);
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the tool input, or {@code null} when absent */
        public JSONObject input() {
            try {
                return json.optJSONObject("input");
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * A {@code compaction} content block - the compaction summary the server
     * inserted after a {@code compact_20260112} context-management edit (the
     * request side is {@code OpenRouterCompactEdit}).
     */
    public static final class OpenRouterCompactionBlock {

        private final JSONObject json;

        OpenRouterCompactionBlock(JSONObject json) {
            this.json = json;
        }

        /** @return the compaction summary text, or {@code null} when absent */
        public String content() {
            try {
                return json.optString("content", null);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the encrypted compaction payload ({@code encrypted_content})
         *         some providers return alongside - or, when {@code content}
         *         is null, instead of - the plaintext {@link #content()}
         *         summary, or {@code null} when absent
         */
        public String encryptedContent() {
            try {
                return json.optString("encrypted_content", null);
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the verbatim block object */
        public JSONObject json() {
            return json;
        }
    }

    /**
     * A {@code container_upload} content block - the container file a
     * bash/shell server tool wrote. The file lives in the code-execution
     * container; download it via {@code client.containers().fileContent(...)}
     * or promote it into durable storage via
     * {@code client.containers().promoteFile(...)}.
     */
    public static final class OpenRouterContainerUploadBlock {

        private final JSONObject json;

        OpenRouterContainerUploadBlock(JSONObject json) {
            this.json = json;
        }

        /** @return the container file id, or {@code null} when absent */
        public String fileId() {
            try {
                return json.optString("file_id", null);
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the verbatim block object */
        public JSONObject json() {
            return json;
        }
    }

    /**
     * A {@code web_search_tool_result} content block - the result (or error)
     * of a server-orchestrated web search. The {@code content} field is a
     * union: either an array of search results or a single error object.
     */
    public static final class OpenRouterWebSearchToolResultBlock {

        private final JSONObject json;

        OpenRouterWebSearchToolResultBlock(JSONObject json) {
            this.json = json;
        }

        /**
         * @return the server tool call id this result belongs to, or
         *         {@code null} when absent
         */
        public String toolUseId() {
            try {
                return json.optString("tool_use_id", null);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the raw {@code caller} object (e.g.
         *         {@code {"type":"direct"}}), or {@code null} when absent
         */
        public JSONObject caller() {
            try {
                return json.optJSONObject("caller");
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the raw {@code content} object when it is the error form,
         *         or {@code null} when absent or when {@code content} is the
         *         results array (use {@link #results()} for that form)
         */
        public JSONObject content() {
            try {
                return json.optJSONObject("content");
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the search result entries ({@code url}, {@code title},
         *         {@code page_age}, {@code encrypted_content} per entry),
         *         empty when absent or on the error form
         */
        public List<JSONObject> results() {
            List<JSONObject> result = new ArrayList<>();
            try {
                JSONArray content = json.optJSONArray("content");
                if (content != null) {
                    for (int i = 0; i < content.length(); i++) {
                        JSONObject entry = content.optJSONObject(i);
                        if (entry != null) {
                            result.add(entry);
                        }
                    }
                }
            } catch (Exception e) {
                // swallow-and-return-empty convention
            }
            return result;
        }

        /**
         * @return the error code when the search failed (e.g.
         *         {@code unavailable}, {@code max_uses_exceeded}), or
         *         {@code null} on the success form
         */
        public String errorCode() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("error_code", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the verbatim block object */
        public JSONObject json() {
            return json;
        }
    }

    /**
     * A {@code web_fetch_tool_result} content block - the result (or error)
     * of a server-orchestrated web fetch. The {@code content} field is a
     * union: {@code web_fetch_result} (fetched document) or
     * {@code web_fetch_tool_result_error}.
     */
    public static final class OpenRouterWebFetchToolResultBlock {

        private final JSONObject json;

        OpenRouterWebFetchToolResultBlock(JSONObject json) {
            this.json = json;
        }

        /**
         * @return the server tool call id this result belongs to, or
         *         {@code null} when absent
         */
        public String toolUseId() {
            try {
                return json.optString("tool_use_id", null);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the raw {@code caller} object, or {@code null} when absent
         */
        public JSONObject caller() {
            try {
                return json.optJSONObject("caller");
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the raw {@code content} union object, or {@code null} when absent */
        public JSONObject content() {
            try {
                return json.optJSONObject("content");
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the fetched URL ({@code content.url}), or {@code null} on
         *         the error form or when absent
         */
        public String url() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("url", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the retrieval timestamp ({@code content.retrieved_at}),
         *         or {@code null} when absent
         */
        public String retrievedAt() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("retrieved_at", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the fetched document ({@code content.content}, a document
         *         block with {@code source} / {@code title}), or {@code null}
         *         when absent
         */
        public JSONObject document() {
            try {
                JSONObject content = content();
                return content != null ? content.optJSONObject("content") : null;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the error code when the fetch failed (e.g.
         *         {@code url_not_accessible}, {@code unavailable}), or
         *         {@code null} on the success form
         */
        public String errorCode() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("error_code", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the verbatim block object */
        public JSONObject json() {
            return json;
        }
    }

    /**
     * A {@code code_execution_tool_result} content block - the result (or
     * error) of a code-execution server tool. The {@code content} field is a
     * union: {@code code_execution_result}, {@code encrypted_code_execution_result}
     * or {@code code_execution_tool_result_error}.
     */
    public static final class OpenRouterCodeExecutionToolResultBlock {

        private final JSONObject json;

        OpenRouterCodeExecutionToolResultBlock(JSONObject json) {
            this.json = json;
        }

        /**
         * @return the server tool call id this result belongs to, or
         *         {@code null} when absent
         */
        public String toolUseId() {
            try {
                return json.optString("tool_use_id", null);
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the raw {@code content} union object, or {@code null} when absent */
        public JSONObject content() {
            try {
                return json.optJSONObject("content");
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the process exit code ({@code content.return_code}), or
         *         {@code null} when absent
         */
        public Integer returnCode() {
            try {
                JSONObject content = content();
                if (content == null || !content.has("return_code") || content.isNull("return_code")) {
                    return null;
                }
                Object value = content.get("return_code");
                return value instanceof Number number ? number.intValue() : null;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the captured standard output ({@code content.stdout}), or
         *         {@code null} when absent (the encrypted variant carries
         *         {@link #encryptedStdout()} instead)
         */
        public String stdout() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("stdout", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the captured standard error ({@code content.stderr}), or
         *         {@code null} when absent
         */
        public String stderr() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("stderr", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the encrypted standard output of the
         *         {@code encrypted_code_execution_result} variant, or
         *         {@code null} on the plaintext form
         */
        public String encryptedStdout() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("encrypted_stdout", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the file ids the execution produced ({@code content.content[].file_id}),
         *         empty when absent or on the error form
         */
        public List<String> outputFileIds() {
            List<String> result = new ArrayList<>();
            try {
                JSONObject content = content();
                JSONArray outputs = content == null ? null : content.optJSONArray("content");
                if (outputs != null) {
                    for (int i = 0; i < outputs.length(); i++) {
                        JSONObject output = outputs.optJSONObject(i);
                        if (output != null && output.optString("file_id", null) != null) {
                            result.add(output.optString("file_id"));
                        }
                    }
                }
            } catch (Exception e) {
                // swallow-and-return-empty convention
            }
            return result;
        }

        /**
         * @return the error code when the execution failed (e.g.
         *         {@code invalid_tool_input}, {@code execution_time_exceeded}),
         *         or {@code null} on the success form
         */
        public String errorCode() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("error_code", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the verbatim block object */
        public JSONObject json() {
            return json;
        }
    }

    /**
     * A {@code bash_code_execution_tool_result} content block - the result
     * (or error) of a bash code-execution server tool. The {@code content}
     * field is a union: {@code bash_code_execution_result} or
     * {@code bash_code_execution_tool_result_error}.
     */
    public static final class OpenRouterBashCodeExecutionToolResultBlock {

        private final JSONObject json;

        OpenRouterBashCodeExecutionToolResultBlock(JSONObject json) {
            this.json = json;
        }

        /**
         * @return the server tool call id this result belongs to, or
         *         {@code null} when absent
         */
        public String toolUseId() {
            try {
                return json.optString("tool_use_id", null);
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the raw {@code content} union object, or {@code null} when absent */
        public JSONObject content() {
            try {
                return json.optJSONObject("content");
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the process exit code ({@code content.return_code}), or
         *         {@code null} when absent
         */
        public Integer returnCode() {
            try {
                JSONObject content = content();
                if (content == null || !content.has("return_code") || content.isNull("return_code")) {
                    return null;
                }
                Object value = content.get("return_code");
                return value instanceof Number number ? number.intValue() : null;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the captured standard output ({@code content.stdout}), or
         *         {@code null} when absent
         */
        public String stdout() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("stdout", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the captured standard error ({@code content.stderr}), or
         *         {@code null} when absent
         */
        public String stderr() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("stderr", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the file ids the execution produced ({@code content.content[].file_id}),
         *         empty when absent or on the error form
         */
        public List<String> outputFileIds() {
            List<String> result = new ArrayList<>();
            try {
                JSONObject content = content();
                JSONArray outputs = content == null ? null : content.optJSONArray("content");
                if (outputs != null) {
                    for (int i = 0; i < outputs.length(); i++) {
                        JSONObject output = outputs.optJSONObject(i);
                        if (output != null && output.optString("file_id", null) != null) {
                            result.add(output.optString("file_id"));
                        }
                    }
                }
            } catch (Exception e) {
                // swallow-and-return-empty convention
            }
            return result;
        }

        /**
         * @return the error code when the execution failed (documented
         *         values: {@code invalid_tool_input}, {@code unavailable},
         *         {@code too_many_requests}, {@code execution_time_exceeded},
         *         {@code output_file_too_large}), or {@code null} on the
         *         success form
         */
        public String errorCode() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("error_code", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the verbatim block object */
        public JSONObject json() {
            return json;
        }
    }

    /**
     * A {@code text_editor_code_execution_tool_result} content block - the
     * result (or error) of the text-editor code execution tool. The
     * {@code content} field is a union of the view / create / str_replace
     * result forms and the error form.
     */
    public static final class OpenRouterTextEditorCodeExecutionToolResultBlock {

        private final JSONObject json;

        OpenRouterTextEditorCodeExecutionToolResultBlock(JSONObject json) {
            this.json = json;
        }

        /**
         * @return the server tool call id this result belongs to, or
         *         {@code null} when absent
         */
        public String toolUseId() {
            try {
                return json.optString("tool_use_id", null);
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the raw {@code content} union object, or {@code null} when absent */
        public JSONObject content() {
            try {
                return json.optJSONObject("content");
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the content variant ({@code content.type}: {@code text_editor_code_execution_view_result},
         *         {@code ..._create_result}, {@code ..._str_replace_result} or
         *         {@code ..._tool_result_error}), or {@code null} when absent
         */
        public String resultType() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("type", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the viewed file content ({@code content.content}, view
         *         result form only), or {@code null} when absent
         */
        public String text() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("content", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the viewed file's type ({@code content.file_type}), or
         *         {@code null} when absent
         */
        public String fileType() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("file_type", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the number of lines in the returned excerpt
         *         ({@code content.num_lines}), or {@code null} when absent
         */
        public Integer numLines() {
            return contentInt("num_lines");
        }

        /**
         * @return the first line of the returned excerpt
         *         ({@code content.start_line}), or {@code null} when absent
         */
        public Integer startLine() {
            return contentInt("start_line");
        }

        /**
         * @return the total line count of the file
         *         ({@code content.total_lines}), or {@code null} when absent
         */
        public Integer totalLines() {
            return contentInt("total_lines");
        }

        /**
         * @return whether the create call updated an existing file
         *         ({@code content.is_file_update}, create result form only),
         *         or {@code null} when absent
         */
        public Boolean isFileUpdate() {
            try {
                JSONObject content = content();
                if (content == null || !content.has("is_file_update") || content.isNull("is_file_update")) {
                    return null;
                }
                return content.getBoolean("is_file_update");
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the lines the str_replace call affected
         *         ({@code content.lines}, str_replace result form only),
         *         empty when absent
         */
        public List<String> lines() {
            List<String> result = new ArrayList<>();
            try {
                JSONObject content = content();
                JSONArray lines = content == null ? null : content.optJSONArray("lines");
                if (lines != null) {
                    for (int i = 0; i < lines.length(); i++) {
                        String line = lines.optString(i, null);
                        if (line != null) {
                            result.add(line);
                        }
                    }
                }
            } catch (Exception e) {
                // swallow-and-return-empty convention
            }
            return result;
        }

        /**
         * @return the first old line of the replaced range
         *         ({@code content.old_start}, str_replace result form only),
         *         or {@code null} when absent
         */
        public Integer oldStart() {
            return contentInt("old_start");
        }

        /**
         * @return the number of old lines replaced
         *         ({@code content.old_lines}, str_replace result form only),
         *         or {@code null} when absent
         */
        public Integer oldLines() {
            return contentInt("old_lines");
        }

        /**
         * @return the first new line after the replacement
         *         ({@code content.new_start}, str_replace result form only),
         *         or {@code null} when absent
         */
        public Integer newStart() {
            return contentInt("new_start");
        }

        /**
         * @return the number of new lines inserted
         *         ({@code content.new_lines}, str_replace result form only),
         *         or {@code null} when absent
         */
        public Integer newLines() {
            return contentInt("new_lines");
        }

        /**
         * @return the error code when the edit failed (e.g.
         *         {@code file_not_found}, {@code invalid_tool_input}), or
         *         {@code null} on the success form
         */
        public String errorCode() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("error_code", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the optional error message of the error form, or
         *         {@code null} when absent
         */
        public String errorMessage() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("error_message", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the verbatim block object */
        public JSONObject json() {
            return json;
        }

        private Integer contentInt(String key) {
            try {
                JSONObject content = content();
                if (content == null || !content.has(key) || content.isNull(key)) {
                    return null;
                }
                Object value = content.get(key);
                return value instanceof Number number ? number.intValue() : null;
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * A {@code tool_search_tool_result} content block - the tool references a
     * {@code openrouter:tool_search} lookup returned (or its error). The
     * {@code content} field is a union: {@code tool_search_tool_search_result}
     * or {@code tool_search_tool_result_error}.
     */
    public static final class OpenRouterToolSearchToolResultBlock {

        private final JSONObject json;

        OpenRouterToolSearchToolResultBlock(JSONObject json) {
            this.json = json;
        }

        /**
         * @return the server tool call id this result belongs to, or
         *         {@code null} when absent
         */
        public String toolUseId() {
            try {
                return json.optString("tool_use_id", null);
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the raw {@code content} union object, or {@code null} when absent */
        public JSONObject content() {
            try {
                return json.optJSONObject("content");
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the tool names this lookup resolved
         *         ({@code content.tool_references[].tool_name}), empty when
         *         absent or on the error form
         */
        public List<String> toolReferenceNames() {
            List<String> result = new ArrayList<>();
            try {
                JSONObject content = content();
                JSONArray references = content == null ? null : content.optJSONArray("tool_references");
                if (references != null) {
                    for (int i = 0; i < references.length(); i++) {
                        JSONObject reference = references.optJSONObject(i);
                        if (reference != null && reference.optString("tool_name", null) != null) {
                            result.add(reference.optString("tool_name"));
                        }
                    }
                }
            } catch (Exception e) {
                // swallow-and-return-empty convention
            }
            return result;
        }

        /**
         * @return the error code when the lookup failed, or {@code null} on
         *         the success form
         */
        public String errorCode() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("error_code", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the optional error message of the error form, or
         *         {@code null} when absent
         */
        public String errorMessage() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("error_message", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the verbatim block object */
        public JSONObject json() {
            return json;
        }
    }

    /**
     * An {@code advisor_tool_result} content block - the advisor model's
     * response. The {@code content} object is provider-shaped; the common
     * form carries {@code text} and {@code type}.
     */
    public static final class OpenRouterAdvisorToolResultBlock {

        private final JSONObject json;

        OpenRouterAdvisorToolResultBlock(JSONObject json) {
            this.json = json;
        }

        /**
         * @return the server tool call id this result belongs to, or
         *         {@code null} when absent
         */
        public String toolUseId() {
            try {
                return json.optString("tool_use_id", null);
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the raw {@code content} object, or {@code null} when absent */
        public JSONObject content() {
            try {
                return json.optJSONObject("content");
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the advice text ({@code content.text}), or {@code null}
         *         when absent - use {@link #content()} for other shapes
         */
        public String text() {
            try {
                JSONObject content = content();
                return content != null ? content.optString("text", null) : null;
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the verbatim block object */
        public JSONObject json() {
            return json;
        }
    }

    /**
     * An {@code openrouter_shell_tool_result} content block - the output of
     * an {@code openrouter:shell} call executed in the OpenRouter sandbox.
     */
    public static final class OpenRouterShellToolResultBlock {

        private final JSONObject json;

        OpenRouterShellToolResultBlock(JSONObject json) {
            this.json = json;
        }

        /**
         * @return the server tool call id this result belongs to, or
         *         {@code null} when absent
         */
        public String toolUseId() {
            try {
                return json.optString("tool_use_id", null);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the canonical container id the command ran under (the
         *         {@code {container_id}} of the Container Files API,
         *         reusable as a {@code container_reference}), or
         *         {@code null} when absent
         */
        public String containerId() {
            try {
                return json.optString("container_id", null);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the raw {@code content} object (e.g. {@code output} entries
         *         with {@code stdout} / {@code stderr} / {@code outcome}), or
         *         {@code null} when absent
         */
        public JSONObject content() {
            try {
                return json.optJSONObject("content");
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the file citations for files the command created or
         *         modified (most-recently-touched first, at most 10; each
         *         entry a {@code container_file_citation} with
         *         {@code container_id} / {@code file_id} / {@code filename} /
         *         {@code start_index} / {@code end_index}), empty when absent
         */
        public List<JSONObject> files() {
            List<JSONObject> result = new ArrayList<>();
            try {
                JSONArray files = json.optJSONArray("files");
                if (files != null) {
                    for (int i = 0; i < files.length(); i++) {
                        JSONObject file = files.optJSONObject(i);
                        if (file != null) {
                            result.add(file);
                        }
                    }
                }
            } catch (Exception e) {
                // swallow-and-return-empty convention
            }
            return result;
        }

        /**
         * @return the container file ids of {@link #files()}, empty when
         *         absent
         */
        public List<String> fileIds() {
            List<String> result = new ArrayList<>();
            for (JSONObject file : files()) {
                String fileId = file.optString("file_id", null);
                if (fileId != null) {
                    result.add(fileId);
                }
            }
            return result;
        }

        /** @return the verbatim block object */
        public JSONObject json() {
            return json;
        }
    }

    /**
     * An {@code openrouter_bash_tool_result} content block - the output of an
     * {@code openrouter:bash} call executed in the OpenRouter sandbox
     * ({@code engine: 'openrouter'}).
     */
    public static final class OpenRouterBashToolResultBlock {

        private final JSONObject json;

        OpenRouterBashToolResultBlock(JSONObject json) {
            this.json = json;
        }

        /**
         * @return the server tool call id this result belongs to, or
         *         {@code null} when absent
         */
        public String toolUseId() {
            try {
                return json.optString("tool_use_id", null);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the canonical container id the command ran under (the
         *         {@code {container_id}} of the Container Files API,
         *         reusable as a {@code container_reference}), or
         *         {@code null} when absent
         */
        public String containerId() {
            try {
                return json.optString("container_id", null);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the raw {@code content} object (e.g. {@code command},
         *         {@code exitCode}, {@code stdout}, {@code stderr}), or
         *         {@code null} when absent
         */
        public JSONObject content() {
            try {
                return json.optJSONObject("content");
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the file citations for files the command created or
         *         modified (most-recently-touched first, at most 10; each
         *         entry a {@code container_file_citation} with
         *         {@code container_id} / {@code file_id} / {@code filename} /
         *         {@code start_index} / {@code end_index}), empty when absent
         */
        public List<JSONObject> files() {
            List<JSONObject> result = new ArrayList<>();
            try {
                JSONArray files = json.optJSONArray("files");
                if (files != null) {
                    for (int i = 0; i < files.length(); i++) {
                        JSONObject file = files.optJSONObject(i);
                        if (file != null) {
                            result.add(file);
                        }
                    }
                }
            } catch (Exception e) {
                // swallow-and-return-empty convention
            }
            return result;
        }

        /**
         * @return the container file ids of {@link #files()}, empty when
         *         absent
         */
        public List<String> fileIds() {
            List<String> result = new ArrayList<>();
            for (JSONObject file : files()) {
                String fileId = file.optString("file_id", null);
                if (fileId != null) {
                    result.add(fileId);
                }
            }
            return result;
        }

        /** @return the verbatim block object */
        public JSONObject json() {
            return json;
        }
    }

    /**
     * One entry of {@code context_management.applied_edits} - a server-side
     * context edit the serving provider applied this turn. The schema types
     * only the {@code type} field; everything else is provider-specific and
     * read from the verbatim JSON.
     */
    public static final class OpenRouterAppliedContextEdit {

        private final JSONObject json;

        OpenRouterAppliedContextEdit(JSONObject json) {
            this.json = json;
        }

        /**
         * JSON path: {@code context_management.applied_edits[].type} - the
         * edit strategy that ran (e.g. {@code clear_tool_uses_20250919}).
         *
         * @return the edit type, or {@code null} when absent
         */
        public String type() {
            try {
                return json.optString("type", null);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the verbatim edit object (the strategy-specific fields,
         *         e.g. what a clear edit removed)
         */
        public JSONObject json() {
            return json;
        }
    }

    /**
     * One entry of {@code input_transformations} - a server-side
     * transformation the serving provider applied to the request input.
     */
    public static final class OpenRouterInputTransformation {

        private final JSONObject json;

        OpenRouterInputTransformation(JSONObject json) {
            this.json = json;
        }

        /**
         * JSON path: {@code input_transformations[].type} - the
         * transformation (e.g. {@code thinking_dropped}).
         *
         * @return the transformation type, or {@code null} when absent
         */
        public String type() {
            try {
                return json.optString("type", null);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * JSON path: {@code input_transformations[].path} - where in the
         * input the transformation hit (e.g.
         * {@code messages.1.content.0}), or {@code null} when absent.
         *
         * @return the path, or {@code null} when absent
         */
        public String path() {
            try {
                return json.optString("path", null);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * JSON path: {@code input_transformations[].reason} - why the
         * transformation ran (e.g. {@code prefix_binding_mismatch}), or
         * {@code null} when absent.
         *
         * @return the reason, or {@code null} when absent
         */
        public String reason() {
            try {
                return json.optString("reason", null);
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * One entry of {@code safeguard_results} - the outcome of an Anthropic
     * server-side safeguard.
     */
    public static final class OpenRouterSafeguardResult {

        private final JSONObject json;

        OpenRouterSafeguardResult(JSONObject json) {
            this.json = json;
        }

        /**
         * JSON path: {@code safeguard_results[].type} - the safeguard type
         * (e.g. {@code dangerous_tool_use}).
         *
         * @return the type, or {@code null} when absent
         */
        public String type() {
            try {
                return json.optString("type", null);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * JSON path: {@code safeguard_results[].status} - a JSON object whose
         * shape varies per safeguard type (e.g. {@code dangerous_tool_use}: a
         * top-level {@code type} status such as {@code allowed}/{@code blocked},
         * plus tool-use-id keys mapped to per-tool status strings), or
         * {@code null} when absent. Use {@link #json()} for the raw form.
         *
         * @return the status object, or {@code null} when absent
         */
        public JSONObject status() {
            try {
                return json.optJSONObject("status");
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the verbatim result object
         */
        public JSONObject json() {
            return json;
        }
    }
}
