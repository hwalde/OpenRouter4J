package de.entwicklertraining.openrouter4j.containers;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the container file list request shape against the recorded shape of
 * GET /containers/{container_id}/files: relative URL building, query
 * parameters, validation and the queryParam escape hatch.
 */
class OpenRouterContainerFileListRequestTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void listUsesGetMethodOnFilesUrlWithoutQueryWhenUnset() {
        OpenRouterContainerFileListRequest request =
                new OpenRouterContainerFileListRequest.Builder(client(), "sess_abc123").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/containers/sess_abc123/files");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
        assertThat(request.containerId()).isEqualTo("sess_abc123");
    }

    @Test
    void limitIsEmittedOnlyWhenSet() {
        OpenRouterContainerFileListRequest withLimit =
                new OpenRouterContainerFileListRequest.Builder(client(), "sess_abc123")
                        .limit(100)
                        .build();
        assertThat(withLimit.getRelativeUrl()).isEqualTo("/containers/sess_abc123/files?limit=100");

        OpenRouterContainerFileListRequest withoutLimit =
                new OpenRouterContainerFileListRequest.Builder(client(), "sess_abc123")
                        .build();
        assertThat(withoutLimit.getRelativeUrl()).isEqualTo("/containers/sess_abc123/files");
        assertThat(withoutLimit.queryParams()).isEmpty();
    }

    @Test
    void afterIsEmittedOnlyWhenSet() {
        OpenRouterContainerFileListRequest withAfter =
                new OpenRouterContainerFileListRequest.Builder(client(), "sess_abc123")
                        .after("cfile_YQ")
                        .build();
        assertThat(withAfter.getRelativeUrl()).isEqualTo("/containers/sess_abc123/files?after=cfile_YQ");

        OpenRouterContainerFileListRequest withoutAfter =
                new OpenRouterContainerFileListRequest.Builder(client(), "sess_abc123")
                        .build();
        assertThat(withoutAfter.getRelativeUrl()).isEqualTo("/containers/sess_abc123/files");
    }

    @Test
    void limitAndAfterAreCombinedInInsertionOrder() {
        OpenRouterContainerFileListRequest request =
                new OpenRouterContainerFileListRequest.Builder(client(), "sess_abc123")
                        .limit(50)
                        .after("cfile_YQ")
                        .build();

        assertThat(request.getRelativeUrl())
                .isEqualTo("/containers/sess_abc123/files?limit=50&after=cfile_YQ");
    }

    @Test
    void limitOutsideOneToThousandIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterContainerFileListRequest.Builder(client(), "sess_abc123")
                .limit(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterContainerFileListRequest.Builder(client(), "sess_abc123")
                .limit(1001))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterContainerFileListRequest.Builder(client(), "sess_abc123")
                .limit(-5))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(new OpenRouterContainerFileListRequest.Builder(client(), "sess_abc123")
                .limit(1).build().getRelativeUrl()).isEqualTo("/containers/sess_abc123/files?limit=1");
        assertThat(new OpenRouterContainerFileListRequest.Builder(client(), "sess_abc123")
                .limit(1000).build().getRelativeUrl()).isEqualTo("/containers/sess_abc123/files?limit=1000");
    }

    @Test
    void queryParamEscapeHatchIsSentVerbatimAndEncoded() {
        OpenRouterContainerFileListRequest request =
                new OpenRouterContainerFileListRequest.Builder(client(), "sess_abc123")
                        .queryParam("custom", "some value")
                        .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/containers/sess_abc123/files?custom=some+value");
    }

    @Test
    void containerIdIsUrlEncodedInThePath() {
        OpenRouterContainerFileListRequest request =
                new OpenRouterContainerFileListRequest.Builder(client(), "sess a/b?").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/containers/sess+a%2Fb%3F/files");
    }

    @Test
    void missingContainerIdIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterContainerFileListRequest.Builder(client(), null).build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterContainerFileListRequest.Builder(client(), "").build())
                .isInstanceOf(IllegalStateException.class);
    }
}
