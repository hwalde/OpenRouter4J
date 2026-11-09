package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import de.entwicklertraining.openrouter4j.extractions.OpenRouterExtractionAnswer;
import de.entwicklertraining.openrouter4j.extractions.OpenRouterExtractionAsset;
import de.entwicklertraining.openrouter4j.extractions.OpenRouterExtractionField;
import de.entwicklertraining.openrouter4j.extractions.OpenRouterExtractionProviderPreferences;
import de.entwicklertraining.openrouter4j.extractions.OpenRouterExtractionsRequest;
import de.entwicklertraining.openrouter4j.extractions.OpenRouterExtractionsResponse;
import de.entwicklertraining.openrouter4j.extractions.OpenRouterExtractionsUsage;

/**
 * Demonstrates the Extractions surface (alpha):
 *
 * - POST /api/alpha/extractions (extract, compute, estimate or forecast
 *   typed fields from a state string and image assets)
 *
 * The endpoint lives in OpenRouter's alpha namespace - the route's own
 * description is "Alpha: the shape is not stable", so the surface may change
 * or disappear without a major version of the API. Which field types a model
 * answers is model-specific; unsupported types are rejected with 400. Asset
 * reference strings are written into {@code state} or a field's
 * {@code instructions} - every asset must be referenced at least once, and a
 * {@code state} that is exactly the sole asset's key is read as file-only.
 */
public class OpenRouterExtractionsExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // One typed field spec per extraction kind, keyed by the name the
        // answers echo. estimate/forecast bound their range with min/max;
        // forecast adds horizon and interval.
        OpenRouterExtractionsResponse response = client.extractions()
                .model("cloudflare/clairic")
                .state("Invoice INV-2291 issued 2026-09-02, net 30. "
                        + "Contacts: Dana Ruiz, Priya Shah. Total $1080.")
                .field("invoice_number", OpenRouterExtractionField.text().build())
                .field("due_date", OpenRouterExtractionField.date()
                        .instructions("Issue date plus the net payment days").build())
                .field("issued_at", OpenRouterExtractionField.datetime().build())
                .field("office_hours", OpenRouterExtractionField.time().build())
                .field("total", OpenRouterExtractionField.number().decimals(2).unit("USD").build())
                .field("project_duration", OpenRouterExtractionField.duration()
                        .unit("weeks").build())
                .field("contacts", OpenRouterExtractionField.list().items("text").build())
                .field("days_to_pay", OpenRouterExtractionField.estimate()
                        .min(0).max(120).step(1).unit("days").build())
                .field("cash_flow", OpenRouterExtractionField.forecast()
                        .min(0).max(5000).horizon(6).interval("month").unit("USD").build())
                // reference_date anchors dates written without a year
                .referenceDate("2026-10-28")
                // session_id groups related requests for observability
                // (Broadcast, private logging); never sent to the provider
                .sessionId("session-1234")
                .user("employee_123")
                .trace(OpenRouterTraceConfig.builder().traceId("trace-1").build())
                .provider(OpenRouterExtractionProviderPreferences.builder()
                        .zdr(true)
                        .build())
                .execute();

        System.out.println("Extraction " + response.id()
                + " via " + response.provider() + " (truncated: " + response.truncated() + ")");
        for (OpenRouterExtractionAnswer answer : response.answers().values()) {
            System.out.println("  " + answer.name()
                    + " [" + answer.type() + "] = " + answer.value()
                    + " (guess: " + answer.guess() + ", p_null: " + answer.pNull() + ")");
        }

        // estimate answers carry the distribution, list answers per-item
        // confidences, forecast answers the per-step series; spans index
        // Unicode code points into the state text and source() names where
        // the value was read from (state/image/document).
        OpenRouterExtractionAnswer estimate = response.answer("days_to_pay");
        System.out.println("days_to_pay mean=" + estimate.mean()
                + " median=" + estimate.median()
                + " p10=" + estimate.p10() + " p90=" + estimate.p90()
                + " p_above=" + estimate.pAbove() + " p_below=" + estimate.pBelow());
        OpenRouterExtractionAnswer contacts = response.answer("contacts");
        System.out.println("contacts confidences=" + contacts.confidences()
                + " spans=" + contacts.spans() + " source=" + contacts.source());
        OpenRouterExtractionAnswer forecast = response.answer("cash_flow");
        System.out.println("cash_flow steps=" + forecast.forecast().size()
                + " meanSeries=" + forecast.meanSeries());
        OpenRouterExtractionsUsage usage = response.usage();
        System.out.println("Usage: " + usage.inputTokens() + " in / "
                + usage.outputTokens() + " out, cost " + usage.cost()
                + " in " + usage.passes() + " pass(es)");

        // Assets: an image sent once and referenced by key. Write the key
        // wherever the asset belongs in state or a field's instructions -
        // a state that is exactly the sole asset's key is read as file-only.
        OpenRouterExtractionsRequest withAsset = client.extractions()
                .model("cloudflare/clairic")
                .state("photo_1")
                .field("caption", OpenRouterExtractionField.text().build())
                .asset("photo_1", OpenRouterExtractionAsset.of("png", "aGVsbG8=", "image"))
                .build();
        System.out.println("File-only request body: " + withAsset.getBody());
    }
}
