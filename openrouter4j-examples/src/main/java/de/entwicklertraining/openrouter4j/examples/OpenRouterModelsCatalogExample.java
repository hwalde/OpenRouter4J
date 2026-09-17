package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import de.entwicklertraining.openrouter4j.models.OpenRouterModel;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelEndpointsResponse;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelEndpoint;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelResponse;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelsCountResponse;
import de.entwicklertraining.openrouter4j.models.OpenRouterModelsListResponse;

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

        // 4. The models this account may use.
        OpenRouterModelsListResponse<?> userModels = client.userModels().limit(5).execute();
        System.out.println("First user models: "
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
        }

        // 6. The serving endpoints of that model (per-provider picture; the
        //    typed view covers provider/model/selected, everything else is
        //    available through json()).
        OpenRouterModelEndpointsResponse endpoints =
                client.modelEndpoints("deepseek/deepseek-v4-flash-0731").execute();
        List<OpenRouterModelEndpoint> endpointList = endpoints.endpoints();
        System.out.println("Endpoints: " + endpointList.size());
        for (OpenRouterModelEndpoint endpoint : endpointList.stream().limit(5).toList()) {
            System.out.printf("  %s (selected: %s)%n", endpoint.provider(), endpoint.selected());
        }
    }
}
