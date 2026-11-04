package de.entwicklertraining.openrouter4j.publicdata;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the public-data requests (URL shape and typed query parameters) and
 * the response accessors against recorded JSON shapes of the OpenRouter
 * benchmarks, datasets and classification endpoints.
 */
class OpenRouterPublicDataTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void benchmarksRequestEmitsTypedQueryParameters() {
        OpenRouterBenchmarksRequest request = new OpenRouterBenchmarksRequest.Builder(client())
                .source("openrouter")
                .taskType("search")
                .benchmarkType("search_browsecomp")
                .includeRunConfig(true)
                .searchEngine("exa")
                .searchSurface("server-tool")
                .arena("models")
                .category("codecategories")
                .maxResults(10)
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/benchmarks"
                + "?source=openrouter"
                + "&task_type=search"
                + "&benchmark_type=search_browsecomp"
                + "&include_run_config=true"
                + "&search_engine=exa"
                + "&search_surface=server-tool"
                + "&arena=models"
                + "&category=codecategories"
                + "&max_results=10");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();

        OpenRouterBenchmarksRequest plain = new OpenRouterBenchmarksRequest.Builder(client()).build();
        assertThat(plain.getRelativeUrl()).isEqualTo("/benchmarks");
    }

    @Test
    void benchmarksResponseSurfacesRowsAndMeta() {
        OpenRouterBenchmarksResponse response = benchmarksResponseOf("""
                {
                  "data": [
                    {"agentic_index": 58.3, "coding_index": 65.8, "display_name": "GPT-4o",
                     "intelligence_index": 71.2, "model_permaslug": "openai/gpt-4o",
                     "source": "artificial-analysis"},
                    {"accuracy": 0.72, "benchmark_type": "gpqa_diamond", "display_name": "GPT-4o",
                     "model_permaslug": "openai/gpt-4o", "source": "openrouter", "total_tasks": 300}
                  ],
                  "meta": {"as_of": "2026-06-03T12:00:00Z", "model_count": 2, "version": "v1"}
                }
                """);

        assertThat(response.items()).hasSize(2);
        assertThat(response.items().get(0).getString("model_permaslug")).isEqualTo("openai/gpt-4o");
        assertThat(response.asOf()).isEqualTo("2026-06-03T12:00:00Z");
        assertThat(response.version()).isEqualTo("v1");
        assertThat(response.modelCount()).isEqualTo(2L);
        assertThat(response.source()).isNull();
        assertThat(response.taskType()).isNull();
    }

    @Test
    void benchmarksResponseReturnsEmptyAndNullWhenAbsent() {
        OpenRouterBenchmarksResponse response = benchmarksResponseOf("{}");
        assertThat(response.items()).isEmpty();
        assertThat(response.benchmarkItems()).isEmpty();
        assertThat(response.asOf()).isNull();
        assertThat(response.version()).isNull();
        assertThat(response.modelCount()).isNull();
    }

    @Test
    void appRankingsRequestEmitsTypedQueryParameters() {
        OpenRouterAppRankingsRequest request = new OpenRouterAppRankingsRequest.Builder(client())
                .category("coding")
                .subcategory("cli-agent")
                .sort("trending")
                .startDate("2026-04-12")
                .endDate("2026-05-11")
                .limit(20)
                .offset(50)
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/datasets/app-rankings"
                + "?category=coding"
                + "&subcategory=cli-agent"
                + "&sort=trending"
                + "&start_date=2026-04-12"
                + "&end_date=2026-05-11"
                + "&limit=20"
                + "&offset=50");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();

        OpenRouterAppRankingsRequest plain = new OpenRouterAppRankingsRequest.Builder(client()).build();
        assertThat(plain.getRelativeUrl()).isEqualTo("/datasets/app-rankings");
    }

    @Test
    void appRankingsResponseSurfacesTypedRows() {
        OpenRouterAppRankingsResponse response = appRankingsResponseOf("""
                {
                  "data": [
                    {"app_id": 12345, "app_name": "Cline", "rank": 1,
                     "total_requests": 4321, "total_tokens": "12345678"},
                    {"app_id": 67890, "app_name": "Roo Code", "rank": 2,
                     "total_requests": 2109, "total_tokens": "9876543"}
                  ],
                  "meta": {"as_of": "2026-05-12T02:00:00Z", "end_date": "2026-05-11",
                           "start_date": "2026-04-12", "version": "v1"}
                }
                """);

        List<OpenRouterAppRanking> items = response.items();
        assertThat(items).hasSize(2);

        OpenRouterAppRanking first = items.get(0);
        assertThat(first.rank()).isEqualTo(1);
        assertThat(first.appId()).isEqualTo(12345L);
        assertThat(first.appName()).isEqualTo("Cline");
        assertThat(first.totalTokens()).isEqualTo("12345678");
        assertThat(first.totalTokensAsLong()).isEqualTo(12345678L);
        assertThat(first.totalRequests()).isEqualTo(4321L);

        assertThat(response.asOf()).isEqualTo("2026-05-12T02:00:00Z");
        assertThat(response.startDate()).isEqualTo("2026-04-12");
        assertThat(response.endDate()).isEqualTo("2026-05-11");
        assertThat(response.version()).isEqualTo("v1");
    }

    @Test
    void appRankingsResponseReturnsEmptyAndNullWhenAbsent() {
        OpenRouterAppRankingsResponse response = appRankingsResponseOf("{}");
        assertThat(response.items()).isEmpty();
        assertThat(response.asOf()).isNull();
    }

    @Test
    void rankingsDailyRequestEmitsTypedQueryParameters() {
        OpenRouterRankingsDailyRequest request = new OpenRouterRankingsDailyRequest.Builder(client())
                .startDate("2026-04-12")
                .endDate("2026-05-11")
                .period("day")
                .modality("tool_calling")
                .contextBucket("100K")
                .category("coding")
                .languageType("programming")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/datasets/rankings-daily"
                + "?start_date=2026-04-12"
                + "&end_date=2026-05-11"
                + "&period=day"
                + "&modality=tool_calling"
                + "&context_bucket=100K"
                + "&category=coding"
                + "&language_type=programming");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();

        OpenRouterRankingsDailyRequest plain = new OpenRouterRankingsDailyRequest.Builder(client()).build();
        assertThat(plain.getRelativeUrl()).isEqualTo("/datasets/rankings-daily");
    }

    @Test
    void rankingsDailyResponseSurfacesTypedRowsAndIdentifiesTheOtherRow() {
        OpenRouterRankingsDailyResponse response = rankingsDailyResponseOf("""
                {
                  "data": [
                    {"date": "2026-05-11", "model_permaslug": "openai/gpt-4o-2024-05-13",
                     "total_tokens": "12345678"},
                    {"date": "2026-05-11", "model_permaslug": "anthropic/claude-3.5-sonnet-20241022",
                     "total_tokens": "9876543"},
                    {"date": "2026-05-11", "model_permaslug": "other", "total_tokens": "4321098"}
                  ],
                  "meta": {"as_of": "2026-05-12T02:00:00Z", "end_date": "2026-05-11",
                           "start_date": "2026-04-12", "version": "v1"}
                }
                """);

        List<OpenRouterRankingsDailyRow> items = response.items();
        assertThat(items).hasSize(3);
        assertThat(items.get(0).date()).isEqualTo("2026-05-11");
        assertThat(items.get(0).modelPermaslug()).isEqualTo("openai/gpt-4o-2024-05-13");
        assertThat(items.get(0).totalTokens()).isEqualTo("12345678");
        assertThat(items.get(0).totalTokensAsLong()).isEqualTo(12345678L);
        assertThat(items.get(0).isOther()).isFalse();
        assertThat(items.get(2).modelPermaslug()).isEqualTo("other");
        assertThat(items.get(2).isOther()).isTrue();
        assertThat(response.startDate()).isEqualTo("2026-04-12");
        assertThat(response.endDate()).isEqualTo("2026-05-11");
    }

    @Test
    void rankingsDailyResponseReturnsEmptyWhenAbsent() {
        assertThat(rankingsDailyResponseOf("{}").items()).isEmpty();
    }

    @Test
    void sessionCostRequestEmitsTypedQueryParameters() {
        OpenRouterSessionCostRequest request = new OpenRouterSessionCostRequest.Builder(client())
                .appSlug("hermes-agent")
                .model("anthropic/claude-4.8-opus")
                .turnRange("10-49-turns")
                .limit(50)
                .offset(100)
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/datasets/session-cost"
                + "?app_slug=hermes-agent"
                + "&model=anthropic%2Fclaude-4.8-opus"
                + "&turn_range=10-49-turns"
                + "&limit=50"
                + "&offset=100");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();

        OpenRouterSessionCostRequest plain = new OpenRouterSessionCostRequest.Builder(client()).build();
        assertThat(plain.getRelativeUrl()).isEqualTo("/datasets/session-cost");
    }

    @Test
    void sessionCostResponseSurfacesTypedRowsAndMeta() {
        OpenRouterSessionCostResponse response = sessionCostResponseOf("""
                {
                  "data": [
                    {"app_name": "Hermes Agent", "app_slug": "hermes-agent",
                     "median_session_cost_usd": 1.74, "model_permaslug": "anthropic/claude-4.8-opus",
                     "turn_range": "10-49-turns"}
                  ],
                  "meta": {"as_of": "2026-05-12T02:00:00.000Z", "version": "v1",
                           "window_days": 30, "window_end_date": "2026-05-11"}
                }
                """);

        List<OpenRouterSessionCostRow> items = response.items();
        assertThat(items).hasSize(1);
        assertThat(items.get(0).appSlug()).isEqualTo("hermes-agent");
        assertThat(items.get(0).appName()).isEqualTo("Hermes Agent");
        assertThat(items.get(0).modelPermaslug()).isEqualTo("anthropic/claude-4.8-opus");
        assertThat(items.get(0).turnRange()).isEqualTo("10-49-turns");
        assertThat(items.get(0).medianSessionCostUsd()).isEqualTo(1.74);

        assertThat(response.asOf()).isEqualTo("2026-05-12T02:00:00.000Z");
        assertThat(response.version()).isEqualTo("v1");
        assertThat(response.windowDays()).isEqualTo(30L);
        assertThat(response.windowEndDate()).isEqualTo("2026-05-11");
    }

    @Test
    void sessionCostResponseReturnsEmptyAndNullWhenAbsent() {
        OpenRouterSessionCostResponse response = sessionCostResponseOf("{}");
        assertThat(response.items()).isEmpty();
        assertThat(response.windowDays()).isNull();
    }

    @Test
    void taskClassificationsRequestEmitsTheWindowParameter() {
        OpenRouterTaskClassificationsRequest request =
                new OpenRouterTaskClassificationsRequest.Builder(client()).window("7d").build();
        assertThat(request.getRelativeUrl()).isEqualTo("/classifications/task?window=7d");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();

        OpenRouterTaskClassificationsRequest plain =
                new OpenRouterTaskClassificationsRequest.Builder(client()).build();
        assertThat(plain.getRelativeUrl()).isEqualTo("/classifications/task");
    }

    @Test
    void publicDataBuildersSupportTheVerbatimQueryParameterEscapeHatch() {
        OpenRouterBenchmarksRequest benchmarks =
                new OpenRouterBenchmarksRequest.Builder(client())
                        .queryParam("min_age_days", 30)
                        .build();
        assertThat(benchmarks.getRelativeUrl()).isEqualTo("/benchmarks?min_age_days=30");

        OpenRouterAppRankingsRequest appRankings =
                new OpenRouterAppRankingsRequest.Builder(client())
                        .queryParam("future_param", "value")
                        .queryParam("ignored", null)
                        .build();
        assertThat(appRankings.getRelativeUrl())
                .isEqualTo("/datasets/app-rankings?future_param=value");

        OpenRouterRankingsDailyRequest rankingsDaily =
                new OpenRouterRankingsDailyRequest.Builder(client())
                        .queryParam("future_param", "value")
                        .build();
        assertThat(rankingsDaily.getRelativeUrl())
                .isEqualTo("/datasets/rankings-daily?future_param=value");

        OpenRouterSessionCostRequest sessionCost =
                new OpenRouterSessionCostRequest.Builder(client())
                        .queryParam("future_param", "value")
                        .build();
        assertThat(sessionCost.getRelativeUrl())
                .isEqualTo("/datasets/session-cost?future_param=value");

        OpenRouterTaskClassificationsRequest classifications =
                new OpenRouterTaskClassificationsRequest.Builder(client())
                        .queryParam("future_param", "value")
                        .queryParam("ignored", null)
                        .build();
        assertThat(classifications.getRelativeUrl())
                .isEqualTo("/classifications/task?future_param=value");
    }

    @Test
    void taskClassificationsResponseSurfacesClassificationsAndMacroCategories() {
        OpenRouterTaskClassificationsResponse response = classificationsResponseOf("""
                {
                  "data": {
                    "as_of": "2026-06-17",
                    "classifications": [
                      {"category_token_share": 0.48, "category_usage_share": 0.51,
                       "display_name": "Code Generation", "macro_category": "code",
                       "models": [
                         {"id": "openai/gpt-4.1-mini", "tag_token_share": 0.75,
                          "tag_usage_share": 0.55}
                       ],
                       "tag": "code:general_impl", "token_share": 0.31, "usage_share": 0.23}
                    ],
                    "macro_categories": [
                      {"key": "code", "label": "Code", "token_share": 0.52, "usage_share": 0.45}
                    ],
                    "window_days": 7
                  }
                }
                """);

        assertThat(response.asOf()).isEqualTo("2026-06-17");
        assertThat(response.windowDays()).isEqualTo(7L);

        List<OpenRouterTaskClassification> classifications = response.classifications();
        assertThat(classifications).hasSize(1);
        assertThat(classifications.get(0).tag()).isEqualTo("code:general_impl");
        assertThat(classifications.get(0).displayName()).isEqualTo("Code Generation");
        assertThat(classifications.get(0).macroCategory()).isEqualTo("code");
        assertThat(classifications.get(0).usageShare()).isEqualTo(0.23);
        assertThat(classifications.get(0).tokenShare()).isEqualTo(0.31);
        assertThat(classifications.get(0).categoryUsageShare()).isEqualTo(0.51);
        assertThat(classifications.get(0).categoryTokenShare()).isEqualTo(0.48);

        List<OpenRouterTaskClassificationModel> models = classifications.get(0).models();
        assertThat(models).hasSize(1);
        assertThat(models.get(0).id()).isEqualTo("openai/gpt-4.1-mini");
        assertThat(models.get(0).tagUsageShare()).isEqualTo(0.55);
        assertThat(models.get(0).tagTokenShare()).isEqualTo(0.75);

        List<OpenRouterMacroCategory> macroCategories = response.macroCategories();
        assertThat(macroCategories).hasSize(1);
        assertThat(macroCategories.get(0).key()).isEqualTo("code");
        assertThat(macroCategories.get(0).label()).isEqualTo("Code");
        assertThat(macroCategories.get(0).usageShare()).isEqualTo(0.45);
        assertThat(macroCategories.get(0).tokenShare()).isEqualTo(0.52);
    }

    @Test
    void taskClassificationsResponseReturnsEmptyWhenAbsent() {
        OpenRouterTaskClassificationsResponse response = classificationsResponseOf("{}");
        assertThat(response.classifications()).isEmpty();
        assertThat(response.macroCategories()).isEmpty();
        assertThat(response.asOf()).isNull();
        assertThat(response.windowDays()).isNull();
    }

    @Test
    void benchmarkItemsDiscriminateTheFourVariants() {
        OpenRouterBenchmarksResponse response = benchmarksResponseOf("""
                {
                  "data": [
                    {
                      "source": "artificial-analysis",
                      "display_name": "GPT-4o",
                      "model_permaslug": "openai/gpt-4o",
                      "pricing": {"prompt": "0.0000025", "completion": "0.00001"},
                      "intelligence_index": 71.2,
                      "coding_index": 65.8,
                      "agentic_index": 58.3
                    },
                    {
                      "source": "design-arena",
                      "display_name": "Claude Sonnet 4",
                      "model_permaslug": "anthropic/claude-sonnet-4",
                      "pricing": {"prompt": "0.000003", "completion": "0.000015"},
                      "arena": "models",
                      "category": "codecategories",
                      "elo": 1423.5,
                      "win_rate": 72.0,
                      "avg_generation_time_ms": 3200,
                      "tournament_stats": {"total": 27, "first_place": 12, "second_place": 8,
                                           "third_place": 5, "fourth_place": 2}
                    },
                    {
                      "source": "openrouter",
                      "display_name": "GPT-4o",
                      "model_permaslug": "openai/gpt-4o",
                      "benchmark_type": "gpqa_diamond",
                      "accuracy": 0.72,
                      "accuracy_stddev": 0.03,
                      "avg_cost_per_task": 0.002,
                      "last_run_timestamp": "2026-06-03T12:00:00Z",
                      "total_tasks": 300
                    },
                    {
                      "source": "openrouter",
                      "display_name": "GPT-4o",
                      "model_permaslug": "openai/gpt-4o",
                      "benchmark_type": "search_browsecomp",
                      "primary_metric": "accuracy",
                      "primary_score": 0.72,
                      "total_tasks": 100,
                      "avg_cost_per_task": 0.031,
                      "avg_latency_per_task_ms": 45210,
                      "search_engine": "exa",
                      "search_surface": "server-tool",
                      "last_run_timestamp": "2026-07-28T13:38:18Z",
                      "run_config": {"max_agent_turns": 25, "reasoning_effort": "high", "temperature": 0.2}
                    }
                  ]
                }
                """);

        List<OpenRouterBenchmarkItem> items = response.benchmarkItems();
        assertThat(items).hasSize(4);
        // the raw items() surface is untouched alongside the typed one
        assertThat(response.items()).hasSize(4);

        OpenRouterBenchmarkItem aa = items.get(0);
        assertThat(aa.isArtificialAnalysis()).isTrue();
        assertThat(aa.isDesignArena()).isFalse();
        assertThat(aa.isOpenrouter()).isFalse();
        assertThat(aa.isSearch()).isFalse();
        assertThat(aa.source()).isEqualTo("artificial-analysis");
        assertThat(aa.displayName()).isEqualTo("GPT-4o");
        assertThat(aa.modelPermaslug()).isEqualTo("openai/gpt-4o");
        assertThat(aa.benchmarkType()).isNull();
        assertThat(aa.pricingPrompt()).isEqualTo("0.0000025");
        assertThat(aa.pricingCompletion()).isEqualTo("0.00001");
        assertThat(aa.intelligenceIndex()).isEqualTo(71.2);
        assertThat(aa.codingIndex()).isEqualTo(65.8);
        assertThat(aa.agenticIndex()).isEqualTo(58.3);
        // variant fields of the other rows answer null
        assertThat(aa.elo()).isNull();
        assertThat(aa.accuracy()).isNull();
        assertThat(aa.primaryScore()).isNull();

        OpenRouterBenchmarkItem da = items.get(1);
        assertThat(da.isDesignArena()).isTrue();
        assertThat(da.arena()).isEqualTo("models");
        assertThat(da.category()).isEqualTo("codecategories");
        assertThat(da.elo()).isEqualTo(1423.5);
        assertThat(da.winRate()).isEqualTo(72.0);
        assertThat(da.avgGenerationTimeMs()).isEqualTo(3200.0);
        OpenRouterBenchmarkTournamentStats stats = da.tournamentStats();
        assertThat(stats.total()).isEqualTo(27);
        assertThat(stats.firstPlace()).isEqualTo(12);
        assertThat(stats.secondPlace()).isEqualTo(8);
        assertThat(stats.thirdPlace()).isEqualTo(5);
        assertThat(stats.fourthPlace()).isEqualTo(2);
        assertThat(stats.json().getInt("total")).isEqualTo(27);

        OpenRouterBenchmarkItem or = items.get(2);
        assertThat(or.isOpenrouter()).isTrue();
        assertThat(or.isSearch()).isFalse();
        assertThat(or.benchmarkType()).isEqualTo("gpqa_diamond");
        assertThat(or.accuracy()).isEqualTo(0.72);
        assertThat(or.accuracyStddev()).isEqualTo(0.03);
        assertThat(or.avgCostPerTask()).isEqualTo(0.002);
        assertThat(or.totalTasks()).isEqualTo(300L);
        assertThat(or.lastRunTimestamp()).isEqualTo("2026-06-03T12:00:00Z");
        assertThat(or.runConfig()).isNull();

        OpenRouterBenchmarkItem search = items.get(3);
        assertThat(search.isOpenrouter()).isTrue();
        assertThat(search.isSearch()).isTrue();
        assertThat(search.benchmarkType()).isEqualTo("search_browsecomp");
        assertThat(search.primaryMetric()).isEqualTo("accuracy");
        assertThat(search.primaryScore()).isEqualTo(0.72);
        assertThat(search.totalTasks()).isEqualTo(100L);
        assertThat(search.avgCostPerTask()).isEqualTo(0.031);
        assertThat(search.avgLatencyPerTaskMs()).isEqualTo(45210.0);
        assertThat(search.searchEngine()).isEqualTo("exa");
        assertThat(search.searchSurface()).isEqualTo("server-tool");
        assertThat(search.lastRunTimestamp()).isEqualTo("2026-07-28T13:38:18Z");
        OpenRouterBenchmarkSearchRunConfig config = search.runConfig();
        assertThat(config.maxAgentTurns()).isEqualTo(25);
        assertThat(config.reasoningEffort()).isEqualTo("high");
        assertThat(config.temperature()).isEqualTo(0.2);
        assertThat(config.json().getInt("max_agent_turns")).isEqualTo(25);
    }

    @Test
    void benchmarkItemsDiscriminateSearchFromOpenrouterEvalByBenchmarkType() {
        OpenRouterBenchmarksResponse response = benchmarksResponseOf("""
                {
                  "data": [
                    {"source": "openrouter", "benchmark_type": "gpqa_diamond"},
                    {"source": "openrouter", "benchmark_type": "search_hle"},
                    {"source": "openrouter", "benchmark_type": "search_unknown_future"},
                    {"source": "openrouter", "benchmark_type": "not_a_search_type"},
                    {"source": "artificial-analysis", "benchmark_type": "search_hle"},
                    {"source": "openrouter"}
                  ]
                }
                """);

        List<OpenRouterBenchmarkItem> items = response.benchmarkItems();
        // the search-vs-OR discrimination is by the search_* benchmark_type,
        // not by the shared "openrouter" source alone
        assertThat(items.get(0).isOpenrouter()).isTrue();
        assertThat(items.get(0).isSearch()).isFalse();
        assertThat(items.get(1).isSearch()).isTrue();
        // unknown search_* spellings count as search verbatim (unknown values allowed)
        assertThat(items.get(2).isSearch()).isTrue();
        assertThat(items.get(2).benchmarkType()).isEqualTo("search_unknown_future");
        assertThat(items.get(3).isSearch()).isFalse();
        // a search_* type on a non-openrouter source is no search row
        assertThat(items.get(4).isArtificialAnalysis()).isTrue();
        assertThat(items.get(4).isSearch()).isFalse();
        assertThat(items.get(5).isOpenrouter()).isTrue();
        assertThat(items.get(5).isSearch()).isFalse();
    }

    @Test
    void benchmarkItemsSwallowAbsentAndMalformedFields() {
        OpenRouterBenchmarksResponse response = benchmarksResponseOf("""
                {
                  "data": [
                    {"source": 42, "display_name": "GPT-4o", "intelligence_index": "high",
                     "pricing": {"prompt": 5, "completion": null}, "elo": "1423",
                     "tournament_stats": "none", "run_config": 7, "total_tasks": "300"},
                    "not an object",
                    {"source": "design-arena", "tournament_stats": {"total": "many"}}
                  ]
                }
                """);

        // non-object entries are dropped, like in items()
        List<OpenRouterBenchmarkItem> items = response.benchmarkItems();
        assertThat(items).hasSize(2);

        OpenRouterBenchmarkItem malformed = items.get(0);
        assertThat(malformed.source()).isNull();
        assertThat(malformed.isArtificialAnalysis()).isFalse();
        assertThat(malformed.isOpenrouter()).isFalse();
        assertThat(malformed.isSearch()).isFalse();
        // non-string strings and non-number numbers are swallowed to null
        assertThat(malformed.displayName()).isEqualTo("GPT-4o");
        assertThat(malformed.intelligenceIndex()).isNull();
        assertThat(malformed.pricingPrompt()).isNull();
        assertThat(malformed.pricingCompletion()).isNull();
        assertThat(malformed.elo()).isNull();
        assertThat(malformed.tournamentStats()).isNull();
        assertThat(malformed.runConfig()).isNull();
        assertThat(malformed.totalTasks()).isNull();

        // malformed fields inside present sub-objects are swallowed too
        OpenRouterBenchmarkItem partial = items.get(1);
        assertThat(partial.tournamentStats()).isNotNull();
        assertThat(partial.tournamentStats().total()).isNull();
        assertThat(partial.tournamentStats().firstPlace()).isNull();
    }

    private OpenRouterBenchmarksResponse benchmarksResponseOf(String json) {
        OpenRouterBenchmarksRequest request = new OpenRouterBenchmarksRequest.Builder(client()).build();
        return request.createResponse(json);
    }

    private OpenRouterAppRankingsResponse appRankingsResponseOf(String json) {
        OpenRouterAppRankingsRequest request = new OpenRouterAppRankingsRequest.Builder(client()).build();
        return request.createResponse(json);
    }

    private OpenRouterRankingsDailyResponse rankingsDailyResponseOf(String json) {
        OpenRouterRankingsDailyRequest request = new OpenRouterRankingsDailyRequest.Builder(client()).build();
        return request.createResponse(json);
    }

    private OpenRouterSessionCostResponse sessionCostResponseOf(String json) {
        OpenRouterSessionCostRequest request = new OpenRouterSessionCostRequest.Builder(client()).build();
        return request.createResponse(json);
    }

    private OpenRouterTaskClassificationsResponse classificationsResponseOf(String json) {
        OpenRouterTaskClassificationsRequest request =
                new OpenRouterTaskClassificationsRequest.Builder(client()).build();
        return request.createResponse(json);
    }
}
