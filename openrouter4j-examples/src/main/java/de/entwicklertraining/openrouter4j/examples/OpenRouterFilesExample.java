package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.files.OpenRouterFile;
import de.entwicklertraining.openrouter4j.files.OpenRouterFileContentResponse;
import de.entwicklertraining.openrouter4j.files.OpenRouterFileDeleteResponse;
import de.entwicklertraining.openrouter4j.files.OpenRouterFileGetResponse;
import de.entwicklertraining.openrouter4j.files.OpenRouterFileListRequest;
import de.entwicklertraining.openrouter4j.files.OpenRouterFileListResponse;
import de.entwicklertraining.openrouter4j.files.OpenRouterFileUploadResponse;

import java.nio.charset.StandardCharsets;

/**
 * Demonstrates the Files API: upload a file (POST /files, multipart form,
 * max 100 MB), list the workspace files (GET /files), read the metadata of
 * one file (GET /files/{file_id}), download its content
 * (GET /files/{file_id}/content) and delete it again
 * (DELETE /files/{file_id} - irreversible). The document shape is
 * negotiated per request via the {@code _shape} field
 * (openrouter / openai / anthropic).
 */
public class OpenRouterFilesExample {

    public static void main(String[] args) throws Exception {
        OpenRouterClient client = new OpenRouterClient();

        // 1. Upload a small text file from raw bytes (fileByPath(Path) reads
        //    a local file instead). workspace_id and provider travel as
        //    query parameters; provider is free-form (openai, anthropic, ...).
        OpenRouterFileUploadResponse uploaded = client.files().upload()
                .fileByBytes("Hello from OpenRouter4J.".getBytes(StandardCharsets.UTF_8), "hello.txt")
                .provider("openai")
                .execute();

        OpenRouterFile uploadedFile = uploaded.file();
        System.out.println("Uploaded: " + uploadedFile.id()
                + " filename=" + uploadedFile.filename()
                + " size=" + uploadedFile.sizeBytes()
                + " shape=" + uploadedFile.shape());

        // 2. List the files of the workspace (limit 1-1000).
        OpenRouterFileListResponse<OpenRouterFileListRequest> list = client.files().list()
                .limit(10)
                .execute();
        System.out.println("Files (" + list.files().size() + "), hasMore=" + list.hasMore() + ":");
        for (OpenRouterFile file : list.files()) {
            System.out.println("- " + file.id() + " filename=" + file.filename());
        }

        // 3. Read the metadata of the uploaded file.
        OpenRouterFileGetResponse metadata = client.files().get(uploadedFile.id()).execute();
        System.out.println("Metadata: downloadable=" + metadata.file().downloadable()
                + " createdAt=" + metadata.file().createdAt());

        // 4. Download the raw file bytes.
        OpenRouterFileContentResponse content = client.files().content(uploadedFile.id()).execute();
        System.out.println("Downloaded " + content.length() + " bytes");

        // 5. Delete the file again (irreversible - guarded so a missing file
        //    does not fail the example).
        if (uploadedFile.id() != null) {
            OpenRouterFileDeleteResponse deleted = client.files().delete(uploadedFile.id()).execute();
            System.out.println("Deleted: " + deleted.isDeleted() + " (" + deleted.type() + ")");
        }
    }
}
