package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed implementation of the {@code openrouter:advisor} server tool: consults
 * a higher-intelligence advisor model mid-generation. Emitted into the
 * {@code tools} request array as {@code {"type": "openrouter:advisor"}} plus a
 * {@code parameters} object holding only the explicitly configured fields.
 * <p>
 * Multiple advisor entries are allowed - one per named instance; the model
 * then sees one tool per named advisor (plus one default for an unnamed
 * entry). Names must be unique across advisor entries.
 * <p>
 * Traps: {@code stream(true)} has no effect on the Chat Completions API (the
 * advice arrives only as the final tool result); when
 * {@code forward_transcript(true)} is set, the full parent conversation is
 * forwarded to the advisor - mind the token cost.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools/advisor">Advisor server tool</a>
 */
public final class OpenRouterAdvisorServerTool implements OpenRouterServerTool {

    /** The server-tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "openrouter:advisor";

    private final String model;
    private final String name;
    private final String instructions;
    private final Integer maxCompletionTokens;
    private final Boolean forwardTranscript;
    private final String reasoningEffort;
    private final Integer reasoningMaxTokens;
    private final Boolean stream;
    private final Double temperature;
    private final Map<String, Object> extraOptions;

    private OpenRouterAdvisorServerTool(Builder builder) {
        this.model = builder.model;
        this.name = builder.name;
        this.instructions = builder.instructions;
        this.maxCompletionTokens = builder.maxCompletionTokens;
        this.forwardTranscript = builder.forwardTranscript;
        this.reasoningEffort = builder.reasoningEffort;
        this.reasoningMaxTokens = builder.reasoningMaxTokens;
        this.stream = builder.stream;
        this.temperature = builder.temperature;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code openrouter:advisor} server tool.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the server-tool discriminator {@code "openrouter:advisor"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /**
     * Returns the configured {@code parameters.model} slug (any OpenRouter
     * model; when omitted, the executor can choose it via the tool call's
     * {@code model} argument, falling back to the outer request's model), or
     * {@code null} when unset.
     */
    public String model() {
        return model;
    }

    /**
     * Returns the configured {@code parameters.name} (the name the model sees
     * for this advisor; unique across advisor entries), or {@code null} when
     * unset.
     */
    public String name() {
        return name;
    }

    /**
     * Returns the configured {@code parameters.instructions} (system
     * instructions for the advisor sub-agent), or {@code null} when unset.
     */
    public String instructions() {
        return instructions;
    }

    /**
     * Returns the configured {@code parameters.max_completion_tokens} value
     * (output tokens including reasoning), or {@code null} when unset.
     */
    public Integer maxCompletionTokens() {
        return maxCompletionTokens;
    }

    /**
     * Returns the configured {@code parameters.forward_transcript} value: when
     * {@code true}, the full parent conversation is forwarded to the advisor
     * so it sees the same context the executor does; or {@code null} when
     * unset (the advisor then receives only the tool call's {@code prompt}).
     */
    public Boolean forwardTranscript() {
        return forwardTranscript;
    }

    /**
     * Returns the configured {@code parameters.reasoning.effort} value
     * ({@code max}, {@code xhigh}, {@code high}, {@code medium}, {@code low},
     * {@code minimal}, {@code none}), or {@code null} when unset.
     */
    public String reasoningEffort() {
        return reasoningEffort;
    }

    /**
     * Returns the configured {@code parameters.reasoning.max_tokens} value
     * (reasoning budget for the advisor), or {@code null} when unset.
     */
    public Integer reasoningMaxTokens() {
        return reasoningMaxTokens;
    }

    /**
     * Returns the configured {@code parameters.stream} value, or {@code null}
     * when unset. Trap: has no effect on the Chat Completions API.
     */
    public Boolean stream() {
        return stream;
    }

    /**
     * Returns the configured {@code parameters.temperature} value forwarded to
     * the advisor call, or {@code null} when unset.
     */
    public Double temperature() {
        return temperature;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "openrouter:advisor"}} plus a {@code parameters} object
     * when at least one option is set. The {@code parameters} object carries
     * only explicitly configured fields, followed by the verbatim escape-hatch
     * options.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);

        JSONObject parameters = new JSONObject();
        if (model != null) {
            parameters.put("model", model);
        }
        if (name != null) {
            parameters.put("name", name);
        }
        if (instructions != null) {
            parameters.put("instructions", instructions);
        }
        if (maxCompletionTokens != null) {
            parameters.put("max_completion_tokens", maxCompletionTokens);
        }
        if (forwardTranscript != null) {
            parameters.put("forward_transcript", forwardTranscript);
        }
        if (reasoningEffort != null || reasoningMaxTokens != null) {
            JSONObject reasoning = new JSONObject();
            if (reasoningEffort != null) {
                reasoning.put("effort", reasoningEffort);
            }
            if (reasoningMaxTokens != null) {
                reasoning.put("max_tokens", reasoningMaxTokens);
            }
            parameters.put("reasoning", reasoning);
        }
        if (stream != null) {
            parameters.put("stream", stream);
        }
        if (temperature != null) {
            parameters.put("temperature", temperature);
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            parameters.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }

        if (!parameters.isEmpty()) {
            tool.put("parameters", parameters);
        }
        return tool;
    }

    /**
     * Builder for the {@code openrouter:advisor} server tool. Only explicitly
     * configured fields are emitted; a verbatim {@link #option(String, Object)}
     * escape hatch covers configuration keys this library does not know yet.
     */
    public static final class Builder {

        private String model;
        private String name;
        private String instructions;
        private Integer maxCompletionTokens;
        private Boolean forwardTranscript;
        private String reasoningEffort;
        private Integer reasoningMaxTokens;
        private Boolean stream;
        private Double temperature;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code parameters.model}: the advisor model slug (any
         * OpenRouter model). When omitted, the executor can choose the model
         * via the tool call's {@code model} argument; if neither is set, the
         * outer request's model is used.
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets {@code parameters.name}: the advisor's name. The model sees one
         * tool per named advisor (and one default for an unnamed entry); names
         * must be unique across advisor entries (letters, digits, spaces,
         * underscores, dashes; 1-64 chars).
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets {@code parameters.instructions}: system instructions for the
         * advisor sub-agent. When omitted, the advisor responds with no system
         * prompt of its own.
         */
        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        /**
         * Sets {@code parameters.max_completion_tokens}: maximum output tokens
         * (including reasoning) the advisor may produce.
         */
        public Builder maxCompletionTokens(Integer maxCompletionTokens) {
            this.maxCompletionTokens = maxCompletionTokens;
            return this;
        }

        /**
         * Sets {@code parameters.forward_transcript}: when {@code true}, the
         * full parent conversation is forwarded to the advisor so it sees the
         * same context the executor does (the tool call's {@code prompt} is
         * appended as a final user turn); when {@code false} or unset, the
         * advisor receives only the tool call's {@code prompt}.
         */
        public Builder forwardTranscript(Boolean forwardTranscript) {
            this.forwardTranscript = forwardTranscript;
            return this;
        }

        /**
         * Sets {@code parameters.reasoning.effort}: reasoning effort level for
         * the advisor call ({@code max}, {@code xhigh}, {@code high},
         * {@code medium}, {@code low}, {@code minimal}, {@code none}).
         */
        public Builder reasoningEffort(String effort) {
            this.reasoningEffort = effort;
            return this;
        }

        /**
         * Sets {@code parameters.reasoning.max_tokens}: maximum reasoning
         * tokens the advisor may use.
         */
        public Builder reasoningMaxTokens(Integer maxTokens) {
            this.reasoningMaxTokens = maxTokens;
            return this;
        }

        /**
         * Sets {@code parameters.stream}: when {@code true}, the advisor's
         * advice streams incrementally as it is produced. Trap: has no effect
         * on the Chat Completions API (the advice arrives only as the final
         * tool result).
         */
        public Builder stream(Boolean stream) {
            this.stream = stream;
            return this;
        }

        /**
         * Sets {@code parameters.temperature}: sampling temperature forwarded
         * to the advisor call.
         */
        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        /**
         * Adds a {@code parameters} entry verbatim - escape hatch for
         * configuration keys this library does not know yet. Null values are
         * emitted as JSON {@code null}.
         */
        public Builder option(String key, Object value) {
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the {@link OpenRouterAdvisorServerTool}.
         */
        public OpenRouterAdvisorServerTool build() {
            return new OpenRouterAdvisorServerTool(this);
        }
    }
}
