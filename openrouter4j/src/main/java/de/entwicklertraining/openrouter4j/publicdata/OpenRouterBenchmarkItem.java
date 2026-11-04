package de.entwicklertraining.openrouter4j.publicdata;

import org.json.JSONObject;

/**
 * A typed, discriminated view of one benchmark row of {@code GET /benchmarks}
 * (the {@code data[]} items of {@link OpenRouterBenchmarksResponse}).
 *
 * <p>The rows are heterogeneous - one of four schema variants - discriminated
 * by {@link #source()} and, within the {@code openrouter} source, by
 * {@link #benchmarkType()}:
 * <ul>
 *   <li>{@code artificial-analysis} - {@link #isArtificialAnalysis()}: the
 *       Artificial Analysis index scores ({@link #intelligenceIndex()},
 *       {@link #codingIndex()}, {@link #agenticIndex()}).</li>
 *   <li>{@code design-arena} - {@link #isDesignArena()}: arena rankings
 *       ({@link #elo()}, {@link #winRate()}, {@link #tournamentStats()}, ...).</li>
 *   <li>{@code openrouter} - {@link #isOpenrouter()}: OpenRouter's own evals
 *       ({@link #accuracy()}, {@link #totalTasks()}, ...).</li>
 *   <li>{@code openrouter} with a {@code search_*} {@code benchmark_type} -
 *       {@link #isSearch()}: the search benchmarks
 *       ({@code search_browsecomp}, {@code search_hle}, {@code search_dsqa},
 *       {@code search_widesearch}). The last two variants share the
 *       {@code openrouter} source, so discrimination is by the presence of a
 *       {@code search_*} {@code benchmark_type}, not by the source alone.</li>
 * </ul>
 *
 * <p>Every accessor answers {@code null} for absent or malformed values and for
 * fields that belong to another variant, which conforming rows omit - a row is
 * always readable through the same type. {@link #benchmarkType()} (and the derived {@link #isSearch()})
 * accept unknown {@code benchmark_type} spellings verbatim (the schema allows
 * unknown values; the four {@code search_*} names above are the documented
 * ones). Use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterBenchmarkItem {

    private final JSONObject json;

    OpenRouterBenchmarkItem(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw JSON row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
 * JSON path: {@code source} - benchmark source discriminator
 * ({@code artificial-analysis}, {@code design-arena} or {@code openrouter});
 * unknown values pass through verbatim.
 */
    public String source() {
        Object value = json.opt("source");
        return value instanceof String text ? text : null;
    }

    /**
     * @return {@code true} for an Artificial Analysis row
     *         ({@code source: "artificial-analysis"})
     */
    public boolean isArtificialAnalysis() {
        return "artificial-analysis".equals(source());
    }

    /**
     * @return {@code true} for a Design Arena row
     *         ({@code source: "design-arena"})
     */
    public boolean isDesignArena() {
        return "design-arena".equals(source());
    }

    /**
     * @return {@code true} for an OpenRouter row ({@code source: "openrouter"}),
     *         which covers both the ordinary eval rows and the search benchmark
     *         rows - use {@link #isSearch()} to separate the two
     */
    public boolean isOpenrouter() {
        return "openrouter".equals(source());
    }

    /**
     * @return {@code true} for a search benchmark row: {@code source} is
     *         {@code openrouter} and {@code benchmark_type} starts with
     *         {@code search_} (the documented values are {@code search_browsecomp},
     *         {@code search_hle}, {@code search_dsqa} and
     *         {@code search_widesearch}; unknown {@code search_*} spellings
     *         count as search too)
     */
    public boolean isSearch() {
        String type = benchmarkType();
        return isOpenrouter() && type != null && type.startsWith("search_");
    }

    /**
 * JSON path: {@code display_name} - human-readable model name (the exact
 * wording differs per source).
 */
    public String displayName() {
        Object value = json.opt("display_name");
        return value instanceof String text ? text : null;
    }
    /**
 * JSON path: {@code model_permaslug} - stable OpenRouter model identifier (on
 * Design Arena rows without a mapping this is the upstream Design Arena model
 * id instead).
 */
    public String modelPermaslug() {
        Object value = json.opt("model_permaslug");
        return value instanceof String text ? text : null;
    }
    /**
 * JSON path: {@code benchmark_type} - the OpenRouter evaluation type on
 * {@code openrouter} rows (e.g. {@code gpqa_diamond} or a {@code search_*}
 * value), absent on the other variants. Unknown values pass through verbatim.
 */
    public String benchmarkType() {
        Object value = json.opt("benchmark_type");
        return value instanceof String text ? text : null;
    }
    /**
 * JSON path: {@code pricing.prompt} - prompt-side price as a decimal string
 * (present on Artificial Analysis and Design Arena rows, {@code null} when
 * pricing is unavailable there). Do not parse to
 * {@code double}; use {@code BigDecimal}.
 */
    public String pricingPrompt() {
        JSONObject pricing = json.optJSONObject("pricing");
        if (pricing == null) {
            return null;
        }
        Object value = pricing.opt("prompt");
        return value instanceof String text ? text : null;
    }
    /**
 * JSON path: {@code pricing.completion} - completion-side price as a decimal
 * string (present on Artificial Analysis and Design Arena rows, {@code null}
 * when pricing is unavailable there). Do not parse
 * to {@code double}; use {@code BigDecimal}.
 */
    public String pricingCompletion() {
        JSONObject pricing = json.optJSONObject("pricing");
        if (pricing == null) {
            return null;
        }
        Object value = pricing.opt("completion");
        return value instanceof String text ? text : null;
    }

    /**
 * JSON path: {@code intelligence_index} - Artificial Analysis Intelligence
 * Index composite score, higher is better ({@code null} when the model was not
 * evaluated on that axis; Artificial Analysis rows only).
 */
    public Double intelligenceIndex() {
        Object value = json.opt("intelligence_index");
        return value instanceof Number number ? number.doubleValue() : null;
    }
    /**
 * JSON path: {@code coding_index} - Artificial Analysis Coding Index composite
 * score, higher is better ({@code null} when the model was not evaluated on
 * that axis; Artificial Analysis rows only).
 */
    public Double codingIndex() {
        Object value = json.opt("coding_index");
        return value instanceof Number number ? number.doubleValue() : null;
    }
    /**
 * JSON path: {@code agentic_index} - Artificial Analysis Agentic Index
 * composite score, higher is better ({@code null} when the model was not
 * evaluated on that axis; Artificial Analysis rows only).
 */
    public Double agenticIndex() {
        Object value = json.opt("agentic_index");
        return value instanceof Number number ? number.doubleValue() : null;
    }

    /**
 * JSON path: {@code arena} - the arena this ranking belongs to (Design Arena
 * rows only).
 */
    public String arena() {
        Object value = json.opt("arena");
        return value instanceof String text ? text : null;
    }
    /**
 * JSON path: {@code category} - the category within the arena (Design Arena
 * rows only).
 */
    public String category() {
        Object value = json.opt("category");
        return value instanceof String text ? text : null;
    }
    /**
 * JSON path: {@code elo} - ELO rating from head-to-head arena battles (Design
 * Arena rows only).
 */
    public Double elo() {
        Object value = json.opt("elo");
        return value instanceof Number number ? number.doubleValue() : null;
    }
    /**
 * JSON path: {@code win_rate} - win rate as a percentage 0-100 (Design Arena
 * rows only).
 */
    public Double winRate() {
        Object value = json.opt("win_rate");
        return value instanceof Number number ? number.doubleValue() : null;
    }
    /**
 * JSON path: {@code avg_generation_time_ms} - average generation time in
 * milliseconds ({@code null} when unavailable; Design Arena rows only).
 */
    public Double avgGenerationTimeMs() {
        Object value = json.opt("avg_generation_time_ms");
        return value instanceof Number number ? number.doubleValue() : null;
    }
    /**
 * JSON path: {@code tournament_stats} - the placement distribution from
 * tournament matches (Design Arena rows only), or {@code null} when absent.
 */
    public OpenRouterBenchmarkTournamentStats tournamentStats() {
        JSONObject stats = json.optJSONObject("tournament_stats");
        return stats != null ? new OpenRouterBenchmarkTournamentStats(stats) : null;
    }

    /**
 * JSON path: {@code accuracy} - aggregate accuracy score from 0 to 1, higher
 * is better (OpenRouter eval rows only).
 */
    public Double accuracy() {
        Object value = json.opt("accuracy");
        return value instanceof Number number ? number.doubleValue() : null;
    }
    /**
 * JSON path: {@code accuracy_stddev} - standard deviation of run accuracy
 * ({@code null} for a single run; OpenRouter eval rows only).
 */
    public Double accuracyStddev() {
        Object value = json.opt("accuracy_stddev");
        return value instanceof Number number ? number.doubleValue() : null;
    }
    /**
 * JSON path: {@code avg_cost_per_task} - average cost per task in USD
 * ({@code null} when unavailable; OpenRouter eval and search rows).
 */
    public Double avgCostPerTask() {
        Object value = json.opt("avg_cost_per_task");
        return value instanceof Number number ? number.doubleValue() : null;
    }
    /**
 * JSON path: {@code total_tasks} - tasks evaluated across the published runs
 * (OpenRouter eval and search rows).
 */
    public Long totalTasks() {
        Object value = json.opt("total_tasks");
        return value instanceof Number number ? number.longValue() : null;
    }
    /**
 * JSON path: {@code last_run_timestamp} - timestamp of the newest qualifying
 * run (OpenRouter eval and search rows).
 */
    public String lastRunTimestamp() {
        Object value = json.opt("last_run_timestamp");
        return value instanceof String text ? text : null;
    }

    /**
 * JSON path: {@code avg_latency_per_task_ms} - average wall-clock latency per
 * task in milliseconds ({@code null} when unavailable; search rows only).
 */
    public Double avgLatencyPerTaskMs() {
        Object value = json.opt("avg_latency_per_task_ms");
        return value instanceof Number number ? number.doubleValue() : null;
    }
    /**
 * JSON path: {@code primary_score} - the headline score from 0 to 1 (search
 * rows only). Its meaning is identified by {@link #primaryMetric()}: higher is
 * better in both cases.
 */
    public Double primaryScore() {
        Object value = json.opt("primary_score");
        return value instanceof Number number ? number.doubleValue() : null;
    }
    /**
 * JSON path: {@code primary_metric} - what {@link #primaryScore()} means:
 * {@code f1_by_item} for WideSearch, {@code accuracy} for the other search
 * benchmarks. Unknown values pass through verbatim (search rows only).
 */
    public String primaryMetric() {
        Object value = json.opt("primary_metric");
        return value instanceof String text ? text : null;
    }
    /**
 * JSON path: {@code search_engine} - the search engine the published
 * configuration used (search rows only).
 */
    public String searchEngine() {
        Object value = json.opt("search_engine");
        return value instanceof String text ? text : null;
    }
    /**
 * JSON path: {@code search_surface} - the request surface the published
 * configuration went through: {@code server-tool} or {@code plugin}. Unknown
 * values pass through verbatim (search rows only).
 */
    public String searchSurface() {
        Object value = json.opt("search_surface");
        return value instanceof String text ? text : null;
    }
    /**
 * JSON path: {@code run_config} - the published lane configuration (search
 * rows only, and only when requested with {@code include_run_config=true}),
 * or {@code null} when absent.
 */
    public OpenRouterBenchmarkSearchRunConfig runConfig() {
        JSONObject config = json.optJSONObject("run_config");
        return config != null ? new OpenRouterBenchmarkSearchRunConfig(config) : null;
    }
}
