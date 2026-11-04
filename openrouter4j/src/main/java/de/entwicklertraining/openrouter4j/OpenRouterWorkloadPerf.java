package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

/**
 * A typed view of one entry of {@code perf_last_30m_by_workload} on an
 * endpoint row: the performance of one kind of request served over the last
 * 30 minutes (the workload keys are {@code text_generation},
 * {@code image_generation}, {@code embeddings}, {@code rerank}, {@code stt},
 * {@code tts}, {@code video_generation}, {@code decisions} and
 * {@code unknown}).
 *
 * <p>Trap: {@link #latency()} is milliseconds, but image and video
 * generation report full end-to-end generation time because their raw
 * latency only measures request acknowledgement; every other workload
 * reports time to first token or result. {@link #throughput()} is only
 * meaningful for text generation - {@code null} for workloads without token
 * throughput. The percentiles are only visible when authenticated with an
 * API key or cookie.
 *
 * <p>These per-workload figures are additive to the legacy singular
 * {@code latency_last_30m} / {@code throughput_last_30m} fields of the same
 * row.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterWorkloadPerf {

    private final JSONObject json;

    /**
     * Wraps one workload entry. A {@code null} argument is read as an empty
     * object - every accessor answers {@code null}.
     *
     * @param json the per-workload performance object
     */
    public OpenRouterWorkloadPerf(JSONObject json) {
        this.json = json != null ? json : new JSONObject();
    }

    /**
     * @return the raw workload entry behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code latency} - the latency percentiles of this workload
     * in milliseconds (time to first token, or end-to-end generation time
     * for image and video generation).
     *
     * @return the percentiles, or {@code null} when absent or not an object
     */
    public OpenRouterPercentileStats latency() {
        JSONObject latency = json.optJSONObject("latency");
        return latency != null ? new OpenRouterPercentileStats(latency) : null;
    }

    /**
     * JSON path: {@code throughput} - the throughput percentiles of this
     * workload in tokens per second ({@code pN} is the speed that N% of
     * requests reached or exceeded, so {@code p90} is lower than
     * {@code p50}). Only meaningful for text generation.
     *
     * @return the percentiles, or {@code null} when absent, not an object
     *         or JSON {@code null} (workloads without token throughput)
     */
    public OpenRouterPercentileStats throughput() {
        JSONObject throughput = json.optJSONObject("throughput");
        return throughput != null ? new OpenRouterPercentileStats(throughput) : null;
    }

    /**
     * JSON path: {@code request_count} - total requests admitted for this
     * workload in the window.
     *
     * @return the value, or {@code null} when absent, not a number or JSON
     *         {@code null}
     */
    public Long requestCount() {
        Object value = json.opt("request_count");
        return value instanceof Number number ? number.longValue() : null;
    }
}
