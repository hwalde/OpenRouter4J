package de.entwicklertraining.openrouter4j.chat.completion;

/**
 * Represents a single safety setting line for OpenRouter.
 * e.g. { "category": "HARM_CATEGORY_HARASSMENT", "threshold": "BLOCK_ONLY_HIGH" }
 */
public record OpenRouterSafetySetting(String category, String threshold) {
}
