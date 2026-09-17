package de.entwicklertraining.openrouter4j.containers;

import de.entwicklertraining.openrouter4j.OpenRouterClient;

/**
 * Entry point for the code-execution container file API: list the files a
 * bash/shell server tool wrote into a sandbox container, read one file's
 * metadata, download its content and promote it into the workspace's durable
 * document storage.
 *
 * <p>The container id is the canonical id exactly as returned in a bash/shell
 * server-tool result (e.g. {@code sess_abc123}; a restarted session carries
 * its own {@code -r<nonce>}-suffixed id).
 */
public class OpenRouterContainers {
    private final OpenRouterClient client;

    /**
     * @param client the client used to send the requests
     */
    public OpenRouterContainers(OpenRouterClient client) {
        this.client = client;
    }

    /**
     * Lists the files of one code-execution container in lexicographic path
     * order:
     * GET /containers/{containerId}/files.
     *
     * @param containerId the container id ({@code sess_...}) whose files to list
     * @return the starting point for the request
     */
    public OpenRouterContainerFileListRequest.Builder listFiles(String containerId) {
        return new OpenRouterContainerFileListRequest.Builder(client, containerId);
    }

    /**
     * Reads the metadata of one container file:
     * GET /containers/{containerId}/files/{fileId}.
     *
     * @param containerId the container id ({@code sess_...}) holding the file
     * @param fileId the container file id ({@code cfile_...})
     * @return the starting point for the request
     */
    public OpenRouterContainerFileGetRequest.Builder file(String containerId, String fileId) {
        return new OpenRouterContainerFileGetRequest.Builder(client, containerId, fileId);
    }

    /**
     * Downloads the content of one container file:
     * GET /containers/{containerId}/files/{fileId}/content (binary response).
     *
     * @param containerId the container id ({@code sess_...}) holding the file
     * @param fileId the container file id ({@code cfile_...})
     * @return the starting point for the request
     */
    public OpenRouterContainerFileContentRequest.Builder fileContent(String containerId, String fileId) {
        return new OpenRouterContainerFileContentRequest.Builder(client, containerId, fileId);
    }

    /**
     * Promotes one container file into the workspace's durable document
     * storage (the copy counts against the storage quota):
     * POST /containers/{containerId}/files/{fileId}/promote.
     *
     * @param containerId the container id ({@code sess_...}) holding the file
     * @param fileId the container file id ({@code cfile_...})
     * @return the starting point for the request
     */
    public OpenRouterContainerFilePromoteRequest.Builder promoteFile(String containerId, String fileId) {
        return new OpenRouterContainerFilePromoteRequest.Builder(client, containerId, fileId);
    }
}
