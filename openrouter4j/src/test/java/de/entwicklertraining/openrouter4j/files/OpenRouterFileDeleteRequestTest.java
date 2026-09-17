package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the file deletion request (URL shape, file id URL-encoding, loud
 * validation) and the tolerant deletion confirmation accessors against the
 * OpenRouter ({@code type: "file_deleted"}) and OpenAI
 * ({@code deleted: true}) answer shapes.
 */
class OpenRouterFileDeleteRequestTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void requestUsesDeleteMethodOnFileUrl() {
        OpenRouterFileDeleteRequest request =
                new OpenRouterFileDeleteRequest.Builder(client(), "or_file_abc123").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/files/or_file_abc123");
        assertThat(request.getHttpMethod()).isEqualTo("DELETE");
        assertThat(request.getBody()).isNull();
        assertThat(request.fileId()).isEqualTo("or_file_abc123");
    }

    @Test
    void fileIdIsUrlEncodedInThePath() {
        OpenRouterFileDeleteRequest request =
                new OpenRouterFileDeleteRequest.Builder(client(), "file/1 x").build();

        assertThat(request.getRelativeUrl()).isEqualTo("/files/file%2F1+x");
    }

    @Test
    void missingFileIdIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterFileDeleteRequest.Builder(client(), null).build())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new OpenRouterFileDeleteRequest.Builder(client(), "").build())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void openRouterShapeConfirmsDeletionThroughTheTypeField() {
        OpenRouterFileDeleteResponse response =
                new OpenRouterFileDeleteRequest.Builder(client(), "or_file_abc123")
                        .build()
                        .createResponse("""
                                {
                                  "id": "or_file_abc123",
                                  "type": "file_deleted"
                                }
                                """);

        assertThat(response.id()).isEqualTo("or_file_abc123");
        assertThat(response.type()).isEqualTo("file_deleted");
        assertThat(response.isDeleted()).isTrue();
    }

    @Test
    void openAiShapeConfirmsDeletionThroughTheDeletedField() {
        OpenRouterFileDeleteResponse response =
                new OpenRouterFileDeleteRequest.Builder(client(), "file-abc123")
                        .build()
                        .createResponse("""
                                {
                                  "id": "file-abc123",
                                  "deleted": true
                                }
                                """);

        assertThat(response.id()).isEqualTo("file-abc123");
        assertThat(response.type()).isNull();
        assertThat(response.isDeleted()).isTrue();
    }

    @Test
    void deletedFalseIsReportedAsFalse() {
        OpenRouterFileDeleteResponse response =
                new OpenRouterFileDeleteRequest.Builder(client(), "file-abc123")
                        .build()
                        .createResponse("""
                                {
                                  "id": "file-abc123",
                                  "deleted": false
                                }
                                """);

        assertThat(response.isDeleted()).isFalse();
    }

    @Test
    void responseAccessorsSwallowMalformedBody() {
        OpenRouterFileDeleteResponse response =
                new OpenRouterFileDeleteRequest.Builder(client(), "file-abc123")
                        .build()
                        .createResponse("{}");

        assertThat(response.id()).isNull();
        assertThat(response.type()).isNull();
        assertThat(response.isDeleted()).isNull();
    }
}
