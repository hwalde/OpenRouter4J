package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link OpenRouterFile} document view against fixture JSON of all
 * three negotiated shapes (openrouter, openai, anthropic) and the list
 * response pagination accessors.
 */
class OpenRouterFileViewTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void openRouterShapeExposesTheSupersetAccessors() {
        OpenRouterFile file = viewOf("""
                {
                  "_shape": "openrouter",
                  "id": "or_file_abc123",
                  "type": "file",
                  "filename": "report.pdf",
                  "mime_type": "application/pdf",
                  "size_bytes": 1024,
                  "created_at": "2026-09-17T10:00:00Z",
                  "downloadable": true
                }
                """);

        assertThat(file.shape()).isEqualTo("openrouter");
        assertThat(file.id()).isEqualTo("or_file_abc123");
        assertThat(file.type()).isEqualTo("file");
        assertThat(file.filename()).isEqualTo("report.pdf");
        assertThat(file.mimeType()).isEqualTo("application/pdf");
        assertThat(file.sizeBytes()).isEqualTo(1024L);
        assertThat(file.createdAt()).isEqualTo("2026-09-17T10:00:00Z");
        assertThat(file.downloadable()).isTrue();
        assertThat(file.sizeBytesOpenAi()).isNull();
        assertThat(file.createdAtUnixSeconds()).isNull();
        assertThat(file.objectType()).isNull();
        assertThat(file.purpose()).isNull();
        assertThat(file.status()).isNull();
    }

    @Test
    void openAiShapeExposesTheObjectStyleAccessors() {
        OpenRouterFile file = viewOf("""
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
                """);

        assertThat(file.shape()).isEqualTo("openai");
        assertThat(file.id()).isEqualTo("file-abc123");
        assertThat(file.objectType()).isEqualTo("file");
        assertThat(file.sizeBytesOpenAi()).isEqualTo(140L);
        assertThat(file.createdAtUnixSeconds()).isEqualTo(1699061776L);
        assertThat(file.filename()).isEqualTo("notes.txt");
        assertThat(file.purpose()).isEqualTo("user_data");
        assertThat(file.status()).isEqualTo("processed");
        assertThat(file.type()).isNull();
        assertThat(file.mimeType()).isNull();
        assertThat(file.sizeBytes()).isNull();
        assertThat(file.createdAt()).isNull();
        assertThat(file.downloadable()).isNull();
    }

    @Test
    void anthropicShapeExposesTheSizeAndTimestampAccessors() {
        OpenRouterFile file = viewOf("""
                {
                  "_shape": "anthropic",
                  "id": "file_011CNha8iCJcUyrQBkqpsD6b",
                  "type": "file",
                  "filename": "notes.txt",
                  "mime_type": "text/plain",
                  "size_bytes": 256,
                  "created_at": "2026-09-17T12:30:00Z"
                }
                """);

        assertThat(file.shape()).isEqualTo("anthropic");
        assertThat(file.id()).isEqualTo("file_011CNha8iCJcUyrQBkqpsD6b");
        assertThat(file.type()).isEqualTo("file");
        assertThat(file.filename()).isEqualTo("notes.txt");
        assertThat(file.mimeType()).isEqualTo("text/plain");
        assertThat(file.sizeBytes()).isEqualTo(256L);
        assertThat(file.createdAt()).isEqualTo("2026-09-17T12:30:00Z");
        assertThat(file.downloadable()).isNull();
        assertThat(file.sizeBytesOpenAi()).isNull();
        assertThat(file.createdAtUnixSeconds()).isNull();
    }

    @Test
    void missingFieldsReturnNullWithoutThrowing() {
        OpenRouterFile file = viewOf("{}");

        assertThat(file.shape()).isNull();
        assertThat(file.id()).isNull();
        assertThat(file.type()).isNull();
        assertThat(file.filename()).isNull();
        assertThat(file.mimeType()).isNull();
        assertThat(file.sizeBytes()).isNull();
        assertThat(file.sizeBytesOpenAi()).isNull();
        assertThat(file.createdAt()).isNull();
        assertThat(file.createdAtUnixSeconds()).isNull();
        assertThat(file.downloadable()).isNull();
        assertThat(file.objectType()).isNull();
        assertThat(file.purpose()).isNull();
        assertThat(file.status()).isNull();
    }

    @Test
    void jsonReturnsADefensiveCopy() {
        OpenRouterFile file = viewOf("{\"_shape\": \"openrouter\", \"id\": \"or_file_abc123\"}");

        file.json().put("id", "mutated");

        assertThat(file.id()).isEqualTo("or_file_abc123");
    }

    @Test
    void listResponseExposesFilesAndPaginationFields() {
        String fixture = """
                {
                  "_shape": "openrouter",
                  "data": [
                    {
                      "_shape": "openrouter",
                      "id": "or_file_abc123",
                      "type": "file",
                      "filename": "a.txt",
                      "mime_type": "text/plain",
                      "size_bytes": 10,
                      "created_at": "2026-09-17T10:00:00Z",
                      "downloadable": true
                    },
                    {
                      "_shape": "openai",
                      "id": "file-def456",
                      "object": "file",
                      "bytes": 20,
                      "created_at": 1699061776,
                      "filename": "b.txt",
                      "purpose": "user_data",
                      "status": "processed"
                    }
                  ],
                  "first_id": "or_file_abc123",
                  "last_id": "file-def456",
                  "has_more": true,
                  "cursor": "cur_9"
                }
                """;
        OpenRouterFileListResponse<OpenRouterFileListRequest> response =
                new OpenRouterFileListRequest.Builder(client()).build().createResponse(fixture);

        assertThat(response.files()).hasSize(2);
        assertThat(response.files().get(0).id()).isEqualTo("or_file_abc123");
        assertThat(response.files().get(0).sizeBytes()).isEqualTo(10L);
        assertThat(response.files().get(1).id()).isEqualTo("file-def456");
        assertThat(response.files().get(1).sizeBytesOpenAi()).isEqualTo(20L);
        assertThat(response.firstId()).isEqualTo("or_file_abc123");
        assertThat(response.lastId()).isEqualTo("file-def456");
        assertThat(response.hasMore()).isTrue();
        assertThat(response.cursor()).isEqualTo("cur_9");
        assertThat(response.shape()).isEqualTo("openrouter");
    }

    @Test
    void listResponseSwallowsMalformedBody() {
        OpenRouterFileListResponse<OpenRouterFileListRequest> response =
                new OpenRouterFileListRequest.Builder(client()).build().createResponse("{}");

        assertThat(response.files()).isEmpty();
        assertThat(response.firstId()).isNull();
        assertThat(response.lastId()).isNull();
        assertThat(response.hasMore()).isNull();
        assertThat(response.cursor()).isNull();
        assertThat(response.shape()).isNull();
    }

    private OpenRouterFile viewOf(String fixture) {
        OpenRouterFileGetResponse response =
                new OpenRouterFileGetRequest.Builder(client(), "file-abc123")
                        .build()
                        .createResponse(fixture);
        return response.file();
    }
}
