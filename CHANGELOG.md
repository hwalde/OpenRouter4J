# Changelog

All notable changes to this project will be documented in this file.

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
