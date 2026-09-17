package de.entwicklertraining.openrouter4j.oauth;

import de.entwicklertraining.openrouter4j.OpenRouterClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the JWKS read request shape (GET /oauth/jwks) and the JWK Set
 * response accessors against the RFC 7517 shape OpenRouter documents.
 */
class OpenRouterJwksTest {

    private OpenRouterClient client() {
        return new OpenRouterClient();
    }

    @Test
    void requestUsesGetMethodOnOauthJwksUrlWithNoBody() {
        OpenRouterJwksRequest request = new OpenRouterJwksRequest.Builder(client()).build();

        assertThat(request.getRelativeUrl()).isEqualTo("/oauth/jwks");
        assertThat(request.getHttpMethod()).isEqualTo("GET");
        assertThat(request.getBody()).isNull();
    }

    @Test
    void clientEntryPointBuildsTheRequest() {
        assertThat(client().oauthJwks().build().getRelativeUrl()).isEqualTo("/oauth/jwks");
    }

    @Test
    void responseExposesTypedJwkEntries() {
        OpenRouterJwksResponse response = new OpenRouterJwksRequest.Builder(client()).build()
                .createResponse("{\"keys\":["
                        + "{\"kty\":\"RSA\",\"use\":\"sig\",\"alg\":\"RS256\","
                        + "\"kid\":\"2026-09\",\"n\":\"0vx7agoebGcQ\",\"e\":\"AQAB\"},"
                        + "{\"kty\":\"RSA\",\"use\":\"sig\",\"alg\":\"RS256\","
                        + "\"kid\":\"2026-10\",\"n\":\"sXchBewbU\",\"e\":\"AQAB\"}]}");

        assertThat(response.keys()).hasSize(2);
        OpenRouterJwk first = response.keys().get(0);
        assertThat(first.keyType()).isEqualTo("RSA");
        assertThat(first.use()).isEqualTo("sig");
        assertThat(first.algorithm()).isEqualTo("RS256");
        assertThat(first.keyId()).isEqualTo("2026-09");
        assertThat(first.modulusBase64Url()).isEqualTo("0vx7agoebGcQ");
        assertThat(first.exponentBase64Url()).isEqualTo("AQAB");
        assertThat(response.key("2026-10").keyId()).isEqualTo("2026-10");
        assertThat(response.key("missing")).isNull();
        assertThat(response.key(null)).isNull();
    }

    @Test
    void responseAccessorsSwallowMalformedBody() {
        OpenRouterJwksResponse response = new OpenRouterJwksRequest.Builder(client()).build()
                .createResponse("{}");

        assertThat(response.keys()).isEmpty();
        assertThat(response.key("kid")).isNull();
    }
}
