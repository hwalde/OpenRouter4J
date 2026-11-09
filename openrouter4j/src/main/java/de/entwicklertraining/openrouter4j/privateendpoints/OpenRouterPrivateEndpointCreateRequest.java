package de.entwicklertraining.openrouter4j.privateendpoints;

import de.entwicklertraining.api.base.ApiRequestBuilderBase;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterRequest;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A request to create a private endpoint as a draft:
 * POST https://openrouter.ai/api/v1/private-endpoints
 *
 * <p>OpenRouter requires a
 * <a href="https://openrouter.ai/docs/guides/overview/auth/management-api-keys">management key</a>
 * and the private endpoints entitlement for this route.
 *
 * <p>Draft lifecycle: a fresh endpoint is a {@code draft} and is not
 * routable - validate it with
 * {@code POST /private-endpoints/{id}/validate}, then activate it with
 * {@code POST /private-endpoints/{id}/activate}, or pass
 * {@link Builder#activate(String)} to do all three in one call. On a failed
 * validation the draft is <b>kept</b> and returned with the failed checks
 * (HTTP 422 carrying the endpoint and the checks) - fix it and call
 * {@code /validate} + {@code /activate} instead of creating it again.
 *
 * <p>Traps:
 * <ul>
 *   <li>The answer is 201 with a {@code ManagedPrivateEndpoint}; it does
 *       <b>not</b> echo {@code declared_parameters} or
 *       {@code tested_parameters} - read those with
 *       GET /private-endpoints/{id}.</li>
 *   <li>{@code base_url} is required unless the provider derives its URL
 *       from the BYOK credential (Azure, Amazon Bedrock, Google Vertex).</li>
 *   <li>400 {@code parameters_invalid} when {@code tool_choice} is declared
 *       without {@code tools} (validated loudly here when the declaration is
 *       set).</li>
 *   <li>Omitted {@code pricing.cache_read}/{@code cache_write} rates are
 *       inherited from the public endpoint of the same model and provider
 *       (scaled by its cache ratios, or the pricing strategy default).</li>
 *   <li>With {@link Builder#idempotencyKey(String)} retries are safe: a
 *       repeated key with the same request returns the endpoint the first
 *       request created; reusing it with different fields answers 422
 *       {@code idempotency_key_reused}. Declared parameters are compared as
 *       a set - a retry that reorders or repeats a name replays the
 *       endpoint; {@code activate} and {@code settings} are not compared,
 *       and later edits to the endpoint do not affect the comparison
 *       ({@code settings} is still validated before the replay, so a retry
 *       with invalid settings answers 400 {@code settings_invalid} instead
 *       of the endpoint).</li>
 * </ul>
 *
 * <p>Not yet live-verified (management route; probing mutates account
 * state): the wire shapes follow the OpenAPI schema.
 */
public final class OpenRouterPrivateEndpointCreateRequest
        extends OpenRouterRequest<OpenRouterManagedPrivateEndpointResponse<OpenRouterPrivateEndpointCreateRequest>> {

    private final OpenRouterClient client;
    private final String modelPermalslug;
    private final String providerSlug;
    private final String upstreamModelId;
    private final String baseUrl;
    private final List<String> declaredParameters;
    private final String declaredRegion;
    private final boolean declaredRegionSet;
    private final Boolean declaredZdr;
    private final boolean declaredZdrSet;
    private final Integer maxCompletionTokens;
    private final boolean maxCompletionTokensSet;
    private final OpenRouterPrivateEndpointPricing pricing;
    private final OpenRouterPrivateEndpointSettingsUpdate settings;
    private final String activateWorkspaceId;

    private OpenRouterPrivateEndpointCreateRequest(Builder builder) {
        super(builder);
        this.client = builder.client;
        this.modelPermalslug = builder.modelPermalslug;
        this.providerSlug = builder.providerSlug;
        this.upstreamModelId = builder.upstreamModelId;
        this.baseUrl = builder.baseUrl;
        this.declaredParameters = builder.declaredParameters;
        this.declaredRegion = builder.declaredRegion;
        this.declaredRegionSet = builder.declaredRegionSet;
        this.declaredZdr = builder.declaredZdr;
        this.declaredZdrSet = builder.declaredZdrSet;
        this.maxCompletionTokens = builder.maxCompletionTokens;
        this.maxCompletionTokensSet = builder.maxCompletionTokensSet;
        this.pricing = builder.pricing;
        this.settings = builder.settings;
        this.activateWorkspaceId = builder.activateWorkspaceId;
        if (builder.idempotencyKey != null) {
            setHeader("Idempotency-Key", builder.idempotencyKey);
        }
    }

    /**
     * @return the permanent slug of the model this endpoint serves
     */
    public String modelPermalslug() {
        return modelPermalslug;
    }

    /**
     * @return the slug of the upstream provider
     */
    public String providerSlug() {
        return providerSlug;
    }

    /**
     * @return the model or deployment identifier sent to the upstream
     *         provider
     */
    public String upstreamModelId() {
        return upstreamModelId;
    }

    /**
     * @return the HTTPS base URL of the deployment, or {@code null} when
     *         unset (the provider derives its URL from the BYOK credential)
     */
    public String baseUrl() {
        return baseUrl;
    }

    /**
     * @return the declared parameter names, or {@code null} when unset
     */
    public List<String> declaredParameters() {
        return declaredParameters;
    }

    /**
     * @return the value of {@code declared_region}, or {@code null} when the
     *         attestation is unset or was explicitly set to {@code null} (the
     *         two states are indistinguishable here - on create both leave
     *         the attestation undeclared; the difference is in the body, not
     *         this accessor)
     */
    public String declaredRegion() {
        return declaredRegion;
    }

    /**
     * @return the value of {@code declared_zdr}, or {@code null} when the
     *         attestation is unset or was explicitly set to {@code null} (the
     *         two states are indistinguishable here - on create both leave
     *         the attestation undeclared)
     */
    public Boolean declaredZdr() {
        return declaredZdr;
    }

    /**
     * @return the completion cap, or {@code null} when unset or explicitly
     *         set to {@code null} (emitted as {@code settings.max_completion_tokens})
     */
    public Integer maxCompletionTokens() {
        return maxCompletionTokens;
    }

    /**
     * @return the pricing value, or {@code null} when unset
     */
    public OpenRouterPrivateEndpointPricing pricing() {
        return pricing;
    }

    /**
     * @return the settings value, or {@code null} when unset
     */
    public OpenRouterPrivateEndpointSettingsUpdate settings() {
        return settings;
    }

    /**
     * @return the workspace id of the {@code activate} object (validate and
     *         activate in the same call), or {@code null} when unset
     */
    public String activateWorkspaceId() {
        return activateWorkspaceId;
    }

    @Override
    public String getRelativeUrl() {
        return "/private-endpoints";
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
        body.put("model_permaslug", modelPermalslug);
        body.put("provider_slug", providerSlug);
        body.put("upstream_model_id", upstreamModelId);
        if (baseUrl != null) {
            body.put("base_url", baseUrl);
        }
        if (declaredParameters != null) {
            body.put("declared_parameters", new JSONArray(declaredParameters));
        }
        if (declaredRegionSet) {
            body.put("declared_region", declaredRegion == null ? JSONObject.NULL : declaredRegion);
        }
        if (declaredZdrSet) {
            body.put("declared_zdr", declaredZdr == null ? JSONObject.NULL : declaredZdr);
        }
        if (maxCompletionTokensSet || settings != null) {
            JSONObject settingsJson = settings != null
                    ? new JSONObject(settings.json().toString())
                    : new JSONObject();
            // a max_completion_tokens named inside settings(...) wins over the
            // shorthand setter
            if (maxCompletionTokensSet && !settingsJson.has("max_completion_tokens")) {
                settingsJson.put("max_completion_tokens",
                        maxCompletionTokens == null ? JSONObject.NULL : maxCompletionTokens);
            }
            if (!settingsJson.isEmpty()) {
                body.put("settings", settingsJson);
            }
        }
        if (pricing != null) {
            body.put("pricing", pricing.json());
        }
        if (activateWorkspaceId != null) {
            body.put("activate", new JSONObject().put("workspace_id", activateWorkspaceId));
        }
        return body.toString();
    }

    @Override
    public OpenRouterManagedPrivateEndpointResponse<OpenRouterPrivateEndpointCreateRequest> createResponse(
            String responseBody) {
        return new OpenRouterManagedPrivateEndpointResponse<>(
                responseBody == null || responseBody.isEmpty()
                        ? new JSONObject()
                        : new JSONObject(responseBody),
                this);
    }

    /**
     * Starting point for building an {@link OpenRouterPrivateEndpointCreateRequest}.
     */
    public static final class Builder
            extends ApiRequestBuilderBase<Builder, OpenRouterPrivateEndpointCreateRequest> {

        private final OpenRouterClient client;
        private String modelPermalslug;
        private String providerSlug;
        private String upstreamModelId;
        private String baseUrl;
        private List<String> declaredParameters;
        private String declaredRegion;
        private boolean declaredRegionSet;
        private Boolean declaredZdr;
        private boolean declaredZdrSet;
        private Integer maxCompletionTokens;
        private boolean maxCompletionTokensSet;
        private OpenRouterPrivateEndpointPricing pricing;
        private OpenRouterPrivateEndpointSettingsUpdate settings;
        private String activateWorkspaceId;
        private String idempotencyKey;

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
         * Sets the body field {@code model_permaslug} (required) - permanent
         * slug of the model this endpoint serves (for example
         * {@code openai/gpt-4o-2024-08-06}).
         *
         * @param modelPermalslug the model permaslug
         * @return this builder
         */
        public Builder modelPermalslug(String modelPermalslug) {
            this.modelPermalslug = modelPermalslug;
            return this;
        }

        /**
         * Sets the body field {@code provider_slug} (required) - slug of the
         * upstream provider (for example {@code azure}).
         *
         * @param providerSlug the provider slug
         * @return this builder
         */
        public Builder providerSlug(String providerSlug) {
            this.providerSlug = providerSlug;
            return this;
        }

        /**
         * Sets the body field {@code upstream_model_id} (required) - model or
         * deployment identifier sent to the upstream provider (for example
         * {@code gpt-4o-prod}).
         *
         * @param upstreamModelId the upstream model or deployment id
         * @return this builder
         */
        public Builder upstreamModelId(String upstreamModelId) {
            this.upstreamModelId = upstreamModelId;
            return this;
        }

        /**
         * Sets the body field {@code base_url} (optional) - HTTPS base URL of
         * the deployment. Required unless the provider derives its URL from
         * the BYOK credential (Azure, Amazon Bedrock, Google Vertex).
         * Never empty ({@code minLength: 1}, validated loudly).
         *
         * @param baseUrl the deployment base URL, {@code null} to unset
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            if (baseUrl != null && baseUrl.isEmpty()) {
                throw new IllegalArgumentException("base_url must not be empty (minLength 1)");
            }
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Sets the body field {@code declared_parameters} (optional) -
         * parameters the upstream supports beyond the tested ones. Plain
         * parameter-name strings (schema
         * {@code PrivateEndpointDeclaredParameter}: {@code temperature},
         * {@code top_p}, {@code top_k}, {@code min_p}, {@code top_a},
         * {@code frequency_penalty}, {@code presence_penalty},
         * {@code repetition_penalty}, {@code max_tokens},
         * {@code max_completion_tokens}, {@code logit_bias}, {@code logprobs},
         * {@code top_logprobs}, {@code prediction}, {@code seed},
         * {@code response_format}, {@code structured_outputs}, {@code stop},
         * {@code tools}, {@code tool_choice}, {@code parallel_tool_calls},
         * {@code reasoning}, {@code reasoning_effort},
         * {@code web_search_options}, {@code verbosity}); unknown spellings
         * pass through verbatim. Order and repeated names are ignored, but
         * the list holds at most 25 entries (validated loudly).
         *
         * <p>Trap: {@code tool_choice} requires {@code tools} to be tested or
         * declared - on create nothing is tested yet, so a list naming
         * {@code tool_choice} without {@code tools} is rejected loudly
         * (400 {@code parameters_invalid}).
         *
         * @param declaredParameters the declared parameter names,
         *        {@code null} to unset
         * @return this builder
         * @throws IllegalArgumentException when the list exceeds 25 entries
         *         or names {@code tool_choice} without {@code tools}
         */
        public Builder declaredParameters(List<String> declaredParameters) {
            if (declaredParameters != null && declaredParameters.size() > 25) {
                throw new IllegalArgumentException(
                        "declared_parameters holds at most 25 entries, got " + declaredParameters.size());
            }
            OpenRouterPrivateEndpointRules.requireToolChoiceCovered(declaredParameters);
            this.declaredParameters = declaredParameters;
            return this;
        }

        /**
         * Sets the body field {@code declared_region} (optional) - attest
         * where this deployment processes data: {@code global} (not
         * regional), {@code europe} or {@code us}; unknown spellings pass
         * through verbatim. Self-declared and not verified by OpenRouter.
         *
         * <p>Three wire states (the {@code declaredZdr} pattern): leaving
         * this unset sends no key; a value is sent verbatim; {@code null}
         * emits an explicit JSON {@code null}. On create, unset and explicit
         * {@code null} both leave the attestation undeclared - the two are
         * indistinguishable in effect; the PATCH request
         * ({@code OpenRouterPrivateEndpointUpdateRequest}) distinguishes
         * them.
         *
         * @param declaredRegion the attestation, {@code null} to send an
         *        explicit JSON {@code null}
         * @return this builder
         */
        public Builder declaredRegion(String declaredRegion) {
            this.declaredRegion = declaredRegion;
            this.declaredRegionSet = true;
            return this;
        }

        /**
         * Sets the body field {@code declared_zdr} (optional) - attest that
         * this deployment retains no prompt or completion data. Self-declared
         * and not verified by OpenRouter.
         *
         * <p>Three wire states like {@link #declaredRegion(String)}: unset
         * omits the key; a value is sent ({@code false} is a set option and
         * is emitted); {@code null} emits an explicit JSON {@code null}. On
         * create, unset and explicit {@code null} both leave the attestation
         * undeclared.
         *
         * @param declaredZdr the attestation, {@code null} to send an
         *        explicit JSON {@code null}
         * @return this builder
         */
        public Builder declaredZdr(Boolean declaredZdr) {
            this.declaredZdr = declaredZdr;
            this.declaredZdrSet = true;
            return this;
        }

        /**
         * Sets the endpoint's completion cap. JSON field:
         * {@code settings.max_completion_tokens} (minimum 1, validated
         * loudly) - the create body has no top-level
         * {@code max_completion_tokens} field; the managed and full response
         * views surface their own top-level {@code max_completion_tokens}.
         *
         * <p>Tri-state like the settings fields: unset omits the key (the
         * value copied from the public endpoint or model is kept); a value is
         * sent; {@code null} is sent as explicit JSON {@code null} and keeps
         * the model default. A {@code max_completion_tokens} named inside
         * {@link #settings(OpenRouterPrivateEndpointSettingsUpdate)} wins
         * over this shorthand.
         *
         * @param maxCompletionTokens the completion cap, {@code null} to
         *        keep the model default
         * @return this builder
         */
        public Builder maxCompletionTokens(Integer maxCompletionTokens) {
            if (maxCompletionTokens != null && maxCompletionTokens < 1) {
                throw new IllegalArgumentException("max_completion_tokens must be at least 1");
            }
            this.maxCompletionTokens = maxCompletionTokens;
            this.maxCompletionTokensSet = true;
            return this;
        }

        /**
         * Sets the body field {@code pricing} (optional) - negotiated
         * per-token rates for requests routed to this endpoint (schema
         * {@code PrivateEndpointPricing}; build one with
         * {@link OpenRouterPrivateEndpointPricing#builder()}). Omitted cache
         * rates are inherited from the public endpoint of the same model and
         * provider. Replaces nothing on create - this is the initial value.
         *
         * @param pricing the pricing value, {@code null} to unset
         * @return this builder
         */
        public Builder pricing(OpenRouterPrivateEndpointPricing pricing) {
            this.pricing = pricing;
            return this;
        }

        /**
         * Sets the body field {@code settings} (optional) - capabilities and
         * limits over the ones copied from the public endpoint for this model
         * and provider (or from the model); omitted fields keep the copied
         * values (schema {@code UpdatePrivateEndpointSettingsRequest}; build
         * one with {@link OpenRouterPrivateEndpointSettingsUpdate#builder()}).
         *
         * @param settings the settings value, {@code null} to unset
         * @return this builder
         */
        public Builder settings(OpenRouterPrivateEndpointSettingsUpdate settings) {
            this.settings = settings;
            return this;
        }

        /**
         * Sets the body field {@code activate} (optional) - validate and
         * activate the new endpoint in the same call, using the given
         * workspace's BYOK credential for the live validation call (the
         * workspace must belong to your account). The endpoint is then
         * created active instead of as a draft.
         *
         * <p>Trap: on a failed validation the draft is kept and returned
         * with the failed checks (HTTP 422) - fix it and call
         * {@code /validate} + {@code /activate} instead of creating it again.
         *
         * @param workspaceId the workspace id (UUID) whose BYOK credential
         *        validates the endpoint, {@code null} to unset
         * @return this builder
         */
        public Builder activate(String workspaceId) {
            if (workspaceId != null && workspaceId.isEmpty()) {
                throw new IllegalArgumentException("activate workspace_id must not be empty");
            }
            this.activateWorkspaceId = workspaceId;
            return this;
        }

        /**
         * Sets the {@code Idempotency-Key} header (1-255 characters,
         * validated loudly) that makes retries safe: a repeated key with the
         * same request from the same organization returns the endpoint the
         * first request created instead of creating another; reusing it with
         * different fields answers 422 {@code idempotency_key_reused}.
         * Declared parameters are compared as a set (a retry that reorders or
         * repeats a name replays the endpoint); {@code activate} and
         * {@code settings} are not compared, and later edits to the endpoint
         * do not affect the comparison.
         *
         * @param idempotencyKey the idempotency key
         * @return this builder
         */
        public Builder idempotencyKey(String idempotencyKey) {
            if (idempotencyKey == null || idempotencyKey.isEmpty()
                    || idempotencyKey.length() > 255) {
                throw new IllegalArgumentException(
                        "idempotency key must be 1 to 255 characters");
            }
            if (idempotencyKey.indexOf('\r') >= 0 || idempotencyKey.indexOf('\n') >= 0) {
                throw new IllegalArgumentException(
                        "idempotency key must not contain CR or LF");
            }
            this.idempotencyKey = idempotencyKey;
            return this;
        }

        @Override
        public OpenRouterPrivateEndpointCreateRequest build() {
            if (modelPermalslug == null || modelPermalslug.isEmpty()) {
                throw new IllegalStateException(
                        "model_permaslug is required for private endpoint creation");
            }
            if (providerSlug == null || providerSlug.isEmpty()) {
                throw new IllegalStateException(
                        "provider_slug is required for private endpoint creation");
            }
            if (upstreamModelId == null || upstreamModelId.isEmpty()) {
                throw new IllegalStateException(
                        "upstream_model_id is required for private endpoint creation");
            }
            return new OpenRouterPrivateEndpointCreateRequest(this);
        }

        @Override
        public OpenRouterManagedPrivateEndpointResponse<OpenRouterPrivateEndpointCreateRequest> execute() {
            return client.sendRequest(build());
        }

        @Override
        public OpenRouterManagedPrivateEndpointResponse<OpenRouterPrivateEndpointCreateRequest> executeWithRetry() {
            return client.sendRequestWithRetry(build());
        }

        @Override
        public OpenRouterManagedPrivateEndpointResponse<OpenRouterPrivateEndpointCreateRequest> executeWithExponentialBackoff() {
            return client.sendRequestWithExponentialBackoff(build());
        }
    }
}
