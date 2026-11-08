package de.entwicklertraining.openrouter4j.tools;

import org.json.JSONObject;

/**
 * A typed view of one entry of {@code aliases[].output_names[]} on a
 * server-tool catalog entry (schema {@code ServerToolOutputName}): what a
 * tool call surfaces as on one accepting API format when the tool was
 * requested under the spelling of the enclosing
 * {@link OpenRouterServerToolInputName}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterServerToolOutputName {

    private final JSONObject json;

    OpenRouterServerToolOutputName(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw output-name row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code api_format} - the API format the output names apply
     * to ({@code responses}, {@code chat-completions},
     * {@code anthropic-messages}; unknown values are passed through
     * verbatim).
     *
     * @return the format, or {@code null} when absent or not a string
     */
    public String apiFormat() {
        Object value = json.opt("api_format");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code type} - the output item {@code type} on Responses,
     * the content block {@code type} on Anthropic Messages, the
     * {@code reasoning_details[].type} on Chat Completions.
     *
     * @return the output type, or {@code null} when absent or not a string
     */
    public String type() {
        Object value = json.opt("type");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code name} - the Anthropic Messages
     * {@code server_tool_use} block {@code name}, when the call surfaces as
     * one.
     *
     * @return the block name, or {@code null} when absent or not a string
     */
    public String name() {
        Object value = json.opt("name");
        return value instanceof String text ? text : null;
    }
}