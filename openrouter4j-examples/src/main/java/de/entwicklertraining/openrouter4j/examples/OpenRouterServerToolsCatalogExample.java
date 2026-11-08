package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.tools.OpenRouterServerToolCatalogDetails;
import de.entwicklertraining.openrouter4j.tools.OpenRouterServerToolCatalogEntry;
import de.entwicklertraining.openrouter4j.tools.OpenRouterServerToolEngine;
import de.entwicklertraining.openrouter4j.tools.OpenRouterServerToolInputName;
import de.entwicklertraining.openrouter4j.tools.OpenRouterServerToolPrice;
import de.entwicklertraining.openrouter4j.tools.OpenRouterToolsListResponse;

/**
 * Demonstrates the server-tool catalog (read-only, any valid API key):
 *
 * - GET /tools (list: accepted {@code tools[].type} spellings per API
 *   format, engines with their pricing, native-support counts)
 * - GET /tools/{name} (one tool by canonical name, with the models that run
 *   it natively)
 *
 * The catalog is the discovery counterpart of the request-side
 * {@code OpenRouterServerTool} family: validate a {@code tools[].type}
 * spelling against the aliases and reason about tool billing against the
 * engines' pricing sources. Trap: a get by an <em>alias</em> answers HTTP
 * 308 (the canonical URL is only in the {@code Location} header, which the
 * transport does not surface), so resolve the canonical id first (step 2)
 * and get by that.
 */
public class OpenRouterServerToolsCatalogExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // 1. Catalog walk: every server tool OpenRouter can run on behalf
        //    of a model. With no offset/limit the API answers the full
        //    list in one page; links.next is null on the last page.
        OpenRouterToolsListResponse<?> page = client.serverTools().list().execute();
        System.out.println("Server tools: " + page.items().size()
                + " of " + page.totalCount()
                + " (next page: " + page.nextPageUrl() + ")");
        for (OpenRouterServerToolCatalogEntry tool : page.items()) {
            System.out.println("  " + tool.id() + " - " + tool.name()
                    + " [" + tool.status() + "] formats " + tool.supportedApiFormats());
            for (OpenRouterServerToolInputName alias : tool.aliases()) {
                System.out.println("    accepted spelling: " + alias.type()
                        + " on " + alias.apiFormats());
            }
            for (OpenRouterServerToolEngine engine : tool.engines()) {
                System.out.println("    engine " + engine.id() + " (" + engine.name() + ")"
                        + " executed_by=" + engine.executedBy()
                        + " byok=" + engine.byok()
                        + " billed by=" + engine.pricingSource());
                for (OpenRouterServerToolPrice price : engine.pricing()) {
                    // price is a decimal-as-string - parse with BigDecimal
                    System.out.println("      " + price.price() + " USD per " + price.unit()
                            + (price.mode() != null ? " (mode " + price.mode() + ")" : ""));
                }
            }
        }

        // 2. An alias lookup: you hold a non-canonical spelling (e.g. a
        //    model's tools[].type) and want the tool behind it. A get() by
        //    the alias itself FAILS with HTTP 308 (the canonical URL is
        //    only in the Location header, which the transport does not
        //    surface - see the class javadoc), so resolve the canonical id
        //    from the catalog's aliases() and get by that.
        String heldSpelling = "web_search_preview";
        OpenRouterServerToolCatalogEntry resolved = page.items().stream()
                .filter(tool -> tool.aliases().stream()
                        .anyMatch(alias -> heldSpelling.equals(alias.type())))
                .findFirst()
                .orElse(null);
        if (resolved == null) {
            System.out.println("No server tool accepts the spelling " + heldSpelling);
            return;
        }
        System.out.println("Spelling " + heldSpelling + " resolves to " + resolved.id());

        // One tool in detail - by its CANONICAL name.
        OpenRouterServerToolCatalogDetails tool = client.serverTools()
                .get(resolved.id())
                .execute()
                .data();
        if (tool != null) {
            System.out.println("Detail: " + tool.id() + " - " + tool.summary());
            System.out.println("Runs natively on " + tool.nativeModels().size() + " models, e.g.");
            tool.nativeModels().stream().limit(3).forEach(model ->
                    System.out.println("    " + model.slug() + " via " + model.providers()));
            System.out.println("Caller-side parameters schema: " + tool.parametersSchema());
            System.out.println("Arguments the model emits: " + tool.inputSchema());
        }

        // 3. Filter by API format when only one surface matters.
        OpenRouterToolsListResponse<?> anthropic = client.serverTools()
                .list()
                .apiFormat("anthropic-messages")
                .execute();
        System.out.println("Usable on anthropic-messages: "
                + anthropic.items().stream().map(OpenRouterServerToolCatalogEntry::id).toList());
    }
}