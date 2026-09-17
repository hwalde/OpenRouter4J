package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.containers.OpenRouterContainerFile;
import de.entwicklertraining.openrouter4j.containers.OpenRouterContainerFileListRequest;
import de.entwicklertraining.openrouter4j.containers.OpenRouterContainerFileListResponse;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Demonstrates the code-execution container file API: after the bash/shell
 * server tools have executed code in a sandbox container, list what the code
 * wrote (GET /containers/{containerId}/files), read one file's metadata
 * (GET /containers/{containerId}/files/{fileId}), download its bytes
 * (GET /containers/{containerId}/files/{fileId}/content) and promote the
 * file into the workspace's durable document storage so it outlives the
 * container (POST /containers/{containerId}/files/{fileId}/promote).
 *
 * <p>The container id is the canonical id exactly as returned in a bash/shell
 * server-tool result (e.g. {@code sess_abc123}; a restarted session carries
 * its own {@code -r<nonce>}-suffixed id).
 */
public class OpenRouterContainersExample {

    /** Placeholder - replace with a container id from a bash/shell server-tool result. */
    private static final String CONTAINER_ID = "sess_abc123";

    public static void main(String[] args) throws Exception {
        if (System.getenv("OPENROUTER_API_KEY") == null) {
            System.err.println("OPENROUTER_API_KEY is not set - set it to run this example against the live API.");
            return;
        }

        OpenRouterClient client = new OpenRouterClient();

        // 1. List the files the executed code wrote into the container,
        //    in lexicographic path order. Use .limit(...) and .after(lastId)
        //    to page through larger listings.
        OpenRouterContainerFileListResponse<OpenRouterContainerFileListRequest> list =
                client.containers().listFiles(CONTAINER_ID).execute();
        System.out.println("Container files (" + list.files().size() + ", has_more=" + list.hasMore() + "):");
        for (OpenRouterContainerFile file : list.files()) {
            System.out.println("- " + file.id() + " path=" + file.path() + " bytes=" + file.bytes());
        }
        if (list.files().isEmpty()) {
            System.out.println("Container is empty - nothing to read or promote.");
            return;
        }

        // 2. Read the metadata of the first file.
        OpenRouterContainerFile first = list.files().get(0);
        OpenRouterContainerFile metadata =
                client.containers().file(CONTAINER_ID, first.id()).execute().file();
        System.out.println("Metadata: path=" + metadata.path()
                + " bytes=" + metadata.bytes()
                + " created_at=" + metadata.createdAt());

        // 3. Download the content bytes and write them to disk.
        byte[] content = client.containers().fileContent(CONTAINER_ID, first.id()).execute().bytes();
        Path out = Path.of("container-file-download.bin");
        Files.write(out, content);
        System.out.println("Wrote " + out.toAbsolutePath() + " (" + content.length + " bytes)");

        // 4. Promote the file into the workspace's durable document storage
        //    (the copy counts against the storage quota; promoted files are
        //    downloadable). The response is the new document in the FILES API
        //    shape - its id works with the endpoints of the files package.
        de.entwicklertraining.openrouter4j.files.OpenRouterFile promoted =
                client.containers().promoteFile(CONTAINER_ID, first.id()).execute().file();
        System.out.println("Promoted document: id=" + (promoted != null ? promoted.id() : null)
                + " filename=" + (promoted != null ? promoted.filename() : null));
    }
}
