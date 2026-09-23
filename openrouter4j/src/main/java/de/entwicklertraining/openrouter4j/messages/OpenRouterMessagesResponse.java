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

    OpenRouterMessagesResponse(JSONObject json, OpenRouterMessagesRequest request) {
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
