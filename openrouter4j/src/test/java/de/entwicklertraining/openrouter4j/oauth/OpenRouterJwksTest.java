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
                        + "{\"kty\":\"EC\",\"use\":\"sig\",\"alg\":\"ES256\","
                        + "\"kid\":\"2026-09\",\"crv\":\"P-256\","
                        + "\"x\":\"MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4\","
                        + "\"y\":\"4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM\"},"
                        + "{\"kty\":\"RSA\",\"use\":\"sig\",\"alg\":\"RS256\","
                        + "\"kid\":\"2026-10\",\"n\":\"sXchBewbU\",\"e\":\"AQAB\"}]}");

        assertThat(response.keys()).hasSize(2);
        OpenRouterJwk ec = response.keys().get(0);
        assertThat(ec.keyType()).isEqualTo("EC");
        assertThat(ec.use()).isEqualTo("sig");
        assertThat(ec.algorithm()).isEqualTo("ES256");
        assertThat(ec.keyId()).isEqualTo("2026-09");
        assertThat(ec.curve()).isEqualTo("P-256");
        assertThat(ec.xCoordinateBase64Url()).isEqualTo("MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4");
        assertThat(ec.yCoordinateBase64Url()).isEqualTo("4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM");
        assertThat(ec.modulusBase64Url()).isNull();
        assertThat(ec.exponentBase64Url()).isNull();

        OpenRouterJwk rsa = response.keys().get(1);
        assertThat(rsa.keyType()).isEqualTo("RSA");
        assertThat(rsa.use()).isEqualTo("sig");
        assertThat(rsa.algorithm()).isEqualTo("RS256");
        assertThat(rsa.keyId()).isEqualTo("2026-10");
        assertThat(rsa.modulusBase64Url()).isEqualTo("sXchBewbU");
        assertThat(rsa.exponentBase64Url()).isEqualTo("AQAB");
        assertThat(rsa.curve()).isNull();
        assertThat(rsa.xCoordinateBase64Url()).isNull();
        assertThat(rsa.yCoordinateBase64Url()).isNull();

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
