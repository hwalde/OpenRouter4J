package de.entwicklertraining.openrouter4j.containers;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the binary container file content request against the recorded shape
 * of GET /containers/{container_id}/files/{file_id}/content, mirroring the
 * video content-download tests.
 */
class OpenRouterContainerFileContentRequestTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void contentRequestIsBinaryAndUsesGetMethodOnContentUrl() {
        OpenRouterContainerFileContentRequest request =
                new OpenRouterContainerFileContentRequest.Builder(client(), "sess_abc123", "cfile_YQ").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/containers/sess_abc123/files/cfile_YQ/content");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.isBinaryResponse()).isTrue();
        assertThat(request.getBody()).isNull();
    }

    @Test
    void bothPathSegmentsAreUrlEncoded() {
        OpenRouterContainerFileContentRequest request =
                new OpenRouterContainerFileContentRequest.Builder(client(), "sess a/b?", "cfile x/y?").build();

        assertThat(request.getRelativeUrl())
                .isEqualTo("/containers/sess+a%2Fb%3F/files/cfile+x%2Fy%3F/content");
    }

    @Test
    void missingContainerIdOrFileIdIsRejectedLoudly() {
        assertThatThrownBy(() ->
                new OpenRouterContainerFileContentRequest.Builder(client(), null, "cfile_YQ").build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() ->
                new OpenRouterContainerFileContentRequest.Builder(client(), "sess_abc123", "").build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void contentResponseHoldsTheFileBytes() {
        OpenRouterContainerFileContentRequest request =
                new OpenRouterContainerFileContentRequest.Builder(client(), "sess_abc123", "cfile_YQ").build();
        byte[] content = "fake-file-bytes".getBytes(StandardCharsets.UTF_8);

        OpenRouterContainerFileContentResponse response = request.createResponse(content);

        assertThat(response.bytes()).isSameAs(content);
        assertThat(response.length()).isEqualTo(content.length);
    }

    @Test
    void textBodyIsWrappedAsUtf8Bytes() {
        OpenRouterContainerFileContentRequest request =
                new OpenRouterContainerFileContentRequest.Builder(client(), "sess_abc123", "cfile_YQ").build();

        OpenRouterContainerFileContentResponse response = request.createResponse("hällo");

        assertThat(new String(response.bytes(), StandardCharsets.UTF_8)).isEqualTo("hällo");
        assertThat(response.length()).isGreaterThan(0);
    }

    @Test
    void nullTextBodyYieldsNullBytesAndZeroLength() {
        OpenRouterContainerFileContentRequest request =
                new OpenRouterContainerFileContentRequest.Builder(client(), "sess_abc123", "cfile_YQ").build();

        OpenRouterContainerFileContentResponse response = request.createResponse((String) null);

        assertThat(response.bytes()).isNull();
        assertThat(response.length()).isEqualTo(0);
    }
}
