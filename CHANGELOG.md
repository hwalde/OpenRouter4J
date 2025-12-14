# Changelog

All notable changes to this project will be documented in this file.

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
