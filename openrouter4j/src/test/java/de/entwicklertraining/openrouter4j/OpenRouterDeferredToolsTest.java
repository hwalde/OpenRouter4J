package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Request-JSON level tests for the {@code deferred_tools} value family: the
 * discriminated search strategy union and the control object's builder
 * contract.
 */
class OpenRouterDeferredToolsTest {

    private static final JSONObject EMPTY = new JSONObject();

    // ------------------------------------------------------------------
    // Search strategy factories
    // ------------------------------------------------------------------

    @Test
    void regexFactoryEmitsTheDiscriminatorAndOptionalMaxResults() {
        JSONObject plain = OpenRouterDeferredToolsSearchStrategy.regex().toJson();
        assertThat(plain.keySet()).containsExactly("type");
        assertThat(plain.getString("type")).isEqualTo("regex");

        JSONObject capped = OpenRouterDeferredToolsSearchStrategy.regex(5).toJson();
        assertThat(capped.keySet()).containsExactlyInAnyOrder("type", "max_results");
        assertThat(capped.getInt("max_results")).isEqualTo(5);
    }

    @Test
    void bm25FactoryEmitsTheDiscriminatorAndOptionalMaxResults() {
        JSONObject plain = OpenRouterDeferredToolsSearchStrategy.bm25().toJson();
        assertThat(plain.keySet()).containsExactly("type");
        assertThat(plain.getString("type")).isEqualTo("bm25");

        JSONObject capped = OpenRouterDeferredToolsSearchStrategy.bm25(3).toJson();
        assertThat(capped.keySet()).containsExactlyInAnyOrder("type", "max_results");
        assertThat(capped.getInt("max_results")).isEqualTo(3);
    }

    @Test
    void customFactoryEmitsToolIdAndResultKey() {
        JSONObject json = OpenRouterDeferredToolsSearchStrategy.custom("search_catalog", "tool_names").toJson();
        assertThat(json.keySet()).containsExactlyInAnyOrder("type", "tool_id", "result_key");
        assertThat(json.getString("type")).isEqualTo("custom");
        assertThat(json.getString("tool_id")).isEqualTo("search_catalog");
        assertThat(json.getString("result_key")).isEqualTo("tool_names");

        JSONObject capped = OpenRouterDeferredToolsSearchStrategy.custom("search_catalog", "tool_names", 5).toJson();
        assertThat(capped.keySet()).containsExactlyInAnyOrder("type", "tool_id", "result_key", "max_results");
        assertThat(capped.getInt("max_results")).isEqualTo(5);
    }

    @Test
    void customFactoryRejectsMissingIdentities() {
        assertThatThrownBy(() -> OpenRouterDeferredToolsSearchStrategy.custom(null, "tool_names"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("toolId");
        assertThatThrownBy(() -> OpenRouterDeferredToolsSearchStrategy.custom("", "tool_names"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("toolId");
        assertThatThrownBy(() -> OpenRouterDeferredToolsSearchStrategy.custom("search_catalog", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("resultKey");
        assertThatThrownBy(() -> OpenRouterDeferredToolsSearchStrategy.custom("search_catalog", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("resultKey");
    }

    @Test
    void rawHatchPassesTheObjectVerbatimAndIsADetachedCopy() {
        JSONObject source = new JSONObject()
                .put("type", "bm25")
                .put("max_results", 5)
                .put("spread", true);
        OpenRouterDeferredToolsSearchStrategy strategy = OpenRouterDeferredToolsSearchStrategy.raw(source);

        source.put("max_results", 99);
        JSONObject json = strategy.toJson();
        assertThat(json.getInt("max_results")).isEqualTo(5);
        assertThat(json.getBoolean("spread")).isTrue();
        assertThat(strategy.option("spread")).isEqualTo(true);
        assertThat(strategy.maxResults()).isEqualTo(5);

        JSONObject emitted = strategy.toJson();
        emitted.put("max_results", 7);
        assertThat(strategy.toJson().getInt("max_results")).isEqualTo(5);
    }

    @Test
    void rawHatchRequiresTypeAndRejectsNull() {
        assertThatThrownBy(() -> OpenRouterDeferredToolsSearchStrategy.raw(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> OpenRouterDeferredToolsSearchStrategy.raw(EMPTY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type");
    }

    @Test
    void strategyAccessorsSwallowMalformedValues() {
        JSONObject malformed = new JSONObject()
                .put("type", 42)
                .put("max_results", "five")
                .put("tool_id", false)
                .put("result_key", new JSONObject());
        OpenRouterDeferredToolsSearchStrategy strategy = OpenRouterDeferredToolsSearchStrategy.raw(malformed);

        assertThat(strategy.type()).isNull();
        assertThat(strategy.maxResults()).isNull();
        assertThat(strategy.toolId()).isNull();
        assertThat(strategy.resultKey()).isNull();
    }

    // ------------------------------------------------------------------
    // Control object
    // ------------------------------------------------------------------

    private OpenRouterDeferredToolsControl.Builder minimalControl() {
        return OpenRouterDeferredToolsControl.builder()
                .protocol("v1")
                .profile("portable")
                .validation("runtime")
                .search(OpenRouterDeferredToolsSearchStrategy.bm25(5));
    }

    @Test
    void minimalRequiredFormEmitsExactlyTheFourRequiredKeys() {
        JSONObject json = minimalControl().build().toJson();

        assertThat(json.keySet()).containsExactlyInAnyOrder("protocol", "profile", "validation", "search");
        assertThat(json.getString("protocol")).isEqualTo("v1");
        assertThat(json.getString("profile")).isEqualTo("portable");
        assertThat(json.getString("validation")).isEqualTo("runtime");
        assertThat(json.getJSONObject("search").getString("type")).isEqualTo("bm25");
        assertThat(json.getJSONObject("search").getInt("max_results")).isEqualTo(5);
    }

    @Test
    void missingRequiredFieldsAreRejectedAtBuild() {
        assertThatThrownBy(() -> OpenRouterDeferredToolsControl.builder()
                .profile("portable").validation("runtime")
                .search(OpenRouterDeferredToolsSearchStrategy.regex()).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protocol");
        assertThatThrownBy(() -> OpenRouterDeferredToolsControl.builder()
                .protocol("v1").validation("runtime")
                .search(OpenRouterDeferredToolsSearchStrategy.regex()).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("profile");
        assertThatThrownBy(() -> OpenRouterDeferredToolsControl.builder()
                .protocol("v1").profile("portable")
                .search(OpenRouterDeferredToolsSearchStrategy.regex()).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("validation");
        assertThatThrownBy(() -> OpenRouterDeferredToolsControl.builder()
                .protocol("v1").profile("portable").validation("runtime").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("search");
    }

    @Test
    void closedEnumsAreValidatedLoudly() {
        assertThatThrownBy(() -> OpenRouterDeferredToolsControl.builder().protocol("v2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("v1");
        assertThatThrownBy(() -> OpenRouterDeferredToolsControl.builder().validation("client"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("runtime");
        assertThatThrownBy(() -> OpenRouterDeferredToolsControl.builder().profile(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void openEnumsAndHintAreEmittedWhenSet() {
        JSONObject json = minimalControl()
                .profile("universal")
                .minDeferredTools(3)
                .scope("mcp")
                .cascadeMode("native")
                .interfaceMode("code_mode")
                .hint("  use the jira tools  ")
                .announceCatalogChanges(true)
                .build().toJson();

        assertThat(json.getString("profile")).isEqualTo("universal");
        assertThat(json.getInt("min_deferred_tools")).isEqualTo(3);
        assertThat(json.getString("scope")).isEqualTo("mcp");
        assertThat(json.getString("cascade_mode")).isEqualTo("native");
        assertThat(json.getString("interface")).isEqualTo("code_mode");
        assertThat(json.getString("hint")).isEqualTo("  use the jira tools  ");
        assertThat(json.getBoolean("announce_catalog_changes")).isTrue();

        JSONObject unknownSpellings = minimalControl()
                .profile("universal")
                .scope("client")
                .cascadeMode("future-mode")
                .interfaceMode("future-interface")
                .build().toJson();
        assertThat(unknownSpellings.getString("scope")).isEqualTo("client");
        assertThat(unknownSpellings.getString("cascade_mode")).isEqualTo("future-mode");
        assertThat(unknownSpellings.getString("interface")).isEqualTo("future-interface");
    }

    @Test
    void announceCatalogChangesFalseIsASetOption() {
        JSONObject json = minimalControl().announceCatalogChanges(false).build().toJson();
        assertThat(json.has("announce_catalog_changes")).isTrue();
        assertThat(json.getBoolean("announce_catalog_changes")).isFalse();
    }

    @Test
    void hintIsValidatedAgainstTheTrimmedLength() {
        assertThatThrownBy(() -> minimalControl().hint("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hint");
        assertThatThrownBy(() -> minimalControl().hint("x".repeat(2001)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2000");
        assertThat(minimalControl().hint("x".repeat(2000)).build().toJson().getString("hint"))
                .hasSize(2000);
    }

    @Test
    void setThenNullUnsetsEveryOptionalField() {
        JSONObject json = minimalControl()
                .minDeferredTools(3)
                .scope("mcp")
                .cascadeMode("native")
                .interfaceMode("code_mode")
                .hint("hint text")
                .announceCatalogChanges(true)
                .minDeferredTools(null)
                .scope(null)
                .cascadeMode(null)
                .interfaceMode(null)
                .hint(null)
                .announceCatalogChanges(null)
                .build().toJson();

        assertThat(json.keySet()).containsExactlyInAnyOrder("protocol", "profile", "validation", "search");
    }

    @Test
    void readBackAccessorsReturnTheConfiguredValues() {
        OpenRouterDeferredToolsControl control = minimalControl()
                .minDeferredTools(3)
                .scope("mcp")
                .cascadeMode("native")
                .interfaceMode("code_mode")
                .hint("hint text")
                .announceCatalogChanges(false)
                .option("future_key", "v")
                .build();

        assertThat(control.protocol()).isEqualTo("v1");
        assertThat(control.profile()).isEqualTo("portable");
        assertThat(control.validation()).isEqualTo("runtime");
        assertThat(control.search().type()).isEqualTo("bm25");
        assertThat(control.minDeferredTools()).isEqualTo(3);
        assertThat(control.scope()).isEqualTo("mcp");
        assertThat(control.cascadeMode()).isEqualTo("native");
        assertThat(control.interfaceMode()).isEqualTo("code_mode");
        assertThat(control.hint()).isEqualTo("hint text");
        assertThat(control.announceCatalogChanges()).isFalse();
        assertThat(control.option("future_key")).isEqualTo("v");
        assertThat(control.option("absent")).isNull();
    }

    @Test
    void optionHatchRejectsTypedKeysAndPassesOthersVerbatim() {
        assertThatThrownBy(() -> minimalControl().option("protocol", "v1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("typed setter");
        assertThatThrownBy(() -> minimalControl().option("interface", "wrappers"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("typed setter");
        assertThatThrownBy(() -> minimalControl().option(null, "v"))
                .isInstanceOf(IllegalArgumentException.class);

        JSONObject json = minimalControl()
                .option("future_key", "v")
                .option("future_null", null)
                .build().toJson();
        assertThat(json.getString("future_key")).isEqualTo("v");
        assertThat(json.has("future_null")).isTrue();
        assertThat(json.isNull("future_null")).isTrue();
    }

    @Test
    void searchSetThenNullFailsTheRequiredCheckAgain() {
        assertThatThrownBy(() -> minimalControl().search(null).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("search");
    }

    @Test
    void detailsViewReadsANullArgumentAsAnEmptyObject() {
        OpenRouterDeferredToolsDetails details = new OpenRouterDeferredToolsDetails(null);

        assertThat(details.turns()).isNull();
        assertThat(details.promptTokens()).isNull();
        assertThat(details.completionTokens()).isNull();
        assertThat(details.totalTokens()).isNull();
        assertThat(details.json().keySet()).isEmpty();
    }
}
