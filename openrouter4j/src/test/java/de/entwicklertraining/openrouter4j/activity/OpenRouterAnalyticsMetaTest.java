package de.entwicklertraining.openrouter4j.activity;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the GET /analytics/meta request shape and the response accessors
 * against the recorded JSON shape of the OpenRouter analytics meta endpoint.
 */
class OpenRouterAnalyticsMetaTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void metaRequestCarriesNoQueryAndNoBody() {
        OpenRouterAnalyticsMetaRequest request = new OpenRouterAnalyticsMetaRequest.Builder(client()).build();
        assertThat(request.getRelativeUrl()).isEqualTo("/analytics/meta");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void metaResponseSurfacesMetricsDimensionsOperatorsAndGranularities() {
        OpenRouterAnalyticsMetaResponse response = metaResponseOf("""
                {
                  "data": {
                    "dimensions": [
                      {"display_label": "Model", "name": "model"},
                      {"display_label": "Workspace", "name": "workspace"}
                    ],
                    "granularities": [
                      {"display_label": "Day", "name": "day"}
                    ],
                    "metrics": [
                      {"display_format": "number", "display_label": "Request Count",
                       "is_rate": false, "name": "request_count"},
                      {"display_format": "currency", "display_label": "Cost",
                       "is_rate": true, "name": "cost"}
                    ],
                    "operators": [
                      {"name": "eq", "value_type": "scalar"}
                    ]
                  }
                }
                """);

        List<OpenRouterAnalyticsMetric> metrics = response.metrics();
        assertThat(metrics).hasSize(2);
        assertThat(metrics.get(0).name()).isEqualTo("request_count");
        assertThat(metrics.get(0).displayLabel()).isEqualTo("Request Count");
        assertThat(metrics.get(0).displayFormat()).isEqualTo("number");
        assertThat(metrics.get(0).isRate()).isFalse();
        assertThat(metrics.get(1).isRate()).isTrue();

        List<OpenRouterAnalyticsDimension> dimensions = response.dimensions();
        assertThat(dimensions).hasSize(2);
        assertThat(dimensions.get(0).name()).isEqualTo("model");
        assertThat(dimensions.get(0).displayLabel()).isEqualTo("Model");
        assertThat(dimensions.get(1).name()).isEqualTo("workspace");

        List<OpenRouterAnalyticsOperator> operators = response.operators();
        assertThat(operators).hasSize(1);
        assertThat(operators.get(0).name()).isEqualTo("eq");
        assertThat(operators.get(0).valueType()).isEqualTo("scalar");

        List<OpenRouterAnalyticsGranularity> granularities = response.granularities();
        assertThat(granularities).hasSize(1);
        assertThat(granularities.get(0).name()).isEqualTo("day");
        assertThat(granularities.get(0).displayLabel()).isEqualTo("Day");
    }

    @Test
    void metaResponseReturnsEmptyWhenDataAbsent() {
        OpenRouterAnalyticsMetaResponse response = metaResponseOf("{}");
        assertThat(response.metrics()).isEmpty();
        assertThat(response.dimensions()).isEmpty();
        assertThat(response.operators()).isEmpty();
        assertThat(response.granularities()).isEmpty();
    }

    private OpenRouterAnalyticsMetaResponse metaResponseOf(String json) {
        OpenRouterAnalyticsMetaRequest request = new OpenRouterAnalyticsMetaRequest.Builder(client()).build();
        return request.createResponse(json);
    }
}
