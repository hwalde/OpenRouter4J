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
                .model("deepseek/deepseek-v4-flash-0731")
                .responseSchema(recipeSchema)
                .responseMimeType("application/json")
                .addMessage("user", "I want to eat 3 portions of smashed potatoes.")
                .execute();

        // If we trust the model obeyed:
        MyRecipe recipe = response.convertTo(MyRecipe.class);
        System.out.println("Recipe => name: " + recipe.name() + ", servings: " + recipe.servings());

        // The json_schema envelope is configurable too. By default the library sends
        // name "response_schema" and strict: true; here we choose our own name, turn
        // strict mode off (API default) and give the model a description of the output.
        OpenRouterChatCompletionResponse namedResponse = client.chat().completion()
                .model("deepseek/deepseek-v4-flash-0731")
                .responseSchema(recipeSchema)
                .responseSchemaName("recipe_extraction")
                .responseSchemaStrict(false)
                .responseSchemaDescription("A recipe with its name and the number of servings")
                // Trap: the schema can still be silently dropped on endpoints without
                // structured-outputs support - requireParameters routes only to
                // endpoints that support all request parameters.
                .requireParameters(true)
                .addMessage("user", "I want to eat 3 portions of smashed potatoes.")
                .execute();

        MyRecipe namedRecipe = namedResponse.convertTo(MyRecipe.class);
        System.out.println("Recipe (custom schema name) => name: " + namedRecipe.name()
                + ", servings: " + namedRecipe.servings());
    }
}
