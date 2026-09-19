package de.entwicklertraining.openrouter4j.decisions;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the Decisions (alpha) request at the request-JSON level (state
 * forms, question factories, provider subset, validation) and the response
 * accessors against the wire shape recorded live on 2026-09-19
 * (POST https://openrouter.ai/api/alpha/decisions).
 */
class OpenRouterDecisionsTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    private OpenRouterDecisionsRequest.Builder baseBuilder() {
        return client().decisions()
                .model("typesafe/jev-1.13")
                .state("The customer reports a blank checkout page.")
                .question("is_bug", OpenRouterDecisionQuestion.noul("Is this a defect?"));
    }

    @Test
    void requestUsesPostOnTheAlphaDecisionsPath() {
        OpenRouterDecisionsRequest request = baseBuilder().build();
        assertThat(request.getRelativeUrl()).isEqualTo("/decisions");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void minimalRequestBodyCarriesModelStateAndQuestions() {
        JSONObject body = new JSONObject(baseBuilder().build().getBody());
        assertThat(body.getString("model")).isEqualTo("typesafe/jev-1.13");
        assertThat(body.getString("state")).isEqualTo("The customer reports a blank checkout page.");
        JSONObject questions = body.getJSONObject("questions");
        assertThat(questions.toMap()).containsOnlyKeys("is_bug");
        assertThat(questions.getJSONObject("is_bug").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                        "type", "noul", "instructions", "Is this a defect?"));
        // Unset optional fields stay out of the JSON.
        assertThat(body.has("user")).isFalse();
        assertThat(body.has("session_id")).isFalse();
        assertThat(body.has("trace")).isFalse();
        assertThat(body.has("provider")).isFalse();
    }

    @Test
    void stateFormsAreMutuallyExclusiveAndLastOneWins() {
        JSONObject textWins = new JSONObject(baseBuilder()
                .state(new JSONObject().put("ticket", "checkout blank"))
                .state("plain text")
                .build()
                .getBody());
        assertThat(textWins.getString("state")).isEqualTo("plain text");

        JSONObject objectWins = new JSONObject(baseBuilder()
                .state("plain text")
                .state(new JSONArray().put("related context entry"))
                .build()
                .getBody());
        assertThat(objectWins.getJSONArray("state").toList())
                .containsExactly("related context entry");

        JSONObject objectForm = new JSONObject(baseBuilder()
                .state(new JSONObject().put("ticket", "checkout blank"))
                .build()
                .getBody());
        assertThat(objectForm.getJSONObject("state").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("ticket", "checkout blank"));
    }

    @Test
    void typedQuestionFactoriesEmitTheirWireShapes() {
        JSONObject body = new JSONObject(client().decisions()
                .model("typesafe/jev-1.13")
                .state(new JSONObject().put("ticket", "checkout blank"))
                .question("is_bug", OpenRouterDecisionQuestion.noul(
                        "Is this a defect?",
                        new JSONObject()
                                .put("true", "Broken behavior")
                                .put("false", "A question")))
                .question("team", OpenRouterDecisionQuestion.choice(
                        "Which team owns this?",
                        new JSONObject()
                                .put("payments", "Checkout and billing")
                                .put("frontend", "Rendering issues")))
                .question("urgency", OpenRouterDecisionQuestion.score(
                        "How urgent?",
                        List.of("Can wait", "This week", "Blocking now")))
                .question("raw", OpenRouterDecisionQuestion.raw(
                        new JSONObject().put("type", "future_kind").put("instructions", "x")))
                .build()
                .getBody());
        JSONObject questions = body.getJSONObject("questions");
        assertThat(questions.getJSONObject("is_bug").getJSONObject("criteria").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                        "true", "Broken behavior", "false", "A question"));
        assertThat(questions.getJSONObject("team").getJSONObject("criteria").toMap())
                .containsOnlyKeys("payments", "frontend");
        assertThat(questions.getJSONObject("urgency").getJSONArray("criteria").toList())
                .containsExactly("Can wait", "This week", "Blocking now");
        assertThat(questions.getJSONObject("raw").getString("type")).isEqualTo("future_kind");
    }

    @Test
    void questionFactoriesValidateTheirRequirementsLoudly() {
        assertThatThrownBy(() -> OpenRouterDecisionQuestion.noul(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterDecisionQuestion.noul("x", new JSONObject()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("omit it entirely");
        assertThatThrownBy(() -> OpenRouterDecisionQuestion.choice("x", new JSONObject()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one option");
        assertThatThrownBy(() -> OpenRouterDecisionQuestion.choice("x", null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> OpenRouterDecisionQuestion.score("x", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one step");
        assertThatThrownBy(() -> OpenRouterDecisionQuestion.score("x", "a", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not be null or empty");
        assertThatThrownBy(() -> OpenRouterDecisionQuestion.raw(new JSONObject().put("x", 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type");
    }

    @Test
    void optionalFieldsAndProviderSubsetAreEmittedOnlyWhenSet() {
        JSONObject body = new JSONObject(baseBuilder()
                .user("user-1")
                .sessionId("session-1")
                .trace(OpenRouterTraceConfig.builder().traceId("trace-1").build())
                .providerOrder("provider-a")
                .requireParameters(true)
                .build()
                .getBody());
        assertThat(body.getString("user")).isEqualTo("user-1");
        assertThat(body.getString("session_id")).isEqualTo("session-1");
        assertThat(body.getJSONObject("trace").getString("trace_id")).isEqualTo("trace-1");
        assertThat(body.getJSONObject("provider").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                        "order", List.of("provider-a"), "require_parameters", true));
    }

    @Test
    void buildRejectsMissingModelStateAndQuestions() {
        assertThatThrownBy(() -> client().decisions().state("s").question("k",
                OpenRouterDecisionQuestion.noul("i")).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("model");
        assertThatThrownBy(() -> client().decisions().model("m").question("k",
                OpenRouterDecisionQuestion.noul("i")).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("state");
        assertThatThrownBy(() -> client().decisions().model("m").state("s").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("question");
        assertThatThrownBy(() -> baseBuilder().question("k", (JSONObject) null).build())
                .isInstanceOf(NullPointerException.class);
    }

    /**
     * Fixture recorded live on 2026-09-19 (model/typesafe/jev-1.13, all three
     * question types in one call).
     */
    private static final String LIVE_FIXTURE = """
            {
              "model": "typesafe/jev-1.13-20260917",
              "answers": {
                "is_bug": {"type": "noul", "noul": 0.96},
                "team": {"type": "choice", "choice": "payments",
                         "probabilities": {"payments": 0.77, "account": 0, "frontend": 0.23},
                         "confidence": 0.66},
                "urgency": {"type": "score", "score": 1.99,
                            "legend": {"0": "Can wait for the next release",
                                       "1": "Should be fixed this week",
                                       "2": "Blocking revenue right now"},
                            "probabilities": {"0": 0, "1": 0.01, "2": 0.99},
                            "confidence": 0.99}
              },
              "usage": {"input_tokens": 476, "output_tokens": 70, "cost": 0.000019992},
              "id": "gen-dec-1789788658-XIxPQ9yAK0qYllKQrQWW",
              "provider": "TypeSafe"
            }
            """;

    @Test
    void responseExposesTheLiveFixtureFields() {
        OpenRouterDecisionsResponse response =
                new OpenRouterDecisionsResponse(new JSONObject(LIVE_FIXTURE), null);

        assertThat(response.id()).isEqualTo("gen-dec-1789788658-XIxPQ9yAK0qYllKQrQWW");
        assertThat(response.model()).isEqualTo("typesafe/jev-1.13-20260917");
        assertThat(response.provider()).isEqualTo("TypeSafe");
        assertThat(response.inputTokens()).isEqualTo(476L);
        assertThat(response.outputTokens()).isEqualTo(70L);
        assertThat(response.cost()).isEqualTo(0.000019992);
        assertThat(response.answers()).containsOnlyKeys("is_bug", "team", "urgency");

        OpenRouterDecisionsResponse.OpenRouterDecisionAnswer noul = response.answer("is_bug");
        assertThat(noul.type()).isEqualTo("noul");
        assertThat(noul.noulProbability()).isEqualTo(0.96);
        assertThat(noul.choice()).isNull();
        assertThat(noul.score()).isNull();

        OpenRouterDecisionsResponse.OpenRouterDecisionAnswer choice = response.answer("team");
        assertThat(choice.type()).isEqualTo("choice");
        assertThat(choice.choice()).isEqualTo("payments");
        assertThat(choice.probabilities().getDouble("payments")).isEqualTo(0.77);
        assertThat(choice.probabilities().getDouble("account")).isEqualTo(0.0);
        assertThat(choice.probabilities().getDouble("frontend")).isEqualTo(0.23);
        assertThat(choice.probabilities().keySet()).containsExactlyInAnyOrder("payments", "account", "frontend");
        assertThat(choice.confidence()).isEqualTo(0.66);
        assertThat(choice.legend()).isNull();

        OpenRouterDecisionsResponse.OpenRouterDecisionAnswer score = response.answer("urgency");
        assertThat(score.type()).isEqualTo("score");
        assertThat(score.score()).isEqualTo(1.99);
        assertThat(score.legend().toMap()).containsOnlyKeys("0", "1", "2");
        assertThat(score.confidence()).isEqualTo(0.99);

        assertThat(response.answer("missing")).isNull();
    }

    @Test
    void responseAccessorsSwallowMalformedBody() {
        OpenRouterDecisionsResponse response =
                new OpenRouterDecisionsResponse(new JSONObject("{\"answers\":\"oops\"}"), null);
        assertThat(response.id()).isNull();
        assertThat(response.model()).isNull();
        assertThat(response.provider()).isNull();
        assertThat(response.answers()).isEmpty();
        assertThat(response.inputTokens()).isNull();
        assertThat(response.outputTokens()).isNull();
        assertThat(response.cost()).isNull();
    }
}
