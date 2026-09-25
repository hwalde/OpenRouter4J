package de.entwicklertraining.openrouter4j.responses;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

/**
 * A typed item for the {@code input} array of an OpenAI Responses request
 * (POST /responses). The API is stateless - {@code previous_response_id} is
 * rejected with HTTP 400 - so continuing a conversation (and especially a tool
 * call) happens by replaying the prior items in {@code input}: send the
 * previous assistant function call back as a {@code function_call} item and
 * your execution result as a {@code function_call_output} item carrying the
 * same {@code call_id}.
 *
 * <p>Create instances through the typed factories ({@link #functionCall},
 * {@link #functionCallOutput}, {@link #itemReference}, {@link #outputMessage},
 * {@link #reasoning}) or the verbatim escape hatch {@link #raw(JSONObject)}
 * for the item forms this library does not type (server-tool output items,
 * shell/apply-patch round trips, compaction markers, MCP items,
 * {@code additional_tools}, ...) - use {@code addInputItem(JSONObject)} for
 * anything not listed here. Append them via
 * {@code client.responses().addInput(...)}.
 *
 * @see <a href="https://openrouter.ai/docs/api_reference/responses/tool-calling">Responses tool calling</a>
 */
public final class OpenRouterInputItem {

    private final JSONObject json;

    private OpenRouterInputItem(JSONObject json) {
        this.json = json;
    }

    /**
     * A {@code function_call} item - a tool call the model made in an earlier
     * turn, replayed so the model sees the call it is answering:
     * {@code {"type":"function_call","call_id":...,"name":...,"arguments":...}}.
     *
     * @param callId the tool call id (echo the id of the original call)
     * @param name the tool name
     * @param arguments the JSON arguments string the model produced
     * @return the input item
     */
    public static OpenRouterInputItem functionCall(String callId, String name, String arguments) {
        return functionCall(callId, name, arguments, null);
    }

    /**
     * A {@code function_call} item with an explicit {@code status}.
     *
     * @param callId the tool call id (echo the id of the original call)
     * @param name the tool name
     * @param arguments the JSON arguments string the model produced
     * @param status the call status ({@code in_progress}, {@code completed},
     *        {@code incomplete} per the schema's {@code ToolCallStatus}; passed
     *        through verbatim), or {@code null} to omit the field
     * @return the input item
     */
    public static OpenRouterInputItem functionCall(String callId, String name, String arguments, String status) {
        requireText(callId, "callId");
        requireText(name, "name");
        Objects.requireNonNull(arguments, "arguments must not be null");
        JSONObject json = new JSONObject();
        json.put("type", "function_call");
        json.put("call_id", callId);
        json.put("name", name);
        json.put("arguments", arguments);
        if (status != null) {
            json.put("status", status);
        }
        return new OpenRouterInputItem(json);
    }

    /**
     * A {@code function_call_output} item - the result of executing the tool
     * call with this {@code call_id}: {@code {"type":"function_call_output",
     * "call_id":...,"output":...}}. The call id must be the one of the replayed
     * {@code function_call} item - that pairing is how the model connects
     * result and call.
     *
     * @param callId the tool call id the output belongs to
     * @param output the tool result (JSON string or plain text)
     * @return the input item
     */
    public static OpenRouterInputItem functionCallOutput(String callId, String output) {
        return functionCallOutput(callId, output, null);
    }

    /**
     * A {@code function_call_output} item with an explicit {@code status}.
     *
     * @param callId the tool call id the output belongs to
     * @param output the tool result (JSON string or plain text; multimodal
     *        content-part arrays need {@link #raw(JSONObject)})
     * @param status the call status ({@code in_progress}, {@code completed},
     *        {@code incomplete}; passed through verbatim), or {@code null} to
     *        omit the field
     * @return the input item
     */
    public static OpenRouterInputItem functionCallOutput(String callId, String output, String status) {
        requireText(callId, "callId");
        Objects.requireNonNull(output, "output must not be null");
        JSONObject json = new JSONObject();
        json.put("type", "function_call_output");
        json.put("call_id", callId);
        json.put("output", output);
        if (status != null) {
            json.put("status", status);
        }
        return new OpenRouterInputItem(json);
    }

    /**
     * An {@code item_reference} item - references an item of a previous
     * (stored) response by id instead of replaying it: {@code {"type":
     * "item_reference","id":...}}. Only usable when the referenced item is
     * still available server-side; otherwise replay the item itself.
     *
     * @param id the id of the referenced item (e.g. {@code msg-abc123})
     * @return the input item
     */
    public static OpenRouterInputItem itemReference(String id) {
        requireText(id, "id");
        JSONObject json = new JSONObject();
        json.put("type", "item_reference");
        json.put("id", id);
        return new OpenRouterInputItem(json);
    }

    /**
     * An output message item replayed into {@code input} - the assistant
     * message of a previous turn: {@code {"type":"message","role":"assistant",
     * "id":...,"content":[{"type":"output_text","text":...}],"status":"completed"}}.
     *
     * @param id the id of the original message item (e.g. {@code msg-abc123})
     * @param text the assistant message text
     * @return the input item
     */
    public static OpenRouterInputItem outputMessage(String id, String text) {
        return outputMessage(id, text, "completed");
    }

    /**
     * An output message item replayed into {@code input} with an explicit
     * {@code status}. Per the schema, the optional {@code phase} field
     * ({@code commentary} / {@code final_answer}) should be preserved and
     * resent on assistant messages for follow-up requests with models like
     * {@code gpt-5.3-codex} and later - omitting it can degrade performance.
     * The typed form does not carry {@code phase}; use {@link #raw(JSONObject)}
     * for that.
     *
     * @param id the id of the original message item
     * @param text the assistant message text
     * @param status the message status ({@code completed}, {@code incomplete},
     *        {@code in_progress}; passed through verbatim), or {@code null} to
     *        use {@code completed}
     * @return the input item
     */
    public static OpenRouterInputItem outputMessage(String id, String text, String status) {
        requireText(id, "id");
        Objects.requireNonNull(text, "text must not be null");
        JSONObject json = new JSONObject();
        json.put("type", "message");
        json.put("role", "assistant");
        json.put("id", id);
        JSONArray content = new JSONArray();
        JSONObject part = new JSONObject();
        part.put("type", "output_text");
        part.put("text", text);
        content.put(part);
        json.put("content", content);
        json.put("status", status == null ? "completed" : status);
        return new OpenRouterInputItem(json);
    }

    /**
     * A reasoning item replayed into {@code input} with one summary entry:
     * {@code {"type":"reasoning","id":...,"summary":[{"type":"summary_text",
     * "text":...}]}}. Some providers require the reasoning item (with its
     * {@code signature}) to be returned to preserve chain-of-thought across
     * turns.
     *
     * <p>Trap: pass the {@code signature} of the original item back
     * <em>unmodified</em> - providers verify it against the reasoning content
     * and a rewritten signature makes the replay fail.
     *
     * @param id the id of the original reasoning item (e.g. {@code reasoning-abc123})
     * @param summaryText the summary text of the single summary entry
     * @return the input item
     */
    public static OpenRouterInputItem reasoning(String id, String summaryText) {
        return reasoning(id, summaryText, null);
    }

    /**
     * A reasoning item replayed into {@code input} with one summary entry and
     * the original {@code signature} (see {@link #reasoning(String, String)}
     * for the signature trap).
     *
     * @param id the id of the original reasoning item
     * @param summaryText the summary text of the single summary entry
     * @param signature the original signature, passed through unmodified; or
     *        {@code null} to omit the field
     * @return the input item
     */
    public static OpenRouterInputItem reasoning(String id, String summaryText, String signature) {
        requireText(id, "id");
        requireText(summaryText, "summaryText");
        JSONArray summary = new JSONArray();
        JSONObject entry = new JSONObject();
        entry.put("type", "summary_text");
        entry.put("text", summaryText);
        summary.put(entry);
        return reasoningWithSummary(id, summary, signature);
    }

    /**
     * A reasoning item replayed into {@code input} with a verbatim summary
     * array (multi-entry summaries, provider-specific entry shapes) and the
     * original {@code signature} (see {@link #reasoning(String, String)} for
     * the signature trap). The entries are copied into the item unchanged.
     *
     * @param id the id of the original reasoning item
     * @param summary the summary array as carried by the previous response
     *        (entries like {@code {"type":"summary_text","text":...}})
     * @param signature the original signature, passed through unmodified; or
     *        {@code null} to omit the field
     * @return the input item
     */
    public static OpenRouterInputItem reasoningWithSummary(String id, JSONArray summary, String signature) {
        requireText(id, "id");
        Objects.requireNonNull(summary, "summary must not be null");
        JSONObject json = new JSONObject();
        json.put("type", "reasoning");
        json.put("id", id);
        json.put("summary", new JSONArray(summary.toString()));
        if (signature != null) {
            json.put("signature", signature);
        }
        return new OpenRouterInputItem(json);
    }

    /**
     * The verbatim escape hatch for item forms this library does not type
     * (shell/apply-patch round trips, compaction markers, MCP items,
     * {@code additional_tools}, custom tool calls, ...). The item must carry a
     * {@code type} field; everything else is passed through unchanged. For a
     * completely untyped path use {@code addInputItem(JSONObject)} directly.
     *
     * @param item the raw input item (must contain {@code type})
     * @return the input item
     */
    public static OpenRouterInputItem raw(JSONObject item) {
        Objects.requireNonNull(item, "item must not be null");
        if (item.optString("type", null) == null) {
            throw new IllegalArgumentException("a raw input item requires a type field");
        }
        return new OpenRouterInputItem(new JSONObject(item.toString()));
    }

    /**
     * @return a copy of the item as a JSON object for the {@code input} array
     *         (the copy pattern of {@code OpenRouterStopCondition} - the value
     *         class stays immutable)
     */
    public JSONObject toJson() {
        return new JSONObject(json.toString());
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be null or empty");
        }
    }
}
