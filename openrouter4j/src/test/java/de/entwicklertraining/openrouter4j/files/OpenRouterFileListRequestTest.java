package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the file listing request shape (query parameter presence/absence,
 * URL encoding, limit validation) against the documented query keys of
 * GET /files.
 */
class OpenRouterFileListRequestTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void unsetFiltersLeaveTheUrlBare() {
        OpenRouterFileListRequest request = new OpenRouterFileListRequest.Builder(client()).build();

        assertThat(request.getRelativeUrl()).isEqualTo("/files");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
        assertThat(request.queryParams()).isEmpty();
    }

    @Test
    void typedQueryParametersAreEmittedWhenSet() {
        OpenRouterFileListRequest request = new OpenRouterFileListRequest.Builder(client())
                .limit(50)
                .cursor("cur_1")
                .workspaceId("ws_123")
                .provider("openai")
                .after("file-1")
                .afterId("file_2")
                .beforeId("file_3")
                .order("asc")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo(
                "/files?limit=50&cursor=cur_1&workspace_id=ws_123&provider=openai"
                        + "&after=file-1&after_id=file_2&before_id=file_3&order=asc");
    }

    @Test
    void limitAcceptsTheDocumentedBoundaries() {
        assertThat(new OpenRouterFileListRequest.Builder(client()).limit(1).build().getRelativeUrl())
                .isEqualTo("/files?limit=1");
        assertThat(new OpenRouterFileListRequest.Builder(client()).limit(1000).build().getRelativeUrl())
                .isEqualTo("/files?limit=1000");
    }

    @Test
    void limitOutsideTheDocumentedRangeIsRejectedLoudly() {
        assertThatThrownBy(() -> new OpenRouterFileListRequest.Builder(client()).limit(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterFileListRequest.Builder(client()).limit(1001))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OpenRouterFileListRequest.Builder(client()).limit(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void escapeHatchAddsParametersVerbatimAndIgnoresNull() {
        OpenRouterFileListRequest request = new OpenRouterFileListRequest.Builder(client())
                .queryParam("custom", "value")
                .queryParam("ignored", null)
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/files?custom=value");
    }

    @Test
    void queryParameterValuesAreUrlEncoded() {
        OpenRouterFileListRequest request = new OpenRouterFileListRequest.Builder(client())
                .workspaceId("ws 1&2")
                .build();

        assertThat(request.getRelativeUrl()).isEqualTo("/files?workspace_id=ws+1%262");
    }
}
