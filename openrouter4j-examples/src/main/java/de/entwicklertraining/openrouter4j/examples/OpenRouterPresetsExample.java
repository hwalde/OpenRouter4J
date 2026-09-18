package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.presets.OpenRouterPreset;
import de.entwicklertraining.openrouter4j.presets.OpenRouterPresetUpsertFromChatRequest;
import de.entwicklertraining.openrouter4j.presets.OpenRouterPresetUpsertResponse;
import de.entwicklertraining.openrouter4j.presets.OpenRouterPresetsListResponse;

/**
 * Demonstrates the preset read and management surface: GET /presets,
 * GET /presets/{slug} and POST /presets/{slug}/chat/completions.
 *
 * <p>The read endpoints are the documented way to check a preset slug's
 * existence before sending an inference request: an unknown slug in the
 * {@code preset} body field is silently ignored on inference, while this
 * read API returns 404.
 *
 * <p>The create/update routes are NOT inference routes: the request body is
 * stored as a new version of the preset (create-not-infer semantics) and a
 * management key is required - a normal inference key is rejected with an
 * authorization error. The body is reused verbatim from an ordinary
 * chat-completions request; the {@code messages} and {@code stream} fields
 * are silently ignored when storing.
 */
public class OpenRouterPresetsExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // Read: list the presets of the account.
        OpenRouterPresetsListResponse list = client.presets().list().limit(20).execute();
        System.out.println("Total presets: " + list.totalCount());
        for (OpenRouterPreset preset : list.presets()) {
            System.out.println("  " + preset.slug() + " (" + preset.status() + ")"
                    + " -> version " + preset.designatedVersionId());
        }

        // Read: one preset with its currently designated version - the
        // existence check for a slug before inference.
        String slug = "my-preset";
        OpenRouterPreset existing = client.presets().get(slug).execute().preset();
        if (existing == null) {
            System.out.println(slug + " does not exist yet; creating it.");
        } else {
            System.out.println(slug + " exists; designated version: " + existing.designatedVersionId());
        }

        // Create/update: store an ordinary chat-completions request body as
        // a new version of the preset (management key required). The chat
        // builder's default model is always emitted, so set .model(...)
        // explicitly to pin the model the preset should use.
        OpenRouterPresetUpsertResponse<OpenRouterPresetUpsertFromChatRequest> upsert =
                client.presets()
                        .upsertFromChat(slug)
                        .body(client.chat().completion()
                                .model("openai/gpt-4o")
                                .addMessage("system", "You are a terse assistant.")
                                .temperature(0.3)
                                .build())
                        .execute();
        OpenRouterPreset stored = upsert.preset();
        if (stored != null && stored.designatedVersion() != null) {
            System.out.println("Stored version " + stored.designatedVersion().version()
                    + " with config " + stored.designatedVersion().config());
        }

        // Inference with the preset afterwards (see the 1.15.0 preset field):
        // client.chat().completion().model("@preset/my-preset").addMessage("user", "Hi").execute()
    }
}
