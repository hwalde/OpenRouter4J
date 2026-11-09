package de.entwicklertraining.openrouter4j.privateendpoints;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import java.util.Collections;
import java.util.List;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the private-endpoints request shapes (URL, method, body, headers)
 * and the response accessors against recorded JSON shapes of the
 * /private-endpoints routes.
 */
class OpenRouterPrivateEndpointTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    // ------------------------------------------------------------------
    // route shapes
    // ------------------------------------------------------------------

    @Test
    void nineRoutesProduceTheDocumentedUrlsAndMethods() {
        assertThat(client().privateEndpoints().list().build().getRelativeUrl())
                .isEqualTo("/private-endpoints");
        assertThat(client().privateEndpoints().list().build().getHttpMethod()).isEqualTo("GET");

        assertThat(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .build().getRelativeUrl()).isEqualTo("/private-endpoints");
        assertThat(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .build().getHttpMethod()).isEqualTo("POST");

        assertThat(client().privateEndpoints().get("ep-1").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep-1");
        assertThat(client().privateEndpoints().get("ep-1").build().getHttpMethod()).isEqualTo("GET");

        assertThat(client().privateEndpoints().update("ep-1").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep-1");
        assertThat(client().privateEndpoints().update("ep-1").build().getHttpMethod()).isEqualTo("PATCH");

        assertThat(client().privateEndpoints().delete("ep-1").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep-1");
        assertThat(client().privateEndpoints().delete("ep-1").build().getHttpMethod()).isEqualTo("DELETE");

        assertThat(client().privateEndpoints().activate("ep-1").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep-1/activate");
        assertThat(client().privateEndpoints().activate("ep-1").build().getHttpMethod()).isEqualTo("POST");

        assertThat(client().privateEndpoints().disable("ep-1").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep-1/disable");
        assertThat(client().privateEndpoints().disable("ep-1").build().getHttpMethod()).isEqualTo("POST");

        assertThat(client().privateEndpoints().enable("ep-1").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep-1/enable");
        assertThat(client().privateEndpoints().enable("ep-1").build().getHttpMethod()).isEqualTo("POST");

        assertThat(client().privateEndpoints().validate("ep-1", "ws-1").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep-1/validate");
        assertThat(client().privateEndpoints().validate("ep-1", "ws-1").build().getHttpMethod())
                .isEqualTo("POST");
    }

    @Test
    void byIdRoutesUrlEncodeThePathSegment() {
        assertThat(client().privateEndpoints().get("a b/c").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/a+b%2Fc");
        assertThat(client().privateEndpoints().update("a b/c").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/a+b%2Fc");
        assertThat(client().privateEndpoints().delete("a b/c").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/a+b%2Fc");
        assertThat(client().privateEndpoints().activate("a b/c").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/a+b%2Fc/activate");
        assertThat(client().privateEndpoints().validate("a b/c", "w").build().getRelativeUrl())
                .isEqualTo("/private-endpoints/a+b%2Fc/validate");
    }

    @Test
    void byIdRoutesRejectMissingIdsLoudly() {
        for (String bad : new String[] {null, ""}) {
            assertThatThrownBy(() -> client().privateEndpoints().get(bad))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> client().privateEndpoints().update(bad))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> client().privateEndpoints().delete(bad))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> client().privateEndpoints().activate(bad))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> client().privateEndpoints().disable(bad))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> client().privateEndpoints().enable(bad))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> client().privateEndpoints().validate(bad, "ws-1"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> client().privateEndpoints().validate("ep-1", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> client().privateEndpoints().validate("ep-1", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Test
    void createEmitsRequiredFieldsOnly() {
        JSONObject body = new JSONObject(client().privateEndpoints().create()
                .modelPermalslug("openai/gpt-4o-2024-08-06")
                .providerSlug("azure")
                .upstreamModelId("gpt-4o-prod")
                .build().getBody());

        assertThat(body.keySet()).containsExactlyInAnyOrder(
                "model_permaslug", "provider_slug", "upstream_model_id");
        assertThat(body.getString("model_permaslug")).isEqualTo("openai/gpt-4o-2024-08-06");
        assertThat(body.getString("provider_slug")).isEqualTo("azure");
        assertThat(body.getString("upstream_model_id")).isEqualTo("gpt-4o-prod");
    }

    @Test
    void createRequiresTheCoreFields() {
        assertThatThrownBy(() -> client().privateEndpoints().create()
                .providerSlug("azure").upstreamModelId("m").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("model_permaslug");
        assertThatThrownBy(() -> client().privateEndpoints().create()
                .modelPermalslug("openai/gpt-4o").upstreamModelId("m").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("provider_slug");
        assertThatThrownBy(() -> client().privateEndpoints().create()
                .modelPermalslug("openai/gpt-4o").providerSlug("azure").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upstream_model_id");
        assertThatThrownBy(() -> client().privateEndpoints().create()
                .modelPermalslug("").providerSlug("azure").upstreamModelId("m").build())
                .isInstanceOf(IllegalStateException.class);
        // the minimal body passes
        assertThat(client().privateEndpoints().create()
                .modelPermalslug("a").providerSlug("b").upstreamModelId("c")
                .build().getBody()).contains("\"model_permaslug\":\"a\"");
    }

    @Test
    void createEmitsEveryOptionalField() {
        JSONObject body = new JSONObject(client().privateEndpoints().create()
                .modelPermalslug("openai/gpt-4o-2024-08-06")
                .providerSlug("azure")
                .upstreamModelId("gpt-4o-prod")
                .baseUrl("https://contoso.openai.azure.com")
                .declaredParameters(List.of("temperature", "seed"))
                .declaredRegion("us")
                .declaredZdr(true)
                .pricing(OpenRouterPrivateEndpointPricing.builder()
                        .prompt("0.0000025").completion("0.00001")
                        .cacheRead("0.00000025").cacheWrite("0.000003125").build())
                .settings(OpenRouterPrivateEndpointSettingsUpdate.builder()
                        .contextLength(128000)
                        .quantization("bf16")
                        .excludedParameters(List.of("logit_bias"))
                        .completionsChat(true).completionsText(false)
                        .inputsAudio(false).inputsBase64Files(null).inputsBase64Images(true)
                        .inputsFileUrls(null).inputsImageUrls(true)
                        .reasoningSupported(true).reasoningReturnMechanism("reasoning")
                        .toolChoiceAuto(true).toolChoiceFunction(false)
                        .toolChoiceNone(true).toolChoiceRequired(true)
                        .build())
                .activate("550e8400-e29b-41d4-a716-446655440000")
                .build().getBody());

        assertThat(body.keySet()).containsExactlyInAnyOrder(
                "model_permaslug", "provider_slug", "upstream_model_id", "base_url",
                "declared_parameters", "declared_region", "declared_zdr",
                "pricing", "settings", "activate");
        assertThat(body.getString("base_url")).isEqualTo("https://contoso.openai.azure.com");
        assertThat(body.getJSONArray("declared_parameters").toList())
                .containsExactly("temperature", "seed");
        assertThat(body.get("declared_region")).isEqualTo("us");
        assertThat(body.get("declared_zdr")).isEqualTo(Boolean.TRUE);

        JSONObject pricing = body.getJSONObject("pricing");
        assertThat(pricing.keySet()).containsExactlyInAnyOrder(
                "prompt", "completion", "cache_read", "cache_write");
        assertThat(pricing.getString("prompt")).isEqualTo("0.0000025");
        assertThat(pricing.getString("cache_write")).isEqualTo("0.000003125");

        JSONObject settings = body.getJSONObject("settings");
        assertThat(settings.keySet()).containsExactlyInAnyOrder(
                "context_length", "quantization", "excluded_parameters",
                "completions", "inputs", "reasoning", "tool_choice");
        assertThat(settings.getJSONObject("completions").keySet())
                .containsExactlyInAnyOrder("chat", "text");
        assertThat(settings.getJSONObject("completions").get("text")).isEqualTo(Boolean.FALSE);
        assertThat(settings.getJSONObject("inputs").keySet()).containsExactlyInAnyOrder(
                "audio", "base64_files", "base64_images", "file_urls", "image_urls");
        assertThat(settings.getJSONObject("inputs").isNull("base64_files")).isTrue();
        assertThat(settings.getJSONObject("reasoning").keySet())
                .containsExactlyInAnyOrder("supported", "return_mechanism");
        assertThat(settings.getJSONObject("reasoning").getString("return_mechanism"))
                .isEqualTo("reasoning");
        assertThat(settings.getJSONObject("tool_choice").keySet()).containsExactlyInAnyOrder(
                "auto", "function", "none", "required");

        assertThat(body.getJSONObject("activate").keySet()).containsExactlyInAnyOrder("workspace_id");
        assertThat(body.getJSONObject("activate").getString("workspace_id"))
                .isEqualTo("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    void createTriStateLegsForDataPolicyDeclarations() {
        // unset: no key at all
        JSONObject unset = new JSONObject(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .build().getBody());
        assertThat(unset.has("declared_region")).isFalse();
        assertThat(unset.has("declared_zdr")).isFalse();

        // value: emitted verbatim, false included
        JSONObject value = new JSONObject(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .declaredRegion("europe").declaredZdr(false)
                .build().getBody());
        assertThat(value.get("declared_region")).isEqualTo("europe");
        assertThat(value.get("declared_zdr")).isEqualTo(Boolean.FALSE);

        // explicit null: emitted as JSON null - and on create the three small
        // bodies are exactly these key sets
        JSONObject explicitNull = new JSONObject(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .declaredRegion(null).declaredZdr(null)
                .build().getBody());
        assertThat(explicitNull.isNull("declared_region")).isTrue();
        assertThat(explicitNull.isNull("declared_zdr")).isTrue();
        assertThat(new JSONObject(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .declaredRegion(null).declaredZdr(null)
                .build().getBody()).toString()).isEqualTo(explicitNull.toString());
    }

    @Test
    void createValidatesDeclaredParametersLoudly() {
        assertThatThrownBy(() -> client().privateEndpoints().create()
                .declaredParameters(Collections.nCopies(26, "temperature")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("25");
        // the 25-entry boundary passes
        assertThat(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .declaredParameters(Collections.nCopies(25, "temperature")).build().getBody())
                .contains("temperature");

        // tool_choice requires tools tested or declared
        assertThatThrownBy(() -> client().privateEndpoints().create()
                .declaredParameters(List.of("temperature", "tool_choice")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tool_choice");
        assertThat(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .declaredParameters(List.of("tools", "tool_choice")).build().getBody())
                .contains("tool_choice");
        // unknown spellings pass through verbatim
        assertThat(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .declaredParameters(List.of("my_future_parameter")).build().getBody())
                .contains("my_future_parameter");
        // null unsets the list
        JSONObject body = new JSONObject(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .declaredParameters(List.of("seed")).declaredParameters(null)
                .build().getBody());
        assertThat(body.has("declared_parameters")).isFalse();
    }

    @Test
    void pricingValueValidatesTheDecimalStringPattern() {
        // boundary values pass
        OpenRouterPrivateEndpointPricing zero = OpenRouterPrivateEndpointPricing.builder()
                .prompt("0").completion("1").build();
        assertThat(zero.prompt()).isEqualTo("0");
        assertThat(zero.completion()).isEqualTo("1");
        assertThat(zero.cacheRead()).isNull();
        assertThat(OpenRouterPrivateEndpointPricing.builder()
                .prompt("12").completion("0.0000025")
                .cacheRead("0.00000025").cacheWrite("3").build().json().keySet())
                .containsExactlyInAnyOrder("prompt", "completion", "cache_read", "cache_write");

        for (String bad : new String[] {"", "abc", "-1", "1.", ".5", "1.2.3", "1,5"}) {
            assertThatThrownBy(() -> OpenRouterPrivateEndpointPricing.builder().prompt(bad))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("decimal string");
            assertThatThrownBy(() -> OpenRouterPrivateEndpointPricing.builder()
                    .completion("1").cacheRead(bad))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> OpenRouterPrivateEndpointPricing.builder().prompt(null))
                .isInstanceOf(IllegalArgumentException.class);
        // required fields are checked at build time
        assertThatThrownBy(() -> OpenRouterPrivateEndpointPricing.builder().prompt("1").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("completion");
        assertThatThrownBy(() -> OpenRouterPrivateEndpointPricing.builder().completion("1").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("prompt");
    }

    @Test
    void createCarriesTheIdempotencyKeyHeader() {
        OpenRouterPrivateEndpointCreateRequest request = client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .idempotencyKey("wayfair-gpt-4o-eastus-2026-09")
                .build();
        assertThat(request.getAdditionalHeaders())
                .containsEntry("Idempotency-Key", "wayfair-gpt-4o-eastus-2026-09");
        // without the setter no header is sent
        assertThat(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .build().getAdditionalHeaders()).doesNotContainKey("Idempotency-Key");

        assertThatThrownBy(() -> client().privateEndpoints().create().idempotencyKey(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idempotency");
        assertThatThrownBy(() -> client().privateEndpoints().create().idempotencyKey("k".repeat(256)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("255");
        assertThatThrownBy(() -> client().privateEndpoints().create().idempotencyKey("k\r\nX: 1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CR or LF");
        // the 255-character boundary passes
        assertThat(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .idempotencyKey("k".repeat(255)).build())
                .isNotNull();
    }

    @Test
    void createMaxCompletionTokensShorthandLandsInSettings() {
        JSONObject body = new JSONObject(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .maxCompletionTokens(131072)
                .build().getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder(
                "model_permaslug", "provider_slug", "upstream_model_id", "settings");
        assertThat(body.getJSONObject("settings").keySet())
                .containsExactlyInAnyOrder("max_completion_tokens");
        assertThat(body.getJSONObject("settings").get("max_completion_tokens")).isEqualTo(131072);

        // tri-state: explicit null clears to the model default
        JSONObject cleared = new JSONObject(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .maxCompletionTokens(null)
                .build().getBody());
        assertThat(cleared.getJSONObject("settings").isNull("max_completion_tokens")).isTrue();

        // a value named inside settings(...) wins over the shorthand
        JSONObject merged = new JSONObject(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .maxCompletionTokens(5)
                .settings(OpenRouterPrivateEndpointSettingsUpdate.builder()
                        .maxCompletionTokens(10).build())
                .build().getBody());
        assertThat(merged.getJSONObject("settings").get("max_completion_tokens")).isEqualTo(10);

        assertThatThrownBy(() -> client().privateEndpoints().create().maxCompletionTokens(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 1");
    }

    // ------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------

    @Test
    void updateEmitsOnlyPresentFields() {
        OpenRouterPrivateEndpointUpdateRequest empty =
                client().privateEndpoints().update("ep-1").build();
        assertThat(empty.getRelativeUrl()).isEqualTo("/private-endpoints/ep-1");
        assertThat(empty.getHttpMethod()).isEqualTo("PATCH");
        assertThat(new JSONObject(empty.getBody()).keySet()).isEmpty();

        JSONObject body = new JSONObject(client().privateEndpoints().update("ep-1")
                .baseUrl("https://contoso.openai.azure.com")
                .upstreamModelId("gpt-4o-v2")
                .build().getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("base_url", "upstream_model_id");
    }

    @Test
    void updateRejectsEmptyDraftOnlyFieldsLoudly() {
        assertThatThrownBy(() -> client().privateEndpoints().update("ep-1").baseUrl(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("base_url");
    }

    @Test
    void updateTriStateLegsForDataPolicyDeclarations() {
        JSONObject unset = new JSONObject(client().privateEndpoints().update("ep-1").build().getBody());
        assertThat(unset.has("declared_zdr")).isFalse();
        assertThat(unset.has("declared_region")).isFalse();

        JSONObject value = new JSONObject(client().privateEndpoints().update("ep-1")
                .declaredZdr(true).declaredRegion("global")
                .build().getBody());
        assertThat(value.get("declared_zdr")).isEqualTo(Boolean.TRUE);
        assertThat(value.get("declared_region")).isEqualTo("global");

        JSONObject explicitNull = new JSONObject(client().privateEndpoints().update("ep-1")
                .declaredZdr(null).declaredRegion(null)
                .build().getBody());
        assertThat(explicitNull.isNull("declared_zdr")).isTrue();
        assertThat(explicitNull.isNull("declared_region")).isTrue();
        assertThat(new JSONObject(client().privateEndpoints().update("ep-1")
                .declaredZdr(null).declaredRegion(null)
                .build().getBody()).toString()).isEqualTo(explicitNull.toString());
    }

    @Test
    void updateReplacesPricingAndDeclaredParameters() {
        JSONObject body = new JSONObject(client().privateEndpoints().update("ep-1")
                .declaredParameters(List.of("seed"))
                .pricing(OpenRouterPrivateEndpointPricing.builder()
                        .prompt("0.0000025").completion("0.00001").build())
                .build().getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("declared_parameters", "pricing");
        // the replacement pricing object omits the cache rates (they revert)
        assertThat(body.getJSONObject("pricing").keySet())
                .containsExactlyInAnyOrder("prompt", "completion");
        assertThatThrownBy(() -> client().privateEndpoints().update("ep-1")
                .declaredParameters(List.of("tool_choice")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateSettingsPartialObjectWithTriStateLegs() {
        JSONObject body = new JSONObject(client().privateEndpoints().update("ep-1")
                .settings(OpenRouterPrivateEndpointSettingsUpdate.builder()
                        .contextLength(128000)
                        .maxPromptTokens(null)
                        .reasoningReturnMechanism(null)
                        .reasoningSupported(true)
                        .toolChoiceAuto(false)
                        .build())
                .build().getBody());
        JSONObject settings = body.getJSONObject("settings");
        assertThat(settings.keySet()).containsExactlyInAnyOrder(
                "context_length", "max_prompt_tokens", "reasoning", "tool_choice");
        assertThat(settings.get("context_length")).isEqualTo(128000);
        assertThat(settings.isNull("max_prompt_tokens")).isTrue();
        assertThat(settings.getJSONObject("reasoning").keySet())
                .containsExactlyInAnyOrder("supported", "return_mechanism");
        assertThat(settings.getJSONObject("reasoning").isNull("return_mechanism")).isTrue();
        assertThat(settings.getJSONObject("reasoning").get("supported")).isEqualTo(Boolean.TRUE);
        assertThat(settings.getJSONObject("tool_choice").keySet()).containsExactlyInAnyOrder("auto");
    }

    @Test
    void settingsValidateTheDocumentedPreconditions() {
        // minimum 1 on the three limits
        assertThatThrownBy(() -> OpenRouterPrivateEndpointSettingsUpdate.builder().contextLength(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 1");
        assertThatThrownBy(() -> OpenRouterPrivateEndpointSettingsUpdate.builder().maxPromptTokens(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterPrivateEndpointSettingsUpdate.builder().maxCompletionTokens(-1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(OpenRouterPrivateEndpointSettingsUpdate.builder().contextLength(1).build())
                .isNotNull();

        // max_prompt_tokens may not exceed the context length named alongside
        assertThatThrownBy(() -> OpenRouterPrivateEndpointSettingsUpdate.builder()
                .contextLength(100).maxPromptTokens(200).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("max_prompt_tokens");
        // equal is fine, and an unset/cleared context length leaves the check
        // to the API (it compares against the model context length)
        assertThat(OpenRouterPrivateEndpointSettingsUpdate.builder()
                .contextLength(100).maxPromptTokens(100).build()).isNotNull();
        assertThat(OpenRouterPrivateEndpointSettingsUpdate.builder()
                .maxPromptTokens(200).build()).isNotNull();
        assertThat(OpenRouterPrivateEndpointSettingsUpdate.builder()
                .contextLength(null).maxPromptTokens(200).build()).isNotNull();

        // both completion APIs may not be off
        assertThatThrownBy(() -> OpenRouterPrivateEndpointSettingsUpdate.builder()
                .completionsChat(false).completionsText(false).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("settings_invalid");
        assertThat(OpenRouterPrivateEndpointSettingsUpdate.builder()
                .completionsChat(false).completionsText(true).build()).isNotNull();
        assertThat(OpenRouterPrivateEndpointSettingsUpdate.builder()
                .completionsChat(false).build()).isNotNull();
    }

    // ------------------------------------------------------------------
    // delete / validate
    // ------------------------------------------------------------------

    @Test
    void deleteAppendsTheDraftOnlyQuery() {
        assertThat(client().privateEndpoints().delete("ep-1").draftOnly(true).build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep-1?draft_only=true");
        assertThat(client().privateEndpoints().delete("ep-1").draftOnly(false).build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep-1?draft_only=false");
        assertThat(client().privateEndpoints().delete("ep-1").draftOnly(null).build().getRelativeUrl())
                .isEqualTo("/private-endpoints/ep-1");
    }

    @Test
    void validateSendsTheWorkspaceId() {
        OpenRouterPrivateEndpointValidateRequest request =
                client().privateEndpoints().validate("ep-1", "ws-1").build();
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("workspace_id");
        assertThat(body.getString("workspace_id")).isEqualTo("ws-1");
        assertThat(request.workspaceId()).isEqualTo("ws-1");
        assertThat(request.id()).isEqualTo("ep-1");
    }

    // ------------------------------------------------------------------
    // response views
    // ------------------------------------------------------------------

    @Test
    void summaryAndListResponseAccessors() {
        OpenRouterPrivateEndpointsListRequest request =
                client().privateEndpoints().list().build();
        OpenRouterPrivateEndpointsListResponse<OpenRouterPrivateEndpointsListRequest> response =
                new OpenRouterPrivateEndpointsListResponse<>(new JSONObject("""
                        {"data": [
                          {"id": "5b1c4c4e-7d0a-4a8e-9f3a-2d6c1b0e8a11", "status": "draft",
                           "model_permaslug": "openai/gpt-4o-2024-08-06",
                           "model_slug": "openai/gpt-4o", "model_name": "OpenAI: GPT-4o",
                           "provider_name": "Azure",
                           "created_at": "2026-09-24T10:30:00Z"},
                          {"id": "6c2d5d5f-8e1b-4b9f-8a4b-3e7d2c1f9b22", "status": "active",
                           "model_permaslug": "anthropic/claude-sonnet-4",
                           "model_slug": null, "model_name": null,
                           "provider_name": "Anthropic",
                           "created_at": "2026-09-25T11:00:00Z"}
                        ]}
                        """), request);

        assertThat(response.items()).hasSize(2);
        OpenRouterPrivateEndpointSummary row = response.items().get(0);
        assertThat(row.id()).isEqualTo("5b1c4c4e-7d0a-4a8e-9f3a-2d6c1b0e8a11");
        assertThat(row.status()).isEqualTo("draft");
        assertThat(row.modelPermalslug()).isEqualTo("openai/gpt-4o-2024-08-06");
        assertThat(row.modelSlug()).isEqualTo("openai/gpt-4o");
        assertThat(row.modelName()).isEqualTo("OpenAI: GPT-4o");
        assertThat(row.providerName()).isEqualTo("Azure");
        assertThat(row.createdAt()).isEqualTo("2026-09-24T10:30:00Z");
        assertThat(row.json().getString("id")).isEqualTo(row.id());
        // unknown status spellings pass through verbatim
        assertThat(response.items().get(1).status()).isEqualTo("active");
        assertThat(response.items().get(1).modelSlug()).isNull();
        assertThat(response.items().get(1).modelName()).isNull();
    }

    @Test
    void managedResponseAccessors() {
        OpenRouterPrivateEndpointCreateRequest request = client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u").build();
        OpenRouterManagedPrivateEndpointResponse<OpenRouterPrivateEndpointCreateRequest> response =
                new OpenRouterManagedPrivateEndpointResponse<>(new JSONObject("""
                        {"data": {
                          "id": "5b1c4c4e-7d0a-4a8e-9f3a-2d6c1b0e8a11", "status": "active",
                          "model_permaslug": "openai/gpt-4o-2024-08-06",
                          "model_slug": "openai/gpt-4o", "model_name": "OpenAI: GPT-4o",
                          "provider_name": "Azure",
                          "created_at": "2026-09-24T10:30:00Z",
                          "declared_zdr": true, "declared_region": "us",
                          "max_completion_tokens": 131072}
                        }
                        """), request);

        OpenRouterManagedPrivateEndpoint endpoint = response.data();
        assertThat(endpoint.id()).isEqualTo("5b1c4c4e-7d0a-4a8e-9f3a-2d6c1b0e8a11");
        assertThat(endpoint.status()).isEqualTo("active");
        assertThat(endpoint.createdAt()).isEqualTo("2026-09-24T10:30:00Z");
        assertThat(endpoint.declaredZdr()).isTrue();
        assertThat(endpoint.declaredRegion()).isEqualTo("us");
        assertThat(endpoint.maxCompletionTokens()).isEqualTo(131072);
        assertThat(endpoint.json().getString("id")).isEqualTo(endpoint.id());

        // the undeclared attestation is a real null, and so is the missing cap
        OpenRouterManagedPrivateEndpointResponse<OpenRouterPrivateEndpointCreateRequest> undeclared =
                new OpenRouterManagedPrivateEndpointResponse<>(new JSONObject("""
                        {"data": {"id": "x", "status": "draft", "model_permaslug": "m",
                          "model_slug": null, "model_name": null, "provider_name": "P",
                          "created_at": "c", "declared_zdr": null, "declared_region": null,
                          "max_completion_tokens": null}}
                        """), request);
        assertThat(undeclared.data().declaredZdr()).isNull();
        assertThat(undeclared.data().declaredRegion()).isNull();
        assertThat(undeclared.data().maxCompletionTokens()).isNull();
    }

    @Test
    void fullEndpointResponseAccessors() {
        OpenRouterPrivateEndpointGetRequest request =
                client().privateEndpoints().get("ep-1").build();
        OpenRouterPrivateEndpointResponse<OpenRouterPrivateEndpointGetRequest> response =
                new OpenRouterPrivateEndpointResponse<>(new JSONObject("""
                        {"data": {
                          "id": "5b1c4c4e-7d0a-4a8e-9f3a-2d6c1b0e8a11",
                          "status": "active",
                          "model_permaslug": "openai/gpt-4o-2024-08-06",
                          "model_slug": "openai/gpt-4o",
                          "model_name": "OpenAI: GPT-4o",
                          "provider_name": "Azure",
                          "provider_slug": "azure",
                          "base_url": "https://contoso.openai.azure.com",
                          "upstream_model_id": "gpt-4o-prod",
                          "declared_zdr": true,
                          "declared_region": "us",
                          "max_completion_tokens": 131072,
                          "pricing": {"prompt": "0.0000025", "completion": "0.00001",
                                      "cache_read": "0.00000025", "cache_write": "0.000003125"},
                          "tested_parameters": ["tools", "tool_choice", "response_format"],
                          "declared_parameters": ["temperature", "seed"],
                          "settings": {
                            "completions": {"chat": true, "text": false},
                            "context_length": 128000,
                            "excluded_parameters": ["logit_bias"],
                            "inputs": {"audio": false, "base64_files": null,
                                       "base64_images": true, "file_urls": null,
                                       "image_urls": true},
                            "max_completion_tokens": 16384,
                            "max_prompt_tokens": null,
                            "quantization": "bf16",
                            "reasoning": {"return_mechanism": "reasoning", "supported": true},
                            "tool_choice": {"auto": true, "function": false,
                                            "none": true, "required": true}
                          }
                        }}
                        """), request);

        OpenRouterPrivateEndpoint endpoint = response.data();
        assertThat(endpoint.id()).isEqualTo("5b1c4c4e-7d0a-4a8e-9f3a-2d6c1b0e8a11");
        assertThat(endpoint.status()).isEqualTo("active");
        assertThat(endpoint.modelPermalslug()).isEqualTo("openai/gpt-4o-2024-08-06");
        assertThat(endpoint.modelSlug()).isEqualTo("openai/gpt-4o");
        assertThat(endpoint.modelName()).isEqualTo("OpenAI: GPT-4o");
        assertThat(endpoint.providerName()).isEqualTo("Azure");
        assertThat(endpoint.providerSlug()).isEqualTo("azure");
        assertThat(endpoint.baseUrl()).isEqualTo("https://contoso.openai.azure.com");
        assertThat(endpoint.upstreamModelId()).isEqualTo("gpt-4o-prod");
        assertThat(endpoint.declaredZdr()).isTrue();
        assertThat(endpoint.declaredRegion()).isEqualTo("us");
        assertThat(endpoint.maxCompletionTokens()).isEqualTo(131072);
        assertThat(endpoint.declaredParameters()).containsExactly("temperature", "seed");
        assertThat(endpoint.testedParameters())
                .containsExactly("tools", "tool_choice", "response_format");
        assertThat(endpoint.pricing().prompt()).isEqualTo("0.0000025");
        assertThat(endpoint.settings().contextLength()).isEqualTo(128000);
        assertThat(endpoint.json().getString("upstream_model_id")).isEqualTo("gpt-4o-prod");

        // the full view carries no created_at - the accessor lives on the
        // summary shape only
        assertThat(endpoint.json().has("created_at")).isFalse();
    }

    @Test
    void settingsViewAccessors() {
        OpenRouterPrivateEndpointSettings settings = new OpenRouterPrivateEndpointResponse<>(
                new JSONObject("""
                        {"data": {"settings": {
                          "completions": {"chat": true, "text": null},
                          "context_length": 128000,
                          "excluded_parameters": ["logit_bias", "seed"],
                          "inputs": {"audio": false, "base64_files": null,
                                     "base64_images": true, "file_urls": null,
                                     "image_urls": true},
                          "max_completion_tokens": 16384,
                          "max_prompt_tokens": null,
                          "quantization": "bf16",
                          "reasoning": {"return_mechanism": null, "supported": true},
                          "tool_choice": {"auto": true, "function": false,
                                          "none": true, "required": true}
                        }}}
                        """),
                client().privateEndpoints().get("ep-1").build()).data().settings();

        assertThat(settings.contextLength()).isEqualTo(128000);
        assertThat(settings.maxPromptTokens()).isNull();
        assertThat(settings.maxCompletionTokens()).isEqualTo(16384);
        assertThat(settings.quantization()).isEqualTo("bf16");
        assertThat(settings.excludedParameters()).containsExactly("logit_bias", "seed");

        assertThat(settings.completions().chat()).isTrue();
        assertThat(settings.completions().text()).isNull();
        assertThat(settings.completions().json().keySet()).containsExactlyInAnyOrder("chat", "text");

        assertThat(settings.inputs().audio()).isFalse();
        assertThat(settings.inputs().base64Files()).isNull();
        assertThat(settings.inputs().base64Images()).isTrue();
        assertThat(settings.inputs().fileUrls()).isNull();
        assertThat(settings.inputs().imageUrls()).isTrue();

        assertThat(settings.reasoning().supported()).isTrue();
        assertThat(settings.reasoning().returnMechanism()).isNull();
        assertThat(settings.toolChoice().auto()).isTrue();
        assertThat(settings.toolChoice().function()).isFalse();
        assertThat(settings.toolChoice().none()).isTrue();
        assertThat(settings.toolChoice().required()).isTrue();
        assertThat(settings.json().getString("quantization")).isEqualTo("bf16");

        // absent groups answer null
        OpenRouterPrivateEndpointSettings bare = new OpenRouterPrivateEndpointResponse<>(
                new JSONObject("{\"data\": {\"settings\": {\"context_length\": 1}}}"),
                client().privateEndpoints().get("ep-1").build()).data().settings();
        assertThat(bare.completions()).isNull();
        assertThat(bare.inputs()).isNull();
        assertThat(bare.reasoning()).isNull();
        assertThat(bare.toolChoice()).isNull();
        assertThat(bare.excludedParameters()).isEmpty();
        assertThat(bare.quantization()).isNull();
    }

    @Test
    void pricingViewAccessors() {
        OpenRouterPrivateEndpointPricing pricing = new OpenRouterPrivateEndpointResponse<>(
                new JSONObject("""
                        {"data": {"pricing": {"prompt": "0.0000025", "completion": "0.00001",
                                              "cache_read": "0.00000025"}}}
                        """),
                client().privateEndpoints().get("ep-1").build()).data().pricing();
        assertThat(pricing.prompt()).isEqualTo("0.0000025");
        assertThat(pricing.completion()).isEqualTo("0.00001");
        assertThat(pricing.cacheRead()).isEqualTo("0.00000025");
        assertThat(pricing.cacheWrite()).isNull();
        assertThat(pricing.json().getString("prompt")).isEqualTo("0.0000025");

        // wrong-typed scalars answer null; wrong-typed pricing answers null
        OpenRouterPrivateEndpointPricing malformed = new OpenRouterPrivateEndpointResponse<>(
                new JSONObject("""
                        {"data": {"pricing": {"prompt": 5, "completion": true,
                                              "cache_read": null, "cache_write": "x"}}}
                        """),
                client().privateEndpoints().get("ep-1").build()).data().pricing();
        assertThat(malformed.prompt()).isNull();
        assertThat(malformed.completion()).isNull();
        assertThat(malformed.cacheRead()).isNull();
        assertThat(malformed.cacheWrite()).isEqualTo("x");
    }

    @Test
    void validationResponseAccessors() {
        OpenRouterPrivateEndpointValidateRequest request =
                client().privateEndpoints().validate("ep-1", "ws-1").build();
        OpenRouterPrivateEndpointValidationResponse<OpenRouterPrivateEndpointValidateRequest> failed =
                new OpenRouterPrivateEndpointValidationResponse<>(new JSONObject("""
                        {"data": {
                          "passed": false,
                          "checks": [
                            {"name": "auth_ok", "passed": false,
                             "reason": "no_byok_key", "upstream_message": null},
                            {"name": "model_ok", "passed": true},
                            "not-an-object"
                          ]
                        }}
                        """), request);

        assertThat(failed.data().passed()).isFalse();
        assertThat(failed.data().checks()).hasSize(2);
        OpenRouterPrivateEndpointCheck check = failed.data().checks().get(0);
        assertThat(check.name()).isEqualTo("auth_ok");
        assertThat(check.passed()).isFalse();
        assertThat(check.reason()).isEqualTo("no_byok_key");
        assertThat(check.upstreamMessage()).isNull();
        assertThat(check.upstreamStatus()).isNull();
        assertThat(check.actualModel()).isNull();
        assertThat(check.json().getString("name")).isEqualTo("auth_ok");
        // parameters are absent on a failed run
        assertThat(failed.data().parameters()).isEmpty();

        OpenRouterPrivateEndpointValidationResponse<OpenRouterPrivateEndpointValidateRequest> passed =
                new OpenRouterPrivateEndpointValidationResponse<>(new JSONObject("""
                        {"data": {
                          "passed": true,
                          "checks": [{"name": "auth_ok", "passed": true}],
                          "parameters": [
                            {"parameter": "response_format", "supported": true},
                            {"parameter": "tools", "supported": false}
                          ]
                        }}
                        """), request);
        assertThat(passed.data().passed()).isTrue();
        assertThat(passed.data().checks()).hasSize(1);
        assertThat(passed.data().parameters()).hasSize(2);
        assertThat(passed.data().parameters().get(0).parameter()).isEqualTo("response_format");
        assertThat(passed.data().parameters().get(0).supported()).isTrue();
        assertThat(passed.data().parameters().get(1).supported()).isFalse();
        assertThat(passed.data().parameters().get(0).json().getString("parameter"))
                .isEqualTo("response_format");

        // a failing check can carry the upstream status and message
        OpenRouterPrivateEndpointValidationResponse<OpenRouterPrivateEndpointValidateRequest> upstream =
                new OpenRouterPrivateEndpointValidationResponse<>(new JSONObject("""
                        {"data": {"passed": false, "checks": [{"name": "call_ok", "passed": false,
                          "reason": "upstream_error", "upstream_status": 503,
                          "upstream_message": "deployment warming up",
                          "actual_model": "gpt-4o-prod"}]}}
                        """), request);
        assertThat(upstream.data().checks().get(0).upstreamStatus()).isEqualTo(503);
        assertThat(upstream.data().checks().get(0).upstreamMessage())
                .isEqualTo("deployment warming up");
        assertThat(upstream.data().checks().get(0).actualModel()).isEqualTo("gpt-4o-prod");
    }

    @Test
    void deleteResponseAccessors() {
        OpenRouterPrivateEndpointDeleteRequest request =
                client().privateEndpoints().delete("ep-1").build();
        OpenRouterPrivateEndpointDeleteResponse<OpenRouterPrivateEndpointDeleteRequest> response =
                request.createResponse("{\"data\": {\"id\": \"5b1c4c4e-7d0a-4a8e-9f3a-2d6c1b0e8a11\"}}");
        assertThat(response.id()).isEqualTo("5b1c4c4e-7d0a-4a8e-9f3a-2d6c1b0e8a11");
    }

    @Test
    void responseAccessorsSwallowMalformedValues() {
        OpenRouterPrivateEndpointGetRequest request =
                client().privateEndpoints().get("ep-1").build();

        // wrong-typed scalars answer null, the list accessors drop non-strings
        OpenRouterPrivateEndpointResponse<OpenRouterPrivateEndpointGetRequest> malformed =
                new OpenRouterPrivateEndpointResponse<>(new JSONObject("""
                        {"data": {
                          "id": 42, "status": true, "model_permaslug": 7,
                          "model_slug": [], "model_name": {}, "provider_name": 1,
                          "provider_slug": false, "base_url": 3, "upstream_model_id": [],
                          "declared_zdr": "yes", "declared_region": 8,
                          "max_completion_tokens": "many",
                          "declared_parameters": ["temperature", 5, null],
                          "tested_parameters": "tools",
                          "settings": "not-an-object",
                          "pricing": "not-an-object"
                        }}
                        """), request);
        OpenRouterPrivateEndpoint endpoint = malformed.data();
        assertThat(endpoint.id()).isNull();
        assertThat(endpoint.status()).isNull();
        assertThat(endpoint.modelPermalslug()).isNull();
        assertThat(endpoint.modelSlug()).isNull();
        assertThat(endpoint.modelName()).isNull();
        assertThat(endpoint.providerName()).isNull();
        assertThat(endpoint.providerSlug()).isNull();
        assertThat(endpoint.baseUrl()).isNull();
        assertThat(endpoint.upstreamModelId()).isNull();
        assertThat(endpoint.declaredZdr()).isNull();
        assertThat(endpoint.declaredRegion()).isNull();
        assertThat(endpoint.maxCompletionTokens()).isNull();
        assertThat(endpoint.settings()).isNull();
        assertThat(endpoint.pricing()).isNull();
        assertThat(endpoint.declaredParameters()).containsExactly("temperature");
        assertThat(endpoint.testedParameters()).isEmpty();

        // absent or non-object data answers null
        assertThat(new OpenRouterPrivateEndpointResponse<>(new JSONObject("{}"), request).data())
                .isNull();
        assertThat(new OpenRouterPrivateEndpointResponse<>(
                new JSONObject("{\"data\": \"not-an-object\"}"), request).data()).isNull();

        // wrong-typed groups answer null, and a wrong-typed list drops rows
        OpenRouterPrivateEndpointSettings groupless = new OpenRouterPrivateEndpointResponse<>(
                new JSONObject("""
                        {"data": {"settings": {"completions": 5, "inputs": "x",
                                  "reasoning": [], "tool_choice": null,
                                  "context_length": "128k", "excluded_parameters": {}}}}
                        """), request).data().settings();
        assertThat(groupless.completions()).isNull();
        assertThat(groupless.inputs()).isNull();
        assertThat(groupless.reasoning()).isNull();
        assertThat(groupless.toolChoice()).isNull();
        assertThat(groupless.contextLength()).isNull();
        assertThat(groupless.excludedParameters()).isEmpty();

        OpenRouterPrivateEndpointsListResponse<OpenRouterPrivateEndpointsListRequest> malformedList =
                new OpenRouterPrivateEndpointsListResponse<>(new JSONObject(
                        "{\"data\": [\"not-an-object\", {\"id\": \"good\"}]}"),
                        client().privateEndpoints().list().build());
        assertThat(malformedList.items()).hasSize(1);
        assertThat(malformedList.items().get(0).id()).isEqualTo("good");

        // the validation view swallows too
        OpenRouterPrivateEndpointValidation validation =
                new OpenRouterPrivateEndpointValidationResponse<>(new JSONObject("""
                        {"data": {"passed": "yes", "checks": 5, "parameters": {"a": 1}}}
                        """), client().privateEndpoints().validate("e", "w").build()).data();
        assertThat(validation.passed()).isNull();
        assertThat(validation.checks()).isEmpty();
        assertThat(validation.parameters()).isEmpty();
    }

    @Test
    void requestsTolerateEmptyAndNullResponseBodies() {
        assertThat(client().privateEndpoints().list().build().createResponse("").items()).isEmpty();
        assertThat(client().privateEndpoints().list().build()
                .createResponse((String) null).items()).isEmpty();
        assertThat(client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u").build()
                .createResponse((String) null).data()).isNull();
        assertThat(client().privateEndpoints().get("ep-1").build().createResponse("").data()).isNull();
        assertThat(client().privateEndpoints().update("ep-1").build()
                .createResponse((String) null).data()).isNull();
        assertThat(client().privateEndpoints().delete("ep-1").build().createResponse("").id()).isNull();
        assertThat(client().privateEndpoints().activate("ep-1").build()
                .createResponse((String) null).data()).isNull();
        assertThat(client().privateEndpoints().disable("ep-1").build().createResponse("").data())
                .isNull();
        assertThat(client().privateEndpoints().enable("ep-1").build()
                .createResponse((String) null).data()).isNull();
        assertThat(client().privateEndpoints().validate("ep-1", "ws-1").build()
                .createResponse("").data()).isNull();
    }

    @Test
    void readBackAccessorsOnRequests() {
        OpenRouterPrivateEndpointCreateRequest create = client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u")
                .baseUrl("https://x.example")
                .declaredParameters(List.of("seed"))
                .declaredRegion("us").declaredZdr(true)
                .maxCompletionTokens(16384)
                .settings(OpenRouterPrivateEndpointSettingsUpdate.builder()
                        .contextLength(128000).build())
                .activate("ws-1")
                .build();
        assertThat(create.modelPermalslug()).isEqualTo("m");
        assertThat(create.providerSlug()).isEqualTo("p");
        assertThat(create.upstreamModelId()).isEqualTo("u");
        assertThat(create.baseUrl()).isEqualTo("https://x.example");
        assertThat(create.declaredParameters()).containsExactly("seed");
        assertThat(create.declaredRegion()).isEqualTo("us");
        assertThat(create.declaredZdr()).isTrue();
        assertThat(create.maxCompletionTokens()).isEqualTo(16384);
        assertThat(create.activateWorkspaceId()).isEqualTo("ws-1");
        assertThat(create.settings()).isNotNull();
        assertThat(create.settings().json().get("context_length")).isEqualTo(128000);
        assertThat(create.pricing()).isNull();

        OpenRouterPrivateEndpointCreateRequest bare = client().privateEndpoints().create()
                .modelPermalslug("m").providerSlug("p").upstreamModelId("u").build();
        assertThat(bare.baseUrl()).isNull();
        assertThat(bare.declaredParameters()).isNull();
        assertThat(bare.declaredRegion()).isNull();
        assertThat(bare.declaredZdr()).isNull();
        assertThat(bare.maxCompletionTokens()).isNull();
        assertThat(bare.settings()).isNull();
        assertThat(bare.activateWorkspaceId()).isNull();

        OpenRouterPrivateEndpointUpdateRequest update = client().privateEndpoints().update("ep-1")
                .baseUrl("https://x.example").upstreamModelId("u2")
                .declaredRegion(null).declaredZdr(null)
                .declaredParameters(List.of("tools"))
                .build();
        assertThat(update.id()).isEqualTo("ep-1");
        assertThat(update.baseUrl()).isEqualTo("https://x.example");
        assertThat(update.upstreamModelId()).isEqualTo("u2");
        assertThat(update.declaredParameters()).containsExactly("tools");
        assertThat(update.pricing()).isNull();

        assertThat(client().privateEndpoints().delete("ep-1").draftOnly(true).build().draftOnly())
                .isTrue();
        assertThat(client().privateEndpoints().delete("ep-1").build().draftOnly()).isNull();
        assertThat(client().privateEndpoints().get("a b").build().id()).isEqualTo("a+b");
    }
}
