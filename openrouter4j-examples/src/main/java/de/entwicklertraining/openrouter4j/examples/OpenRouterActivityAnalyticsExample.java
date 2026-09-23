package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.activity.OpenRouterActivityResponse;
import de.entwicklertraining.openrouter4j.activity.OpenRouterAnalyticsQueryResponse;
import de.entwicklertraining.openrouter4j.activity.OpenRouterActivityItem;
import org.json.JSONObject;

import java.util.List;

/**
 * Demonstrates the usage endpoints:
 *
 * - GET /activity - per-day, per-model, per-endpoint usage rows (typed)
 * - POST /analytics/query - the metric/dimension query engine (rows are
 *   free-form objects keyed by the requested metrics and dimensions)
 *
 * <p>OpenRouter requires a management key for both endpoints.
 */
public class OpenRouterActivityAnalyticsExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // 1. Activity rows for one day.
        OpenRouterActivityResponse activity = client.activity()
                .date("2026-09-14")
                .execute();

        System.out.println("Activity rows: " + activity.items().size());
        for (OpenRouterActivityItem item : activity.items()) {
            System.out.printf("  %s %s via %s - %d request(s), %d/%d tokens, %.4f USD%n",
                    item.date(),
                    item.model(),
                    item.providerName(),
                    item.requests(),
                    item.promptTokens(),
                    item.completionTokens(),
                    item.usage());
        }

        // 2. Analytics query: daily request counts per model over a window.
        OpenRouterAnalyticsQueryResponse analytics = client.analyticsQuery()
                .metrics("request_count", "cost")
                .dimensions("model")
                .granularity("day")
                .timeRange("2026-09-01T00:00:00Z", "2026-09-14T23:59:59Z")
                .orderBy("request_count", "desc")
                .limit(100)
                .execute();

        System.out.println("Analytics rows: " + analytics.rowCount()
                + " (query time " + analytics.queryTimeMs() + " ms, truncated: " + analytics.truncated() + ")");
        for (JSONObject row : analytics.rows()) {
            System.out.println("  " + row);
        }
        // Filter-resolution warnings do not fail the query:
        if (!analytics.warnings().isEmpty()) {
            System.out.println("Warnings: " + analytics.warnings());
        }

        // 3. Classifier dimensions and filters (custom tags; requires an active
        //    classifier on the workspace). Both objects must share one classifier_id.
        OpenRouterAnalyticsQueryResponse byClassifierTag = client.analyticsQuery()
                .metrics("request_count")
                .classifierDimensions("550e8400-e29b-41d4-a716-446655440000", "department")
                .classifierIncludeNulls(true)
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .classifierFilter("department", "eq", "Engineering")
                .classifierFilter("seats", "eq", 25)
                .classifierFilterIn("work_type", "in", List.of("consulting", "support"))
                .execute();

        System.out.println("Rows by department tag: " + byClassifierTag.rowCount());
    }
}
