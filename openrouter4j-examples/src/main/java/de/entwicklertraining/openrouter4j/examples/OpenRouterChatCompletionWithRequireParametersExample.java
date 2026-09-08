package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.api.base.ApiClient;
import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterJsonSchema;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates provider.require_parameters - restricting routing to endpoints that
 * support ALL request parameters (here: structured outputs via responseSchema).
 *
 * Without requireParameters(true), OpenRouter may route the request to an endpoint
 * that does NOT support structured_outputs - the schema is then silently ignored
 * and the model answers with free-form text (often markdown-fenced JSON).
 *
 * Two cases are shown:
 *   1. deepseek/deepseek-v4-flash-0731 with requireParameters(true): only schema-capable
 *      endpoints are eligible, so the response is guaranteed schema-conformant JSON.
 *   2. minimax/minimax-m3 with requireParameters(true): NO endpoint of this model
 *      supports structured_outputs, so OpenRouter answers with HTTP 404
 *      ("No endpoints found that can handle the requested parameters").
 */
public class OpenRouterChatCompletionWithRequireParametersExample {

    public record MyRecipe(String name, int servings) {}

    public static void main(String[] args) {
        OpenRouterJsonSchema recipeSchema = OpenRouterJsonSchema.objectSchema()
                .property("name", OpenRouterJsonSchema.stringSchema("Name of the recipe"), true)
                .property("servings", OpenRouterJsonSchema.integerSchema("Number of servings"), true)
                .additionalProperties(false);

        OpenRouterClient client = new OpenRouterClient();

        // Case 1: model with schema-capable endpoints -> clean structured output
        System.out.println("=== Case 1: deepseek/deepseek-v4-flash-0731 + requireParameters(true) ===");
        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .provider("alibaba")           // preferred provider (supports structured_outputs)
                .requireParameters(true)       // never route to endpoints without schema support
                .responseSchema(recipeSchema)
                .responseMimeType("application/json")
                .addMessage("user", "I want to eat 3 portions of smashed potatoes.")
                .execute();

        MyRecipe recipe = response.convertTo(MyRecipe.class);
        System.out.println("Recipe => name: " + recipe.name() + ", servings: " + recipe.servings());

        // Case 2: model WITHOUT any schema-capable endpoint -> expected HTTP 404
        System.out.println();
        System.out.println("=== Case 2: minimax/minimax-m3 + requireParameters(true) (expected 404) ===");
        try {
            client.chat().completion()
                    .model("minimax/minimax-m3")
                    .requireParameters(true)
                    .responseSchema(recipeSchema)
                    .responseMimeType("application/json")
                    .addMessage("user", "I want to eat 3 portions of smashed potatoes.")
                    .execute();
            System.out.println("Unexpected: the call succeeded although no endpoint supports structured_outputs.");
        } catch (ApiClient.HTTP_404_NotFoundException e) {
            System.out.println("Expected 404: no endpoint of minimax/minimax-m3 supports structured_outputs.");
            System.out.println("OpenRouter message: " + e.getMessage());
            System.out.println("=> Without requireParameters(true) this request would NOT fail -");
            System.out.println("   the schema would be silently ignored instead.");
        }
    }
}
