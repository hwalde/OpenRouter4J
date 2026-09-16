package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.publicdata.OpenRouterAppRanking;
import de.entwicklertraining.openrouter4j.publicdata.OpenRouterAppRankingsResponse;
import de.entwicklertraining.openrouter4j.publicdata.OpenRouterBenchmarksResponse;
import de.entwicklertraining.openrouter4j.publicdata.OpenRouterMacroCategory;
import de.entwicklertraining.openrouter4j.publicdata.OpenRouterRankingsDailyResponse;
import de.entwicklertraining.openrouter4j.publicdata.OpenRouterRankingsDailyRow;
import de.entwicklertraining.openrouter4j.publicdata.OpenRouterSessionCostResponse;
import de.entwicklertraining.openrouter4j.publicdata.OpenRouterSessionCostRow;
import de.entwicklertraining.openrouter4j.publicdata.OpenRouterTaskClassification;
import de.entwicklertraining.openrouter4j.publicdata.OpenRouterTaskClassificationsResponse;

import java.util.List;

/**
 * Demonstrates the public data endpoints (read-only, any valid API key):
 *
 * - GET /benchmarks (unified benchmark rows from several sources)
 * - GET /datasets/app-rankings (top apps by token usage)
 * - GET /datasets/rankings-daily (daily token totals for the top 50 models)
 * - GET /datasets/session-cost (cost per session by harness and model)
 * - GET /classifications/task (task-classification market share)
 */
public class OpenRouterPublicDataExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // 1. Benchmarks: rows differ per source; the typed surface covers the
        //    meta, the rows stay raw JSON objects.
        OpenRouterBenchmarksResponse benchmarks = client.benchmarks()
                .source("openrouter")
                .benchmarkType("gpqa_diamond")
                .maxResults(5)
                .execute();
        System.out.println("Benchmarks (gpqa_diamond): " + benchmarks.items().size()
                + " rows as of " + benchmarks.asOf());
        benchmarks.items().forEach(row ->
                System.out.println("  " + row.optString("display_name")
                        + " accuracy " + row.opt("accuracy")));

        // 2. Top apps by token usage in a date window.
        OpenRouterAppRankingsResponse apps = client.datasets().appRankings()
                .category("coding")
                .sort("popular")
                .limit(5)
                .execute();
        List<OpenRouterAppRanking> appRows = apps.items();
        System.out.println("Top apps (window " + apps.startDate() + " .. " + apps.endDate() + "):");
        for (OpenRouterAppRanking app : appRows) {
            System.out.printf("  #%d %s - %s tokens in %s requests%n",
                    app.rank(), app.appName(), app.totalTokens(), app.totalRequests());
        }

        // 3. Daily token totals for the top 50 models; the reserved
        //    permaslug "other" aggregates everything outside the top 50.
        OpenRouterRankingsDailyResponse daily = client.datasets().rankingsDaily()
                .period("day")
                .execute();
        for (OpenRouterRankingsDailyRow row : daily.items()) {
            System.out.printf("  %s %s%s: %s tokens%n",
                    row.date(),
                    row.modelPermaslug(),
                    row.isOther() ? " (aggregated)" : "",
                    row.totalTokens());
        }

        // 4. Cost per session by harness and model.
        OpenRouterSessionCostResponse sessions = client.datasets().sessionCost()
                .turnRange("10-49-turns")
                .limit(5)
                .execute();
        for (OpenRouterSessionCostRow row : sessions.items()) {
            System.out.printf("  %s on %s: median %.2f USD/session%n",
                    row.appSlug(), row.modelPermaslug(), row.medianSessionCostUsd());
        }

        // 5. Task-classification market share (fractions 0..1, "other" excluded).
        OpenRouterTaskClassificationsResponse classifications = client.taskClassifications()
                .window("7d")
                .execute();
        for (OpenRouterTaskClassification classification : classifications.classifications()) {
            System.out.printf("  %s: usage %.2f, token %.2f (%d top models)%n",
                    classification.displayName(),
                    classification.usageShare(),
                    classification.tokenShare(),
                    classification.models().size());
        }
        for (OpenRouterMacroCategory macro : classifications.macroCategories()) {
            System.out.printf("  macro %s (%s): usage %.2f%n",
                    macro.key(), macro.label(), macro.usageShare());
        }
    }
}
