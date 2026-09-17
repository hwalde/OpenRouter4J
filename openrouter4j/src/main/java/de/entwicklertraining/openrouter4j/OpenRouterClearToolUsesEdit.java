package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code clear_tool_uses_20250919} context-management strategy: clears
 * older tool uses (tool call/result pairs) from the conversation once a
 * trigger fires, so long agentic Messages conversations do not drown in old
 * tool output. Every field is optional - an unset field is omitted from the
 * JSON.
 * <p>
 * JSON fields: {@code type} (fixed), {@code trigger}
 * ({@code {"type":"input_tokens","value":N}} or
 * {@code {"type":"tool_uses","value":N}}), {@code keep}
 * ({@code {"type":"tool_uses","value":N}} - the number of most recent tool
 * uses to keep), {@code clear_at_least}
 * ({@code {"type":"input_tokens","value":N}} - even when the trigger fired,
 * at least this many input tokens are cleared), {@code clear_tool_inputs}
 * ({@code true} for all tools, or an array of tool names), and
 * {@code exclude_tools} (array of tool names never cleared).
 *
 * @see <a href="https://docs.claude.com/en/api/messages">Anthropic Messages API</a>
 */
public final class OpenRouterClearToolUsesEdit implements OpenRouterContextManagementEdit {

    private final JSONObject trigger;
    private final JSONObject keep;
    private final JSONObject clearAtLeast;
    private final Boolean clearToolInputsAll;
    private final List<String> clearToolInputsNames;
    private final List<String> excludeTools;

    private OpenRouterClearToolUsesEdit(Builder builder) {
        this.trigger = builder.trigger;
        this.keep = builder.keep;
        this.clearAtLeast = builder.clearAtLeast;
        this.clearToolInputsAll = builder.clearToolInputsAll;
        this.clearToolInputsNames = builder.clearToolInputsNames == null
                ? null : List.copyOf(builder.clearToolInputsNames);
        this.excludeTools = builder.excludeTools == null
                ? null : List.copyOf(builder.excludeTools);
    }

    /**
     * The JSON field {@code trigger}, or {@code null} when unset - the
     * condition that starts clearing ({@code input_tokens} or
     * {@code tool_uses} variant).
     *
     * @return the trigger JSON, or {@code null}
     */
    public JSONObject trigger() {
        return trigger == null ? null : new JSONObject(trigger.toString());
    }

    /**
     * The JSON field {@code keep}, or {@code null} when unset - the number
     * of most recent tool uses kept in the conversation
     * ({@code {"type":"tool_uses","value":N}}).
     *
     * @return the keep JSON, or {@code null}
     */
    public JSONObject keep() {
        return keep == null ? null : new JSONObject(keep.toString());
    }

    /**
     * The JSON field {@code clear_at_least}, or {@code null} when unset - a
     * floor on the number of input tokens cleared even when the trigger
     * fired ({@code {"type":"input_tokens","value":N}}).
     *
     * @return the clear-at-least JSON, or {@code null}
     */
    public JSONObject clearAtLeast() {
        return clearAtLeast == null ? null : new JSONObject(clearAtLeast.toString());
    }

    /**
     * The JSON field {@code clear_tool_inputs} in boolean form, or
     * {@code null} when the array form or nothing was set.
     *
     * @return {@code true}/{@code false}, or {@code null}
     */
    public Boolean clearToolInputsAll() {
        return clearToolInputsAll;
    }

    /**
     * The JSON field {@code clear_tool_inputs} in array form (only the
     * listed tools' inputs are cleared), or {@code null} when the boolean
     * form or nothing was set.
     *
     * @return the tool names, or {@code null}
     */
    public List<String> clearToolInputsNames() {
        return clearToolInputsNames == null ? null : List.copyOf(clearToolInputsNames);
    }

    /**
     * The JSON field {@code exclude_tools}, or {@code null} when unset -
     * tool names whose uses are never cleared.
     *
     * @return the excluded tool names, or {@code null}
     */
    public List<String> excludeTools() {
        return excludeTools == null ? null : List.copyOf(excludeTools);
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", "clear_tool_uses_20250919");
        if (trigger != null) {
            json.put("trigger", new JSONObject(trigger.toString()));
        }
        if (keep != null) {
            json.put("keep", new JSONObject(keep.toString()));
        }
        if (clearAtLeast != null) {
            json.put("clear_at_least", new JSONObject(clearAtLeast.toString()));
        }
        if (clearToolInputsAll != null) {
            json.put("clear_tool_inputs", clearToolInputsAll);
        } else if (clearToolInputsNames != null && !clearToolInputsNames.isEmpty()) {
            JSONArray arr = new JSONArray();
            for (String name : clearToolInputsNames) {
                arr.put(name);
            }
            json.put("clear_tool_inputs", arr);
        }
        if (excludeTools != null && !excludeTools.isEmpty()) {
            JSONArray arr = new JSONArray();
            for (String name : excludeTools) {
                arr.put(name);
            }
            json.put("exclude_tools", arr);
        }
        return json;
    }

    /**
     * Starting point for building a {@link OpenRouterClearToolUsesEdit}.
     */
    public static final class Builder {

        private JSONObject trigger;
        private JSONObject keep;
        private JSONObject clearAtLeast;
        private Boolean clearToolInputsAll;
        private List<String> clearToolInputsNames;
        private List<String> excludeTools;

        private Builder() {
        }

        /**
         * Sets {@code trigger} to the {@code input_tokens} variant
         * ({@code {"type":"input_tokens","value":N}}): start clearing once a
         * conversation's input exceeds this many tokens.
         *
         * @param inputTokens the input-token threshold
         * @return this builder
         */
        public Builder triggerInputTokens(long inputTokens) {
            this.trigger = new JSONObject().put("type", "input_tokens").put("value", inputTokens);
            return this;
        }

        /**
         * Sets {@code trigger} to the {@code tool_uses} variant
         * ({@code {"type":"tool_uses","value":N}}): start clearing once the
         * conversation has this many tool uses.
         *
         * @param toolUses the tool-use count threshold
         * @return this builder
         */
        public Builder triggerToolUses(long toolUses) {
            this.trigger = new JSONObject().put("type", "tool_uses").put("value", toolUses);
            return this;
        }

        /**
         * Sets {@code keep} ({@code {"type":"tool_uses","value":N}}) - the
         * number of most recent tool uses kept in the conversation while
         * older ones are cleared.
         *
         * @param toolUses the number of recent tool uses to keep
         * @return this builder
         */
        public Builder keepLastToolUses(int toolUses) {
            this.keep = new JSONObject().put("type", "tool_uses").put("value", toolUses);
            return this;
        }

        /**
         * Sets {@code clear_at_least}
         * ({@code {"type":"input_tokens","value":N}}) - even when the
         * trigger fired, at least this many input tokens are cleared before
         * the conversation is sent.
         *
         * @param inputTokens the minimum number of input tokens to clear
         * @return this builder
         */
        public Builder clearAtLeastInputTokens(long inputTokens) {
            this.clearAtLeast = new JSONObject().put("type", "input_tokens").put("value", inputTokens);
            return this;
        }

        /**
         * Sets {@code clear_tool_inputs} to the boolean form: with
         * {@code true}, the inputs of all cleared tool calls are removed;
         * with {@code false}, none are. Calling this clears a previously set
         * {@link #clearToolInputs(String...)}.
         *
         * @param clearAll whether to clear all tool inputs
         * @return this builder
         */
        public Builder clearToolInputs(boolean clearAll) {
            this.clearToolInputsAll = clearAll;
            this.clearToolInputsNames = null;
            return this;
        }

        /**
         * Sets {@code clear_tool_inputs} to the array form: only the inputs
         * of the named tools are cleared. Calling this clears a previously
         * set {@link #clearToolInputs(boolean)}. Trap: a call as
         * {@code clearToolInputs(null)} is treated as a null {@code String[]}
         * (not as the boolean form) and fails with a
         * {@code NullPointerException} while iterating - pass a non-null
         * array or use the boolean form explicitly.
         *
         * @param toolNames the tool names whose inputs are cleared
         * @return this builder
         */
        public Builder clearToolInputs(String... toolNames) {
            List<String> names = new ArrayList<>();
            for (String name : toolNames) {
                if (name != null && !name.isEmpty()) {
                    names.add(name);
                }
            }
            this.clearToolInputsNames = names;
            this.clearToolInputsAll = null;
            return this;
        }

        /**
         * Sets {@code exclude_tools} - tool names whose uses are never
         * cleared from the conversation.
         *
         * @param toolNames the excluded tool names
         * @return this builder
         */
        public Builder excludeTools(String... toolNames) {
            List<String> names = new ArrayList<>();
            for (String name : toolNames) {
                if (name != null && !name.isEmpty()) {
                    names.add(name);
                }
            }
            this.excludeTools = names;
            return this;
        }

        /**
         * Builds the {@link OpenRouterClearToolUsesEdit} value type.
         *
         * @return the strategy entry
         */
        public OpenRouterClearToolUsesEdit build() {
            return new OpenRouterClearToolUsesEdit(this);
        }
    }

    /**
     * Creates a new, empty builder for the
     * {@code clear_tool_uses_20250919} strategy.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }
}
