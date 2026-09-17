package de.entwicklertraining.openrouter4j.containers;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the container file get request shape against the recorded shape of
 * GET /containers/{container_id}/files/{file_id}: relative URL building,
 * URL-encoding of both path segments and the loud validation.
 */
class OpenRouterContainerFileGetRequestTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void getUsesGetMethodOnFileUrl() {
        OpenRouterContainerFileGetRequest request =
                new OpenRouterContainerFileGetRequest.Builder(client(), "sess_abc123", "cfile_YQ").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/containers/sess_abc123/files/cfile_YQ");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
        assertThat(request.containerId()).isEqualTo("sess_abc123");
        assertThat(request.fileId()).isEqualTo("cfile_YQ");
    }

    @Test
    void bothPathSegmentsAreUrlEncoded() {
        OpenRouterContainerFileGetRequest request =
                new OpenRouterContainerFileGetRequest.Builder(client(), "sess a/b?", "cfile x/y?").build();

        assertThat(request.getRelativeUrl())
                .isEqualTo("/containers/sess+a%2Fb%3F/files/cfile+x%2Fy%3F");
    }

    @Test
    void missingContainerIdOrFileIdIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterContainerFileGetRequest.Builder(client(), null, "cfile_YQ").build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterContainerFileGetRequest.Builder(client(), "", "cfile_YQ").build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterContainerFileGetRequest.Builder(client(), "sess_abc123", null).build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterContainerFileGetRequest.Builder(client(), "sess_abc123", "").build())
                .isInstanceOf(IllegalStateException.class);
    }
}
