package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

/**
 * The {@code compact_20260112} context-management strategy: server-side
 * compaction of the Messages conversation once the trigger fires - the
 * server summarizes and shrinks the context instead of the request failing
 * on an exhausted context window.
 * <p>
 * JSON fields: {@code type} (fixed), {@code instructions} (optional string
 * guiding what the compaction summary preserves), {@code pause_after_compaction}
 * (optional boolean) and {@code trigger} (the {@code input_tokens} variant,
 * {@code {"type":"input_tokens","value":N}} - the only trigger variant this
 * strategy accepts). An unset field is omitted from the JSON.
 *
 * @see <a href="https://docs.claude.com/en/api/messages">Anthropic Messages API</a>
 */
public final class OpenRouterCompactEdit implements OpenRouterContextManagementEdit {

    private final String instructions;
    private final Boolean pauseAfterCompaction;
    private final JSONObject trigger;

    private OpenRouterCompactEdit(Builder builder) {
        this.instructions = builder.instructions;
        this.pauseAfterCompaction = builder.pauseAfterCompaction;
        this.trigger = builder.trigger;
    }

    /**
     * The JSON field {@code instructions}, or {@code null} when unset -
     * guidance for what the compaction summary preserves.
     *
     * @return the instructions, or {@code null}
     */
    public String instructions() {
        return instructions;
    }

    /**
     * The JSON field {@code pause_after_compaction}, or {@code null} when
     * unset.
     *
     * @return the pause flag, or {@code null}
     */
    public Boolean pauseAfterCompaction() {
        return pauseAfterCompaction;
    }

    /**
     * The JSON field {@code trigger}, or {@code null} when unset - always
     * the {@code input_tokens} variant
     * ({@code {"type":"input_tokens","value":N}}).
     *
     * @return the trigger JSON, or {@code null}
     */
    public JSONObject trigger() {
        return trigger == null ? null : new JSONObject(trigger.toString());
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", "compact_20260112");
        if (instructions != null) {
            json.put("instructions", instructions);
        }
        if (pauseAfterCompaction != null) {
            json.put("pause_after_compaction", pauseAfterCompaction);
        }
        if (trigger != null) {
            json.put("trigger", new JSONObject(trigger.toString()));
        }
        return json;
    }

    /**
     * Starting point for building a {@link OpenRouterCompactEdit}.
     */
    public static final class Builder {

        private String instructions;
        private Boolean pauseAfterCompaction;
        private JSONObject trigger;

        private Builder() {
        }

        /**
         * Sets {@code instructions} - guidance for what the compaction
         * summary preserves.
         *
         * @param instructions the compaction instructions
         * @return this builder
         */
        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        /**
         * Sets {@code pause_after_compaction} - whether the agent pauses
         * after a compaction instead of continuing immediately.
         *
         * @param pause whether to pause after compaction
         * @return this builder
         */
        public Builder pauseAfterCompaction(boolean pause) {
            this.pauseAfterCompaction = pause;
            return this;
        }

        /**
         * Sets {@code trigger} to the {@code input_tokens} variant
         * ({@code {"type":"input_tokens","value":N}}): compact once the
         * conversation's input exceeds this many tokens. This is the only
         * trigger variant the {@code compact_20260112} strategy accepts.
         *
         * @param inputTokens the input-token threshold
         * @return this builder
         */
        public Builder triggerInputTokens(long inputTokens) {
            this.trigger = new JSONObject().put("type", "input_tokens").put("value", inputTokens);
            return this;
        }

        /**
         * Builds the {@link OpenRouterCompactEdit} value type.
         *
         * @return the strategy entry
         */
        public OpenRouterCompactEdit build() {
            return new OpenRouterCompactEdit(this);
        }
    }

    /**
     * Creates a new, empty builder for the {@code compact_20260112}
     * strategy.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }
}
