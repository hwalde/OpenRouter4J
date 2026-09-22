package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the binary file content request against the recorded wire form of
 * GET /files/{file_id}/content, mirroring the video content-download tests.
 */
class OpenRouterFileContentRequestTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void contentRequestIsBinaryAndUsesGetMethodOnContentUrl() {
        OpenRouterFileContentRequest request =
                new OpenRouterFileContentRequest.Builder(client(), "file-abc123").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/files/file-abc123/content");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
        assertThat(request.isBinaryResponse()).isTrue();
    }

    @Test
    void fileIdIsUrlEncodedInThePath() {
        OpenRouterFileContentRequest request =
                new OpenRouterFileContentRequest.Builder(client(), "file/1 x").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/files/file%2F1+x/content");
    }

    @Test
    void missingFileIdIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterFileContentRequest.Builder(client(), null).build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterFileContentRequest.Builder(client(), "").build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void scopeQueryParametersAreAppendedOnlyWhenSet() {
        OpenRouterFileContentRequest unset =
                new OpenRouterFileContentRequest.Builder(client(), "file-abc123").build();
        assertThat(unset.getRelativeUrl()).isEqualTo("/files/file-abc123/content");
        assertThat(unset.workspaceId()).isNull();
        assertThat(unset.provider()).isNull();

        OpenRouterFileContentRequest providerOnly =
                new OpenRouterFileContentRequest.Builder(client(), "file-abc123")
                        .provider("openai")
                        .build();
        assertThat(providerOnly.getRelativeUrl())
                .isEqualTo("/files/file-abc123/content?provider=openai");
        assertThat(providerOnly.provider()).isEqualTo("openai");
        assertThat(providerOnly.workspaceId()).isNull();

        OpenRouterFileContentRequest both =
                new OpenRouterFileContentRequest.Builder(client(), "file-abc123")
                        .workspaceId("ws 1")
                        .provider("anthropic")
                        .build();
        assertThat(both.getRelativeUrl())
                .isEqualTo("/files/file-abc123/content?workspace_id=ws+1&provider=anthropic");
        assertThat(both.workspaceId()).isEqualTo("ws 1");
    }

    @Test
    void contentResponseHoldsTheFileBytes() {
        OpenRouterFileContentRequest request =
                new OpenRouterFileContentRequest.Builder(client(), "file-abc123").build();
        byte[] content = "fake-file-bytes".getBytes(StandardCharsets.UTF_8);

        OpenRouterFileContentResponse response = request.createResponse(content);

        assertThat(response.bytes()).isSameAs(content);
        assertThat(response.length()).isEqualTo(content.length);
    }

    @Test
    void textFallbackResponseWrapsTheUtf8Bytes() {
        OpenRouterFileContentRequest request =
                new OpenRouterFileContentRequest.Builder(client(), "file-abc123").build();

        OpenRouterFileContentResponse response = request.createResponse("not-a-file");

        assertThat(response.bytes()).isEqualTo("not-a-file".getBytes(StandardCharsets.UTF_8));
        assertThat(response.length()).isEqualTo("not-a-file".getBytes(StandardCharsets.UTF_8).length);
    }
}
