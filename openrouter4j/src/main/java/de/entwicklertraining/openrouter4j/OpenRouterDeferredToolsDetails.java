package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

/**
 * A typed view of one {@code DeferredToolsDetails} object: the totals of a
 * deferred-tools run across <b>every</b> upstream model call the run made.
 * The native usage fields of the response report only the final call (and
 * {@code cost} covers every call), so a run's true token consumption is read
 * from here: {@code usage.deferred_tools_details} on the chat-completions
 * usage, the Anthropic-Messages usage and the Responses usage alike. On
 * streaming responses the object arrives with the terminal usage chunk like
 * {@code server_tool_use_details}.
 *
 * <p>Shared by the three inference response views
 * ({@code OpenRouterChatCompletionResponse}, {@code OpenRouterMessagesResponse},
 * {@code OpenRouterResponsesResponse}).
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterDeferredToolsDetails {

    private final JSONObject json;

    /**
     * Wraps one {@code DeferredToolsDetails} object. A {@code null} argument
     * is read as an empty object - every accessor answers {@code null}.
     *
     * @param json the {@code usage.deferred_tools_details} object
     */
    public OpenRouterDeferredToolsDetails(JSONObject json) {
        this.json = json != null ? json : new JSONObject();
    }

    /**
     * @return the raw details object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code turns} - the upstream model calls the run made.
     *
     * @return the number of turns, or {@code null} when absent or not a number
     */
    public Integer turns() {
        return optInt("turns");
    }

    /**
     * JSON path: {@code prompt_tokens} - prompt tokens summed over every
     * upstream call.
     *
     * @return the summed prompt tokens, or {@code null} when absent or not a
     *         number
     */
    public Integer promptTokens() {
        return optInt("prompt_tokens");
    }

    /**
     * JSON path: {@code completion_tokens} - completion tokens summed over
     * every upstream call.
     *
     * @return the summed completion tokens, or {@code null} when absent or
     *         not a number
     */
    public Integer completionTokens() {
        return optInt("completion_tokens");
    }

    /**
     * JSON path: {@code total_tokens} - the sum of {@code prompt_tokens} and
     * {@code completion_tokens} over every upstream call.
     *
     * @return the summed total tokens, or {@code null} when absent or not a
     *         number
     */
    public Integer totalTokens() {
        return optInt("total_tokens");
    }

    private Integer optInt(String key) {
        Object value = json.opt(key);
        return value instanceof Number n ? n.intValue() : null;
    }
}
