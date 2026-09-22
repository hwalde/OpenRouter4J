package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.api.base.ApiClientSettings;
import de.entwicklertraining.api.base.ApiHttpConfiguration;
import de.entwicklertraining.openrouter4j.activity.OpenRouterAnalyticsMetaRequest;
import de.entwicklertraining.openrouter4j.activity.OpenRouterActivityRequest;
import de.entwicklertraining.openrouter4j.activity.OpenRouterAnalyticsQueryRequest;
import de.entwicklertraining.openrouter4j.audio.OpenRouterSpeechRequest;
import de.entwicklertraining.openrouter4j.audio.OpenRouterSttRequest;
import de.entwicklertraining.openrouter4j.byok.OpenRouterByokCreateRequest;
import de.entwicklertraining.openrouter4j.byok.OpenRouterByokDeleteRequest;
import de.entwicklertraining.openrouter4j.byok.OpenRouterByokGetRequest;
import de.entwicklertraining.openrouter4j.byok.OpenRouterByokListRequest;
import de.entwicklertraining.openrouter4j.byok.OpenRouterByokUpdateRequest;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest;
import de.entwicklertraining.openrouter4j.containers.OpenRouterContainerFileContentRequest;
import de.entwicklertraining.openrouter4j.containers.OpenRouterContainerFileGetRequest;
import de.entwicklertraining.openrouter4j.containers.OpenRouterContainerFileListRequest;
import de.entwicklertraining.openrouter4j.containers.OpenRouterContainerFilePromoteRequest;
import de.entwicklertraining.openrouter4j.containers.OpenRouterContainers;
import de.entwicklertraining.openrouter4j.credits.OpenRouterCreditsRequest;
import de.entwicklertraining.openrouter4j.decisions.OpenRouterDecisionsRequest;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsModelsRequest;
import de.entwicklertraining.openrouter4j.embeddings.OpenRouterEmbeddingsRequest;
import de.entwicklertraining.openrouter4j.files.OpenRouterFileContentRequest;
import de.entwicklertraining.openrouter4j.files.OpenRouterFileDeleteRequest;
import de.entwicklertraining.openrouter4j.files.OpenRouterFileGetRequest;
import de.entwicklertraining.openrouter4j.files.OpenRouterFileListRequest;
import de.entwicklertraining.openrouter4j.files.OpenRouterFileUploadRequest;
import de.entwicklertraining.openrouter4j.files.OpenRouterFiles;
import de.entwicklertraining.openrouter4j.generation.OpenRouterGenerationContentRequest;
import de.entwicklertraining.openrouter4j.generation.OpenRouterGenerationFeedbackRequest;
import de.entwicklertraining.openrouter4j.generation.OpenRouterGenerationRequest;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailAllKeyAssignmentsListRequest;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailAllMemberAssignmentsListRequest;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailCreateRequest;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailDeleteRequest;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailGetRequest;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailKeyAssignmentsListRequest;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailKeysAssignRequest;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailKeysUnassignRequest;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailMemberAssignmentsListRequest;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailMembersAssignRequest;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailMembersUnassignRequest;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailUpdateRequest;
import de.entwicklertraining.openrouter4j.guardrails.OpenRouterGuardrailsListRequest;
import de.entwicklertraining.openrouter4j.image.OpenRouterImageGenerationRequest;
import de.entwicklertraining.openrouter4j.image.OpenRouterImageModelEndpointsRequest;
import de.entwicklertraining.openrouter4j.image.OpenRouterImageModelsRequest;
import de.entwicklertraining.openrouter4j.keys.OpenRouterKeyCreateRequest;
import de.entwicklertraining.openrouter4j.keys.OpenRouterKeyDeleteRequest;
import de.entwicklertraining.openrouter4j.keys.OpenRouterKeyGetRequest;
import de.entwicklertraining.openrouter4j.keys.OpenRouterKeyUpdateRequest;
import de.entwicklertraining.openrouter4j.keys.OpenRouterKeysListRequest;
import de.entwicklertraining.openrouter4j.keys.OpenRouterCurrentKeyRequest;
import de.entwicklertraining.openrouter4j.messages.OpenRouterMessagesRequest;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelEndpointsRequest;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelRequest;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelsCountRequest;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelsListRequest;
import de.entwicklertraining.openrouter4j.models.OpenRouterUserModelsRequest;
import de.entwicklertraining.openrouter4j.observability.OpenRouterObservabilityDestinationCreateRequest;
import de.entwicklertraining.openrouter4j.observability.OpenRouterObservabilityDestinationDeleteRequest;
import de.entwicklertraining.openrouter4j.observability.OpenRouterObservabilityDestinationGetRequest;
import de.entwicklertraining.openrouter4j.observability.OpenRouterObservabilityDestinationUpdateRequest;
import de.entwicklertraining.openrouter4j.observability.OpenRouterObservabilityDestinationsListRequest;
import de.entwicklertraining.openrouter4j.oauth.OpenRouterAuthorizationCodeExchangeRequest;
import de.entwicklertraining.openrouter4j.oauth.OpenRouterCreateAuthorizationCodeRequest;
import de.entwicklertraining.openrouter4j.oauth.OpenRouterJwksRequest;
import de.entwicklertraining.openrouter4j.oauth.OpenRouterWorkloadIdentityExchangeRequest;
import de.entwicklertraining.openrouter4j.providers.OpenRouterProvidersRequest;
import de.entwicklertraining.openrouter4j.providers.OpenRouterZdrEndpointsRequest;
import de.entwicklertraining.openrouter4j.publicdata.OpenRouterAppRankingsRequest;
import de.entwicklertraining.openrouter4j.publicdata.OpenRouterBenchmarksRequest;
import de.entwicklertraining.openrouter4j.publicdata.OpenRouterRankingsDailyRequest;
import de.entwicklertraining.openrouter4j.publicdata.OpenRouterSessionCostRequest;
import de.entwicklertraining.openrouter4j.publicdata.OpenRouterTaskClassificationsRequest;
import de.entwicklertraining.openrouter4j.presets.OpenRouterPresetGetRequest;
import de.entwicklertraining.openrouter4j.presets.OpenRouterPresetUpsertFromChatRequest;
import de.entwicklertraining.openrouter4j.presets.OpenRouterPresetUpsertFromMessagesRequest;
import de.entwicklertraining.openrouter4j.presets.OpenRouterPresetUpsertFromResponsesRequest;
import de.entwicklertraining.openrouter4j.presets.OpenRouterPresetVersionGetRequest;
import de.entwicklertraining.openrouter4j.presets.OpenRouterPresetVersionsListRequest;
import de.entwicklertraining.openrouter4j.presets.OpenRouterPresetsListRequest;
import de.entwicklertraining.openrouter4j.rerank.OpenRouterRerankRequest;
import de.entwicklertraining.openrouter4j.responses.OpenRouterResponsesRequest;
import de.entwicklertraining.openrouter4j.scim.OpenRouterScimGroupMappingCreateRequest;
import de.entwicklertraining.openrouter4j.scim.OpenRouterScimGroupMappingDeleteRequest;
import de.entwicklertraining.openrouter4j.scim.OpenRouterScimGroupMappingGetRequest;
import de.entwicklertraining.openrouter4j.scim.OpenRouterScimGroupMappingUpdateRequest;
import de.entwicklertraining.openrouter4j.scim.OpenRouterScimGroupMappingsListRequest;
import de.entwicklertraining.openrouter4j.scim.OpenRouterScimGroupsListRequest;
import de.entwicklertraining.openrouter4j.scim.OpenRouterScimSyncJobCreateRequest;
import de.entwicklertraining.openrouter4j.scim.OpenRouterScimSyncJobGetRequest;
import de.entwicklertraining.openrouter4j.interns.OpenRouterInternChatRequest;
import de.entwicklertraining.openrouter4j.interns.OpenRouterInternCreateRequest;
import de.entwicklertraining.openrouter4j.interns.OpenRouterInternDeleteRequest;
import de.entwicklertraining.openrouter4j.interns.OpenRouterInternGetRequest;
import de.entwicklertraining.openrouter4j.interns.OpenRouterInternProvisionRequest;
import de.entwicklertraining.openrouter4j.interns.OpenRouterInternSuspendRequest;
import de.entwicklertraining.openrouter4j.interns.OpenRouterInternUpdateRequest;
import de.entwicklertraining.openrouter4j.interns.OpenRouterInternsListRequest;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultInternSecretDeleteRequest;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultInternSecretStoreRequest;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultInternSecretsListRequest;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultSecretCopyRequest;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultSecretDeleteRequest;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultSecretStoreRequest;
import de.entwicklertraining.openrouter4j.vault.OpenRouterVaultSecretsListRequest;
import de.entwicklertraining.openrouter4j.video.OpenRouterVideoContentRequest;
import de.entwicklertraining.openrouter4j.video.OpenRouterVideoGenerationRequest;
import de.entwicklertraining.openrouter4j.video.OpenRouterVideoJobRequest;
import de.entwicklertraining.openrouter4j.video.OpenRouterVideoModelsRequest;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterOrganizationMembersListRequest;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspacesListRequest;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspaceBudgetDeleteRequest;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspaceBudgetGetRequest;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspaceBudgetsListRequest;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspaceBudgetUpsertRequest;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspaceCreateRequest;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspaceDeleteRequest;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspaceGetRequest;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspaceMembersAddRequest;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspaceMembersListRequest;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspaceMembersRemoveRequest;
import de.entwicklertraining.openrouter4j.workspace.OpenRouterWorkspaceUpdateRequest;

// Import exception classes
import static de.entwicklertraining.api.base.ApiClient.HTTP_400_RequestRejectedException;
import static de.entwicklertraining.api.base.ApiClient.HTTP_403_PermissionDeniedException;
import static de.entwicklertraining.api.base.ApiClient.HTTP_404_NotFoundException;
import static de.entwicklertraining.api.base.ApiClient.HTTP_429_RateLimitOrQuotaException;
import static de.entwicklertraining.api.base.ApiClient.HTTP_500_ServerErrorException;
import static de.entwicklertraining.api.base.ApiClient.HTTP_503_ServerUnavailableException;
import static de.entwicklertraining.api.base.ApiClient.HTTP_504_ServerTimeoutException;

/**
 * Ein Client für die OpenRouter-API mit integrierter Rate-Limit-Prüfung.
 * OpenRouter verwendet Bearer Token Authentication im Authorization Header.
 *
 * <p>The client automatically reads the API key from the OPENROUTER_API_KEY environment variable
 * if not explicitly provided via {@link ApiHttpConfiguration}.
 */
public final class OpenRouterClient extends ApiClient {

    private static final String DEFAULT_BASE_URL = "https://openrouter.ai/api/v1";

    private volatile OpenRouterAppAttribution appAttribution;

    /**
     * Creates a new OpenRouterClient with default settings.
     * The API key is read from the OPENROUTER_API_KEY environment variable.
     */
    public OpenRouterClient() {
        this(ApiClientSettings.builder().build(), null, DEFAULT_BASE_URL);
    }

    /**
     * Creates a new OpenRouterClient with custom settings.
     * The API key is read from the OPENROUTER_API_KEY environment variable.
     *
     * @param settings Client settings for retry behavior and timeouts
     */
    public OpenRouterClient(ApiClientSettings settings) {
        this(settings, null, DEFAULT_BASE_URL);
    }

    /**
     * Creates a new OpenRouterClient with custom settings and HTTP configuration.
     *
     * @param settings Client settings for retry behavior and timeouts
     * @param httpConfig HTTP configuration including authentication headers
     */
    public OpenRouterClient(ApiClientSettings settings, ApiHttpConfiguration httpConfig) {
        this(settings, httpConfig, DEFAULT_BASE_URL);
    }

    /**
     * Creates a new OpenRouterClient with custom settings, HTTP configuration, and base URL.
     *
     * @param settings Client settings for retry behavior and timeouts
     * @param httpConfig HTTP configuration including authentication headers (can be null)
     * @param customBaseUrl Custom base URL for the API
     */
    public OpenRouterClient(ApiClientSettings settings, ApiHttpConfiguration httpConfig, String customBaseUrl) {
        super(settings, buildHttpConfig(httpConfig));

        setBaseUrl(customBaseUrl);

        // Register OpenRouter-specific HTTP status code exceptions
        registerStatusCodeException(400, HTTP_400_RequestRejectedException.class, "HTTP 400 (Bad Request)", false);
        registerStatusCodeException(403, HTTP_403_PermissionDeniedException.class, "HTTP 403 (Forbidden)", false);
        registerStatusCodeException(404, HTTP_404_NotFoundException.class, "HTTP 404 (Not Found)", false);
        registerStatusCodeException(429, HTTP_429_RateLimitOrQuotaException.class, "HTTP 429 (Rate Limited)", true);
        registerStatusCodeException(500, HTTP_500_ServerErrorException.class, "HTTP 500 (Internal Server Error)", true);
        registerStatusCodeException(503, HTTP_503_ServerUnavailableException.class, "HTTP 503 (Service Unavailable)", true);
        registerStatusCodeException(504, HTTP_504_ServerTimeoutException.class, "HTTP 504 (Gateway Timeout)", false);
    }

    /**
     * Builds the HTTP configuration, adding the API key from environment variable if not already set.
     */
    private static ApiHttpConfiguration buildHttpConfig(ApiHttpConfiguration existingConfig) {
        // Check if we already have an Authorization header
        if (existingConfig != null && existingConfig.getGlobalHeaders().containsKey("Authorization")) {
            return existingConfig;
        }

        // Try to get API key from environment variable
        String apiKey = System.getenv("OPENROUTER_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            // No API key available - return existing config or empty config
            return existingConfig != null ? existingConfig : new ApiHttpConfiguration();
        }

        // Build new config with API key
        ApiHttpConfiguration.Builder builder = existingConfig != null
            ? existingConfig.toBuilder()
            : ApiHttpConfiguration.builder();

        return builder
            .header("Authorization", "Bearer " + apiKey)
            .build();
    }

    public OpenRouterChat chat() {
        return new OpenRouterChat(this);
    }

    /**
     * Reads the credit balance of the account:
     * GET /credits (management key required).
     *
     * @return the starting point for the request
     */
    public OpenRouterCreditsRequest.Builder credits() {
        return new OpenRouterCreditsRequest.Builder(this);
    }

    /**
     * Lists the full OpenRouter model catalog:
     * GET /models.
     *
     * @return the starting point for the request
     */
    public OpenRouterModelsListRequest.Builder models() {
        return new OpenRouterModelsListRequest.Builder(this);
    }

    /**
     * Counts the models in the catalog:
     * GET /models/count.
     *
     * @return the starting point for the request
     */
    public OpenRouterModelsCountRequest.Builder modelsCount() {
        return new OpenRouterModelsCountRequest.Builder(this);
    }

    /**
     * Lists the models the authenticated account may use:
     * GET /models/user.
     *
     * @return the starting point for the request
     */
    public OpenRouterUserModelsRequest.Builder userModels() {
        return new OpenRouterUserModelsRequest.Builder(this);
    }

    /**
     * Fetches one model from the catalog:
     * GET /model/{author}/{slug}.
     *
     * @param modelId the full model id ({@code author/slug}); the first slash splits it
     * @return the starting point for the request
     */
    public OpenRouterModelRequest.Builder model(String modelId) {
        int slash = modelId != null ? modelId.indexOf('/') : -1;
        if (modelId == null || modelId.isEmpty() || slash <= 0 || slash == modelId.length() - 1) {
            throw new IllegalArgumentException(
                    "modelId must be of the form \"author/slug\", got: " + modelId);
        }
        return new OpenRouterModelRequest.Builder(this,
                modelId.substring(0, slash), modelId.substring(slash + 1));
    }

    /**
     * Lists the serving endpoints of one model:
     * GET /models/{author}/{slug}/endpoints.
     *
     * @param modelId the full model id ({@code author/slug}); the first slash splits it
     * @return the starting point for the request
     */
    public OpenRouterModelEndpointsRequest.Builder modelEndpoints(String modelId) {
        int slash = modelId != null ? modelId.indexOf('/') : -1;
        if (modelId == null || modelId.isEmpty() || slash <= 0 || slash == modelId.length() - 1) {
            throw new IllegalArgumentException(
                    "modelId must be of the form \"author/slug\", got: " + modelId);
        }
        return new OpenRouterModelEndpointsRequest.Builder(this,
                modelId.substring(0, slash), modelId.substring(slash + 1));
    }

    /**
     * Fetches the request/usage metadata of one generation:
     * GET /generation?id=... (management key required). OpenRouter needs a few
     * seconds after a completion before the metadata is queryable - querying
     * earlier fails with HTTP 404.
     *
     * @param generationId the generation id ({@code gen-...})
     * @return the starting point for the request
     */
    public OpenRouterGenerationRequest.Builder generation(String generationId) {
        return new OpenRouterGenerationRequest.Builder(this, generationId);
    }

    /**
     * Fetches the stored prompt and completion of one generation:
     * GET /generation/content?id=... (management key required). OpenRouter
     * needs a few seconds after a completion before the content is queryable -
     * querying earlier fails with HTTP 404.
     *
     * @param generationId the generation id ({@code gen-...})
     * @return the starting point for the request
     */
    public OpenRouterGenerationContentRequest.Builder generationContent(String generationId) {
        return new OpenRouterGenerationContentRequest.Builder(this, generationId);
    }

    /**
     * Submits structured feedback on one generation:
     * POST /generation/feedback (management key required).
     *
     * @return the starting point for the request
     */
    public OpenRouterGenerationFeedbackRequest.Builder generationFeedback() {
        return new OpenRouterGenerationFeedbackRequest.Builder(this);
    }

    /**
     * Reads the usage activity of the account:
     * GET /activity (management key required).
     *
     * @return the starting point for the request
     */
    public OpenRouterActivityRequest.Builder activity() {
        return new OpenRouterActivityRequest.Builder(this);
    }

    /**
     * Queries the account's usage analytics:
     * POST /analytics/query (management key required).
     *
     * @return the starting point for the request
     */
    public OpenRouterAnalyticsQueryRequest.Builder analyticsQuery() {
        return new OpenRouterAnalyticsQueryRequest.Builder(this);
    }

    /**
     * Reads the metrics, dimensions, filter operators and granularities the
     * analytics query engine accepts:
     * GET /analytics/meta (management key required).
     *
     * @return the starting point for the request
     */
    public OpenRouterAnalyticsMetaRequest.Builder analyticsMeta() {
        return new OpenRouterAnalyticsMetaRequest.Builder(this);
    }

    /**
     * Lists all providers integrated on OpenRouter:
     * GET /providers - the discovery counterpart of the provider-routing
     * options.
     *
     * @return the starting point for the request
     */
    public OpenRouterProvidersRequest.Builder providers() {
        return new OpenRouterProvidersRequest.Builder(this);
    }

    /**
     * Previews the impact of zero-data-retention on the available endpoints:
     * GET /endpoints/zdr - the documented way to check before
     * {@code zdr(true)} that enough ZDR endpoints remain.
     *
     * @return the starting point for the request
     */
    public OpenRouterZdrEndpointsRequest.Builder zdrEndpoints() {
        return new OpenRouterZdrEndpointsRequest.Builder(this);
    }

    /**
     * Reads the unified benchmark data:
     * GET /benchmarks (works with any valid API key).
     *
     * @return the starting point for the request
     */
    public OpenRouterBenchmarksRequest.Builder benchmarks() {
        return new OpenRouterBenchmarksRequest.Builder(this);
    }

    /**
     * Reads the public market datasets: top apps by token usage
     * (GET /datasets/app-rankings), daily token totals for the top 50 models
     * (GET /datasets/rankings-daily) and cost per session by harness and
     * model (GET /datasets/session-cost).
     *
     * @return the starting point for the dataset requests
     */
    public OpenRouterDatasets datasets() {
        return new OpenRouterDatasets(this);
    }

    /**
     * Reads the task-classification market share:
     * GET /classifications/task (works with any valid API key).
     *
     * @return the starting point for the request
     */
    public OpenRouterTaskClassificationsRequest.Builder taskClassifications() {
        return new OpenRouterTaskClassificationsRequest.Builder(this);
    }

    /**
     * Reads the data of the API key making the call:
     * GET /key (works with a normal inference key).
     *
     * @return the starting point for the request
     */
    public OpenRouterCurrentKeyRequest.Builder currentKey() {
        return new OpenRouterCurrentKeyRequest.Builder(this);
    }

    /**
     * Manages the API keys of the account:
     * GET /keys, POST /keys, GET /keys/{hash}, PATCH /keys/{hash},
     * DELETE /keys/{hash} (management key required).
     *
     * @return the starting point for the key-management requests
     */
    public OpenRouterKeys keys() {
        return new OpenRouterKeys(this);
    }

    /**
     * Manages the workspaces and budgets of the account:
     * GET/POST /workspaces, GET/PATCH/DELETE /workspaces/{id},
     * the members endpoints under /workspaces/{id}/members and the budget
     * endpoints under /workspaces/{ref}/budgets (management key required).
     *
     * @return the starting point for the workspace-management requests
     */
    public OpenRouterWorkspaces workspaces() {
        return new OpenRouterWorkspaces(this);
    }

    /**
     * Reads the organization of the account:
     * GET /organization/members (management key required).
     *
     * @return the starting point for the organization requests
     */
    public OpenRouterOrganization organization() {
        return new OpenRouterOrganization(this);
    }

    /**
     * Manages the guardrails of the account:
     * GET/POST /guardrails, GET/PATCH/DELETE /guardrails/{id} and the
     * assignment endpoints under /guardrails/assignments and
     * /guardrails/{id}/assignments (management key required).
     *
     * @return the starting point for the guardrail requests
     */
    public OpenRouterGuardrails guardrails() {
        return new OpenRouterGuardrails(this);
    }

    /**
     * Manages the BYOK provider credentials of the account:
     * GET/POST /byok, GET/PATCH/DELETE /byok/{id} (management key required).
     *
     * @return the starting point for the BYOK requests
     */
    public OpenRouterByok byok() {
        return new OpenRouterByok(this);
    }

    /**
     * Manages the observability destinations the traces are broadcast to:
     * GET/POST /observability/destinations, GET/PATCH/DELETE
     * /observability/destinations/{id} (management key required). Requests
     * opt into tracing via the {@code trace} request object
     * ({@code OpenRouterTraceConfig}).
     *
     * @return the starting point for the observability requests
     */
    public OpenRouterObservability observability() {
        return new OpenRouterObservability(this);
    }

    /**
     * The Anthropic Messages API on OpenRouter:
     * POST /messages (top-level {@code system}, {@code max_tokens} required
     * by Anthropic semantics, SSE streaming with the Anthropic event model).
     *
     * @return the starting point for the request
     */
    public OpenRouterMessagesRequest.Builder messages() {
        return new OpenRouterMessagesRequest.Builder(this);
    }

    /**
     * The OpenAI Responses API on OpenRouter:
     * POST /responses (the successor surface for features that only exist
     * there, e.g. full {@code openrouter:tool_search} support,
     * {@code openrouter:apply_patch}, subagent function inheritance). SSE
     * streaming with the Responses event model.
     *
     * @return the starting point for the request
     */
    public OpenRouterResponsesRequest.Builder responses() {
        return new OpenRouterResponsesRequest.Builder(this);
    }

    /**
     * Reads and manages the presets of the account:
     * GET /presets, GET /presets/{slug}, the version endpoints under
     * /presets/{slug}/versions and the create/update routes
     * POST /presets/{slug}/chat/completions, /messages and /responses
     * (create routes require a management key; they are NOT inference
     * routes).
     *
     * @return the starting point for the preset requests
     */
    public OpenRouterPresets presets() {
        return new OpenRouterPresets(this);
    }

    /**
     * Manages the SCIM provisioning of the organization:
     * GET/POST /scim/group-mappings, GET/PATCH/DELETE
     * /scim/group-mappings/{id}, GET /scim/groups and the sync-job endpoints
     * under /scim/sync-jobs (management key required for all of them).
     *
     * @return the starting point for the SCIM requests
     */
    public OpenRouterScim scim() {
        return new OpenRouterScim(this);
    }

    /**
     * Manages and drives the interns of the account - the OpenRouter "Ori"
     * programme:
     * GET/POST /interns, GET/PATCH/DELETE /interns/{internId},
     * POST /interns/{internId}/provision, POST /interns/{internId}/suspend
     * and the streaming chat POST /interns/{internId}/chat/completions.
     * Every path answers 404 for keys outside the interns programme.
     *
     * @return the starting point for the intern requests
     */
    public OpenRouterInterns interns() {
        return new OpenRouterInterns(this);
    }

    /**
     * Manages the vault secrets of the account - host-bound secrets for the
     * active workspace and for interns:
     * GET /vault/secrets, PUT/DELETE /vault/secrets/{name}, the intern
     * routes under /vault/interns/{internId}/secrets and the copy route
     * POST /vault/interns/{internId}/secrets/copy. Responses are metadata
     * only - the plaintext value is never returned. Every vault route,
     * including the list, answers 404 outside the Intern API programme.
     *
     * @return the starting point for the vault requests
     */
    public OpenRouterVault vault() {
        return new OpenRouterVault(this);
    }

    /**
     * Speech-to-text and text-to-speech on the dedicated Audio API:
     * POST /audio/transcriptions (STT) and POST /audio/speech (TTS, binary
     * audio response).
     *
     * @return the starting point for the audio requests
     */
    public OpenRouterAudio audio() {
        return new OpenRouterAudio(this);
    }

    /**
     * Async video generation with polling:
     * POST /videos, GET /videos/{jobId},
     * GET /videos/{jobId}/content and the video model listing
     * (GET /videos/models).
     *
     * @return the starting point for the video requests
     */
    public OpenRouterVideos videos() {
        return new OpenRouterVideos(this);
    }

    /**
     * Image generation on the dedicated Image API:
     * POST /images plus the image model listings
     * (GET /images/models, GET /images/models/{author}/{slug}/endpoints).
     *
     * @return the starting point for the image requests
     */
    public OpenRouterImages images() {
        return new OpenRouterImages(this);
    }

    /**
     * Files API: upload files, list them, read their metadata, download
     * their content and delete them again (POST/GET/DELETE /files*).
     *
     * @return the starting point for the Files API requests
     */
    public OpenRouterFiles files() {
        return new OpenRouterFiles(this);
    }

    /**
     * Code-execution container files: list the files a bash/shell server
     * tool wrote into a sandbox container, read one file's metadata,
     * download its content and promote it into the workspace's durable
     * document storage (GET/POST /containers/{container_id}/files*).
     *
     * @return the starting point for the container file requests
     */
    public OpenRouterContainers containers() {
        return new OpenRouterContainers(this);
    }

    /**
     * Creates embedding vectors:
     * POST /embeddings.
     *
     * @return the starting point for the request
     */
    public OpenRouterEmbeddingsRequest.Builder embeddings() {
        return new OpenRouterEmbeddingsRequest.Builder(this);
    }

    /**
     * Lists the embedding models of the catalog:
     * GET /embeddings/models.
     *
     * @return the starting point for the request
     */
    public OpenRouterEmbeddingsModelsRequest.Builder embeddingsModels() {
        return new OpenRouterEmbeddingsModelsRequest.Builder(this);
    }

    /**
     * Reranks documents against a query:
     * POST /rerank.
     *
     * @return the starting point for the request
     */
    public OpenRouterRerankRequest.Builder rerank() {
        return new OpenRouterRerankRequest.Builder(this);
    }

    /**
     * Creates an OAuth authorization code:
     * POST /auth/keys/code. Step one of the OAuth authorization-code flow
     * with PKCE; pair it with {@link de.entwicklertraining.openrouter4j.oauth.OpenRouterPkce}.
     *
     * @return the starting point for the request
     */
    public OpenRouterCreateAuthorizationCodeRequest.Builder createAuthorizationCode() {
        return new OpenRouterCreateAuthorizationCodeRequest.Builder(this);
    }

    /**
     * Exchanges an OAuth authorization code for an API key:
     * POST /auth/keys. Final step of the OAuth authorization-code flow with
     * PKCE.
     *
     * @return the starting point for the request
     */
    public OpenRouterAuthorizationCodeExchangeRequest.Builder exchangeAuthorizationCode() {
        return new OpenRouterAuthorizationCodeExchangeRequest.Builder(this);
    }

    /**
     * Exchanges a workload identity token (identity-provider JWT) for a
     * short-lived OpenRouter access token:
     * POST /oauth/token (RFC 8693, urlencoded form body).
     *
     * @return the starting point for the request
     */
    public OpenRouterWorkloadIdentityExchangeRequest.Builder exchangeWorkloadIdentityToken() {
        return new OpenRouterWorkloadIdentityExchangeRequest.Builder(this);
    }

    /**
     * Reads the public signing keys of OpenRouter access tokens:
     * GET /oauth/jwks - an RFC 7517 JWK Set. The counterpart needed to verify
     * the access tokens the exchange endpoints hand out (notably
     * {@link #exchangeWorkloadIdentityToken()}, which returns a short-lived
     * access token whose signature can only be checked against these keys).
     *
     * @return the starting point for the request
     */
    public OpenRouterJwksRequest.Builder oauthJwks() {
        return new OpenRouterJwksRequest.Builder(this);
    }

    /**
     * Submits a Decisions request to the Decisions router (alpha):
     * POST /api/alpha/decisions - evaluate content against typed questions
     * (boolean {@code noul}, multi-class {@code choice}, ordered
     * {@code score}).
     *
     * <p>Trap: the endpoint lives in OpenRouter's alpha namespace, outside
     * the {@code /api/v1} base of every other endpoint. This builder routes
     * through an internal client whose base URL is the API host's
     * {@code /api/alpha} namespace (derived from this client's base URL); the
     * alpha surface may change or disappear without a major version of the
     * API.
     *
     * @return the starting point for the request
     */
    public OpenRouterDecisionsRequest.Builder decisions() {
        return new OpenRouterDecisionsRequest.Builder(alphaClient());
    }

    private volatile OpenRouterClient alphaClient;

    /**
     * The internal client for the {@code /api/alpha} namespace, created
     * lazily on first use with the same settings, HTTP configuration and app
     * attribution as this client.
     *
     * @return the alpha-namespace client
     */
    OpenRouterClient alphaClient() {
        OpenRouterClient result = alphaClient;
        if (result == null) {
            synchronized (this) {
                result = alphaClient;
                if (result == null) {
                    result = new OpenRouterClient(settings, httpConfig, alphaBaseUrl());
                    result.appAttribution(this.appAttribution);
                    alphaClient = result;
                }
            }
        }
        return result;
    }

    String alphaBaseUrl() {
        String base = getBaseUrl();
        if (base.endsWith("/api/alpha")) {
            return base;
        }
        if (base.endsWith("/api/v1")) {
            return base.substring(0, base.length() - "/api/v1".length()) + "/api/alpha";
        }
        return base + "/api/alpha";
    }

    /**
     * Configures app attribution headers (app URL, display name, marketplace
     * categories) once on the client; they are then sent with every request.
     * <p>
     * Per-request configuration via the
     * {@code httpReferer}/{@code appTitle}/{@code appCategories} builder methods
     * wins over this client-level default for that single request.
     * <p>
     * JSON fields: none - the values travel as the {@code HTTP-Referer},
     * {@code X-OpenRouter-Title} (legacy alias {@code X-Title}) and
     * {@code X-OpenRouter-Categories} (max 2 categories) HTTP headers.
     *
     * @param attribution the attribution data, or {@code null} to remove a previously
     *        configured client-level attribution
     * @return this client instance
     */
    public OpenRouterClient appAttribution(OpenRouterAppAttribution attribution) {
        this.appAttribution = attribution;
        return this;
    }

    /**
     * @return the client-level app attribution, or {@code null} when none is configured
     */
    public OpenRouterAppAttribution appAttribution() {
        return appAttribution;
    }

    /**
     * Entry point for the dedicated Audio API: transcribe audio to text
     * (STT) and synthesize speech from text (TTS).
     */
    public static class OpenRouterAudio {
        private final OpenRouterClient client;

        /**
         * @param client the client used to send the requests
         */
        public OpenRouterAudio(OpenRouterClient client) {
            this.client = client;
        }

        /**
         * Transcribes audio to text:
         * POST /audio/transcriptions (JSON body with base64 audio, or
         * multipart form with a file part).
         *
         * @return the starting point for the request
         */
        public OpenRouterSttRequest.Builder transcriptions() {
            return new OpenRouterSttRequest.Builder(client);
        }

        /**
         * Synthesizes speech from text:
         * POST /audio/speech (binary audio response, mp3 or pcm).
         *
         * @return the starting point for the request
         */
        public OpenRouterSpeechRequest.Builder speech() {
            return new OpenRouterSpeechRequest.Builder(client);
        }
    }

    /**
     * Entry point for the async video generation API: submit jobs, poll them
     * and download the finished video bytes.
     */
    public static class OpenRouterVideos {
        private final OpenRouterClient client;

        /**
         * @param client the client used to send the requests
         */
        public OpenRouterVideos(OpenRouterClient client) {
            this.client = client;
        }

        /**
         * Submits an async video generation job:
         * POST /videos (responds 202 with the job id and polling URL).
         *
         * @return the starting point for the request
         */
        public OpenRouterVideoGenerationRequest.Builder generate() {
            return new OpenRouterVideoGenerationRequest.Builder(client);
        }

        /**
         * Polls the status of one video generation job:
         * GET /videos/{jobId}.
         *
         * @param jobId the job id ({@code job-...}) from the submission response
         * @return the starting point for the request
         */
        public OpenRouterVideoJobRequest.Builder job(String jobId) {
            return new OpenRouterVideoJobRequest.Builder(client, jobId);
        }

        /**
         * Downloads the generated video content of one job:
         * GET /videos/{jobId}/content (binary video bytes, typically mp4).
         *
         * @param jobId the job id ({@code job-...}) of a completed job
         * @return the starting point for the request
         */
        public OpenRouterVideoContentRequest.Builder jobContent(String jobId) {
            return new OpenRouterVideoContentRequest.Builder(client, jobId);
        }

        /**
         * Lists the video generation models of the catalog:
         * GET /videos/models.
         *
         * @return the starting point for the request
         */
        public OpenRouterVideoModelsRequest.Builder models() {
            return new OpenRouterVideoModelsRequest.Builder(client);
        }
    }

    /**
     * Entry point for the dedicated Image API: generate images and discover
     * the image generation models.
     */
    public static class OpenRouterImages {
        private final OpenRouterClient client;

        /**
         * @param client the client used to send the requests
         */
        public OpenRouterImages(OpenRouterClient client) {
            this.client = client;
        }

        /**
         * Generates an image:
         * POST /images.
         *
         * @return the starting point for the request
         */
        public OpenRouterImageGenerationRequest.Builder generate() {
            return new OpenRouterImageGenerationRequest.Builder(client);
        }

        /**
         * Lists the image generation models of the catalog:
         * GET /images/models.
         *
         * @return the starting point for the request
         */
        public OpenRouterImageModelsRequest.Builder models() {
            return new OpenRouterImageModelsRequest.Builder(client);
        }

        /**
         * Lists the serving endpoints of one image model:
         * GET /images/models/{author}/{slug}/endpoints.
         *
         * @param modelId the full model id ({@code author/slug}); the first slash splits it
         * @return the starting point for the request
         */
        public OpenRouterImageModelEndpointsRequest.Builder modelEndpoints(String modelId) {
            int slash = modelId != null ? modelId.indexOf('/') : -1;
            if (modelId == null || modelId.isEmpty() || slash <= 0 || slash == modelId.length() - 1) {
                throw new IllegalArgumentException(
                        "modelId must be of the form \"author/slug\", got: " + modelId);
            }
            return new OpenRouterImageModelEndpointsRequest.Builder(client,
                    modelId.substring(0, slash), modelId.substring(slash + 1));
        }
    }

    /**
     * Entry point for the public market datasets: app rankings, daily model
     * rankings and per-session costs.
     */
    public static class OpenRouterDatasets {
        private final OpenRouterClient client;

        /**
         * @param client the client used to send the requests
         */
        public OpenRouterDatasets(OpenRouterClient client) {
            this.client = client;
        }

        /**
         * Reads the top public apps ranked by token usage:
         * GET /datasets/app-rankings.
         *
         * @return the starting point for the request
         */
        public OpenRouterAppRankingsRequest.Builder appRankings() {
            return new OpenRouterAppRankingsRequest.Builder(client);
        }

        /**
         * Reads the daily token totals for the top 50 public models:
         * GET /datasets/rankings-daily.
         *
         * @return the starting point for the request
         */
        public OpenRouterRankingsDailyRequest.Builder rankingsDaily() {
            return new OpenRouterRankingsDailyRequest.Builder(client);
        }

        /**
         * Reads the aggregated cost per session by harness and model:
         * GET /datasets/session-cost.
         *
         * @return the starting point for the request
         */
        public OpenRouterSessionCostRequest.Builder sessionCost() {
            return new OpenRouterSessionCostRequest.Builder(client);
        }
    }

    /**
     * Entry point for the API key management endpoints: list, create, get,
     * update and delete keys.
     */
    public static class OpenRouterKeys {
        private final OpenRouterClient client;

        /**
         * @param client the client used to send the requests
         */
        public OpenRouterKeys(OpenRouterClient client) {
            this.client = client;
        }

        /**
         * Lists the API keys of the account:
         * GET /keys (management key required).
         *
         * @return the starting point for the request
         */
        public OpenRouterKeysListRequest.Builder list() {
            return new OpenRouterKeysListRequest.Builder(client);
        }

        /**
         * Creates a new API key:
         * POST /keys (management key required). The plaintext key travels
         * only in the response - treat it as a secret.
         *
         * @return the starting point for the request
         */
        public OpenRouterKeyCreateRequest.Builder create() {
            return new OpenRouterKeyCreateRequest.Builder(client);
        }

        /**
         * Gets a single API key by hash:
         * GET /keys/{hash} (management key required).
         *
         * @param hash the hash identifier of the key
         * @return the starting point for the request
         */
        public OpenRouterKeyGetRequest.Builder get(String hash) {
            return new OpenRouterKeyGetRequest.Builder(client, hash);
        }

        /**
         * Updates an API key:
         * PATCH /keys/{hash} (management key required).
         *
         * @param hash the hash identifier of the key
         * @return the starting point for the request
         */
        public OpenRouterKeyUpdateRequest.Builder update(String hash) {
            return new OpenRouterKeyUpdateRequest.Builder(client, hash);
        }

        /**
         * Deletes an API key:
         * DELETE /keys/{hash} (management key required, permanent).
         *
         * @param hash the hash identifier of the key
         * @return the starting point for the request
         */
        public OpenRouterKeyDeleteRequest.Builder delete(String hash) {
            return new OpenRouterKeyDeleteRequest.Builder(client, hash);
        }
    }

    /**
     * Facade for the workspace-management endpoints (management key
     * required): workspaces, their members and their budgets.
     */
    public static class OpenRouterWorkspaces {
        private final OpenRouterClient client;

        /**
         * @param client the client used to send the requests
         */
        public OpenRouterWorkspaces(OpenRouterClient client) {
            this.client = client;
        }

        /**
         * Lists the workspaces of the account:
         * GET /workspaces (management key required).
         *
         * @return the starting point for the request
         */
        public OpenRouterWorkspacesListRequest.Builder list() {
            return new OpenRouterWorkspacesListRequest.Builder(client);
        }

        /**
         * Creates a new workspace:
         * POST /workspaces (management key required).
         *
         * @return the starting point for the request
         */
        public OpenRouterWorkspaceCreateRequest.Builder create() {
            return new OpenRouterWorkspaceCreateRequest.Builder(client);
        }

        /**
         * Gets a single workspace by id:
         * GET /workspaces/{id} (management key required).
         *
         * @param id the id (UUID) of the workspace
         * @return the starting point for the request
         */
        public OpenRouterWorkspaceGetRequest.Builder get(String id) {
            return new OpenRouterWorkspaceGetRequest.Builder(client, id);
        }

        /**
         * Updates a workspace (renaming via {@code slug} included):
         * PATCH /workspaces/{id} (management key required).
         *
         * @param id the id (UUID) of the workspace
         * @return the starting point for the request
         */
        public OpenRouterWorkspaceUpdateRequest.Builder update(String id) {
            return new OpenRouterWorkspaceUpdateRequest.Builder(client, id);
        }

        /**
         * Deletes a workspace (permanent; deleting the default workspace
         * needs {@code confirmDefaultWorkspaceDeletion(true)}):
         * DELETE /workspaces/{id} (management key required).
         *
         * @param id the id (UUID) of the workspace
         * @return the starting point for the request
         */
        public OpenRouterWorkspaceDeleteRequest.Builder delete(String id) {
            return new OpenRouterWorkspaceDeleteRequest.Builder(client, id);
        }

        /**
         * Lists the members of a workspace:
         * GET /workspaces/{id}/members (management key required).
         *
         * @param id the id (UUID) of the workspace
         * @return the starting point for the request
         */
        public OpenRouterWorkspaceMembersListRequest.Builder members(String id) {
            return new OpenRouterWorkspaceMembersListRequest.Builder(client, id);
        }

        /**
         * Adds organization members to a workspace:
         * POST /workspaces/{id}/members/add (management key required).
         *
         * @param id the id (UUID) of the workspace
         * @return the starting point for the request
         */
        public OpenRouterWorkspaceMembersAddRequest.Builder addMembers(String id) {
            return new OpenRouterWorkspaceMembersAddRequest.Builder(client, id);
        }

        /**
         * Removes members from a workspace:
         * POST /workspaces/{id}/members/remove (management key required).
         *
         * @param id the id (UUID) of the workspace
         * @return the starting point for the request
         */
        public OpenRouterWorkspaceMembersRemoveRequest.Builder removeMembers(String id) {
            return new OpenRouterWorkspaceMembersRemoveRequest.Builder(client, id);
        }

        /**
         * Lists the budgets of a workspace:
         * GET /workspaces/{ref}/budgets (management key required).
         *
         * @param workspaceRef the workspace id or slug
         * @return the starting point for the request
         */
        public OpenRouterWorkspaceBudgetsListRequest.Builder budgets(String workspaceRef) {
            return new OpenRouterWorkspaceBudgetsListRequest.Builder(client, workspaceRef);
        }

        /**
         * Gets one budget of a workspace:
         * GET /workspaces/{ref}/budgets/{interval} (management key required).
         *
         * @param workspaceRef the workspace id or slug
         * @param interval the budget interval ({@code daily}, {@code weekly},
         *                 {@code monthly} or {@code lifetime})
         * @return the starting point for the request
         */
        public OpenRouterWorkspaceBudgetGetRequest.Builder budget(String workspaceRef, String interval) {
            return new OpenRouterWorkspaceBudgetGetRequest.Builder(client, workspaceRef, interval);
        }

        /**
         * Creates or updates one budget of a workspace:
         * PUT /workspaces/{ref}/budgets/{interval} (management key required).
         *
         * @param workspaceRef the workspace id or slug
         * @param interval the budget interval ({@code daily}, {@code weekly},
         *                 {@code monthly} or {@code lifetime})
         * @return the starting point for the request
         */
        public OpenRouterWorkspaceBudgetUpsertRequest.Builder upsertBudget(String workspaceRef, String interval) {
            return new OpenRouterWorkspaceBudgetUpsertRequest.Builder(client, workspaceRef, interval);
        }

        /**
         * Deletes one budget of a workspace:
         * DELETE /workspaces/{ref}/budgets/{interval} (management key required).
         *
         * @param workspaceRef the workspace id or slug
         * @param interval the budget interval ({@code daily}, {@code weekly},
         *                 {@code monthly} or {@code lifetime})
         * @return the starting point for the request
         */
        public OpenRouterWorkspaceBudgetDeleteRequest.Builder deleteBudget(String workspaceRef, String interval) {
            return new OpenRouterWorkspaceBudgetDeleteRequest.Builder(client, workspaceRef, interval);
        }
    }

    /**
     * Facade for the organization endpoints (management key required).
     */
    public static class OpenRouterOrganization {
        private final OpenRouterClient client;

        /**
         * @param client the client used to send the requests
         */
        public OpenRouterOrganization(OpenRouterClient client) {
            this.client = client;
        }

        /**
         * Lists the members of the organization:
         * GET /organization/members (management key required).
         *
         * @return the starting point for the request
         */
        public OpenRouterOrganizationMembersListRequest.Builder members() {
            return new OpenRouterOrganizationMembersListRequest.Builder(client);
        }
    }

    /**
     * Facade for the guardrail endpoints (management key required):
     * CRUD plus key and member assignments.
     */
    public static class OpenRouterGuardrails {
        private final OpenRouterClient client;

        /**
         * @param client the client used to send the requests
         */
        public OpenRouterGuardrails(OpenRouterClient client) {
            this.client = client;
        }

        /**
         * Lists the guardrails of the account:
         * GET /guardrails (management key required).
         *
         * @return the starting point for the request
         */
        public OpenRouterGuardrailsListRequest.Builder list() {
            return new OpenRouterGuardrailsListRequest.Builder(client);
        }

        /**
         * Creates a new guardrail:
         * POST /guardrails (management key required). A created guardrail
         * enforces nothing until it is assigned to API keys or members.
         *
         * @return the starting point for the request
         */
        public OpenRouterGuardrailCreateRequest.Builder create() {
            return new OpenRouterGuardrailCreateRequest.Builder(client);
        }

        /**
         * Gets a single guardrail by id:
         * GET /guardrails/{id} (management key required).
         *
         * @param id the id (UUID) of the guardrail
         * @return the starting point for the request
         */
        public OpenRouterGuardrailGetRequest.Builder get(String id) {
            return new OpenRouterGuardrailGetRequest.Builder(client, id);
        }

        /**
         * Updates a guardrail:
         * PATCH /guardrails/{id} (management key required).
         *
         * @param id the id (UUID) of the guardrail
         * @return the starting point for the request
         */
        public OpenRouterGuardrailUpdateRequest.Builder update(String id) {
            return new OpenRouterGuardrailUpdateRequest.Builder(client, id);
        }

        /**
         * Deletes a guardrail (permanent):
         * DELETE /guardrails/{id} (management key required).
         *
         * @param id the id (UUID) of the guardrail
         * @return the starting point for the request
         */
        public OpenRouterGuardrailDeleteRequest.Builder delete(String id) {
            return new OpenRouterGuardrailDeleteRequest.Builder(client, id);
        }

        /**
         * Lists the key assignments of one guardrail:
         * GET /guardrails/{id}/assignments/keys (management key required).
         *
         * @param id the id (UUID) of the guardrail
         * @return the starting point for the request
         */
        public OpenRouterGuardrailKeyAssignmentsListRequest.Builder keyAssignments(String id) {
            return new OpenRouterGuardrailKeyAssignmentsListRequest.Builder(client, id);
        }

        /**
         * Lists the member assignments of one guardrail:
         * GET /guardrails/{id}/assignments/members (management key required).
         *
         * @param id the id (UUID) of the guardrail
         * @return the starting point for the request
         */
        public OpenRouterGuardrailMemberAssignmentsListRequest.Builder memberAssignments(String id) {
            return new OpenRouterGuardrailMemberAssignmentsListRequest.Builder(client, id);
        }

        /**
         * Lists the key assignments of every guardrail:
         * GET /guardrails/assignments/keys (management key required).
         *
         * @return the starting point for the request
         */
        public OpenRouterGuardrailAllKeyAssignmentsListRequest.Builder allKeyAssignments() {
            return new OpenRouterGuardrailAllKeyAssignmentsListRequest.Builder(client);
        }

        /**
         * Lists the member assignments of every guardrail:
         * GET /guardrails/assignments/members (management key required).
         *
         * @return the starting point for the request
         */
        public OpenRouterGuardrailAllMemberAssignmentsListRequest.Builder allMemberAssignments() {
            return new OpenRouterGuardrailAllMemberAssignmentsListRequest.Builder(client);
        }

        /**
         * Assigns API keys to a guardrail:
         * POST /guardrails/{id}/assignments/keys (management key required).
         *
         * @param id the id (UUID) of the guardrail
         * @return the starting point for the request
         */
        public OpenRouterGuardrailKeysAssignRequest.Builder assignKeys(String id) {
            return new OpenRouterGuardrailKeysAssignRequest.Builder(client, id);
        }

        /**
         * Unassigns API keys from a guardrail:
         * POST /guardrails/{id}/assignments/keys/remove (management key required).
         *
         * @param id the id (UUID) of the guardrail
         * @return the starting point for the request
         */
        public OpenRouterGuardrailKeysUnassignRequest.Builder unassignKeys(String id) {
            return new OpenRouterGuardrailKeysUnassignRequest.Builder(client, id);
        }

        /**
         * Assigns members to a guardrail:
         * POST /guardrails/{id}/assignments/members (management key required).
         *
         * @param id the id (UUID) of the guardrail
         * @return the starting point for the request
         */
        public OpenRouterGuardrailMembersAssignRequest.Builder assignMembers(String id) {
            return new OpenRouterGuardrailMembersAssignRequest.Builder(client, id);
        }

        /**
         * Unassigns members from a guardrail:
         * POST /guardrails/{id}/assignments/members/remove (management key required).
         *
         * @param id the id (UUID) of the guardrail
         * @return the starting point for the request
         */
        public OpenRouterGuardrailMembersUnassignRequest.Builder unassignMembers(String id) {
            return new OpenRouterGuardrailMembersUnassignRequest.Builder(client, id);
        }
    }

    /**
     * Facade for the BYOK endpoints (management key required).
     */
    public static class OpenRouterByok {
        private final OpenRouterClient client;

        /**
         * @param client the client used to send the requests
         */
        public OpenRouterByok(OpenRouterClient client) {
            this.client = client;
        }

        /**
         * Lists the BYOK provider credentials of the account:
         * GET /byok (management key required).
         *
         * @return the starting point for the request
         */
        public OpenRouterByokListRequest.Builder list() {
            return new OpenRouterByokListRequest.Builder(client);
        }

        /**
         * Creates a BYOK provider credential:
         * POST /byok (management key required). The provider key travels
         * only in the request - treat it as a secret.
         *
         * @return the starting point for the request
         */
        public OpenRouterByokCreateRequest.Builder create() {
            return new OpenRouterByokCreateRequest.Builder(client);
        }

        /**
         * Gets a single BYOK provider credential by id:
         * GET /byok/{id} (management key required).
         *
         * @param id the id (UUID) of the credential
         * @return the starting point for the request
         */
        public OpenRouterByokGetRequest.Builder get(String id) {
            return new OpenRouterByokGetRequest.Builder(client, id);
        }

        /**
         * Updates a BYOK provider credential (key rotation included):
         * PATCH /byok/{id} (management key required).
         *
         * @param id the id (UUID) of the credential
         * @return the starting point for the request
         */
        public OpenRouterByokUpdateRequest.Builder update(String id) {
            return new OpenRouterByokUpdateRequest.Builder(client, id);
        }

        /**
         * Deletes a BYOK provider credential (permanent):
         * DELETE /byok/{id} (management key required).
         *
         * @param id the id (UUID) of the credential
         * @return the starting point for the request
         */
        public OpenRouterByokDeleteRequest.Builder delete(String id) {
            return new OpenRouterByokDeleteRequest.Builder(client, id);
        }
    }

    /**
     * Facade for the observability endpoints (management key required).
     */
    public static class OpenRouterObservability {
        private final OpenRouterClient client;

        /**
         * @param client the client used to send the requests
         */
        public OpenRouterObservability(OpenRouterClient client) {
            this.client = client;
        }

        /**
         * Manages the observability destinations the traces are broadcast
         * to (the request side is {@code OpenRouterTraceConfig}).
         *
         * @return the starting point for the destination requests
         */
        public OpenRouterObservabilityDestinations destinations() {
            return new OpenRouterObservabilityDestinations(client);
        }
    }

    /**
     * Facade for the observability destination endpoints (management key
     * required): CRUD on the destinations traces are broadcast to.
     */
    public static class OpenRouterObservabilityDestinations {
        private final OpenRouterClient client;

        /**
         * @param client the client used to send the requests
         */
        public OpenRouterObservabilityDestinations(OpenRouterClient client) {
            this.client = client;
        }

        /**
         * Lists the destinations:
         * GET /observability/destinations (management key required).
         *
         * @return the starting point for the request
         */
        public OpenRouterObservabilityDestinationsListRequest.Builder list() {
            return new OpenRouterObservabilityDestinationsListRequest.Builder(client);
        }

        /**
         * Creates a destination:
         * POST /observability/destinations (management key required).
         *
         * @return the starting point for the request
         */
        public OpenRouterObservabilityDestinationCreateRequest.Builder create() {
            return new OpenRouterObservabilityDestinationCreateRequest.Builder(client);
        }

        /**
         * Gets a single destination by id:
         * GET /observability/destinations/{id} (management key required).
         *
         * @param id the id (UUID) of the destination
         * @return the starting point for the request
         */
        public OpenRouterObservabilityDestinationGetRequest.Builder get(String id) {
            return new OpenRouterObservabilityDestinationGetRequest.Builder(client, id);
        }

        /**
         * Updates a destination:
         * PATCH /observability/destinations/{id} (management key required).
         *
         * @param id the id (UUID) of the destination
         * @return the starting point for the request
         */
        public OpenRouterObservabilityDestinationUpdateRequest.Builder update(String id) {
            return new OpenRouterObservabilityDestinationUpdateRequest.Builder(client, id);
        }

        /**
         * Deletes a destination (permanent):
         * DELETE /observability/destinations/{id} (management key required).
         *
         * @param id the id (UUID) of the destination
         * @return the starting point for the request
         */
        public OpenRouterObservabilityDestinationDeleteRequest.Builder delete(String id) {
            return new OpenRouterObservabilityDestinationDeleteRequest.Builder(client, id);
        }
    }

    public static class OpenRouterChat {
        private final OpenRouterClient client;

        public OpenRouterChat(OpenRouterClient client) {
            this.client = client;
        }

        public OpenRouterChatCompletionRequest.Builder completion() {
            return OpenRouterChatCompletionRequest.builder(client);
        }
    }

    /**
     * Facade for the preset read and management endpoints. The read
     * endpoints surface the presets that inference requests reference via
     * the {@code preset} body field or a {@code @preset/} model id; the
     * create/update routes store a request body as a new preset version
     * (management key required, create-not-infer semantics).
     */
    public static class OpenRouterPresets {
        private final OpenRouterClient client;

        /**
         * @param client the client used to send the requests
         */
        public OpenRouterPresets(OpenRouterClient client) {
            this.client = client;
        }

        /**
         * Lists the presets of the account:
         * GET /presets with {@code offset} / {@code limit}.
         *
         * @return the starting point for the request
         */
        public OpenRouterPresetsListRequest.Builder list() {
            return new OpenRouterPresetsListRequest.Builder(client);
        }

        /**
         * Gets one preset by slug, including its currently designated
         * version:
         * GET /presets/{slug}. The documented way to check a slug's
         * existence before an inference request - an unknown slug in the
         * {@code preset} body field is silently ignored on inference.
         *
         * @param slug the preset slug
         * @return the starting point for the request
         */
        public OpenRouterPresetGetRequest.Builder get(String slug) {
            return new OpenRouterPresetGetRequest.Builder(client, slug);
        }

        /**
         * Lists the versions of one preset:
         * GET /presets/{slug}/versions with {@code offset} / {@code limit}.
         *
         * @param slug the preset slug
         * @return the starting point for the request
         */
        public OpenRouterPresetVersionsListRequest.Builder versions(String slug) {
            return new OpenRouterPresetVersionsListRequest.Builder(client, slug);
        }

        /**
         * Gets one specific version of a preset:
         * GET /presets/{slug}/versions/{version}.
         *
         * @param slug the preset slug
         * @param version the version number (or {@code latest})
         * @return the starting point for the request
         */
        public OpenRouterPresetVersionGetRequest.Builder version(String slug, String version) {
            return new OpenRouterPresetVersionGetRequest.Builder(client, slug, version);
        }

        /**
         * Creates or updates a preset from a chat-completions request body:
         * POST /presets/{slug}/chat/completions (management key required;
         * create-not-infer semantics - the body is stored as a new version,
         * nothing is generated).
         *
         * @param slug the preset slug to create or update
         * @return the starting point for the request
         */
        public OpenRouterPresetUpsertFromChatRequest.Builder upsertFromChat(String slug) {
            return new OpenRouterPresetUpsertFromChatRequest.Builder(client, slug);
        }

        /**
         * Creates or updates a preset from an Anthropic-Messages request
         * body: POST /presets/{slug}/messages (management key required;
         * create-not-infer semantics).
         *
         * @param slug the preset slug to create or update
         * @return the starting point for the request
         */
        public OpenRouterPresetUpsertFromMessagesRequest.Builder upsertFromMessages(String slug) {
            return new OpenRouterPresetUpsertFromMessagesRequest.Builder(client, slug);
        }

        /**
         * Creates or updates a preset from a Responses-API request body:
         * POST /presets/{slug}/responses (management key required;
         * create-not-infer semantics).
         *
         * @param slug the preset slug to create or update
         * @return the starting point for the request
         */
        public OpenRouterPresetUpsertFromResponsesRequest.Builder upsertFromResponses(String slug) {
            return new OpenRouterPresetUpsertFromResponsesRequest.Builder(client, slug);
        }
    }

    /**
     * Facade for the SCIM provisioning endpoints (management key required):
     * group-to-workspace mappings, the SCIM groups and the directory-sync
     * jobs.
     */
    public static class OpenRouterScim {
        private final OpenRouterClient client;

        /**
         * @param client the client used to send the requests
         */
        public OpenRouterScim(OpenRouterClient client) {
            this.client = client;
        }

        /**
         * Lists the group-to-workspace mappings:
         * GET /scim/group-mappings with {@code offset} / {@code limit}.
         *
         * @return the starting point for the request
         */
        public OpenRouterScimGroupMappingsListRequest.Builder groupMappings() {
            return new OpenRouterScimGroupMappingsListRequest.Builder(client);
        }

        /**
         * Creates a group-to-workspace mapping:
         * POST /scim/group-mappings. Trap: re-creating the same mapping
         * with the same role succeeds and re-applies it; a different role
         * for an existing mapping is rejected with HTTP 409 - use
         * {@link #updateGroupMapping(String)} to change a role.
         *
         * @return the starting point for the request
         */
        public OpenRouterScimGroupMappingCreateRequest.Builder createGroupMapping() {
            return new OpenRouterScimGroupMappingCreateRequest.Builder(client);
        }

        /**
         * Gets one group-to-workspace mapping by id:
         * GET /scim/group-mappings/{id}.
         *
         * @param id the mapping id (UUID)
         * @return the starting point for the request
         */
        public OpenRouterScimGroupMappingGetRequest.Builder groupMapping(String id) {
            return new OpenRouterScimGroupMappingGetRequest.Builder(client, id);
        }

        /**
         * Updates a group-to-workspace mapping:
         * PATCH /scim/group-mappings/{id} ({@code role} only).
         *
         * @param id the mapping id (UUID)
         * @return the starting point for the request
         */
        public OpenRouterScimGroupMappingUpdateRequest.Builder updateGroupMapping(String id) {
            return new OpenRouterScimGroupMappingUpdateRequest.Builder(client, id);
        }

        /**
         * Deletes a group-to-workspace mapping:
         * DELETE /scim/group-mappings/{id} with the optional
         * {@code keep_members} query parameter.
         *
         * @param id the mapping id (UUID)
         * @return the starting point for the request
         */
        public OpenRouterScimGroupMappingDeleteRequest.Builder deleteGroupMapping(String id) {
            return new OpenRouterScimGroupMappingDeleteRequest.Builder(client, id);
        }

        /**
         * Lists the SCIM groups of the organization:
         * GET /scim/groups with {@code offset} / {@code limit} (the group
         * ids are the {@code scim_group_id} values of the mapping
         * endpoints).
         *
         * @return the starting point for the request
         */
        public OpenRouterScimGroupsListRequest.Builder groups() {
            return new OpenRouterScimGroupsListRequest.Builder(client);
        }

        /**
         * Starts a SCIM directory sync:
         * POST /scim/sync-jobs (answers 202 with the created job).
         *
         * @return the starting point for the request
         */
        public OpenRouterScimSyncJobCreateRequest.Builder startSyncJob() {
            return new OpenRouterScimSyncJobCreateRequest.Builder(client);
        }

        /**
         * Polls the status of a SCIM directory sync:
         * GET /scim/sync-jobs/{id}.
         *
         * @param id the sync-job id (UUID)
         * @return the starting point for the request
         */
        public OpenRouterScimSyncJobGetRequest.Builder syncJob(String id) {
            return new OpenRouterScimSyncJobGetRequest.Builder(client, id);
        }
    }

    /**
     * Facade for the intern endpoints (the OpenRouter "Ori" programme).
     * Every path answers 404 for keys outside the interns programme; there
     * is no default workspace fallback and regional hostnames are refused.
     */
    public static class OpenRouterInterns {
        private final OpenRouterClient client;

        /**
         * @param client the client used to send the requests
         */
        public OpenRouterInterns(OpenRouterClient client) {
            this.client = client;
        }

        /**
         * Lists the interns visible to the API key, newest first:
         * GET /interns with the typed filters ({@code limit}, lifecycle
         * {@code status}, {@code starting_after} cursor,
         * {@code workspace_id}) plus the {@code queryParam} escape hatch.
         *
         * @return the starting point for the request
         */
        public OpenRouterInternsListRequest.Builder list() {
            return new OpenRouterInternsListRequest.Builder(client);
        }

        /**
         * Creates an intern:
         * POST /interns (idempotent on retry; the body is capped at 1 MiB).
         * Set {@code provision(true)} to boot immediately, otherwise the
         * intern stays queued until the provision endpoint is called.
         *
         * @param name the intern name (2-17 chars, lowercase pattern,
         *             validated loudly)
         * @return the starting point for the request
         */
        public OpenRouterInternCreateRequest.Builder create(String name) {
            return new OpenRouterInternCreateRequest.Builder(client, name);
        }

        /**
         * Reads one intern's public lifecycle state and settings:
         * GET /interns/{internId}.
         *
         * @param internId the id (UUID) of the intern
         * @return the starting point for the request
         */
        public OpenRouterInternGetRequest.Builder get(String internId) {
            return new OpenRouterInternGetRequest.Builder(client, internId);
        }

        /**
         * Updates an intern:
         * PATCH /interns/{internId} - omitted fields stay unchanged.
         *
         * @param internId the id (UUID) of the intern
         * @return the starting point for the request
         */
        public OpenRouterInternUpdateRequest.Builder update(String internId) {
            return new OpenRouterInternUpdateRequest.Builder(client, internId);
        }

        /**
         * Deletes an intern - the safe teardown of intern, runtime and
         * vault: DELETE /interns/{internId}. Trap: without
         * {@code acknowledgeWorkspaceLoss(true)} the teardown is refused
         * while a workspace archive is missing.
         *
         * @param internId the id (UUID) of the intern
         * @return the starting point for the request
         */
        public OpenRouterInternDeleteRequest.Builder delete(String internId) {
            return new OpenRouterInternDeleteRequest.Builder(client, internId);
        }

        /**
         * Provisions an intern - the first boot, or the resume after a
         * suspension: POST /interns/{internId}/provision.
         *
         * @param internId the id (UUID) of the intern
         * @return the starting point for the request
         */
        public OpenRouterInternProvisionRequest.Builder provision(String internId) {
            return new OpenRouterInternProvisionRequest.Builder(client, internId);
        }

        /**
         * Suspends an intern - stops the runtime, keeps the disk and the
         * configuration: POST /interns/{internId}/suspend.
         *
         * @param internId the id (UUID) of the intern
         * @return the starting point for the request
         */
        public OpenRouterInternSuspendRequest.Builder suspend(String internId) {
            return new OpenRouterInternSuspendRequest.Builder(client, internId);
        }

        /**
         * Streams one turn with an intern in the OpenAI chat-completions SSE
         * format: POST /interns/{internId}/chat/completions. The endpoint
         * only streams - a request without an installed streaming handler is
         * refused loudly. Only the last message is read; a tool reply
         * requires the {@code session_id} of the interaction's stream. Use
         * {@code OpenRouterInternChatAccumulator} as the handler for typed
         * access to {@code finish_reason}, the
         * {@code openrouter.provide_input} tool call and the
         * {@code session_id}.
         *
         * @param internId the id (UUID) of the intern
         * @return the starting point for the request
         */
        public OpenRouterInternChatRequest.Builder chat(String internId) {
            return new OpenRouterInternChatRequest.Builder(client, internId);
        }
    }

    /**
     * Facade for the vault endpoints - host-bound secrets for the active
     * workspace and for interns. Responses are metadata only; the plaintext
     * value is never returned. Scope is selected by the API key's active
     * workspace (no workspace parameter, no fallback). Every vault route,
     * including the list, answers 404 outside the Intern API programme.
     */
    public static class OpenRouterVault {
        private final OpenRouterClient client;

        /**
         * @param client the client used to send the requests
         */
        public OpenRouterVault(OpenRouterClient client) {
            this.client = client;
        }

        /**
         * Lists the workspace secret metadata:
         * GET /vault/secrets with {@code limit} / {@code offset}, sorted by
         * name.
         *
         * @return the starting point for the request
         */
        public OpenRouterVaultSecretsListRequest.Builder list() {
            return new OpenRouterVaultSecretsListRequest.Builder(client);
        }

        /**
         * Stores (creates or replaces) a workspace secret:
         * PUT /vault/secrets/{name}. The value is a write-only secret - it
         * is never returned by any vault response.
         *
         * @param name the secret name (validated loudly)
         * @return the starting point for the request
         */
        public OpenRouterVaultSecretStoreRequest.Builder store(String name) {
            return new OpenRouterVaultSecretStoreRequest.Builder(client, name);
        }

        /**
         * Deletes a workspace secret (permanent):
         * DELETE /vault/secrets/{name}. Unknown names answer 404.
         *
         * @param name the secret name (validated loudly)
         * @return the starting point for the request
         */
        public OpenRouterVaultSecretDeleteRequest.Builder delete(String name) {
            return new OpenRouterVaultSecretDeleteRequest.Builder(client, name);
        }

        /**
         * Lists one intern's secret metadata:
         * GET /vault/interns/{internId}/secrets with {@code limit} /
         * {@code offset}, sorted by name.
         *
         * @param internId the id (UUID) of the intern
         * @return the starting point for the request
         */
        public OpenRouterVaultInternSecretsListRequest.Builder internSecrets(String internId) {
            return new OpenRouterVaultInternSecretsListRequest.Builder(client, internId);
        }

        /**
         * Stores (creates or replaces) an intern secret:
         * PUT /vault/interns/{internId}/secrets/{name}. The value is a
         * write-only secret - it is never returned by any vault response.
         *
         * @param internId the id (UUID) of the intern
         * @param name the secret name (validated loudly)
         * @return the starting point for the request
         */
        public OpenRouterVaultInternSecretStoreRequest.Builder storeInternSecret(
                String internId, String name) {
            return new OpenRouterVaultInternSecretStoreRequest.Builder(client, internId, name);
        }

        /**
         * Deletes an intern secret (permanent):
         * DELETE /vault/interns/{internId}/secrets/{name}. Unknown names
         * answer 404.
         *
         * @param internId the id (UUID) of the intern
         * @param name the secret name (validated loudly)
         * @return the starting point for the request
         */
        public OpenRouterVaultInternSecretDeleteRequest.Builder deleteInternSecret(
                String internId, String name) {
            return new OpenRouterVaultInternSecretDeleteRequest.Builder(client, internId, name);
        }

        /**
         * Copies named workspace secrets into an intern's scope:
         * POST /vault/interns/{internId}/secrets/copy. Trap: every
         * requested name must exist - otherwise the API answers 404 and
         * nothing is copied.
         *
         * @param internId the id (UUID) of the intern
         * @return the starting point for the request
         */
        public OpenRouterVaultSecretCopyRequest.Builder copySecretsToIntern(String internId) {
            return new OpenRouterVaultSecretCopyRequest.Builder(client, internId);
        }
    }

}
