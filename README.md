# OpenRouter4J

OpenRouter4J is a fluent Java wrapper for the [OpenRouter API](https://openrouter.ai/docs).
It builds on top of the lightweight [`api-base`](https://github.com/hwalde/api-base) library which
handles HTTP communication, authentication and exponential backoff. The goal is to provide a type safe
and convenient way to access hundreds of AI models through OpenRouter from Java, being as close to the raw API as possible.

> **A word from the author**
>
> I created this library because I was looking for a Java library that interacts with the OpenRouter API while staying as close to the raw API as possible. OpenRouter provides a unified API that gives you access to hundreds of AI models (OpenAI, Anthropic, Google, Meta, Mistral, and many more) through a single endpoint. This implementation is fully compatible with OpenRouter but only includes the features I personally require. I maintain similar libraries for Gemini, DeepSeek and OpenAI, each in its own repository so usage remains explicit.
>
> At the moment the library only covers the parts I need. Chat Completions are implemented with nearly every option, but many specialized endpoints are missing. If you need additional functionality, feel free to implement it yourself or submit a pull request and I will consider adding it.


## Features

* Chat Completions including tool calling (including forcing a specific tool), structured outputs, vision inputs and reasoning configuration (effort / max_tokens / exclude / enabled / summary)
* **Multimodal inputs**: images (URL or base64, with optional `detail` resolution tier), documents (`addFileByUrl` / `addFileById`, PDF understanding), audio (`addAudioByBase64`, `input_audio`) and video (`addVideoByUrl`, optional `processing` mode via `OpenRouterVideoProcessing`) - plus a verbatim `addContentPart` escape hatch; multi-part tool results (`OpenRouterToolResult.ofParts`) hand screenshots or documents back to the model
* Message extras: author names (`addNamedMessage`), the `developer` role (`addDeveloperMessage`) and mid-conversation reasoning-effort changes that keep the prompt cache valid (`addConfigurationUpdate`)
* **Streaming support** for real-time token generation using Server-Sent Events (SSE) - the synthetic response of the streaming loop carries reasoning, reasoning details, refusal, logprobs and the chunk-level `service_tier` / `openrouter_metadata` / `system_fingerprint` fields, so both execution paths behave identically
* Server-side plugins - typed implementations for all ten plugin ids (`web` search, `auto-router`, `auto-beta-router`, `moderation`, `web-fetch`, `file-parser`, `response-healing`, `context-compression`, `pareto-router`, `fusion`) plus a generic escape hatch; built-in server tools typed as well (`openrouter:web_search`, `openrouter:web_fetch`, `openrouter:datetime`, `openrouter:advisor`, `openrouter:bash`, `openrouter:shell`, `openrouter:apply_patch`, `openrouter:files`, `openrouter:fusion`, `openrouter:image_generation`, `openrouter:experimental__search_models`, `openrouter:subagent`, `openrouter:tool_search`) and multimodal output (`modalities` / `image_config`, with `images`/`audio` response accessors; note that the audio output path currently has no runnable example because only OpenAI models serve it)
* Preset-based inference: `model("@preset/slug")` lets a stored OpenRouter preset supply everything including the model; `preset("slug")` emits the `preset` body field, where request fields shallow-merge server-side and win over the preset - setting a preset does not remove the request's model (the explicit one or the builder default), so in that form the preset's model does not apply, and an unknown slug is silently ignored
* Predicted outputs (`prediction`), prompt-caching controls (`cache_control`, `prompt_cache_key`, `prompt_cache_options`, per-content-part cache markers via `addMessage(role, text, marker)`, per-tool `cache_control`) and capacity tiers (`service_tier`, echoed back on the response)
* Response formats: structured outputs (`json_schema` with a configurable envelope), `json_object`, `text`, custom grammars (`response_format: {"type":"grammar"}`) and Python-code output (`response_format: {"type":"python"}`)
* Deferred tool loading: `deferLoading(true)` on a tool definition keeps large tool catalogs out of the prompt until tool search reveals them - on Chat Completions via the provider-managed path (Anthropic models and Anthropic-compatible endpoints only, no `openrouter:tool_search` server tool needed; that server tool is only served by the Responses and Anthropic Messages APIs)
* OpenRouter-specific response details: reasoning output, provider, native finish reason, typed routing-metadata accessors (`metadataRequestedModel()`, `metadataRoutingStrategy()`, `metadataSelectedProvider()`, `metadataGenerationTimeMs()`, attempts/pipeline and more - opt-in via `metadataInResponse(true)`), cost, logprobs, system fingerprint and a loud error path for mid-request failures
* **Read endpoints beyond chat**: credit balance (`GET /credits`, `client.credits()`), the model catalog (`GET /models` with typed filters - including age, Artificial Analysis intelligence/coding/agentic index and tool-success-rate bounds - `GET /models/count`, `GET /models/user`, `GET /model/{author}/{slug}`, `GET /models/{author}/{slug}/endpoints` via `client.models()` and friends - typed pricing, `supported_parameters`, context-length and architecture accessors plus the `reasoning` / `benchmarks` / `alias_target` / `per_request_limits` / `default_parameters` views), generation metadata (`GET /generation`, `GET /generation/content`, `POST /generation/feedback` via `client.generation()` and friends - the post-mortem routing view with provider, native finish reason and token/cost breakdown) and usage analytics (`GET /activity`, `POST /analytics/query` via `client.activity()` / `client.analyticsQuery()`, including typed `classifier_dimensions` / `classifier_filters` grouping and filtering on custom classifier tags - both objects must share one `classifier_id`; see `OpenRouterActivityAnalyticsExample`); note that `/credits`, `/generation*` and `/activity*` require a management key
* **Embeddings**: embedding vectors via `POST /embeddings` (`client.embeddings()` - required `model` plus `input(String)` or batch `inputs(List)`; optional `dimensions`, `encodingFormat("float"|"base64")`, `inputType`, `user`, `sessionId` and `trace(OpenRouterTraceConfig)` and a provider-routing subset `providerOrder` / `providerOnly` / `providerIgnore` / `requireParameters` / `allowFallbacks`), typed response with `embeddings()` / `embedding(index)` and token usage; base64 vectors decode via `vectorOrDecoded()`. Trap: `dimensions` is only accepted by models with configurable dimensions, others reject it with HTTP 400. Embedding-model listing via `client.embeddingsModels()` (`GET /embeddings/models`). Example: `OpenRouterEmbeddingsExample`
* **Rerank**: relevance-sorted document reranking via `POST /rerank` (`client.rerank()` - required `model`, `query` and at least one document via `addDocument(String)` (bare JSON string) or the structured object form `addDocument(String text, String image)`; optional `topN`, `user(...)`, `sessionId(...)` and `trace(OpenRouterTraceConfig)` and the same provider-routing subset as the embeddings endpoint), typed results with `index()` (position in the input list), `relevanceScore()` and the echoed document, plus `searchUnits()` (Cohere billing unit). Trap: a structured document needs text or image, and the image must be a URL or a `data:image/...` URI. Example: `OpenRouterRerankExample`
* **OAuth authorization-code flow**: turn a user consent into an OpenRouter API key via `client.createAuthorizationCode()` (`POST /auth/keys/code` - required `callbackUrl`; optional `codeChallenge`/`codeChallengeMethod("S256"|"plain")`, `expiresAt` (seconds precision, minute-precision rejected), `keyLabel` (max 100 chars), `limit`, `usageLimitType("daily"|"weekly"|"monthly")`, `workspaceId`) followed by `client.exchangeAuthorizationCode()` (`POST /auth/keys` - required `code`, optional `codeVerifier` and `codeChallengeMethod`); the `OpenRouterPkce` helper generates RFC 7636 verifiers and S256 challenges. Traps: authorization codes expire 10 minutes after issuance, and (observed 2026-09-14) `POST /api/v1/auth/keys/code` currently serves the website's 404 page at the documented path - the implementation follows the published spec. The exchanged key is a long-lived secret - store it, never log it. Example: `OpenRouterOAuthExample`
* **Workload identity federation**: exchange an identity-provider JWT for a short-lived OpenRouter access token via `client.exchangeWorkloadIdentityToken()` (`POST /oauth/token`, RFC 8693 with an urlencoded form body - `grant_type`/`subject_token_type` are fixed constants; required `subjectToken` and `federationPolicyId`, optional `scope("inference")` / `requestedTokenType`). The response token lives at most 15 minutes (`expiresIn()`) - a short-lived secret for CI and service accounts instead of an embedded long-lived key. Verify that token against `client.oauthJwks()` (`GET /oauth/jwks`, RFC 7517 JWK Set with the public signing keys, typed `OpenRouterJwk` views and a `key(kid)` lookup). Example: `OpenRouterWorkloadIdentityExample`; JWKS: `OpenRouterJwksExample`
* **Anthropic Messages API**: `client.messages()` issues `POST /messages` - the `system` prompt travels as a top-level field, `maxTokens` is required by Anthropic semantics (rejected loudly when missing), the effort setting travels in `output_config` (`effort`, `outputFormat`, `taskBudget`), and the response carries typed views over the content blocks (`textBlocks()`, `toolUseBlocks()`, `thinkingBlocks()`, ...). Streaming via `stream(handler)`: every Anthropic event (`message_start`, `content_block_delta`, `message_stop`, ...) arrives as its raw JSON string. The server-tool agent loop can be bounded with `stopServerToolsWhen(...)` (same `OpenRouterStopCondition` factories as chat: OR logic, overrides `max_tool_calls`, ends with one final turn with tools disabled), and long agentic conversations shed old tool results, thinking blocks or compact server-side via `contextManagement(...)` (the three Anthropic strategy types `clear_tool_uses_20250919`, `clear_thinking_20251015`, `compact_20260112` plus a verbatim escape hatch). Example: `OpenRouterMessagesExample`
* **OpenAI Responses API**: `client.responses()` issues `POST /responses` - the successor surface for features that only exist there (full `openrouter:tool_search` support, `openrouter:apply_patch`, subagent function inheritance). Input as a plain string or item array (`addMessage(role, text)` plus a verbatim `addInputItem` escape hatch), typed sampling/reasoning/tool/provider options reusing the chat plugin and server-tool types, `tool_choice` object forms via `toolChoiceFunction(name)` (flat `{"type":"function","name":...}`) / `toolChoiceAllowedTools(mode, refs)` / `toolChoiceType(type)` (precedence: named function > allowed_tools > tool-type > plain keyword), stored prompt templates via `prompt(id)` + `promptVariable(key, value)`, image generation via `imageConfig(OpenRouterImageConfig)`, `debug(echoUpstreamBody)`, the typed `text` output options (`textFormat` / `textVerbosity`), `topLogprobs(n)` and `promptCacheOptions(mode[, ttl])` for the explicit-cache setup, and `stream(handler)` delivering every Responses event as raw JSON. The API is stateless - `previous_response_id` is rejected, send the full history in `input`. Example: `OpenRouterResponsesExample`
* **Decisions API (alpha)**: `client.decisions()` issues `POST /api/alpha/decisions` - evaluate content against typed questions: boolean `noul`, multi-class `choice` and ordered `score` (typed `OpenRouterDecisionQuestion` factories plus a verbatim escape hatch; the state accepts a string, object or array form). Typed answers with per-type accessors (`noulProbability()`, `choice()`, `score()`, `probabilities()`, `confidence()`). Trap: alpha surface - it may change or disappear without a major version of the API, and the Decisions router serves its own judging models (e.g. `typesafe/jev-1.13`). Example: `OpenRouterDecisionsExample`
* **Preset read and management**: `client.presets()` - `list()` / `get(slug)` / `versions(slug)` / `version(slug, v)` (the documented existence check for a slug: an unknown slug in the `preset` body field is silently ignored on inference) and the create/update routes `upsertFromChat(slug)` / `upsertFromMessages(slug)` / `upsertFromResponses(slug)`, which store an ordinary inference request body as a new preset version (management key required; create-not-infer semantics - nothing is generated). Example: `OpenRouterPresetsExample`
* **SCIM provisioning**: `client.scim()` - CRUD on the group-to-workspace mappings (`createGroupMapping`, `groupMapping`, `updateGroupMapping`, `deleteGroupMapping` with the `keep_members` query), `groups()` and the directory-sync endpoints (`startSyncJob`, `syncJob`), all management-key only. Trap: a different role for an existing mapping is rejected with HTTP 409 - update instead. Example: `OpenRouterScimExample`
* **Image generation**: `client.images().generate()` issues `POST /images` (aspect ratio, quality, resolution/size, n, output format, up to 16 `input_references` for image-to-image) and the response decodes the base64 `b64_json` into bytes (`firstImage().bytes()`); model discovery via `client.images().models()` and `client.images().modelEndpoints("author/slug")` (per-endpoint parameters and pricing). Trap (observed live 2026-09-15): the endpoint currently returns `created: 0`. Example: `OpenRouterImageGenerationExample`
* **Video generation**: `client.videos().generate()` submits `POST /videos` (aspect ratio, resolution/size, duration, audio, first/last frame images, reference assets, `previousJobId(...)` for chaining to a finished job, `sessionId(...)`, `user(...)` and `trace(OpenRouterTraceConfig)`), the shared job response offers `awaitCompletion(client)` polling and `executeAndAwaitCompletion()`, and `client.videos().jobContent(jobId)` downloads the raw video bytes (binary endpoint; `index(n)` selects one output of a multi-output job). Model discovery via `client.videos().models()`. Example: `OpenRouterVideoGenerationExample`
* **Speech-to-text / text-to-speech**: `client.audio().transcriptions()` issues `POST /audio/transcriptions` (JSON body with base64 audio via `audioByBase64`/`audioByPath`, or the schema's multipart form via `audioByFile`; optional language, `verbose_json` with segment/word timestamps) and `client.audio().speech()` issues `POST /audio/speech` (voice, speed, voice-cloning references; the response holds the raw mp3/pcm audio bytes); STT and speech also accept `trace(OpenRouterTraceConfig)`, `user(...)`, `sessionId(...)` and the `provider.options` passthrough (`providerOption(slug, options)`, JSON mode only on STT - in multipart form `trace` travels as a JSON-encoded string, the way the schema defines it). Example: `OpenRouterAudioExample`
* **Files API**: `client.files()` - `upload()` (`POST /files`, multipart form with the single `file` part, max 100 MB and empty files rejected loudly; the file type is determined from the file contents, not the filename - PDF, PNG/JPEG/GIF/WebP, DOCX/XLSX/PPTX, MP3/WAV/FLAC/OGG or UTF-8 text only; query parameters `workspaceId` / `provider`), `list()` (`GET /files` with `limit` (1-1000), `cursor`, `workspaceId`, `provider`, the OpenAI-style `after`, the Anthropic-style `afterId` / `beforeId` and `order` cursors plus the verbatim `queryParam` escape hatch), `get(fileId)`, `delete(fileId)` (irreversible) and `content(fileId)` (binary download) - each carrying the same `workspaceId` / `provider` storage-scope query parameters as upload/list. The shared `OpenRouterFile` view covers all three negotiated wire shapes (`_shape`: `openrouter`, `openai`, `anthropic`), so the same accessors work whichever shape the API answers with. Example: `OpenRouterFilesExample`
* **Container files**: `client.containers()` retrieves what the bash/shell server tools wrote into a code-execution sandbox - `listFiles(containerId)` (`GET /containers/{container_id}/files`, lexicographic path order, `limit` (1-1000) plus the `after(lastId)` forward cursor), `file(containerId, fileId)`, `fileContent(containerId, fileId)` (binary download) and `promoteFile(containerId, fileId)` (copies the file into the workspace's durable document storage, counts against the storage quota; the response is the new document in the Files API shape). The container id is the canonical id exactly as returned in a bash/shell server-tool result. Example: `OpenRouterContainersExample`
* **Vault secrets**: `client.vault()` - the host-bound secrets of the API key's active workspace and of interns: `list()` (`GET /vault/secrets`, `limit` 1-100 / `offset` 0-10,000, sorted by name), `store(name)` (`PUT /vault/secrets/{name}`; `value` 1-65,536 chars and `hosts` 1-100 entries, both required, cheap rules validated loudly), `delete(name)` (204, 404 for unknown names), the intern counterparts `internSecrets(id)` / `storeInternSecret(id, name)` / `deleteInternSecret(id, name)` and `copySecretsToIntern(id).names(...)` (1-100 distinct names). Responses are metadata only - the plaintext is a write-only secret, never returned, and the request `toString()` never prints it. Traps: the fingerprint is comparable only within one vault; host matching is exact (`api.example.com` is never released to `example.com`); a copy with one unknown name copies nothing (404); every vault route, including the list, answers 404 outside the Intern API programme. Example: `OpenRouterVaultExample`
* **Interns (the "Ori" programme)**: `client.interns()` - `list()` (newest first, typed `status`/`limit`/`startingAfter`/`workspaceId` filters plus `queryParam`), `create(name)` (idempotency key via header; `provision(true)` boots immediately), `get` / `update` (omitted fields stay unchanged) / `delete` (safe teardown, `acknowledgeWorkspaceLoss(true)` consent) / `provision` / `suspend`, and the streaming `chat(internId)` with the `openrouter.provide_input` interaction loop: the `OpenRouterInternChatAccumulator` handler gives typed access to `finishReason()`, the paused interaction's `toolCall()` and the `sessionId()`, which the `tool` reply must send back. Traps: the endpoint only streams (a request without a handler is refused loudly); a paused run waits 5 minutes; closing an active stream cancels the run; there is deliberately no `model` builder method (the API accepts it but never uses it). Every path answers 404 outside the programme. Example: `OpenRouterInternsExample`
* **Routing discovery**: `client.providers()` (`GET /providers` - the provider slugs the routing options accept, with datacenter/headquarters data) and `client.zdrEndpoints()` (`GET /endpoints/zdr` - the endpoint picture before enabling `zdr(true)`, with latency/throughput/uptime percentiles); both work with a normal inference key. The analytics meta companion `client.analyticsMeta()` (`GET /analytics/meta`, management key required) lists the metrics, dimensions, operators and granularities the `analyticsQuery()` builder accepts. Example: `OpenRouterRoutingDiscoveryExample`
* **Public data**: read-only market endpoints working with any valid API key - `client.benchmarks()` (`GET /benchmarks`, unified benchmark rows from Artificial Analysis, Design Arena and OpenRouter's own evals), `client.datasets().appRankings()` / `.rankingsDaily()` / `.sessionCost()` (top apps by token usage, daily top-50 model totals with the reserved `other` row, cost per session by harness and model) and `client.taskClassifications()` (`GET /classifications/task`, task-classification market share). Example: `OpenRouterPublicDataExample`
* **API key management**: `client.currentKey()` (`GET /key`, works with a normal inference key - limits, usage, whether it is a management key) and the `client.keys()` surface `list()` / `create()` / `get(hash)` / `update(hash)` / `delete(hash)` (management key required; `create()` returns the plaintext key exactly once and the response `toString()` never prints it). Example: `OpenRouterKeysManagementExample`
* **Workspace and organization management**: the `client.workspaces()` surface - `list()` / `create()` (name plus URL-friendly `slug`, validated loudly) / `get(id)` / `update(id)` (`slug` renames the workspace) / `delete(id)`, the members endpoints `members(id)` / `addMembers(id)` / `removeMembers(id)`, and the budget endpoints `budgets(ref)` / `budget(ref, interval)` / `upsertBudget(ref, interval)` (limit in USD per `daily` / `weekly` / `monthly` / `lifetime`; `includeByokInBudgets` is a workspace-wide setting) / `deleteBudget(ref, interval)`; plus `client.organization().members()`. Example: `OpenRouterWorkspacesExample`
* **Guardrails management**: the `client.guardrails()` surface - CRUD on the server-side request policy layer (allowed models/providers, data regions, spend limits, content filters, per-provider zero-data-retention) and the key/member assignment endpoints (`assignKeys` / `assignMembers` and their list/remove counterparts, global and per guardrail). Trap: a created guardrail enforces nothing until it is assigned to API keys or members. Example: `OpenRouterGuardrailsExample`
* **BYOK credential management**: the `client.byok()` surface - `list()` / `create()` (provider and key required and validated loudly) / `get(id)` / `update(id)` (in-place key rotation) / `delete(id)`, including the `declared_zdr` self-declaration (`declaredZdr(Boolean)` - the credential-side counterpart of request-side `zdr(true)` routing). The provider credential is a write-only secret: encrypted at rest, never returned by any response. Example: `OpenRouterByokExample`
* **Observability destination management**: `client.observability().destinations()` - CRUD on the destinations the `trace(OpenRouterTraceConfig)` traces are broadcast to (Langfuse, Datadog, Weave, ...); the destination-type-specific `config` object is built with `config(JSONObject)` or the `configOption(key, value)` escape hatch. Example: `OpenRouterObservabilityExample`
* Access to 200+ AI models through a single unified API
* Provider selection for routing requests to specific providers
* Vision capabilities for image understanding and analysis (optional per-image resolution tier `detail`: auto/low/high/original)
* Fluent builder APIs for all requests
* Examples demonstrating each feature

## Installation

Add the dependency from Maven Central:

```xml
<dependency>
    <groupId>de.entwicklertraining</groupId>
    <artifactId>openrouter4j</artifactId>
    <version>1.26.0</version>
</dependency>
```

## Basic Usage

Instantiate an `OpenRouterClient` and use the builders exposed by its fluent API. The
[function calling example](openrouter4j-examples/src/main/java/de/entwicklertraining/openrouter4j/examples/OpenRouterChatCompletionWithFunctionCallingExample.java)
shows how tools can be defined and executed:

```java
OpenRouterToolDefinition weatherFunction = OpenRouterToolDefinition.builder("get_local_weather")
        .description("Get weather information for a location.")
        .parameter("location", OpenRouterJsonSchema.stringSchema("Name of the city"), true)
        .callback(ctx -> {
            String loc = ctx.arguments().getString("location");
            return OpenRouterToolResult.of(new JSONObject().put("weather", "Sunny in " + loc + " with a high of 25°C."));
        })
        .build();

OpenRouterClient client = new OpenRouterClient(); // reads the API key from OPENROUTER_API_KEY

OpenRouterChatCompletionResponse resp = client.chat().completion()
        .model("deepseek/deepseek-v4-flash-0731")
        .systemInstruction("You are a helpful assistant.")
        .addMessage("user", "What's the weather in Berlin?")
        .addTool(weatherFunction)
        .execute();
System.out.println(resp.assistantMessage());
```

### Vision Example

The [vision example](openrouter4j-examples/src/main/java/de/entwicklertraining/openrouter4j/examples/OpenRouterChatCompletionWithVisionUrlExample.java)
demonstrates image analysis capabilities:

```java
OpenRouterClient client = new OpenRouterClient();
OpenRouterChatCompletionResponse response = client.chat().completion()
        .model("z-ai/glm-5.3-flash")
        .addMessage("user", "What's in this image?")
        .addImageByUrl("https://example.com/image.jpg")
        .execute();
System.out.println(response.assistantMessage());
```

See the `openrouter4j-examples` module for more demonstrations including base64 images, per-image resolution tiers (`detail`: auto/low/high/original via `OpenRouterChatCompletionWithVisionDetailExample`), documents, audio and video inputs (`OpenRouterChatCompletionWithFileAudioVideoExample`), multi-part tool results (`OpenRouterChatCompletionWithMultimodalToolResultExample`), message names / developer role / mid-conversation effort changes (`OpenRouterChatCompletionWithConfigurationUpdateExample`), structured outputs (with a configurable `json_schema` envelope: name, `strict` and description), reasoning configuration, named tool choice, sampling options, model fallbacks, app attribution, observability parameters (metadata/user/session), trace metadata for broadcast destinations (`OpenRouterChatCompletionWithTraceExample`), extended provider preferences (data collection, ignore/only providers, price caps, quantizations, sort with partition, performance thresholds, Zero Data Retention via `zdr(true)`), streaming-only debug echo of the transformed upstream request body (`OpenRouterChatCompletionWithDebugEchoExample`), server-side plugins (web search), built-in server tools (with `strict` schema adherence on function tools) and stop conditions (`stop_server_tools_when`), multimodal output (`modalities` / `image_config` with the `images` response accessor), token log probabilities (`OpenRouterChatCompletionWithLogprobsExample`), predicted outputs (`prediction`), prompt-caching controls (`cache_control`, `prompt_cache_key`, `prompt_cache_options`), explicit per-block cache breakpoints (`OpenRouterChatCompletionWithExplicitCacheBreakpointsExample`), deferred tool loading (provider-managed path on Anthropic models, `OpenRouterChatCompletionWithToolSearchExample`), grammar/python response formats (`OpenRouterChatCompletionWithResponseFormatGrammarExample`), capacity tiers (`service_tier`), and OpenRouter-specific response details (reasoning, provider, cost, full usage token details, server-tool cost and server-tool use details, message model/name, loud error handling), typed router-metadata accessors (`OpenRouterChatCompletionWithRouterMetadataExample`), typed plugins beyond web search (auto-router, web-fetch, file-parser, moderation, ... via `OpenRouterChatCompletionWithTypedPluginsExample`), typed server tools (advisor/subagent/search-models/image-generation via `OpenRouterChatCompletionWithAdvisorSubagentExample`), preset-based inference (`OpenRouterChatCompletionWithPresetExample`), the credit balance (`OpenRouterCreditsExample`), the model catalog (`OpenRouterModelsCatalogExample`), generation metadata (`OpenRouterGenerationMetadataExample`), usage analytics (`OpenRouterActivityAnalyticsExample`), embeddings (`OpenRouterEmbeddingsExample`), rerank (`OpenRouterRerankExample`), the Decisions API (alpha, `OpenRouterDecisionsExample`), the OAuth authorization-code flow (`OpenRouterOAuthExample`), the workload identity exchange (`OpenRouterWorkloadIdentityExample`), the Anthropic Messages API with streaming (`OpenRouterMessagesExample`), image generation on the dedicated Image API (`OpenRouterImageGenerationExample`), async video generation with polling (`OpenRouterVideoGenerationExample`) and speech-to-text / text-to-speech (`OpenRouterAudioExample`).

### Provider Selection

OpenRouter allows you to route requests to specific providers. This is useful when you want to use a specific provider's infrastructure:

```java
client.chat().completion()
        .model("deepseek/deepseek-v4-flash-0731")
        .provider("deepseek")  // Use DeepSeek's official endpoint
        .addMessage("user", "Hello!")
        .execute();

// Or route to another provider serving the same model
client.chat().completion()
        .model("z-ai/glm-5.3-flash")
        .provider("z-ai")
        .addMessage("user", "Hello!")
        .execute();
```

Two additional routing options control how strictly OpenRouter follows your request:

```java
// require_parameters: only route to endpoints that support ALL request parameters.
// Without this, endpoints lacking structured_outputs support silently IGNORE a
// responseSchema and answer with free-form text. If no endpoint qualifies,
// OpenRouter responds with HTTP 404 ("No endpoints found that can handle the
// requested parameters").
client.chat().completion()
        .model("deepseek/deepseek-v4-flash-0731")
        .requireParameters(true)
        .responseSchema(mySchema)
        .addMessage("user", "Hello!")
        .execute();

// allow_fallbacks=false: pin the request strictly to the providers in the order
// list - no silent fallback to other providers if they are unavailable.
client.chat().completion()
        .model("deepseek/deepseek-v4-flash-0731")
        .provider("alibaba")
        .allowFallbacks(false)
        .addMessage("user", "Hello!")
        .execute();
```

See `OpenRouterChatCompletionWithRequireParametersExample` and
`OpenRouterChatCompletionWithAllowFallbacksExample` in the examples module.

Further routing controls are available on the same builder and all emitted inside the
same `provider` object: `dataCollection` (`"allow"`/`"deny"`), `ignoreProviders`/`onlyProviders`,
`maxPrice` (per-million-token price caps), `quantizations`, `sort` (`"price"`, `"throughput"`,
`"latency"`) and `enforceDistillableText`. The performance thresholds
`preferredMaxLatency`/`preferredMinThroughput` (plain number or percentile cutoffs p50/p75/p90/p99)
deprioritize endpoints beyond the threshold instead of excluding them, and
`sortBy(criterion, partition)` is the sort-object form whose `partition: "none"` sorts all
endpoints together regardless of model - the documented way to let a `models(...)` fallback
list pick whichever model is fastest right now. See
`OpenRouterChatCompletionWithProviderPreferencesExample`.

### Streaming Example

The [streaming example](openrouter4j-examples/src/main/java/de/entwicklertraining/openrouter4j/examples/OpenRouterChatCompletionStreamingExample.java)
demonstrates real-time token streaming:

```java
OpenRouterClient client = new OpenRouterClient();

StreamingResponseHandler<String> handler = new StreamingResponseHandler<>() {
    @Override
    public void onStreamStart() {
        System.out.println("=== Streaming started ===");
    }

    @Override
    public void onData(String chunk) {
        System.out.print(chunk);  // Print each token as it arrives
    }

    @Override
    public void onComplete() {
        System.out.println("\n=== Streaming completed ===");
    }

    @Override
    public void onError(Throwable throwable) {
        System.err.println("Error: " + throwable.getMessage());
    }
};

client.chat().completion()
        .model("deepseek/deepseek-v4-flash-0731")
        .addMessage("user", "Explain streaming APIs in 3-4 sentences.")
        .stream(handler)  // Enable streaming with handler
        .executeAsync()
        .get();
```

### Streaming with Function Calling

The [streaming + function calling example](openrouter4j-examples/src/main/java/de/entwicklertraining/openrouter4j/examples/OpenRouterChatCompletionStreamingWithFunctionCallingExample.java)
demonstrates both features combined. The library automatically handles the tool-call loop while streaming the final answer:

```java
StreamingToolCallHandler handler = new StreamingToolCallHandler() {
    @Override
    public void onData(String chunk) {
        System.out.print(chunk);  // Streamed token by token
    }

    @Override
    public void onToolCallDetected(String name, String id, JSONObject args) {
        System.out.println("Tool called: " + name);
    }

    @Override
    public void onFinalComplete() {
        System.out.println("\nDone!");
    }

    @Override public void onComplete() {}
    @Override public void onError(Throwable t) { t.printStackTrace(); }
};

client.chat().completion()
        .model("deepseek/deepseek-v4-flash-0731")
        .addTool(weatherTool)
        .stream(handler)
        .addMessage("user", "What's the weather in Berlin?")
        .execute();
```

### Configuring the Client

`OpenRouterClient` accepts an `ApiClientSettings` object for fine-grained control over retries and timeouts.
The API key is automatically read from the `OPENROUTER_API_KEY` environment variable, or can be configured
via `ApiHttpConfiguration`:

App attribution (app URL, display name, marketplace categories) can be configured once on the client via
`client.appAttribution(...)` - the headers (`HTTP-Referer`, `X-OpenRouter-Title`, `X-OpenRouter-Categories`)
are then sent with every request. Per-request builder methods (`httpReferer`, `appTitle`, `appCategories`)
win for that single request; see `OpenRouterChatCompletionWithAppAttributionExample`.

```java
// Option 1: Use environment variable (recommended)
OpenRouterClient client = new OpenRouterClient();

// Option 2: Explicit API key via HTTP configuration
ApiHttpConfiguration httpConfig = ApiHttpConfiguration.builder()
        .header("Authorization", "Bearer your-openrouter-api-key")
        .build();
OpenRouterClient client = new OpenRouterClient(ApiClientSettings.builder().build(), httpConfig);
```

## Project Structure

The library follows a clear structure:

* **`OpenRouterClient`** – entry point for all API calls. Extends `ApiClient` from *api-base*
  and registers error handling. Currently exposes the chat completion endpoint via `chat()`.
* **Request/Response classes** – located in the `chat.completion` package.
  Each request extends `OpenRouterRequest` and has an inner `Builder` that extends
  `ApiRequestBuilderBase` from *api-base*. Responses extend `OpenRouterResponse`.
* **Tool calling** – defined via `OpenRouterToolDefinition` and handled by
  `OpenRouterToolsCallback` and `OpenRouterToolCallContext`.
* **Structured outputs** – use `OpenRouterJsonSchema` for defining response schemas.

The `openrouter4j-examples` module demonstrates various use cases and can be used as a quick start.

## Extending OpenRouter4J

1. **Create a Request** – subclass `OpenRouterRequest` and implement `getRelativeUrl`,
   `getHttpMethod`, `getBody` and `createResponse`. Provide a nested builder
   extending `ApiRequestBuilderBase`.
2. **Create a Response** – subclass `OpenRouterResponse` and parse the JSON payload
   returned by OpenRouter.
3. **Expose a builder** – add a convenience method in `OpenRouterClient` returning your
   new builder so users can call it fluently.

Thanks to *api-base*, sending the request is handled by calling
`client.sendRequest(request)` or by using the builder's `execute()` method which
internally delegates to `sendRequest` with optional exponential backoff.
See [api-base's Readme](https://github.com/hwalde/api-base) for details on available
settings like retries, timeouts or capture hooks.

## Building

This project uses Maven. Compile the library and run examples with:

```bash
mvn package
```

## License

OpenRouter4J is distributed under the MIT License as defined in the project `pom.xml`.
