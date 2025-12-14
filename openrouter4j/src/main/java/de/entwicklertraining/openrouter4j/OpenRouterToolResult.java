package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

/**
 * Encapsulates the output from a tool invocation.
 */
public record OpenRouterToolResult(JSONObject content) {

    public static OpenRouterToolResult of(JSONObject content) {
        return new OpenRouterToolResult(content);
    }
}
