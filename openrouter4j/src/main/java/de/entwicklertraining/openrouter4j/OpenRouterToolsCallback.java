package de.entwicklertraining.openrouter4j;

/**
 * A functional interface for implementing the callback when OpenRouter calls a function.
 */
@FunctionalInterface
public interface OpenRouterToolsCallback {
    OpenRouterToolResult handle(OpenRouterToolCallContext context);
}
