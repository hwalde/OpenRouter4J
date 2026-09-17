package de.entwicklertraining.openrouter4j.files;

import de.entwicklertraining.openrouter4j.OpenRouterClient;

/**
 * Entry point for the Files API: upload files, list them, read their
 * metadata, download their content and delete them again. The response
 * documents negotiate their shape per request ({@code _shape}:
 * {@code "openrouter"}, {@code "openai"} or {@code "anthropic"} - see
 * {@link OpenRouterFile}).
 */
public final class OpenRouterFiles {

    private final OpenRouterClient client;

    /**
     * @param client the client used to send the requests
     */
    public OpenRouterFiles(OpenRouterClient client) {
        this.client = client;
    }

    /**
     * Uploads a file:
     * POST /files (multipart form with a {@code file} part, max 100 MB).
     *
     * @return the starting point for the request
     */
    public OpenRouterFileUploadRequest.Builder upload() {
        return new OpenRouterFileUploadRequest.Builder(client);
    }

    /**
     * Lists the files of the workspace:
     * GET /files (paginated).
     *
     * @return the starting point for the request
     */
    public OpenRouterFileListRequest.Builder list() {
        return new OpenRouterFileListRequest.Builder(client);
    }

    /**
     * Fetches the metadata of one file:
     * GET /files/{fileId}.
     *
     * @param fileId the file id ({@code or_file_...} or {@code file-...})
     * @return the starting point for the request
     */
    public OpenRouterFileGetRequest.Builder get(String fileId) {
        return new OpenRouterFileGetRequest.Builder(client, fileId);
    }

    /**
     * Deletes a file (irreversible):
     * DELETE /files/{fileId}.
     *
     * @param fileId the file id to delete
     * @return the starting point for the request
     */
    public OpenRouterFileDeleteRequest.Builder delete(String fileId) {
        return new OpenRouterFileDeleteRequest.Builder(client, fileId);
    }

    /**
     * Downloads the content of one file:
     * GET /files/{fileId}/content (binary file bytes).
     *
     * @param fileId the file id whose content to download
     * @return the starting point for the request
     */
    public OpenRouterFileContentRequest.Builder content(String fileId) {
        return new OpenRouterFileContentRequest.Builder(client, fileId);
    }
}
