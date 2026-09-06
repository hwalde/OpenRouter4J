package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.Objects;

/**
 * A stop condition for OpenRouter's server-tool agent loop, sent in the
 * {@code stop_server_tools_when} request array. Any condition firing halts the
 * loop (OR logic); when set, it overrides {@code max_tool_calls}. When a
 * condition fires while the model is still emitting tool calls, the pending
 * tool calls are executed and one final turn is made with tool calls disabled,
 * so the response ends in natural language instead of an unfinished tool call.
 * <p>
 * Create instances through the typed factories ({@link #stepCountIs(int)},
 * {@link #hasToolCall(String)}, {@link #maxTokensUsed(long)},
 * {@link #maxCost(double)}, {@link #finishReasonIs(String)}) or the verbatim
 * escape hatch {@link #raw(JSONObject)} for condition types OpenRouter adds
 * after this library was released.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools">OpenRouter server tools</a>
 */
public final class OpenRouterStopCondition {

    private final JSONObject json;

    private OpenRouterStopCondition(JSONObject json) {
        this.json = json;
    }

    /**
     * Stops the loop after the agent loop has executed this many steps
     * ({@code {"type":"step_count_is","step_count":N}}).
     *
     * @param stepCount number of loop steps after which to stop
     * @return the stop condition
     */
    public static OpenRouterStopCondition stepCountIs(int stepCount) {
        JSONObject json = new JSONObject();
        json.put("type", "step_count_is");
        json.put("step_count", stepCount);
        return new OpenRouterStopCondition(json);
    }

    /**
     * Stops the loop after a tool with this name has been called
     * ({@code {"type":"has_tool_call","tool_name":"..."}}).
     *
     * @param toolName name of the tool whose call stops the loop
     * @return the stop condition
     */
    public static OpenRouterStopCondition hasToolCall(String toolName) {
        Objects.requireNonNull(toolName, "toolName must not be null");
        JSONObject json = new JSONObject();
        json.put("type", "has_tool_call");
        json.put("tool_name", toolName);
        return new OpenRouterStopCondition(json);
    }

    /**
     * Stops the loop once cumulative token usage across the loop exceeds this
     * threshold ({@code {"type":"max_tokens_used","max_tokens":N}}).
     *
     * @param maxTokens cumulative token threshold
     * @return the stop condition
     */
    public static OpenRouterStopCondition maxTokensUsed(long maxTokens) {
        JSONObject json = new JSONObject();
        json.put("type", "max_tokens_used");
        json.put("max_tokens", maxTokens);
        return new OpenRouterStopCondition(json);
    }

    /**
     * Stops the loop once cumulative cost across the loop exceeds this dollar
     * threshold ({@code {"type":"max_cost","max_cost_in_dollars":X}}).
     *
     * @param maxCostInDollars cumulative cost threshold in USD
     * @return the stop condition
     */
    public static OpenRouterStopCondition maxCost(double maxCostInDollars) {
        JSONObject json = new JSONObject();
        json.put("type", "max_cost");
        json.put("max_cost_in_dollars", maxCostInDollars);
        return new OpenRouterStopCondition(json);
    }

    /**
     * Stops the loop when the upstream model emits this finish reason, e.g.
     * {@code "length"} ({@code {"type":"finish_reason_is","reason":"..."}}).
     *
     * @param reason the upstream finish reason that stops the loop
     * @return the stop condition
     */
    public static OpenRouterStopCondition finishReasonIs(String reason) {
        Objects.requireNonNull(reason, "reason must not be null");
        JSONObject json = new JSONObject();
        json.put("type", "finish_reason_is");
        json.put("reason", reason);
        return new OpenRouterStopCondition(json);
    }

    /**
     * Verbatim escape hatch: emits the given condition object unchanged. Use it
     * for condition types OpenRouter adds after this library was released.
     *
     * @param condition the raw condition JSON; must contain a {@code type} field
     * @return the stop condition
     */
    public static OpenRouterStopCondition raw(JSONObject condition) {
        Objects.requireNonNull(condition, "condition must not be null");
        if (!condition.has("type")) {
            throw new IllegalArgumentException("A stop condition must carry a 'type' field");
        }
        return new OpenRouterStopCondition(new JSONObject(condition.toString()));
    }

    /**
     * The JSON object emitted into the {@code stop_server_tools_when} request array.
     */
    public JSONObject toJson() {
        return new JSONObject(json.toString());
    }

    @Override
    public String toString() {
        return json.toString();
    }
}
