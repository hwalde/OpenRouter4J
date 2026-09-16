package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A request to create a new workspace:
 * POST https://openrouter.ai/api/v1/workspaces
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 * <p>The {@code slug} must be a URL-friendly lowercase slug (lowercase
 * alphanumeric segments separated by single hyphens); it can be renamed later
 * via PATCH /workspaces/{id}.
 */
public final class OpenRouterWorkspaceCreateRequest extends OpenRouterRequest<OpenRouterWorkspaceCreateResponse> {

    private final OpenRouterClient client;
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

    private OpenRouterWorkspaceCreateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
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
    }


    @Override
    public String getRelativeUrl() {
        return "/workspaces";
    }

    @Override
    public String getHttpMethod() {
        return "POST";
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
        body.put("name", name);
        body.put("slug", slug);
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
        return body.toString();
    }

    @Override
    public OpenRouterWorkspaceCreateResponse createResponse(String responseBody) {
        return new OpenRouterWorkspaceCreateResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterWorkspaceCreateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterWorkspaceCreateRequest> {

        private final OpenRouterClient client;
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


    /**
     * Creates a builder bound to the given client.
     *
     * @param client the client used to send the request
     */
    public Builder(OpenRouterClient client) {
        super(client);
        this.client = client;
    }

    /**
 * Sets the body field {@code name} (required) - display name of the new
 * workspace (max 100 characters).
 */
    public Builder name(String name) {
        this.name = name;
        return this;
    }
    /**
 * Sets the body field {@code slug} (required) - URL-friendly slug
 * (lowercase alphanumeric segments separated by single hyphens, no
 * leading/trailing hyphens, max 50 characters).
 */
    public Builder slug(String slug) {
        this.slug = slug;
        return this;
    }
    /**
 * Sets the body field {@code description} (optional) - human-readable
 * description (max 500 characters).
 */
    public Builder description(String description) {
        this.description = description;
        return this;
    }
    /**
 * Sets the body field {@code default_text_model} (optional) - default text
 * model for this workspace.
 */
    public Builder defaultTextModel(String defaultTextModel) {
        this.defaultTextModel = defaultTextModel;
        return this;
    }
    /**
 * Sets the body field {@code default_image_model} (optional) - default image
 * model for this workspace.
 */
    public Builder defaultImageModel(String defaultImageModel) {
        this.defaultImageModel = defaultImageModel;
        return this;
    }
    /**
 * Sets the body field {@code default_provider_sort} (optional) - default
 * provider sort preference (price, throughput, latency, exacto).
 */
    public Builder defaultProviderSort(String defaultProviderSort) {
        this.defaultProviderSort = defaultProviderSort;
        return this;
    }
    /**
 * Sets the body field {@code io_logging_api_key_ids} (optional) - the API key
 * ids I/O logging is filtered to; {@code null} (unset) means all keys are logged.
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
        @Override
        public OpenRouterWorkspaceCreateRequest build() {
        if (name == null || name.isEmpty()) {
            throw new IllegalStateException("name is required for workspace creation");
        }
        if (slug == null || slug.isEmpty()) {
            throw new IllegalStateException("slug is required for workspace creation");
        }
            return new OpenRouterWorkspaceCreateRequest(this);
        }


    @Override
    public OpenRouterWorkspaceCreateResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterWorkspaceCreateResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterWorkspaceCreateResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
