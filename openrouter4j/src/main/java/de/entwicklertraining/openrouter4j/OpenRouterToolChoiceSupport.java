package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

/**
 * A typed view of the {@code ToolChoiceSupport} object on an endpoint row
 * ({@code supports_tool_choice}): per-variant {@code tool_choice} support.
 *
 * <p>Trap: {@code tool_choice} in {@code supported_parameters} only says
 * the parameter is accepted; these flags say which of its values passed
 * testing. Check them before relying on a specific {@code tool_choice}
 * form against an endpoint.
 *
 * <p>Shared by the two views over the {@code PublicEndpoint} schema:
 * {@code OpenRouterModelEndpoint} (GET /models/{author}/{slug}/endpoints)
 * and {@code OpenRouterZdrEndpoint} (GET /endpoints/zdr).
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterToolChoiceSupport {

    private final JSONObject json;

    /**
     * Wraps one {@code supports_tool_choice} object. A {@code null} argument
     * is read as an empty object - every accessor answers {@code null}.
     *
     * @param json the tool-choice support object
     */
    public OpenRouterToolChoiceSupport(JSONObject json) {
        this.json = json != null ? json : new JSONObject();
    }

    /**
     * @return the raw support object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code auto} - whether the endpoint supports
     * {@code "tool_choice": "auto"}.
     *
     * @return the value, or {@code null} when absent or not a boolean
     */
    public Boolean auto() {
        return optBoolean("auto");
    }

    /**
     * JSON path: {@code function} - whether the endpoint supports naming a
     * function, {@code "tool_choice": {"type": "function", ...}}.
     *
     * @return the value, or {@code null} when absent or not a boolean
     */
    public Boolean function() {
        return optBoolean("function");
    }

    /**
     * JSON path: {@code none} - whether the endpoint supports
     * {@code "tool_choice": "none"}.
     *
     * @return the value, or {@code null} when absent or not a boolean
     */
    public Boolean none() {
        return optBoolean("none");
    }

    /**
     * JSON path: {@code required} - whether the endpoint supports
     * {@code "tool_choice": "required"}.
     *
     * @return the value, or {@code null} when absent or not a boolean
     */
    public Boolean required() {
        return optBoolean("required");
    }

    private Boolean optBoolean(String key) {
        Object value = json.opt(key);
        return value instanceof Boolean flag ? flag : null;
    }
}
