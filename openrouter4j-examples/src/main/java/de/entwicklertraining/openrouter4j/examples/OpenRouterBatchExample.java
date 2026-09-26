package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.batches.OpenRouterBatch;
import de.entwicklertraining.openrouter4j.batches.OpenRouterBatchEndpoint;
import de.entwicklertraining.openrouter4j.batches.OpenRouterBatchItem;
import de.entwicklertraining.openrouter4j.batches.OpenRouterBatchListResponse;
import de.entwicklertraining.openrouter4j.batches.OpenRouterBatchResponse;
import de.entwicklertraining.openrouter4j.batches.OpenRouterBatchResult;
import de.entwicklertraining.openrouter4j.batches.OpenRouterBatchSubmitRequest;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionRequest;

import org.json.JSONObject;

/**
 * Demonstrates the Batch API: submit asynchronous inference requests in one
 * call, poll until the batch is terminal, read the per-request results and
 * delete the batch. Batch inference is typically billed at ~50% of the
 * standard per-token pricing and must finish within the {@code 24h}
 * completion window.
 *
 * <p>Traps shown here: the model must have a {@code :batch} endpoint
 * variant, submission success ({@code 202 validating}) is not request
 * success, and per-request bodies are restricted (URL-only multimodal input,
 * no {@code stream: true}, no OpenRouter-orchestrated web search).
 */
public class OpenRouterBatchExample {

    public static void main(String[] args) throws InterruptedException {
        OpenRouterClient client = new OpenRouterClient();

        // Item bodies may come from an existing inference request (its
        // getBody() is reused verbatim) or from a raw JSONObject in the
        // shape of the batch's endpoint. A body may omit "model" to inherit
        // the batch-level model; a body that sets its own model must match.
        OpenRouterChatCompletionRequest secondQuestion = client.chat().completion()
                .model("openai/gpt-4o")
                .addMessage("user", "What is a batch API?")
                .build();

        OpenRouterBatchResponse<OpenRouterBatchSubmitRequest> submitted = client.batches().submit()
                .endpoint(OpenRouterBatchEndpoint.CHAT_COMPLETIONS)
                .model("openai/gpt-4o")
                .providerOnly("openai")
                .addRequest(OpenRouterBatchItem.of("req-0001", new JSONObject()
                        .put("messages", new org.json.JSONArray()
                                .put(new JSONObject()
                                        .put("role", "user")
                                        .put("content", "Summarize OpenRouter in one sentence.")))))
                .addRequest("req-0002", secondQuestion)
                .execute();

        OpenRouterBatch batch = submitted.batch();
        System.out.println("Submitted batch " + batch.id() + ", status " + batch.status());

        // Poll until the batch reaches a terminal status.
        while (!batch.isTerminal()) {
            Thread.sleep(5000);
            batch = client.batches().get(batch.id()).execute().batch();
            System.out.println("  status " + batch.status()
                    + (batch.requestCounts() == null ? ""
                            : " (" + batch.requestCounts().completed() + "/"
                                    + batch.requestCounts().total() + " completed)"));
        }

        // Read the per-request results - one entry per custom_id, each
        // carrying the standard chat completions response in its body.
        for (OpenRouterBatchResult result : batch.results()) {
            if (result.hasResponse()) {
                System.out.println(result.customId() + ": "
                        + result.chatCompletionResponse().assistantMessage());
            } else {
                System.out.println(result.customId() + " failed: " + result.error());
            }
        }
        if (batch.usage() != null) {
            System.out.println("Usage: " + batch.usage().totalTokens() + " tokens, cost "
                    + batch.usage().cost());
        }

        // List the batches of the workspace (metadata only, newest first).
        OpenRouterBatchListResponse list = client.batches().list()
                .limit(5)
                .status("completed", "failed")
                .execute();
        for (OpenRouterBatch row : list.batches()) {
            System.out.println("  " + row.id() + " " + row.status() + " " + row.model());
        }

        // Delete the terminal batch - purges its artifacts instead of
        // waiting out the 30-day retention window.
        System.out.println("Deleted: "
                + client.batches().delete(batch.id()).execute().deletion().openrouter());
    }
}
