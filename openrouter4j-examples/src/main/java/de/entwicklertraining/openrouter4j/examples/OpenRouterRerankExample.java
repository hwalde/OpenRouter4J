package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.rerank.OpenRouterRerankResponse;
import de.entwicklertraining.openrouter4j.rerank.OpenRouterRerankResult;

/**
 * Demonstrates POST /rerank - reranking documents against a query by
 * relevance. Results arrive sorted by relevance score descending and
 * reference the input documents by their index.
 */
public class OpenRouterRerankExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterRerankResponse response = client.rerank()
                .model("cohere/rerank-v3.5")
                .query("What is the capital of France?")
                .addDocument("Paris is the capital of France.")
                .addDocument("Berlin is the capital of Germany.")
                .addDocument("The Eiffel Tower is in Paris.")
                .topN(2)
                .execute();

        System.out.println("Model:        " + response.model());
        System.out.println("Provider:     " + response.provider());
        System.out.println("Search units: " + response.searchUnits());
        for (OpenRouterRerankResult result : response.results()) {
            System.out.println("index=" + result.index()
                    + " score=" + result.relevanceScore()
                    + " text=" + result.documentText());
        }
    }
}
