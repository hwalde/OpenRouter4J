package de.entwicklertraining.openrouter4j.activity;

import org.json.JSONArray;
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
    void analyticsRequestEmitsTypedClassifierDimensions() {
        OpenRouterAnalyticsQueryRequest request = new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierDimensions("550e8400-e29b-41d4-a716-446655440000", "department", "work_type")
                .classifierIncludeNulls(true)
                .build();

        JSONObject body = new JSONObject(request.getBody());
        JSONObject classifierDimensions = body.getJSONObject("classifier_dimensions");
        assertThat(classifierDimensions.getString("classifier_id"))
                .isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(classifierDimensions.getJSONArray("dimension_names").join(","))
                .isEqualTo("\"department\",\"work_type\"");
        assertThat(classifierDimensions.getBoolean("include_nulls")).isTrue();

        assertThat(request.classifierDimensionsId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(request.classifierDimensionNames()).containsExactly("department", "work_type");
        assertThat(request.classifierIncludeNulls()).isTrue();
    }

    @Test
    void analyticsRequestEmitsClassifierDimensionsWithoutNamesAndWithFalseIncludeNulls() {
        OpenRouterAnalyticsQueryRequest request = new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierDimensions("550e8400-e29b-41d4-a716-446655440000")
                .classifierIncludeNulls(false)
                .build();

        JSONObject classifierDimensions = new JSONObject(request.getBody()).getJSONObject("classifier_dimensions");
        assertThat(classifierDimensions.keySet()).containsExactlyInAnyOrder("classifier_id", "include_nulls");
        assertThat(classifierDimensions.getBoolean("include_nulls")).isFalse();
    }

    @Test
    void analyticsRequestOmitsClassifierFieldsWhenUnset() {
        OpenRouterAnalyticsQueryRequest request = new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .build();

        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.has("classifier_dimensions")).isFalse();
        assertThat(body.has("classifier_filters")).isFalse();
        assertThat(request.classifierDimensionsId()).isNull();
        assertThat(request.classifierDimensionNames()).isEmpty();
        assertThat(request.classifierIncludeNulls()).isNull();
        assertThat(request.classifierFiltersId()).isNull();
        assertThat(request.classifierFilters()).isEmpty();
    }

    @Test
    void analyticsRequestEmitsTypedClassifierFilters() {
        OpenRouterAnalyticsQueryRequest request = new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .classifierFilter("department", "eq", "Engineering")
                .classifierFilter("priority", "eq", 2)
                .classifierFilterIn("work_type", "in", List.of("consulting", "support"))
                .build();

        JSONObject body = new JSONObject(request.getBody());
        JSONObject classifierFilters = body.getJSONObject("classifier_filters");
        assertThat(classifierFilters.getString("classifier_id"))
                .isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(classifierFilters.getJSONArray("filters").length()).isEqualTo(3);

        JSONObject first = classifierFilters.getJSONArray("filters").getJSONObject(0);
        assertThat(first.getString("field")).isEqualTo("department");
        assertThat(first.getString("operator")).isEqualTo("eq");
        assertThat(first.getString("value")).isEqualTo("Engineering");

        JSONObject second = classifierFilters.getJSONArray("filters").getJSONObject(1);
        assertThat(second.getInt("value")).isEqualTo(2);

        JSONObject third = classifierFilters.getJSONArray("filters").getJSONObject(2);
        assertThat(third.getJSONArray("value").join(",")).isEqualTo("\"consulting\",\"support\"");

        assertThat(request.classifierFiltersId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(request.classifierFilters()).hasSize(3);
    }

    @Test
    void analyticsRequestCombinesClassifierDimensionsAndFiltersWithTheSameId() {
        OpenRouterAnalyticsQueryRequest request = new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilter("department", "eq", "Engineering")
                .classifierDimensions("550e8400-e29b-41d4-a716-446655440000", "department")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .classifierDimensions("550e8400-e29b-41d4-a716-446655440000")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .build();

        JSONObject body = new JSONObject(request.getBody());
        JSONObject classifierDimensions = body.getJSONObject("classifier_dimensions");
        assertThat(classifierDimensions.getString("classifier_id"))
                .isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(classifierDimensions.has("include_nulls")).isFalse();
        assertThat(classifierDimensions.keySet()).containsExactlyInAnyOrder("classifier_id", "dimension_names");
        assertThat(body.getJSONObject("classifier_filters").getString("classifier_id"))
                .isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void analyticsRequestEmitsTheDocumentedClassifierFilterOperatorVariants() {
        OpenRouterAnalyticsQueryRequest request = new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .classifierFilter("priority", "neq", 2)
                .classifierFilterIn("work_type", "not_in", List.of("consulting", 7))
                .build();

        JSONArray filters = new JSONObject(request.getBody())
                .getJSONObject("classifier_filters")
                .getJSONArray("filters");
        assertThat(filters.getJSONObject(0).getString("operator")).isEqualTo("neq");
        assertThat(filters.getJSONObject(0).getInt("value")).isEqualTo(2);
        assertThat(filters.getJSONObject(1).getString("operator")).isEqualTo("not_in");
        JSONArray values = filters.getJSONObject(1).getJSONArray("value");
        assertThat(values.getString(0)).isEqualTo("consulting");
        assertThat(values.getInt(1)).isEqualTo(7);
    }

    @Test
    void analyticsRequestRejectsMismatchedClassifierIds() {
        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierDimensions("550e8400-e29b-41d4-a716-446655440000")
                .classifierFilters("660e8400-e29b-41d4-a716-446655440001")
                .classifierFilter("department", "eq", "Engineering")
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("same classifier_id");
    }

    @Test
    void analyticsRequestRejectsReSettingAClassifierIdToADifferentValue() {
        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierDimensions("550e8400-e29b-41d4-a716-446655440000")
                .classifierDimensions("660e8400-e29b-41d4-a716-446655440001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("classifier_dimensions.classifier_id");

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .classifierFilters("660e8400-e29b-41d4-a716-446655440001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("classifier_filters.classifier_id");
    }

    @Test
    void analyticsRequestRejectsIncompleteClassifierFilterConfiguration() {
        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilter("department", "eq", "Engineering")
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("classifierFilters");

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("classifierFilter");
    }

    @Test
    void analyticsRequestRejectsIncludeNullsWithoutClassifierDimensions() {
        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierIncludeNulls(true)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("classifierDimensions");
    }

    @Test
    void analyticsRequestRejectsTooManyClassifierDimensionNames() {
        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierDimensions("550e8400-e29b-41d4-a716-446655440000", "a", "b", "c"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void analyticsRequestRejectsTooManyClassifierFilterEntries() {
        OpenRouterAnalyticsQueryRequest.Builder builder = new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000");
        for (int i = 0; i < 10; i++) {
            builder.classifierFilter("field_" + i, "eq", "v" + i);
        }
        assertThatThrownBy(() -> builder.classifierFilter("overflow", "eq", "v"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("10");
    }

    @Test
    void analyticsRequestOmitsIncludeNullsWhenSetToNull() {
        OpenRouterAnalyticsQueryRequest request = new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierDimensions("550e8400-e29b-41d4-a716-446655440000", "department")
                .classifierIncludeNulls(null)
                .build();

        JSONObject classifierDimensions = new JSONObject(request.getBody()).getJSONObject("classifier_dimensions");
        assertThat(classifierDimensions.has("include_nulls")).isFalse();
        assertThat(request.classifierIncludeNulls()).isNull();
    }

    @Test
    void analyticsRequestRejectsInvalidClassifierFilterArguments() {
        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .classifierFilter("", "eq", "Engineering"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .classifierFilter(null, "eq", "Engineering"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .classifierFilter("department", "", "Engineering"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .classifierFilter("department", null, "Engineering"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .classifierFilter("department", "eq", (String) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value");

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .classifierFilter("priority", "eq", (Number) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value");

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .classifierFilter("department", "gt", "Engineering"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .classifierFilterIn("work_type", "eq", List.of("x")))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilterIn("work_type", "in", List.of()))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilterIn("work_type", "in", null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters("550e8400-e29b-41d4-a716-446655440000")
                .classifierFilterIn("work_type", "in", List.of((Object) List.of("nested"))))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierDimensions(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("classifier_id");

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierDimensions(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("classifier_id");

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("classifier_id");

        assertThatThrownBy(() -> new OpenRouterAnalyticsQueryRequest.Builder(client())
                .metrics("request_count")
                .classifierFilters(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("classifier_id");
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
