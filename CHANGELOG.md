# Changelog

All notable changes to this project will be documented in this file.

## [1.9.0] - 2026-09-08
### Added
- **NEW**: Image resolution tiers - `addImageByUrl(String, OpenRouterImageDetail)` and `addImageByBase64(Path, OpenRouterImageDetail)` emit the `content[].image_url.detail` key (`auto`/`low`/`high`/`original`) on image content parts, for URL and base64 images alike. The key is only emitted when explicitly configured; the no-detail overloads behave exactly as before. Javadoc notes that `original` is an OpenRouter extension not present in the OpenAI Chat Completions spec and is downgraded to `high` on providers without an original-resolution tier. The tier survives the tool-call loop's follow-up requests (messages are carried verbatim - regression-tested).
- **NEW**: Response accessors for multimodal output on `OpenRouterChatCompletionResponse` - `images()` / `imageUrls()` surface `choices[0].message.images` (the URL/base64 payloads produced by image-generation models) and `audio()` / `audioId()` / `audioData()` / `audioExpiresAt()` / `audioTranscript()` surface `choices[0].message.audio` (id, base64 data, expiry, provider transcript). All follow the swallow-and-return-null/empty convention; javadoc notes the `modalities(...)` prerequisite.
- **NEW**: Response accessors for the logprobs round trip - `logprobs()` returns the raw `choices[0].logprobs` object (`content`/`refusal` arrays of per-token log probabilities) so the data requested via `logprobs(true)` / `topLogprobs(n)` becomes inspectable, and `systemFingerprint()` returns the top-level provider-side model snapshot.
- **NEW**: Examples for image resolution tiers (`OpenRouterChatCompletionWithVisionDetailExample`), logprobs (`OpenRouterChatCompletionWithLogprobsExample`), audio output (`OpenRouterChatCompletionWithAudioOutputExample`) and the streaming reasoning/refusal accessors (`OpenRouterChatCompletionStreamingReasoningExample`).

### Fixed
- **The streaming tool-call accumulator no longer drops the `reasoning`, `reasoning_details`, `refusal` and `audio` deltas.** The accumulator previously captured only `role`/`content`/`tool_calls` from each chunk's `delta`, so the synthetic response of the streaming loop reported an empty reasoning/refusal/audio side while the synchronous path exposed them - an asymmetry between the two execution paths of the same feature set. The deltas are now accumulated (reasoning and refusal as appended text; `reasoning_details` entries appended in arrival order; `audio` merged by concatenating `data` and `transcript` fragments and keeping the first `id`/`expires_at`) and the synthetic response exposes them through the existing accessors. Deliberate behaviour choice: reasoning and refusal deltas are still NOT forwarded as content chunks - the user handler keeps receiving content only; the accumulated values live on the synthetic response.
- **Chunk-level `service_tier`, `openrouter_metadata` and `system_fingerprint` are carried into the synthetic response of the streaming loop**, so `serviceTier()`, `openrouterMetadata()` and `systemFingerprint()` work on the streaming path too, matching the synchronous path. Previously they always returned `null` when streaming.

## [1.8.0] - 2026-09-07
### Added
- **NEW**: Predicted outputs support - `prediction(String)` / `predictionParts(String...)` / `predictionParts(List)` emit the `prediction` request object as `{"type":"content","content":...}` (string form wins when both are set) so supported models can reuse known content and answer faster. Response accessors `acceptedPredictionTokens()` / `rejectedPredictionTokens()` surface `usage.completion_tokens_details.accepted_prediction_tokens` / `rejected_prediction_tokens`.
- **NEW**: Prompt-caching controls at request root - `cacheControl()` / `cacheControl(String ttl)` emit `{"type":"ephemeral"[,"ttl":...]}` (automatic caching; supported by Anthropic, Google Vertex, Azure, Amazon Bedrock), `promptCacheKey(String)` emits the sticky-routing key `prompt_cache_key` (only used by OpenRouter when `session_id` is unset - precedence documented in the javadoc), and `promptCacheOptions(String mode)` / `promptCacheOptions(String mode, String ttl)` emit `{"mode":...[,"ttl":...]}` (e.g. `"explicit"` disables OpenAI-managed breakpoints).
- **NEW**: `reasoningSummary(String)` builder method - emits `reasoning.summary` (`auto`/`concise`/`detailed`) to control the verbosity of the reasoning summaries returned in `reasoning_details`; coexists with the existing reasoning options in one `reasoning` object.
- **NEW**: Capacity tiers - `serviceTier(String)` emits `service_tier` (`auto`/`default`/`fast`/`flex`/`priority`/`scale`; `fast` aliases `priority`) and `OpenRouterChatCompletionResponse.serviceTier()` returns the tier that actually served the request.
- **NEW**: Examples for predicted outputs (`OpenRouterChatCompletionWithPredictionExample`), prompt caching (`OpenRouterChatCompletionWithPromptCachingExample`), capacity tiers (`OpenRouterChatCompletionWithServiceTierExample`) and the reasoning summary verbosity (added as example 5 to `OpenRouterChatCompletionWithThinkingExample`).

## [1.7.0] - 2026-09-06
### Added
- **NEW**: OpenRouter server tools in the `tools` array - `serverTools(OpenRouterServerTool...)` / `serverTools(List)` / `addServerTool(OpenRouterServerTool)`. Server tools are executed by OpenRouter itself (results arrive as `server_tool_calls`, no client-side callback) and are emitted as `{"type":"<server-tool-type>"[, "parameters":{...}]}` into the same `tools` array as the function tools, freely mixed with them. Typed implementations: `OpenRouterWebSearchServerTool` (`openrouter:web_search`; engine, max_results, max_total_results, max_uses, max_characters, mode, search_context_size, allowed/excluded domains plus a verbatim `option(key, value)` escape hatch), `OpenRouterWebFetchServerTool` (`openrouter:web_fetch`) and `OpenRouterDatetimeServerTool` (`openrouter:datetime`); `OpenRouterServerTool.of(String)` / `OpenRouterGenericServerTool` is the verbatim escape hatch for every other (or future) server-tool type. The `tool_choice` / `parallel_tool_calls` emission invariant is unchanged: both are emitted only when the tools array is non-empty (function or server tools).
- **NEW**: Server-tool `tool_choice` form - `toolChoiceServerTool(String)` emits `{"type":"<server-tool-type>"}` (e.g. `openrouter:web_search`, `web_search`, `web_search_preview`); the type string is accepted verbatim because the API schema is free-form here on purpose. Precedence when several forms are set: named function form (`toolChoiceFunction`) > server-tool form > plain string keywords (`toolChoice`). Documented in the javadoc.
- **NEW**: `stop_server_tools_when` support - `stopServerToolsWhen(OpenRouterStopCondition...)` / `stopServerToolsWhen(List)` / `addStopServerToolsWhen(...)`. Typed condition factories for all five schema types: `OpenRouterStopCondition.stepCountIs(int)`, `hasToolCall(String)`, `maxTokensUsed(long)`, `maxCost(double)` and `finishReasonIs(String)`, plus `raw(JSONObject)` as the verbatim escape hatch (requires a `type` field). The array is emitted only when at least one condition is set. Javadoc documents the API semantics: any condition firing halts the server-tool agent loop (OR logic), when set it overrides `max_tool_calls`, and a firing condition leads to one final turn with tools disabled so the answer ends in natural language.
- **NEW**: Examples for server tools with stop conditions (`OpenRouterChatCompletionWithServerToolsExample`), the server-tool `tool_choice` form (`OpenRouterChatCompletionWithServerToolChoiceExample`) and streaming error handling (`OpenRouterChatCompletionStreamingErrorExample`).

### Fixed
- **Mid-stream error chunks are no longer silently dropped by the streaming tool-call accumulator.** OpenRouter reports mid-generation failures as a regular `data:` event with a top-level `error` object inside an otherwise valid HTTP 200 stream; the accumulator previously ignored it, so a failed stream looked exactly like an empty one. The chunk-level `error` object is now captured and the synthetic response of the streaming loop exposes it through the existing accessors (`hasError()`, `error()`, `errorCode()`, `errorMessage()`), matching the synchronous path. Deliberate behaviour choice: the streaming loop finishes normally after an error chunk (content already streamed stays streamed) and exposes the failure via the accessors instead of throwing - `throwOnError()` remains the opt-in loud check. Chunks without an `error` field behave exactly as before.

## [1.6.0] - 2026-09-05
### Added
- **NEW**: Server-side plugins support - `plugins(OpenRouterPlugin...)` / `plugins(List)` / `addPlugin(OpenRouterPlugin)` builder methods emitting the `plugins` request array (behind a null/empty check). `OpenRouterWebSearchPlugin` is the typed implementation of the `web` plugin (engine, max_results, max_uses, mode, search_prompt, include/exclude_domains, approximate user_location - only explicitly configured fields are emitted); `OpenRouterGenericPlugin` / `OpenRouterPlugin.of(String)` is the verbatim escape hatch for every other plugin id (or ids added by OpenRouter later). Javadoc documents the interaction with `tool_choice`: web search results reach the model as server-executed tool calls (`server_tool_calls`), so plugins and client-side tools are two independent tool-call flows.
- **NEW**: Multimodal output support - `modalities(String...)` / `modalities(List)` / `addModality(String)` (`modalities` request array selecting `text` / `image` / `audio`) and `imageConfig(OpenRouterImageConfig)` (`image_config`; typed conveniences for `num_images`, `aspect_ratio`, `resolution`, `quality` plus a verbatim `option(key, value)` escape hatch, since the API defines it as provider-specific free-form keys). Without `modalities("...", "image")`, image-output models never produce images.
- **NEW**: Response accessors for OpenRouter-specific fields on `OpenRouterChatCompletionResponse`: `reasoning()` and `reasoningDetails()` (chain-of-thought output), `nativeFinishReason()` (provider-native finish reason next to the normalised one), `provider()` (which provider served the request) and `openrouterMetadata()` (routing metadata, present only with the `metadataInResponse(true)` opt-in).
- **NEW**: Loud error path on `OpenRouterChatCompletionResponse`: `hasError()`, `error()`, `errorCode()`, `errorMessage()` and `throwOnError()`. OpenRouter reports mid-request failures as a top-level `error` object inside an otherwise valid HTTP 200 response; because the accessors swallow exceptions and return `null`, such a response previously looked like an *empty* one. The existing "swallow and return null" convention of the other accessors is deliberately unchanged - `throwOnError()` is the opt-in loud check.
- **NEW**: Usage extras on `OpenRouterChatCompletionResponse`: `cost()` (USD, `usage.cost`), `costDetails()` (`usage.cost_details`), `cachedPromptTokens()` (`usage.prompt_tokens_details.cached_tokens`) and `reasoningTokens()` (`usage.completion_tokens_details.reasoning_tokens`).
- **NEW**: The streaming tool-call accumulator now captures the terminal usage chunk (OpenRouter emits one final chunk with an empty `choices` array and a `usage` object; previously it was silently dropped) and exposes it on the synthetic response of the streaming loop, so `cost()` and the token accessors work on the streaming path too. The provider-native finish reason is carried into `native_finish_reason` of the synthetic response as well.
- **NEW**: Examples for the web search plugin (`OpenRouterChatCompletionWithWebSearchPluginExample`), image output (`OpenRouterChatCompletionWithImageOutputExample`) and the response details (`OpenRouterChatCompletionWithResponseDetailsExample`).

## [1.5.0] - 2026-09-04
### Added
- **NEW**: App-attribution support - `OpenRouterAppAttribution` value type plus client-level configuration via `OpenRouterClient.appAttribution(...)`. The values travel as `HTTP-Referer` (app URL, primary identifier for the leaderboards), `X-OpenRouter-Title` (display name; the legacy `X-Title` header remains an accepted API alias, the library always sends the current one) and `X-OpenRouter-Categories` (at most 2 marketplace categories; more throw an `IllegalArgumentException`). Per-request overrides via the new `httpReferer(String)`, `appTitle(String)` and `appCategories(String...)` builder methods win over the client-level default.
- **NEW**: `models(String...)` / `models(List)` builder method - the fallback model list. The single `model` key is always emitted alongside; per the API semantics `models` acts as fallback candidates tried in order when the primary model cannot serve the request.
- **NEW**: Observability parameters: `metadata(Map)` / `addMetadata(String, String)` (up to 16 string key/value pairs, keys max 64 chars, values max 512 chars - validated up front), `user(String)` (stable per-end-user identifier for abuse isolation) and `sessionId(String)` (sticky-routing key for prompt-cache hits; sets the `x-session-id` header in addition to the `session_id` body field, which takes precedence).
- **NEW**: `metadataInResponse(boolean)` builder method - sends the `X-OpenRouter-Metadata: enabled` opt-in header so the response may carry routing metadata under `openrouter_metadata` (the legacy `X-OpenRouter-Experimental-Metadata` header remains an accepted API alias).
- **NEW**: Extended provider preferences, all emitted inside the existing `provider` object: `dataCollection(String)` (`provider.data_collection`, `"allow"`/`"deny"`), `ignoreProviders(String...)` / `addIgnoreProvider(String)` (`provider.ignore`), `onlyProviders(String...)` / `addOnlyProvider(String)` (`provider.only`), `maxPrice(String prompt, String completion[, String image, String audio])` (`provider.max_price`, prices as strings), `quantizations(String...)` (`provider.quantizations`), `sort(String)` (`provider.sort`: `"price"`, `"throughput"`, `"latency"`) and `enforceDistillableText(Boolean)` (`provider.enforce_distillable_text`). The `provider` object is emitted when any of the routing options - old or new - is set.
- **NEW**: Examples for app attribution (`OpenRouterChatCompletionWithAppAttributionExample`), model fallbacks (`OpenRouterChatCompletionWithModelFallbacksExample`), observability (`OpenRouterChatCompletionWithObservabilityExample`) and provider preferences (`OpenRouterChatCompletionWithProviderPreferencesExample`).

### Changed
- **Copy lists replaced by a copy factory**: the tool-call loop's follow-up requests were built from two hand-maintained option copy lists in `OpenRouterChatCompletionCallHandler` (`buildNextRequest` / `buildStreamingRequest`) - the structure that silently dropped `requireParameters`/`allowFallbacks` in 1.4.0 (defect D1) and was the reason every new option needed the error-prone "step 5" pass. Both methods now delegate to a new package-private `copyForNextTurn(...)` on `OpenRouterChatCompletionRequest`, which reads every private field of the same class directly, so a newly added option can no longer be forgotten on follow-up requests. Behaviour is unchanged (covered by the propagation regression tests, now extended to all new options and their headers).

## [1.4.0] - 2026-09-03
### Added
- **NEW**: `reasoningEffort(String)`, `reasoningMaxTokens(Integer)`, `reasoningExclude(Boolean)` and `reasoningEnabled(Boolean)` builder methods - configure reasoning via the current `reasoning` object format (`effort`, `max_tokens`, `exclude`, `enabled`). See the Changed entry below for the replaced wire format.
- **NEW**: `maxCompletionTokens(Integer)` builder method - emits `max_completion_tokens`, the non-deprecated successor of `max_tokens`. If both variants are set, both keys are emitted verbatim and the API decides precedence.
- **NEW**: `toolChoiceFunction(String)` builder method - named `tool_choice` form, emits `{"type":"function","function":{"name":...}}` to force one specific tool. The string keywords ("auto", "required", "none") keep working; the named form wins when both are set.
- **NEW**: Sampling parameters as typed builder methods: `frequencyPenalty(Double)`, `presencePenalty(Double)`, `repetitionPenalty(Double)`, `seed(Integer)`, `minP(Double)`, `topA(Double)`, `logitBias(Map)`/`addLogitBias(Integer, Double)`, `logprobs(Boolean)` and `topLogprobs(Integer)`.
- **NEW**: Examples for reasoning (`OpenRouterChatCompletionWithThinkingExample`, rewritten), named tool choice (`OpenRouterChatCompletionWithNamedToolChoiceExample`) and sampling options (`OpenRouterChatCompletionWithSamplingOptionsExample`)

### Changed
- `thinking(Integer)` no longer sends the obsolete `"reasoning": {"type": "enabled", "budget": N}` shape. It now emits `"reasoning": {"max_tokens": N}`. This is a wire-format change of a published method, deliberately released as a **fix, not a breaking change**: the old format no longer exists in the OpenRouter API schema, so callers of `thinking()` have not received working reasoning behaviour to preserve - the request silently did not do what it said. Source and binary compatibility are fully intact; no code changes are required. The old JSON keys appear nowhere in the current OpenAPI schema (verified against https://openrouter.ai/openapi.yaml).

### Deprecated
- `thinking(Integer)` in favour of `reasoningMaxTokens(Integer)` (same emission, current name).
- `maxOutputTokens(Integer)` in favour of `maxCompletionTokens(Integer)`; `maxOutputTokens` keeps emitting the deprecated `max_tokens` key exactly as before - existing callers are unaffected, the deprecation only steers new code to the non-deprecated parameter.
- Accessor `thinkingBudget()` in favour of `reasoningMaxTokens()`; it keeps returning the same value.

### Fixed
- **Copy lists**: `requireParameters` and `allowFallbacks` were silently lost on follow-up requests of the tool-call loop (from turn 2 on), both synchronously and in streaming mode. The hand-maintained option copy lists in `OpenRouterChatCompletionCallHandler` now carry all options, and the copy methods are covered by tests.
- **Build**: `maven-surefire-plugin` is now pinned to 3.5.2. Maven's built-in default is 2.12.4, which cannot execute JUnit 5 tests: the suite reported `Tests run: 0` while the build stayed green, so no test in this project had ever actually run. The whole suite now executes (43 tests as of this release).
- **Build**: GPG signing moved into a `release` profile. It was bound to the `verify` phase, so `mvn verify` failed for anyone without a private signing key - every contributor and every CI run. Plain `mvn verify` now works without a key; releases use `mvn -Prelease deploy`.
- **Javadoc**: `OpenRouterResponse` was documented in German with unescaped generics, producing 14 `invalid input: '<'` warnings in the published javadoc jar. Rewritten in English with the generics escaped and the missing `@param <T>` added.
- **Javadoc**: `StreamingToolCallHandler` linked to `#onData(String)`, which does not exist on that type - `onData` is inherited as `onData(T)`. The link now resolves.

## [1.3.0] - 2026-06-04
### Added
- **NEW**: `requireParameters(boolean)` builder method - sets `provider.require_parameters` so OpenRouter only routes to endpoints that support ALL request parameters (e.g. structured outputs). Prevents the silent-ignore pitfall where a `responseSchema` is dropped without error on endpoints lacking `structured_outputs` support. If no endpoint qualifies, OpenRouter responds with HTTP 404 ("No endpoints found that can handle the requested parameters").
- **NEW**: `allowFallbacks(boolean)` builder method - sets `provider.allow_fallbacks`; with `false` the request is pinned strictly to the providers in the order list.
- **NEW**: Examples `OpenRouterChatCompletionWithRequireParametersExample` and `OpenRouterChatCompletionWithAllowFallbacksExample`

### Changed
- The `provider` object is now also emitted when only `require_parameters`/`allow_fallbacks` are set (previously it required a non-empty provider order list)

## [1.2.0] - 2026-04-01
### Added
- **NEW**: Combined streaming + function calling (tool use) support
- **NEW**: `StreamingToolCallHandler` interface with lifecycle events: `onToolCallDetected()`, `onToolExecuted()`, `onTurnComplete()`, `onFinalComplete()`
- **NEW**: Streaming + function calling example (`OpenRouterChatCompletionStreamingWithFunctionCallingExample`)
- When both `.stream(handler)` and `.addTool(tool)` are set, `execute()` and `executeAsync()` automatically orchestrate a streaming multi-turn tool-call loop

### Changed
- Updated api-base dependency from 2.2.1 to 2.2.2

## [1.1.1] - 2025-12-15
### Changed
- Updated api-base dependency from 2.2.0 to 2.2.1

## [1.1.0] - 2025-12-14
### Added
- **NEW**: Streaming support for chat completions using Server-Sent Events (SSE)
- **NEW**: `stream(boolean)` builder method to enable/disable streaming
- **NEW**: Integration with api-base 2.2.0 streaming infrastructure via `stream(StreamingResponseHandler)` method

### Changed
- **BREAKING**: Updated api-base dependency from 1.0.4 to 2.2.0
- **BREAKING**: `OpenRouterClient` now uses `ApiHttpConfiguration` for authentication instead of `ApiClientSettings.getBearerAuthenticationKey()`
- Builder now properly supports `execute()`, `executeAsync()`, `executeWithRetry()`, and `executeAsyncWithRetry()` from api-base
- `OpenRouterChatCompletionCallHandler` now uses `execute()` and `executeWithRetry()` instead of deprecated methods

### Migration Guide
- If you were using `ApiClientSettings.builder().setBearerAuthenticationKey("key")`, use `ApiHttpConfiguration.builder().header("Authorization", "Bearer key")` instead
- The environment variable `OPENROUTER_API_KEY` continues to work automatically

## [1.0.0] - 2025-12-14
### Added
- Initial open source release (general availability) of OpenRouter4J.
- Chat Completions with tool calling, structured outputs, and vision support.
- Access to 200+ AI models through OpenRouter's unified API.
- Provider selection for routing requests to specific providers.
- Comprehensive examples demonstrating all features.
