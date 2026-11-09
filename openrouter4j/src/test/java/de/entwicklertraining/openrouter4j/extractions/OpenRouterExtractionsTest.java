package de.entwicklertraining.openrouter4j.extractions;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import de.entwicklertraining.openrouter4j.OpenRouterPercentileCutoffs;
import de.entwicklertraining.openrouter4j.OpenRouterTraceConfig;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the Extractions (alpha) surface at the request-JSON level (the nine
 * field spec types, assets, the full provider preferences, the optional
 * fields, loud validation) and the response accessors against the wire
 * shapes published in the OpenAPI spec (the route's own response example
 * plus one fixture per answer type).
 */
class OpenRouterExtractionsTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    private OpenRouterExtractionsRequest.Builder baseBuilder() {
        return client().extractions()
                .model("cloudflare/clairic")
                .state("Invoice INV-2291 issued 2026-09-02, net 30.")
                .field("invoice_number", OpenRouterExtractionField.text().build());
    }

    @Test
    void requestUsesPostOnTheExtractionsPath() {
        OpenRouterExtractionsRequest request = baseBuilder().build();
        assertThat(request.getRelativeUrl()).isEqualTo("/extractions");
        assertThat(request.getHttpMethod()).isEqualTo("POST");
    }

    @Test
    void minimalRequestBodyCarriesModelStateAndFields() {
        JSONObject body = new JSONObject(baseBuilder().build().getBody());
        assertThat(body.keySet()).containsExactlyInAnyOrder("model", "state", "fields");
        assertThat(body.getString("model")).isEqualTo("cloudflare/clairic");
        assertThat(body.getString("state")).isEqualTo("Invoice INV-2291 issued 2026-09-02, net 30.");
        JSONObject fields = body.getJSONObject("fields");
        assertThat(fields.keySet()).containsExactly("invoice_number");
        assertThat(fields.getJSONObject("invoice_number").toMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of("type", "text"));
        // Unset optional fields stay out of the JSON.
        assertThat(body.has("assets")).isFalse();
        assertThat(body.has("provider")).isFalse();
        assertThat(body.has("reference_date")).isFalse();
        assertThat(body.has("session_id")).isFalse();
        assertThat(body.has("user")).isFalse();
        assertThat(body.has("trace")).isFalse();
    }

    @Test
    void simpleFieldSpecsEmitOnlyTheirDocumentedKeys() {
        JSONObject fields = new JSONObject(client().extractions()
                .model("m")
                .state("s")
                .field("a", OpenRouterExtractionField.text().build())
                .field("b", OpenRouterExtractionField.date()
                        .instructions("Issue date plus the net payment days").nullThreshold(0.3).build())
                .field("c", OpenRouterExtractionField.time().build())
                .field("d", OpenRouterExtractionField.datetime().nullThreshold(0.0).build())
                .build()
                .getBody())
                .getJSONObject("fields");

        assertThat(fields.getJSONObject("a").keySet()).containsExactly("type");
        assertThat(fields.getJSONObject("b").keySet())
                .containsExactlyInAnyOrder("type", "instructions", "null_threshold");
        assertThat(fields.getJSONObject("b").getString("instructions"))
                .isEqualTo("Issue date plus the net payment days");
        assertThat(fields.getJSONObject("b").getDouble("null_threshold")).isEqualTo(0.3);
        assertThat(fields.getJSONObject("c").getString("type")).isEqualTo("time");
        // the 0 boundary of null_threshold passes and is emitted
        assertThat(fields.getJSONObject("d").getDouble("null_threshold")).isEqualTo(0.0);
        assertThat(fields.getJSONObject("a").getString("type")).isEqualTo("text");
        assertThat(fields.getJSONObject("b").getString("type")).isEqualTo("date");
        assertThat(fields.getJSONObject("d").getString("type")).isEqualTo("datetime");
    }

    @Test
    void numberAndDurationSpecsEmitTheirWireShapes() {
        JSONObject fields = new JSONObject(client().extractions()
                .model("m")
                .state("s")
                .field("total", OpenRouterExtractionField.number()
                        .decimals(2).integer(false).unit("USD").build())
                .field("gap", OpenRouterExtractionField.duration()
                        .decimals(0).integer(true).unit("days").build())
                .field("bare", OpenRouterExtractionField.number().build())
                .build()
                .getBody())
                .getJSONObject("fields");

        assertThat(fields.getJSONObject("total").toMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "type", "number", "decimals", 2, "integer", false, "unit", "USD"));
        assertThat(fields.getJSONObject("gap").toMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "type", "duration", "decimals", 0, "integer", true, "unit", "days"));
        assertThat(fields.getJSONObject("bare").keySet()).containsExactly("type");
    }

    @Test
    void listSpecEmitsItemsAndDecimalsWithUnknownItemsPassThrough() {
        JSONObject fields = new JSONObject(client().extractions()
                .model("m")
                .state("s")
                .field("contacts", OpenRouterExtractionField.list().items("text").build())
                .field("amounts", OpenRouterExtractionField.list().items("widget").decimals(4).build())
                .build()
                .getBody())
                .getJSONObject("fields");

        assertThat(fields.getJSONObject("contacts").toMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of("type", "list", "items", "text"));
        // unknown items spellings pass through verbatim
        assertThat(fields.getJSONObject("amounts").toMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "type", "list", "items", "widget", "decimals", 4));
    }

    @Test
    void estimateSpecEmitsItsWireShape() {
        JSONObject fields = new JSONObject(client().extractions()
                .model("m")
                .state("s")
                .field("days_to_pay", OpenRouterExtractionField.estimate()
                        .min(0).max(120).step(1).unit("days").build())
                .field("total", OpenRouterExtractionField.estimate()
                        .min("100.00").max("200.00").scale("logarithmic").step("month")
                        .instructions("the invoice total").nullThreshold(0.2).build())
                .build()
                .getBody())
                .getJSONObject("fields");

        assertThat(fields.getJSONObject("days_to_pay").toMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "type", "estimate", "min", 0, "max", 120, "step", 1, "unit", "days"));
        // min/max travel as strings when set as strings, and unknown scale
        // spellings pass through verbatim (numbers are compared by value:
        // the JSON parser picks the numeric Java type)
        JSONObject totalSpec = fields.getJSONObject("total");
        assertThat(totalSpec.keySet()).containsExactlyInAnyOrder(
                "type", "min", "max", "scale", "step", "instructions", "null_threshold");
        assertThat(totalSpec.getString("min")).isEqualTo("100.00");
        assertThat(totalSpec.getString("max")).isEqualTo("200.00");
        assertThat(totalSpec.getString("scale")).isEqualTo("logarithmic");
        assertThat(totalSpec.getString("step")).isEqualTo("month");
        assertThat(totalSpec.getString("instructions")).isEqualTo("the invoice total");
        assertThat(totalSpec.getDouble("null_threshold")).isEqualTo(0.2);
    }

    @Test
    void forecastSpecEmitsItsWireShape() {
        JSONObject fields = new JSONObject(client().extractions()
                .model("m")
                .state("s")
                .field("revenue", OpenRouterExtractionField.forecast()
                        .min(0).max(1000).horizon(12).interval("month")
                        .scale("linear").step("year").unit("USD").build())
                .field("tiny", OpenRouterExtractionField.forecast()
                        .min("0.00").max("1.00").horizon(1).interval("fortnight")
                        .nullThreshold(0.5).build())
                .build()
                .getBody())
                .getJSONObject("fields");

        assertThat(fields.getJSONObject("revenue").toMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "type", "forecast", "min", 0, "max", 1000,
                        "horizon", 12, "interval", "month",
                        "scale", "linear", "step", "year", "unit", "USD"));
        // unknown interval spellings pass through verbatim
        JSONObject tinySpec = fields.getJSONObject("tiny");
        assertThat(tinySpec.keySet()).containsExactlyInAnyOrder(
                "type", "min", "max", "horizon", "interval", "null_threshold");
        assertThat(tinySpec.getString("min")).isEqualTo("0.00");
        assertThat(tinySpec.getString("max")).isEqualTo("1.00");
        assertThat(tinySpec.getInt("horizon")).isEqualTo(1);
        assertThat(tinySpec.getString("interval")).isEqualTo("fortnight");
        assertThat(tinySpec.getDouble("null_threshold")).isEqualTo(0.5);
    }

    @Test
    void fieldSpecValidationIsLoudAndTheBoundariesPass() {
        assertThatThrownBy(() -> OpenRouterExtractionField.text().nullThreshold(-0.1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null_threshold");
        assertThatThrownBy(() -> OpenRouterExtractionField.text().nullThreshold(1.1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null_threshold");
        OpenRouterExtractionField.text().nullThreshold(0.0).build();
        OpenRouterExtractionField.text().nullThreshold(1.0).build();

        assertThatThrownBy(() -> OpenRouterExtractionField.number().decimals(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("decimals");
        assertThatThrownBy(() -> OpenRouterExtractionField.number().decimals(7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("decimals");
        OpenRouterExtractionField.number().decimals(0).build();
        OpenRouterExtractionField.number().decimals(6).build();

        assertThatThrownBy(() -> OpenRouterExtractionField.forecast().horizon(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("horizon");
        assertThatThrownBy(() -> OpenRouterExtractionField.forecast().horizon(65))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("horizon");
        OpenRouterExtractionField.forecast().min(0).max(1).horizon(1).interval("step").build();
        OpenRouterExtractionField.forecast().min(0).max(1).horizon(64).interval("step").build();

        assertThatThrownBy(() -> OpenRouterExtractionField.estimate().step(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("step");
        assertThatThrownBy(() -> OpenRouterExtractionField.estimate().step(-1))
                .isInstanceOf(IllegalArgumentException.class);
        OpenRouterExtractionField.estimate().min(0).max(1).step(0.5).build();
        // the step string enum is closed on the wire, so it is validated loudly
        assertThatThrownBy(() -> OpenRouterExtractionField.estimate().step("week"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("month");
        OpenRouterExtractionField.estimate().min(0).max(1).step("month").build();
        OpenRouterExtractionField.estimate().min(0).max(1).step("year").build();
    }

    @Test
    void fieldSpecsRequireTheirOptionsAtBuildTime() {
        assertThatThrownBy(() -> OpenRouterExtractionField.estimate().build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("min and max");
        assertThatThrownBy(() -> OpenRouterExtractionField.estimate().min(0).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("min and max");
        assertThatThrownBy(() -> OpenRouterExtractionField.forecast().min(0).max(1).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horizon");
        assertThatThrownBy(() -> OpenRouterExtractionField.forecast()
                .min(0).max(1).horizon(2).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horizon");
        assertThatThrownBy(() -> OpenRouterExtractionField.list().build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("items");
        assertThatThrownBy(() -> OpenRouterExtractionField.raw(new JSONObject().put("x", 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type");
        assertThatThrownBy(() -> OpenRouterExtractionField.raw(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fieldAndFieldsMapValidateTheirInput() {
        assertThatThrownBy(() -> baseBuilder().field(null, OpenRouterExtractionField.text().build()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> baseBuilder().field("", OpenRouterExtractionField.text().build()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> baseBuilder().field("k", (OpenRouterExtractionField) null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> baseBuilder().field("k", (JSONObject) null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> baseBuilder().fields((Map<String, OpenRouterExtractionField>) null))
                .isInstanceOf(IllegalArgumentException.class);

        // repeated names replace the previous spec, the raw hatch emits as written
        JSONObject fields = new JSONObject(baseBuilder()
                .field("invoice_number", OpenRouterExtractionField.number().build())
                .field("raw", new JSONObject().put("type", "future_kind").put("weird", 1))
                .build()
                .getBody())
                .getJSONObject("fields");
        assertThat(fields.getJSONObject("invoice_number").getString("type")).isEqualTo("number");
        assertThat(fields.getJSONObject("raw").getInt("weird")).isEqualTo(1);
    }

    @Test
    void assetsEmitTheAssetMapWithUnknownSpellingPassThrough() {
        OpenRouterExtractionsRequest request = baseBuilder()
                .asset("scan_1", OpenRouterExtractionAsset.of("png", "aGVsbG8=", "screenshot"))
                .asset("doc_1", OpenRouterExtractionAsset.of("webp", "aGVsbG8="))
                .asset("doc_2", OpenRouterExtractionAsset.of("pdf", "aGVsbG8=", "scan"))
                .build();
        JSONObject body = new JSONObject(request.getBody());
        JSONObject assets = body.getJSONObject("assets");
        assertThat(assets.keySet()).containsExactlyInAnyOrder("scan_1", "doc_1", "doc_2");
        assertThat(assets.getJSONObject("scan_1").toMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "format", "png", "data_base64", "aGVsbG8=", "type", "screenshot"));
        // unknown format and type spellings pass through verbatim, and the
        // optional type key stays out when unset
        assertThat(assets.getJSONObject("doc_1").toMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of(
                        "format", "webp", "data_base64", "aGVsbG8="));
        assertThat(assets.getJSONObject("doc_2").getString("type")).isEqualTo("scan");

        // read-back
        assertThat(request.assets()).containsOnlyKeys("scan_1", "doc_1", "doc_2");
        OpenRouterExtractionAsset asset = request.assets().get("scan_1");
        assertThat(asset.format()).isEqualTo("png");
        assertThat(asset.dataBase64()).isEqualTo("aGVsbG8=");
        assertThat(asset.type()).isEqualTo("screenshot");
        assertThat(asset.toJson().getString("format")).isEqualTo("png");
        assertThat(request.assets().get("doc_1").type()).isNull();

        // the map convenience merges and same references replace
        JSONObject merged = new JSONObject(baseBuilder()
                .asset("a", OpenRouterExtractionAsset.of("png", "aGVsbG8="))
                .assets(Map.of("b", OpenRouterExtractionAsset.of("jpg", "aGVsbG8=")))
                .asset("a", OpenRouterExtractionAsset.of("pdf", "aGVsbG8="))
                .build()
                .getBody());
        assertThat(merged.getJSONObject("assets").keySet()).containsExactlyInAnyOrder("a", "b");
        assertThat(merged.getJSONObject("assets").getJSONObject("a").getString("format"))
                .isEqualTo("pdf");
    }

    @Test
    void assetValidationIsLoud() {
        assertThatThrownBy(() -> OpenRouterExtractionAsset.of(null, "aGVsbG8="))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("format");
        assertThatThrownBy(() -> OpenRouterExtractionAsset.of("", "aGVsbG8="))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterExtractionAsset.of("png", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("data_base64");
        assertThatThrownBy(() -> OpenRouterExtractionAsset.of("png", ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterExtractionAsset.of("png", "not base64!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("base64");
        assertThatThrownBy(() -> baseBuilder().asset(null, OpenRouterExtractionAsset.of("png", "aGVsbG8=")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> baseBuilder().asset("ref", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> baseBuilder().assets((Map<String, OpenRouterExtractionAsset>) null))
                .isInstanceOf(IllegalArgumentException.class);
        // valid payloads pass
        OpenRouterExtractionAsset.of("png", "aGVsbG8=").toJson();
    }

    @Test
    void fullProviderPreferencesAreEmittedOnlyWhenSet() {
        OpenRouterExtractionProviderPreferences preferences =
                OpenRouterExtractionProviderPreferences.builder()
                        .order("openai", "anthropic")
                        .only("openai")
                        .ignore("azure")
                        .requireParameters(true)
                        .allowFallbacks(false)
                        .dataCollection("deny")
                        .maxPricePrompt("1.00")
                        .maxPriceCompletion("2.00")
                        .maxPriceImage("3.00")
                        .maxPriceAudio("4.00")
                        .maxPriceRequest("5.00")
                        .quantizations("fp8", "unknown-quant")
                        .sortConfig("price", "none")
                        .enforceDistillableText(true)
                        .zdr(true)
                        .preferredMaxLatency(5.0)
                        .preferredMinThroughput(OpenRouterPercentileCutoffs.builder()
                                .p50(100.0).p90(50.0).build())
                        .options(new JSONObject().put("openai", new JSONObject().put("max_tokens", 1000)))
                        .build();
        JSONObject provider = new JSONObject(baseBuilder().provider(preferences).build().getBody())
                .getJSONObject("provider");
        assertThat(provider.keySet()).containsExactlyInAnyOrder(
                "order", "only", "ignore", "require_parameters", "allow_fallbacks",
                "data_collection", "max_price", "quantizations", "sort",
                "enforce_distillable_text", "zdr",
                "preferred_max_latency", "preferred_min_throughput", "options");
        assertThat(provider.getJSONArray("order").toList()).containsExactly("openai", "anthropic");
        assertThat(provider.getJSONArray("only").toList()).containsExactly("openai");
        assertThat(provider.getJSONArray("ignore").toList()).containsExactly("azure");
        assertThat(provider.getBoolean("require_parameters")).isTrue();
        assertThat(provider.getBoolean("allow_fallbacks")).isFalse();
        assertThat(provider.getString("data_collection")).isEqualTo("deny");
        assertThat(provider.getJSONObject("max_price").keySet()).containsExactlyInAnyOrder(
                "prompt", "completion", "image", "audio", "request");
        assertThat(provider.getJSONObject("max_price").getString("prompt")).isEqualTo("1.00");
        assertThat(provider.getJSONObject("max_price").getString("completion")).isEqualTo("2.00");
        assertThat(provider.getJSONObject("max_price").getString("image")).isEqualTo("3.00");
        assertThat(provider.getJSONObject("max_price").getString("audio")).isEqualTo("4.00");
        assertThat(provider.getJSONObject("max_price").getString("request")).isEqualTo("5.00");
        assertThat(provider.getJSONArray("quantizations").toList())
                .containsExactly("fp8", "unknown-quant");
        assertThat(provider.getJSONObject("sort").toMap())
                .containsExactlyInAnyOrderEntriesOf(Map.of("by", "price", "partition", "none"));
        assertThat(provider.getBoolean("enforce_distillable_text")).isTrue();
        assertThat(provider.getBoolean("zdr")).isTrue();
        assertThat(provider.getDouble("preferred_max_latency")).isEqualTo(5.0);
        JSONObject throughputCutoffs = provider.getJSONObject("preferred_min_throughput");
        assertThat(throughputCutoffs.keySet()).containsExactlyInAnyOrder("p50", "p90");
        assertThat(throughputCutoffs.getDouble("p50")).isEqualTo(100.0);
        assertThat(throughputCutoffs.getDouble("p90")).isEqualTo(50.0);
        assertThat(provider.getJSONObject("options").getJSONObject("openai").getInt("max_tokens"))
                .isEqualTo(1000);

        // read-back
        assertThat(preferences.order()).containsExactly("openai", "anthropic");
        assertThat(preferences.only()).containsExactly("openai");
        assertThat(preferences.ignore()).containsExactly("azure");
        assertThat(preferences.requireParameters()).isTrue();
        assertThat(preferences.allowFallbacks()).isFalse();
        assertThat(preferences.dataCollection()).isEqualTo("deny");
        assertThat(preferences.maxPricePrompt()).isEqualTo("1.00");
        assertThat(preferences.maxPriceCompletion()).isEqualTo("2.00");
        assertThat(preferences.maxPriceImage()).isEqualTo("3.00");
        assertThat(preferences.maxPriceAudio()).isEqualTo("4.00");
        assertThat(preferences.maxPriceRequest()).isEqualTo("5.00");
        assertThat(preferences.quantizations()).containsExactly("fp8", "unknown-quant");
        assertThat(preferences.sortBy()).isEqualTo("price");
        assertThat(preferences.sortPartition()).isEqualTo("none");
        assertThat(preferences.enforceDistillableText()).isTrue();
        assertThat(preferences.zdr()).isTrue();
        assertThat(preferences.preferredMaxLatency()).isEqualTo(5.0);
        assertThat(preferences.preferredMinThroughputCutoffs().p50()).isEqualTo(100.0);
        assertThat(preferences.preferredMinThroughputCutoffs().p90()).isEqualTo(50.0);
        assertThat(preferences.options().getJSONObject("openai").getInt("max_tokens"))
                .isEqualTo(1000);

        // an unset (or empty) preferences object emits no provider key at all
        assertThat(new JSONObject(baseBuilder().build().getBody()).has("provider")).isFalse();
        assertThat(new JSONObject(baseBuilder()
                .provider(OpenRouterExtractionProviderPreferences.builder().build())
                .build().getBody()).has("provider")).isFalse();
    }

    @Test
    void providerFormsPreferTheObjectFormAndValidateSlugLists() {
        // the object form of sort wins over the plain string, the plain
        // number wins over the percentile cutoffs form
        JSONObject provider = new JSONObject(baseBuilder()
                .provider(OpenRouterExtractionProviderPreferences.builder()
                        .sort("throughput")
                        .sortConfig("price", "model")
                        .preferredMaxLatency(5.0)
                        .preferredMaxLatency(OpenRouterPercentileCutoffs.builder().p50(9.0).build())
                        .preferredMinThroughput(100.0)
                        .preferredMinThroughput(OpenRouterPercentileCutoffs.builder().p50(50.0).build())
                        .build())
                .build()
                .getBody())
                .getJSONObject("provider");
        assertThat(provider.getJSONObject("sort").getString("by")).isEqualTo("price");
        assertThat(provider.getDouble("preferred_max_latency")).isEqualTo(5.0);
        assertThat(provider.getDouble("preferred_min_throughput")).isEqualTo(100.0);

        // the plain-string sort and the cutoffs forms work on their own
        JSONObject plain = new JSONObject(baseBuilder()
                .provider(OpenRouterExtractionProviderPreferences.builder()
                        .sort("latency")
                        .preferredMaxLatency(OpenRouterPercentileCutoffs.builder().p99(2.0).build())
                        .build())
                .build()
                .getBody())
                .getJSONObject("provider");
        assertThat(plain.getString("sort")).isEqualTo("latency");
        assertThat(plain.getJSONObject("preferred_max_latency").getDouble("p99")).isEqualTo(2.0);

        assertThatThrownBy(() -> OpenRouterExtractionProviderPreferences.builder().order())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one provider slug");
        assertThatThrownBy(() -> OpenRouterExtractionProviderPreferences.builder().only(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterExtractionProviderPreferences.builder().ignore())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterExtractionProviderPreferences.builder().quantizations())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void referenceDateSessionIdUserAndTraceAreEmittedAndReadBack() {
        OpenRouterExtractionsRequest request = baseBuilder()
                .referenceDate("2026-10-28")
                .sessionId("session-1234")
                .user("user-1")
                .trace(OpenRouterTraceConfig.builder().traceId("trace-1").build())
                .build();
        JSONObject body = new JSONObject(request.getBody());
        assertThat(body.getString("reference_date")).isEqualTo("2026-10-28");
        assertThat(body.getString("session_id")).isEqualTo("session-1234");
        assertThat(body.getString("user")).isEqualTo("user-1");
        assertThat(body.getJSONObject("trace").getString("trace_id")).isEqualTo("trace-1");

        assertThat(request.referenceDate()).isEqualTo("2026-10-28");
        assertThat(request.sessionId()).isEqualTo("session-1234");
        assertThat(request.user()).isEqualTo("user-1");
        assertThat(request.trace().traceId()).isEqualTo("trace-1");
    }

    @Test
    void sessionIdUserAndReferenceDateValidateLoudly() {
        assertThatThrownBy(() -> baseBuilder().referenceDate("2026-1-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("YYYY-MM-DD");
        assertThatThrownBy(() -> baseBuilder().referenceDate("not-a-date"))
                .isInstanceOf(IllegalArgumentException.class);
        baseBuilder().referenceDate(null).build();
        baseBuilder().referenceDate("2026-13-45").build(); // the pattern passes; the API rejects semantics

        assertThatThrownBy(() -> baseBuilder().sessionId("a".repeat(257)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("256");
        assertThatThrownBy(() -> baseBuilder().user("a".repeat(257)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("256");
        // the 256 boundary passes, and characters are counted in code
        // points, so a surrogate pair counts once
        assertThat(baseBuilder().sessionId("a".repeat(256)).build().sessionId())
                .isEqualTo("a".repeat(256));
        assertThat(baseBuilder().user("\uD83D\uDE00".repeat(256)).build().user())
                .isEqualTo("\uD83D\uDE00".repeat(256));
        assertThatThrownBy(() -> baseBuilder().sessionId("\uD83D\uDE00".repeat(257)))
                .isInstanceOf(IllegalArgumentException.class);
        baseBuilder().sessionId(null).build();
        baseBuilder().user(null).build();
    }

    @Test
    void buildRejectsMissingModelStateAndFields() {
        assertThatThrownBy(() -> client().extractions()
                .state("s").field("k", OpenRouterExtractionField.text().build()).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("model");
        assertThatThrownBy(() -> client().extractions()
                .model("m").field("k", OpenRouterExtractionField.text().build()).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("state");
        assertThatThrownBy(() -> client().extractions().model("m").state("s").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("field");
    }

    @Test
    void requestReadBackAccessors() {
        OpenRouterExtractionsRequest request = baseBuilder()
                .asset("scan", OpenRouterExtractionAsset.of("png", "aGVsbG8="))
                .provider(OpenRouterExtractionProviderPreferences.builder().zdr(true).build())
                .fields(Map.of("total", OpenRouterExtractionField.number().build()))
                .build();
        assertThat(request.model()).isEqualTo("cloudflare/clairic");
        assertThat(request.state()).isEqualTo("Invoice INV-2291 issued 2026-09-02, net 30.");
        assertThat(request.fields()).containsOnlyKeys("invoice_number", "total");
        assertThat(request.fields().get("invoice_number").type()).isEqualTo("text");
        assertThat(request.fields().get("total").type()).isEqualTo("number");
        assertThat(new JSONObject(request.getBody()).getJSONObject("fields").keySet())
                .containsExactlyInAnyOrder("invoice_number", "total");
        assertThat(request.assets()).containsOnlyKeys("scan");
        assertThat(request.provider().zdr()).isTrue();
        assertThat(request.referenceDate()).isNull();
        assertThat(request.sessionId()).isNull();
        assertThat(request.user()).isNull();
        assertThat(request.trace()).isNull();

        OpenRouterExtractionsRequest bare = client().extractions()
                .model("m").state("").field("k", OpenRouterExtractionField.text().build()).build();
        assertThat(bare.state()).isEmpty();
        assertThat(bare.assets()).isEmpty();
        assertThat(bare.provider()).isNull();
    }

    @Test
    void fieldSpecReadBackAccessorsExposeEveryOption() {
        OpenRouterExtractionField estimate = OpenRouterExtractionField.estimate()
                .instructions("the invoice total").nullThreshold(0.4)
                .min("100.00").max(200).scale("log").step("year").unit("USD").build();
        assertThat(estimate.type()).isEqualTo("estimate");
        assertThat(estimate.instructions()).isEqualTo("the invoice total");
        assertThat(estimate.nullThreshold()).isEqualTo(0.4);
        assertThat(estimate.min()).isEqualTo("100.00");
        assertThat(estimate.max()).isEqualTo(200);
        assertThat(estimate.scale()).isEqualTo("log");
        assertThat(estimate.step()).isEqualTo("year");
        assertThat(estimate.unit()).isEqualTo("USD");
        assertThat(estimate.decimals()).isNull();
        assertThat(estimate.integer()).isNull();
        assertThat(estimate.items()).isNull();
        assertThat(estimate.horizon()).isNull();
        assertThat(estimate.interval()).isNull();

        OpenRouterExtractionField forecast = OpenRouterExtractionField.forecast()
                .min(0).max(1).step(2.5).horizon(3).interval("day").build();
        assertThat(forecast.horizon()).isEqualTo(3);
        assertThat(forecast.interval()).isEqualTo("day");
        assertThat(forecast.step()).isEqualTo(2.5);

        OpenRouterExtractionField list = OpenRouterExtractionField.list()
                .items("number").decimals(6).build();
        assertThat(list.items()).isEqualTo("number");
        assertThat(list.decimals()).isEqualTo(6);

        OpenRouterExtractionField number = OpenRouterExtractionField.number()
                .integer(true).build();
        assertThat(number.integer()).isTrue();
        assertThat(number.toJson().keySet()).containsExactlyInAnyOrder("type", "integer");

        // the raw hatch keeps unknown keys verbatim
        OpenRouterExtractionField raw = OpenRouterExtractionField.raw(
                new JSONObject().put("type", "future_kind").put("weird", 1));
        assertThat(raw.type()).isEqualTo("future_kind");
        assertThat(raw.toJson().getInt("weird")).isEqualTo(1);
    }

    /**
     * Fixture: the response example published with the
     * {@code POST /api/alpha/extractions} route in the OpenAPI spec.
     */
    private static final String SPEC_EXAMPLE_FIXTURE = """
            {
              "answers": {
                "contacts": {"confidence": 0.9999, "spans": [[53, 62], [64, 74]],
                             "type": "list", "value": ["Dana Ruiz", "Priya Shah"]},
                "days_to_pay": {"mean": 29.5, "median": 30, "p10": 21, "p90": 38,
                                "type": "estimate", "value": 30},
                "due_date": {"guess": "2026-10-02", "p_null": 1.9e-8,
                             "type": "date", "value": "2026-10-02"},
                "invoice_number": {"confidence": 1, "guess": "INV-2291", "p_null": 3.8e-10,
                                   "span": [8, 16], "type": "text", "value": "INV-2291"},
                "total": {"confidence": 0.9999, "guess": 1080, "p_null": 2.3e-8,
                          "type": "number", "value": 1080}
              },
              "id": "gen-dec-1793251006-X5e5eKGQdvR9rblyX250",
              "model": "cloudflare/clairic",
              "provider": "Cloudflare",
              "truncated": false,
              "usage": {"cost": 0.0000655, "image_tokens": 0, "input_tokens": 655,
                        "output_tokens": 0, "passes": 3, "schema_tokens": 472,
                        "state_tokens": 61}
            }
            """;

    @Test
    void responseExposesTheSpecExampleFields() {
        OpenRouterExtractionsResponse response = new OpenRouterExtractionsResponse(
                new JSONObject(SPEC_EXAMPLE_FIXTURE), null);

        assertThat(response.id()).isEqualTo("gen-dec-1793251006-X5e5eKGQdvR9rblyX250");
        assertThat(response.model()).isEqualTo("cloudflare/clairic");
        assertThat(response.provider()).isEqualTo("Cloudflare");
        assertThat(response.stateText()).isNull();
        assertThat(response.truncated()).isFalse();
        assertThat(response.answers()).containsOnlyKeys(
                "contacts", "days_to_pay", "due_date", "invoice_number", "total");

        OpenRouterExtractionAnswer text = response.answer("invoice_number");
        assertThat(text.name()).isEqualTo("invoice_number");
        assertThat(text.type()).isEqualTo("text");
        assertThat(text.valueString()).isEqualTo("INV-2291");
        assertThat(text.guessString()).isEqualTo("INV-2291");
        assertThat(text.pNull()).isEqualTo(3.8e-10);
        assertThat(text.confidence()).isEqualTo(1.0);
        assertThat(text.span()).containsExactly(8L, 16L);
        assertThat(text.spans()).isEmpty();
        assertThat(text.json().getString("value")).isEqualTo("INV-2291");

        OpenRouterExtractionAnswer list = response.answer("contacts");
        assertThat(list.type()).isEqualTo("list");
        assertThat(list.valueList()).containsExactly("Dana Ruiz", "Priya Shah");
        assertThat(list.spans()).containsExactly(List.of(53L, 62L), List.of(64L, 74L));
        assertThat(list.span()).isNull();
        assertThat(list.confidence()).isEqualTo(0.9999);

        OpenRouterExtractionAnswer estimate = response.answer("days_to_pay");
        assertThat(estimate.type()).isEqualTo("estimate");
        assertThat(estimate.valueNumber()).isEqualTo(30);
        // decimals arrive as whatever numeric type the JSON parser picks;
        // the accessors hand the number through untouched
        assertThat(((Number) estimate.mean()).doubleValue()).isEqualTo(29.5);
        assertThat(estimate.median()).isEqualTo(30);
        assertThat(estimate.p10()).isEqualTo(21);
        assertThat(estimate.p90()).isEqualTo(38);

        OpenRouterExtractionAnswer date = response.answer("due_date");
        assertThat(date.type()).isEqualTo("date");
        assertThat(date.valueString()).isEqualTo("2026-10-02");
        assertThat(date.guessString()).isEqualTo("2026-10-02");
        assertThat(date.pNull()).isEqualTo(1.9e-8);

        OpenRouterExtractionAnswer number = response.answer("total");
        assertThat(number.valueNumber()).isEqualTo(1080);
        assertThat(number.guessNumber()).isEqualTo(1080);

        assertThat(response.answer("missing")).isNull();

        OpenRouterExtractionsUsage usage = response.usage();
        assertThat(usage.inputTokens()).isEqualTo(655);
        assertThat(usage.outputTokens()).isEqualTo(0);
        assertThat(usage.cost()).isEqualTo(0.0000655);
        assertThat(usage.imageTokens()).isEqualTo(0);
        assertThat(usage.passes()).isEqualTo(3);
        assertThat(usage.schemaTokens()).isEqualTo(472);
        assertThat(usage.stateTokens()).isEqualTo(61);
        assertThat(usage.json().getInt("passes")).isEqualTo(3);
    }

    /**
     * Fixture: one answer per remaining type (duration, estimate with the
     * full statistics, list with per-item guesses, forecast with the
     * per-step series, a null {@code value} with {@code guess} present),
     * with unknown {@code source} spellings and a malformed span pair.
     */
    private static final String TYPED_ANSWERS_FIXTURE = """
            {
              "answers": {
                "lead_time": {"type": "duration", "value": 45, "unit": "minutes",
                              "guess": 50, "confidence": 0.8,
                              "source": "chart", "spell_confidence": 0.7},
                "total": {"type": "estimate", "value": "1080.50", "guess": "1080.00",
                          "mean": "1080.25", "median": "1080.00",
                          "p10": "1050.00", "p90": "1100.00",
                          "p_above": 0.01, "p_below": 0.02,
                          "quantiles": ["1000.00", 1001, 1002], "p_null": 0.01},
                "contacts": {"type": "list", "value": ["Dana Ruiz", "Priya Shah"],
                             "guess": ["Dana Ruiz", "Priya R."],
                             "confidences": [0.99, 0.9],
                             "spans": [[53, 62], ["bad", 74]]},
                "revenue": {"type": "forecast", "value": [100, 110], "guess": [105, 115],
                            "mean": [102, 112], "median": [101, 111],
                            "p10": [90, 100], "p90": [115, 125], "p_null": 0.01,
                            "steps": [
                              {"value": 100, "mean": 102, "median": 101,
                               "p10": 90, "p90": 115,
                               "p_above": 0.02, "p_below": 0.03,
                               "quantiles": [95, 100, 105]},
                              {"value": null, "mean": 112},
                              "not-an-object"
                            ]},
                "note": {"type": "text", "value": null, "guess": "probably", "p_null": 0.7}
              },
              "state_text": "Invoice INV-2291 issued 2026-09-02",
              "truncated": true
            }
            """;

    @Test
    void responseExposesTheTypedAnswerExtras() {
        OpenRouterExtractionsResponse response = new OpenRouterExtractionsResponse(
                new JSONObject(TYPED_ANSWERS_FIXTURE), null);
        assertThat(response.stateText()).isEqualTo("Invoice INV-2291 issued 2026-09-02");
        assertThat(response.truncated()).isTrue();

        OpenRouterExtractionAnswer duration = response.answer("lead_time");
        assertThat(duration.type()).isEqualTo("duration");
        assertThat(duration.valueNumber()).isEqualTo(45);
        assertThat(duration.guessNumber()).isEqualTo(50);
        assertThat(duration.json().getString("unit")).isEqualTo("minutes");
        // unknown source spellings pass through verbatim
        assertThat(duration.source()).isEqualTo("chart");
        assertThat(duration.spellConfidence()).isEqualTo(0.7);

        OpenRouterExtractionAnswer estimate = response.answer("total");
        assertThat(estimate.valueString()).isEqualTo("1080.50");
        assertThat(estimate.valueNumber()).isNull();
        assertThat(estimate.guessString()).isEqualTo("1080.00");
        assertThat(estimate.mean()).isEqualTo("1080.25");
        assertThat(estimate.median()).isEqualTo("1080.00");
        assertThat(estimate.p10()).isEqualTo("1050.00");
        assertThat(estimate.p90()).isEqualTo("1100.00");
        assertThat(estimate.pAbove()).isEqualTo(0.01);
        assertThat(estimate.pBelow()).isEqualTo(0.02);
        assertThat(estimate.quantiles()).containsExactly("1000.00", 1001, 1002);
        assertThat(estimate.pNull()).isEqualTo(0.01);

        OpenRouterExtractionAnswer list = response.answer("contacts");
        assertThat(list.guessList()).containsExactly("Dana Ruiz", "Priya R.");
        assertThat(list.confidences()).containsExactly(0.99, 0.9);
        // the malformed pair is dropped while the good sibling survives
        assertThat(list.spans()).containsExactly(List.of(53L, 62L));

        OpenRouterExtractionAnswer forecast = response.answer("revenue");
        assertThat(forecast.type()).isEqualTo("forecast");
        assertThat(forecast.valueList()).containsExactly(100, 110);
        assertThat(forecast.guessList()).containsExactly(105, 115);
        // the flat per-step series and the scalar forms stay apart
        assertThat(forecast.meanSeries()).containsExactly(102, 112);
        assertThat(forecast.medianSeries()).containsExactly(101, 111);
        assertThat(forecast.p10Series()).containsExactly(90, 100);
        assertThat(forecast.p90Series()).containsExactly(115, 125);
        assertThat(forecast.mean()).isNull();

        List<OpenRouterExtractionForecastStep> steps = forecast.forecast();
        assertThat(steps).hasSize(2); // the non-object entry is dropped
        OpenRouterExtractionForecastStep first = steps.get(0);
        assertThat(first.value()).isEqualTo(100);
        assertThat(first.mean()).isEqualTo(102);
        assertThat(first.median()).isEqualTo(101);
        assertThat(first.p10()).isEqualTo(90);
        assertThat(first.p90()).isEqualTo(115);
        assertThat(first.pAbove()).isEqualTo(0.02);
        assertThat(first.pBelow()).isEqualTo(0.03);
        assertThat(first.quantiles()).containsExactly(95, 100, 105);
        assertThat(first.json().getInt("value")).isEqualTo(100);
        OpenRouterExtractionForecastStep second = steps.get(1);
        assertThat(second.value()).isNull();
        assertThat(second.mean()).isEqualTo(112);
        assertThat(second.median()).isNull();
        assertThat(second.quantiles()).isEmpty();

        // value is null when the state does not determine it, guess stays
        OpenRouterExtractionAnswer note = response.answer("note");
        assertThat(note.value()).isNull();
        assertThat(note.valueString()).isNull();
        assertThat(note.guessString()).isEqualTo("probably");
        assertThat(note.pNull()).isEqualTo(0.7);
    }

    @Test
    void responseAccessorsSwallowMalformedBody() {
        OpenRouterExtractionsResponse malformed = new OpenRouterExtractionsResponse(new JSONObject("""
                {
                  "id": 42,
                  "model": {"nested": true},
                  "provider": 7,
                  "state_text": 9,
                  "truncated": "yes",
                  "answers": {
                    "good": {"type": "text", "value": "ok"},
                    "broken": "not-an-object",
                    "wrong_types": {"type": 5, "value": {"nested": 1},
                                    "p_null": "high", "confidence": true,
                                    "span": "x", "spans": [1, [1, 2]],
                                    "confidences": "none", "source": 3,
                                    "spell_confidence": "low", "guess": null,
                                    "mean": [], "quantiles": "q", "steps": "s"}
                  },
                  "usage": {"input_tokens": "many", "output_tokens": null, "cost": "free"}
                }
                """), null);

        assertThat(malformed.id()).isNull();
        assertThat(malformed.model()).isNull();
        assertThat(malformed.provider()).isNull();
        assertThat(malformed.stateText()).isNull();
        assertThat(malformed.truncated()).isNull();
        // the non-object answer is dropped while the good sibling survives
        assertThat(malformed.answers()).containsOnlyKeys("good", "wrong_types");
        assertThat(malformed.answer("broken")).isNull();

        OpenRouterExtractionAnswer wrong = malformed.answer("wrong_types");
        assertThat(wrong.name()).isEqualTo("wrong_types");
        assertThat(wrong.type()).isNull();
        assertThat(wrong.value()).isNull();
        assertThat(wrong.valueString()).isNull();
        assertThat(wrong.valueNumber()).isNull();
        assertThat(wrong.valueList()).isNull();
        assertThat(wrong.guess()).isNull();
        assertThat(wrong.pNull()).isNull();
        assertThat(wrong.confidence()).isNull();
        assertThat(wrong.source()).isNull();
        assertThat(wrong.spellConfidence()).isNull();
        assertThat(wrong.span()).isNull();
        // the good span pair survives its non-array sibling
        assertThat(wrong.spans()).containsExactly(List.of(1L, 2L));
        assertThat(wrong.confidences()).isEmpty();
        assertThat(wrong.mean()).isNull();
        assertThat(wrong.meanSeries()).isEmpty();
        assertThat(wrong.quantiles()).isEmpty();
        assertThat(wrong.forecast()).isEmpty();

        OpenRouterExtractionsUsage usage = malformed.usage();
        assertThat(usage.inputTokens()).isNull();
        assertThat(usage.outputTokens()).isNull();
        assertThat(usage.cost()).isNull();
        assertThat(usage.imageTokens()).isNull();

        // absent, empty and null bodies look like empty responses
        OpenRouterExtractionsRequest request = baseBuilder().build();
        assertThat(request.createResponse("").answers()).isEmpty();
        assertThat(request.createResponse("").usage()).isNull();
        assertThat(request.createResponse("").model()).isNull();
        assertThat(request.createResponse((String) null).answers()).isEmpty();
        assertThat(request.createResponse((String) null).usage()).isNull();
        assertThat(new OpenRouterExtractionsResponse(
                new JSONObject("{\"answers\": \"oops\", \"usage\": 3}"), null).answers()).isEmpty();
        assertThat(new OpenRouterExtractionsResponse(
                new JSONObject("{\"answers\": \"oops\", \"usage\": 3}"), null).usage()).isNull();
    }

    @Test
    void listConfidencesKeepPositionalNullPlaceholders() {
        // one entry per item of value(), same order: a malformed entry stays
        // as a positional null so the alignment survives (unlike spans(),
        // which drops malformed pairs because it has no alignment to keep)
        OpenRouterExtractionAnswer list = new OpenRouterExtractionAnswer("contacts", new JSONObject("""
                {"type": "list", "value": ["Dana Ruiz", "Priya R."], "confidences": [0.99, "bad"]}
                """));
        assertThat(list.valueList()).containsExactly("Dana Ruiz", "Priya R.");
        assertThat(list.confidences()).containsExactly(0.99, null);
    }
}
