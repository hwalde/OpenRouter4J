package de.entwicklertraining.openrouter4j.providers;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the routing-discovery requests and the response accessors against
 * recorded JSON shapes of the OpenRouter /providers and /endpoints/zdr
 * endpoints.
 */
class OpenRouterProvidersTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void providersRequestCarriesNoQueryAndNoBody() {
        OpenRouterProvidersRequest request = new OpenRouterProvidersRequest.Builder(client()).build();
        assertThat(request.getRelativeUrl()).isEqualTo("/providers");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void providersResponseSurfacesTypedProviderRows() {
        OpenRouterProvidersResponse response = providersResponseOf("""
                {
                  "data": [
                    {
                      "datacenters": ["US", "IE"],
                      "headquarters": "US",
                      "name": "OpenAI",
                      "privacy_policy_url": "https://openai.com/privacy",
                      "slug": "openai",
                      "status_page_url": "https://status.openai.com",
                      "terms_of_service_url": "https://openai.com/terms"
                    }
                  ]
                }
                """);

        List<OpenRouterProvider> items = response.items();
        assertThat(items).hasSize(1);

        OpenRouterProvider provider = items.get(0);
        assertThat(provider.slug()).isEqualTo("openai");
        assertThat(provider.name()).isEqualTo("OpenAI");
        assertThat(provider.headquarters()).isEqualTo("US");
        assertThat(provider.datacenters()).containsExactly("US", "IE");
        assertThat(provider.privacyPolicyUrl()).isEqualTo("https://openai.com/privacy");
        assertThat(provider.statusPageUrl()).isEqualTo("https://status.openai.com");
        assertThat(provider.termsOfServiceUrl()).isEqualTo("https://openai.com/terms");
    }

    @Test
    void providersResponseReturnsEmptyWhenDataAbsent() {
        assertThat(providersResponseOf("{}").items()).isEmpty();
    }

    @Test
    void zdrEndpointsRequestCarriesNoQueryAndNoBody() {
        OpenRouterZdrEndpointsRequest request = new OpenRouterZdrEndpointsRequest.Builder(client()).build();
        assertThat(request.getRelativeUrl()).isEqualTo("/endpoints/zdr");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void zdrEndpointsResponseSurfacesTypedEndpointRows() {
        OpenRouterZdrEndpointsResponse response = zdrResponseOf("""
                {
                  "data": [
                    {
                      "context_length": 8192,
                      "latency_last_30m": {"p50": 0.25, "p75": 0.35, "p90": 0.48, "p99": 0.85},
                      "max_completion_tokens": 4096,
                      "max_prompt_tokens": 8192,
                      "model_id": "openai/gpt-4",
                      "model_name": "GPT-4",
                      "name": "OpenAI: GPT-4",
                      "pricing": {"completion": "0.00006", "image": "0", "prompt": "0.00003", "request": "0"},
                      "provider_name": "OpenAI",
                      "quantization": "fp16",
                      "status": 0,
                      "supported_parameters": ["temperature", "top_p", "max_tokens"],
                      "supports_implicit_caching": true,
                      "supports_voice_cloning": false,
                      "tag": "openai",
                      "throughput_last_30m": {"p50": 45.2, "p75": 38.5},
                      "uptime_last_1d": 99.8,
                      "uptime_last_30m": 99.5,
                      "uptime_last_5m": 100
                    }
                  ]
                }
                """);

        List<OpenRouterZdrEndpoint> items = response.items();
        assertThat(items).hasSize(1);

        OpenRouterZdrEndpoint endpoint = items.get(0);
        assertThat(endpoint.name()).isEqualTo("OpenAI: GPT-4");
        assertThat(endpoint.modelId()).isEqualTo("openai/gpt-4");
        assertThat(endpoint.modelName()).isEqualTo("GPT-4");
        assertThat(endpoint.providerName()).isEqualTo("OpenAI");
        assertThat(endpoint.tag()).isEqualTo("openai");
        assertThat(endpoint.contextLength()).isEqualTo(8192L);
        assertThat(endpoint.maxPromptTokens()).isEqualTo(8192L);
        assertThat(endpoint.maxCompletionTokens()).isEqualTo(4096L);
        assertThat(endpoint.quantization()).isEqualTo("fp16");
        assertThat(endpoint.status()).isZero();
        assertThat(endpoint.pricing().getString("prompt")).isEqualTo("0.00003");
        assertThat(endpoint.supportedParameters()).containsExactly("temperature", "top_p", "max_tokens");
        assertThat(endpoint.supportsImplicitCaching()).isTrue();
        assertThat(endpoint.supportsVoiceCloning()).isFalse();
        assertThat(endpoint.uptimeLast5m()).isEqualTo(100.0);
        assertThat(endpoint.uptimeLast30m()).isEqualTo(99.5);
        assertThat(endpoint.uptimeLast1d()).isEqualTo(99.8);
        assertThat(endpoint.latencyP50()).isEqualTo(0.25);
        assertThat(endpoint.latencyP99()).isEqualTo(0.85);
        assertThat(endpoint.throughputP50()).isEqualTo(45.2);
        assertThat(endpoint.throughputP99()).isNull();
    }

    @Test
    void zdrEndpointsResponseReturnsEmptyWhenDataAbsent() {
        assertThat(zdrResponseOf("{}").items()).isEmpty();
    }

    @Test
    void zdrEndpointReturnsNullWhenMetricObjectsAbsent() {
        OpenRouterZdrEndpoint endpoint = new OpenRouterZdrEndpoint(new JSONObject());
        assertThat(endpoint.latencyP50()).isNull();
        assertThat(endpoint.throughputP75()).isNull();
        assertThat(endpoint.pricing()).isNull();
        assertThat(endpoint.status()).isNull();
    }

    private OpenRouterProvidersResponse providersResponseOf(String json) {
        OpenRouterProvidersRequest request = new OpenRouterProvidersRequest.Builder(client()).build();
        return request.createResponse(json);
    }

    private OpenRouterZdrEndpointsResponse zdrResponseOf(String json) {
        OpenRouterZdrEndpointsRequest request = new OpenRouterZdrEndpointsRequest.Builder(client()).build();
        return request.createResponse(json);
    }
}
