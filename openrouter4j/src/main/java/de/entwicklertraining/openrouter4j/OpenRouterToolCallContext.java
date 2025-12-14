package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

/**
 * Holds the arguments that the model is passing when it calls a "function" (tool).
 */
public record OpenRouterToolCallContext(JSONObject arguments) {}
