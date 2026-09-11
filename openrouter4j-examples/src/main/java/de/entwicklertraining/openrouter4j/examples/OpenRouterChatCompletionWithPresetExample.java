package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates preset-based chat completions: a stored, versioned OpenRouter
 * preset supplies request defaults (provider routing, system prompt,
 * generation parameters, tools, ...) and the request body overrides them per
 * call. Replace "email-copywriter" with a preset that exists in your account.
 *
 * Two ways to reference a preset, with different model semantics:
 *
 * 1. {@code .model("@preset/email-copywriter")} - the preset chooses the model
 *    as well. An unknown slug fails loudly (404, preset_not_found).
 *
 * 2. {@code .preset("email-copywriter")} emits the {@code preset} body field.
 *    Per the API the two are shallow-merged: every field present in the
 *    request overrides the preset's stored value, and preset fields not sent
 *    are preserved. Trap: the request ALWAYS carries a model (the explicit
 *    {@code .model(...)} or the builder default), so the preset's model never
 *    applies in this form - set the model you want explicitly. Second trap:
 *    an unknown slug in the {@code preset} field is silently ignored and the
 *    request runs without the preset.
 *
 * The combined form {@code .model("openai/gpt-4@preset/email-copywriter")}
 * pins a model and applies the preset in one reference.
 *
 * Trap: {@code POST /presets/{slug}/chat/completions} is NOT an inference
 * route - it creates/updates a preset from a request body (management key
 * required). Presets are created/managed in the OpenRouter web UI or via the
 * management API.
 */
public class OpenRouterChatCompletionWithPresetExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // 1. The preset supplies everything, including the model.
        OpenRouterChatCompletionResponse byReference = client.chat().completion()
                .model("@preset/email-copywriter")
                .addMessage("user", "Write a two-sentence product announcement for a coffee subscription.")
                .execute();
        print("@preset/ model reference", byReference);

        // 2. The preset field: model and temperature come from this request and win
        //    over the preset's values; everything not sent here falls back to the preset.
        OpenRouterChatCompletionResponse byField = client.chat().completion()
                .preset("email-copywriter")
                .model("deepseek/deepseek-v4-flash-0731") // always sent - overrides the preset's model
                .temperature(0.2)                         // per-call override
                .addMessage("user", "Write a two-sentence product announcement for a coffee subscription.")
                .execute();
        print("preset body field", byField);
    }

    private static void print(String label, OpenRouterChatCompletionResponse response) {
        if (response.hasError()) {
            throw new IllegalStateException(label + ": OpenRouter failed: code=" + response.errorCode()
                    + " message=" + response.errorMessage());
        }
        System.out.println("[" + label + "] Answer: " + response.assistantMessage());
        System.out.println("[" + label + "] Model served: " + response.model()
                + ", provider: " + response.provider());
    }
}
