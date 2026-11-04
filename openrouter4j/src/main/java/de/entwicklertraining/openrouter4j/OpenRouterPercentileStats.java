package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

/**
 * A typed view of one {@code PercentileStats} object: the p50/p75/p90/p99
 * performance percentiles of an endpoint metric. Which metric it is depends
 * on the field the object sits in - latency (milliseconds, time to first
 * token) or throughput (tokens per second, where {@code pN} is the speed
 * that N% of requests reached or exceeded, so {@code p90} is lower than
 * {@code p50}). The object is absent or {@code null} when the endpoint had
 * no traffic in the window, and it is only visible when authenticated with
 * an API key or cookie - unauthenticated requests read {@code null}.
 *
 * <p>Shared by the two views over the {@code PublicEndpoint} schema:
 * {@code OpenRouterModelEndpoint} (GET /models/{author}/{slug}/endpoints)
 * and {@code OpenRouterZdrEndpoint} (GET /endpoints/zdr).
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterPercentileStats {

    private final JSONObject json;

    /**
     * Wraps one percentile object. A {@code null} argument is read as an
     * empty object - every accessor answers {@code null}.
     *
     * @param json the {@code PercentileStats} object
     */
    public OpenRouterPercentileStats(JSONObject json) {
        this.json = json != null ? json : new JSONObject();
    }

    /**
     * @return the raw percentile object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code p50} - the 50th percentile (the median).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double p50() {
        return optDouble("p50");
    }

    /**
     * JSON path: {@code p75} - the 75th percentile.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double p75() {
        return optDouble("p75");
    }

    /**
     * JSON path: {@code p90} - the 90th percentile.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double p90() {
        return optDouble("p90");
    }

    /**
     * JSON path: {@code p99} - the 99th percentile.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double p99() {
        return optDouble("p99");
    }

    private Double optDouble(String key) {
        Object value = json.opt(key);
        return value instanceof Number number ? number.doubleValue() : null;
    }
}
