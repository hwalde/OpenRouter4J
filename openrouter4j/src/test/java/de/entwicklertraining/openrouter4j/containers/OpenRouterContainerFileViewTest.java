package de.entwicklertraining.openrouter4j.containers;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link OpenRouterContainerFile} view and the container file list
 * response against recorded JSON shapes of the ContainerFile schema and
 * GET /containers/{container_id}/files, including null-tolerance.
 */
class OpenRouterContainerFileViewTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void containerFileViewExposesAllFields() {
        String fixture = """
                {
                  "id": "cfile_L2hvbWUvb2FpL3NoYXJlL291dC5jc3Y",
                  "object": "container.file",
                  "container_id": "sess_abc123",
                  "bytes": 2048,
                  "created_at": 1760000000,
                  "path": "/home/oai/share/out.csv",
                  "source": "assistant"
                }
                """;
        OpenRouterContainerFile file = new OpenRouterContainerFile(new JSONObject(fixture));

        assertThat(file.id()).isEqualTo("cfile_L2hvbWUvb2FpL3NoYXJlL291dC5jc3Y");
        assertThat(file.objectType()).isEqualTo("container.file");
        assertThat(file.containerId()).isEqualTo("sess_abc123");
        assertThat(file.bytes()).isEqualTo(2048L);
        assertThat(file.createdAt()).isEqualTo(1760000000L);
        assertThat(file.path()).isEqualTo("/home/oai/share/out.csv");
        assertThat(file.source()).isEqualTo("assistant");
    }

    @Test
    void containerFileViewToleratesMissingFields() {
        OpenRouterContainerFile file = new OpenRouterContainerFile(new JSONObject("{}"));

        assertThat(file.id()).isNull();
        assertThat(file.objectType()).isNull();
        assertThat(file.containerId()).isNull();
        assertThat(file.bytes()).isNull();
        assertThat(file.createdAt()).isNull();
        assertThat(file.path()).isNull();
        assertThat(file.source()).isNull();
    }

    @Test
    void containerFileViewToleratesNullJsonFieldValues() {
        String fixture = """
                {
                  "id": "cfile_YQ",
                  "bytes": null,
                  "created_at": null,
                  "path": null
                }
                """;
        OpenRouterContainerFile file = new OpenRouterContainerFile(new JSONObject(fixture));

        assertThat(file.id()).isEqualTo("cfile_YQ");
        assertThat(file.bytes()).isNull();
        assertThat(file.createdAt()).isNull();
        assertThat(file.path()).isNull();
    }

    @Test
    void jsonReturnsADefensiveCopy() {
        String fixture = """
                {
                  "id": "cfile_YQ",
                  "path": "/home/oai/share/out.csv"
                }
                """;
        OpenRouterContainerFile file = new OpenRouterContainerFile(new JSONObject(fixture));

        JSONObject copy = file.json();
        copy.put("path", "mutated");

        assertThat(file.json().getString("path")).isEqualTo("/home/oai/share/out.csv");
    }

    @Test
    void listResponseExposesFilesAndPagination() {
        String fixture = """
                {
                  "object": "list",
                  "data": [
                    {
                      "id": "cfile_L2hvbWUvb2FpL3NoYXJlL2EudHh0",
                      "object": "container.file",
                      "container_id": "sess_abc123",
                      "bytes": 12,
                      "created_at": 1760000000,
                      "path": "/home/oai/share/a.txt",
                      "source": "assistant"
                    },
                    {
                      "id": "cfile_L2hvbWUvb2FpL3NoYXJlL2IudHh0",
                      "object": "container.file",
                      "container_id": "sess_abc123",
                      "bytes": 34,
                      "created_at": 1760000100,
                      "path": "/home/oai/share/b.txt",
                      "source": "assistant"
                    }
                  ],
                  "first_id": "cfile_L2hvbWUvb2FpL3NoYXJlL2EudHh0",
                  "last_id": "cfile_L2hvbWUvb2FpL3NoYXJlL2IudHh0",
                  "has_more": true
                }
                """;
        OpenRouterContainerFileListResponse<OpenRouterContainerFileListRequest> response =
                new OpenRouterContainerFileListRequest.Builder(client(), "sess_abc123")
                        .build()
                        .createResponse(fixture);

        assertThat(response.objectType()).isEqualTo("list");
        assertThat(response.files()).hasSize(2);
        assertThat(response.files().get(0).path()).isEqualTo("/home/oai/share/a.txt");
        assertThat(response.files().get(1).id()).isEqualTo("cfile_L2hvbWUvb2FpL3NoYXJlL2IudHh0");
        assertThat(response.firstId()).isEqualTo("cfile_L2hvbWUvb2FpL3NoYXJlL2EudHh0");
        assertThat(response.lastId()).isEqualTo("cfile_L2hvbWUvb2FpL3NoYXJlL2IudHh0");
        assertThat(response.hasMore()).isTrue();
    }

    @Test
    void listResponseToleratesMissingData() {
        OpenRouterContainerFileListResponse<OpenRouterContainerFileListRequest> response =
                new OpenRouterContainerFileListRequest.Builder(client(), "sess_abc123")
                        .build()
                        .createResponse("{}");

        assertThat(response.files()).isEmpty();
        assertThat(response.firstId()).isNull();
        assertThat(response.lastId()).isNull();
        assertThat(response.hasMore()).isNull();
        assertThat(response.objectType()).isNull();
    }

    @Test
    void listResponseSkipsNonObjectDataEntries() {
        String fixture = """
                {
                  "object": "list",
                  "data": [
                    "not-an-object",
                    {
                      "id": "cfile_YQ",
                      "path": "/home/oai/share/a.txt"
                    }
                  ]
                }
                """;
        OpenRouterContainerFileListResponse<OpenRouterContainerFileListRequest> response =
                new OpenRouterContainerFileListRequest.Builder(client(), "sess_abc123")
                        .build()
                        .createResponse(fixture);

        assertThat(response.files()).hasSize(1);
        assertThat(response.files().get(0).id()).isEqualTo("cfile_YQ");
    }

    @Test
    void getResponseExposesTheFileView() {
        String fixture = """
                {
                  "id": "cfile_YQ",
                  "object": "container.file",
                  "container_id": "sess_abc123",
                  "bytes": 12,
                  "created_at": 1760000000,
                  "path": "/home/oai/share/a.txt",
                  "source": "assistant"
                }
                """;
        OpenRouterContainerFileGetResponse response =
                new OpenRouterContainerFileGetRequest.Builder(client(), "sess_abc123", "cfile_YQ")
                        .build()
                        .createResponse(fixture);

        assertThat(response.file()).isNotNull();
        assertThat(response.file().id()).isEqualTo("cfile_YQ");
        assertThat(response.file().path()).isEqualTo("/home/oai/share/a.txt");
    }

    @Test
    void getResponseToleratesEmptyBody() {
        OpenRouterContainerFileGetResponse response =
                new OpenRouterContainerFileGetRequest.Builder(client(), "sess_abc123", "cfile_YQ")
                        .build()
                        .createResponse("{}");

        assertThat(response.file()).isNull();
    }
}
