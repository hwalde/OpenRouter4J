package de.entwicklertraining.openrouter4j.responses;

import de.entwicklertraining.openrouter4j.OpenRouterAlignmentPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterArmorPlugin;
import de.entwicklertraining.openrouter4j.OpenRouterDeferredToolsDetails;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import de.entwicklertraining.openrouter4j.OpenRouterSafeguardResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The response of a {@link OpenRouterResponsesRequest}:
 * POST /responses (non-streaming form, schema
 * {@code OpenResponsesResult}).
 *
 * <p>All accessors follow the swallow-and-return-null convention: a missing
 * or unexpected field yields {@code null} (or an empty list), never an
 * exception - a malformed response looks like an empty one.
 *
 * <p>The {@code output} array is heterogeneous (message, reasoning,
 * function_call, server-tool items, ...); typed convenience views exist for
 * the common item kinds, and {@link #output()} exposes the raw items
 * verbatim.
 *
 * <p>Citations: the {@code output_text} content parts of message items carry
 * {@link OpenRouterTextAnnotation} entries (url/file citations) reachable via
 * {@link OutputMessageItem#outputTextParts()} /
 * {@link OutputMessageItem#annotations()}. {@code start_index} /
 * {@code end_index} are character offsets into the annotated part's
 * {@code text}, so a citation span is only meaningful together with the part
 * text it annotates. The streaming events carry annotations per content part
 * too - on the streaming path they are readable from the raw event JSON
 * (which the Responses streaming handler delivers verbatim) unless/until an
 * accumulator types them.
 */
public final class OpenRouterResponsesResponse extends OpenRouterResponse<OpenRouterResponsesRequest> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterResponsesResponse(JSONObject json, OpenRouterResponsesRequest request) {
        super(json, request);
    }

    /** @return the JSON field {@code id} (e.g. {@code resp_abc123}) */
    public String id() {
        return json.optString("id", null);
    }

    /** @return the JSON field {@code object} (always {@code response} on success) */
    public String object() {
        return json.optString("object", null);
    }

    /** @return the JSON field {@code model} */
    public String model() {
        return json.optString("model", null);
    }

    /** @return the JSON field {@code status} ({@code completed}, {@code failed}, {@code incomplete}, ...) */
    public String status() {
        return json.optString("status", null);
    }

    /** @return the JSON field {@code created_at} (unix seconds) */
    public Long createdAt() {
        return optLongOrNull("created_at");
    }

    /** @return the JSON field {@code completed_at} (unix seconds), or {@code null} while not completed */
    public Long completedAt() {
        return optLongOrNull("completed_at");
    }

    /**
     * @return the raw {@code error} object, or {@code null} when the
     *         response carries no error
     */
    public JSONObject error() {
        return json.optJSONObject("error");
    }

    /**
     * JSON path: {@code error_type} (top level; falls back to
     * {@code error.error_type} when the top-level key is absent) - the
     * canonical OpenRouter error type (the {@code ApiErrorType} schema enum:
     * {@code context_length_exceeded}, {@code payment_required},
     * {@code provider_overloaded}, {@code refusal}, {@code timeout}, ...).
     * Per the API docs this identifier is stable across all OpenRouter API
     * formats, so callers can branch on it. Follows the
     * swallow-and-return-null convention: {@code null} when neither location
     * carries the key.
     *
     * @return the canonical error type, or {@code null} when absent
     */
    public String errorType() {
        if (json.has("error_type") && !json.isNull("error_type")) {
            return json.optString("error_type", null);
        }
        JSONObject error = json.optJSONObject("error");
        if (error != null && error.has("error_type") && !error.isNull("error_type")) {
            return error.optString("error_type", null);
        }
        return null;
    }

    /** @return the raw {@code incomplete_details} object, or {@code null} */
    public JSONObject incompleteDetails() {
        return json.optJSONObject("incomplete_details");
    }

    /** @return the raw {@code output} items, empty when absent */
    public List<JSONObject> output() {
        JSONArray array = json.optJSONArray("output");
        if (array == null) {
            return List.of();
        }
        List<JSONObject> items = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Convenience: all assistant text of the output, i.e. the {@code text}
     * of every {@code output_text} content part of every {@code message}
     * item, concatenated. Falls back to the top-level {@code output_text}
     * field when present. Empty when the output carries no message text.
     *
     * @return the concatenated output text
     */
    public String outputText() {
        StringBuilder sb = new StringBuilder();
        for (OutputMessageItem item : messageItems()) {
            for (String part : item.textParts()) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(part);
            }
        }
        if (sb.length() > 0) {
            return sb.toString();
        }
        return json.optString("output_text", null);
    }

    /** @return the {@code message} items of the output, empty when absent */
    public List<OutputMessageItem> messageItems() {
        List<OutputMessageItem> items = new ArrayList<>();
        for (JSONObject raw : output()) {
            if ("message".equals(raw.optString("type"))) {
                items.add(new OutputMessageItem(raw));
            }
        }
        return items;
    }

    /** @return the {@code function_call} items of the output, empty when absent */
    public List<OutputFunctionCallItem> functionCallItems() {
        List<OutputFunctionCallItem> items = new ArrayList<>();
        for (JSONObject raw : output()) {
            if ("function_call".equals(raw.optString("type"))) {
                items.add(new OutputFunctionCallItem(raw));
            }
        }
        return items;
    }

    /** @return the {@code reasoning} items of the output, empty when absent */
    public List<OutputReasoningItem> reasoningItems() {
        List<OutputReasoningItem> items = new ArrayList<>();
        for (JSONObject raw : output()) {
            if ("reasoning".equals(raw.optString("type"))) {
                items.add(new OutputReasoningItem(raw));
            }
        }
        return items;
    }

    /** @return the JSON field {@code service_tier}, or {@code null} when unset */
    public String serviceTier() {
        return json.optString("service_tier", null);
    }

    /**
     * @return the raw {@code openrouter_metadata} routing object (present
     *         when the request opted in via the {@code X-OpenRouter-Metadata}
     *         header or the legacy experimental header), or {@code null}
     */
    public JSONObject openrouterMetadata() {
        return json.optJSONObject("openrouter_metadata");
    }

    /**
     * JSON path: {@code alignment} - the result of the
     * {@link OpenRouterAlignmentPlugin} for this request (Beta, schema
     * {@code Alignment}: {@code {"calls": [...]}}), or {@code null} when
     * absent or malformed. Swallow-and-return-{@code null} convention.
     * <p>
     * Traps: the {@code alignment} plugin and its response object are in Beta
     * and the per-call record shapes may change. On a streamed Responses
     * request, {@code alignment} travels on the {@code response} object inside
     * the {@code response.completed} event - read it from the raw event JSON
     * delivered to the stream handler.
     *
     * @return the raw {@code alignment} object, or {@code null} when absent
     * @see OpenRouterAlignmentPlugin
     * @see #alignmentCalls()
     */
    public JSONObject alignment() {
        try {
            return json.optJSONObject("alignment");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code alignment.calls} - the evaluated assistant-message
     * call records in production order, empty when {@code alignment} or
     * {@code calls} is absent (never {@code null}). Swallow-and-return-empty
     * convention.
     *
     * @return the evaluated call records in order, empty when absent
     * @see #alignment()
     * @see OpenRouterAlignmentPlugin
     */
    public List<JSONObject> alignmentCalls() {
        try {
            JSONObject alignment = alignment();
            List<JSONObject> result = new ArrayList<>();
            if (alignment != null && alignment.has("calls") && !alignment.isNull("calls")) {
                JSONArray calls = alignment.optJSONArray("calls");
                if (calls != null) {
                    for (int i = 0; i < calls.length(); i++) {
                        JSONObject entry = calls.optJSONObject(i);
                        if (entry != null) {
                            result.add(entry);
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * JSON path: {@code safeguard_results} - OpenRouter Armor's verdict for
     * each client tool call of this response (schema
     * {@code ArmorSafeguardResult}), present whenever Armor
     * ({@link OpenRouterArmorPlugin}) evaluated the response (even when
     * nothing was flagged; independent of the {@code openrouter_metadata}
     * opt-in). Swallow-and-return-empty convention.
     * <p>
     * Traps: Armor is in Beta and the {@code safeguard_results} shape may
     * change. On a streamed Responses request, {@code safeguard_results}
     * travels on the {@code response} object inside the
     * {@code response.completed} event - read it from the raw event JSON
     * delivered to the stream handler.
     *
     * @return the safeguard results in order, empty when absent or malformed
     *         (never {@code null})
     * @see OpenRouterArmorPlugin
     * @see OpenRouterSafeguardResult
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

    /** @return the JSON field {@code usage.input_tokens} */
    public Long inputTokens() {
        return usageNestedLong("input_tokens");
    }

    /** @return the JSON field {@code usage.output_tokens} */
    public Long outputTokens() {
        return usageNestedLong("output_tokens");
    }

    /** @return the JSON field {@code usage.total_tokens} */
    public Long totalTokens() {
        return usageNestedLong("total_tokens");
    }

    /** @return the JSON field {@code usage.input_tokens_details.cached_tokens} */
    public Long cachedTokens() {
        JSONObject details = usageDetails("input_tokens_details");
        return details == null ? null : optLong(details, "cached_tokens");
    }

    /** @return the JSON field {@code usage.output_tokens_details.reasoning_tokens} */
    public Long reasoningTokens() {
        JSONObject details = usageDetails("output_tokens_details");
        return details == null ? null : optLong(details, "reasoning_tokens");
    }

    /** @return the JSON field {@code usage.cost} (USD), or {@code null} when absent */
    public Double cost() {
        JSONObject usage = json.optJSONObject("usage");
        if (usage == null || !usage.has("cost")) {
            return null;
        }
        return usage.optDouble("cost");
    }

    /** @return the raw {@code usage} object, or {@code null} when absent */
    public JSONObject usage() {
        return json.optJSONObject("usage");
    }

    /**
     * JSON path: {@code usage.deferred_tools_details} - the totals of a
     * deferred-tools run summed over <b>every</b> upstream model call (the
     * native usage fields report only the final call).
     *
     * @return the typed details view, or {@code null} when absent (no
     *         deferred-tools run, or a malformed value)
     * @see de.entwicklertraining.openrouter4j.OpenRouterDeferredToolsDetails
     */
    public OpenRouterDeferredToolsDetails deferredToolsDetails() {
        JSONObject details = usageDetails("deferred_tools_details");
        return details != null ? new OpenRouterDeferredToolsDetails(details) : null;
    }

    private JSONObject usageDetails(String key) {
        JSONObject usage = json.optJSONObject("usage");
        return usage == null ? null : usage.optJSONObject(key);
    }

    private Long usageNestedLong(String key) {
        JSONObject usage = json.optJSONObject("usage");
        return usage == null ? null : optLong(usage, key);
    }

    private Long optLongOrNull(String key) {
        return optLong(json, key);
    }

    private Long optLong(JSONObject object, String key) {
        if (!object.has(key) || object.isNull(key)) {
            return null;
        }
        Object value = object.get(key);
        return value instanceof Number number ? number.longValue() : null;
    }

    /** A typed view of one {@code message} output item. */
    public static final class OutputMessageItem {

        private final JSONObject raw;

        private OutputMessageItem(JSONObject raw) {
            this.raw = raw;
        }

        /** @return the raw item JSON */
        public JSONObject json() {
            return raw;
        }

        /** @return the item id */
        public String id() {
            return raw.optString("id", null);
        }

        /** @return the item role */
        public String role() {
            return raw.optString("role", null);
        }

        /** @return the item status */
        public String status() {
            return raw.optString("status", null);
        }

        /**
         * @return the {@code text} of every {@code output_text} content
         *         part, in order; empty when the content is a plain string
         *         (use {@link #text()}) or carries no text parts
         */
        public List<String> textParts() {
            JSONArray content = raw.optJSONArray("content");
            if (content == null) {
                return List.of();
            }
            List<String> parts = new ArrayList<>();
            for (int i = 0; i < content.length(); i++) {
                JSONObject part = content.optJSONObject(i);
                if (part != null && "output_text".equals(part.optString("type"))) {
                    parts.add(part.optString("text", ""));
                }
            }
            return parts;
        }

        /**
         * Typed views of the {@code output_text} content parts, carrying the
         * part text together with its {@link OpenRouterTextAnnotation}
         * citations. Empty when the content is a plain string or carries no
         * text parts.
         *
         * @return the typed text parts, in order
         */
        public List<OutputTextPart> outputTextParts() {
            JSONArray content = raw.optJSONArray("content");
            if (content == null) {
                return List.of();
            }
            List<OutputTextPart> parts = new ArrayList<>();
            for (int i = 0; i < content.length(); i++) {
                JSONObject part = content.optJSONObject(i);
                if (part != null && "output_text".equals(part.optString("type"))) {
                    parts.add(new OutputTextPart(part));
                }
            }
            return parts;
        }

        /**
         * Convenience: every annotation of every {@code output_text} part,
         * flattened across the parts in order - the citations that attribute
         * spans of the answer to their sources. Empty when the output text
         * carries no annotations. Pair each entry with the
         * {@link OutputTextPart#text()} it annotates -
         * {@code start_index}/{@code end_index} are character offsets into
         * that part's text.
         *
         * @return the flattened annotations
         */
        public List<OpenRouterTextAnnotation> annotations() {
            List<OpenRouterTextAnnotation> all = new ArrayList<>();
            for (OutputTextPart part : outputTextParts()) {
                all.addAll(part.annotations());
            }
            return all;
        }

        /**
         * @return the concatenated {@code output_text} parts, the plain
         *         string content, or {@code null} when neither is present
         */
        public String text() {
            List<String> parts = textParts();
            if (!parts.isEmpty()) {
                return String.join("\n", parts);
            }
            return raw.optString("content", null);
        }
    }

    /** A typed view of one {@code function_call} output item. */
    public static final class OutputFunctionCallItem {

        private final JSONObject raw;

        private OutputFunctionCallItem(JSONObject raw) {
            this.raw = raw;
        }

        /** @return the raw item JSON */
        public JSONObject json() {
            return raw;
        }

        /** @return the item id */
        public String id() {
            return raw.optString("id", null);
        }

        /** @return the JSON field {@code call_id} */
        public String callId() {
            return raw.optString("call_id", null);
        }

        /** @return the called function name */
        public String name() {
            return raw.optString("name", null);
        }

        /** @return the JSON-encoded {@code arguments} string */
        public String arguments() {
            return raw.optString("arguments", null);
        }

        /** @return the item status */
        public String status() {
            return raw.optString("status", null);
        }
    }

    /** A typed view of one {@code reasoning} output item. */
    public static final class OutputReasoningItem {

        private final JSONObject raw;

        private OutputReasoningItem(JSONObject raw) {
            this.raw = raw;
        }

        /** @return the raw item JSON */
        public JSONObject json() {
            return raw;
        }

        /** @return the item id */
        public String id() {
            return raw.optString("id", null);
        }

        /** @return the JSON field {@code summary} array, empty when absent */
        public List<JSONObject> summary() {
            JSONArray array = raw.optJSONArray("summary");
            if (array == null) {
                return List.of();
            }
            List<JSONObject> entries = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject entry = array.optJSONObject(i);
                if (entry != null) {
                    entries.add(entry);
                }
            }
            return entries;
        }

        /** @return the concatenated {@code summary_text} entries, or {@code null} when there are none */
        public String summaryText() {
            StringBuilder sb = new StringBuilder();
            for (JSONObject entry : summary()) {
                if ("summary_text".equals(entry.optString("type"))) {
                    if (sb.length() > 0) {
                        sb.append('\n');
                    }
                    sb.append(entry.optString("text", ""));
                }
            }
            return sb.length() > 0 ? sb.toString() : null;
        }
    }

    /**
     * A typed view of one {@code output_text} content part of a message item:
     * the part text together with the {@link OpenRouterTextAnnotation}
     * citations that attribute spans of it to their sources.
     */
    public static final class OutputTextPart {

        private final JSONObject raw;

        private OutputTextPart(JSONObject raw) {
            this.raw = raw;
        }

        /** @return the raw content-part JSON */
        public JSONObject json() {
            return raw;
        }

        /**
         * @return the JSON field {@code text} - the part text the annotations
         *         index into ({@code start_index}/{@code end_index} are
         *         character offsets in this string)
         */
        public String text() {
            return raw.optString("text", null);
        }

        /**
         * @return the typed {@code annotations} of this part, in order;
         *         empty when absent (swallow-and-return-empty convention)
         */
        public List<OpenRouterTextAnnotation> annotations() {
            JSONArray array = raw.optJSONArray("annotations");
            if (array == null) {
                return List.of();
            }
            List<OpenRouterTextAnnotation> annotations = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject entry = array.optJSONObject(i);
                if (entry != null) {
                    annotations.add(OpenRouterTextAnnotation.of(entry));
                }
            }
            return annotations;
        }
    }
}
