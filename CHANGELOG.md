# Changelog

All notable changes to this project will be documented in this file.

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
