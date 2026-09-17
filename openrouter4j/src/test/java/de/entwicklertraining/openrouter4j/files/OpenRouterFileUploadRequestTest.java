package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the file upload request shape (multipart body, boundary, query
 * parameters, loud validation) against the recorded wire form of
 * POST /files.
 */
class OpenRouterFileUploadRequestTest {

    @TempDir
    Path tempDir;

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void multipartBodyCarriesFilePartWithFilenameAndRawBytes() throws Exception {
        byte[] content = "fake-file-bytes".getBytes(StandardCharsets.UTF_8);
        Path file = tempDir.resolve("notes.txt");
        Files.write(file, content);

        OpenRouterFileUploadRequest request = new OpenRouterFileUploadRequest.Builder(client())
                .fileByPath(file)
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/files");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
        assertThat(request.getContentType()).startsWith("multipart/form-data; boundary=");

        byte[] bodyBytes = request.getBodyBytes();
        String bodyText = new String(bodyBytes, StandardCharsets.UTF_8);

        assertThat(bodyText).contains("name=\"file\"; filename=\"notes.txt\"");
        assertThat(bodyText).contains("Content-Type: application/octet-stream");
        assertThat(bodyBytes).contains(content);
        String boundary = request.getContentType().split("boundary=")[1];
        assertThat(bodyText).endsWith("--\r\n").contains("\r\n--" + boundary);
    }

    @Test
    void fileByBytesCarriesTheGivenFilenameAndContent() {
        byte[] content = new byte[]{0x01, 0x02, 0x03};

        OpenRouterFileUploadRequest request = new OpenRouterFileUploadRequest.Builder(client())
                .fileByBytes(content, "data.bin")
                .build();

        byte[] bodyBytes = request.getBodyBytes();
        String bodyText = new String(bodyBytes, StandardCharsets.UTF_8);

        assertThat(bodyText).contains("name=\"file\"; filename=\"data.bin\"");
        assertThat(bodyBytes).contains(content);
        assertThat(request.fileName()).isEqualTo("data.bin");
    }

    @Test
    void multipartSummaryNamesTheFileAndOperationOptions() {
        OpenRouterFileUploadRequest request = new OpenRouterFileUploadRequest.Builder(client())
                .fileByBytes(new byte[]{0x01}, "data.bin")
                .workspaceId("ws_123")
                .provider("openai")
                .build();

        assertThat(request.getBody())
                .contains("data.bin")
                .contains("workspace_id=ws_123")
                .contains("provider=openai");
    }

    @Test
    void queryParametersAppearInTheRelativeUrlOnlyWhenSet() {
        OpenRouterFileUploadRequest unset = new OpenRouterFileUploadRequest.Builder(client())
                .fileByBytes(new byte[]{0x01}, "data.bin")
                .build();
        assertThat(unset.getRelativeUrl()).isEqualTo("/files");

        OpenRouterFileUploadRequest set = new OpenRouterFileUploadRequest.Builder(client())
                .fileByBytes(new byte[]{0x01}, "data.bin")
                .workspaceId("ws_123")
                .provider("openai")
                .build();
        assertThat(set.getRelativeUrl()).isEqualTo("/files?workspace_id=ws_123&provider=openai");
    }

    @Test
    void queryParameterValuesAreUrlEncoded() {
        OpenRouterFileUploadRequest request = new OpenRouterFileUploadRequest.Builder(client())
                .fileByBytes(new byte[]{0x01}, "data.bin")
                .workspaceId("ws 1&2")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/files?workspace_id=ws+1%262");
    }

    @Test
    void missingFileIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterFileUploadRequest.Builder(client()).build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterFileUploadRequest.Builder(client())
                .fileByBytes(null, "data.bin"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emptyFileIsRejectedLoudly() throws Exception {
        assertThatThrownBy(() -> new OpenRouterFileUploadRequest.Builder(client())
                .fileByBytes(new byte[0], "empty.bin"))
                .isInstanceOf(IllegalArgumentException.class);

        Path empty = tempDir.resolve("empty.bin");
        Files.write(empty, new byte[0]);
        assertThatThrownBy(() -> new OpenRouterFileUploadRequest.Builder(client())
                .fileByPath(empty)
                .build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void lineBreaksInFilenameAreRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterFileUploadRequest.Builder(client())
                .fileByBytes(new byte[]{0x01}, "bad\r\nname.txt")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CR/LF");
    }

    @Test
    void oversizedFileIsRejectedLoudly() {
        byte[] oversized = new byte[100 * 1024 * 1024 + 1];
        assertThatThrownBy(() -> new OpenRouterFileUploadRequest.Builder(client())
                .fileByBytes(oversized, "big.bin")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100 MB");
    }

    @Test
    void missingFilenameIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterFileUploadRequest.Builder(client())
                .fileByBytes(new byte[]{0x01}, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void uploadResponseResolvesDataWrapperAndTopLevelFile() {
        String wrapped = """
                {
                  "data": {
                    "_shape": "openrouter",
                    "id": "or_file_abc123",
                    "type": "file",
                    "filename": "notes.txt",
                    "mime_type": "text/plain",
                    "size_bytes": 15,
                    "created_at": "2026-09-17T10:00:00Z",
                    "downloadable": true
                  }
                }
                """;
        OpenRouterFileUploadResponse wrappedResponse =
                new OpenRouterFileUploadRequest.Builder(client())
                        .fileByBytes(new byte[]{0x01}, "notes.txt")
                        .build()
                        .createResponse(wrapped);
        assertThat(wrappedResponse.file().id()).isEqualTo("or_file_abc123");
        assertThat(wrappedResponse.file().sizeBytes()).isEqualTo(15L);

        String topLevel = """
                {
                  "_shape": "openai",
                  "id": "file-abc123",
                  "object": "file",
                  "bytes": 140,
                  "created_at": 1699061776,
                  "filename": "notes.txt",
                  "purpose": "user_data",
                  "status": "processed"
                }
                """;
        OpenRouterFileUploadResponse topLevelResponse =
                new OpenRouterFileUploadRequest.Builder(client())
                        .fileByBytes(new byte[]{0x01}, "notes.txt")
                        .build()
                        .createResponse(topLevel);
        assertThat(topLevelResponse.file().id()).isEqualTo("file-abc123");
        assertThat(topLevelResponse.file().sizeBytesOpenAi()).isEqualTo(140L);
    }
}
