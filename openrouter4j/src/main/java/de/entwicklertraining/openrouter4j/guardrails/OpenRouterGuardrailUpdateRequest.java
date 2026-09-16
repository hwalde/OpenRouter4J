package de.entwicklertraining.openrouter4j.guardrails;

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
 * A request to update a guardrail:
 * PATCH https://openrouter.ai/api/v1/guardrails/{id}
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * for this endpoint - a normal inference key is rejected with an authorization
 * error (the schema documents HTTP 403; observed rejection codes vary).
 *
 * <p>Only explicitly configured fields are sent; every field is optional.
 */
public final class OpenRouterGuardrailUpdateRequest extends OpenRouterRequest<OpenRouterGuardrailUpdateResponse> {

    private final OpenRouterClient client;
    private final String id;
    private final String name;
    private final String description;
    private final List<String> allowedModels;
    private final List<String> ignoredModels;
    private final List<String> allowedProviders;
    private final List<String> ignoredProviders;
    private final List<String> allowedDataRegions;
    private final List<JSONObject> contentFilters;
    private final List<JSONObject> contentFilterBuiltins;
    private final Double limitUsd;
    private final String resetInterval;
    private final Boolean includeByokInBudgets;
    private final Boolean enforceZdr;
    private final Boolean enforceZdrAnthropic;
    private final Boolean enforceZdrGoogle;
    private final Boolean enforceZdrOpenai;
    private final Boolean enforceZdrXai;
    private final Boolean enforceZdrOther;
    private final Boolean enableFreeModelPublication;
    private final Boolean enableFreeModelTraining;
    private final Boolean enablePaidModelTraining;
    private final JSONObject modelCatalog;

    private OpenRouterGuardrailUpdateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.allowedModels = builder.allowedModels;
        this.ignoredModels = builder.ignoredModels;
        this.allowedProviders = builder.allowedProviders;
        this.ignoredProviders = builder.ignoredProviders;
        this.allowedDataRegions = builder.allowedDataRegions;
        this.contentFilters = builder.contentFilters;
        this.contentFilterBuiltins = builder.contentFilterBuiltins;
        this.limitUsd = builder.limitUsd;
        this.resetInterval = builder.resetInterval;
        this.includeByokInBudgets = builder.includeByokInBudgets;
        this.enforceZdr = builder.enforceZdr;
        this.enforceZdrAnthropic = builder.enforceZdrAnthropic;
        this.enforceZdrGoogle = builder.enforceZdrGoogle;
        this.enforceZdrOpenai = builder.enforceZdrOpenai;
        this.enforceZdrXai = builder.enforceZdrXai;
        this.enforceZdrOther = builder.enforceZdrOther;
        this.enableFreeModelPublication = builder.enableFreeModelPublication;
        this.enableFreeModelTraining = builder.enableFreeModelTraining;
        this.enablePaidModelTraining = builder.enablePaidModelTraining;
        this.modelCatalog = builder.modelCatalog;
    }

    /**
     * @return the URL-encoded path segment
     */
    public String id() {
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }

    @Override
    public String getRelativeUrl() {
        return "/guardrails/" + URLEncoder.encode(id, StandardCharsets.UTF_8);
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
        if (description != null) {
            body.put("description", description);
        }
        if (allowedModels != null) {
            body.put("allowed_models", new JSONArray(allowedModels));
        }
        if (ignoredModels != null) {
            body.put("ignored_models", new JSONArray(ignoredModels));
        }
        if (allowedProviders != null) {
            body.put("allowed_providers", new JSONArray(allowedProviders));
        }
        if (ignoredProviders != null) {
            body.put("ignored_providers", new JSONArray(ignoredProviders));
        }
        if (allowedDataRegions != null) {
            body.put("allowed_data_regions", new JSONArray(allowedDataRegions));
        }
        if (contentFilters != null) {
            body.put("content_filters", new JSONArray(contentFilters));
        }
        if (contentFilterBuiltins != null) {
            body.put("content_filter_builtins", new JSONArray(contentFilterBuiltins));
        }
        if (limitUsd != null) {
            body.put("limit_usd", limitUsd);
        }
        if (resetInterval != null) {
            body.put("reset_interval", resetInterval);
        }
        if (includeByokInBudgets != null) {
            body.put("include_byok_in_budgets", includeByokInBudgets);
        }
        if (enforceZdr != null) {
            body.put("enforce_zdr", enforceZdr);
        }
        if (enforceZdrAnthropic != null) {
            body.put("enforce_zdr_anthropic", enforceZdrAnthropic);
        }
        if (enforceZdrGoogle != null) {
            body.put("enforce_zdr_google", enforceZdrGoogle);
        }
        if (enforceZdrOpenai != null) {
            body.put("enforce_zdr_openai", enforceZdrOpenai);
        }
        if (enforceZdrXai != null) {
            body.put("enforce_zdr_xai", enforceZdrXai);
        }
        if (enforceZdrOther != null) {
            body.put("enforce_zdr_other", enforceZdrOther);
        }
        if (enableFreeModelPublication != null) {
            body.put("enable_free_model_publication", enableFreeModelPublication);
        }
        if (enableFreeModelTraining != null) {
            body.put("enable_free_model_training", enableFreeModelTraining);
        }
        if (enablePaidModelTraining != null) {
            body.put("enable_paid_model_training", enablePaidModelTraining);
        }
        if (modelCatalog != null) {
            body.put("model_catalog", modelCatalog);
        }
        return body.toString();
    }

    @Override
    public OpenRouterGuardrailUpdateResponse createResponse(String responseBody) {
        return new OpenRouterGuardrailUpdateResponse(new JSONObject(responseBody), this);
    }

    /**
     * Starting point for building a {@link OpenRouterGuardrailUpdateRequest}.
     */
    public static final class Builder extends ApiRequestBuilderBase<Builder, OpenRouterGuardrailUpdateRequest> {

        private final OpenRouterClient client;
    private final String id;
    private String name;
    private String description;
    private List<String> allowedModels;
    private List<String> ignoredModels;
    private List<String> allowedProviders;
    private List<String> ignoredProviders;
    private List<String> allowedDataRegions;
    private List<JSONObject> contentFilters;
    private List<JSONObject> contentFilterBuiltins;
    private Double limitUsd;
    private String resetInterval;
    private Boolean includeByokInBudgets;
    private Boolean enforceZdr;
    private Boolean enforceZdrAnthropic;
    private Boolean enforceZdrGoogle;
    private Boolean enforceZdrOpenai;
    private Boolean enforceZdrXai;
    private Boolean enforceZdrOther;
    private Boolean enableFreeModelPublication;
    private Boolean enableFreeModelTraining;
    private Boolean enablePaidModelTraining;
    private JSONObject modelCatalog;


    /**
     * Creates a builder bound to the given client.
     *
     * @param client the client used to send the request
     * @param id the id (UUID) of the guardrail
     */
    public Builder(OpenRouterClient client, String id) {
        super(client);
        this.client = client;
        this.id = id;
    }

    /**
 * Sets the body field {@code name} (optional on update) - new name for the
 * guardrail (max 200 characters).
 */
    public Builder name(String name) {
        this.name = name;
        return this;
    }
    /**
 * Sets the body field {@code description} (optional) - description of the
 * guardrail (max 1000 characters).
 */
    public Builder description(String description) {
        this.description = description;
        return this;
    }
    /**
 * Sets the body field {@code allowed_models} (optional) - model identifiers
 * (slug or canonical_slug) the guardrail restricts traffic to; must contain at
 * least one entry when set.
 */
    public Builder allowedModels(List<String> allowedModels) {
        this.allowedModels = allowedModels;
        return this;
    }
    /**
 * Sets the body field {@code ignored_models} (optional) - model identifiers
 * excluded from routing; must contain at least one entry when set.
 */
    public Builder ignoredModels(List<String> ignoredModels) {
        this.ignoredModels = ignoredModels;
        return this;
    }
    /**
 * Sets the body field {@code allowed_providers} (optional) - provider ids the
 * guardrail restricts routing to; must contain at least one entry when set.
 */
    public Builder allowedProviders(List<String> allowedProviders) {
        this.allowedProviders = allowedProviders;
        return this;
    }
    /**
 * Sets the body field {@code ignored_providers} (optional) - provider ids
 * excluded from routing; must contain at least one entry when set.
 */
    public Builder ignoredProviders(List<String> ignoredProviders) {
        this.ignoredProviders = ignoredProviders;
        return this;
    }
    /**
 * Sets the body field {@code allowed_data_regions} (optional) - data regions
 * requests governed by this guardrail must arrive through ({@code global},
 * {@code europe}, {@code us}); must contain at least one entry when set, an
 * empty array is rejected by the API.
 */
    public Builder allowedDataRegions(List<String> allowedDataRegions) {
        this.allowedDataRegions = allowedDataRegions;
        return this;
    }
    /**
 * Sets the body field {@code content_filters} (optional) - custom regex content
 * filters as raw JSON objects, each carrying {@code action}
 * ({@code block}/{@code redact}/{@code flag}), {@code pattern} and the optional
 * {@code label}.
 */
    public Builder contentFilters(List<JSONObject> contentFilters) {
        this.contentFilters = contentFilters;
        return this;
    }
    /**
 * Sets the body field {@code content_filter_builtins} (optional) - builtin
 * content filters as raw JSON objects, each carrying {@code slug},
 * {@code action} ({@code block}/{@code redact}/{@code flag}) and the optional
 * {@code label}.
 */
    public Builder contentFilterBuiltins(List<JSONObject> contentFilterBuiltins) {
        this.contentFilterBuiltins = contentFilterBuiltins;
        return this;
    }
    /**
     * Adds one builtin content filter entry to the body field
     * {@code content_filter_builtins} (creating the list lazily).
     *
     * @param slug the builtin filter slug (e.g. {@code email},
     *             {@code regex-prompt-injection})
     * @param action the filter action ({@code block}, {@code redact} or the
     *               detect-only {@code flag})
     * @return this builder
     */
    public Builder addBuiltinContentFilter(String slug, String action) {
        if (this.contentFilterBuiltins == null) {
            this.contentFilterBuiltins = new ArrayList<>();
        }
        this.contentFilterBuiltins.add(new JSONObject().put("slug", slug).put("action", action));
        return this;
    }

    /**
 * Sets the body field {@code limit_usd} (optional) - spending limit in USD.
 * Trap: the API rejects the request with 400 when exactly one of {@code limit_usd}
 * and {@code reset_interval} is set, so always set both.
 */
    public Builder limitUsd(Double limitUsd) {
        this.limitUsd = limitUsd;
        return this;
    }
    /**
 * Sets the body field {@code reset_interval} (optional) - interval at which the
 * limit resets ({@code daily}, {@code weekly}, {@code monthly}). Trap: the API
 * rejects the request with 400 when exactly one of {@code limit_usd} and
 * {@code reset_interval} is set, so always set both.
 */
    public Builder resetInterval(String resetInterval) {
        this.resetInterval = resetInterval;
        return this;
    }
    /**
 * Sets the body field {@code include_byok_in_budgets} (optional) - whether BYOK
 * spend counts toward the guardrail's {@code limit_usd} in addition to OpenRouter
 * credit spend.
 */
    public Builder includeByokInBudgets(Boolean includeByokInBudgets) {
        this.includeByokInBudgets = includeByokInBudgets;
        return this;
    }
    /**
 * Sets the body field {@code enforce_zdr} (optional, deprecated) - blanket
 * zero-data-retention switch; the server copies it into the per-provider fields
 * that are not explicitly set. Prefer the per-provider methods.
 */
    public Builder enforceZdr(Boolean enforceZdr) {
        this.enforceZdr = enforceZdr;
        return this;
    }
    /**
 * Sets the body field {@code enforce_zdr_anthropic} (optional) - whether to
 * enforce zero data retention for Anthropic models.
 */
    public Builder enforceZdrAnthropic(Boolean enforceZdrAnthropic) {
        this.enforceZdrAnthropic = enforceZdrAnthropic;
        return this;
    }
    /**
 * Sets the body field {@code enforce_zdr_google} (optional) - whether to enforce
 * zero data retention for Google models.
 */
    public Builder enforceZdrGoogle(Boolean enforceZdrGoogle) {
        this.enforceZdrGoogle = enforceZdrGoogle;
        return this;
    }
    /**
 * Sets the body field {@code enforce_zdr_openai} (optional) - whether to enforce
 * zero data retention for OpenAI models.
 */
    public Builder enforceZdrOpenai(Boolean enforceZdrOpenai) {
        this.enforceZdrOpenai = enforceZdrOpenai;
        return this;
    }
    /**
 * Sets the body field {@code enforce_zdr_xai} (optional) - whether to enforce
 * zero data retention for xAI models.
 */
    public Builder enforceZdrXai(Boolean enforceZdrXai) {
        this.enforceZdrXai = enforceZdrXai;
        return this;
    }
    /**
 * Sets the body field {@code enforce_zdr_other} (optional) - whether to enforce
 * zero data retention for models of other providers.
 */
    public Builder enforceZdrOther(Boolean enforceZdrOther) {
        this.enforceZdrOther = enforceZdrOther;
        return this;
    }
    /**
 * Sets the body field {@code enable_free_model_publication} (optional) - whether
 * the guardrail allows free endpoints that publish prompts.
 */
    public Builder enableFreeModelPublication(Boolean enableFreeModelPublication) {
        this.enableFreeModelPublication = enableFreeModelPublication;
        return this;
    }
    /**
 * Sets the body field {@code enable_free_model_training} (optional) - whether
 * the guardrail allows free endpoints that train on request data.
 */
    public Builder enableFreeModelTraining(Boolean enableFreeModelTraining) {
        this.enableFreeModelTraining = enableFreeModelTraining;
        return this;
    }
    /**
 * Sets the body field {@code enable_paid_model_training} (optional) - whether
 * the guardrail allows paid endpoints that train on request data.
 */
    public Builder enablePaidModelTraining(Boolean enablePaidModelTraining) {
        this.enablePaidModelTraining = enablePaidModelTraining;
        return this;
    }
    /**
 * Sets the body field {@code model_catalog} (optional) - catalog presentation
 * policy for {@code GET /api/v1/models/user} as a raw JSON object ({@code models},
 * {@code sort}, {@code include_private_models}, ...). It never widens routing
 * access beyond {@code allowed_models} and {@code ignored_models}.
 */
    public Builder modelCatalog(JSONObject modelCatalog) {
        this.modelCatalog = modelCatalog;
        return this;
    }
        @Override
        public OpenRouterGuardrailUpdateRequest build() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("id is required");
        }
        if ((limitUsd == null) != (resetInterval == null)) {
            throw new IllegalStateException("limit_usd and reset_interval must be provided together");
        }
            return new OpenRouterGuardrailUpdateRequest(this);
        }


    @Override
    public OpenRouterGuardrailUpdateResponse execute() {
        return client.sendRequest(build());
    }

    @Override
    public OpenRouterGuardrailUpdateResponse executeWithRetry() {
        return client.sendRequestWithRetry(build());
    }

    @Override
    public OpenRouterGuardrailUpdateResponse executeWithExponentialBackoff() {
        return client.sendRequestWithExponentialBackoff(build());
    }
    }
}
