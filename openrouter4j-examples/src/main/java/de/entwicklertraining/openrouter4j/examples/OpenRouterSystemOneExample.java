package de.entwicklertraining.openrouter4j.examples;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.decisions.OpenRouterDecisionQuestion;
import de.entwicklertraining.openrouter4j.systemone.OpenRouterSystemOneResponse;
import org.json.JSONObject;

import java.util.List;

/**
 * Demonstrates the System One API: POST /systemone evaluates content against
 * typed questions with a System One model (TypeSafe Jev family) - boolean
 * {@code noul}, multi-class {@code choice} and ordered {@code score}.
 *
 * <p>Related but distinct from the Decisions API ({@code client.decisions()}):
 * the wire format and the {@link OpenRouterDecisionQuestion} factories are
 * identical, but System One lives on the ordinary {@code /api/v1} base (no
 * alpha namespace) and accepts bare model ids such as {@code jev-1.13} /
 * {@code jev-latest}, which the API maps onto the {@code typesafe/} namespace.
 */
public class OpenRouterSystemOneExample {

    public static void main(String[] args) {
        OpenRouterClient client = new OpenRouterClient();

        OpenRouterSystemOneResponse response = client.systemOne()
                // Bare System One model ids work here (mapped to typesafe/).
                .model("jev-1.13")
                // The state accepts a plain string, a JSON object or an
                // array of related context - the last form set wins.
                .state(new JSONObject()
                        .put("customer_tier", "enterprise")
                        .put("ticket", "My checkout page shows a blank screen after I click Pay."))
                .question("is_bug", OpenRouterDecisionQuestion.noul(
                        "Is the customer reporting a software defect?",
                        new JSONObject()
                                .put("true", "The customer describes broken or unexpected product behavior.")
                                .put("false", "The customer is asking a question or requesting a feature.")))
                .question("team", OpenRouterDecisionQuestion.choice(
                        "Which team should own this ticket?",
                        new JSONObject()
                                .put("account", "Login, permissions, or profile issues.")
                                .put("frontend", "Rendering, layout, or browser compatibility issues.")
                                .put("payments", "Checkout, billing, or payment processing issues.")))
                .question("urgency", OpenRouterDecisionQuestion.score(
                        "How urgent is this ticket?",
                        List.of("Can wait for the next release",
                                "Should be fixed this week",
                                "Blocking revenue right now")))
                .execute();

        System.out.println("id:       " + response.id());
        System.out.println("model:    " + response.model());
        System.out.println("provider: " + response.provider());
        System.out.println("usage:    in=" + response.inputTokens()
                + " out=" + response.outputTokens() + " cost=" + response.cost());

        for (String key : response.answers().keySet()) {
            OpenRouterSystemOneResponse.OpenRouterSystemOneAnswer answer = response.answer(key);
            switch (answer.type()) {
                case "noul" -> System.out.println(key + ": noul=" + answer.noulProbability());
                case "choice" -> System.out.println(key + ": choice=" + answer.choice()
                        + " probabilities=" + answer.probabilities());
                case "score" -> System.out.println(key + ": score=" + answer.score()
                        + " confidence=" + answer.confidence());
                default -> System.out.println(key + ": " + answer.json());
            }
        }
    }
}
