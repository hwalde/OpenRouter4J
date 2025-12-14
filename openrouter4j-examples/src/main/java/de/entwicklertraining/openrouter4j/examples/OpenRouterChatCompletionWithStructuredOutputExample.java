package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterJsonSchema;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates returning structured JSON from OpenRouter by specifying a responseSchema
 */
public class OpenRouterChatCompletionWithStructuredOutputExample {

    public record MyRecipe(String name, int servings) {}

    public static void main(String[] args) {
        // Build a simple schema
        // Expect: { "name":"Chocolate Cake", "servings":4 }
        OpenRouterJsonSchema recipeSchema = OpenRouterJsonSchema.objectSchema()
                .property("name", OpenRouterJsonSchema.stringSchema("Name of the recipe"), true)
                .property("servings", OpenRouterJsonSchema.integerSchema("Number of servings"), true)
                .additionalProperties(false);

        // Create the OpenRouter client
        OpenRouterClient client = new OpenRouterClient();

        // We'll request JSON output
        OpenRouterChatCompletionResponse response = client.chat().completion()
                .model("google/gemini-2.5-flash")
                .provider("google-ai-studio")
                .responseSchema(recipeSchema)
                .responseMimeType("application/json")
                .addMessage("user", "I want to eat 3 portions of smashed potatoes.")
                .execute();

        // If we trust the model obeyed:
        MyRecipe recipe = response.convertTo(MyRecipe.class);
        System.out.println("Recipe => name: " + recipe.name() + ", servings: " + recipe.servings());
    }
}
