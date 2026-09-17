package de.entwicklertraining.openrouter4j.containers;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the container file promote request shape against the recorded shape
 * of POST /containers/{container_id}/files/{file_id}/promote: method, URL,
 * URL-encoding of both path segments and the empty body.
 */
class OpenRouterContainerFilePromoteRequestTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void promoteUsesPostMethodOnPromoteUrlWithEmptyBody() {
        OpenRouterContainerFilePromoteRequest request =
                new OpenRouterContainerFilePromoteRequest.Builder(client(), "sess_abc123", "cfile_YQ").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/containers/sess_abc123/files/cfile_YQ/promote");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
        assertThat(request.getBody()).isEqualTo("");
        assertThat(request.containerId()).isEqualTo("sess_abc123");
        assertThat(request.fileId()).isEqualTo("cfile_YQ");
    }

    @Test
    void bothPathSegmentsAreUrlEncoded() {
        OpenRouterContainerFilePromoteRequest request =
                new OpenRouterContainerFilePromoteRequest.Builder(client(), "sess a/b?", "cfile x/y?").build();

        assertThat(request.getRelativeUrl())
                .isEqualTo("/containers/sess+a%2Fb%3F/files/cfile+x%2Fy%3F/promote");
    }

    @Test
    void missingContainerIdOrFileIdIsRejectedLoudly() {
        assertThatThrownBy(() ->
                new OpenRouterContainerFilePromoteRequest.Builder(client(), null, "cfile_YQ").build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() ->
                new OpenRouterContainerFilePromoteRequest.Builder(client(), "sess_abc123", "").build())
                .isInstanceOf(IllegalStateException.class);
    }
}
