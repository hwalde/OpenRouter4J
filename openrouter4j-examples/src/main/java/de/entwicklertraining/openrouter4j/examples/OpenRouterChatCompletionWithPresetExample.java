package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.chat.completion.OpenRouterChatCompletionResponse;

/**
 * Demonstrates preset-based chat completions: a stored, versioned OpenRouter
 * preset supplies the request defaults (model, provider routing, system
 * prompt, generation parameters, tools, ...) and the request body overrides
 * them per call.
 *
 * The {@code preset("email-copywriter")} builder method emits the
 * {@code preset} body field. Per the API, the two are shallow-merged: every
 * field present in the request overrides the preset's stored value, and preset
 * fields not sent are preserved - here the explicit {@code temperature(0.2)}
 * wins over the preset, everything else falls back to the preset.
 *
 * Two alternative referencing styles need no library support:
 * - direct model reference:  .model("@preset/email-copywriter")
 * - combined form:           .model("openai/gpt-4@preset/email-copywriter")
 *
 * Trap: {@code POST /presets/{slug}/chat/completions} is NOT an inference
 * route - it creates/updates a preset from a request body (management key
 * required). Presets are created/managed in the OpenRouter web UI or via the
 * management API.
 */
public class OpenRouterChatCompletionWithPresetExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterChatCompletionResponse response = client.chat().completion()
                .preset("email-copywriter")
                .temperature(0.2) // per-call override; wins over the preset's value
                .addMessage("user", "Write a two-sentence product announcement for a coffee subscription.")
                .execute();

        if (response.hasError()) {
            throw new IllegalStateException("OpenRouter failed: code=" + response.errorCode()
                    + " message=" + response.errorMessage());
        }

        System.out.println("Answer: " + response.assistantMessage());
        System.out.println("Model served: " + response.model());
        System.out.println("Provider: " + response.provider());
    }
}
