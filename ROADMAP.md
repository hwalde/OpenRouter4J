# OpenRouter4J Roadmap

## Planned Improvements

### Refactoring: Migrate from deprecated api-base methods

**Current State:**
- `OpenRouterChatCompletionCallHandler` uses deprecated `sendRequest()` and `sendRequestWithExponentialBackoff()` methods
- These methods return the concrete response type directly (e.g., `OpenRouterChatCompletionResponse`)

**Target State:**
- Use new api-base 2.x methods: `execute()`, `executeWithRetry()`, `executeAsync()`, `executeAsyncWithRetry()`
- These methods return `ApiResponse<?>` which requires casting

**Why it's not done yet:**
- OpenAI4J also still uses the deprecated methods
- The new methods require explicit casting: `(OpenRouterChatCompletionResponse) client.execute(request)`
- This is a breaking change for the CallHandler pattern

**When to do:**
- When OpenAI4J migrates to the new methods
- Or when api-base provides a better typed alternative
