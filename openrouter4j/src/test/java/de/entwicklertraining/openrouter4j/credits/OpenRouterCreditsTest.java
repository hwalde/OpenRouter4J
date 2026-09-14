package de.entwicklertraining.openrouter4j.credits;

import org.json.JSONObject;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the credits request shape and the response accessors against a
 * recorded JSON shape of the GET /credits response.
 */
class OpenRouterCreditsTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void requestUsesGetMethodOnCreditsUrlWithoutBody() {
        OpenRouterCreditsRequest request = new OpenRouterCreditsRequest.Builder(client()).build();

        assertThat(request.getRelativeUrl()).isEqualTo("/credits");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void accessorsAreSurfaced() {
        OpenRouterCreditsResponse response = responseOf("""
                {"data": {"total_credits": 100.5, "total_usage": 25.75}}
                """);

        assertThat(response.totalCredits()).isEqualTo(100.5);
        assertThat(response.totalUsage()).isEqualTo(25.75);
        assertThat(response.data()).isNotNull();
    }

    @Test
    void accessorsReturnNullWhenAbsent() {
        OpenRouterCreditsResponse response = responseOf("{}");

        assertThat(response.totalCredits()).isNull();
        assertThat(response.totalUsage()).isNull();
        assertThat(response.data()).isNull();
    }

    @Test
    void accessorsSwallowMalformedBodies() {
        OpenRouterCreditsResponse response = responseOf("{\"data\": \"not-an-object\"}");

        assertThat(response.totalCredits()).isNull();
        assertThat(response.totalUsage()).isNull();
        assertThat(response.data()).isNull();
    }

    private OpenRouterCreditsResponse responseOf(String json) {
        OpenRouterCreditsRequest request = new OpenRouterCreditsRequest.Builder(client()).build();
        return request.createResponse(json);
    }
}
