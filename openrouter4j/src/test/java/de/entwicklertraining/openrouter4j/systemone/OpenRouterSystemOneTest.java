package de.entwicklertraining.openrouter4j.systemone;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.decisions.OpenRouterDecisionQuestion;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the System One request at the request-JSON level (the Decisions wire
 * shape on the ordinary {@code /systemone} path, bare model ids) and the
 * response accessors against the documented {@code DecisionsResponse} shape.
 */
class OpenRouterSystemOneTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    private OpenRouterSystemOneRequest.Builder baseBuilder() {
        return client().systemOne()
                .model("typesafe/jev-1.13")
                .state("The customer reports a blank checkout page.")
                .question("is_bug", OpenRouterDecisionQuestion.noul("Is this a defect?"));
    }

    @Test
    void requestUsesPostOnThePlainSystemOnePath() {
        OpenRouterSystemOneRequest request = baseBuilder().build();
        assertThat(request.getRelativeUrl()).isEqualTo("/systemone");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void minimalRequestBodyCarriesModelStateAndQuestions() {
        OpenRouterSystemOneRequest request = baseBuilder().build();
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getString("model")).isEqualTo("typesafe/jev-1.13");
        assertThat(body.getString("state")).isEqualTo("The customer reports a blank checkout page.");
        JSONObject questions = body.getJSONObject("questions");
        assertThat(questions.toMap()).containsOnlyKeys("is_bug");
        assertThat(questions.getJSONObject("is_bug").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                        "type", "noul", "instructions", "Is this a defect?"));
        assertThat(body.has("user")).isFalse();
        assertThat(body.has("session_id")).isFalse();
        assertThat(body.has("trace")).isFalse();
        assertThat(body.has("provider")).isFalse();

        assertThat(request.model()).isEqualTo("typesafe/jev-1.13");
        assertThat(request.stateText()).isEqualTo("The customer reports a blank checkout page.");
        assertThat(request.questions()).containsOnlyKeys("is_bug");
        assertThat(request.questions().get("is_bug").toJson().getString("instructions"))
                .isEqualTo("Is this a defect?");
        assertThat(request.user()).isNull();
        assertThat(request.sessionId()).isNull();
        assertThat(request.trace()).isNull();
    }

    @Test
    void requestAccessorsReadBackTheConfiguredOptionals() {
        OpenRouterSystemOneRequest request = client().systemOne()
                .model("jev-1.13")
                .state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .user("user-1")
                .sessionId("session-1")
                .trace(de.entwicklertraining.openrouter4j.OpenRouterTraceConfig.builder()
                        .traceId("trace-1")
                        .build())
                .build();
        assertThat(request.model()).isEqualTo("jev-1.13");
        assertThat(request.user()).isEqualTo("user-1");
        assertThat(request.sessionId()).isEqualTo("session-1");
        assertThat(request.trace()).isNotNull();
        assertThat(request.trace().traceId()).isEqualTo("trace-1");
        assertThat(request.questions()).containsOnlyKeys("k");
        assertThat(request.questions().get("k").toJson().toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                        "type", "noul", "instructions", "i"));
    }

    @Test
    void optionalsAreUnsetAgainByExplicitNullAndProviderOptionsReplace() {
        JSONObject cleared = new JSONObject(client().systemOne()
                .model("jev-1.13").state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .user("u").user(null)
                .sessionId("s1").sessionId(null)
                .trace(de.entwicklertraining.openrouter4j.OpenRouterTraceConfig.builder()
                        .traceId("t").build()).trace(null)
                .requireParameters(true).requireParameters(null)
                .allowFallbacks(true).allowFallbacks(null)
                .build()
                .getBody());
        assertThat(cleared.has("user")).isFalse();
        assertThat(cleared.has("session_id")).isFalse();
        assertThat(cleared.has("trace")).isFalse();
        assertThat(cleared.has("provider")).isFalse();

        JSONObject replaced = new JSONObject(client().systemOne()
                .model("jev-1.13").state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .providerOrder("a", "b")
                .providerOrder("c")
                .build()
                .getBody());
        assertThat(replaced.getJSONObject("provider").getJSONArray("order").toList())
                .containsExactly("c");

        JSONObject onlyReplaced = new JSONObject(client().systemOne()
                .model("jev-1.13").state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .providerOnly("a", "b")
                .providerOnly("c")
                .build()
                .getBody());
        assertThat(onlyReplaced.getJSONObject("provider").getJSONArray("only").toList())
                .containsExactly("c");

        JSONObject ignoreReplaced = new JSONObject(client().systemOne()
                .model("jev-1.13").state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .providerIgnore("a", "b")
                .providerIgnore("c")
                .build()
                .getBody());
        assertThat(ignoreReplaced.getJSONObject("provider").getJSONArray("ignore").toList())
                .containsExactly("c");
    }

    @Test
    void explicitNullStateFormsRejectTheBuild() {
        assertThatThrownBy(() -> baseBuilder().state((String) null).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("state");
        assertThatThrownBy(() -> baseBuilder().state((JSONObject) null).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("state");
        assertThatThrownBy(() -> baseBuilder().state((org.json.JSONArray) null).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("state");
    }

    @Test
    void bareModelIdsAreAcceptedVerbatim() {
        JSONObject body = new JSONObject(client().systemOne()
                .model("jev-1.13")
                .state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .build()
                .getBody());
        assertThat(body.getString("model")).isEqualTo("jev-1.13");

        JSONObject latest = new JSONObject(client().systemOne()
                .model("jev-latest")
                .state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .build()
                .getBody());
        assertThat(latest.getString("model")).isEqualTo("jev-latest");
    }

    @Test
    void stateFormsAreMutuallyExclusiveAndLastOneWins() {
        JSONObject textWins = new JSONObject(baseBuilder()
                .state(new JSONObject().put("ticket", "checkout blank"))
                .state("plain text wins")
                .build()
                .getBody());
        assertThat(textWins.get("state")).isEqualTo("plain text wins");

        JSONObject objectForm = new JSONObject(client().systemOne()
                .model("jev-1.13")
                .state(new JSONObject().put("ticket", "checkout blank"))
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .build()
                .getBody());
        assertThat(objectForm.getJSONObject("state").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("ticket", "checkout blank"));

        JSONObject arrayForm = new JSONObject(client().systemOne()
                .model("jev-1.13")
                .state(new org.json.JSONArray().put("related context entry"))
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .build()
                .getBody());
        assertThat(arrayForm.getJSONArray("state").toList())
                .containsExactly("related context entry");

        JSONObject arrayWinsOverText = new JSONObject(baseBuilder()
                .state("plain text first")
                .state(new org.json.JSONArray().put("x"))
                .build()
                .getBody());
        assertThat(arrayWinsOverText.getJSONArray("state").toList()).containsExactly("x");

        JSONObject objectWinsOverText = new JSONObject(baseBuilder()
                .state("plain text first")
                .state(new JSONObject().put("ticket", "t"))
                .build()
                .getBody());
        assertThat(objectWinsOverText.getJSONObject("state").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("ticket", "t"));

        OpenRouterSystemOneRequest arrayWinner = baseBuilder()
                .state(new JSONObject().put("ticket", "t"))
                .state(new org.json.JSONArray().put("x"))
                .build();
        JSONObject arrayWinsOverObject = new JSONObject(arrayWinner.getBody());
        assertThat(arrayWinsOverObject.getJSONArray("state").toList()).containsExactly("x");
        assertThat(arrayWinner.stateArray().toList()).containsExactly("x");
        assertThat(arrayWinner.stateText()).isNull();
        assertThat(arrayWinner.stateObject()).isNull();

        JSONObject textWinsOverArray = new JSONObject(baseBuilder()
                .state(new org.json.JSONArray().put("x"))
                .state("plain text wins")
                .build()
                .getBody());
        assertThat(textWinsOverArray.get("state")).isEqualTo("plain text wins");

        JSONObject objectWinsOverArray = new JSONObject(baseBuilder()
                .state(new org.json.JSONArray().put("x"))
                .state(new JSONObject().put("ticket", "t"))
                .build()
                .getBody());
        assertThat(objectWinsOverArray.getJSONObject("state").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("ticket", "t"));
    }

    @Test
    void providerOrderIgnoreAndAllowFallbacksAreEmittedPerInvariant() {
        JSONObject orderOnly = new JSONObject(client().systemOne()
                .model("jev-1.13").state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .providerOrder("a", "b")
                .build()
                .getBody());
        assertThat(orderOnly.getJSONObject("provider").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("order", java.util.List.of("a", "b")));

        JSONObject ignoreOnly = new JSONObject(client().systemOne()
                .model("jev-1.13").state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .providerIgnore("c")
                .build()
                .getBody());
        assertThat(ignoreOnly.getJSONObject("provider").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("ignore", java.util.List.of("c")));

        JSONObject allowOnly = new JSONObject(client().systemOne()
                .model("jev-1.13").state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .allowFallbacks(false)
                .build()
                .getBody());
        assertThat(allowOnly.getJSONObject("provider").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("allow_fallbacks", false));

        JSONObject requireFalse = new JSONObject(client().systemOne()
                .model("jev-1.13").state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .requireParameters(false)
                .build()
                .getBody());
        assertThat(requireFalse.getJSONObject("provider").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("require_parameters", false));

        JSONObject onlyAlone = new JSONObject(client().systemOne()
                .model("jev-1.13").state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .providerOnly("typesafe")
                .build()
                .getBody());
        assertThat(onlyAlone.getJSONObject("provider").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                        "only", java.util.List.of("typesafe"), "allow_fallbacks", false));
    }

    @Test
    void rawQuestionHatchDelegatesUnderTheGivenKey() {
        JSONObject raw = new JSONObject()
                .put("type", "future_kind")
                .put("instructions", "x")
                .put("extra", true);
        java.util.Map<String, Object> expected = java.util.Map.of(
                "type", "future_kind", "instructions", "x", "extra", true);
        JSONObject body = new JSONObject(client().systemOne()
                .model("jev-1.13").state("s")
                .question("k", raw)
                .build()
                .getBody());
        assertThat(body.getJSONObject("questions").getJSONObject("k").toMap())
                .containsExactlyInAnyOrderEntriesOf(expected);
        assertThat(raw.toMap()).containsExactlyInAnyOrderEntriesOf(expected);
    }

    @Test
    void allowFallbacksTrueIsEmittedAndExplicitValueWinsOverProviderOnly() {
        JSONObject allowTrue = new JSONObject(client().systemOne()
                .model("jev-1.13").state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .allowFallbacks(true)
                .build()
                .getBody());
        assertThat(allowTrue.getJSONObject("provider").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("allow_fallbacks", true));

        JSONObject explicitWins = new JSONObject(client().systemOne()
                .model("jev-1.13").state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .providerOnly("typesafe")
                .allowFallbacks(true)
                .build()
                .getBody());
        assertThat(explicitWins.getJSONObject("provider").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                        "only", java.util.List.of("typesafe"), "allow_fallbacks", true));
    }

    @Test
    void providerSlugGuardsRejectEmptyLists() {
        assertThatThrownBy(() -> baseBuilder().providerOrder())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one provider slug");
        assertThatThrownBy(() -> baseBuilder().providerOnly(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> baseBuilder().providerIgnore((String) null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void stateAccessorsReflectTheWinningForm() {
        OpenRouterSystemOneRequest request = baseBuilder()
                .state(new JSONObject().put("ticket", "t"))
                .state("plain")
                .build();
        assertThat(request.stateText()).isEqualTo("plain");
        assertThat(request.stateObject()).isNull();
        assertThat(request.stateArray()).isNull();

        OpenRouterSystemOneRequest arrayToText = baseBuilder()
                .state(new org.json.JSONArray().put("x"))
                .state("plain")
                .build();
        assertThat(arrayToText.stateText()).isEqualTo("plain");
        assertThat(arrayToText.stateObject()).isNull();
        assertThat(arrayToText.stateArray()).isNull();

        OpenRouterSystemOneRequest arrayToObject = baseBuilder()
                .state(new org.json.JSONArray().put("x"))
                .state(new JSONObject().put("ticket", "t"))
                .build();
        assertThat(arrayToObject.stateObject().toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("ticket", "t"));
        assertThat(arrayToObject.stateText()).isNull();
        assertThat(arrayToObject.stateArray()).isNull();
    }

    @Test
    void optionalFieldsAndProviderSubsetAreEmittedOnlyWhenSet() {
        JSONObject body = new JSONObject(client().systemOne()
                .model("jev-1.13")
                .state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .user("user-1")
                .sessionId("session-1")
                .trace(de.entwicklertraining.openrouter4j.OpenRouterTraceConfig.builder()
                        .traceId("trace-1")
                        .build())
                .providerOnly("typesafe")
                .requireParameters(true)
                .build()
                .getBody());
        assertThat(body.getString("user")).isEqualTo("user-1");
        assertThat(body.getString("session_id")).isEqualTo("session-1");
        assertThat(body.getJSONObject("trace").getString("trace_id")).isEqualTo("trace-1");
        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getJSONArray("only").toList()).containsExactly("typesafe");
        assertThat(provider.getBoolean("allow_fallbacks")).isFalse();
        assertThat(provider.getBoolean("require_parameters")).isTrue();
        assertThat(provider.has("order")).isFalse();
        assertThat(provider.has("ignore")).isFalse();

        JSONObject requireOnly = new JSONObject(client().systemOne()
                .model("jev-1.13")
                .state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i"))
                .requireParameters(true)
                .build()
                .getBody());
        assertThat(requireOnly.getJSONObject("provider").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of("require_parameters", true));
    }

    @Test
    void questionArgumentGuardsRejectNullAndEmptyKeys() {
        assertThatThrownBy(() -> baseBuilder().question("", OpenRouterDecisionQuestion.noul("i")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> baseBuilder().question(null, OpenRouterDecisionQuestion.noul("i")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> baseBuilder().question("k", (OpenRouterDecisionQuestion) null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> baseBuilder().question("k", (JSONObject) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void repeatedQuestionKeyReplacesThePreviousQuestion() {
        JSONObject body = new JSONObject(client().systemOne()
                .model("jev-1.13")
                .state("s")
                .question("k", OpenRouterDecisionQuestion.choice("first",
                        new JSONObject().put("a", "A")))
                .question("k", OpenRouterDecisionQuestion.noul("second"))
                .build()
                .getBody());
        assertThat(body.getJSONObject("questions").toMap()).containsOnlyKeys("k");
        assertThat(body.getJSONObject("questions").getJSONObject("k").toMap())
                .containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                        "type", "noul", "instructions", "second"));
    }

    @Test
    void buildValidatesModelStateAndQuestions() {
        assertThatThrownBy(() -> client().systemOne().state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i")).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("model");
        assertThatThrownBy(() -> client().systemOne().model("").state("s")
                .question("k", OpenRouterDecisionQuestion.noul("i")).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("model");
        assertThatThrownBy(() -> client().systemOne().model("m")
                .question("k", OpenRouterDecisionQuestion.noul("i")).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("state");
        assertThatThrownBy(() -> client().systemOne().model("m").state("s").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("question");
    }

    /**
     * Fixture in the documented DecisionsResponse wire shape (System One
     * answers follow the same schema).
     */
    private static final String FIXTURE = """
            {
              "model": "typesafe/jev-1.13-20260917",
              "answers": {
                "is_bug": {"type": "noul", "noul": 0.96},
                "team": {"type": "choice", "choice": "payments",
                         "probabilities": {"payments": 0.77, "account": 0, "frontend": 0.23},
                         "confidence": 0.66},
                "urgency": {"type": "score", "score": 1.99,
                            "legend": {"0": "Can wait", "1": "This week", "2": "Blocking"},
                            "probabilities": {"0": 0, "1": 0.01, "2": 0.99},
                            "confidence": 0.99}
              },
              "usage": {"input_tokens": 476, "output_tokens": 70, "cost": 0.000019992},
              "id": "gen-dec-1789788658-XIxPQ9yAK0qYllKQrQWW",
              "provider": "TypeSafe"
            }
            """;

    @Test
    void responseExposesAnswersAndUsage() {
        OpenRouterSystemOneResponse response = baseBuilder().build().createResponse(FIXTURE);

        assertThat(response.id()).isEqualTo("gen-dec-1789788658-XIxPQ9yAK0qYllKQrQWW");
        assertThat(response.model()).isEqualTo("typesafe/jev-1.13-20260917");
        assertThat(response.provider()).isEqualTo("TypeSafe");
        assertThat(response.inputTokens()).isEqualTo(476L);
        assertThat(response.outputTokens()).isEqualTo(70L);
        assertThat(response.cost()).isEqualTo(0.000019992);
        assertThat(response.answers()).containsOnlyKeys("is_bug", "team", "urgency");

        OpenRouterSystemOneResponse.OpenRouterSystemOneAnswer noul = response.answer("is_bug");
        assertThat(noul.key()).isEqualTo("is_bug");
        assertThat(noul.type()).isEqualTo("noul");
        assertThat(noul.noulProbability()).isEqualTo(0.96);
        assertThat(noul.json().getDouble("noul")).isEqualTo(0.96);
        assertThat(noul.choice()).isNull();
        assertThat(noul.score()).isNull();
        assertThat(noul.probabilities()).isNull();

        OpenRouterSystemOneResponse.OpenRouterSystemOneAnswer choice = response.answer("team");
        assertThat(choice.type()).isEqualTo("choice");
        assertThat(choice.choice()).isEqualTo("payments");
        assertThat(choice.probabilities().keySet())
                .containsExactlyInAnyOrder("payments", "account", "frontend");
        assertThat(choice.probabilities().getDouble("payments")).isEqualTo(0.77);
        assertThat(choice.probabilities().getDouble("account")).isEqualTo(0.0);
        assertThat(choice.probabilities().getDouble("frontend")).isEqualTo(0.23);
        assertThat(choice.confidence()).isEqualTo(0.66);
        assertThat(choice.legend()).isNull();

        OpenRouterSystemOneResponse.OpenRouterSystemOneAnswer score = response.answer("urgency");
        assertThat(score.type()).isEqualTo("score");
        assertThat(score.score()).isEqualTo(1.99);
        assertThat(score.legend().toMap()).containsExactlyInAnyOrderEntriesOf(java.util.Map.of(
                "0", "Can wait", "1", "This week", "2", "Blocking"));
        assertThat(score.probabilities().keySet()).containsExactlyInAnyOrder("0", "1", "2");
        assertThat(score.probabilities().getDouble("0")).isEqualTo(0.0);
        assertThat(score.probabilities().getDouble("1")).isEqualTo(0.01);
        assertThat(score.probabilities().getDouble("2")).isEqualTo(0.99);
        assertThat(score.confidence()).isEqualTo(0.99);

        assertThat(response.answer("missing")).isNull();
    }

    @Test
    void answersSkipMalformedEntriesAndKeepTheGoodOnes() {
        OpenRouterSystemOneResponse mixed = baseBuilder().build().createResponse(
                "{\"answers\":{\"is_bug\":{\"type\":\"noul\",\"noul\":0.9},"
                        + "\"bad\":\"oops\","
                        + "\"team\":{\"type\":\"choice\",\"choice\":\"payments\"}}}");
        assertThat(mixed.answers()).containsOnlyKeys("is_bug", "team");
        assertThat(mixed.answer("bad")).isNull();
        assertThat(mixed.answer("is_bug").noulProbability()).isEqualTo(0.9);
        assertThat(mixed.answer("team").choice()).isEqualTo("payments");
    }

    @Test
    void responseAccessorsSwallowMalformedBody() {
        OpenRouterSystemOneResponse response =
                baseBuilder().build().createResponse("{\"answers\":\"oops\"}");
        assertThat(response.id()).isNull();
        assertThat(response.model()).isNull();
        assertThat(response.provider()).isNull();
        assertThat(response.answers()).isEmpty();
        assertThat(response.inputTokens()).isNull();
        assertThat(response.outputTokens()).isNull();
        assertThat(response.cost()).isNull();
    }
}
