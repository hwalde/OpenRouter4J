package de.entwicklertraining.openrouter4j;

import de.entwicklertraining.openrouter4j.credits.OpenRouterCreditsRequest;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the client entry points of the read endpoints: each factory must
 * produce the correct request type with the expected relative URL, and the
 * model-id splitting of {@code model()} / {@code modelEndpoints()} must
 * validate its input.
 */
class OpenRouterClientEntryPointsTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void creditsEntryPointProducesTheCreditsRequest() {
        OpenRouterCreditsRequest request = client().credits().build();
        assertThat(request.getRelativeUrl()).isEqualTo("/credits");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
    }

    @Test
    void modelsEntryPointsProduceTheCatalogRequests() {
        assertThat(client().models().build().getRelativeUrl()).isEqualTo("/models");
        assertThat(client().modelsCount().build().getRelativeUrl()).isEqualTo("/models/count");
        assertThat(client().userModels().build().getRelativeUrl()).isEqualTo("/models/user");
    }

    @Test
    void modelEntryPointSplitsTheModelId() {
        assertThat(client().model("openai/gpt-4").build().getRelativeUrl())
                .isEqualTo("/model/openai/gpt-4");
        assertThat(client().modelEndpoints("openai/gpt-4").build().getRelativeUrl())
                .isEqualTo("/models/openai/gpt-4/endpoints");

        // only the first slash splits: slugs may not contain slashes themselves,
        // and everything after it is the slug
        OpenRouterModelRequest request = client().model("a/b/c").build();
        assertThat(request.author()).isEqualTo("a");
        assertThat(request.slug()).isEqualTo("b/c");
    }

    @Test
    void modelEntryPointRejectsMalformedModelIds() {
        assertThatThrownBy(() -> client().model(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().model("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().model("gpt-4")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().model("/gpt-4")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().model("openai/")).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> client().modelEndpoints(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().modelEndpoints("gpt-4")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generationEntryPointsProduceTheGenerationRequests() {
        assertThat(client().generation("gen-1").build().getRelativeUrl())
                .isEqualTo("/generation?id=gen-1");
        assertThat(client().generationContent("gen-1").build().getRelativeUrl())
                .isEqualTo("/generation/content?id=gen-1");
        assertThat(client().generationFeedback().generationId("gen-1").category("other")
                .build().getRelativeUrl())
                .isEqualTo("/generation/feedback");
        assertThat(client().generationFeedback().generationId("gen-1").category("other")
                .build().getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void activityEntryPointsProduceTheUsageRequests() {
        assertThat(client().activity().build().getRelativeUrl()).isEqualTo("/activity");
        assertThat(client().activity().date("2026-09-14").build().getRelativeUrl())
                .isEqualTo("/activity?date=2026-09-14");
        assertThat(client().analyticsQuery().metrics("request_count").build().getRelativeUrl())
                .isEqualTo("/analytics/query");
        assertThat(client().analyticsQuery().metrics("request_count").build().getHttpMethod())
                .isEqualTo("POST");
    }

    @Test
    void embeddingsEntryPointsProduceTheEmbeddingsRequests() {
        assertThat(client().embeddings()
                .model("openai/text-embedding-3-small").input("hello")
                .build().getRelativeUrl()).isEqualTo("/embeddings");
        assertThat(client().embeddings()
                .model("openai/text-embedding-3-small").input("hello")
                .build().getHttpMethod()).isEqualTo("POST");
        assertThat(client().embeddingsModels().build().getRelativeUrl())
                .isEqualTo("/embeddings/models");
        assertThat(client().embeddingsModels().build().getHttpMethod()).isEqualTo("GET");
    }
}
