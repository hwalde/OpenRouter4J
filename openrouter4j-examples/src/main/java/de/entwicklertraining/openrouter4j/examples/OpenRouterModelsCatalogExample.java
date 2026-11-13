package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import de.entwicklertraining.openrouter4j.OpenRouterWorkloadPerf;
import de.entwicklertraining.openrouter4j.models.OpenRouterModel;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelEndpointsResponse;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelEndpoint;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelResponse;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelsCountResponse;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelsListResponse;
import de.entwicklertraining.openrouter4j.models.OpenRouterPricingOverride;

import java.util.List;

/**
 * Demonstrates the model-catalog endpoints:
 *
 * - GET /models (typed filters, e.g. by supported parameters - the data behind
 *   the {@code requireParameters(true)} pitfall)
 * - GET /models/count
 * - GET /models/user (the models the account may use)
 * - GET /model/{author}/{slug} (one entry)
 * - GET /models/{author}/{slug}/endpoints (the serving endpoints of a model)
 */
public class OpenRouterModelsCatalogExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        // 1. Full catalog, filtered: models that support structured outputs,
        //    sorted by price. supported_parameters is the catalog question
        //    behind the requireParameters(true) trap of the chat builder.
        OpenRouterModelsListResponse<?> list = client.models()
                .supportedParameters("response_format")
                .maxOutputPrice(2.0)
                .sort("pricing-low-to-high")
                .limit(10)
                .execute();

        System.out.println("Models supporting response_format (first " + list.models().size() + "):");
        for (OpenRouterModel model : list.models()) {
            System.out.printf("  %s (%s) - prompt %s USD/token, output modalities %s%n",
                    model.id(),
                    model.name(),
                    model.pricingCompletion(),
                    model.outputModalities());
        }

        // 2. Filter by quality signals: recent models with a tool-calling
        //    success rate of at least 80% (fraction in [0,1]) and a minimum
        //    Artificial Analysis agentic index.
        OpenRouterModelsListResponse<?> capable = client.models()
                .minToolSuccessRate(0.8)
                .minAgenticIndex(30.0)
                .maxAgeDays(365)
                .limit(10)
                .execute();
        System.out.println("Recent capable models (first " + capable.models().size() + "):");
        for (OpenRouterModel m : capable.models()) {
            System.out.println("  " + m.id() + " / " + m.name());
        }

        // 3. Count.
        OpenRouterModelsCountResponse count = client.modelsCount().execute();
        System.out.println("Catalog size: " + count.count());

        // 4. The models this account may use (filtered by user preferences,
        //    privacy settings, guardrails and optional harness client policy,
        //    sorted server-side).
        OpenRouterModelsListResponse<?> userModels = client.userModels()
                .sort("pricing-low-to-high")
                .client("claude_code")
                .limit(5)
                .execute();
        System.out.println("First user models (claude_code, cheapest first): "
                + userModels.models().stream().map(OpenRouterModel::id).toList());

        // 5. One entry with its typed pricing and architecture data.
        OpenRouterModelResponse one = client.model("deepseek/deepseek-v4-flash-0731").execute();
        OpenRouterModel model = one.model();
        if (model != null) {
            System.out.println("deepseek/deepseek-v4-flash-0731:");
            System.out.println("  context length: " + model.contextLength());
            System.out.println("  supported parameters: " + model.supportedParameters());
            System.out.println("  top provider moderated: " + model.topProviderIsModerated());
            System.out.println("  default parameters: " + model.defaultParameters());
            System.out.println("  per-request limits: " + model.perRequestLimits());
            if (model.reasoning() != null) {
                System.out.println("  reasoning: default effort " + model.reasoning().defaultEffort()
                        + ", mandatory " + model.reasoning().mandatory()
                        + ", supported efforts " + model.reasoning().supportedEfforts());
            }
            if (model.benchmarks() != null) {
                System.out.println("  benchmarks: intelligence index " + model.benchmarks().intelligenceIndex()
                        + ", coding index " + model.benchmarks().codingIndex()
                        + ", agentic index " + model.benchmarks().agenticIndex()
                        + ", design-arena rows " + model.benchmarks().designArenaEntries().size());
            }
            // The newer price fields, and the date/size-scoped overrides - a
            // cost estimate must consult the overrides, not only the base
            // prices, whenever any are present.
            System.out.println("  image token/output, audio output, reasoning, web search: "
                    + model.pricingImageToken() + " / " + model.pricingImageOutput()
                    + " / " + model.pricingAudioOutput() + " / " + model.pricingInternalReasoning()
                    + " / " + model.pricingWebSearch());
            System.out.println("  cache write 1h / input audio cache / discount: "
                    + model.pricingInputCacheWrite1h() + " / " + model.pricingInputAudioCache()
                    + " / " + model.pricingDiscount());
            if (!model.pricingOverrides().isEmpty()) {
                OpenRouterPricingOverride first = model.pricingOverrides().get(0);
                System.out.println("  pricing override for prompt size > " + first.minPromptTokens()
                        + " on " + first.utcDays() + " " + first.utcStart() + "-" + first.utcEnd()
                        + " UTC: prompt " + first.prompt() + " / completion " + first.completion());
            }
        }

        // 6. The serving endpoints of that model (per-provider picture: the
        //    limits, prices and routing-relevant reliability numbers of each
        //    endpoint).
        OpenRouterModelEndpointsResponse endpoints =
                client.modelEndpoints("deepseek/deepseek-v4-flash-0731").execute();
        List<OpenRouterModelEndpoint> endpointList = endpoints.endpoints();
        System.out.println("Endpoints: " + endpointList.size());
        for (OpenRouterModelEndpoint endpoint : endpointList.stream().limit(5).toList()) {
            System.out.printf("  %s (tag %s, context %d, uptime 30m %s)%n",
                    endpoint.name(),
                    endpoint.tag(),
                    endpoint.contextLength(),
                    endpoint.uptimeLast30m());
            // Latency/throughput percentiles and the per-workload figures
            // behind them - only visible when authenticated.
            if (endpoint.latency() != null) {
                System.out.printf("    latency p50 %s ms / throughput p50 %s tok/s%n",
                        endpoint.latency().p50(),
                        endpoint.throughput() != null ? endpoint.throughput().p50() : null);
            }
            if (endpoint.perfLast30mByWorkload().containsKey("text_generation")) {
                OpenRouterWorkloadPerf text = endpoint.perfLast30mByWorkload().get("text_generation");
                System.out.printf("    text_generation: %s requests, latency p50 %s ms%n",
                        text.requestCount(),
                        text.latency() != null ? text.latency().p50() : null);
            }
            // tool_choice in supported_parameters only says the parameter is
            // accepted; the per-variant flags say which values passed testing.
            if (endpoint.supportsToolChoice() != null) {
                System.out.printf("    tool_choice auto %s / function %s / none %s / required %s%n",
                        endpoint.supportsToolChoice().auto(),
                        endpoint.supportsToolChoice().function(),
                        endpoint.supportsToolChoice().none(),
                        endpoint.supportsToolChoice().required());
            }
            System.out.printf("    service tier %s, native tools %s%n",
                    endpoint.serviceTier(), endpoint.nativeTools().keySet());
        }
    }
}
