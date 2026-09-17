package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the file metadata request (URL shape, file id URL-encoding, loud
 * validation) and the response accessors against recorded JSON shapes of
 * GET /files/{file_id}.
 */
class OpenRouterFileGetRequestTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void requestUsesGetMethodOnFileUrl() {
        OpenRouterFileGetRequest request =
                new OpenRouterFileGetRequest.Builder(client(), "file-abc123").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/files/file-abc123");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
        assertThat(request.fileId()).isEqualTo("file-abc123");
    }

    @Test
    void fileIdIsUrlEncodedInThePath() {
        OpenRouterFileGetRequest request =
                new OpenRouterFileGetRequest.Builder(client(), "file/1 x").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/files/file%2F1+x");
    }

    @Test
    void missingFileIdIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterFileGetRequest.Builder(client(), null).build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterFileGetRequest.Builder(client(), "").build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void responseResolvesTheDataWrapperToAFileView() {
        String fixture = """
                {
                  "data": {
                    "_shape": "openrouter",
                    "id": "or_file_abc123",
                    "type": "file",
                    "filename": "report.pdf",
                    "mime_type": "application/pdf",
                    "size_bytes": 1024,
                    "created_at": "2026-09-17T10:00:00Z",
                    "downloadable": true
                  }
                }
                """;
        OpenRouterFileGetResponse response =
                new OpenRouterFileGetRequest.Builder(client(), "or_file_abc123")
                        .build()
                        .createResponse(fixture);

        assertThat(response.file().id()).isEqualTo("or_file_abc123");
        assertThat(response.file().filename()).isEqualTo("report.pdf");
        assertThat(response.file().mimeType()).isEqualTo("application/pdf");
        assertThat(response.file().sizeBytes()).isEqualTo(1024L);
        assertThat(response.file().createdAt()).isEqualTo("2026-09-17T10:00:00Z");
        assertThat(response.file().downloadable()).isTrue();
    }

    @Test
    void responseFallsBackToTheTopLevelFile() {
        String fixture = """
                {
                  "_shape": "openai",
                  "id": "file-abc123",
                  "object": "file",
                  "bytes": 140,
                  "created_at": 1699061776,
                  "filename": "report.pdf",
                  "purpose": "user_data",
                  "status": "processed"
                }
                """;
        OpenRouterFileGetResponse response =
                new OpenRouterFileGetRequest.Builder(client(), "file-abc123")
                        .build()
                        .createResponse(fixture);

        assertThat(response.file().id()).isEqualTo("file-abc123");
        assertThat(response.file().objectType()).isEqualTo("file");
        assertThat(response.file().sizeBytesOpenAi()).isEqualTo(140L);
        assertThat(response.file().createdAtUnixSeconds()).isEqualTo(1699061776L);
        assertThat(response.file().purpose()).isEqualTo("user_data");
        assertThat(response.file().status()).isEqualTo("processed");
    }
}
