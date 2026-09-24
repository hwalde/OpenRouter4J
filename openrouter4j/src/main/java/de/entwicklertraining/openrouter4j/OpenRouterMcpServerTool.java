package de.entwicklertraining.openrouter4j;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Typed implementation of the Model Context Protocol (MCP) server tool
 * ({@code type: "mcp"}) - a third-party tool server the model can call through
 * the {@code tools} request array.
 * <p>
 * Emitted as {@code {"type": "mcp", "server_label": ...}} plus the explicitly
 * configured {@code server_url}, {@code connector_id}, {@code authorization},
 * {@code allowed_tools}, {@code headers}, {@code require_approval} and
 * {@code server_description} fields and the verbatim escape-hatch options -
 * flat fields at the top level of the tool object, not a {@code parameters}
 * wrapper (this is not an {@code openrouter:*} namespace tool).
 * <p>
 * <strong>Schema surface:</strong> the published schema declares
 * {@code McpServerTool} on the Responses request's {@code tools} array (and on
 * the mid-input {@code additional_tools} item). The chat-completions
 * {@code tools} union does not list it - sending it through
 * {@code serverTools(...)} is an escape-hatch use at the caller's risk. It is
 * usable on both because it implements {@link OpenRouterServerTool} and
 * therefore plugs into the existing {@code addTool(toJson())} /
 * {@code serverTools(...)} plumbing.
 * <p>
 * Traps: {@code server_label} is required and names the server for the model;
 * {@code server_url} and {@code connector_id} are the two alternative ways to
 * locate it (a connector id selects a pre-integrated provider such as
 * {@code connector_dropbox}). {@code allowed_tools} narrows which of the
 * server's tools the model may call. {@code require_approval} controls whether
 * tool calls need a human approval step.
 *
 * @see <a href="https://openrouter.ai/docs/guides/overview/mcp-server">MCP server tools</a>
 */
public final class OpenRouterMcpServerTool implements OpenRouterServerTool {

    /** The tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "mcp";

    private final String serverLabel;
    private final String serverUrl;
    private final String connectorId;
    private final String authorization;
    private final List<String> allowedToolNames;
    private final Boolean allowedToolsReadOnly;
    private final List<String> allowedToolNamesObjectForm;
    private final Map<String, String> headers;
    private final Object requireApproval;
    private final String serverDescription;
    private final Map<String, Object> extraOptions;

    private OpenRouterMcpServerTool(Builder builder) {
        this.serverLabel = builder.serverLabel;
        this.serverUrl = builder.serverUrl;
        this.connectorId = builder.connectorId;
        this.authorization = builder.authorization;
        this.allowedToolNames = builder.allowedToolNames == null ? null : List.copyOf(builder.allowedToolNames);
        this.allowedToolsReadOnly = builder.allowedToolsReadOnly;
        this.allowedToolNamesObjectForm = builder.allowedToolNamesObjectForm == null
                ? null : List.copyOf(builder.allowedToolNamesObjectForm);
        this.headers = builder.headers == null ? null : Map.copyOf(builder.headers);
        this.requireApproval = builder.requireApproval;
        this.serverDescription = builder.serverDescription;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the MCP server tool.
     *
     * @param serverLabel the required label naming this server for the model
     *                    (must not be blank)
     * @return a new builder
     */
    public static Builder builder(String serverLabel) {
        return new Builder(serverLabel);
    }

    /** Returns the tool discriminator {@code "mcp"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /** Returns the required {@code server_label}. */
    public String serverLabel() {
        return serverLabel;
    }

    /** Returns the configured {@code server_url}, or {@code null} when unset. */
    public String serverUrl() {
        return serverUrl;
    }

    /** Returns the configured {@code connector_id}, or {@code null} when unset. */
    public String connectorId() {
        return connectorId;
    }

    /** Returns the configured {@code authorization} token, or {@code null} when unset. */
    public String authorization() {
        return authorization;
    }

    /**
     * Returns the configured {@code allowed_tools} in its array-of-names form,
     * empty when unset (never {@code null}).
     */
    public List<String> allowedToolNames() {
        return allowedToolNames == null ? List.of() : allowedToolNames;
    }

    /**
     * Returns the configured {@code allowed_tools.read_only} of the object form,
     * or {@code null} when unset.
     */
    public Boolean allowedToolsReadOnly() {
        return allowedToolsReadOnly;
    }

    /**
     * Returns the configured {@code allowed_tools.tool_names} of the object form,
     * empty when unset (never {@code null}).
     */
    public List<String> allowedToolNamesObjectForm() {
        return allowedToolNamesObjectForm == null ? List.of() : allowedToolNamesObjectForm;
    }

    /** Returns the configured {@code headers}, empty when unset (never {@code null}). */
    public Map<String, String> headers() {
        return headers == null ? Map.of() : headers;
    }

    /**
     * Returns the configured {@code require_approval} value (either the bare
     * string {@code "always"}/{@code "never"} or the object form), or
     * {@code null} when unset.
     */
    public Object requireApproval() {
        return requireApproval;
    }

    /** Returns the configured {@code server_description}, or {@code null} when unset. */
    public String serverDescription() {
        return serverDescription;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "mcp", "server_label": ...}} plus the explicitly
     * configured fields at the top level, followed by the verbatim
     * escape-hatch options.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);
        tool.put("server_label", serverLabel);
        if (serverUrl != null) {
            tool.put("server_url", serverUrl);
        }
        if (connectorId != null) {
            tool.put("connector_id", connectorId);
        }
        if (authorization != null) {
            tool.put("authorization", authorization);
        }
        if (allowedToolNames != null && !allowedToolNames.isEmpty()) {
            JSONArray arr = new JSONArray();
            allowedToolNames.forEach(arr::put);
            tool.put("allowed_tools", arr);
        } else if (allowedToolsReadOnly != null
                || (allowedToolNamesObjectForm != null && !allowedToolNamesObjectForm.isEmpty())) {
            JSONObject allowed = new JSONObject();
            if (allowedToolsReadOnly != null) {
                allowed.put("read_only", allowedToolsReadOnly);
            }
            if (allowedToolNamesObjectForm != null && !allowedToolNamesObjectForm.isEmpty()) {
                JSONArray arr = new JSONArray();
                allowedToolNamesObjectForm.forEach(arr::put);
                allowed.put("tool_names", arr);
            }
            tool.put("allowed_tools", allowed);
        }
        if (headers != null && !headers.isEmpty()) {
            JSONObject headersJson = new JSONObject();
            headers.forEach(headersJson::put);
            tool.put("headers", headersJson);
        }
        if (requireApproval != null) {
            tool.put("require_approval", requireApproval);
        }
        if (serverDescription != null) {
            tool.put("server_description", serverDescription);
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            tool.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return tool;
    }

    /**
     * Builder for {@link OpenRouterMcpServerTool}. Only explicitly configured
     * fields are emitted (plus the required {@code type} and
     * {@code server_label}); a verbatim {@link #option(String, Object)} escape
     * hatch covers keys this library does not type.
     */
    public static final class Builder {

        private final String serverLabel;
        private String serverUrl;
        private String connectorId;
        private String authorization;
        private List<String> allowedToolNames;
        private Boolean allowedToolsReadOnly;
        private List<String> allowedToolNamesObjectForm;
        private Map<String, String> headers;
        private Object requireApproval;
        private String serverDescription;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder(String serverLabel) {
            this.serverLabel = Objects.requireNonNull(serverLabel, "serverLabel must not be null");
            if (serverLabel.isBlank()) {
                throw new IllegalArgumentException("serverLabel must not be blank");
            }
        }

        /**
         * Sets {@code server_url}: the MCP server endpoint. Emitted only when
         * set; the alternative to {@link #connectorId(String)}.
         *
         * @param serverUrl the MCP server URL
         * @return this builder
         */
        public Builder serverUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        /**
         * Sets {@code connector_id}: selects a pre-integrated connector instead
         * of a raw {@code server_url}. Documented values:
         * {@code connector_dropbox}, {@code connector_gmail},
         * {@code connector_googlecalendar}, {@code connector_googledrive},
         * {@code connector_microsoftteams}, {@code connector_outlookcalendar},
         * {@code connector_outlookemail}, {@code connector_sharepoint}.
         * Unknown values are accepted verbatim (the schema allows them).
         *
         * @param connectorId the connector id
         * @return this builder
         */
        public Builder connectorId(String connectorId) {
            this.connectorId = connectorId;
            return this;
        }

        /**
         * Sets {@code authorization}: the authorization token/header value
         * presented to the MCP server. Emitted only when set.
         *
         * @param authorization the authorization value
         * @return this builder
         */
        public Builder authorization(String authorization) {
            this.authorization = authorization;
            return this;
        }

        /**
         * Sets {@code allowed_tools} in its array-of-names form
         * {@code ["tool_a", "tool_b"]}: only these tools of the server are
         * exposed to the model. A later call replaces the whole list. Calling
         * this clears any previously set object form
         * ({@link #allowedToolsObject(Boolean, List)}); an empty list clears
         * {@code allowed_tools} entirely (the field is omitted).
         *
         * @param toolNames the allowed tool names
         * @return this builder
         */
        public Builder allowedTools(String... toolNames) {
            this.allowedToolNames = toolNames == null ? null : List.of(toolNames);
            this.allowedToolsReadOnly = null;
            this.allowedToolNamesObjectForm = null;
            return this;
        }

        /**
         * Sets {@code allowed_tools} in its array-of-names form. A later call
         * replaces the whole list. Calling this clears any previously set
         * object form ({@link #allowedToolsObject(Boolean, List)}); an empty
         * list clears {@code allowed_tools} entirely (the field is omitted).
         *
         * @param toolNames the allowed tool names
         * @return this builder
         */
        public Builder allowedTools(List<String> toolNames) {
            this.allowedToolNames = toolNames == null ? null : new ArrayList<>(toolNames);
            this.allowedToolsReadOnly = null;
            this.allowedToolNamesObjectForm = null;
            return this;
        }

        /**
         * Sets {@code allowed_tools} in its object form
         * {@code {"read_only": ..., "tool_names": [...]}}. Either part may be
         * {@code null} and is then omitted. Calling this clears any previously
         * set array-of-names form ({@link #allowedTools(List)}); both parts
         * null or empty clears {@code allowed_tools} entirely (the field is
         * omitted).
         *
         * @param readOnly the {@code read_only} flag (may be {@code null})
         * @param toolNames the {@code tool_names} list (may be {@code null} or empty)
         * @return this builder
         */
        public Builder allowedToolsObject(Boolean readOnly, List<String> toolNames) {
            this.allowedToolsReadOnly = readOnly;
            this.allowedToolNamesObjectForm = toolNames == null ? null : new ArrayList<>(toolNames);
            this.allowedToolNames = null;
            return this;
        }

        /**
         * Sets {@code headers}: extra HTTP headers presented to the MCP server.
         * Emitted only when at least one entry is set.
         *
         * @param headers the request headers for the MCP server
         * @return this builder
         */
        public Builder headers(Map<String, String> headers) {
            this.headers = headers == null ? null : new LinkedHashMap<>(headers);
            return this;
        }

        /**
         * Sets {@code require_approval} to the bare string form. Documented
         * values: {@code "always"} or {@code "never"} (the schema declares no
         * other values for this branch; other strings are still sent verbatim
         * as client-side leniency). Emitted only when set.
         *
         * @param approval the approval policy string
         * @return this builder
         */
        public Builder requireApproval(String approval) {
            this.requireApproval = approval;
            return this;
        }

        /**
         * Sets {@code require_approval} to the object form
         * {@code {"always": {"tool_names": [...]}}} or
         * {@code {"never": {"tool_names": [...]}}}.
         * <p>
         * Trap: a bare {@code requireApproval(null)} is a compile-time
         * ambiguity against the {@link #requireApproval(String)} overload -
         * write {@code requireApproval((JSONObject) null)} to unset the field.
         *
         * @param approval the approval policy object (may be {@code null} to unset)
         * @return this builder
         */
        public Builder requireApproval(JSONObject approval) {
            this.requireApproval = approval;
            return this;
        }

        /**
         * Sets {@code require_approval} to the object form with an
         * {@code always} ({@code true}) or {@code never} ({@code false})
         * tool-name list. A {@code null} or empty {@code toolNames} list means
         * "all tools" and omits {@code tool_names} inside the variant.
         *
         * @param always {@code true} for the {@code always} variant,
         *               {@code false} for the {@code never} variant
         * @param toolNames the tool names the variant applies to
         *                (may be {@code null} or empty for "all")
         * @return this builder
         */
        public Builder requireApproval(boolean always, List<String> toolNames) {
            JSONObject approval = new JSONObject();
            JSONObject variant = new JSONObject();
            if (toolNames != null && !toolNames.isEmpty()) {
                JSONArray arr = new JSONArray();
                toolNames.forEach(arr::put);
                variant.put("tool_names", arr);
            }
            approval.put(always ? "always" : "never", variant);
            this.requireApproval = approval;
            return this;
        }

        /**
         * Sets {@code server_description}: a human-readable description of the
         * server. Emitted only when set.
         *
         * @param serverDescription the server description
         * @return this builder
         */
        public Builder serverDescription(String serverDescription) {
            this.serverDescription = serverDescription;
            return this;
        }

        /**
         * Adds a top-level field verbatim - escape hatch for keys this library
         * does not type. Null values are emitted as JSON {@code null}.
         *
         * @param key the field name (must not be {@code "type"} or {@code "server_label"})
         * @param value the field value (String, Number, Boolean, org.json types or null)
         * @return this builder
         */
        public Builder option(String key, Object value) {
            if ("type".equals(key) || "server_label".equals(key)) {
                throw new IllegalArgumentException(
                        "The '" + key + "' field is set from the constructor/typed API and must not be set via option()");
            }
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the MCP server tool.
         *
         * @return the MCP server tool
         */
        public OpenRouterMcpServerTool build() {
            return new OpenRouterMcpServerTool(this);
        }
    }
}
