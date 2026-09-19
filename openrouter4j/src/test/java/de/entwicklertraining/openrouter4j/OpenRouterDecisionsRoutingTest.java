package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.api.base.ApiClientSettings;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the internal routing of the alpha-namespace endpoints: the Decisions
 * request must leave the /api/v1 base (the endpoint answers 404 there) and
 * travel against the /api/alpha base derived from the client's base URL.
 */
class OpenRouterDecisionsRoutingTest {

    @Test
    void alphaBaseIsDerivedFromTheV1Base() {
        assertThat(new OpenRouterClient().alphaBaseUrl())
                .isEqualTo("https://openrouter.ai/api/alpha");
    }

    @Test
    void alphaBaseIsStableForCustomBaseUrls() {
        OpenRouterClient proxyClient = new OpenRouterClient(
                ApiClientSettings.builder().build(), null, "https://proxy.example.com/api/v1");
        assertThat(proxyClient.alphaBaseUrl()).isEqualTo("https://proxy.example.com/api/alpha");

        OpenRouterClient rootClient = new OpenRouterClient(
                ApiClientSettings.builder().build(), null, "https://openrouter.ai");
        assertThat(rootClient.alphaBaseUrl()).isEqualTo("https://openrouter.ai/api/alpha");
    }

    @Test
    void decisionsBuilderBindsToTheAlphaClientAndReusesIt() {
        OpenRouterClient client = new OpenRouterClient();
        OpenRouterClient first = client.alphaClient();
        OpenRouterClient second = client.alphaClient();
        assertThat(first).isSameAs(second);
        assertThat(first).isNotSameAs(client);
        assertThat(first.alphaBaseUrl()).isEqualTo("https://openrouter.ai/api/alpha");
    }
}
