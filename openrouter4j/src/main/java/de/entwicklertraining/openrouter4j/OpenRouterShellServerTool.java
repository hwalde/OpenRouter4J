package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Typed implementation of the {@code openrouter:shell} server tool: runs shell
 * commands server-side in a sandboxed container (a sandbox-backed clone of
 * OpenAI's hosted shell tool). Emitted into the {@code tools} request array as
 * {@code {"type": "openrouter:shell"}} plus a {@code parameters} object
 * holding only the explicitly configured fields.
 * <p>
 * Traps: the {@code engine} default {@code "auto"} keeps the provider's native
 * hosted shell when available (OpenAI); on other providers the call is routed
 * to the OpenRouter sandbox - set {@code "openrouter"} to force the sandbox. A
 * {@code container_reference} environment (with a {@code container_id}
 * previously returned by a bash or shell tool result) reattaches to the same
 * container and files; using the same container_id from both the bash and the
 * shell tool shares the same files (last-write-wins on concurrent flushes).
 *
 * @see OpenRouterBashServerTool
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools/shell">Shell server tool</a>
 */
public final class OpenRouterShellServerTool implements OpenRouterServerTool {

    /** The server-tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "openrouter:shell";

    private final String engine;
    private final String environmentType;
    private final String environmentContainerId;
    private final List<String> environmentFileIds;
    private final Map<String, Object> extraOptions;

    private OpenRouterShellServerTool(Builder builder) {
        this.engine = builder.engine;
        this.environmentType = builder.environmentType;
        this.environmentContainerId = builder.environmentContainerId;
        this.environmentFileIds = builder.environmentFileIds == null ? null : List.copyOf(builder.environmentFileIds);
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code openrouter:shell} server tool.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the server-tool discriminator {@code "openrouter:shell"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /**
     * Returns the configured {@code parameters.engine} value
     * ({@code "auto"} default keeps the provider's native hosted shell when
     * available; {@code "openrouter"} forces the OpenRouter sandbox), or
     * {@code null} when unset.
     */
    public String engine() {
        return engine;
    }

    /**
     * Returns the configured environment type ({@code "container_auto"} or
     * {@code "container_reference"}), or {@code null} when unset.
     */
    public String environmentType() {
        return environmentType;
    }

    /**
     * Returns the configured {@code environment.container_id}, or {@code null}
     * when unset.
     */
    public String environmentContainerId() {
        return environmentContainerId;
    }

    /**
     * Returns the configured {@code environment.file_ids} (workspace file ids
     * attached into the container before the first command runs), empty when
     * unset (never {@code null}).
     */
    public List<String> environmentFileIds() {
        return environmentFileIds == null ? List.of() : environmentFileIds;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "openrouter:shell"}} plus a {@code parameters} object
     * when at least one option is set.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);

        JSONObject parameters = new JSONObject();
        if (engine != null) {
            parameters.put("engine", engine);
        }
        if (environmentType != null || environmentContainerId != null
                || (environmentFileIds != null && !environmentFileIds.isEmpty())) {
            JSONObject environment = new JSONObject();
            if (environmentContainerId != null) {
                environment.put("type", environmentType == null ? "container_reference" : environmentType);
            } else {
                environment.put("type", environmentType == null ? "container_auto" : environmentType);
            }
            if (environmentContainerId != null) {
                environment.put("container_id", environmentContainerId);
            }
            if (environmentFileIds != null && !environmentFileIds.isEmpty()) {
                environment.put("file_ids", new JSONArray(environmentFileIds));
            }
            parameters.put("environment", environment);
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
     * Builder for the {@code openrouter:shell} server tool. Only explicitly
     * configured fields are emitted; a verbatim {@link #option(String, Object)}
     * escape hatch covers configuration keys this library does not know yet.
     */
    public static final class Builder {

        private String engine;
        private String environmentType;
        private String environmentContainerId;
        private List<String> environmentFileIds;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code parameters.engine}: {@code "auto"} (default) keeps the
         * provider's native hosted shell when available (OpenAI) and routes to
         * the OpenRouter sandbox on other providers; {@code "openrouter"} runs
         * commands in the OpenRouter sandbox.
         */
        public Builder engine(String engine) {
            this.engine = engine;
            return this;
        }

        /**
         * Sets {@code parameters.environment.type}: {@code "container_auto"}
         * for an OpenRouter-managed ephemeral container (default) or
         * {@code "container_reference"} to attach to a container id.
         */
        public Builder environmentType(String type) {
            this.environmentType = type;
            return this;
        }

        /**
         * Sets {@code parameters.environment.container_id}: a container id
         * previously returned by a bash or shell tool result (reattaches to
         * the same container and files), or a fresh name to create a new
         * persistent container. Implies {@code environment.type =
         * "container_reference"}.
         */
        public Builder environmentContainerId(String containerId) {
            this.environmentContainerId = containerId;
            return this;
        }

        /**
         * Sets {@code parameters.environment.file_ids}: workspace file ids
         * (or_file_...) attached into the container before the first command
         * runs (each file arrives as a writable copy in the container home).
         */
        public Builder environmentFileIds(List<String> fileIds) {
            this.environmentFileIds = fileIds;
            return this;
        }

        /**
         * Adds a {@code parameters} entry verbatim - escape hatch for
         * configuration keys this library does not know yet (e.g. a network
         * policy object). Null values are emitted as JSON {@code null}.
         */
        public Builder option(String key, Object value) {
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the {@link OpenRouterShellServerTool}.
         */
        public OpenRouterShellServerTool build() {
            return new OpenRouterShellServerTool(this);
        }
    }
}
