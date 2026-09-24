package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Typed implementation of the OpenAI <em>custom (freeform) tool</em> accepted by
 * the Responses request's {@code tools} array - a tool executed by the client
 * that is <em>not</em> a JSON-schema function.
 * <p>
 * Emitted as {@code {"type": "custom", "name": ...}} plus the explicitly
 * configured {@code description}, {@code format} and {@code async} fields and
 * the verbatim escape-hatch options. Pass it to
 * {@code OpenRouterResponsesRequest.Builder#addTool(JSONObject)} via
 * {@link #toJson()}.
 * <p>
 * <strong>Responses-only.</strong> The published schema declares {@code CustomTool}
 * on the Responses {@code tools} array only - the chat-completions {@code tools}
 * union does not accept it. This class deliberately does not implement
 * {@link OpenRouterServerTool}, so it cannot be sent through the chat builder's
 * {@code serverTools(...)} by accident.
 * <p>
 * Traps: {@code async: true} lets the model keep working after calling this
 * tool instead of waiting for its output. The tool is still executed by the
 * client; return the result in a <em>later</em> request as a
 * {@code function_call_output} with the original {@code call_id} - not as a
 * per-turn tool reply. Async tools are only honored by providers whose
 * Responses API supports them; elsewhere the flag is ignored.
 *
 * @see <a href="https://openrouter.ai/docs/agent-sdk/call-model/async-tools">Async tools / client-executed tools</a>
 */
public final class OpenRouterCustomTool {

    /** The tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "custom";

    private final String name;
    private final String description;
    private final String formatType;
    private final String formatDefinition;
    private final String formatSyntax;
    private final Boolean async;
    private final Map<String, Object> extraOptions;

    private OpenRouterCustomTool(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.formatType = builder.formatType;
        this.formatDefinition = builder.formatDefinition;
        this.formatSyntax = builder.formatSyntax;
        this.async = builder.async;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for a custom tool with the given {@code name}.
     *
     * @param name the tool name the model calls (required, not blank)
     * @return a new builder
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Creates a custom tool with no description and no explicit {@code format}
     * (the API default is plain text, so the field is omitted).
     *
     * @param name the tool name the model calls
     * @return the custom tool
     */
    public static OpenRouterCustomTool text(String name) {
        return builder(name).build();
    }

    /**
     * Creates a custom tool with a description and no explicit {@code format}
     * (the API default is plain text, so the field is omitted).
     *
     * @param name the tool name the model calls
     * @param description what the tool does (may be {@code null})
     * @return the custom tool
     */
    public static OpenRouterCustomTool text(String name, String description) {
        return builder(name).description(description).build();
    }

    /**
     * Creates a custom tool whose output must match a grammar.
     *
     * @param name the tool name the model calls
     * @param definition the grammar definition (required, not blank)
     * @param syntax the grammar syntax - {@code "lark"} or {@code "regex"}
     *               (accepted verbatim, documented values only)
     * @return the custom tool
     */
    public static OpenRouterCustomTool grammar(String name, String definition, String syntax) {
        return builder(name).formatGrammar(definition, syntax).build();
    }

    /** Returns the tool name sent as {@code name}. */
    public String name() {
        return name;
    }

    /** Returns the configured {@code description}, or {@code null} when unset. */
    public String description() {
        return description;
    }

    /** Returns the configured {@code format.type} ({@code "text"} or {@code "grammar"}), or {@code null} when unset. */
    public String formatType() {
        return formatType;
    }

    /** Returns the configured {@code format.definition}, or {@code null} when unset. */
    public String formatDefinition() {
        return formatDefinition;
    }

    /** Returns the configured {@code format.syntax}, or {@code null} when unset. */
    public String formatSyntax() {
        return formatSyntax;
    }

    /** Returns the configured {@code async} flag, or {@code null} when unset. */
    public Boolean async() {
        return async;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "custom", "name": ...}} plus the explicitly configured
     * {@code description}, {@code format} and {@code async} fields and the
     * verbatim escape-hatch options.
     */
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);
        tool.put("name", name);
        if (description != null) {
            tool.put("description", description);
        }
        if (formatType != null) {
            JSONObject format = new JSONObject();
            format.put("type", formatType);
            if (formatDefinition != null) {
                format.put("definition", formatDefinition);
            }
            if (formatSyntax != null) {
                format.put("syntax", formatSyntax);
            }
            tool.put("format", format);
        }
        if (async != null) {
            tool.put("async", async);
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            tool.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return tool;
    }

    /**
     * Builder for {@link OpenRouterCustomTool}. Only explicitly configured
     * fields are emitted; a verbatim {@link #option(String, Object)} escape
     * hatch covers keys this library does not type.
     */
    public static final class Builder {

        private final String name;
        private String description;
        private String formatType;
        private String formatDefinition;
        private String formatSyntax;
        private Boolean async;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder(String name) {
            this.name = Objects.requireNonNull(name, "name must not be null");
            if (name.isBlank()) {
                throw new IllegalArgumentException("name must not be blank");
            }
        }

        /**
         * Sets {@code description}: what the tool does. Emitted only when set.
         *
         * @param description the tool description (may be {@code null} to unset)
         * @return this builder
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets {@code format} to the plain-text form {@code {"type":"text"}} -
         * the API default when {@code format} is omitted. Calling this replaces
         * any previously set format form.
         *
         * @return this builder
         */
        public Builder formatText() {
            this.formatType = "text";
            this.formatDefinition = null;
            this.formatSyntax = null;
            return this;
        }

        /**
         * Sets {@code format} to the grammar form
         * {@code {"type":"grammar","definition":...,"syntax":...}}. Calling this
         * replaces any previously set format form.
         *
         * @param definition the grammar definition (required, not blank)
         * @param syntax the grammar syntax - {@code "lark"} or {@code "regex"}
         *               (accepted verbatim, documented values only)
         * @return this builder
         */
        public Builder formatGrammar(String definition, String syntax) {
            Objects.requireNonNull(definition, "definition must not be null");
            Objects.requireNonNull(syntax, "syntax must not be null");
            if (definition.isBlank()) {
                throw new IllegalArgumentException("definition must not be blank");
            }
            if (syntax.isBlank()) {
                throw new IllegalArgumentException("syntax must not be blank");
            }
            this.formatType = "grammar";
            this.formatDefinition = definition;
            this.formatSyntax = syntax;
            return this;
        }

        /**
         * Sets {@code async}: lets the model keep working after calling this
         * tool instead of waiting for its output. {@code false} is a set option
         * and is emitted.
         * <p>
         * Trap: only honored by providers whose Responses API supports async
         * tools; ignored elsewhere. The result must come back as a
         * {@code function_call_output} with the original {@code call_id} in a
         * later request.
         *
         * @param async whether the tool call is asynchronous
         * @return this builder
         */
        public Builder async(Boolean async) {
            this.async = async;
            return this;
        }

        /**
         * Adds a top-level field verbatim - escape hatch for keys this library
         * does not type. Null values are emitted as JSON {@code null}.
         *
         * @param key the field name (must not be {@code "type"} or {@code "name"})
         * @param value the field value (String, Number, Boolean, org.json types or null)
         * @return this builder
         */
        public Builder option(String key, Object value) {
            if ("type".equals(key) || "name".equals(key)) {
                throw new IllegalArgumentException(
                        "The '" + key + "' field is set from the constructor/typed API and must not be set via option()");
            }
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the custom tool.
         *
         * @return the custom tool
         */
        public OpenRouterCustomTool build() {
            return new OpenRouterCustomTool(this);
        }
    }
}
