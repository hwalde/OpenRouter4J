package de.entwicklertraining.openrouter4j.activity;

import org.json.JSONObject;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the activity/analytics request shapes and the response accessors
 * against recorded JSON shapes of the OpenRouter usage endpoints.
 */
class OpenRouterActivityTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void activityRequestEmitsTypedQueryParameters() {
        OpenRouterActivityRequest request = new OpenRouterActivityRequest.Builder(client())
                .date("2025-08-24")
                .apiKeyHash("abc123")
                .userId("user-1")
                .groupBy("workspace")
                .workspaceId("550e8400-e29b-41d4-a716-446655440000")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/activity"
                + "?date=2025-08-24"
                + "&api_key_hash=abc123"
                + "&user_id=user-1"
                + "&group_by=workspace"
                + "&workspace_id=550e8400-e29b-41d4-a716-446655440000");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();

        OpenRouterActivityRequest plain = new OpenRouterActivityRequest.Builder(client()).build();
        assertThat(plain.getRelativeUrl()).isEqualTo("/activity");
    }

    @Test
    void activityResponseSurfacesTypedItems() {
        OpenRouterActivityResponse response = responseOf("""
                {
                  "data": [
                    {
                      "byok_usage_inference": 0.012,
                      "completion_tokens": 125,
                      "date": "2025-08-24",
                      "endpoint_id": "550e8400-e29b-41d4-a716-446655440000",
                      "model": "openai/gpt-4.1",
                      "model_permaslug": "openai/gpt-4.1-2025-04-14",
                      "prompt_tokens": 50,
                      "provider_name": "OpenAI",
                      "reasoning_tokens": 25,
                      "requests": 5,
                      "usage": 0.015,
                      "workspace_id": "550e8400-e29b-41d4-a716-446655440000"
                    }
                  ]
                }
                """);

        List<OpenRouterActivityItem> items = response.items();
        assertThat(items).hasSize(1);

        OpenRouterActivityItem item = items.get(0);
        assertThat(item.date()).isEqualTo("2025-08-24");
        assertThat(item.model()).isEqualTo("openai/gpt-4.1");
        assertThat(item.modelPermaslug()).isEqualTo("openai/gpt-4.1-2025-04-14");
        assertThat(item.endpointId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(item.providerName()).isEqualTo("OpenAI");
        assertThat(item.usage()).isEqualTo(0.015);
        assertThat(item.byokUsageInference()).isEqualTo(0.012);
        assertThat(item.requests()).isEqualTo(5L);
        assertThat(item.promptTokens()).isEqualTo(50L);
        assertThat(item.completionTokens()).isEqualTo(125L);
        assertThat(item.reasoningTokens()).isEqualTo(25L);
        assertThat(item.workspaceId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void activityResponseReturnsEmptyWhenDataAbsent() {
        assertThat(responseOf("{}").items()).isEmpty();
    }

    @Test
    void analyticsRequestEmitsTheSchemaBody() {
        OpenRouterAnalyticsQueryRequest request = new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count", "cost")
                .dimensions("model")
                .filter("model", "eq", "openai/gpt-4.1")
                .filterIn("provider", "in", List.of("OpenAI", "Azure"))
                .granularity("day")
                .timeRange("2025-08-01T00:00:00Z", "2025-08-31T23:59:59Z")
                .limit(500)
                .groupLimit(100)
                .orderBy("request_count", "desc")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/analytics/query");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
        assertThat(request.metrics()).containsExactly("request_count", "cost");
        assertThat(request.dimensions()).containsExactly("model");

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getJSONArray("metrics").join(","))
                .isEqualTo("\"request_count\",\"cost\"");
        assertThat(body.getJSONArray("dimensions").join(","))
                .isEqualTo("\"model\"");
        assertThat(body.getJSONArray("filters").length()).isEqualTo(2);
        assertThat(body.getJSONArray("filters").getJSONObject(0).getString("field")).isEqualTo("model");
        assertThat(body.getJSONArray("filters").getJSONObject(0).getString("operator")).isEqualTo("eq");
        assertThat(body.getJSONArray("filters").getJSONObject(0).getString("value")).isEqualTo("openai/gpt-4.1");
        assertThat(body.getJSONArray("filters").getJSONObject(1).getJSONArray("value").join(","))
                .isEqualTo("\"OpenAI\",\"Azure\"");
        assertThat(body.getString("granularity")).isEqualTo("day");
        assertThat(body.getJSONObject("time_range").getString("start")).isEqualTo("2025-08-01T00:00:00Z");
        assertThat(body.getJSONObject("time_range").getString("end")).isEqualTo("2025-08-31T23:59:59Z");
        assertThat(body.getInt("limit")).isEqualTo(500);
        assertThat(body.getInt("group_limit")).isEqualTo(100);
        assertThat(body.getJSONObject("order_by").getString("field")).isEqualTo("request_count");
        assertThat(body.getJSONObject("order_by").getString("direction")).isEqualTo("desc");
    }

    @Test
    void analyticsRequestOmitsUnsetKeys() {
        OpenRouterAnalyticsQueryRequest request = new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsOnly("metrics");
    }

    @Test
    void analyticsRequestRequiresAtLeastOneMetric() {
        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client()).build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void analyticsRequestRejectsMoreThanTwoDimensions() {
        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .dimensions("model", "provider", "origin")
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void analyticsRequestSupportsVerbatimOptions() {
        OpenRouterAnalyticsQueryRequest request = new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .option("classifier_dimensions", new JSONObject()
                        .put("classifier_id", "550e8400-e29b-41d4-a716-446655440000"))
                .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getJSONObject("classifier_dimensions").getString("classifier_id"))
                .isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void analyticsResponseSurfacesRowsAndMetadata() {
        OpenRouterAnalyticsQueryResponse response = queryResponseOf("""
                {
                  "data": {
                    "cachedAt": 1756022400.0,
                    "data": [
                      {"date__day": "2025-01-01T00:00:00.000Z", "request_count": 1500}
                    ],
                    "metadata": {
                      "query_time_ms": 42.0,
                      "row_count": 1,
                      "truncated": false
                    },
                    "warnings": ["api_key_hash could not be resolved for one filter"]
                  }
                }
                """);

        List<JSONObject> rows = response.rows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getString("date__day")).isEqualTo("2025-01-01T00:00:00.000Z");
        assertThat(rows.get(0).getLong("request_count")).isEqualTo(1500L);
        assertThat(response.queryTimeMs()).isEqualTo(42.0);
        assertThat(response.rowCount()).isEqualTo(1L);
        assertThat(response.truncated()).isFalse();
        assertThat(response.cachedAt()).isEqualTo(1756022400.0);
        assertThat(response.warnings()).containsExactly("api_key_hash could not be resolved for one filter");
    }

    @Test
    void analyticsResponseReturnsEmptyOrNullWhenAbsent() {
        OpenRouterAnalyticsQueryResponse response = queryResponseOf("{}");

        assertThat(response.rows()).isEmpty();
        assertThat(response.queryTimeMs()).isNull();
        assertThat(response.rowCount()).isNull();
        assertThat(response.truncated()).isNull();
        assertThat(response.cachedAt()).isNull();
        assertThat(response.warnings()).isEmpty();
        assertThat(response.data()).isNull();
    }

    private OpenRouterActivityResponse responseOf(String json) {
        OpenRouterActivityRequest request = new OpenRouterActivityRequest.Builder(client()).build();
        return request.createResponse(json);
    }

    private OpenRouterAnalyticsQueryResponse queryResponseOf(String json) {
        OpenRouterAnalyticsQueryRequest request = new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .build();
        return request.createResponse(json);
    }
}
