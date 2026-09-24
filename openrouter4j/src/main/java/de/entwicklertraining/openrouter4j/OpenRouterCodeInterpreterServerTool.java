package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Typed implementation of the OpenAI {@code code_interpreter} server tool:
 * runs model-generated code in a sandboxed container.
 * <p>
 * Emitted as {@code {"type": "code_interpreter", "container": ...}} plus the
 * verbatim escape-hatch options - flat fields at the top level of the tool
 * object (this is an OpenAI-native tool type, not an {@code openrouter:*}
 * namespace tool with a {@code parameters} wrapper).
 * <p>
 * {@code container} is required by the schema and comes in two wire forms:
 * a plain string, or the {@code {"type":"auto"}} object with optional
 * {@code file_ids} and {@code memory_limit}. Use
 * {@link Builder#container(String)} for the string form and
 * {@link Builder#containerAuto(List, String)} (and its overloads) for the
 * object form. A request without a container is rejected loudly at
 * {@code build()}.
 * <p>
 * <strong>Schema surface:</strong> the published schema declares
 * {@code CodeInterpreterServerTool} on the Responses request's {@code tools}
 * array (and on the mid-input {@code additional_tools} item). The
 * chat-completions {@code tools} union does not list it - sending it through
 * {@code serverTools(...)} is an escape-hatch use at the caller's risk.
 * <p>
 * Trap: files the code writes into the container can be read back through
 * {@code client.containers()} (list, download, promote into durable workspace
 * storage).
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/containers">Containers</a>
 */
public final class OpenRouterCodeInterpreterServerTool implements OpenRouterServerTool {

    /** The tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "code_interpreter";

    private final Object container;
    private final Map<String, Object> extraOptions;

    private OpenRouterCodeInterpreterServerTool(Builder builder) {
        this.container = builder.container;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code code_interpreter} tool.
     * {@code container} is required - set it before {@code build()}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the tool discriminator {@code "code_interpreter"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /**
     * Returns the configured {@code container} value (either a
     * {@code String} or a {@code JSONObject} of the {@code auto} object form),
     * or {@code null} when unset.
     */
    public Object container() {
        return container;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "code_interpreter", "container": ...}} plus the verbatim
     * escape-hatch options.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);
        tool.put("container", container);
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            tool.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return tool;
    }

    /**
     * Builder for {@link OpenRouterCodeInterpreterServerTool}. Only explicitly
     * configured fields are emitted (plus the required {@code type} and
     * {@code container}); a verbatim {@link #option(String, Object)} escape
     * hatch covers keys this library does not type.
     */
    public static final class Builder {

        private Object container;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code container} to the plain string form (for example a
         * container id or {@code "auto"} as a bare string - the schema accepts
         * any string). Replaces any previously set container form.
         *
         * @param container the container reference string (required, not blank)
         * @return this builder
         */
        public Builder container(String container) {
            Objects.requireNonNull(container, "container must not be null");
            if (container.isBlank()) {
                throw new IllegalArgumentException("container must not be blank");
            }
            this.container = container;
            return this;
        }

        /**
         * Sets {@code container} to the {@code {"type":"auto"}} object form
         * with no file ids and the default memory limit. Replaces any
         * previously set container form.
         *
         * @return this builder
         */
        public Builder containerAuto() {
            return containerAuto(null, null);
        }

        /**
         * Sets {@code container} to the {@code {"type":"auto"}} object form
         * with the given workspace file ids attached before the first run.
         * Replaces any previously set container form.
         *
         * @param fileIds workspace file ids to attach (may be {@code null} or empty)
         * @return this builder
         */
        public Builder containerAuto(List<String> fileIds) {
            return containerAuto(fileIds, null);
        }

        /**
         * Sets {@code container} to the {@code {"type":"auto"}} object form.
         * Replaces any previously set container form.
         * <p>
         * {@code memoryLimit} is validated against the documented enum
         * {@code 1g}, {@code 4g}, {@code 16g}, {@code 64g}; {@code null} emits
         * JSON {@code null} (the documented "no explicit limit" value).
         *
         * @param fileIds workspace file ids to attach (may be {@code null} or empty)
         * @param memoryLimit {@code "1g"}, {@code "4g"}, {@code "16g"},
         *                    {@code "64g"} or {@code null}
         * @return this builder
         */
        public Builder containerAuto(List<String> fileIds, String memoryLimit) {
            if (memoryLimit != null
                    && !memoryLimit.equals("1g")
                    && !memoryLimit.equals("4g")
                    && !memoryLimit.equals("16g")
                    && !memoryLimit.equals("64g")) {
                throw new IllegalArgumentException(
                        "memoryLimit must be one of \"1g\", \"4g\", \"16g\", \"64g\" or null, was: " + memoryLimit);
            }
            JSONObject auto = new JSONObject();
            auto.put("type", "auto");
            if (fileIds != null && !fileIds.isEmpty()) {
                JSONArray arr = new JSONArray();
                fileIds.forEach(arr::put);
                auto.put("file_ids", arr);
            }
            if (memoryLimit != null) {
                auto.put("memory_limit", memoryLimit);
            }
            this.container = auto;
            return this;
        }

        /**
         * Sets {@code container} to a verbatim object - escape hatch for a
         * container shape this library does not type. Replaces any previously
         * set container form.
         *
         * @param container the container object (required, not {@code null})
         * @return this builder
         */
        public Builder container(JSONObject container) {
            this.container = Objects.requireNonNull(container, "container must not be null");
            return this;
        }

        /**
         * Adds a top-level field verbatim - escape hatch for keys this library
         * does not type. Null values are emitted as JSON {@code null}.
         *
         * @param key the field name (must not be {@code "type"} or {@code "container"})
         * @param value the field value (String, Number, Boolean, org.json types or null)
         * @return this builder
         */
        public Builder option(String key, Object value) {
            if ("type".equals(key) || "container".equals(key)) {
                throw new IllegalArgumentException(
                        "The '" + key + "' field is set from the typed API and must not be set via option()");
            }
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the code interpreter tool. Rejects a tool without a container
         * loudly - the schema requires it.
         *
         * @return the code interpreter tool
         */
        public OpenRouterCodeInterpreterServerTool build() {
            if (container == null) {
                throw new IllegalStateException(
                        "container is required - set container(String), containerAuto(...) or container(JSONObject)");
            }
            return new OpenRouterCodeInterpreterServerTool(this);
        }
    }
}
