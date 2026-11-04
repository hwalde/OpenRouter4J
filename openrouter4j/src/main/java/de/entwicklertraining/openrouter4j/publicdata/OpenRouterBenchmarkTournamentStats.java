package de.entwicklertraining.openrouter4j.publicdata;

import org.json.JSONObject;

/**
 * A typed view of the {@code tournament_stats} object of a Design Arena
 * benchmark row: the placement distribution from tournament matches.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterBenchmarkTournamentStats {

    private final JSONObject json;

    OpenRouterBenchmarkTournamentStats(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw JSON object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
 * JSON path: {@code total} - tournament matches counted.
 */
    public Integer total() {
        Object value = json.opt("total");
        return value instanceof Number number ? number.intValue() : null;
    }
    /**
 * JSON path: {@code first_place} - first-place finishes.
 */
    public Integer firstPlace() {
        Object value = json.opt("first_place");
        return value instanceof Number number ? number.intValue() : null;
    }
    /**
 * JSON path: {@code second_place} - second-place finishes.
 */
    public Integer secondPlace() {
        Object value = json.opt("second_place");
        return value instanceof Number number ? number.intValue() : null;
    }
    /**
 * JSON path: {@code third_place} - third-place finishes.
 */
    public Integer thirdPlace() {
        Object value = json.opt("third_place");
        return value instanceof Number number ? number.intValue() : null;
    }
    /**
 * JSON path: {@code fourth_place} - fourth-place finishes.
 */
    public Integer fourthPlace() {
        Object value = json.opt("fourth_place");
        return value instanceof Number number ? number.intValue() : null;
    }
}
