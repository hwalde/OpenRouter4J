package de.entwicklertraining.openrouter4j.publicdata;

import org.json.JSONObject;

/**
 * A typed view of the {@code run_config} object of a search benchmark row: the
 * published lane configuration (only the agent turn count, reasoning effort
 * and temperature are public - other harness settings are intentionally not
 * part of the API's contract). Present only when the request set
 * {@code include_run_config=true}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterBenchmarkSearchRunConfig {

    private final JSONObject json;

    OpenRouterBenchmarkSearchRunConfig(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw JSON object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
 * JSON path: {@code max_agent_turns} - agent-turn count for the published
 * lane ({@code null} for plugin lanes).
 */
    public Integer maxAgentTurns() {
        Object value = json.opt("max_agent_turns");
        return value instanceof Number number ? number.intValue() : null;
    }
    /**
 * JSON path: {@code reasoning_effort} - reasoning effort configured for the
 * published lane ({@code null} when omitted); unknown values pass through
 * verbatim.
 */
    public String reasoningEffort() {
        Object value = json.opt("reasoning_effort");
        return value instanceof String text ? text : null;
    }
    /**
 * JSON path: {@code temperature} - sampling temperature configured for the
 * published lane ({@code null} when omitted).
 */
    public Double temperature() {
        Object value = json.opt("temperature");
        return value instanceof Number number ? number.doubleValue() : null;
    }
}
