package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

/**
 * The {@code clear_thinking_20251015} context-management strategy: clears
 * older thinking blocks from the conversation so long agentic Messages
 * conversations do not carry stale reasoning forward. The only field is
 * {@code keep} - how many recent thinking turns survive.
 * <p>
 * JSON fields: {@code type} (fixed) and {@code keep} - either
 * {@code {"type":"thinking_turns","value":N}} (keep the last N thinking
 * turns) or {@code {"type":"all"}} (the API schema also accepts the bare
 * string {@code "all"}; the library emits the object form). An unset
 * {@code keep} is omitted from the JSON.
 *
 * @see <a href="https://docs.claude.com/en/api/messages">Anthropic Messages API</a>
 */
public final class OpenRouterClearThinkingEdit implements OpenRouterContextManagementEdit {

    private final JSONObject keep;

    private OpenRouterClearThinkingEdit(Builder builder) {
        this.keep = builder.keep;
    }

    /**
     * The JSON field {@code keep}, or {@code null} when unset - either
     * {@code {"type":"thinking_turns","value":N}} or {@code {"type":"all"}}.
     *
     * @return the keep JSON, or {@code null}
     */
    public JSONObject keep() {
        return keep == null ? null : new JSONObject(keep.toString());
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", "clear_thinking_20251015");
        if (keep != null) {
            json.put("keep", new JSONObject(keep.toString()));
        }
        return json;
    }

    /**
     * Starting point for building a {@link OpenRouterClearThinkingEdit}.
     */
    public static final class Builder {

        private JSONObject keep;

        private Builder() {
        }

        /**
         * Sets {@code keep} to {@code {"type":"thinking_turns","value":N}} -
         * keep the last N thinking turns in the conversation.
         *
         * @param turns the number of recent thinking turns to keep
         * @return this builder
         */
        public Builder keepLastTurns(int turns) {
            this.keep = new JSONObject().put("type", "thinking_turns").put("value", turns);
            return this;
        }

        /**
         * Sets {@code keep} to {@code {"type":"all"}} - keep all thinking
         * turns (no thinking content is cleared). Trap: the API schema also
         * accepts the bare string {@code "all"} for this field; the library
         * deliberately emits the object form.
         *
         * @return this builder
         */
        public Builder keepAll() {
            this.keep = new JSONObject().put("type", "all");
            return this;
        }

        /**
         * Builds the {@link OpenRouterClearThinkingEdit} value type.
         *
         * @return the strategy entry
         */
        public OpenRouterClearThinkingEdit build() {
            return new OpenRouterClearThinkingEdit(this);
        }
    }

    /**
     * Creates a new, empty builder for the {@code clear_thinking_20251015}
     * strategy.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }
}
