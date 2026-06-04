package de.entwicklertraining.openrouter4j.chat.completion;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenRouterChatCompletionRequestTest {

    private OpenRouterChatCompletionRequest.Builder baseBuilder() {
        return OpenRouterChatCompletionRequest.builder(new OpenRouterClient())
                .model("test/model")
                .addMessage("user", "Hello");
    }

    private JSONObject bodyOf(OpenRouterChatCompletionRequest.Builder builder) {
        return new JSONObject(builder.build().getBody());
    }

    @Test
    void noProviderObjectWhenNothingIsSet() {
        JSONObject body = bodyOf(baseBuilder());
        assertThat(body.has("provider")).isFalse();
    }

    @Test
    void providerObjectWithOrderOnly() {
        JSONObject body = bodyOf(baseBuilder().provider("alibaba", "deepseek"));

        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getJSONArray("order").toList()).containsExactly("alibaba", "deepseek");
        assertThat(provider.has("require_parameters")).isFalse();
        assertThat(provider.has("allow_fallbacks")).isFalse();
    }

    @Test
    void providerObjectWithRequireParametersOnly() {
        JSONObject body = bodyOf(baseBuilder().requireParameters(true));

        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getBoolean("require_parameters")).isTrue();
        assertThat(provider.has("order")).isFalse();
        assertThat(provider.has("allow_fallbacks")).isFalse();
    }

    @Test
    void providerObjectWithAllowFallbacksOnly() {
        JSONObject body = bodyOf(baseBuilder().allowFallbacks(false));

        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getBoolean("allow_fallbacks")).isFalse();
        assertThat(provider.has("order")).isFalse();
        assertThat(provider.has("require_parameters")).isFalse();
    }

    @Test
    void providerObjectWithAllOptionsCombined() {
        JSONObject body = bodyOf(baseBuilder()
                .provider("alibaba")
                .requireParameters(true)
                .allowFallbacks(false));

        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getJSONArray("order").toList()).containsExactly("alibaba");
        assertThat(provider.getBoolean("require_parameters")).isTrue();
        assertThat(provider.getBoolean("allow_fallbacks")).isFalse();
    }

    @Test
    void explicitFalseRequireParametersIsSerialized() {
        JSONObject body = bodyOf(baseBuilder().requireParameters(false));

        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getBoolean("require_parameters")).isFalse();
    }

    @Test
    void explicitTrueAllowFallbacksIsSerialized() {
        JSONObject body = bodyOf(baseBuilder().allowFallbacks(true));

        JSONObject provider = body.getJSONObject("provider");
        assertThat(provider.getBoolean("allow_fallbacks")).isTrue();
    }

    @Test
    void requestAccessorsExposeProviderRoutingOptions() {
        OpenRouterChatCompletionRequest request = baseBuilder()
                .provider("alibaba")
                .requireParameters(true)
                .allowFallbacks(false)
                .build();

        assertThat(request.providers()).containsExactly("alibaba");
        assertThat(request.requireParameters()).isTrue();
        assertThat(request.allowFallbacks()).isFalse();
    }

    @Test
    void unsetProviderRoutingOptionsAreNull() {
        OpenRouterChatCompletionRequest request = baseBuilder().build();

        assertThat(request.requireParameters()).isNull();
        assertThat(request.allowFallbacks()).isNull();
    }
}
