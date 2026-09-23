package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A request to update a workspace:
 * PATCH https://openrouter.ai/api/v1/workspaces/{id}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 * <p>Only explicitly configured fields are sent; every field is optional.
 * Unlike the create request, {@code slug} can be set here to rename the
 * workspace.
 */
public final class OpenRouterWorkspaceUpdateRequest extends OpenRouterRequest<OpenRouterWorkspaceUpdateResponse> {

    private final OpenRouterClient client;
    private final String id;
    private final String name;
    private final String slug;
    private final String description;
    private final String defaultTextModel;
    private final String defaultImageModel;
    private final String defaultProviderSort;
    private final List<Integer> ioLoggingApiKeyIds;
    private final Double ioLoggingSamplingRate;
    private final Boolean isDataDiscountLoggingEnabled;
    private final Boolean isObservabilityBroadcastEnabled;
    private final Boolean isObservabilityIoLoggingEnabled;
    private final List<String> disabledServerTools;

    private OpenRouterWorkspaceUpdateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.name = builder.name;
        this.slug = builder.slug;
        this.description = builder.description;
        this.defaultTextModel = builder.defaultTextModel;
        this.defaultImageModel = builder.defaultImageModel;
        this.defaultProviderSort = builder.defaultProviderSort;
        this.ioLoggingApiKeyIds = builder.ioLoggingApiKeyIds;
        this.ioLoggingSamplingRate = builder.ioLoggingSamplingRate;
        this.isDataDiscountLoggingEnabled = builder.isDataDiscountLoggingEnabled;
        this.isObservabilityBroadcastEnabled = builder.isObservabilityBroadcastEnabled;
        this.isObservabilityIoLoggingEnabled = builder.isObservabilityIoLoggingEnabled;
        this.disabledServerTools = builder.disabledServerTools == null
                ? null : List.copyOf(builder.disabledServerTools);
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/workspaces/" + URLEncoder.encode(id, StandardCharsets.UTF_8) + "";
    }

    @Override
    public String getHttpMethod() {
        return "PATCH";
    }

    /**
     * Builds the JSON body with only the explicitly configured fields
     * (an unset option never appears in the JSON).
     *
     * @return the JSON body
     */
    @Override
    public String getBody() {
        JSONObject body = new JSONObject();
        if (name != null) {
            body.put("name", name);
        }
        if (slug != null) {
            body.put("slug", slug);
        }
        if (description != null) {
            body.put("description", description);
        }
        if (defaultTextModel != null) {
            body.put("default_text_model", defaultTextModel);
        }
        if (defaultImageModel != null) {
            body.put("default_image_model", defaultImageModel);
        }
        if (defaultProviderSort != null) {
            body.put("default_provider_sort", defaultProviderSort);
        }
        if (ioLoggingApiKeyIds != null) {
            body.put("io_logging_api_key_ids", new JSONArray(ioLoggingApiKeyIds));
        }
        if (ioLoggingSamplingRate != null) {
            body.put("io_logging_sampling_rate", ioLoggingSamplingRate);
        }
        if (isDataDiscountLoggingEnabled != null) {
            body.put("is_data_discount_logging_enabled", isDataDiscountLoggingEnabled);
        }
        if (isObservabilityBroadcastEnabled != null) {
            body.put("is_observability_broadcast_enabled", isObservabilityBroadcastEnabled);
        }
        if (isObservabilityIoLoggingEnabled != null) {
            body.put("is_observability_io_logging_enabled", isObservabilityIoLoggingEnabled);
        }
        if (disabledServerTools != null) {
            body.put("disabled_server_tools", new JSONArray(disabledServerTools));
        }
        return body.toString();
    }

    @Override
    public OpenRouterWorkspaceUpdateResponse createResponse(String responseBody) {
        return new OpenRouterWorkspaceUpdateResponse(new JSONObject(responseBody), this);
    }

    /**
     * @return the configured {@code disabled_server_tools} list, empty when
     *         unset (never {@code null}). An empty list when set to empty
     *         (the API clears the disabled list on an empty array).
     */
    public List<String> disabledServerTools() {
        return disabledServerTools == null ? List.of() : disabledServerTools;
    }

    /**
     * Starting point for building a {@link OpenRouterWorkspaceUpdateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterWorkspaceUpdateRequest> {

        private final OpenRouterClient client;
    private final String id;
    private String name;
    private String slug;
    private String description;
    private String defaultTextModel;
    private String defaultImageModel;
    private String defaultProviderSort;
    private List<Integer> ioLoggingApiKeyIds;
    private Double ioLoggingSamplingRate;
    private Boolean isDataDiscountLoggingEnabled;
    private Boolean isObservabilityBroadcastEnabled;
    private Boolean isObservabilityIoLoggingEnabled;
    private List<String> disabledServerTools;


    /**
     * Creates a builder bound to the given client.
     *
     * @param client the client used to send the request
     * @param id the id (UUID) of the workspace
     */
    public Builder(OpenRouterClient client, String id) {
        super(client);
        this.client = client;
        this.id = id;
    }

    /**
 * Sets the body field {@code name} (optional) - new display name for the
 * workspace (max 100 characters).
 */
    public Builder name(String name) {
        this.name = name;
        return this;
    }
    /**
 * Sets the body field {@code slug} (optional) - new URL-friendly slug
 * (lowercase alphanumeric segments separated by single hyphens, no
 * leading/trailing hyphens, max 50 characters); renaming the workspace.
 */
    public Builder slug(String slug) {
        this.slug = slug;
        return this;
    }
    /**
 * Sets the body field {@code description} (optional) - new description (max
 * 500 characters).
 */
    public Builder description(String description) {
        this.description = description;
        return this;
    }
    /**
 * Sets the body field {@code default_text_model} (optional) - new default text
 * model for this workspace.
 */
    public Builder defaultTextModel(String defaultTextModel) {
        this.defaultTextModel = defaultTextModel;
        return this;
    }
    /**
 * Sets the body field {@code default_image_model} (optional) - new default
 * image model for this workspace.
 */
    public Builder defaultImageModel(String defaultImageModel) {
        this.defaultImageModel = defaultImageModel;
        return this;
    }
    /**
 * Sets the body field {@code default_provider_sort} (optional) - new default
 * provider sort preference (price, throughput, latency, exacto).
 */
    public Builder defaultProviderSort(String defaultProviderSort) {
        this.defaultProviderSort = defaultProviderSort;
        return this;
    }
    /**
 * Sets the body field {@code io_logging_api_key_ids} (optional) - the API key
 * ids I/O logging is filtered to.
 */
    public Builder ioLoggingApiKeyIds(List<Integer> ioLoggingApiKeyIds) {
        this.ioLoggingApiKeyIds = ioLoggingApiKeyIds;
        return this;
    }
    /**
 * Sets the body field {@code io_logging_sampling_rate} (optional) - I/O-logging
 * sampling rate between 0.0001 and 1 (1 = 100% of requests logged).
 */
    public Builder ioLoggingSamplingRate(Double ioLoggingSamplingRate) {
        this.ioLoggingSamplingRate = ioLoggingSamplingRate;
        return this;
    }
    /**
 * Sets the body field {@code is_data_discount_logging_enabled} (optional) -
 * whether data discount logging is enabled.
 */
    public Builder isDataDiscountLoggingEnabled(Boolean isDataDiscountLoggingEnabled) {
        this.isDataDiscountLoggingEnabled = isDataDiscountLoggingEnabled;
        return this;
    }
    /**
 * Sets the body field {@code is_observability_broadcast_enabled} (optional) -
 * whether observability broadcast is enabled.
 */
    public Builder isObservabilityBroadcastEnabled(Boolean isObservabilityBroadcastEnabled) {
        this.isObservabilityBroadcastEnabled = isObservabilityBroadcastEnabled;
        return this;
    }
    /**
 * Sets the body field {@code is_observability_io_logging_enabled} (optional) -
 * whether private I/O logging is enabled.
 */
    public Builder isObservabilityIoLoggingEnabled(Boolean isObservabilityIoLoggingEnabled) {
        this.isObservabilityIoLoggingEnabled = isObservabilityIoLoggingEnabled;
        return this;
    }

    /**
     * Sets the body field {@code disabled_server_tools} - the OpenRouter
     * server tools that requests in this workspace may not invoke. A request
     * naming a disabled tool is rejected with HTTP 403. The schema enumerates
     * {@code openrouter:advisor}, {@code openrouter:apply_patch},
     * {@code openrouter:bash}, {@code openrouter:datetime},
     * {@code openrouter:fusion}, {@code openrouter:image_generation},
     * {@code openrouter:experimental__search_models}, {@code openrouter:shell},
     * {@code openrouter:subagent}, {@code openrouter:tool_search},
     * {@code openrouter:web_fetch}, {@code openrouter:web_search} and allows
     * unknown values - the library keeps the ids verbatim and validates only
     * that each is non-empty. An empty array or {@code null} clears the list.
     * Unset (never called) leaves the stored value unchanged.
     * Calling this replaces a previously set list.
     *
     * @param toolIds the disabled server-tool ids
     * @return this builder
     */
    public Builder disabledServerTools(String... toolIds) {
        if (toolIds == null) {
            this.disabledServerTools = null;
            return this;
        }
        return disabledServerTools(List.of(toolIds));
    }

    /**
     * List-based variant of {@link #disabledServerTools(String...)}.
     * {@code null} unsets the field (leaves the stored value unchanged);
     * an empty list emits {@code []} and clears the disabled list.
     *
     * @param toolIds the disabled server-tool ids
     * @return this builder
     */
    public Builder disabledServerTools(List<String> toolIds) {
        if (toolIds == null) {
            this.disabledServerTools = null;
            return this;
        }
        for (String toolId : toolIds) {
            if (toolId == null || toolId.isEmpty()) {
                throw new IllegalArgumentException("disabled server tool id must not be empty");
            }
        }
        this.disabledServerTools = new ArrayList<>(toolIds);
        return this;
    }

    /**
     * Adds a single server-tool id to {@code disabled_server_tools} (see
     * {@link #disabledServerTools(String...)}). Unlike the setter, this
     * accumulates: repeated calls append one id each.
     *
     * @param toolId the disabled server-tool id
     * @return this builder
     */
    public Builder addDisabledServerTool(String toolId) {
        if (toolId == null || toolId.isEmpty()) {
            throw new IllegalArgumentException("disabled server tool id must not be empty");
        }
        if (this.disabledServerTools == null) {
            this.disabledServerTools = new ArrayList<>();
        }
        this.disabledServerTools.add(toolId);
        return this;
    }
        @Override
        public OpenRouterWorkspaceUpdateRequest build() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("id is required");
        }
            return new OpenRouterWorkspaceUpdateRequest(this);
        }


    @Override
    public OpenRouterWorkspaceUpdateResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterWorkspaceUpdateResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterWorkspaceUpdateResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
